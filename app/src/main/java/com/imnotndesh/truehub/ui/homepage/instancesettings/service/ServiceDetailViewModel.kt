package com.imnotndesh.truehub.ui.homepage.instancesettings.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceDetailUiState(
    val service: System.ServiceQueryResponse,
    val isLoading: Boolean = false,
    val isControlling: Boolean = false,
    val isSavingStartOnBoot: Boolean = false,
    val error: String? = null
)

/**
 * Owns a single service's live state. Initialized with the snapshot passed
 * from the list screen, then immediately re-fetches the live instance so
 * state/pids aren't stale by the time this screen renders.
 */
@HiltViewModel(assistedFactory = ServiceDetailViewModel.Factory::class)
class ServiceDetailViewModel @AssistedInject constructor(
    @Assisted initialService: System.ServiceQueryResponse,
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceDetailUiState(service = initialService))
    val uiState: StateFlow<ServiceDetailUiState> = _uiState.asStateFlow()

    init {
        refreshInstance()
    }

    private fun refreshInstance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = manager.system.getInstanceServiceInstance(_uiState.value.service.id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(service = result.data, isLoading = false) }
                }
                is ApiResult.Error -> {
                    // Keep showing the snapshot we already have; surface the
                    // error without blanking the screen.
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun controlService(action: System.ServiceControlOptions) {
        viewModelScope.launch {
            _uiState.update { it.copy(isControlling = true) }
            val service = _uiState.value.service

            val result = manager.system.controlService(
                action = action,
                service = service.service,
                options = System.ServiceControlCallOptions()
            )

            if (result is ApiResult.Error) {
                _uiState.update { it.copy(isControlling = false, error = result.message) }
                return@launch
            }

            // Re-pull the instance so state/pids reflect the actual result
            // of the control call rather than optimistically flipping state.
            when (val instanceResult = manager.system.getInstanceServiceInstance(service.id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(service = instanceResult.data, isControlling = false) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isControlling = false, error = instanceResult.message) }
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Toggles "start on boot" via System.updateService. The endpoint accepts
     * either the service name (String) or id (Int) as the identifier; the
     * service name is used here since it's the more stable/readable choice
     * and matches what controlService already keys off of.
     */
    fun setStartOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStartOnBoot = true) }
            val service = _uiState.value.service

            val result = manager.system.updateService(
                serviceIdentifier = service.service,
                startOnBoot = enabled
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            service = it.service.copy(enable = enabled),
                            isSavingStartOnBoot = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isSavingStartOnBoot = false, error = result.message)
                    }
                }
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    @AssistedFactory
    interface Factory {
        fun create(initialService: System.ServiceQueryResponse): ServiceDetailViewModel
    }
}