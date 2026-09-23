package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HardwareInformationUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val systemInfo: System.SystemInfo? = null,
    val hostId: String? = null,
    val bootId: String? = null,
    val chassisHardware: String? = null,
    val isIxHardware: Boolean? = null,
    val model: String? = null,
    val diskCount: Int = 0,
    val disks: List<System.DiskDetails> = emptyList(),
    val poolsHealthy: Boolean? = null,
    val error: String? = null
)

@HiltViewModel
class HardwareInformationViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareInformationUiState())
    val uiState: StateFlow<HardwareInformationUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val infoDeferred = async { manager.system.getSystemInfoWithResult() }
            val hostIdDeferred = async { manager.system.getHostId() }
            val bootIdDeferred = async { manager.system.getBootId() }
            val chassisDeferred = async { manager.system.getChassisHardware() }
            val ixHardwareDeferred = async { manager.system.isIxHardware() }
            val disksDeferred = async { manager.system.getDisksWithResult() }
            val poolsDeferred = async { manager.system.getPoolsWithResult() }

            val systemInfo = (infoDeferred.await() as? ApiResult.Success)?.data
            val hostId = (hostIdDeferred.await() as? ApiResult.Success)?.data
            val bootId = (bootIdDeferred.await() as? ApiResult.Success)?.data
            val chassis = (chassisDeferred.await() as? ApiResult.Success)?.data
            val ixHardware = (ixHardwareDeferred.await() as? ApiResult.Success)?.data
            val disks = (disksDeferred.await() as? ApiResult.Success)?.data.orEmpty()
            val pools = (poolsDeferred.await() as? ApiResult.Success)?.data.orEmpty()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    systemInfo = systemInfo ?: it.systemInfo,
                    hostId = hostId ?: it.hostId,
                    bootId = bootId ?: it.bootId,
                    chassisHardware = chassis ?: it.chassisHardware,
                    isIxHardware = ixHardware ?: it.isIxHardware,
                    model = systemInfo?.model ?: it.model,
                    diskCount = disks.size,
                    disks = disks,
                    poolsHealthy = pools.firstOrNull()?.healthy
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
