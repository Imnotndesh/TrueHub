package com.imnotndesh.truehub.ui.homepage.instancesettings.boot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BootUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActing: Boolean = false,
    val bootState: System.BootGetState? = null,
    val disks: List<String> = emptyList(),
    val environments: List<System.BootEnvironmentQueryResultItem> = emptyList(),
    val error: String? = null,
    val actionMessage: String? = null
)

/**
 * Backs the [BootScreen] and [BootPoolScreen].
 *
 * Uses companion-level static caches (mirroring GeneralSystemSettingsViewModel) so that
 * navigating between the boot overview and boot pool detail screens stays snappy and does
 * not re-hit the network on every entry. Use [refresh] to reload from the server.
 */
@HiltViewModel
class BootViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BootUiState())
    val uiState: StateFlow<BootUiState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            if (forceRefresh || cachedBootState == null) {
                cachedBootState = (manager.system.getBootState() as? ApiResult.Success)?.data
            }
            if (forceRefresh || cachedDisks == null) {
                cachedDisks = (manager.system.getBootDisks() as? ApiResult.Success)?.data
            }
            if (forceRefresh || cachedEnvironments == null) {
                cachedEnvironments =
                    (manager.system.queryBootEnvironments() as? ApiResult.Success)?.data
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    bootState = cachedBootState,
                    disks = cachedDisks ?: emptyList(),
                    environments = cachedEnvironments ?: emptyList()
                )
            }
        }
    }

    // ── Boot pool actions ──
    fun scrubBootPool() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.scrubBootPool()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = "Scrub started on boot pool")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun setScrubInterval(interval: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.setBootScrubInterval(interval)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActing = false,
                            actionMessage = "Scrub interval set to ${result.data} days"
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun attachDisk(dev: String, expand: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.attachBootDisk(dev, expand)) {
                is ApiResult.Success -> {
                    cachedDisks = null
                    _uiState.update { it.copy(isActing = false, actionMessage = "Disk attached") }
                    loadAll(forceRefresh = true)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun replaceDisk(label: String, dev: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.replaceBootDisk(label, dev)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = "Disk replacement initiated")
                    }
                    cachedDisks = null
                    loadAll(forceRefresh = true)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun detachDisk(dev: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.detachBootDisk(dev)) {
                is ApiResult.Success -> {
                    cachedDisks = null
                    _uiState.update { it.copy(isActing = false, actionMessage = "Disk detached") }
                    loadAll(forceRefresh = true)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() = loadAll(forceRefresh = true)

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    companion object {
        private var cachedBootState: System.BootGetState? = null
        private var cachedDisks: List<String>? = null
        private var cachedEnvironments: List<System.BootEnvironmentQueryResultItem>? = null

        /** Clears all cached boot data (e.g. after logout). */
        fun clearBootCache() {
            cachedBootState = null
            cachedDisks = null
            cachedEnvironments = null
        }
    }
}
