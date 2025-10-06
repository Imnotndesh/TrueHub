package com.example.truehub.ui.services.vm

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.Vm
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VmScreenUiState(
    val vms: List<Vm.VmQueryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

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
                    Log.e("VM-Check", result.data.toString())
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

    fun startVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.startVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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

    fun stopVm(id: String, force: Boolean = false) {
        viewModelScope.launch {
            when (val result = manager.vmService.stopVmInstanceWithResult(id, force)) {
                is ApiResult.Success -> {
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

    fun restartVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.restartVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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

    fun suspendVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.suspendVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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

    fun resumeVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.resumeVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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

    fun powerOffVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.powerOffVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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

    fun deleteVm(id: String) {
        viewModelScope.launch {
            when (val result = manager.vmService.deleteVmInstanceWithResult(id)) {
                is ApiResult.Success -> {
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