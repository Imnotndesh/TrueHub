package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SystemInformationUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val bootId: String? = null,
    val hostId: String? = null,
    val state: String? = null,
    val ready: Boolean? = null,
    val version: String? = null,
    val versionShort: String? = null,
    val productType: String? = null,
    val releaseNotesUrl: String? = null,
    val dedupEnabled: Boolean? = null,
    val fibreChannelEnabled: Boolean? = null,
    val vmEnabled: Boolean? = null,
    val chassisHardware: String? = null,
    val isIxHardware: Boolean? = null,
    val isProduction: Boolean? = null,
    val isEulaAccepted: Boolean? = null,
    val managedByTruecommand: Boolean? = null,
    val error: String? = null
)

@HiltViewModel
class SystemInformationViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemInformationUiState())
    val uiState: StateFlow<SystemInformationUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val hostIdDeferred = async { manager.system.getHostId() }
            val bootIdDeferred = async { manager.system.getBootId() }
            val stateDeferred = async { manager.system.getSystemState() }
            val readyDeferred = async { manager.system.isSystemReady() }
            val versionDeferred = async { manager.system.getSystemVersion() }
            val versionShortDeferred = async { manager.system.getSystemVersionShort() }
            val productTypeDeferred = async { manager.system.getProductType() }
            val releaseNotesDeferred = async { manager.system.getReleaseNotesUrl(null) }
            val dedupDeferred = async { manager.system.isFeatureEnabled("DEDUP") }
            val fcDeferred = async { manager.system.isFeatureEnabled("FIBRECHANNEL") }
            val vmDeferred = async { manager.system.isFeatureEnabled("VM") }
            val chassisDeferred = async { manager.system.getChassisHardware() }
            val ixHardwareDeferred = async { manager.system.isIxHardware() }
            val productionDeferred = async { manager.system.isProduction() }
            val eulaAcceptedDeferred = async { manager.system.isEulaAccepted() }
            val managedByDeferred = async { manager.system.isManagedByTruecommand() }

            val hostId = (hostIdDeferred.await() as? ApiResult.Success)?.data
            val bootId = (bootIdDeferred.await() as? ApiResult.Success)?.data
            val state = (stateDeferred.await() as? ApiResult.Success)?.data
            val ready = (readyDeferred.await() as? ApiResult.Success)?.data
            val version = (versionDeferred.await() as? ApiResult.Success)?.data
            val versionShort = (versionShortDeferred.await() as? ApiResult.Success)?.data
            val productType = (productTypeDeferred.await() as? ApiResult.Success)?.data
            val releaseNotesUrl = (releaseNotesDeferred.await() as? ApiResult.Success)?.data
            val dedup = (dedupDeferred.await() as? ApiResult.Success)?.data
            val fc = (fcDeferred.await() as? ApiResult.Success)?.data
            val vm = (vmDeferred.await() as? ApiResult.Success)?.data
            val chassis = (chassisDeferred.await() as? ApiResult.Success)?.data
            val ixHardware = (ixHardwareDeferred.await() as? ApiResult.Success)?.data
            val production = (productionDeferred.await() as? ApiResult.Success)?.data
            val eulaAccepted = (eulaAcceptedDeferred.await() as? ApiResult.Success)?.data
            val managedBy = (managedByDeferred.await() as? ApiResult.Success)?.data

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    bootId = bootId ?: it.bootId,
                    hostId = hostId ?: it.hostId,
                    state = state ?: it.state,
                    ready = ready ?: it.ready,
                    version = version ?: it.version,
                    versionShort = versionShort ?: it.versionShort,
                    productType = productType ?: it.productType,
                    releaseNotesUrl = releaseNotesUrl ?: it.releaseNotesUrl,
                    dedupEnabled = dedup ?: it.dedupEnabled,
                    fibreChannelEnabled = fc ?: it.fibreChannelEnabled,
                    vmEnabled = vm ?: it.vmEnabled,
                    chassisHardware = chassis ?: it.chassisHardware,
                    isIxHardware = ixHardware ?: it.isIxHardware,
                    isProduction = production ?: it.isProduction,
                    isEulaAccepted = eulaAccepted ?: it.isEulaAccepted,
                    managedByTruecommand = managedBy ?: it.managedByTruecommand
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
