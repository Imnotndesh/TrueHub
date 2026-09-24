package com.imnotndesh.truehub.ui.services.vm.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.utils.AppCache
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VmDetailUiState(
    val vm: Vm.VmQueryResponse? = null,
    val consoleUri: String? = null,
    val consoleString: String? = null,
    val memoryUsageMb: Int? = null,
    val memoryInfo: Map<String, Any?>? = null,
    val vmemoryInUse: Map<String, Long>? = null,
    val flags: Map<String, Boolean>? = null,
    val virtualizationDetails: Map<String, Any?>? = null,
    val logPath: String? = null,
    val liveStatus: String? = null,
    val displayDevices: List<Map<String, Any?>> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VmDetailViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val vmId: Int = savedStateHandle["vmId"] ?: -1
    private val _uiState = MutableStateFlow(VmDetailUiState())
    val uiState: StateFlow<VmDetailUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = VmDetailUiState(
            vm = AppCache.cachedVms.value.firstOrNull { it.id == vmId }
        )
        load()
    }

    fun load() {
        if (vmId < 0) {
            _uiState.value = VmDetailUiState(error = "VM ID is missing")
            return
        }
        viewModelScope.launch {
            val hasVm = _uiState.value.vm != null
            _uiState.update { it.copy(isLoading = !hasVm, isRefreshing = hasVm, error = null) }
            when (val result = manager.vmService.queryVmInstanceWithResult(vmId)) {
                is ApiResult.Success -> {
                    _uiState.value = VmDetailUiState(vm = result.data)
                    loadDiagnostics()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                ApiResult.Loading -> _uiState.update { it.copy(isLoading = !hasVm, isRefreshing = false) }
            }
        }
    }

    fun start(overcommit: Boolean = true) = runJobOperation("STARTING") {
        manager.vmService.startVmInstanceWithResult(vmId, overcommit)
    }

    fun stop(force: Boolean = false, forceAfterTimeout: Boolean = false) = runJobOperation("STOPPING") {
        manager.vmService.stopVmInstanceWithResult(vmId, force, forceAfterTimeout)
    }

    fun restart() = runJobOperation("RESTARTING") {
        manager.vmService.restartVmInstanceWithResult(vmId)
    }

    fun suspend() {
        viewModelScope.launch {
            when (val result = manager.vmService.suspendVmInstanceWithResult(vmId)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun resume() {
        viewModelScope.launch {
            when (val result = manager.vmService.resumeVmInstanceWithResult(vmId)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun powerOff() {
        viewModelScope.launch {
            when (val result = manager.vmService.powerOffVmInstanceWithResult(vmId)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun delete(deleteZvols: Boolean, forceDelete: Boolean, onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (val result = manager.vmService.deleteVmInstanceWithResult(vmId, deleteZvols, forceDelete)) {
                is ApiResult.Success -> onDeleted()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun clone(name: String, onCloned: () -> Unit) {
        viewModelScope.launch {
            when (val result = manager.vmService.cloneVmInstanceWithResult(vmId, name.trim().ifBlank { null })) {
                is ApiResult.Success -> onCloned()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun exportDiskImage(format: String, directory: String, zvol: String) {
        viewModelScope.launch {
            val payload = mapOf(
                "format" to format,
                "directory" to directory.trim(),
                "zvol" to zvol.trim()
            )
            when (val result = manager.vmService.exportDiskImageWithResult(payload)) {
                is ApiResult.Success -> VirtualizationJobTracker.track(
                    manager, vmId.toString(), result.data, "EXPORTING"
                )
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun loadDiagnostics() {
        if (vmId < 0) return
        viewModelScope.launch {
            val memoryUsage = manager.vmService.getVmMemoryUsageWithResult(vmId)
            val memoryInfo = manager.vmService.getVmMemoryInfoWithResult(vmId)
            val vmemory = manager.vmService.getVmemoryInUseWithResult()
            val flags = manager.vmService.flagsWithResult()
            val virtualization = manager.vmService.virtualizationDetailsWithResult()
            val logPath = manager.vmService.logFilePathWithResult(vmId)
            val displays = manager.vmService.getDisplayDevicesWithResult(vmId)
            val consoleString = manager.vmService.getConsoleWithResult(vmId)
            val liveStatus = manager.vmService.getVmInstanceStatusWithResult(vmId)
            _uiState.update {
                it.copy(
                    memoryUsageMb = (memoryUsage as? ApiResult.Success)?.data ?: it.memoryUsageMb,
                    memoryInfo = (memoryInfo as? ApiResult.Success)?.data ?: it.memoryInfo,
                    vmemoryInUse = (vmemory as? ApiResult.Success)?.data ?: it.vmemoryInUse,
                    flags = (flags as? ApiResult.Success)?.data ?: it.flags,
                    virtualizationDetails = (virtualization as? ApiResult.Success)?.data ?: it.virtualizationDetails,
                    logPath = (logPath as? ApiResult.Success)?.data ?: it.logPath,
                    displayDevices = (displays as? ApiResult.Success)?.data ?: it.displayDevices,
                    consoleString = (consoleString as? ApiResult.Success)?.data ?: it.consoleString,
                    liveStatus = (liveStatus as? ApiResult.Success)?.data?.state ?: it.liveStatus
                )
            }
        }
    }

    fun loadConsoleUri() {
        if (vmId < 0) return
        viewModelScope.launch {
            when (val result = manager.vmService.getVmDisplayWithResult(vmId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(consoleUri = result.data.uri, error = result.data.error)
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    suspend fun downloadLog(fileName: String, outputStream: OutputStream): Boolean {
        if (vmId < 0) return false
        val args = System.CoreDownloadArgs(
            method = "vm.log_file_download",
            args = listOf(vmId),
            filename = fileName
        )
        return when (val result = manager.system.coreDownload(args)) {
            is ApiResult.Success -> withContext(Dispatchers.IO) {
                manager.downloadFile(result.data.downloadUrl, outputStream)
            }
            else -> false
        }
    }

    private fun runJobOperation(operation: String, action: suspend () -> ApiResult<Int>) {
        if (vmId < 0) return
        viewModelScope.launch {
            when (val result = action()) {
                is ApiResult.Success -> {
                    VirtualizationJobTracker.track(manager, vmId.toString(), result.data, operation) { load() }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }
}
