package com.imnotndesh.truehub.data.helpers

import android.content.Context
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMechanisms
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import kotlinx.coroutines.CancellationException

/**
 * Single owner of authentication for the whole app.
 *
 * Every entry point (login, account switcher, workers, widgets, app-functions, proactive/
 * reactive recovery) authenticates through here so the mechanism is consistent:
 * API keys use `auth.login_with_api_key` (key-only), passwords/TOTP use `auth.login_ex`.
 */
object SessionProvider {

    sealed interface AuthOutcome {
        data object Ready : AuthOutcome
        data class OtpRequired(val username: String) : AuthOutcome
        data object CredentialsRejected : AuthOutcome
        data object Unauthenticated : AuthOutcome
        data object Retryable : AuthOutcome
    }

    sealed interface RecoveryResult {
        data object Recovered : RecoveryResult
        data object AuthenticatedTemporary : RecoveryResult
        data class OtpRequired(val username: String) : RecoveryResult
        data object CredentialsRejected : RecoveryResult
        data object Unauthenticated : RecoveryResult
        data object Retryable : RecoveryResult
    }

    sealed interface TokenFailure {
        data class Generation(
            val message: String,
            val throwable: Throwable? = null
        ) : TokenFailure
        data class Storage(val throwable: Throwable) : TokenFailure
    }

    sealed interface OpenResult {
        data class Ready(val manager: TrueNASApiManager, val client: TrueNASClient) : OpenResult
        data class TemporaryAuthenticated(
            val manager: TrueNASApiManager,
            val client: TrueNASClient,
            val tokenFailure: TokenFailure
        ) : OpenResult
        data class OtpRequired(
            val manager: TrueNASApiManager,
            val client: TrueNASClient,
            val username: String
        ) : OpenResult
        data object Unauthenticated : OpenResult
        data object Retryable : OpenResult
    }

    suspend fun open(
        context: Context,
        server: SavedServer,
        account: SavedAccount,
        allowCredentialRecovery: Boolean = true
    ): OpenResult {
        if (!allowCredentialRecovery) return OpenResult.Unauthenticated
        val client = TrueNASClient(
            ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
        )
        if (!client.connect()) {
            client.disconnect()
            return OpenResult.Retryable
        }
        val manager = TrueNASApiManager(client, context.applicationContext)
        val outcome = authenticateWithToken(context, manager, server.id, account)
            ?: authenticate(context, manager, account)
        return when (outcome) {
            AuthOutcome.Ready -> {
                when (val tokenResult = generateToken(manager)) {
                    is ApiResult.Success -> {
                        try {
                            MultiAccountPrefs.activateLastUsedProfileThenSaveCurrentSession(
                                context,
                                server.id,
                                account.id,
                                tokenResult.data
                            )
                            OpenResult.Ready(manager, client)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            OpenResult.TemporaryAuthenticated(
                                manager,
                                client,
                                TokenFailure.Storage(error)
                            )
                        }
                    }
                    is ApiResult.Error -> OpenResult.TemporaryAuthenticated(
                        manager,
                        client,
                        TokenFailure.Generation(tokenResult.message, tokenResult.throwable)
                    )
                    is ApiResult.Loading -> OpenResult.TemporaryAuthenticated(
                        manager,
                        client,
                        TokenFailure.Generation("Token generation did not complete")
                    )
                }
            }
            is AuthOutcome.OtpRequired -> OpenResult.OtpRequired(manager, client, outcome.username)
            AuthOutcome.CredentialsRejected,
            AuthOutcome.Unauthenticated -> {
                client.disconnect()
                OpenResult.Unauthenticated
            }
            AuthOutcome.Retryable -> {
                client.disconnect()
                OpenResult.Retryable
            }
        }
    }

    private suspend fun authenticateWithToken(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        account: SavedAccount
    ): AuthOutcome? {
        val snapshot = MultiAccountPrefs.resolveAccountTokenSnapshot(context, serverId, account.id)
            ?.takeUnless { it.isExpiringSoon() }
            ?: return null
        if (!isSuccessfulBooleanResult(manager.auth.loginWithTokenAndResult(snapshot.token))) {
            return null
        }
        val identity = manager.auth.getUserDetailsWithResult()
        return if (
            identity is ApiResult.Success &&
            isValidTokenIdentity(identity.data.pw_name, account.loginMethod, account.username)
        ) {
            AuthOutcome.Ready
        } else {
            null
        }
    }

