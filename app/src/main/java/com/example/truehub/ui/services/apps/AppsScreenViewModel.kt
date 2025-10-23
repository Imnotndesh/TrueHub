// ServicesScreenViewModel.kt
package com.example.truehub.ui.services.apps

import android.util.Log
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.Apps
import com.example.truehub.data.models.System
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppsScreenUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val error: String? = null,
    val upgradeSummaryResult: Apps.AppUpgradeSummaryResult? = null,
    val isRefreshing: Boolean = false,
    val upgradeJobs :Map<String, System.UpgradeJobState> = emptyMap(),
    val isLoadingUpgradeSummaryForApp: String? = null,
    // Rollback Stuff
    val rollbackVersions: List<String> = emptyList(),
    val isLoadingRollbackVersions: Boolean = false,
    val rollbackJobs: Map<String, System.UpgradeJobState> = emptyMap()
)

class AppsScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsScreenUiState())
    val uiState: StateFlow<AppsScreenUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            if (_uiState.value.apps.isEmpty()){
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                when(val result = manager.apps.getInstalledAppsWithResult()) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                apps = result.data,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.message
                            )
                        }
                    }
                    is ApiResult.Loading -> {
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load apps"
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadApps()
        }
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
                    ToastManager.showSuccess("Started Container")
                    refresh()
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
                    ToastManager.showInfo("Starting Container")
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
                    ToastManager.showSuccess("Stopped Container")
                    refresh()
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
                    ToastManager.showInfo("Stopping Container")
                }
            }
        }
    }
    fun upgradeApp(appName: String) {
        viewModelScope.launch {
            val result = manager.apps.upgradeAppWithResult(appName)
            when (result) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    _uiState.value = _uiState.value.copy(
                        upgradeJobs = _uiState.value.upgradeJobs + (
                                appName to System.UpgradeJobState(state = "UPGRADING", progress = 0)
                                )
                    )

                    // Fixed polling with proper error handling and termination
                    var pollAttempts = 0
                    val maxPollAttempts = 150 // 5 minutes max (150 * 2 seconds)

                    while (pollAttempts < maxPollAttempts) {
                        try {
                            val jobResult = manager.system.getJobInfoJobWithResult(jobId)
                            when (jobResult) {
                                is ApiResult.Success -> {
                                    val job = jobResult.data
                                    val state = job.state
                                    val percent = job.progress?.percent ?: 0
                                    val description = job.progress?.description

                                    _uiState.value = _uiState.value.copy(
                                        upgradeJobs = _uiState.value.upgradeJobs + (
                                                appName to System.UpgradeJobState(
                                                    state = state,
                                                    progress = percent,
                                                    description = description
                                                )
                                                )
                                    )

                                    Log.d("UpgradeJob", "App: $appName, State: $state, Progress: $percent%")

                                    // Check for terminal states
                                    if (state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                                        // Remove from upgradeJobs after a delay to show final state
                                        delay(3000)
                                        _uiState.value = _uiState.value.copy(
                                            upgradeJobs = _uiState.value.upgradeJobs - appName
                                        )

                                        // Refresh apps list to get updated state
                                        if (state == "SUCCESS") {
                                            loadApps()
                                        }
                                        break
                                    }
                                }
                                is ApiResult.Error -> {
                                    Log.e("UpgradeJob", "Error polling job $jobId: ${jobResult.message}")
                                    // Remove from upgrade jobs and show error
                                    _uiState.value = _uiState.value.copy(
                                        upgradeJobs = _uiState.value.upgradeJobs - appName,
                                        error = "Upgrade monitoring failed: ${jobResult.message}"
                                    )
                                    break
                                }
                                is ApiResult.Loading -> {
                                    // Continue polling
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UpgradeJob", "Exception while polling: ${e.message}", e)
                            _uiState.value = _uiState.value.copy(
                                upgradeJobs = _uiState.value.upgradeJobs - appName,
                                error = "Upgrade monitoring error: ${e.message}"
                            )
                            break
                        }

                        pollAttempts++
                        delay(2000)
                    }

                    // Timeout handling
                    if (pollAttempts >= maxPollAttempts) {
                        _uiState.value = _uiState.value.copy(
                            upgradeJobs = _uiState.value.upgradeJobs - appName,
                            error = "Upgrade monitoring timeout for $appName"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }
    // load app upgrade summary
    fun loadUpgradeSummary(appName: String, appVersion: String? = "latest") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingUpgradeSummaryForApp = appName,
                error = null
            )

            try {
                val result = manager.apps.getUpgradeSummaryWithResult(appName, appVersion)
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            upgradeSummaryResult = result.data,
                            error = null
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            upgradeSummaryResult = null,
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingUpgradeSummaryForApp = appName,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUpgradeSummaryForApp = appName,
                    upgradeSummaryResult = null,
                    error = e.message ?: "Failed to load upgrade summary"
                )
            }
        }
    }
    fun clearUpgradeSummary() {
        _uiState.value = _uiState.value.copy(
            upgradeSummaryResult = null,
            isLoadingUpgradeSummaryForApp = null
        )
    }
    fun loadRollbackVersions(appName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingRollbackVersions = true,
                rollbackVersions = emptyList(),
                error = null
            )

            try {
                val result = manager.apps.getRollbackVersionsWithResult(appName)
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingRollbackVersions = false,
                            rollbackVersions = result.data,
                            error = null
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingRollbackVersions = false,
                            rollbackVersions = emptyList(),
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingRollbackVersions = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRollbackVersions = false,
                    rollbackVersions = emptyList(),
                    error = e.message ?: "Failed to load rollback versions"
                )
            }
        }
    }

    fun rollbackApp(appName: String, version: String, rollbackSnapshot: Boolean = true) {
        viewModelScope.launch {
            val result = manager.apps.rollbackAppWithResult(appName, version, rollbackSnapshot)
            when (result) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    _uiState.value = _uiState.value.copy(
                        rollbackJobs = _uiState.value.rollbackJobs + (
                                appName to System.UpgradeJobState(state = "ROLLING_BACK", progress = 0, description = "Starting rollback...")
                                )
                    )

                    var pollAttempts = 0
                    val maxPollAttempts = 150

                    while (pollAttempts < maxPollAttempts) {
                        try {
                            val jobResult = manager.system.getJobInfoJobWithResult(jobId)
                            when (jobResult) {
                                is ApiResult.Success -> {
                                    val job = jobResult.data
                                    val state = job.state
                                    val percent = job.progress?.percent ?: 0
                                    val description = job.progress?.description

                                    _uiState.value = _uiState.value.copy(
                                        rollbackJobs = _uiState.value.rollbackJobs + (
                                                appName to System.UpgradeJobState(
                                                    state = state,
                                                    progress = percent,
                                                    description = description
                                                )
                                                )
                                    )

                                    Log.d("RollbackJob", "App: $appName, State: $state, Progress: $percent%")

                                    if (state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                                        delay(3000)
                                        _uiState.value = _uiState.value.copy(
                                            rollbackJobs = _uiState.value.rollbackJobs - appName
                                        )

                                        if (state == "SUCCESS") {
                                            ToastManager.showSuccess("App rolled back successfully")
                                            loadApps()
                                        } else {
                                        }
                                        break
                                    }
                                }
                                is ApiResult.Error -> {
                                    Log.e("RollbackJob", "Error polling job $jobId: ${jobResult.message}")
                                    _uiState.value = _uiState.value.copy(
                                        rollbackJobs = _uiState.value.rollbackJobs - appName,
                                        error = "Rollback monitoring failed: ${jobResult.message}"
                                    )
                                    break
                                }
                                is ApiResult.Loading -> {}
                            }
                        } catch (e: Exception) {
                            Log.e("RollbackJob", "Exception while polling: ${e.message}", e)
                            _uiState.value = _uiState.value.copy(
                                rollbackJobs = _uiState.value.rollbackJobs - appName,
                                error = "Rollback monitoring error: ${e.message}"
                            )
                            break
                        }

                        pollAttempts++
                        delay(2000)
                    }

                    if (pollAttempts >= maxPollAttempts) {
                        _uiState.value = _uiState.value.copy(
                            rollbackJobs = _uiState.value.rollbackJobs - appName,
                            error = "Rollback monitoring timeout for $appName"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearRollbackVersions() {
        _uiState.value = _uiState.value.copy(
            rollbackVersions = emptyList(),
            isLoadingRollbackVersions = false
        )
    }





    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class AppsScreenViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppsScreenViewModel::class.java)) {
                return AppsScreenViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}