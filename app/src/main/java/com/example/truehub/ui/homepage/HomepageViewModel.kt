package com.example.truehub.ui.homepage

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
import kotlinx.coroutines.async

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val systemInfo: System.SystemInfo,
        val poolDetails: List<System.Pool>,
        val diskDetails: List<System.DiskDetails>,
        val cpuData: List<System.ReportingGraphResponse>?,
        val memoryData: List<System.ReportingGraphResponse>?,
        val isRefreshing: Boolean = false
    ) : HomeUiState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : HomeUiState()
}

class HomeViewModel(
    private val apiManager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refresh() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(isRefreshing = true)
        } else {
            _uiState.value = HomeUiState.Loading
        }
        loadDashboardData()
    }

    fun retryLoad() {
        _uiState.value = HomeUiState.Loading
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val systemInfoResult = apiManager.system.getSystemInfoWithResult()

                if (systemInfoResult !is ApiResult.Success) {
                    _uiState.value = HomeUiState.Error("Failed to load system information")
                    ToastManager.showError("Unable to connect to TrueNAS system")
                    return@launch
                }

                val systemInfo = systemInfoResult.data

                // Load other data concurrently
                val poolDeferred = async {
                    apiManager.system.getPoolsWithResult()
                }
                val diskDeferred = async {
                    apiManager.system.getDisksWithResult()
                }
                val cpuDataDeferred = async {
                    val cpuGraphRequest = listOf(
                        System.ReportingGraphRequest(
                            name = System.ReportingGraphName.CPU
                        )
                    )
                    val query = System.ReportingGraphQuery(
                        unit = System.ReportingUnit.HOUR,
                        aggregate = true
                    )
                    apiManager.system.getReportingDataWithResult(cpuGraphRequest, query)
                }
                val memoryDataDeferred = async {
                    val memoryGraphRequest = listOf(
                        System.ReportingGraphRequest(
                            name = System.ReportingGraphName.MEMORY
                        )
                    )
                    val query = System.ReportingGraphQuery(
                        unit = System.ReportingUnit.HOUR,
                        aggregate = true
                    )
                    apiManager.system.getReportingDataWithResult(memoryGraphRequest, query)
                }

                // Await all results
                val poolResult = poolDeferred.await()
                val diskResult = diskDeferred.await()
                val cpuDataResult = cpuDataDeferred.await()
                val memoryDataResult = memoryDataDeferred.await()

                val pools = if (poolResult is ApiResult.Success) poolResult.data else emptyList()
                val disks = if (diskResult is ApiResult.Success) diskResult.data else emptyList()

                _uiState.value = HomeUiState.Success(
                    systemInfo = systemInfo,
                    poolDetails = pools,
                    diskDetails = disks,
                    cpuData = if (cpuDataResult is ApiResult.Success) cpuDataResult.data else null,
                    memoryData = if (memoryDataResult is ApiResult.Success) memoryDataResult.data else null,
                    isRefreshing = false
                )
                ToastManager.showSuccess("Dashboard updated successfully")

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Unknown error occurred",
                    canRetry = true
                )
                ToastManager.showError("Failed to load dashboard: ${e.message}")
            }
        }
    }

    fun shutdownSystem(reason: String) {
        viewModelScope.launch {
            try {
                ToastManager.showInfo("Initiating system shutdown...")

                val result = apiManager.system.shutdownSystemWithResult(reason)
                when (result) {
                    is ApiResult.Success -> {
                        ToastManager.showSuccess("System shutdown initiated successfully")
                    }
                    is ApiResult.Error -> {
                        ToastManager.showError("Failed to shutdown system: ${result.message}")
                    }
                    ApiResult.Loading -> {
                        ToastManager.showInfo("Initiating system shutdown...")
                    }
                }
            } catch (e: Exception) {
                ToastManager.showError("Shutdown failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is HomeUiState.Error) {
            _uiState.value = HomeUiState.Loading
            loadDashboardData()
        }
    }

    class HomeViewModelFactory(
        private val apiManager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(apiManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}