    /** Authenticates an already-connected [manager] using the account's saved credentials. */
    suspend fun authenticate(
        context: Context,
        manager: TrueNASApiManager,
        account: SavedAccount
    ): AuthOutcome {
        val (primary, secondary) = MultiAccountPrefs.getAccountCredentials(
            context,
            account.id,
            account.loginMethod
        )
        return when (account.loginMethod) {
            LoginMethod.API_KEY -> {
                if (primary.isNullOrBlank()) return AuthOutcome.Unauthenticated
                when (val result = manager.auth.loginWithApiKeyWithResult(primary)) {
                    is ApiResult.Success -> if (isSuccessfulBooleanResult(result)) {
                        AuthOutcome.Ready
                    } else {
                        AuthOutcome.CredentialsRejected
                    }
                    is ApiResult.Error -> AuthOutcome.Retryable
                    is ApiResult.Loading -> AuthOutcome.Unauthenticated
                }
            }
            LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                if (primary.isNullOrBlank() || secondary.isNullOrBlank()) {
                    return AuthOutcome.Unauthenticated
                }
                val mechanism = LoginMechanisms.AuthPasswordPlain(
                    username = primary,
                    password = secondary,
                    login_options = LoginMechanisms.LoginOptions(user_info = true)
                )
                when (val result = manager.auth.loginEx(mechanism, includeUserInfo = true)) {
                    is ApiResult.Success -> mapLoginExAuthResult(result.data)
                    is ApiResult.Error -> AuthOutcome.Retryable
                    is ApiResult.Loading -> AuthOutcome.Unauthenticated
                }
            }
        }
    }

    /** Re-authenticates and persists a fresh token. */
    suspend fun recover(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String
    ): RecoveryResult {
        val account = MultiAccountPrefs.getAccount(context, accountId) ?: return RecoveryResult.Unauthenticated
        if (account.serverId != serverId || !account.autoLoginEnabled) {
            return RecoveryResult.Unauthenticated
        }
        return when (val outcome = authenticate(context, manager, account)) {
            AuthOutcome.Ready -> {
                try {
                    if (storeToken(context, manager, serverId, accountId)) {
                        RecoveryResult.Recovered
                    } else {
                        RecoveryResult.AuthenticatedTemporary
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    RecoveryResult.AuthenticatedTemporary
                }
            }
            is AuthOutcome.OtpRequired -> RecoveryResult.OtpRequired(outcome.username)
            AuthOutcome.CredentialsRejected -> RecoveryResult.CredentialsRejected
            AuthOutcome.Unauthenticated -> RecoveryResult.Unauthenticated
            AuthOutcome.Retryable -> RecoveryResult.Retryable
        }
    }

    internal fun allowsRefreshAfterRecovery(result: RecoveryResult): Boolean {
        return result == RecoveryResult.Recovered || result == RecoveryResult.AuthenticatedTemporary
    }

    suspend fun recoverAfterDisconnect(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String
    ): RecoveryResult {
        val account = MultiAccountPrefs.getAccount(context, accountId) ?: return RecoveryResult.Unauthenticated
        if (account.serverId != serverId || !account.autoLoginEnabled) {
            return RecoveryResult.Unauthenticated
        }
        if (!manager.ensureConnected()) return RecoveryResult.Retryable

        val token = MultiAccountPrefs.resolveAccountTokenSnapshot(context, serverId, accountId)
            ?.takeUnless { it.isExpiringSoon() }
        if (token != null && isSuccessfulBooleanResult(manager.auth.loginWithTokenAndResult(token.token))) {
            val identity = manager.auth.getUserDetailsWithResult()
            if (identity is ApiResult.Success &&
                isValidTokenIdentity(identity.data.pw_name, account.loginMethod, account.username)
            ) {
                return RecoveryResult.Recovered
            }
        }

        return recover(context, manager, serverId, accountId)
    }

    /** Generates a long-lived token and stores it with freshness metadata. */
    suspend fun storeToken(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        ttlSeconds: Int = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
    ): Boolean {
        val result = generateToken(manager, ttlSeconds)
        if (result is ApiResult.Success) {
            MultiAccountPrefs.saveCurrentSession(context, serverId, accountId, result.data, ttlSeconds)
            return true
        }
        return false
    }

    private suspend fun generateToken(
        manager: TrueNASApiManager,
        ttlSeconds: Int = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
    ): ApiResult<String> {
        return manager.auth.generateTokenWithResult(Auth.TokenRequest(ttl = ttlSeconds))
    }

    internal fun isSuccessfulBooleanResult(result: ApiResult<Boolean>): Boolean {
        return result is ApiResult.Success && result.data
    }

    internal fun isValidTokenIdentity(
        authenticatedUsername: String?,
        loginMethod: LoginMethod,
        savedUsername: String
    ): Boolean {
        if (authenticatedUsername.isNullOrBlank() || authenticatedUsername == "missing") return false
        return loginMethod == LoginMethod.API_KEY ||
            authenticatedUsername.equals(savedUsername, ignoreCase = true)
    }

    internal fun mapLoginExAuthResult(result: LoginExResult): AuthOutcome {
        return when (result) {
            is LoginExResult.AuthRespSuccess -> AuthOutcome.Ready
            is LoginExResult.AuthRespOTPRequired -> AuthOutcome.OtpRequired(result.username)
            is LoginExResult.AuthRespAuthErr,
            is LoginExResult.AuthRespAuthExpired -> AuthOutcome.CredentialsRejected
            is LoginExResult.AuthRespAuthRedirect -> AuthOutcome.Unauthenticated
        }
    }
}
