package com.imnotndesh.truehub.ui.homepage.instancesettings.globalvirt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlobalVirtUiState(
    val config: Virt.GlobalEntry? = null,
    val poolChoices: Map<String, String> = emptyMap(),
    val bridgeChoices: Map<String, String> = emptyMap(),
    val bridgeNetwork: Virt.GlobalNetwork? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val saveJob: System.Job? = null,
    val error: String? = null
)

@HiltViewModel
class GlobalVirtSettingsViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalVirtUiState())
    val uiState: StateFlow<GlobalVirtUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        loadInternal(isRefresh = false)
    }

    fun refresh() {
        loadInternal(isRefresh = true)
    }

    private fun loadInternal(isRefresh: Boolean) {
        viewModelScope.launch {
            val hasConfig = _uiState.value.config != null
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && !hasConfig,
                    isRefreshing = isRefresh || hasConfig,
                    error = null
                )
            }
            val configResult = manager.virtService.getGlobalConfigWithResult()
            val poolsResult = manager.virtService.getGlobalPoolChoicesWithResult()
            val bridgesResult = manager.virtService.getGlobalBridgeChoicesWithResult()
            _uiState.update {
                it.copy(
                    config = (configResult as? ApiResult.Success)?.data ?: it.config,
                    poolChoices = (poolsResult as? ApiResult.Success)?.data ?: it.poolChoices,
                    bridgeChoices = (bridgesResult as? ApiResult.Success)?.data ?: it.bridgeChoices,
                    isLoading = false,
                    isRefreshing = false,
                    error = listOfNotNull(
                        (configResult as? ApiResult.Error)?.message,
                        (poolsResult as? ApiResult.Error)?.message,
                        (bridgesResult as? ApiResult.Error)?.message
                    ).firstOrNull()
                )
            }
            (configResult as? ApiResult.Success)?.data?.bridge?.let { loadBridgeNetwork(it) }
        }
    }

    fun loadBridgeNetwork(bridge: String) {
        if (bridge.isBlank() || bridge == "[AUTO]") {
            _uiState.update { it.copy(bridgeNetwork = null) }
            return
        }
        viewModelScope.launch {
            when (val result = manager.virtService.getGlobalNetworkWithResult(bridge)) {
                is ApiResult.Success -> _uiState.update { it.copy(bridgeNetwork = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(bridgeNetwork = null) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun update(
        pool: String,
        bridge: String,
        storagePools: List<String>,
        v4Network: String,
        v6Network: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveJob = null, error = null) }
            val update = Virt.GlobalUpdate(
                pool = pool.trim().ifBlank { null },
                bridge = bridge.trim().ifBlank { null },
                storagePools = storagePools.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null },
                v4Network = v4Network.trim().ifBlank { null },
                v6Network = v6Network.trim().ifBlank { null }
            )
            when (val result = manager.virtService.updateGlobalConfigWithResult(update)) {
                is ApiResult.Success -> trackSaveJob(result.data)
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun trackSaveJob(jobId: Int) {
        viewModelScope.launch {
            repeat(150) { attempt ->
                when (val result = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _uiState.update { it.copy(saveJob = job) }
                        when (job.state) {
                            "SUCCESS" -> {
                                _uiState.update { it.copy(isSaving = false, saveJob = null) }
                                loadInternal(isRefresh = false)
                                return@launch
                            }
                            "FAILED", "ABORTED" -> {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        error = job.exception?.takeIf { e -> e.isNotBlank() }
                                            ?: "Failed to update global settings"
                                    )
                                }
                                return@launch
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isSaving = false, error = result.message) }
                        return@launch
                    }
                    ApiResult.Loading -> Unit
                }
                if (attempt < 149) delay(2000)
            }
            _uiState.update { it.copy(isSaving = false, error = "Timed out waiting for the update") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
