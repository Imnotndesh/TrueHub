// ServicesScreenViewModel.kt
package com.example.truehub.ui.services

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.api.Auth
import com.example.truehub.helpers.models.Apps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicesUiState(
    val isLoading: Boolean = false,
    val apps: List<Apps.AppQueryResponse> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class ServicesScreenViewModel(private val auth: Auth) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState(isLoading = true))
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        // Only set loading if we don't have data yet
        if (_uiState.value.apps.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        } else {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
        }

        viewModelScope.launch {
            try {
                val apps = auth.getInstalledApps()
                Log.e("view-model-log",apps.toString())
                _uiState.value = ServicesUiState(
                    isLoading = false,
                    apps = apps,
                    error = null,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load services"
                )
            }
        }
    }

    fun refresh() {
        loadApps()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class ServicesViewModelFactory(
        private val auth: Auth
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ServicesScreenViewModel::class.java)) {
                return ServicesScreenViewModel(auth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}