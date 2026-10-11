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

data class BootEnvironmentsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActing: Boolean = false,
    val activeId: String? = null,
    val environments: List<System.BootEnvironmentQueryResultItem> = emptyList(),
    val error: String? = null,
    val actionMessage: String? = null
)

/**
 * Backs the boot environments list and detail screens.
 *
 * Uses a companion-level static cache of the freshly loaded list, plus a transient
 * `lastFetchedEnvironments` copy so detail screens can stay responsive when re-created
 * while also allowing a network refresh. Loading is debounced-free and cheap on entry.
 */
@HiltViewModel
class BootEnvironmentsViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BootEnvironmentsUiState())
    val uiState: StateFlow<BootEnvironmentsUiState> = _uiState.asStateFlow()

    init { loadEnvironments() }

    fun loadEnvironments(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            if (forceRefresh || cachedEnvironments == null) {
                cachedEnvironments =
                    (manager.system.queryBootEnvironments() as? ApiResult.Success)?.data
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    environments = cachedEnvironments ?: emptyList(),
                    activeId = cachedEnvironments?.firstOrNull { env -> env.active == true }?.id
                )
            }
        }
    }

    fun activate(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.activateBootEnvironment(id)) {
                is ApiResult.Success -> {
                    cachedEnvironments = null
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = "Activated $id for next boot")
                    }
                    loadEnvironments(forceRefresh = true)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun keep(id: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.keepBootEnvironment(id, value)) {
                is ApiResult.Success -> {
                    cachedEnvironments = null
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = if (value) "Safekeeping on" else "Safekeeping off")
                    }
                    loadEnvironments(forceRefresh = true)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clone(id: String, target: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.cloneBootEnvironment(id, target)) {
                is ApiResult.Success -> {
                    cachedEnvironments = null
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = "Cloned to $target")
                    }
                    loadEnvironments(forceRefresh = true)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun destroy(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true) }
            when (val result = manager.system.destroyBootEnvironment(id)) {
                is ApiResult.Success -> {
                    cachedEnvironments = null
                    _uiState.update {
                        it.copy(isActing = false, actionMessage = "Destroyed $id")
                    }
                    loadEnvironments(forceRefresh = true)
                }
                is ApiResult.Error ->
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() = loadEnvironments(forceRefresh = true)

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    companion object {
        private var cachedEnvironments: List<System.BootEnvironmentQueryResultItem>? = null

        /** Clears cached environment data (e.g. after logout). */
        fun clearEnvironmentsCache() {
            cachedEnvironments = null
        }
    }
}
