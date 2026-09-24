package com.imnotndesh.truehub.ui.services.containers.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.models.System
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContainerDevicesUiState(
    val devices: List<Map<String, Any?>> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val deletingDeviceName: String? = null,
    val isUploading: Boolean = false,
    val uploadBytes: Long = 0L,
    val uploadTotal: Long = 0L,
    val uploadJob: System.Job? = null,
    val error: String? = null
)

@HiltViewModel
class ContainerDevicesViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val _uiState = MutableStateFlow(ContainerDevicesUiState())
    val uiState: StateFlow<ContainerDevicesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        loadInternal(isRefresh = false)
    }

    fun refresh() {
        loadInternal(isRefresh = true)
    }

    private fun loadInternal(isRefresh: Boolean) {
        if (containerId.isBlank()) {
            _uiState.value = ContainerDevicesUiState(isLoading = false, error = "Container ID is missing")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            when (val result = manager.virtService.getInstanceDeviceListRawWithResult(containerId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    devices = result.data,
                    isLoading = false,
                    isRefreshing = false
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.message
                )
                ApiResult.Loading -> Unit
            }
        }
    }

    fun delete(deviceName: String) {
        if (deviceName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(deletingDeviceName = deviceName, error = null)
            when (val result = manager.virtService.deleteVirtInstanceDeviceWithResult(containerId, deviceName)) {
                is ApiResult.Success -> {
                    if (result.data) {
                        _uiState.value = _uiState.value.copy(
                            devices = _uiState.value.devices.filterNot { it["name"]?.toString() == deviceName },
                            deletingDeviceName = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            deletingDeviceName = null,
                            error = "The server did not confirm device removal"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    deletingDeviceName = null,
                    error = result.message
                )
                ApiResult.Loading -> Unit
            }
        }
    }

    fun importDiskImage(diskimg: String, zvol: String) {
        if (containerId.isBlank() || diskimg.isBlank() || zvol.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Disk image path and target zvol are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            val payload = mapOf("diskimg" to diskimg.trim(), "zvol" to zvol.trim())
            when (val result = manager.virtService.importDiskImageWithResult(payload)) {
                is ApiResult.Success -> VirtualizationJobTracker.track(manager, containerId, result.data, "IMPORTING") {
                    loadInternal(isRefresh = true)
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun uploadIso(
        targetDirectory: String,
        fileName: String,
        contentLength: Long,
        openStream: () -> java.io.InputStream,
        cleanup: () -> Unit = {}
    ) {
        if (targetDirectory.isBlank() || fileName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Target directory and file name are required")
            return
        }
        val serverPath = targetDirectory.trimEnd('/') + "/" + fileName.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                uploadBytes = 0L,
                uploadTotal = contentLength,
                uploadJob = null,
                error = null
            )
            val result = try {
                manager.uploadFile(
                    serverPath = serverPath,
                    fileName = fileName.trim(),
                    contentType = "application/octet-stream",
                    contentLength = contentLength,
                    openStream = openStream,
                    onProgress = { sent, total ->
                        _uiState.value = _uiState.value.copy(uploadBytes = sent, uploadTotal = total)
                    }
                )
            } finally {
                cleanup()
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isUploading = false)
                    VirtualizationJobTracker.track(manager, containerId, result.data, "UPLOADING") {
                        loadInternal(isRefresh = true)
                    }
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isUploading = false, error = result.message)
                ApiResult.Loading -> _uiState.value = _uiState.value.copy(isUploading = false)
            }
        }
    }
}
