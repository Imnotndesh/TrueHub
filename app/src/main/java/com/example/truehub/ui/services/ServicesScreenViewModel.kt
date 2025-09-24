// ServicesScreenViewModel.kt
package com.example.truehub.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.Apps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicesUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class ServicesScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState(isLoading = true))
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        // Only set loading if we don't have data yet
        if (_uiState.value.apps.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        } else {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
        }

        viewModelScope.launch {
            try {
                val apps = manager.apps.getInstalledAppsWithResult()
                when(apps){
                    is ApiResult.Success -> {
                        _uiState.value = ServicesUiState(
                            isLoading = false,
                            apps = apps.data,
                            error = null,
                            isRefreshing = false
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = ServicesUiState(
                            isLoading = false,
                            apps = emptyList(),
                            error = apps.message,
                            isRefreshing = false
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = ServicesUiState(
                            isLoading = true,
                            apps = emptyList(),
                            error = null,
                            isRefreshing = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load services"
                )
            }
        }
    }

    fun refresh() {
        loadApps()
    }

    // Start sleeping app
    fun startApp(appName:String){
        viewModelScope.launch {
            val result = manager.apps.startAppWithResult(appName)
            when(result){
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        isRefreshing = false,
                        error = null
                    )
                }
            }
        }
    }
    fun stopApp(appName:String){
        viewModelScope.launch {
            val result = manager.apps.stopAppWithResult(appName)
            when(result){
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        isRefreshing = false,
                        error = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class ServicesViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ServicesScreenViewModel::class.java)) {
                return ServicesScreenViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}