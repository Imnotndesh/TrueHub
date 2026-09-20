package com.imnotndesh.truehub.data.workers

import android.content.Context
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
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CancelJobWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME_PREFIX = "TrueNAS_Cancel_Job_"
        private const val KEY_JOB_ID = "job_id"

        fun enqueue(context: Context, jobId: Int) {
            val request = OneTimeWorkRequestBuilder<CancelJobWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf(KEY_JOB_ID to jobId))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_PREFIX + jobId,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val jobId = inputData.getInt(KEY_JOB_ID, -1)
        if (jobId == -1) return@withContext Result.failure()

        var client: TrueNASClient? = null
        try {
            val session = WorkerSession.open(context)
            val manager = when (session) {
                is WorkerSession.Result.Ready -> {
                    client = session.client
                    session.manager
                }
                WorkerSession.Result.Unauthenticated -> return@withContext Result.failure()
                WorkerSession.Result.Retryable -> return@withContext Result.retry()
            }

            val result = manager.system.cancelJob(jobId)
            client?.disconnect()

            when (result) {
                is ApiResult.Success -> Result.success()
                is ApiResult.Error -> {
                    TrueHubLogger.e("CancelJobWorker", "Failed to cancel job $jobId: ${result.message}")
                    Result.failure()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            TrueHubLogger.e("CancelJobWorker", "Error cancelling job $jobId", e)
            client?.disconnect()
            Result.retry()
        }
    }
}