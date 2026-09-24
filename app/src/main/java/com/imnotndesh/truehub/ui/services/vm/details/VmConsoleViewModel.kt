package com.imnotndesh.truehub.ui.services.vm.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Vm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VmConsoleUiState(
    val vm: Vm.VmQueryResponse? = null,
    val uri: String? = null,
    val isLoading: Boolean = true,
    val notReadyReason: String? = null,
    val error: String? = null
)

@HiltViewModel
class VmConsoleViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val vmId: Int = savedStateHandle["vmId"] ?: -1
    private val _uiState = MutableStateFlow(VmConsoleUiState())
    val uiState: StateFlow<VmConsoleUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (vmId < 0) {
            _uiState.value = VmConsoleUiState(isLoading = false, error = "VM ID is missing")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, notReadyReason = null) }
            when (val result = manager.vmService.queryVmInstanceWithResult(vmId)) {
                is ApiResult.Success -> {
                    val vm = result.data
                    when {
                        !vm.status.state.equals("running", ignoreCase = true) ->
                            _uiState.update {
                                it.copy(vm = vm, isLoading = false, notReadyReason = "Start the VM to use the console")
                            }
                        !vm.display_available ->
                            _uiState.update {
                                it.copy(
                                    vm = vm,
                                    isLoading = false,
                                    notReadyReason = "This VM has no display device configured"
                                )
                            }
                        else -> loadUri(vm)
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private suspend fun loadUri(vm: Vm.VmQueryResponse) {
        when (val result = manager.vmService.getVmDisplayWithResult(vmId)) {
            is ApiResult.Success -> _uiState.update {
                it.copy(
                    vm = vm,
                    uri = result.data.uri,
                    isLoading = false,
                    error = result.data.error
                )
            }
            is ApiResult.Error -> _uiState.update {
                it.copy(vm = vm, isLoading = false, error = result.message)
            }
            ApiResult.Loading -> _uiState.update { it.copy(vm = vm, isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
