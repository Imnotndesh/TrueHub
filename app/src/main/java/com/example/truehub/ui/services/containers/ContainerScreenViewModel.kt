package com.example.truehub.ui.services.containers

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.data.models.Virt
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContainerScreenUiState(
    val containers: List<Virt.ContainerResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val operationJobs: Map<String, System.Job> = emptyMap()
)

class ContainerScreenViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerScreenUiState())
    val uiState: StateFlow<ContainerScreenUiState> = _uiState.asStateFlow()

    init {
        loadContainers()
    }

    fun loadContainers() {
        viewModelScope.launch {
            if (_uiState.value.containers.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            when (val result = manager.virtService.getAllInstancesWithResult()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            containers = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }

                ApiResult.Loading ->{
                        _uiState.update {
                        it.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            when (val result = manager.virtService.getAllInstancesWithResult()) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Refreshed ${result.data.size} containers")
                    _uiState.update {
                        it.copy(
                            containers = result.data,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    fun startContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.startVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    val jobId = result.data // Assuming this returns job ID
                    trackContainerOperation(id, jobId.toInt(), "STARTING")
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to start container: ${result.message}")
                    }
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(isLoading = true, error = null)
                }
            }
        }
    }

    fun stopContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.stopVirtInstanceWithResult(id, 2)) {
                is ApiResult.Success -> {
                    val jobId = result.data // Assuming this returns job ID
                    trackContainerOperation(id, jobId.toInt(), "STOPPING")
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to stop container: ${result.message}")
                    }
                    Log.e("View-Model-Debug", result.message)
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(isLoading = true, error = null)
                }
            }
        }
    }
    fun restartContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.restartVirtInstanceWithResult(id,2)) {
                is ApiResult.Success -> {
                    val jobId = result.data // Assuming this returns job ID
                    trackContainerOperation(id, jobId.toInt(), "RESTARTING")
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to restart container: ${result.message}")
                    }
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(isLoading = true, error = null)
                }
            }
        }
    }
    private fun trackContainerOperation(containerId: String, jobId: Int, operation: String) {
        viewModelScope.launch {
            var pollAttempts = 0
            val maxPollAttempts = 150 // 5 minutes max

            while (pollAttempts < maxPollAttempts) {
                try {
                    val jobResult = manager.system.getJobInfoJobWithResult(jobId)
                    when (jobResult) {
                        is ApiResult.Success -> {
                            val job = jobResult.data

                            // Update job state
                            _uiState.update { state ->
                                state.copy(
                                    operationJobs = state.operationJobs + (containerId to job)
                                )
                            }

                            // Check for terminal states
                            if (job.state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                                kotlinx.coroutines.delay(2000)
                                _uiState.update { state ->
                                    state.copy(
                                        operationJobs = state.operationJobs - containerId
                                    )
                                }
                                refresh()
                                break
                            }
                        }
                        is ApiResult.Error -> {
                            break
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    break
                }

                pollAttempts++
                kotlinx.coroutines.delay(2000)
            }

            // Timeout cleanup
            if (pollAttempts >= maxPollAttempts) {
                _uiState.update { state ->
                    state.copy(operationJobs = state.operationJobs - containerId)
                }
            }
        }
    }

    fun deleteContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    trackContainerOperation(id,jobId,"DELETING")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to delete container: ${result.message}")
                    }
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class ContainerViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContainerScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ContainerScreenViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}