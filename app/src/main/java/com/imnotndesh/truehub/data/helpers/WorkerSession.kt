package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.work.Data
import androidx.work.workDataOf
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Config.ClientConfig

/**
 * Shared, session-aware bootstrap for background work. Reuses a still-valid stored token when
 * possible, otherwise delegates to [SessionProvider] for a credential-based login — so the
 * worker auth path is identical to the UI's.
 */
object WorkerSession {

    const val KEY_SERVER_ID = "worker_server_id"
    const val KEY_ACCOUNT_ID = "worker_account_id"

    fun profileInputData(serverId: String?, accountId: String?): Data {
        return workDataOf(
            KEY_SERVER_ID to serverId,
            KEY_ACCOUNT_ID to accountId
        )
    }

    fun Data.profileIds(): Pair<String, String>? {
        val serverId = getString(KEY_SERVER_ID) ?: return null
        val accountId = getString(KEY_ACCOUNT_ID) ?: return null
        return serverId to accountId
    }

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

    suspend fun open(
        context: Context,
        serverId: String? = null,
        accountId: String? = null
    ): Result {
        val profile = if (serverId != null && accountId != null) {
            serverId to accountId
        } else {
            MultiAccountPrefs.getLastUsedProfile(context)
        } ?: return Result.Unauthenticated
        val (resolvedServerId, resolvedAccountId) = profile
        val server = MultiAccountPrefs.getServer(context, resolvedServerId)
            ?: return Result.Unauthenticated
        val account = MultiAccountPrefs.getAccount(context, resolvedAccountId)
            ?: return Result.Unauthenticated
        if (account.serverId != resolvedServerId || !account.autoLoginEnabled) {
            return Result.Unauthenticated
        }

        val snapshot = MultiAccountPrefs.resolveAccountTokenSnapshot(
            context,
            resolvedServerId,
            resolvedAccountId
        )
        if (snapshot != null && !snapshot.isExpiringSoon()) {
            val client = TrueNASClient(
                ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
            )
            if (!client.connect()) {
                client.disconnect()
                return Result.Retryable
            }
            val manager = TrueNASApiManager(client, context.applicationContext)
            val tokenResult = manager.auth.loginWithTokenAndResult(snapshot.token)
            if (SessionProvider.isSuccessfulBooleanResult(tokenResult)) {
                val identity = manager.auth.getUserDetailsWithResult()
                if (identity is ApiResult.Success &&
                    SessionProvider.isValidTokenIdentity(
                        identity.data.pw_name,
                        account.loginMethod,
                        account.username
                    )
                ) {
                    com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, resolvedServerId)
                    return Result.Ready(manager, client)
                }
            }
            client.disconnect()
        }

        return when (val outcome = SessionProvider.open(context, server, account)) {
            is SessionProvider.OpenResult.Ready -> {
                com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, resolvedServerId)
                Result.Ready(outcome.manager, outcome.client)
            }
            is SessionProvider.OpenResult.TemporaryAuthenticated -> {
                com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, resolvedServerId)
                Result.Ready(outcome.manager, outcome.client)
            }
            SessionProvider.OpenResult.Retryable -> Result.Retryable
            else -> Result.Unauthenticated
        }
    }
}
