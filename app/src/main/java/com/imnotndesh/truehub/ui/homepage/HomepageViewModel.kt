package com.imnotndesh.truehub.ui.homepage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.utils.AppCache
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val systemInfo: System.SystemInfo,
        val systemVersionShort: String? = null,
        val poolDetails: List<System.Pool>,
        val diskDetails: List<System.DiskDetails>,
        val nfsShares : List<Shares.NfsShare>,
        val cpuData: List<System.ReportingGraphResponse>?,
        val memoryData: List<System.ReportingGraphResponse>?,
        val temperatureData: List<System.ReportingGraphResponse>? = null,
        val smbShares: List<Shares.SmbShare> = emptyList(),
        val isRefreshing: Boolean = false,
        val systemUpdateVersions: List<System.UpdateAvailableVersionsResponse> = emptyList()
    ) : HomeUiState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : HomeUiState()
}

/*
sealed class LoadAveragesState {
    object Loading : LoadAveragesState()
    data class Success(
        val cpuAverage: Double?,
        val memoryAverage: Double?,
        val loadAverage: Double?,
        val tempAverage: Double?
    ) : LoadAveragesState()
    data class Error(val message: String) : LoadAveragesState()
}
*/

class HomeViewModel(
    private val apiManager: TrueNASApiManager,
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // private val _loadAverages = MutableStateFlow<LoadAveragesState>(LoadAveragesState.Loading)
    // val loadAverages: StateFlow<LoadAveragesState> = _loadAverages.asStateFlow()

    // private var averagesPolling = false

    init {
        startConnectivityMonitoring()
        loadDashboardData()
        loadUserData()
    }

    private fun startConnectivityMonitoring() {
        viewModelScope.launch {
            while (true) {
                checkConnectivity()
                delay(30_000)
            }
        }
    }

    /**
     * Called once after dashboard data is loaded.
     * Polls load averages every 30 seconds (up from 10s) to reduce API pressure.
     */
    /*
    private fun startLoadAveragesMonitoring() {
        if (averagesPolling) return
        averagesPolling = true
        viewModelScope.launch {
            // Initial load
            loadLoadAverages()
            while (true) {
                delay(30_000)
                loadLoadAverages()
            }
        }
    }

    private fun loadLoadAverages() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is HomeUiState.Success) return@launch

                val systemInfo = currentState.systemInfo

                // Single graph query for all needed metrics to reduce round trips
                val result = apiManager.system.getReportingDataWithResult(
                    listOf(
                        System.ReportingGraphRequest(System.ReportingGraphName.CPU),
                        System.ReportingGraphRequest(System.ReportingGraphName.MEMORY),
                        System.ReportingGraphRequest(System.ReportingGraphName.CPUTEMP),
                        System.ReportingGraphRequest(System.ReportingGraphName.LOAD)
                    ),
                    System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                )

                if (result !is ApiResult.Success || result.data.isNullOrEmpty()) return@launch

                val dataList = result.data
                val cpuData = dataList.find { it.name == System.ReportingGraphName.CPU.name.lowercase() }?.data
                val memoryData = dataList.find { it.name == System.ReportingGraphName.MEMORY.name.lowercase() }?.data
                val tempData = dataList.find { it.name == System.ReportingGraphName.CPUTEMP.name.lowercase() }?.data
                val loadData = dataList.find { it.name == System.ReportingGraphName.LOAD.name.lowercase() }?.data

                fun calculateAverage(data: List<List<Double>>?): Double? {
                    if (data.isNullOrEmpty()) return null
                    val values = data.flatMap { row -> row.drop(1) }
                    return if (values.isNotEmpty()) values.average() else null
                }

                fun calculateMemoryAverage(data: List<List<Double>>?): Double? {
                    if (data.isNullOrEmpty()) return null
                    val values = data.mapNotNull { row -> row.getOrNull(1) }
                    if (values.isEmpty()) return null
                    val avgBytes = values.average()
                    val totalBytes = systemInfo.physmem.toDouble()
                    return (avgBytes / totalBytes) * 100.0
                }

                _loadAverages.value = LoadAveragesState.Success(
                    cpuAverage = calculateAverage(cpuData),
                    memoryAverage = calculateMemoryAverage(memoryData),
                    loadAverage = calculateAverage(loadData),
                    tempAverage = calculateAverage(tempData)
                )

            } catch (e: Exception) {
                _loadAverages.value = LoadAveragesState.Error(e.message ?: "Failed to load averages")
            }
        }
    }
    */

    // ═══════════════════════════════════════════════════════════
    //  Connectivity & Refresh
    // ═══════════════════════════════════════════════════════════

    private suspend fun checkConnectivity() {
        try {
            val result = apiManager.system.getSystemInfoWithResult()
            if (result is ApiResult.Success) {
                _isConnected.value = true
            } else if (result is ApiResult.Error) {
                _isConnected.value = false
            }
        } catch (e: Exception) {
            _isConnected.value = false
        }
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

    fun clearError() {
        if (_uiState.value is HomeUiState.Error) {
            _uiState.value = HomeUiState.Loading
            loadDashboardData()
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Dashboard data load (batched to reduce API pressure)
    // ═══════════════════════════════════════════════════════════

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val systemInfoResult = apiManager.system.getSystemInfoWithResult()
                if (systemInfoResult !is ApiResult.Success) {
                    _uiState.value = HomeUiState.Error("Failed to load system information")
                    _isConnected.value = false
                    return@launch
                }

                _isConnected.value = true
                val systemInfo = systemInfoResult.data
                AppCache.updateSystemInfo(systemInfo)

                // Concurrent batch: pools + disks + shares + update versions + version short
                val poolsResult = async { apiManager.system.getPoolsWithResult() }
                val disksResult = async { apiManager.system.getDisksWithResult() }
                val smbSharesResult = async { apiManager.sharing.getSmbSharesWithResult() }
                val nfsSharesResult = async { apiManager.sharing.getNfsSharesWithResult() }
                val updateVersionsResult = async { apiManager.system.getSystemUpdateVersions() }
                val versionShortResult = async { apiManager.system.getSystemVersionShort() }

                // Graph data: one combined call for all metrics (CPU, MEM, TEMP)
                val graphResult = async {
                    apiManager.system.getReportingDataWithResult(
                        listOf(
                            System.ReportingGraphRequest(System.ReportingGraphName.CPU),
                            System.ReportingGraphRequest(System.ReportingGraphName.MEMORY),
                            System.ReportingGraphRequest(System.ReportingGraphName.CPUTEMP)
                        ),
                        System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    )
                }

                val pools = (poolsResult.await() as? ApiResult.Success)?.data ?: emptyList()
                val disks = (disksResult.await() as? ApiResult.Success)?.data ?: emptyList()
                val smbShares = (smbSharesResult.await() as? ApiResult.Success)?.data ?: emptyList()
                val nfsShares = (nfsSharesResult.await() as? ApiResult.Success)?.data ?: emptyList()
                val updateVersions = (updateVersionsResult.await() as? ApiResult.Success)?.data ?: emptyList()
                val versionShort = (versionShortResult.await() as? ApiResult.Success)?.data

                // Parse combined graph response
                val graphData = (graphResult.await() as? ApiResult.Success)?.data
                val cpuData = graphData?.find { it.name == System.ReportingGraphName.CPU.name.lowercase() }
                val memoryData = graphData?.find { it.name == System.ReportingGraphName.MEMORY.name.lowercase() }
                val tempData = graphData?.find { it.name == System.ReportingGraphName.CPUTEMP.name.lowercase() }

                AppCache.updatePools(pools)
                AppCache.updateDisks(disks)
                AppCache.updateSmbShares(smbShares)
                AppCache.updateNfsShares(nfsShares)
                AppCache.updateSystemUpdateVersions(updateVersions)

                _uiState.value = HomeUiState.Success(
                    systemInfo = systemInfo,
                    systemVersionShort = versionShort,
                    poolDetails = pools,
                    diskDetails = disks,
                    cpuData = if (cpuData != null) listOf(cpuData) else null,
                    memoryData = if (memoryData != null) listOf(memoryData) else null,
                    temperatureData = if (tempData != null) listOf(tempData) else null,
                    smbShares = smbShares,
                    nfsShares = nfsShares,
                    systemUpdateVersions = updateVersions,
                    isRefreshing = false
                )

                // Start load averages polling AFTER main data is loaded
                // startLoadAveragesMonitoring()  // commented out — too expensive

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Unknown error occurred",
                    canRetry = true
                )
                _isConnected.value = false
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Actions
    // ═══════════════════════════════════════════════════════════

    fun shutdownSystem(reason: String) {
        viewModelScope.launch {
            try {
                ToastManager.showInfo("Initiating system shutdown...")
                val result = apiManager.system.shutdownSystemWithResult(reason)
                when (result) {
                    is ApiResult.Success -> ToastManager.showSuccess("System shutdown initiated successfully")
                    is ApiResult.Error -> ToastManager.showError(result.message)
                    is ApiResult.Loading -> { /* no-op */ }
                }
            } catch (_: Exception) { /* fail silent */ }
        }
    }

    fun rebootSystem(reason: String = "") {
        viewModelScope.launch {
            try {
                ToastManager.showInfo("Initiating system restart...")
                val result = apiManager.system.rebootSystem(reason)
                when (result) {
                    is ApiResult.Success -> ToastManager.showSuccess("System restart initiated successfully")
                    is ApiResult.Error -> ToastManager.showError(result.message)
                    is ApiResult.Loading -> { /* no-op */ }
                }
            } catch (_: Exception) { /* fail silent */ }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                when (val res = apiManager.auth.getUserDetailsWithResult()) {
                    is ApiResult.Success -> {
                        EncryptedPrefs.saveUsername(applicationContext, res.data.pw_name.toString())
                    }
                    is ApiResult.Error -> { /* silent */ }
                    is ApiResult.Loading -> { /* no-op */ }
                }
            } catch (_: Exception) { /* fail silent */ }
        }
    }

    class HomeViewModelFactory(
        private val apiManager: TrueNASApiManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(apiManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
