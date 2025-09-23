package com.example.truehub.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.TrueNASApiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val user = manager.auth.getUserDetailsWithResult()
                val system = manager.system.getSystemInfoWithResult()
                Log.e("ProfileViewModel", "User: $user, System: $system")
                if (user is ApiResult.Success && system is ApiResult.Success)
                    _uiState.value = UiState.Success(user.data, system.data)
                else if (user is ApiResult.Error || system is ApiResult.Error)
                    _uiState.value = UiState.Error("Failed to fetch data: Api error probably")
                else
                    _uiState.value = UiState.Error("Failed to fetch data: Unknown error")
            }
            catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    class ProfileViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(manager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
