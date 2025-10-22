package com.example.truehub.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
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
    val isChangingPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isChangePassSuccess: Boolean = false
)

sealed class SettingsEvent {
    object Logout : SettingsEvent()
    data class ChangePassword(val oldPassword : String, val newPassword: String) : SettingsEvent()
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
            is SettingsEvent.ChangePassword -> changePassword(event.newPassword,event.oldPassword)
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
                EncryptedPrefs.clearIsLoggedIn(application)
                if (EncryptedPrefs.getUseAutoLogin(application)?: false){
                    when (EncryptedPrefs.getLoginMethod(application)){
                        "api_key" -> {
                            EncryptedPrefs.clearApiKey(application)
                        }
                        "password" ->{
                            EncryptedPrefs.clearUsername(application)
                            EncryptedPrefs.clearUserPass(application)
                        }
                    }
                }
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
    private fun changePassword(newPassword :String,oldPassword : String){
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val username = EncryptedPrefs.getUsername(application)
            if (username == null) {
                ToastManager.showError("Username not found")
                return@launch
            }
            try {
                val result = manager?.user?.changeUserPasswordWithResult(username,oldPassword,newPassword)
                when (result){
                    is ApiResult.Error -> {
                        ToastManager.showError("Password change failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isChangePassSuccess = false,
                            error = result.message ?: "Password change failed"
                        )
                    }
                    is ApiResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, isChangePassSuccess = true, error = null)
                        ToastManager.showSuccess("Password changed successfully")
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        ToastManager.showError("Password change failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isChangePassSuccess = false,
                    error = e.message ?: "Password change failed"
                )
                ToastManager.showError("Password change failed: ${e.message}")
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