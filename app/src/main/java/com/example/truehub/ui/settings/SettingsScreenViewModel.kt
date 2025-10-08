package com.example.truehub.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false,
    val error: String? = null
)

sealed class SettingsEvent {
    object Logout : SettingsEvent()
    object ClearLogoutSuccess : SettingsEvent()
    object ClearError : SettingsEvent()
}

class SettingsScreenViewModel(
    private val manager: TrueNASApiManager?,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.Logout -> performLogout()
            is SettingsEvent.ClearLogoutSuccess -> {
                _uiState.value = _uiState.value.copy(logoutSuccess = false)
            }
            is SettingsEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun performLogout() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true, error = null)

        viewModelScope.launch {
            try {
                EncryptedPrefs.clearAuthToken(application)
                EncryptedPrefs.clearApiKey(application)
                EncryptedPrefs.clearIsLoggedIn(application)
                manager?.disconnect()

                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = true,
                    error = null
                )

                ToastManager.showSuccess("Logged out successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = false,
                    error = e.message ?: "Logout failed"
                )
                ToastManager.showError("Logout failed: ${e.message}")
            }
        }
    }

    class SettingsViewModelFactory(
        private val manager: TrueNASApiManager?,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsScreenViewModel::class.java)) {
                return SettingsScreenViewModel(manager, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}