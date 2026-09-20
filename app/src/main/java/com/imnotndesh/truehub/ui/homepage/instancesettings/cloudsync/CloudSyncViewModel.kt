package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Cloudsync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CloudSyncUiState(
    val tasks: List<Cloudsync.Entry> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null
)

class CloudSyncViewModel(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = manager.cloudsync.queryTasks()) {
                is ApiResult.Success -> _uiState.update { it.copy(tasks = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                else -> Unit
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            when (val result = manager.cloudsync.deleteTask(id)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(tasks = it.tasks.filterNot { task -> task.id == id }, deleted = true)
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                else -> Unit
            }
        }
    }

    fun createTask(payload: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = manager.cloudsync.createTask(payload)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateTask(id: Int, payload: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = manager.cloudsync.updateTask(id, payload)) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(manager: TrueNASApiManager): ViewModelProvider.Factory = viewModelFactory {
            initializer { CloudSyncViewModel(manager) }
        }
    }
}
