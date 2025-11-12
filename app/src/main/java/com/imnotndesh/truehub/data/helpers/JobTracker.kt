package com.imnotndesh.truehub.data.helpers

import android.util.Log
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A generic job state holder.
 */
data class JobState(
    val state: String = "INITIALIZING",
    val progress: Int = 0,
    val description: String? = null
)

object JobTracker {

    /**
     * Polls a job's status and updates a StateFlow with its progress.
     *
     * @param jobId The ID of the job to track.
     * @param manager The TrueNASApiManager instance.
     * @param jobsStateFlow The StateFlow holding the map of tracked jobs.
     * @param trackingKey A unique key to identify this job in the map (e.g., app name, task ID).
     * @param pollIntervalMillis The delay between each poll attempt.
     * @param maxPollAttempts The maximum number of times to poll before timing out.
     * @param onComplete A lambda to execute when the job finishes (SUCCESS, FAILED, ABORTED).
     */
    suspend fun pollJobStatus(
        jobId: Int,
        manager: TrueNASApiManager,
        jobsStateFlow: MutableStateFlow<Map<String, JobState>>,
        trackingKey: String,
        pollIntervalMillis: Long = 2000L,
        maxPollAttempts: Int = 150, // 5 minutes default timeout
        onComplete: ((finalState: String) -> Unit)? = null
    ) {
        // Initial state
        jobsStateFlow.update { it + (trackingKey to JobState(state = "WAITING", progress = 0)) }

        var pollAttempts = 0
        while (pollAttempts < maxPollAttempts) {
            try {
                when (val jobResult = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = jobResult.data
                        val newState = JobState(
                            state = job.state,
                            progress = job.progress?.percent ?: 0,
                            description = job.progress?.description
                        )
                        jobsStateFlow.update { it + (trackingKey to newState) }

                        Log.d("JobTracker", "Key: $trackingKey, State: ${newState.state}, Progress: ${newState.progress}%")

                        if (newState.state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                            onComplete?.invoke(newState.state)
                            // Remove from tracking after a delay
                            delay(5000)
                            jobsStateFlow.update { it - trackingKey }
                            return
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e("JobTracker", "Error polling job $jobId: ${jobResult.message}")
                        jobsStateFlow.update { it - trackingKey }
                        onComplete?.invoke("FAILED")
                        return
                    }
                    is ApiResult.Loading -> { /* Continue polling */ }
                }
            } catch (e: Exception) {
                Log.e("JobTracker", "Exception while polling job $jobId: ${e.message}", e)
                jobsStateFlow.update { it - trackingKey }
                onComplete?.invoke("FAILED")
                return
            }
            pollAttempts++
            delay(pollIntervalMillis)
        }

        // Handle timeout
        Log.w("JobTracker", "Polling timed out for job $jobId.")
        jobsStateFlow.update { it - trackingKey }
        onComplete?.invoke("TIMED_OUT")
    }
}
