package com.example.truehub.ui.services.vm

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.core.graphics.convertTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.data.models.Vm
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

data class VmScreenUiState(
    val vms: List<Vm.VmQueryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val operationJobs : Map<Int, System.Job> = emptyMap(),
    val error: String? = null
)

// TODO: In future maybe add the get memory usage api in the info sheet
class VmScreenViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VmScreenUiState())
    val uiState: StateFlow<VmScreenUiState> = _uiState.asStateFlow()

    init {
        loadVms()
    }

    fun loadVms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = manager.vmService.queryAllVmsWithResult()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            vms = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    Log.e("VM-Check", result.message)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }

                ApiResult.Loading -> {
                    ToastManager.showError("Unable to load VMs")
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

            when (val result = manager.vmService.queryAllVmsWithResult()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            vms = result.data,
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
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to refresh VM")
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
    // TODO: Find a way also to showuser dialog option to overcommit
    fun startVm(id: Int, overcommit: Boolean = true) {
        viewModelScope.launch {
            val jobId : Int
            when (val result = manager.vmService.startVmInstanceWithResult(id,overcommit)) {
                is ApiResult.Success -> {
                    jobId = result.data
                    ToastManager.showSuccess("Starting")
                    trackContainerOperation(id,jobId,"STARTING")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to start VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to start VM")
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

    fun stopVm(id: Int, force: Boolean = false,timeout: Boolean = false) {
        viewModelScope.launch {
            when (val result = manager.vmService.stopVmInstanceWithResult(id, force,timeout)) {
                is ApiResult.Success -> {
                    val jobID = result.data
                    ToastManager.showSuccess("Stopping")
                    trackContainerOperation(id,jobID,"STOPPING")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to stop VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to stop VM")
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

    fun restartVm(id: Int) {
        viewModelScope.launch {
            when (val result = manager.vmService.restartVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    val jobID = result.data
                    ToastManager.showSuccess("Restarting")
                    trackContainerOperation(id,jobID,"RESTARTING")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to restart VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to restart VM")
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

    fun suspendVm(id: Int) {
        viewModelScope.launch {
            when (val result = manager.vmService.suspendVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Suspending")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to suspend VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to suspend VM")
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

    fun resumeVm(id: Int) {
        viewModelScope.launch {
            when (val result = manager.vmService.resumeVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Resuming VM")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to resume VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to resume VM")
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

    fun powerOffVm(id: Int) {
        viewModelScope.launch {
            when (val result = manager.vmService.powerOffVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Starting Shutdown")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to power off VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to Power-Off VM")
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

    fun deleteVm(id: Int,deleteZvols : Boolean ?= false, forceDelete : Boolean? = false) {
        viewModelScope.launch {
            when (val result = manager.vmService.deleteVmInstanceWithResult(id,deleteZvols,forceDelete)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Deleted Virtual Machine")
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(error = "Failed to delete VM: ${result.message}")
                    }
                }
                ApiResult.Loading -> {
                    ToastManager.showError("Unable to Delete VM")
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    private fun trackContainerOperation(vmID: Int, jobId: Int, operation: String) {
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
                                    operationJobs = state.operationJobs + (vmID to job)
                                )
                            }

                            // Check for terminal states
                            if (job.state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                                kotlinx.coroutines.delay(2000)
                                _uiState.update { state ->
                                    state.copy(
                                        operationJobs = state.operationJobs - vmID
                                    )
                                }
                                refresh()
                                break
                            }
                        }
                        is ApiResult.Error -> {
                            ToastManager.showError("Failed to get job info: ${jobResult.message}")
                            break
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    ToastManager.showError("Failed to get job info: ${e.message}")
                    break
                }

                pollAttempts++
                kotlinx.coroutines.delay(2000)
            }

            // Timeout cleanup
            if (pollAttempts >= maxPollAttempts) {
                _uiState.update { state ->
                    state.copy(operationJobs = state.operationJobs - vmID)
                }
            }
        }
    }

    class VmViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VmScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VmScreenViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}