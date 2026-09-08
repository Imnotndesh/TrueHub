package com.imnotndesh.truehub.data.helpers

import android.content.Context
import android.content.Intent
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.JobNotificationService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

object GlobalJobTracker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<Int, Job>()

    fun startTracking(
        context: Context,
        manager: TrueNASApiManager,
        type : String = "UPGRADING",
        jobId: Int,
        appName: String,
        showNotif: Boolean
    ) {
        if (jobs.containsKey(jobId)) return

        jobs[jobId] = scope.launch {
            var running = true
            while (running) {
                val result = manager.system.getJobInfoJobWithResult(jobId)
                if (result is ApiResult.Success) {
                    val data = result.data
                    val progress = data.progress?.percent ?: 0
                    val state = data.state
                    val isDone = state in listOf("SUCCESS", "FAILED", "ABORTED")

                    JobRepository.updateJob(TrackedJob(jobId, appName, type,state,
                        progress, data.progress?.description))

                    if (showNotif) {
                        val intent = Intent(context, JobNotificationService::class.java).apply {
                            action = "ACTION_${type}_JOB_$jobId"
                            putExtra("id", jobId)
                            putExtra("type", type)
                            putExtra("name", appName)
                            putExtra("progress", progress)
                            putExtra("done", isDone)
                            putExtra("status_text", data.progress?.description ?: "Processing...")
                        }
                        context.startForegroundService(intent)
                    }

                    if (isDone) {
                        running = false
                        delay(5000.milliseconds)
                        JobRepository.removeJob(jobId)
                        continue
                    }
                }
                delay(2000.milliseconds)
            }
            jobs.remove(jobId)
        }
    }
}