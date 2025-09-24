package com.example.truehub.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.AuthService
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.data.models.Auth.LoginMode
import com.example.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val loginMode: LoginMode = LoginMode.PASSWORD,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown,
    val isLoginSuccessful: Boolean = false
)

sealed class ConnectionStatus {
    object Unknown : ConnectionStatus()
    object Connected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Disconnected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

sealed class LoginEvent {
    data class UpdateUsername(val username: String) : LoginEvent()
    data class UpdatePassword(val password: String) : LoginEvent()
    data class UpdateApiKey(val apiKey: String) : LoginEvent()
    data class UpdateLoginMode(val mode: LoginMode) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    data class Login(val context: Context) : LoginEvent()
    object ResetLoginState : LoginEvent()
    object CheckConnection : LoginEvent()
}

class LoginScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkConnection()
    }

    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UpdateUsername -> {
                _uiState.value = _uiState.value.copy(username = event.username)
            }
            is LoginEvent.UpdatePassword -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }
            is LoginEvent.UpdateApiKey -> {
                _uiState.value = _uiState.value.copy(apiKey = event.apiKey)
            }
            is LoginEvent.UpdateLoginMode -> {
                _uiState.value = _uiState.value.copy(loginMode = event.mode)
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            is LoginEvent.Login -> {
                performLogin(event.context)
            }
            is LoginEvent.ResetLoginState -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = false
                )
            }
            is LoginEvent.CheckConnection -> {
                checkConnection()
            }
        }
    }

    private fun checkConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.Connecting)

            try {
                withTimeout(5000L) { // 5 second timeout
                    if (manager.isConnected()) {
                        _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.Connected)
                    } else {
                        manager.connect()
                        _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.Connected)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = ConnectionStatus.Error(e.message ?: "Connection failed")
                )
                ToastManager.showError(
                    message = "Connection failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    private fun performLogin(context: Context) {
        val currentState = _uiState.value

        // Check connection first
        if (currentState.connectionStatus !is ConnectionStatus.Connected) {
            ToastManager.showWarning(
                "Not connected to server",
            )
            return
        }

        // Input validation
        when (currentState.loginMode) {
            LoginMode.PASSWORD -> {
                if (currentState.username.isBlank() || currentState.password.isBlank()) {
                    ToastManager.showWarning("Please enter username and password")
                    return
                }
            }
            LoginMode.API_KEY -> {
                if (currentState.apiKey.isBlank()) {
                    ToastManager.showWarning("Please enter your API key")
                    return
                }
            }
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            when (currentState.loginMode) {
                LoginMode.PASSWORD -> {
                    performPasswordLogin(context, currentState)
                }
                LoginMode.API_KEY -> {
                    performApiKeyLogin(context, currentState)
                }
            }
        }
    }

    private suspend fun performPasswordLogin(context: Context, state: LoginUiState) {
        ToastManager.showInfo("Authenticating...")

        try {
            withTimeout(15000L) { // 15 second timeout for login
                val loginResult = manager.auth.loginUserWithResult(
                    AuthService.DefaultAuth(state.username, state.password)
                )

                when (loginResult) {
                    is ApiResult.Success -> {
                        if (loginResult.data) {
                            ToastManager.showInfo("Generating secure token...")

                            val tokenResult = manager.auth.generateTokenWithResult()
                            when (tokenResult) {
                                is ApiResult.Success -> {
                                    savePasswordLoginCredentials(context, state.username, tokenResult.data)
                                    ToastManager.showSuccess("Login successful!")
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        isLoginSuccessful = true
                                    )
                                }
                                is ApiResult.Error -> {
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                    ToastManager.showError("Failed to generate secure token: ${tokenResult.message}")
                                }
                                is ApiResult.Loading -> {
                                    // This shouldn't happen in our case
                                }
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            ToastManager.showError("Invalid username or password")
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        ToastManager.showError("Login failed: ${loginResult.message}")
                    }
                    is ApiResult.Loading -> {
                        // Continue waiting
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (e) {
                is kotlinx.coroutines.TimeoutCancellationException -> {
                    ToastManager.showError(
                        "Login timeout. Server may be slow or unreachable.",
                    )
                }
                else -> {
                    ToastManager.showError("Login error: ${e.message}")
                }
            }
        }
    }

    private suspend fun performApiKeyLogin(context: Context, state: LoginUiState) {
        ToastManager.showInfo("Validating API key...")

        try {
            withTimeout(15000L) { // 15 second timeout
                val loginResult = manager.auth.loginWithApiKeyWithResult(state.apiKey)

                when (loginResult) {
                    is ApiResult.Success -> {
                        if (loginResult.data) {
                            saveApiKeyLoginCredentials(context, state.apiKey)
                            ToastManager.showSuccess("API key validated successfully!")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccessful = true
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            ToastManager.showError("Invalid API key")
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        ToastManager.showError("API key validation failed: ${loginResult.message}")
                    }
                    is ApiResult.Loading -> {
                        // Continue waiting
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (e) {
                is kotlinx.coroutines.TimeoutCancellationException -> {
                    ToastManager.showError(
                        "Validation timeout. Please check your connection.",
                    )
                }
                else -> {
                    ToastManager.showError("Validation error: ${e.message}")
                }
            }
        }
    }

    private suspend fun savePasswordLoginCredentials(context: Context, username: String, token: String) {
        EncryptedPrefs.saveAuthToken(context, token)
        EncryptedPrefs.saveIsLoggedIn(context, true)
        EncryptedPrefs.saveUsername(context, username)
        EncryptedPrefs.saveLoginMethod(context, "password")
    }

    private suspend fun saveApiKeyLoginCredentials(context: Context, apiKey: String) {
        EncryptedPrefs.saveIsLoggedIn(context, true)
        EncryptedPrefs.saveApiKey(context, apiKey)
        EncryptedPrefs.saveLoginMethod(context, "api_key")
    }
}