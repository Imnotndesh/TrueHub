import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.AppCache
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

enum class AppCategory(val label: String) {
    ALL("All Apps"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    UPDATES("Has Updates")
}

data class AppsScreenUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val error: String? = null,
    val upgradeSummaryResult: Apps.AppUpgradeSummaryResult? = null,
    val isRefreshing: Boolean = false,
    val upgradeJobs: Map<String, com.imnotndesh.truehub.data.models.System.UpgradeJobState> = emptyMap(),
    val isLoadingUpgradeSummaryForApp: String? = null,
    val rollbackVersions: List<String> = emptyList(),
    val isLoadingRollbackVersions: Boolean = false,
    val rollbackJobs: Map<String, com.imnotndesh.truehub.data.models.System.UpgradeJobState> = emptyMap(),
    // New State for Category
    val selectedCategory: AppCategory = AppCategory.ALL
)

class AppsScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsScreenUiState())
    val uiState: StateFlow<AppsScreenUiState> = _uiState.asStateFlow()

    init {
        val cachedData = AppCache.cachedApps.value
        if (cachedData.isNotEmpty()) {
            _uiState.update { it.copy(apps = cachedData, isLoading = false) }
        }
        loadApps()
        startPeriodicRefresh()
    }

    // New Function to handle category selection
    fun updateCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30000)
                if (_uiState.value.apps.isNotEmpty()) {
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
            }
        }
    }

    fun loadApps() {
        viewModelScope.launch {
            if (_uiState.value.apps.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            try {
                when (val result = manager.apps.getInstalledAppsWithResult()) {
                    is ApiResult.Success -> {
                        AppCache.updateApps(result.data)
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
                                error = if (it.apps.isEmpty()) result.message else null
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
                        error = if (it.apps.isEmpty()) e.message ?: "Failed to load apps" else null
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

    fun startApp(appName: String) {
        viewModelScope.launch {
            when (val result = manager.apps.startAppWithResult(appName)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Started Container")
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = if (_uiState.value.apps.isEmpty()) result.message else null) }
                }
                is ApiResult.Loading -> {
                    ToastManager.showInfo("Starting Container")
                }
            }
        }
    }

    fun stopApp(appName: String) {
        viewModelScope.launch {
            when (val result = manager.apps.stopAppWithResult(appName)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Stopped Container")
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = if (_uiState.value.apps.isEmpty()) result.message else null) }
                }
                is ApiResult.Loading -> {
                    ToastManager.showInfo("Stopping Container")
                }
            }
        }
    }

    fun upgradeApp(appName: String) {
        viewModelScope.launch {
            when (val result = manager.apps.upgradeAppWithResult(appName)) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    _uiState.value = _uiState.value.copy(
                        upgradeJobs = _uiState.value.upgradeJobs + (
                                appName to com.imnotndesh.truehub.data.models.System.UpgradeJobState(state = "UPGRADING", progress = 0)
                                )
                    )
                    var pollAttempts = 0
                    val maxPollAttempts = 150

                    while (pollAttempts < maxPollAttempts) {
                        try {
                            when (val jobResult = manager.system.getJobInfoJobWithResult(jobId)) {
                                is ApiResult.Success -> {
                                    val job = jobResult.data
                                    val state = job.state
                                    val percent = job.progress?.percent ?: 0
                                    val description = job.progress?.description

                                    _uiState.value = _uiState.value.copy(
                                        upgradeJobs = _uiState.value.upgradeJobs + (
                                                appName to com.imnotndesh.truehub.data.models.System.UpgradeJobState(
                                                    state = state,
                                                    progress = percent,
                                                    description = description
                                                )
                                                )
                                    )

                                    if (state in listOf("SUCCESS", "FAILED", "ABORTED")) {
                                        delay(3000)
                                        _uiState.value = _uiState.value.copy(
                                            upgradeJobs = _uiState.value.upgradeJobs - appName
                                        )
                                        if (state == "SUCCESS") {
                                            loadApps()
                                        }
                                        break
                                    }
                                }
                                is ApiResult.Error -> {
                                    _uiState.value = _uiState.value.copy(
                                        upgradeJobs = _uiState.value.upgradeJobs - appName,
                                        error = "Upgrade monitoring failed: ${jobResult.message}"
                                    )
                                    break
                                }
                                is ApiResult.Loading -> {}
                            }
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                upgradeJobs = _uiState.value.upgradeJobs - appName,
                                error = "Upgrade monitoring error: ${e.message}"
                            )
                            break
                        }
                        pollAttempts++
                        delay(2000)
                    }

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

    fun loadUpgradeSummary(appName: String, appVersion: String? = "latest") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingUpgradeSummaryForApp = appName,
                error = null
            )
            try {
                when (val result = manager.apps.getUpgradeSummaryWithResult(appName, appVersion)) {
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
                when (val result = manager.apps.getRollbackVersionsWithResult(appName)) {
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
                        _uiState.value = _uiState.value.copy(isLoadingRollbackVersions = true)
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
                                appName to System.UpgradeJobState(
                                    state = "ROLLING_BACK",
                                    progress = 0,
                                    description = "Starting rollback..."
                                )
                                )
                    )
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
