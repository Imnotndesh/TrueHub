package com.imnotndesh.truehub.ui.services.vm.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Vm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VmDevicesUiState(
    val devices: List<Vm.DeviceEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val deletingDeviceId: Int? = null,
    val isImporting: Boolean = false,
    val importJob: System.Job? = null,
    val error: String? = null
)

@HiltViewModel
class VmDevicesViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val vmId: Int = savedStateHandle["vmId"] ?: -1
    private val _uiState = MutableStateFlow(VmDevicesUiState())
    val uiState: StateFlow<VmDevicesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = loadInternal(isRefresh = false)

    fun refresh() = loadInternal(isRefresh = true)

    private fun loadInternal(isRefresh: Boolean) {
        if (vmId < 0) {
            _uiState.value = VmDevicesUiState(isLoading = false, error = "VM ID is missing")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, error = null) }
            when (val result = manager.vmService.deviceQueryWithResult(filters = listOf(listOf("vm", "=", vmId)))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(devices = result.data, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun delete(deviceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingDeviceId = deviceId, error = null) }
            when (val result = manager.vmService.deviceDeleteWithResult(deviceId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(devices = it.devices.filterNot { d -> d.id == deviceId }, deletingDeviceId = null)
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(deletingDeviceId = null, error = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun convertDiskImage(source: String, destination: String) {
        if (source.isBlank() || destination.isBlank()) {
            _uiState.update { it.copy(error = "Source and destination are required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val payload = mapOf("source" to source.trim(), "destination" to destination.trim())
            when (val result = manager.vmService.deviceConvertWithResult(payload)) {
                is ApiResult.Success -> VirtualizationJobTracker.track(
                    manager, vmId.toString(), result.data, "CONVERTING"
                ) { loadInternal(isRefresh = true) }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun importDiskImage(diskimg: String, zvol: String) {
        if (diskimg.isBlank() || zvol.isBlank()) {
            _uiState.update { it.copy(error = "Disk image path and target zvol are required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importJob = null, error = null) }
            val payload = mapOf("diskimg" to diskimg.trim(), "zvol" to zvol.trim())
            when (val result = manager.vmService.importDiskImageWithResult(payload)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isImporting = false) }
                    VirtualizationJobTracker.track(manager, vmId.toString(), result.data, "IMPORTING") {
                        loadInternal(isRefresh = true)
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isImporting = false, error = result.message) }
                ApiResult.Loading -> _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
