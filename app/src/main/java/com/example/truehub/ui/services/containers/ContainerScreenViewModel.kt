package com.example.truehub.ui.services.containers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
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
    val error: String? = null
)

class ContainerScreenViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerScreenUiState())
    val uiState: StateFlow<ContainerScreenUiState> = _uiState.asStateFlow()

    init {
        loadContainers()
    }

    private fun loadContainers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = manager.virtService.getAllInstancesWithResult()) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Loaded ${result.data.size} containers")
                    _uiState.update {
                        it.copy(
                            containers = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    ToastManager.showError("Failed to load containers: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
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
                    ToastManager.showError("Failed to refresh containers: ${result.message}")
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
                    ToastManager.showSuccess("Started container: $id")
                    refresh()
                }
                is ApiResult.Error -> {
                    ToastManager.showError("Failed to Start containers: ${result.message}")
                    _uiState.update {
                        it.copy(error = "Failed to start container: ${result.message}")
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

    fun stopContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.stopVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Stopped container: $id")
                    refresh()
                }
                is ApiResult.Error -> {
                    ToastManager.showError("Failed to Stop containers: ${result.message}")
                    _uiState.update {
                        it.copy(error = "Failed to stop container: ${result.message}")
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

    fun restartContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.restartVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Restarted container: $id")
                    refresh()
                }
                is ApiResult.Error -> {
                    ToastManager.showError("Failed to Restart containers: ${result.message}")
                    _uiState.update {
                        it.copy(error = "Failed to restart container: ${result.message}")
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

    fun deleteContainer(id: String) {
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVirtInstanceWithResult(id)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Deleted container: $id")
                    refresh()
                }
                is ApiResult.Error -> {
                    ToastManager.showError("Failed to Delete containers: ${result.message}")
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