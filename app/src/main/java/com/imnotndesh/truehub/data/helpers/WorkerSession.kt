package com.imnotndesh.truehub.data.helpers

import android.content.Context
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

        val snapshot = MultiAccountPrefs.getSessionSnapshot(context)
        if (snapshot != null && snapshot.serverId == serverId && !snapshot.isExpiringSoon()) {
            val client = TrueNASClient(
                ClientConfig(serverUrl = server.serverUrl, insecure = server.insecure)
            )
            if (!client.connect()) {
                client.disconnect()
                return Result.Retryable
            }
            val manager = TrueNASApiManager(client, context.applicationContext)
            if (manager.auth.loginWithTokenAndResult(snapshot.token) is ApiResult.Success) {
                com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, serverId)
                return Result.Ready(manager, client)
            }
            client.disconnect()
        }

        return when (val outcome = SessionProvider.open(context, server, account)) {
            is SessionProvider.OpenResult.Ready -> {
                com.imnotndesh.truehub.ui.utils.AppCache.bindServer(context, serverId)
                Result.Ready(outcome.manager, outcome.client)
            }
            SessionProvider.OpenResult.Retryable -> Result.Retryable
            else -> Result.Unauthenticated
        }
    }
}
