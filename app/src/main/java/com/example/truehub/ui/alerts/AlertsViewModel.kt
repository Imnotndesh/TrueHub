package com.example.truehub.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.ui.components.ToastManager
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
                    // Also load categories for better alert display
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
                    ToastManager.showInfo("Loading alerts")
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

                ApiResult.Loading ->{
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                    ToastManager.showInfo("Loading alert categories")
                }
            }
        }
    }

    fun dismissAlert(uuid: String) {
        viewModelScope.launch {
            when (manager.system.dismissAlertWithResult(uuid)) {
                is ApiResult.Success -> {
                    // Update local state
                    val updatedAlerts = _uiState.value.alerts.map { alert ->
                        if (alert.uuid == uuid) {
                            alert.copy(dismissed = true)
                        } else {
                            alert
                        }
                    }
                    val unreadCount = updatedAlerts.count { !it.dismissed }
                    _uiState.value = _uiState.value.copy(
                        alerts = updatedAlerts,
                        unreadCount = unreadCount
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to dismiss alert"
                    )
                }

                ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                    ToastManager.showInfo("Dismissing alerts")
                }
            }
        }
    }

    fun restoreAlert(uuid: String) {
        viewModelScope.launch {
            when (manager.system.restoreAlertWithResult(uuid)) {
                is ApiResult.Success -> {
                    // Update local state
                    val updatedAlerts = _uiState.value.alerts.map { alert ->
                        if (alert.uuid == uuid) {
                            alert.copy(dismissed = false)
                        } else {
                            alert
                        }
                    }
                    val unreadCount = updatedAlerts.count { !it.dismissed }
                    _uiState.value = _uiState.value.copy(
                        alerts = updatedAlerts,
                        unreadCount = unreadCount
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to restore alert"
                    )
                }

                ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                    ToastManager.showInfo("Restoring alert")
                }
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