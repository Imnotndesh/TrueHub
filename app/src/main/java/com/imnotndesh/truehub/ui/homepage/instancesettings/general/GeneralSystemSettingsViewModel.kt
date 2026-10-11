package com.imnotndesh.truehub.ui.homepage.instancesettings.general

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

data class GeneralSystemSettingsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val config: System.SystemGeneralEntry? = null,
    val timezoneChoices: Map<String, String> = emptyMap(),
    val kbdmapChoices: Map<String, String> = emptyMap(),
    val uiAddressChoices: Map<String, String> = emptyMap(),
    val uiV6AddressChoices: Map<String, String> = emptyMap(),
    val uiCertificateChoices: Map<String, String> = emptyMap(),
    val uiHttpsProtocolsChoices: Map<String, String> = emptyMap(),
    val countryChoices: Map<String, String> = emptyMap(),
    val localUrl: String? = null,
    val checkinWaiting: Int? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false
)
@HiltViewModel
class GeneralSystemSettingsViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {
    init {
        prefetchChoices()
    }

    private fun prefetchChoices() {
        if (cachedTimezoneChoices != null) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            cachedTimezoneChoices = (manager.system.generalTimezoneChoices() as? ApiResult.Success)?.data
            cachedKbdmapChoices = (manager.system.generalKbdmapChoices() as? ApiResult.Success)?.data
            cachedUiAddressChoices = (manager.system.generalUiAddressChoices() as? ApiResult.Success)?.data
            cachedUiV6AddressChoices = (manager.system.generalUiV6AddressChoices() as? ApiResult.Success)?.data
            cachedUiCertificateChoices = (manager.system.generalUiCertificateChoices() as? ApiResult.Success)?.data
            cachedUiHttpsProtocolsChoices = (manager.system.generalUiHttpsProtocolsChoices() as? ApiResult.Success)?.data
            cachedCountryChoices = (manager.system.generalCountryChoices() as? ApiResult.Success)?.data
            _uiState.update {
                it.copy(
                    timezoneChoices = cachedTimezoneChoices ?: emptyMap(),
                    kbdmapChoices = cachedKbdmapChoices ?: emptyMap(),
                    uiAddressChoices = cachedUiAddressChoices ?: emptyMap(),
                    uiV6AddressChoices = cachedUiV6AddressChoices ?: emptyMap(),
                    uiCertificateChoices = cachedUiCertificateChoices ?: emptyMap(),
                    uiHttpsProtocolsChoices = cachedUiHttpsProtocolsChoices ?: emptyMap(),
                    countryChoices = cachedCountryChoices ?: emptyMap()
                )
            }
        }
    }
    private val _uiState = MutableStateFlow(GeneralSystemSettingsUiState())
    val uiState: StateFlow<GeneralSystemSettingsUiState> = _uiState.asStateFlow()

    fun refresh() = loadAll(forceRefresh = true)

    companion object {
        private var cachedTimezoneChoices: Map<String, String>? = null
        private var cachedKbdmapChoices: Map<String, String>? = null
        private var cachedUiAddressChoices: Map<String, String>? = null
        private var cachedUiV6AddressChoices: Map<String, String>? = null
        private var cachedUiCertificateChoices: Map<String, String>? = null
        private var cachedUiHttpsProtocolsChoices: Map<String, String>? = null
        private var cachedCountryChoices: Map<String, String>? = null

        /** Clears all cached choice data (e.g. after logout or theme change). */
        fun clearChoicesCache() {
            cachedTimezoneChoices = null
            cachedKbdmapChoices = null
            cachedUiAddressChoices = null
            cachedUiV6AddressChoices = null
            cachedUiCertificateChoices = null
            cachedUiHttpsProtocolsChoices = null
            cachedCountryChoices = null
        }
    }

    fun loadAll(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }
            try {
                // Load config (always fresh)
                val configResult = manager.system.generalConfig()
                if (configResult is ApiResult.Error) {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = configResult.message) }
                    return@launch
                }

                // ── Load choices, using cache when available ──
                if (cachedTimezoneChoices == null) {
                    cachedTimezoneChoices =
                        (manager.system.generalTimezoneChoices() as? ApiResult.Success)?.data
                }
                if (cachedKbdmapChoices == null) {
                    cachedKbdmapChoices =
                        (manager.system.generalKbdmapChoices() as? ApiResult.Success)?.data
                }
                if (cachedUiAddressChoices == null) {
                    cachedUiAddressChoices =
                        (manager.system.generalUiAddressChoices() as? ApiResult.Success)?.data
                }
                if (cachedUiV6AddressChoices == null) {
                    cachedUiV6AddressChoices =
                        (manager.system.generalUiV6AddressChoices() as? ApiResult.Success)?.data
                }
                if (cachedUiCertificateChoices == null) {
                    cachedUiCertificateChoices =
                        (manager.system.generalUiCertificateChoices() as? ApiResult.Success)?.data
                }
                if (cachedUiHttpsProtocolsChoices == null) {
                    cachedUiHttpsProtocolsChoices =
                        (manager.system.generalUiHttpsProtocolsChoices() as? ApiResult.Success)?.data
                }
                if (cachedCountryChoices == null) {
                    cachedCountryChoices =
                        (manager.system.generalCountryChoices() as? ApiResult.Success)?.data
                }

                // Always-fresh data
                val localUrlResult = manager.system.generalLocalUrl()
                val checkinResult = manager.system.generalCheckinWaiting()
                if (cachedTimezoneChoices == null) prefetchChoices()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        config = (configResult as? ApiResult.Success)?.data,
                        timezoneChoices = cachedTimezoneChoices ?: it.timezoneChoices,
                        kbdmapChoices = cachedKbdmapChoices ?: it.kbdmapChoices,
                        uiAddressChoices = cachedUiAddressChoices ?: it.uiAddressChoices,
                        uiV6AddressChoices = cachedUiV6AddressChoices ?: it.uiV6AddressChoices,
                        uiCertificateChoices = cachedUiCertificateChoices ?: it.uiCertificateChoices,
                        uiHttpsProtocolsChoices = cachedUiHttpsProtocolsChoices ?: it.uiHttpsProtocolsChoices,
                        countryChoices = cachedCountryChoices ?: it.countryChoices,
                        localUrl = (localUrlResult as? ApiResult.Success)?.data,
                        checkinWaiting = (checkinResult as? ApiResult.Success)?.data
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun updateSettings(settings: System.SystemGeneralUpdateArgs) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            when (val result = manager.system.generalUpdate(settings)) {
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

    fun doCheckin() {
        viewModelScope.launch {
            when (val result = manager.system.generalCheckin()) {
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> { /* success - no-op */ }
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
