package com.imnotndesh.truehub.ui.homepage.instancesettings.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.utils.AppCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServicesScreenUiState(
    val services: List<System.ServiceQueryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    // Service ids with an in-flight control call; drives the per-row spinner
    // so toggling one service doesn't block interaction with the others.
    val pendingServiceIds: Set<Int> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class ServicesScreenViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesScreenUiState())
    val uiState: StateFlow<ServicesScreenUiState> = _uiState.asStateFlow()

    init {
        loadServices(isInitial = true)
    }

    fun refresh() = loadServices(isInitial = false)

    private fun loadServices(isInitial: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                if (isInitial) it.copy(isLoading = true, error = null)
                else it.copy(isRefreshing = true, error = null)
            }

            when (val result = manager.system.getInstanceServices()) {
                is ApiResult.Success -> {
                    val sorted = result.data.sortedBy { svc -> svc.service }
                    AppCache.updateServices(sorted)
                    _uiState.update {
                        it.copy(
                            services = sorted,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.message)
                    }
                }
                is ApiResult.Loading -> { /* no-op, callWithResult resolves synchronously to Success/Error */ }
            }
        }
    }

    /**
     * Starts or stops a service from the list row's quick-toggle switch.
     * Uses System.ServiceControlOptions.START / STOP via controlService,
     * then re-fetches just that service instance to reflect the real
     * resulting state (the control call's Boolean result doesn't carry
     * the new state/pids).
     */
    fun setServiceRunning(service: System.ServiceQueryResponse, running: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(pendingServiceIds = it.pendingServiceIds + service.id) }

            val action = if (running) System.ServiceControlOptions.START else System.ServiceControlOptions.STOP
            val controlResult = manager.system.controlService(
                action = action,
                service = service.service,
                options = System.ServiceControlCallOptions()
            )

            if (controlResult is ApiResult.Error) {
                _uiState.update {
                    it.copy(
                        pendingServiceIds = it.pendingServiceIds - service.id,
                        error = controlResult.message
                    )
                }
                return@launch
            }

            when (val instanceResult = manager.system.getInstanceServiceInstance(service.id)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingServiceIds = state.pendingServiceIds - service.id,
                            services = state.services.map { svc ->
                                if (svc.id == service.id) instanceResult.data else svc
                            }
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            pendingServiceIds = it.pendingServiceIds - service.id,
                            error = instanceResult.message
                        )
                    }
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}