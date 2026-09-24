package com.imnotndesh.truehub.ui.services.containers.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.utils.AppCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContainerDetailUiState(
    val container: Virt.ContainerResponse? = null,
    val availableZvols: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContainerDetailViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val _uiState = MutableStateFlow(ContainerDetailUiState())
    val uiState: StateFlow<ContainerDetailUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ContainerDetailUiState(
            container = AppCache.cachedContainers.value.firstOrNull { it.id == containerId }
        )
        load()
    }

    fun load() {
        if (containerId.isBlank()) {
            _uiState.value = ContainerDetailUiState(error = "Container ID is missing")
            return
        }
        viewModelScope.launch {
            val hasContainer = _uiState.value.container != null
            _uiState.update {
                it.copy(
                    isLoading = !hasContainer,
                    isRefreshing = hasContainer,
                    error = null
                )
            }
            when (val result = manager.virtService.getInstanceWithResult(containerId)) {
                is ApiResult.Success -> {
                    _uiState.value = ContainerDetailUiState(container = result.data)
                    loadZvols()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(isLoading = !hasContainer, isRefreshing = false)
                }
            }
        }
    }

    fun start() = runOperation("STARTING") {
        manager.virtService.startVirtInstanceWithResult(containerId)
    }

    fun stop() = runOperation("STOPPING") {
        manager.virtService.stopVirtInstanceWithResult(containerId, 2)
    }

    fun restart() = runOperation("RESTARTING") {
        manager.virtService.restartVirtInstanceWithResult(containerId, 2)
    }

    private fun loadZvols() {
        if (containerId.isBlank()) return
        viewModelScope.launch {
            when (val devices = manager.virtService.getInstanceDeviceListRawWithResult(containerId)) {
                is ApiResult.Success -> {
                    val zvols = devices.data
                        .filter { it["dev_type"]?.toString()?.uppercase() in listOf("DISK", "CDROM") }
                        .mapNotNull { it["source"]?.toString()?.takeIf { source -> source.isNotBlank() } }
                        .distinct()
                    _uiState.update { it.copy(availableZvols = zvols) }
                }
                else -> Unit
            }
        }
    }

    fun exportDiskImage(format: String, directory: String, zvol: String) {
        if (containerId.isBlank()) return
        viewModelScope.launch {
            val payload = mapOf(
                "format" to format,
                "directory" to directory.trim(),
                "zvol" to zvol.trim()
            )
            when (val result = manager.virtService.exportDiskImageWithResult(payload)) {
                is ApiResult.Success -> VirtualizationJobTracker.track(manager, containerId, result.data, "EXPORTING")
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (containerId.isBlank()) return
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVirtInstanceWithResult(containerId)) {
                is ApiResult.Success -> {
                    VirtualizationJobTracker.track(manager, containerId, result.data, "DELETING") { state ->
                        if (state == "SUCCESS") {
                            onDeleted()
                        } else {
                            _uiState.update { it.copy(error = "Failed to delete container") }
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun runOperation(operation: String, action: suspend () -> ApiResult<Double>) {
        if (containerId.isBlank()) return
        viewModelScope.launch {
            when (val result = action()) {
                is ApiResult.Success -> {
                    VirtualizationJobTracker.track(manager, containerId, result.data.toInt(), operation) { load() }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }
}
