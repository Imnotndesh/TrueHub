package com.imnotndesh.truehub.ui.services.containers.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
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
}
