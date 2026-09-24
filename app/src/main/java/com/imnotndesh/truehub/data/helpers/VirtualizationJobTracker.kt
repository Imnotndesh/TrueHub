package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VirtualizationOperation(
    val job: System.Job,
    val operation: String
)

object VirtualizationJobTracker {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _operations = MutableStateFlow<Map<String, VirtualizationOperation>>(emptyMap())
    val operations: StateFlow<Map<String, VirtualizationOperation>> = _operations.asStateFlow()

    fun track(
        manager: TrueNASApiManager,
        instanceId: String,
        jobId: Int,
        operation: String,
        onFinished: suspend (String) -> Unit = {}
    ) {
        scope.launch {
            var pollAttempts = 0
            val maxPollAttempts = 150

            while (pollAttempts < maxPollAttempts) {
                when (val result = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _operations.value = _operations.value + (instanceId to VirtualizationOperation(job, operation))

                        if (job.state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                            delay(2000)
                            _operations.value = _operations.value - instanceId
                            withContext(Dispatchers.Main) { onFinished(job.state) }
                            break
                        }
                    }
                    is ApiResult.Error -> break
                    else -> Unit
                }
                pollAttempts++
                delay(2000)
            }

            if (pollAttempts >= maxPollAttempts) {
                _operations.value = _operations.value - instanceId
            }
        }
    }
}

fun virtualizationOperationLabel(operation: String): String = when (operation) {
    "STARTING" -> "Starting"
    "STOPPING" -> "Stopping"
    "RESTARTING" -> "Restarting"
    "SUSPENDING" -> "Suspending"
    "RESUMING" -> "Resuming"
    "POWERING_OFF" -> "Powering off"
    "CLONING" -> "Cloning"
    "DELETING" -> "Deleting"
    "EXPORTING" -> "Exporting"
    "IMPORTING" -> "Importing"
    "UPLOADING" -> "Uploading"
    else -> operation.lowercase().replaceFirstChar { it.uppercase() }
}
