package com.imnotndesh.truehub.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.WorkerSession
import com.imnotndesh.truehub.data.helpers.WorkerSession.profileIds
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DismissAlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME_PREFIX = "TrueNAS_Dismiss_Alert_"
        private const val KEY_ALERT_UUID = "alert_uuid"

        fun enqueue(
            context: Context,
            alertUuid: String,
            serverId: String? = null,
            accountId: String? = null
        ) {
            val request = OneTimeWorkRequestBuilder<DismissAlertWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        KEY_ALERT_UUID to alertUuid,
                        WorkerSession.KEY_SERVER_ID to serverId,
                        WorkerSession.KEY_ACCOUNT_ID to accountId
                    )
                )
                .build()

            // Keyed by uuid so a double-tap before the system removes the
            // action doesn't queue two identical dismiss calls.
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_PREFIX + alertUuid,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val alertUuid = inputData.getString(KEY_ALERT_UUID)
            ?: return@withContext Result.failure()

        var client: TrueNASClient? = null
        try {
            val profileIds = inputData.profileIds()
            val session = WorkerSession.open(
                context,
                profileIds?.first,
                profileIds?.second
            )
            val manager = when (session) {
                is WorkerSession.Result.Ready -> {
                    client = session.client
                    session.manager
                }
                WorkerSession.Result.Unauthenticated -> return@withContext Result.failure()
                WorkerSession.Result.Retryable -> return@withContext Result.retry()
            }

            val result = manager.system.dismissAlertWithResult(alertUuid)
            client?.disconnect()

            when (result) {
                is ApiResult.Success -> Result.success()
                is ApiResult.Error -> {
                    TrueHubLogger.e("DismissAlertWorker", "Failed to dismiss $alertUuid: ${result.message}")
                    Result.retry()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            TrueHubLogger.e("DismissAlertWorker", "Error dismissing $alertUuid", e)
            client?.disconnect()
            Result.retry()
        }
    }
}