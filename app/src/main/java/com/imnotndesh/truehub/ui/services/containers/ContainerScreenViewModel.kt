package com.imnotndesh.truehub.ui.services.containers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.ui.utils.AppCache
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContainerScreenUiState(
    val containers: List<Virt.ContainerResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContainerScreenViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerScreenUiState())
    val uiState: StateFlow<ContainerScreenUiState> = _uiState.asStateFlow()

    init {
        val cachedData = AppCache.cachedContainers.value
        if (cachedData.isNotEmpty()) {
            _uiState.update { it.copy(containers = cachedData, isLoading = false) }
        }
        loadContainers()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000)
                _uiState.update { it.copy(isRefreshing = true) }
                loadContainers()
            }
        }
    }

    fun loadContainers() {
        viewModelScope.launch {
            if (_uiState.value.containers.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            when (val result = manager.virtService.getAllInstancesWithResult()) {
                is ApiResult.Success -> {
                    val containers = result.data.filter { it.type == Virt.Type.CONTAINER }
                    AppCache.updateContainers(containers)
                    _uiState.update {
                        it.copy(
                            containers = containers,
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
                            error = if (it.containers.isEmpty()) result.message else null
                        )
                    }
                }

                ApiResult.Loading -> {
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
                    val containers = result.data.filter { it.type == Virt.Type.CONTAINER }
                    AppCache.updateContainers(containers)
                    ToastManager.showSuccess("Refreshed ${containers.size} containers")
                    _uiState.update {
                        it.copy(
                            containers = containers,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = if (it.containers.isEmpty()) result.message else null
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
                    VirtualizationJobTracker.track(manager, id, result.data.toInt(), "STARTING") { refresh() }
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
                    VirtualizationJobTracker.track(manager, id, result.data.toInt(), "STOPPING") { refresh() }
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
            when (val result = manager.virtService.restartVirtInstanceWithResult(id, 2)) {
                is ApiResult.Success -> {
                    VirtualizationJobTracker.track(manager, id, result.data.toInt(), "RESTARTING") { refresh() }
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

    fun deleteContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    VirtualizationJobTracker.track(manager, id, result.data, "DELETING") { refresh() }
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
}
