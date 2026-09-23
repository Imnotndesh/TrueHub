package com.imnotndesh.truehub.ui.homepage.instancesettings.alertsservice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlertServiceDetailUiState(
    val service: Alerts.AlertServiceEntry? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val deleteResult: Boolean? = null,
    val testResult: Boolean? = null,
    val isDeleting: Boolean = false,
    val isTesting: Boolean = false,
    val isUpdating: Boolean = false,
    val updateResult: Boolean? = null
)

@HiltViewModel
class AlertServiceDetailViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val serviceId: Int = savedStateHandle.get<Int>("serviceId") ?: 0

    private val _uiState = MutableStateFlow(AlertServiceDetailUiState())
    val uiState: StateFlow<AlertServiceDetailUiState> = _uiState.asStateFlow()

    init {
        loadService()
    }

    fun loadService() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = manager.alertsService.getAlertServiceWithResult(serviceId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(service = result.data, isLoading = false)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateService(update: Alerts.AlertServiceUpdate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateResult = null) }
            when (val result = manager.alertsService.updateAlertServiceWithResult(serviceId, update)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(service = result.data, isUpdating = false, updateResult = true)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUpdating = false, updateResult = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteService() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteResult = null) }
            when (val result = manager.alertsService.deleteAlertServiceWithResult(serviceId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, deleteResult = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isDeleting = false, deleteResult = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun testService(create: Alerts.AlertServiceCreate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            when (val result = manager.alertsService.testAlertServiceWithResult(create)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isTesting = false, testResult = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isTesting = false, testResult = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }
}
