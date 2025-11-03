package com.imnotndesh.truehub.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertsUiState(
    val alerts: List<System.AlertResponse> = emptyList(),
    val categories: List<System.AlertCategoriesResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
)

class AlertsViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )

            when (val result = manager.system.listAlertsWithResult()) {
                is ApiResult.Success -> {
                    loadCategories()

                    val unreadCount = result.data.count { !it.dismissed }
                    _uiState.value = _uiState.value.copy(
                        alerts = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        unreadCount = unreadCount,
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
                ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = manager.system.listCategoriesWithResult()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        categories = result.data
                    )
                }
                is ApiResult.Error -> {
                    // Categories are optional, don't update error state
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun dismissAlert(uuid: String) {
        viewModelScope.launch {
            val result = try {
                manager.system.dismissAlertWithResult(uuid)
                ApiResult.Success(Unit)
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Failed to dismiss alert")
            }

            when (result) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Alert dismissed")
                    loadAlerts(isRefresh = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                    ToastManager.showError(result.message)
                }
                is ApiResult.Loading ->{}
            }
        }
    }

    fun restoreAlert(uuid: String) {
        viewModelScope.launch {
            val result = try {
                manager.system.restoreAlertWithResult(uuid)
                // If we get here without exception, it succeeded (null response is success)
                ApiResult.Success(Unit)
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Failed to restore alert")
            }

            when (result) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Alert restored")
                    // Refresh to get updated list
                    loadAlerts(isRefresh = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                    ToastManager.showError(result.message)
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadAlerts(isRefresh = true)
    }

    class AlertsViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
                return AlertsViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}