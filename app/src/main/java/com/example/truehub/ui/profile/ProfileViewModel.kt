package com.example.truehub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.api_methods.Auth
import com.example.truehub.helpers.models.AuthUserDetailsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val auth: Auth): ViewModel() {
    data class ProfileUiState(
        val user : AuthUserDetailsResponse? = null,
        val error : String? = null,
        val isLoading : Boolean = false,
        val isConnected: Boolean = true,
        val isAuthenticated: Boolean = true
    )

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        refreshUser()
    }

    fun refreshUser() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val user = auth.getUserDetails()
                _uiState.value = ProfileUiState(user = user, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = e.message, isLoading = false)
            }
        }
    }
    class ProfileViewModelFactory(
        private val auth: Auth
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(auth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}