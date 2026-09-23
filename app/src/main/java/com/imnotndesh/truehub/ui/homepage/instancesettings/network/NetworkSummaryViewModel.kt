package com.imnotndesh.truehub.ui.homepage.instancesettings.network

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

data class NetworkSummaryUiState(
    val isLoading: Boolean = false,
    val summary: System.NetworkGeneralSummaryResult? = null,
    val error: String? = null
)

@HiltViewModel
class NetworkSummaryViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkSummaryUiState())
    val uiState: StateFlow<NetworkSummaryUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = manager.system.networkGeneralSummary()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, summary = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
