package com.imnotndesh.truehub.ui.homepage.instancesettings.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdvancedSystemSettingsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val config: System.SystemAdvancedEntry? = null,
    val gpuPciChoices: Map<String, String> = emptyMap(),
    val serialPortChoices: Map<String, String> = emptyMap(),
    val syslogCaChoices: Map<String, String> = emptyMap(),
    val syslogCertChoices: Map<String, String> = emptyMap(),
    val loginBanner: String? = null,
    val sedPasswordIsSet: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AdvancedSystemSettingsViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {
    init {
        prefetchChoices()
    }

    private fun prefetchChoices() {
        if (cachedGpuPciChoices != null) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            cachedGpuPciChoices = (manager.system.advancedGpuPciChoices() as? ApiResult.Success)?.data
            cachedSerialPortChoices = (manager.system.advancedSerialPortChoices() as? ApiResult.Success)?.data
            cachedSyslogCaChoices = (manager.system.advancedSyslogCertificateAuthorityChoices() as? ApiResult.Success)?.data
            cachedSyslogCertChoices = (manager.system.advancedSyslogCertificateChoices() as? ApiResult.Success)?.data
            _uiState.update {
                it.copy(
                    gpuPciChoices = cachedGpuPciChoices ?: emptyMap(),
                    serialPortChoices = cachedSerialPortChoices ?: emptyMap(),
                    syslogCaChoices = cachedSyslogCaChoices ?: emptyMap(),
                    syslogCertChoices = cachedSyslogCertChoices ?: emptyMap()
                )
            }
        }
    }
    private val _uiState = MutableStateFlow(AdvancedSystemSettingsUiState())
    val uiState: StateFlow<AdvancedSystemSettingsUiState> = _uiState.asStateFlow()

    fun refresh() = loadAll(forceRefresh = true)

    companion object {
        private var cachedGpuPciChoices: Map<String, String>? = null
        private var cachedSerialPortChoices: Map<String, String>? = null
        private var cachedSyslogCaChoices: Map<String, String>? = null
        private var cachedSyslogCertChoices: Map<String, String>? = null

        fun clearChoicesCache() {
            cachedGpuPciChoices = null
            cachedSerialPortChoices = null
            cachedSyslogCaChoices = null
            cachedSyslogCertChoices = null
        }
    }

    fun loadAll(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }
            try {

                val configResult = manager.system.advancedConfig()
                if (configResult is ApiResult.Error) {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = configResult.message) }
                    return@launch
                }


                if (cachedGpuPciChoices == null) {
                    cachedGpuPciChoices =
                        (manager.system.advancedGpuPciChoices() as? ApiResult.Success)?.data
                }
                if (cachedSerialPortChoices == null) {
                    cachedSerialPortChoices =
                        (manager.system.advancedSerialPortChoices() as? ApiResult.Success)?.data
                }
                if (cachedSyslogCaChoices == null) {
                    cachedSyslogCaChoices =
                        (manager.system.advancedSyslogCertificateAuthorityChoices() as? ApiResult.Success)?.data
                }
                if (cachedSyslogCertChoices == null) {
                    cachedSyslogCertChoices =
                        (manager.system.advancedSyslogCertificateChoices() as? ApiResult.Success)?.data
                }

                if (cachedGpuPciChoices == null) prefetchChoices()

                val loginBannerResult = manager.system.advancedLoginBanner()
                val sedPasswordIsSetResult = manager.system.advancedSedGlobalPasswordIsSet()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        config = (configResult as? ApiResult.Success)?.data,
                        gpuPciChoices = cachedGpuPciChoices ?: it.gpuPciChoices,
                        serialPortChoices = cachedSerialPortChoices ?: it.serialPortChoices,
                        syslogCaChoices = cachedSyslogCaChoices ?: it.syslogCaChoices,
                        syslogCertChoices = cachedSyslogCertChoices ?: it.syslogCertChoices,
                        loginBanner = (loginBannerResult as? ApiResult.Success)?.data,
                        sedPasswordIsSet = (sedPasswordIsSetResult as? ApiResult.Success)?.data ?: false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun updateSettings(data: System.SystemAdvancedUpdateArgs) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            when (val result = manager.system.advancedUpdate(data)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            config = result.data,
                            saveSuccess = true
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
