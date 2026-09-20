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
        data object Unauthenticated : AuthOutcome
        data object Retryable : AuthOutcome
    }

    sealed interface OpenResult {
        data class Ready(val manager: TrueNASApiManager, val client: TrueNASClient) : OpenResult
        data class OtpRequired(
            val manager: TrueNASApiManager,
            val client: TrueNASClient,
            val username: String
        ) : OpenResult
        data object Unauthenticated : OpenResult
        data object Retryable : OpenResult
    }

    suspend fun open(context: Context, server: SavedServer, account: SavedAccount): OpenResult {
        val client = TrueNASClient(
            ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
        )
        if (!client.connect()) {
            client.disconnect()
            return OpenResult.Retryable
        }
        val manager = TrueNASApiManager(client, context.applicationContext)
        return when (val outcome = authenticate(context, manager, account)) {
            AuthOutcome.Ready -> {
                storeToken(context, manager, server.id, account.id)
                OpenResult.Ready(manager, client)
            }
            is AuthOutcome.OtpRequired -> OpenResult.OtpRequired(manager, client, outcome.username)
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
                    is ApiResult.Success ->
                        if (result.data) AuthOutcome.Ready else AuthOutcome.Unauthenticated
                    is ApiResult.Error -> AuthOutcome.Retryable
                    else -> AuthOutcome.Unauthenticated
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
                    is ApiResult.Success -> when (val data = result.data) {
                        is LoginExResult.AuthRespSuccess -> AuthOutcome.Ready
                        is LoginExResult.AuthRespOTPRequired -> AuthOutcome.OtpRequired(data.username)
                        else -> AuthOutcome.Unauthenticated
                    }
                    is ApiResult.Error -> AuthOutcome.Retryable
                    else -> AuthOutcome.Unauthenticated
                }
            }
        }
    }

    /** Re-authenticates and persists a fresh token. Returns true when the session is usable. */
    suspend fun recover(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String
    ): Boolean {
        val account = MultiAccountPrefs.getAccount(context, accountId) ?: return false
        return when (authenticate(context, manager, account)) {
            AuthOutcome.Ready -> storeToken(context, manager, serverId, accountId)
            else -> false
        }
    }

    /** Generates a long-lived token and stores it with freshness metadata. */
    suspend fun storeToken(
        context: Context,
        manager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        ttlSeconds: Int = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
    ): Boolean {
        val result = manager.auth.generateTokenWithResult(Auth.TokenRequest(ttl = ttlSeconds))
        if (result is ApiResult.Success) {
            MultiAccountPrefs.saveCurrentSession(context, serverId, accountId, result.data, ttlSeconds)
            return true
        }
        return false
    }
}
