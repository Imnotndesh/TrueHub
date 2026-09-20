package com.imnotndesh.truehub.data.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.WorkerSession
import com.imnotndesh.truehub.data.helpers.QuickLaunchSync
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.workers.AppsRefreshWorker.Companion.scheduleImmediate
import com.imnotndesh.truehub.data.workers.AppsRefreshWorker.Companion.scheduleRecurring
import com.imnotndesh.truehub.ui.utils.AppCache
import com.imnotndesh.truehub.ui.widgets.AppsUpdateWidgetUpdater
import com.imnotndesh.truehub.ui.widgets.pools.PoolsWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker that refreshes both the Apps widget cache and the Pools
 * widget cache in a single authenticated session.
 *
 * Call sites are unchanged — [scheduleRecurring] and [scheduleImmediate] have
 * the same signatures and work names as before. The only internal change is
 * that pools are fetched in parallel with apps, stored via
 * [WidgetDataStore.saveAppsAndPools], and [PoolsWidgetUpdater.update] is
 * called so the pools widget refreshes alongside the apps widget.
 */
class AppsRefreshWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME          = "TrueHub_Apps_Refresh"
        const val ONE_TIME_WORK_NAME = "TrueHub_Apps_Refresh_Once"

        fun scheduleRecurring(context: Context) {
            val request = PeriodicWorkRequestBuilder<AppsRefreshWorker>(3, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun scheduleImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<AppsRefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var client: TrueNASClient? = null
        try {
            val servers = MultiAccountPrefs.getServers(context)
            if (servers.isEmpty()) return@withContext Result.success()

            val manager: TrueNASApiManager
            when (val session = WorkerSession.open(context)) {
                is WorkerSession.Result.Ready -> {
                    manager = session.manager
                    client = session.client
                }
                is WorkerSession.Result.Unauthenticated -> return@withContext Result.success()
                is WorkerSession.Result.Retryable ->
                    return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
            }

            // ── Fetch apps + pools concurrently ──────────────────────────────
            val appsDeferred  = async { manager.apps.getInstalledAppsWithResult() }
            val poolsDeferred = async { manager.system.getPoolsWithResult() }

            val appsResult  = appsDeferred.await()
            val poolsResult = poolsDeferred.await()

            val apps  = if (appsResult  is ApiResult.Success) appsResult.data  else null
            val pools = if (poolsResult is ApiResult.Success) poolsResult.data else null

            when {
                apps != null && pools != null -> {
                    WidgetDataStore.saveAppsAndPools(context, apps, pools)
                    AppCache.updateApps(apps)
                    AppCache.updatePools(pools)
                }
                apps != null -> {
                    WidgetDataStore.saveUpgradableApps(context, apps)
                    AppCache.updateApps(apps)
                    QuickLaunchSync.refresh(context, apps)
                }
                pools != null -> {
                    WidgetDataStore.savePools(context, pools)
                    AppCache.updatePools(pools)
                }
                else -> {
                    client.disconnect()
                    return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }

            // ── Trigger widget redraws ────────────────────────────────────────
            if (apps  != null) AppsUpdateWidgetUpdater.update(context)
            if (pools != null) PoolsWidgetUpdater.update(context)

            client.disconnect()
            Result.success()

        } catch (e: Exception) {
            client?.disconnect()
            TrueHubLogger.e("AppsRefreshWorker", "Failed: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}