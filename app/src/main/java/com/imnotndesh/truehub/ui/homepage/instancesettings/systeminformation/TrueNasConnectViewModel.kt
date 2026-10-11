package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

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

data class TrueNasConnectUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActing: Boolean = false,
    val config: System.TNCEntry? = null,
    val claimToken: String? = null,
    val registrationUri: String? = null,
    val error: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class TrueNasConnectViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrueNasConnectUiState())
    val uiState: StateFlow<TrueNasConnectUiState> = _uiState.asStateFlow()

    init { load() }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }
            when (val result = manager.system.getTnConnectConfig()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, config = result.data)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.message)
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateConfig(data: System.TNConnectUpdateArgs) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true, error = null) }
            when (val result = manager.system.updateTnConnect(data)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActing = false,
                            config = result.data,
                            actionMessage = "TrueNAS Connect updated"
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

    fun generateClaimToken() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true, error = null) }
            when (val result = manager.system.generateTnConnectClaimToken()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActing = false, claimToken = result.data, actionMessage = "Claim token generated")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadRegistrationUri() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isActing = true, error = null) }
            when (val result = manager.system.getTnConnectRegistrationUri()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActing = false, registrationUri = result.data, actionMessage = "Registration URI fetched")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isActing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
