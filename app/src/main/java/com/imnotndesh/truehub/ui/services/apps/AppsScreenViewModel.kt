package com.imnotndesh.truehub.ui.services.apps

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.utils.AppCache
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.canUpgradeNow
import com.imnotndesh.truehub.data.models.isAsleep
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.time.Duration.Companion.milliseconds

enum class AppCategory(val label: String) {
    ALL("All Apps"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    UPDATES("Has Updates")
}
private val _isInstalling = MutableStateFlow(false)
private val _installError = MutableStateFlow<String?>(null)
private val _installJobId = MutableStateFlow<Int?>(null)


data class AppsScreenUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val marketplaceApps : List<Apps.AppAvailableItem> = emptyList(),
    val error: String? = null,
    val upgradeSummaryResult: Apps.AppUpgradeSummaryResult? = null,
    val isRefreshing: Boolean = false,
    val upgradeJobs: Map<String, System.UpgradeJobState> = emptyMap(),
    val isLoadingUpgradeSummaryForApp: String? = null,
    val rollbackVersions: List<String> = emptyList(),
    val isLoadingRollbackVersions: Boolean = false,
    val selectedCategory: AppCategory = AppCategory.ALL,
    val searchQuery: String = "",
    val catalogAppDetails: Apps.CatalogAppDetails? = null,
    val isLoadingCatalogDetails: Boolean = false,
    val catalogDetailsError: String? = null,
    val preloadedDetailsCache: Map<String, Apps.CatalogAppDetails> = emptyMap(),
    val isInstalling: StateFlow<Boolean> = _isInstalling.asStateFlow(),
    val installError: StateFlow<String?> = _installError.asStateFlow(),
    val installJobId: StateFlow<Int?> = _installJobId.asStateFlow()
)
data class AppConfigUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val config: Map<String, Any>? = null,
    val error: String? = null,
    val saveJobId: Int? = null,
    val saveSuccess: Boolean = false,
    val saveFailed: Boolean = false,
    val saveError: String? = null
)

class AppsScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsScreenUiState())
    val uiState: StateFlow<AppsScreenUiState> = _uiState.asStateFlow()
    private val _appConfigState = MutableStateFlow(AppConfigUiState())
    val appConfigState: StateFlow<AppConfigUiState> = _appConfigState.asStateFlow()

    init {
        val cachedData = AppCache.cachedApps.value
        if (cachedData.isNotEmpty()) {
            _uiState.update { it.copy(apps = cachedData, isLoading = false) }
        }
        loadApps()
        startPeriodicRefresh()
    }


    fun updateCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    fun clearInstallState() {
        _isInstalling.value = false
        _installError.value = null
        _installJobId.value = null
    }
    fun loadAppConfig(appName: String) {
        viewModelScope.launch {
            _appConfigState.update { it.copy(isLoading = true, error = null) }
            when (val result = manager.apps.getAppConfig(appName)) {
                is ApiResult.Success -> {
                    val safeConfig = result.data.filterValues { it != null }
                    _appConfigState.update {
                        it.copy(isLoading = false, config = safeConfig, error = null)
                    }
                }
                is ApiResult.Error -> {
                    _appConfigState.update {
                        it.copy(isLoading = false, config = null, error = result.message)
                    }
                }
                is ApiResult.Loading -> { /* Handled by above */}
            }
        }
    }
    fun updateAppConfig(context: Context,appName: String, updatedValues: Map<String, Any?>) {
        viewModelScope.launch {
            _appConfigState.update {
                it.copy(isSaving = true, saveSuccess = false, saveError = null, saveFailed = false, error = null)
            }
            val cleanValues = updatedValues.filterValues { it != null }.mapValues { (_, v) -> v as Any }
            val options = Apps.UpdateAppConfigOptions(values = cleanValues)
            when (val result = manager.apps.updateAppConfig(appName, options)) {
                is ApiResult.Success -> {
                    _appConfigState.update {
                        it.copy(isSaving = false, saveJobId = result.data, saveSuccess = true, saveFailed = false)
                    }
                    GlobalJobTracker.startTracking(
                        context = context,
                        manager = manager,
                        jobId = result.data,
                        appName = appName,
                        showNotif = true
                    )
                    delay(1500.milliseconds)
                    _appConfigState.update { it.copy(saveSuccess = false) }
                }
                is ApiResult.Error -> {
                    _appConfigState.update {
                        it.copy(isSaving = false, saveSuccess = false, saveFailed = true, saveError = result.message)
                    }
                }
                is ApiResult.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Upgrades all apps that currently allow it.
     *
     * When [wakeStoppedApps] is true, apps that have updates but are asleep
     * (STOPPED) will first be started and polled until they reach a RUNNING
     * state before being upgraded. Otherwise only the apps already in a
     * non-stopped state (RUNNING/DEPLOYING) are upgraded.
     */
    fun upgradeAllApps(context: Context, wakeStoppedApps: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value.apps.none { it.upgrade_available }) {
                ToastManager.showInfo("No updates available")
                return@launch
            }

            // Apps we can upgrade right now without waking anything.
            val readyApps = _uiState.value.apps.filter { it.canUpgradeNow() }
            // Apps that need to be started before they can be upgraded.
            val sleepingApps = _uiState.value.apps.filter { it.upgrade_available && it.isAsleep() }

            readyApps.forEach { app ->
                upgradeAppToLatest(app.name, context)
                delay(500.milliseconds)
            }

            if (wakeStoppedApps && sleepingApps.isNotEmpty()) {
                val woken = startAppsAndWaitForRunning(sleepingApps)
                woken.forEach { app ->
                    upgradeAppToLatest(app.name, context)
                    delay(500.milliseconds)
                }
            }
        }
    }

    /**
     * Starts the given asleep apps and polls their state until each reaches
     * RUNNING (or the wait budget is exhausted). Returns the apps that reached
     * RUNNING and should therefore be upgraded.
     */
    private suspend fun startAppsAndWaitForRunning(
        apps: List<Apps.AppQueryResponse>
    ): List<Apps.AppQueryResponse> {
        val woken = mutableListOf<Apps.AppQueryResponse>()

        apps.forEach { app ->
            // Fire the start request (ignore errors here; some states may report
            // that a start is already in progress, which is fine).
            manager.apps.startAppWithResult(app.name)

            // Poll until the app reports RUNNING or the budget runs out.
            var reachedRunning = false
            repeat(20) {
                when (val q = manager.apps.getAppByName(app.name)) {
                    is ApiResult.Success -> {
                        if (q.data?.state.equals("RUNNING", ignoreCase = true)) {
                            reachedRunning = true
                            return@repeat
                        }
                    }
                    else -> {}
                }
                delay(1500.milliseconds)
            }

            if (reachedRunning) {
                woken.add(app)
            } else {
                _uiState.update {
                    it.copy(error = "Started ${app.name} but it did not reach RUNNING; it will not be upgraded.")
                }
            }
        }

        return woken
    }

    private suspend fun upgradeAppToLatest(appName: String, context: Context) {
        try {
            when (val summaryResult = manager.apps.getUpgradeSummaryWithResult(appName, "latest")) {
                is ApiResult.Success -> {
                    val latestVersion = summaryResult.data.upgrade_version
                    when (val result = manager.apps.upgradeAppWithResult(appName, latestVersion, true)) {
                        is ApiResult.Success -> {
                            val jobId = result.data
                            GlobalJobTracker.startTracking(
                                context = context.applicationContext,
                                manager = manager,
                                jobId = jobId,
                                appName = appName,
                                showNotif = true
                            )
                        }
                        is ApiResult.Error -> {
                            _uiState.update { it.copy(error = "Failed to upgrade $appName: ${result.message}") }
                        }
                        else -> {}
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = "Failed to get latest version for $appName") }
                }
                else -> {}
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error upgrading $appName: ${e.message}") }
        }
    }
    fun deleteApp(context: Context, appName: String, options: Apps.DeleteAppOptions = Apps.DeleteAppOptions()) {
        viewModelScope.launch {
            when (val result = manager.apps.removeAppWithResult(appName, options)) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    GlobalJobTracker.startTracking(
                        context = context,
                        manager = manager,
                        jobId = jobId,
                        appName = appName,
                        showNotif = true
                    )
                    refresh()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }
    fun clearAppConfigError() {
        _appConfigState.update {
            it.copy(error = null, saveFailed = false, saveError = null, saveJobId = null, saveSuccess = false)
        }
    }
    fun installMarketplaceApp(
        context: Context,
        appName: String,
        catalogApp: String,
        train: String,
        version: String,
        flatValues: Map<String, Any?>,
        unflattenMapFunc: (Map<String, Any?>) -> Map<String, Any?>
    ) {
        viewModelScope.launch {
            _isInstalling.value = true
            _installError.value = null

            val structuralValues = unflattenMapFunc(flatValues)

            when (val result = manager.apps.createAppWithResult(
                appName = appName,
                catalogApp = catalogApp,
                train = train,
                version = version,
                values = structuralValues
            )) {
                is ApiResult.Success -> {
                    _installJobId.value = result.data
                    GlobalJobTracker.startTracking(
                        context = context,
                        manager = manager,
                        jobId = result.data,
                        appName = appName,
                        showNotif = true
                    )
                }
                is ApiResult.Error -> {
                    _isInstalling.value = false
                    _installError.value = result.message
                }
                else -> {
                    _isInstalling.value = false
                }
            }
        }
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

    fun loadMarketplaceApps() {
        viewModelScope.launch {
            if (_uiState.value.marketplaceApps.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isRefreshing = true) }
            }
            try {
                when (val result = manager.apps.queryMarketplaceAvailableItems()) {
                    is ApiResult.Success -> {
                        AppCache.updateMarketplaceApps(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                marketplaceApps = result.data,
                                error = null
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = if (it.marketplaceApps.isEmpty()) result.message else null
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
                        error = if (it.marketplaceApps.isEmpty()) e.message ?: "Failed to load apps" else null
                    )
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
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadApps()
                }
                is ApiResult.Error -> {
                    ToastManager.showSuccess("Failed to Start Application")
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

    fun upgradeApp(appName: String, context: Context, version: String, backup: Boolean) {
        viewModelScope.launch {
            when (val result = manager.apps.upgradeAppWithResult(appName, version, backup)) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    GlobalJobTracker.startTracking(
                        context = context.applicationContext,
                        manager = manager,
                        jobId = jobId,
                        appName = appName,
                        showNotif = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
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

    fun rollbackApp(context: Context, appName: String, version: String, rollbackSnapshot: Boolean = true) {
        viewModelScope.launch {
            val result = manager.apps.rollbackAppWithResult(appName, version, rollbackSnapshot)
            when (result) {
                is ApiResult.Success -> {
                    GlobalJobTracker.startTracking(
                        context = context.applicationContext,
                        manager = manager,
                        jobId = result.data,
                        appName = appName,
                        showNotif = true
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }
    fun preloadCatalogDetails(appName: String, train: String) {
        if (_uiState.value.preloadedDetailsCache.containsKey(appName)) return
        viewModelScope.launch {
            when (val result = manager.apps.getCatalogAppDetails(appName, train)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            catalogAppDetails = result.data,
                            isLoadingCatalogDetails = false,
                            preloadedDetailsCache = it.preloadedDetailsCache + (appName to result.data)
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(catalogDetailsError = result.message, isLoadingCatalogDetails = false) }
                }
                else -> {}
            }
        }
    }

    fun loadCatalogAppDetails(appName: String, train: String) {

        val cached = _uiState.value.preloadedDetailsCache[appName]
        if (cached != null) {
            _uiState.update {
                it.copy(
                    catalogAppDetails = cached,
                    isLoadingCatalogDetails = false,
                    catalogDetailsError = null
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCatalogDetails = true, catalogDetailsError = null) }
            when (val result = manager.apps.getCatalogAppDetails(appName, train)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCatalogDetails = false,
                            catalogAppDetails = result.data,
                            preloadedDetailsCache = it.preloadedDetailsCache + (appName to result.data),
                            catalogDetailsError = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingCatalogDetails = false, catalogDetailsError = result.message)
                    }
                }
                else -> {}
            }
        }
    }

    fun clearCatalogAppDetails() {
        _uiState.update { it.copy(catalogAppDetails = null, catalogDetailsError = null) }
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