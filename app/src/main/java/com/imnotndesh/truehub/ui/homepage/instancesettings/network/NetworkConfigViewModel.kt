package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val summary: System.NetworkGeneralSummaryResult? = null,
    val config: System.NetworkConfigurationEntry? = null,
    val activityChoices: List<List<String>> = emptyList(),
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class NetworkConfigViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    init {
        prefetchChoices()
    }

    private fun prefetchChoices() {
        if (cachedActivityChoices != null) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            cachedActivityChoices =
                (manager.system.networkConfigurationActivityChoices() as? ApiResult.Success)?.data
            _uiState.update {
                it.copy(activityChoices = cachedActivityChoices ?: emptyList())
            }
        }
    }

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    companion object {
        private var cachedActivityChoices: List<List<String>>? = null

        fun clearChoicesCache() {
            cachedActivityChoices = null
        }
    }

    fun refresh() = loadAll(forceRefresh = true)

    fun loadAll(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }
            try {

                val summaryDeferred = async { manager.system.networkGeneralSummary() }
                val configDeferred = async { manager.system.networkConfigurationConfig() }

                val summaryResult = summaryDeferred.await()
                val configResult = configDeferred.await()

                if (configResult is ApiResult.Error) {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = configResult.message)
                    }
                    return@launch
                }


                if (cachedActivityChoices == null) {
                    cachedActivityChoices =
                        (manager.system.networkConfigurationActivityChoices() as? ApiResult.Success)?.data
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        summary = (summaryResult as? ApiResult.Success)?.data,
                        config = (configResult as? ApiResult.Success)?.data,
                        activityChoices = cachedActivityChoices ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun updateConfig(data: System.NetworkConfigurationUpdateArgs) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
            when (val result = manager.system.networkConfigurationUpdate(data)) {
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
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}