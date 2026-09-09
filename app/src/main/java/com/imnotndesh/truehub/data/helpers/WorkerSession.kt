package com.imnotndesh.truehub.data.helpers

import android.content.Context
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod

/**
 * Shared, session-aware bootstrap for background workers.
 *
 * Background work must not race the UI for tokens, so both workers converge here: they reuse a
 * still-valid stored token when possible (skipping a needless login), otherwise fall back to
 * saved credentials, and always hand back a [TrueNASApiManager] whose call path already owns
 * proactive refresh + single-owner recovery.
 */
object WorkerSession {

    sealed interface Result {
        data class Ready(
            val manager: TrueNASApiManager,
            val client: TrueNASClient
        ) : Result
        /** No usable saved profile / credentials; caller should stop quietly or fail. */
        data object Unauthenticated : Result
        /** Transient failure (connect / auth); caller should retry with backoff. */
        data object Retryable : Result
    }

    suspend fun open(context: Context): Result {
        val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context)
            ?: return Result.Unauthenticated
        val server = MultiAccountPrefs.getServer(context, serverId)
            ?: return Result.Unauthenticated
        val account = MultiAccountPrefs.getAccount(context, accountId)
            ?: return Result.Unauthenticated

        val client = TrueNASClient(
            Config.ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
        )
        if (!client.connect()) {
            client.disconnect()
            return Result.Retryable
        }

        val manager = TrueNASApiManager(client, context)

        val freshToken = MultiAccountPrefs.getSessionSnapshot(context)
            ?.takeIf { it.serverId == serverId && !it.isExpiringSoon() }
            ?.token
        val tokenWorks = freshToken != null &&
            (manager.auth.loginWithTokenAndResult(freshToken) is ApiResult.Success)
        if (tokenWorks) return Result.Ready(manager, client).also {
            com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, serverId)
        }

        val (credentialPrimary, credentialSecondary) = MultiAccountPrefs.getAccountCredentials(
            context, accountId, account.loginMethod
        )
        val loginResult = when (account.loginMethod) {
            LoginMethod.API_KEY -> {
                if (credentialPrimary.isNullOrBlank()) {
                    client.disconnect()
                    return Result.Unauthenticated
                }
                manager.auth.loginWithApiKeyWithResult(credentialPrimary)
            }
            LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                if (credentialPrimary.isNullOrBlank() || credentialSecondary.isNullOrBlank()) {
                    client.disconnect()
                    return Result.Unauthenticated
                }
                manager.auth.loginUserWithResult(
                    AuthService.DefaultAuth(credentialPrimary, credentialSecondary)
                )
            }
        }

        return if (loginResult is ApiResult.Success && loginResult.data == true) {
            com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, serverId)
            Result.Ready(manager, client)
        } else {
            client.disconnect()
            Result.Retryable
        }
    }
}
