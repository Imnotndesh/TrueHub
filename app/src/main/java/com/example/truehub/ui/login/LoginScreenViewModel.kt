package com.example.truehub.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.truehub.data.ApiResult
import com.example.truehub.data.api.AuthService
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.data.models.Auth.LoginMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val loginMode: LoginMode = LoginMode.PASSWORD,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val loginResult: ApiResult<Boolean>? = null,
    val tokenResult: ApiResult<String>? = null,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

sealed class LoginEvent {
    data class UpdateUsername(val username: String) : LoginEvent()
    data class UpdatePassword(val password: String) : LoginEvent()
    data class UpdateApiKey(val apiKey: String) : LoginEvent()
    data class UpdateLoginMode(val mode: LoginMode) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    data class Login(val context: Context) : LoginEvent()
    object ClearError : LoginEvent()
    object ResetLoginState : LoginEvent()
}

class LoginScreenViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
            is LoginEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null,
                    loginResult = null,
                    tokenResult = null
                )
            }
            is LoginEvent.ResetLoginState -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginResult = null,
                    tokenResult = null,
                    errorMessage = null,
                    isLoginSuccessful = false
                )
            }
        }
    }

    private fun performLogin(context: Context) {
        val currentState = _uiState.value

        // Input validation
        when (currentState.loginMode) {
            LoginMode.PASSWORD -> {
                if (currentState.username.isBlank() || currentState.password.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Please enter username and password"
                    )
                    return
                }
            }
            LoginMode.API_KEY -> {
                if (currentState.apiKey.isBlank()) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Please enter your API key"
                    )
                    return
                }
            }
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

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
        // Set login loading state
        _uiState.value = state.copy(
            isLoading = true,
            loginResult = ApiResult.Loading
        )

        val loginResult = manager.auth.loginUserWithResult(
            AuthService.DefaultAuth(state.username, state.password)
        )

        _uiState.value = _uiState.value.copy(loginResult = loginResult)

        when (loginResult) {
            is ApiResult.Success -> {
                if (loginResult.data) {
                    // Login successful, generate token
                    _uiState.value = _uiState.value.copy(tokenResult = ApiResult.Loading)

                    val tokenResult = manager.auth.generateTokenWithResult()
                    _uiState.value = _uiState.value.copy(tokenResult = tokenResult)

                    when (tokenResult) {
                        is ApiResult.Success -> {
                            // Save credentials and complete login
                            savePasswordLoginCredentials(context, state.username, tokenResult.data)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                                errorMessage = null
                            )
                        }
                        is ApiResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Failed to generate token: ${tokenResult.message}"
                            )
                        }
                        is ApiResult.Loading -> {
                            // Keep loading state
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Invalid credentials"
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login error: ${loginResult.message}"
                )
            }
            is ApiResult.Loading -> {
                // Keep loading state
            }
        }
    }

    private suspend fun performApiKeyLogin(context: Context, state: LoginUiState) {
        // Set login loading state
        _uiState.value = state.copy(
            isLoading = true,
            loginResult = ApiResult.Loading
        )

        val loginResult = manager.auth.loginWithApiKeyWithResult(state.apiKey)
        _uiState.value = _uiState.value.copy(loginResult = loginResult)

        when (loginResult) {
            is ApiResult.Success -> {
                if (loginResult.data) {
                    // Save credentials and complete login
                    Log.e("API KEY", state.apiKey)
                    saveApiKeyLoginCredentials(context, state.apiKey)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Invalid API key"
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "API key login error: ${loginResult.message}"
                )
            }
            is ApiResult.Loading -> {
                // Keep loading state
            }
        }
    }

    private fun savePasswordLoginCredentials(context: Context, username: String, token: String) {
        runBlocking  {
            EncryptedPrefs.saveAuthToken(context, token)
            EncryptedPrefs.saveIsLoggedIn(context, true)
            EncryptedPrefs.saveUsername(context, username)
            EncryptedPrefs.saveLoginMethod(context, "password")
        }
    }

    private fun saveApiKeyLoginCredentials(context: Context, apiKey: String) {
        runBlocking {
            EncryptedPrefs.saveIsLoggedIn(context, true)
            EncryptedPrefs.saveApiKey(context, apiKey)
            EncryptedPrefs.saveLoginMethod(context, "api_key")
        }
    }
}