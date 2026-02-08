package com.imnotndesh.truehub.ui.homepage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.AppCache
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val systemInfo: System.SystemInfo,
        val poolDetails: List<System.Pool>,
        val diskDetails: List<System.DiskDetails>,
        val nfsShares : List<Shares.NfsShare>,
        val cpuData: List<System.ReportingGraphResponse>?,
        val memoryData: List<System.ReportingGraphResponse>?,
        val temperatureData: List<System.ReportingGraphResponse>? = null,
        val smbShares: List<Shares.SmbShare> = emptyList(),
        val isRefreshing: Boolean = false
    ) : HomeUiState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : HomeUiState()
}

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

class HomeViewModel(
    private val apiManager: TrueNASApiManager,
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var _performanceDataLoading = MutableStateFlow(false)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)

    private val _loadAverages = MutableStateFlow<LoadAveragesState>(LoadAveragesState.Loading)
    val loadAverages: StateFlow<LoadAveragesState> = _loadAverages.asStateFlow()

    init {
        startConnectivityMonitoring()
        loadDashboardData()
        loadUserData()
        startLoadAveragesMonitoring()
    }

    private fun startConnectivityMonitoring() {
        viewModelScope.launch {
            while (true) {
                checkConnectivity()
                delay(30000)
            }
        }

    }
    private fun startLoadAveragesMonitoring() {
        viewModelScope.launch {
            // Initial load
            loadLoadAverages()

            // Periodic updates every 10 seconds
            while (true) {
                delay(10000)
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

                // Fetch all metrics concurrently
                val cpuDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.CPU))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                val memoryDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.MEMORY))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                val tempDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.CPUTEMP))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                val loadDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.LOAD))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                // Await results
                val cpuResult = cpuDeferred.await()
                val memoryResult = memoryDeferred.await()
                val tempResult = tempDeferred.await()
                val loadResult = loadDeferred.await()

                // Helper functions
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

                val cpuAvg = calculateAverage(
                    (cpuResult as? ApiResult.Success)?.data?.firstOrNull()?.data
                )
                val memoryAvg = calculateMemoryAverage(
                    (memoryResult as? ApiResult.Success)?.data?.firstOrNull()?.data
                )
                val tempAvg = calculateAverage(
                    (tempResult as? ApiResult.Success)?.data?.firstOrNull()?.data
                )
                val loadAvg = calculateAverage(
                    (loadResult as? ApiResult.Success)?.data?.firstOrNull()?.data
                )

                _loadAverages.value = LoadAveragesState.Success(
                    cpuAverage = cpuAvg,
                    memoryAverage = memoryAvg,
                    loadAverage = loadAvg,
                    tempAverage = tempAvg
                )

            } catch (e: Exception) {
                _loadAverages.value = LoadAveragesState.Error(e.message ?: "Failed to load averages")
            }
        }
    }
    private suspend fun checkConnectivity() {
        try {
            val result = apiManager.system.getSystemInfoWithResult()

            if (result is ApiResult.Success) {
                _isConnected.value = true
                _connectionError.value = null
            } else if (result is ApiResult.Error) {
                _isConnected.value = false
                _connectionError.value = result.message
            }
        } catch (e: Exception) {
            _isConnected.value = false
            _connectionError.value = e.message
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

                val poolDeferred = async {
                    apiManager.system.getPoolsWithResult()
                }
                val diskDeferred = async {
                    apiManager.system.getDisksWithResult()
                }
                val smbSharesDeferred = async {
                    apiManager.sharing.getSmbSharesWithResult()
                }
                val nfsSharesDeferred = async {
                    apiManager.sharing.getNfsSharesWithResult()
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
                val tempDataDeferred = async {
                    val cpuGraphRequest = listOf(
                        System.ReportingGraphRequest(
                            name = System.ReportingGraphName.CPUTEMP
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

                val poolResult = poolDeferred.await()
                val diskResult = diskDeferred.await()
                val smbSharesResult = smbSharesDeferred.await()
                val nfsSharesResult = nfsSharesDeferred.await()
                val cpuDataResult = cpuDataDeferred.await()
                val memoryDataResult = memoryDataDeferred.await()
                val tempDataResult = tempDataDeferred.await()

                val pools = if (poolResult is ApiResult.Success) poolResult.data else emptyList()
                val disks = if (diskResult is ApiResult.Success) diskResult.data else emptyList()
                val smbShares = if (smbSharesResult is ApiResult.Success) smbSharesResult.data else emptyList()
                val nfsShares = if (nfsSharesResult is ApiResult.Success) nfsSharesResult.data else emptyList()

                AppCache.updatePools(pools)
                AppCache.updateDisks(disks)
                AppCache.updateSmbShares(smbShares)
                AppCache.updateNfsShares(nfsShares)

                _uiState.value = HomeUiState.Success(
                    systemInfo = systemInfo,
                    poolDetails = pools,
                    diskDetails = disks,
                    cpuData = if (cpuDataResult is ApiResult.Success) cpuDataResult.data else null,
                    memoryData = if (memoryDataResult is ApiResult.Success) memoryDataResult.data else null,
                    temperatureData = if (tempDataResult is ApiResult.Success) tempDataResult.data else null,
                    smbShares = smbShares,
                    nfsShares = nfsShares,
                    isRefreshing = false
                )

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Unknown error occurred",
                    canRetry = true
                )

                _isConnected.value = false
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

                    }
                    ApiResult.Loading -> {
                        ToastManager.showInfo("Initiating system shutdown...")
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    fun clearError() {
        if (_uiState.value is HomeUiState.Error) {
            _uiState.value = HomeUiState.Loading
            loadDashboardData()
        }
    }

    fun loadUserData(){
        viewModelScope.launch {
            try {
                when (val res = apiManager.auth.getUserDetailsWithResult()){
                    is ApiResult.Error ->{
                        ToastManager.showWarning("Failed to get user information")
                    }
                    ApiResult.Loading ->{
                        /**
                         * Nothing to show really since it is a background process
                         */
                    }
                    is ApiResult.Success ->{
                        EncryptedPrefs.saveUsername(applicationContext,res.data.pw_name.toString())
                    }
                }
            }catch (_: Exception){
                /**
                 * Fail silently
                 */
                ToastManager.showWarning("Failed to get user information")
            }
        }
    }

    fun loadPerformanceData(): Triple<List<System.ReportingGraphResponse>?, List<System.ReportingGraphResponse>?, List<System.ReportingGraphResponse>?> {
        var cpuData: List<System.ReportingGraphResponse>? = null
        var memoryData: List<System.ReportingGraphResponse>? = null
        var temperatureData: List<System.ReportingGraphResponse>? = null

        viewModelScope.launch {
            _performanceDataLoading.value = true

            try {
                // Fetch CPU data
                val cpuDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.CPU))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                // Fetch Memory data
                val memoryDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.MEMORY))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                // Fetch CPU Temperature data
                val tempDeferred = async {
                    val request = listOf(System.ReportingGraphRequest(System.ReportingGraphName.CPUTEMP))
                    val query = System.ReportingGraphQuery(unit = System.ReportingUnit.HOUR, aggregate = true)
                    apiManager.system.getReportingDataWithResult(request, query)
                }

                // Await results
                val cpuResult = cpuDeferred.await()
                val memoryResult = memoryDeferred.await()
                val tempResult = tempDeferred.await()

                cpuData = if (cpuResult is ApiResult.Success) cpuResult.data else null
                memoryData = if (memoryResult is ApiResult.Success) memoryResult.data else null
                temperatureData = if (tempResult is ApiResult.Success) tempResult.data else null

            } catch (_: Exception) {

            } finally {
                _performanceDataLoading.value = false
            }
        }

        return Triple(cpuData, memoryData, temperatureData)
    }

    class HomeViewModelFactory(
        private val apiManager: TrueNASApiManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(apiManager,context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}