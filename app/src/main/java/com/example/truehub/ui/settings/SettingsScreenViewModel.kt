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
    val showAutoLoginDialog: Boolean = false,
    val autoLoginDialogType: AutoLoginDialogType = AutoLoginDialogType.OFF_WARNING,
    val isAutoLoginSaving: Boolean = false,
    val error: String? = null,
    val isChangePassSuccess: Boolean = false
)
enum class AutoLoginDialogType {
    OFF_WARNING,
    PROMPT_API_KEY,
    PROMPT_PASSWORD
}

sealed class SettingsEvent {
    object Logout : SettingsEvent()
    data class ChangePassword(val oldPassword : String, val newPassword: String) : SettingsEvent()
    object ClearLogoutSuccess : SettingsEvent()
    object ClearError : SettingsEvent()
    data class ToggleAutoLogin(val newValue: Boolean) : SettingsEvent()
    object DismissAutoLoginDialog : SettingsEvent()
    data class SaveAutoLoginCredentials(val apiKey: String?, val username: String?, val userPass: String?) : SettingsEvent()
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
            is SettingsEvent.ToggleAutoLogin -> handleAutoLoginToggle(event.newValue)
            is SettingsEvent.DismissAutoLoginDialog -> {
                _uiState.value = _uiState.value.copy(showAutoLoginDialog = false)
            }
            is SettingsEvent.SaveAutoLoginCredentials -> saveAutoLoginCredentials(
                event.apiKey, event.username, event.userPass
            )
        }
    }
    private fun handleAutoLoginToggle(newValue: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (newValue) {
                val loginMethod = getLoginMethod()
                val hasCredentials = when (loginMethod) {
                    "api_key" -> getApiKey() != null
                    "password" -> getUsername() != null && getUserPass() != null
                    else -> false
                }

                if (hasCredentials) {
                    // FIX: Credentials exist, save it and update UI status immediately.
                    saveUseAutoLogin()
                    ToastManager.showSuccess("Auto Login Enabled.")
                    _uiState.value = _uiState.value.copy(isLoading = false) // MUST set to false to trigger LaunchedEffect
                } else {
                    val dialogType = when (loginMethod) {
                        "api_key" -> AutoLoginDialogType.PROMPT_API_KEY
                        "password" -> AutoLoginDialogType.PROMPT_PASSWORD
                        else -> {
                            ToastManager.showError("Unknown login method. Cannot enable Auto Login.")
                            return@launch
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        showAutoLoginDialog = true,
                        autoLoginDialogType = dialogType,
                        isLoading = false // Done checking, ready for dialog input
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    showAutoLoginDialog = true,
                    autoLoginDialogType = AutoLoginDialogType.OFF_WARNING,
                    isLoading = false // Done checking, ready for dialog input
                )
            }
        }
    }

    private fun saveAutoLoginCredentials(apiKey: String?, username: String?, userPass: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoLoginSaving = true)

            try {
                if (apiKey != null) {
                    saveApiKey(apiKey)
                }
                if (username != null && userPass != null) {
                    saveUsername(username)
                    saveUserPass(userPass)
                }
                saveUseAutoLogin()

                _uiState.value = _uiState.value.copy(
                    showAutoLoginDialog = false,
                    isAutoLoginSaving = false
                )
                ToastManager.showSuccess("Credentials saved and Auto Login enabled.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAutoLoginSaving = false)
                ToastManager.showError("Failed to save credentials: ${e.message}")
            }
        }
    }

    private fun performLogout() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true, error = null)

        viewModelScope.launch {
            try {
                EncryptedPrefs.clearAuthToken(application)
                EncryptedPrefs.clearIsLoggedIn(application)
                EncryptedPrefs.revokeUseAutoLogin(application)
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
                            error = result.message
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

    // API Key
    suspend fun getApiKey(): String? = EncryptedPrefs.getApiKey(application)
    suspend fun saveApiKey(apiKey: String) = EncryptedPrefs.saveApiKey(application, apiKey)

    // Username
    suspend fun getUsername(): String? = EncryptedPrefs.getUsername(application)
    suspend fun saveUsername(username: String) = EncryptedPrefs.saveUsername(application, username)

    // Password
    suspend fun getUserPass(): String? = EncryptedPrefs.getUserPass(application)
    suspend fun saveUserPass(userPass: String) = EncryptedPrefs.saveUserPass(application, userPass)

    // Get saved login method
    suspend fun getLoginMethod(): String? = EncryptedPrefs.getLoginMethod(application)

    // Auto-Login
    suspend fun getUseAutoLogin(): Boolean? = EncryptedPrefs.getUseAutoLogin(application)
    suspend fun saveUseAutoLogin() = EncryptedPrefs.saveUseAutoLogin(application)
    suspend fun clearUseAutoLogin() = EncryptedPrefs.revokeUseAutoLogin(application)

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
