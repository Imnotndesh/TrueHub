package com.imnotndesh.truehub.ui.services.containers.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContainerDetailUiState(
    val container: Virt.ContainerResponse? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContainerDetailViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val _uiState = MutableStateFlow(ContainerDetailUiState())
    val uiState: StateFlow<ContainerDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (containerId.isBlank()) {
            _uiState.value = ContainerDetailUiState(error = "Container ID is missing")
            return
        }
        viewModelScope.launch {
            val hasContainer = _uiState.value.container != null
            _uiState.update {
                it.copy(
                    isLoading = !hasContainer,
                    isRefreshing = hasContainer,
                    error = null
                )
            }
            when (val result = manager.virtService.getInstanceWithResult(containerId)) {
                is ApiResult.Success -> _uiState.value = ContainerDetailUiState(container = result.data)
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                ApiResult.Loading -> _uiState.update {
                    it.copy(isLoading = !hasContainer, isRefreshing = false)
                }
            }
        }
    }
}
