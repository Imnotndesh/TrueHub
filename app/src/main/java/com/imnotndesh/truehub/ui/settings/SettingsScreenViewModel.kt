package com.imnotndesh.truehub.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.ui.components.ToastManager
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
    object SignOut : SettingsEvent()
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
            is SettingsEvent.SignOut -> performSignOut()
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
                val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(application)?: Pair(null,null)

                if (serverId != null && accountId != null) {
                    val account = MultiAccountPrefs.getAccount(application, accountId)

                    if (account != null) {
                        val (cred1, cred2) = MultiAccountPrefs.getAccountCredentials(
                            application,
                            accountId,
                            account.loginMethod
                        )

                        val hasCredentials = when (account.loginMethod) {
                            com.imnotndesh.truehub.data.models.LoginMethod.API_KEY -> cred1 != null
                            com.imnotndesh.truehub.data.models.LoginMethod.PASSWORD -> cred1 != null && cred2 != null
                        }

                        if (hasCredentials) {
                            val updatedAccount = account.copy(autoLoginEnabled = true)
                            MultiAccountPrefs.saveAccount(application, updatedAccount)
                            ToastManager.showSuccess("Auto Login Enabled.")
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        } else {
                            // Prompt for credentials
                            val dialogType = when (account.loginMethod) {
                                com.imnotndesh.truehub.data.models.LoginMethod.API_KEY -> AutoLoginDialogType.PROMPT_API_KEY
                                com.imnotndesh.truehub.data.models.LoginMethod.PASSWORD -> AutoLoginDialogType.PROMPT_PASSWORD
                            }
                            _uiState.value = _uiState.value.copy(
                                showAutoLoginDialog = true,
                                autoLoginDialogType = dialogType,
                                isLoading = false
                            )
                        }
                    } else {
                        ToastManager.showError("Current account not found")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    ToastManager.showError("No active session found")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    showAutoLoginDialog = true,
                    autoLoginDialogType = AutoLoginDialogType.OFF_WARNING,
                    isLoading = false
                )
            }
        }
    }

    private fun saveAutoLoginCredentials(apiKey: String?, username: String?, userPass: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoLoginSaving = true)

            try {
                val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(application)?: Pair(null,null)


                if (serverId != null && accountId != null) {
                    val account = MultiAccountPrefs.getAccount(application, accountId)

                    if (account != null) {
                        MultiAccountPrefs.saveAccountCredentials(
                            context = application,
                            accountId = accountId,
                            loginMethod = account.loginMethod,
                            apiKey = apiKey,
                            username = username,
                            password = userPass
                        )

                        // Enable auto-login
                        val updatedAccount = account.copy(autoLoginEnabled = true)
                        MultiAccountPrefs.saveAccount(application, updatedAccount)

                        _uiState.value = _uiState.value.copy(
                            showAutoLoginDialog = false,
                            isAutoLoginSaving = false
                        )
                        ToastManager.showSuccess("Credentials saved and Auto Login enabled.")
                    } else {
                        throw Exception("Account not found")
                    }
                } else {
                    throw Exception("No active session")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isAutoLoginSaving = false)
                ToastManager.showError("Failed to save credentials: ${e.message}")
            }
        }
    }

    private fun performSignOut() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true, error = null)

        viewModelScope.launch {
            try {
                val (_, accountId) = MultiAccountPrefs.getLastUsedProfile(application)?: Pair(null,null)
                MultiAccountPrefs.clearCurrentSession(application)
                if (accountId != null) {
                    MultiAccountPrefs.deleteAccount(application, accountId)
                }

                // Disconnect manager
                manager?.disconnect()

                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = true,
                    error = null
                )

                ToastManager.showSuccess("Signed out successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    logoutSuccess = false,
                    error = e.message ?: "Sign out failed"
                )
                ToastManager.showError("Sign out failed: ${e.message}")
            }
        }
    }

    private fun performLogout() {
        _uiState.value = _uiState.value.copy(isLoggingOut = true, error = null)

        viewModelScope.launch {
            try {
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

    suspend fun clearUseAutoLogin() {
        val (_, accountId) = MultiAccountPrefs.getLastUsedProfile(application) ?: Pair(null, null)

        if (accountId != null) {
            val account = MultiAccountPrefs.getAccount(application, accountId)
            if (account != null) {
                val updatedAccount = account.copy(autoLoginEnabled = false)
                MultiAccountPrefs.saveAccount(application, updatedAccount)

                MultiAccountPrefs.clearAccountCredentials(application, accountId)

                ToastManager.showSuccess("Auto Login disabled")
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
    suspend fun isAutoLoginEnabled(): Boolean {
        val (_, accountId) = MultiAccountPrefs.getLastUsedProfile(application)?: Pair(null,null)
        return if (accountId != null) {
            MultiAccountPrefs.getAccount(application, accountId)?.autoLoginEnabled ?: false
        } else {
            false
        }
    }
}
