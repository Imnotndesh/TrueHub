package com.imnotndesh.truehub.ui.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.Prefs
import com.imnotndesh.truehub.data.models.Auth.LoginMode
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMechanisms
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val otpToken: String = "",
    val loginMode: LoginMode = LoginMode.PASSWORD,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Unknown,
    val isLoginSuccessful: Boolean = false,
    val isApiKeyVisible: Boolean = false,
    val saveDetailsForAutoLogin: Boolean = true,
    val showOtpField: Boolean = false,
    val otpUsername: String = ""
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
    object LoginNavigationCompleted : LoginEvent()
    object ToggleApiKeyVisibility : LoginEvent()
    data class UpdateOtpToken(val token: String) : LoginEvent()
    object SubmitOtp : LoginEvent()
    data class UpdateSaveApiKey(val enabled: Boolean, val context: Context) : LoginEvent()
}

class LoginScreenViewModel(
    private var manager: TrueNASApiManager?,
    private val application: Application
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkConnection()
        // Load the initial auto-login state
        loadInitialAutoLoginState()
    }

    fun updateManager(newManager: TrueNASApiManager) {
        manager = newManager
        checkConnection()
    }

    fun enterOtpMode(username: String) {
        // Also load saved credentials so the submit flow can re-save them after OTP
        viewModelScope.launch {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(application) ?: return@launch
            val account = MultiAccountPrefs.getAccount(application, accountId) ?: return@launch
            val (savedUser, savedPass) = MultiAccountPrefs.getAccountCredentials(application, accountId, account.loginMethod)
            _uiState.update {
                it.copy(
                    showOtpField = true,
                    otpUsername = username,
                    otpToken = "",
                    username = savedUser ?: username,
                    password = savedPass ?: ""
                )
            }
        }
    }

    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UpdateUsername -> {
                _uiState.update { it.copy(username = event.username) }
            }

            is LoginEvent.UpdatePassword -> {
                _uiState.update { it.copy(password = event.password) }
            }

            is LoginEvent.UpdateApiKey -> {
                _uiState.update { it.copy(apiKey = event.apiKey) }
            }

            is LoginEvent.UpdateLoginMode -> {
                _uiState.update { it.copy(loginMode = event.mode) }
            }

            is LoginEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is LoginEvent.Login -> {
                performLogin(event.context)
            }

            is LoginEvent.LoginNavigationCompleted -> {
                _uiState.update { it.copy(isLoginSuccessful = false) }
            }

            is LoginEvent.ResetLoginState -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = false,
                        showOtpField = false,
                        otpToken = "",
                        otpUsername = ""
                    )
                }
            }

            is LoginEvent.CheckConnection -> {
                checkConnection()
            }

            is LoginEvent.ToggleApiKeyVisibility -> {
                _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
            }

            is LoginEvent.UpdateSaveApiKey -> {
                _uiState.update { it.copy(saveDetailsForAutoLogin = event.enabled) }
            }

            is LoginEvent.UpdateOtpToken -> {
                _uiState.update { it.copy(otpToken = event.token) }
            }

            is LoginEvent.SubmitOtp -> {
                submitOtpToken()
            }
        }
    }

    private fun loadInitialAutoLoginState() {
        viewModelScope.launch {
            try {
                val isAutoLoginEnabled = EncryptedPrefs.getUseAutoLogin(application) ?: true
                _uiState.update { it.copy(saveDetailsForAutoLogin = isAutoLoginEnabled) }
            } catch (e: Exception) {
                // Default to true if loading fails
                _uiState.update { it.copy(saveDetailsForAutoLogin = true) }
            }
        }
    }

    private fun checkConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connecting) }

            try {
                withTimeout(5000L) {
                    if (manager?.isConnected() == true) {
                        _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connected) }
                    } else {
                        manager?.connect()
                        _uiState.update { it.copy(connectionStatus = ConnectionStatus.Connected) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.Error(e.message ?: "Connection failed")
                    )
                }
                ToastManager.showError("Connection failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun performLogin(context: Context) {
        val currentState = _uiState.value

        if (manager == null) {
            ToastManager.showError("Connection not ready. Please check server configuration.")
            return
        }

        if (currentState.connectionStatus !is ConnectionStatus.Connected) {
            ToastManager.showWarning("Not connected to server")
            return
        }
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

        _uiState.update { it.copy(isLoading = true) }

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
        try {
            withTimeout(15000L) {
                val mechanism = LoginMechanisms.AuthPasswordPlain(
                    username = state.username,
                    password = state.password,
                    login_options = LoginMechanisms.LoginOptions(user_info = true)
                )
                val result = manager!!.auth.loginEx(mechanism, includeUserInfo = true)

                when (result) {
                    is ApiResult.Success -> {
                        handleLoginExResult(context, result.data, state)
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        ToastManager.showError("Login failed: ${result.message}")
                    }
                    is ApiResult.Loading -> { /* no-op */ }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            when (e) {
                is kotlinx.coroutines.TimeoutCancellationException ->
                    ToastManager.showError("Login timeout. Server may be slow or unreachable.")
                else -> ToastManager.showError("Login error: ${e.message}")
            }
        }
    }

    private suspend fun handleLoginExResult(
        context: Context,
        loginResult: LoginExResult,
        state: LoginUiState
    ) {
        when (loginResult) {
            is LoginExResult.AuthRespSuccess -> {
                // Generate token and save
                val tokenResult = manager!!.auth.generateTokenWithResult(
                    com.imnotndesh.truehub.data.models.Auth.TokenRequest(
                        ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                    )
                )
                when (tokenResult) {
                    is ApiResult.Success -> {
                        saveBaseInfo(context, tokenResult.data, "password")
                        saveDetailsForAutoLogin(
                            context, "password", null,
                            state.username, state.password,
                            state.saveDetailsForAutoLogin
                        )
                        _uiState.update {
                            it.copy(isLoading = false, isLoginSuccessful = true, showOtpField = false)
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        ToastManager.showError("Token generation failed: ${tokenResult.message}")
                    }
                    is ApiResult.Loading -> { /* no-op */ }
                }
            }

            is LoginExResult.AuthRespOTPRequired -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showOtpField = true,
                        otpUsername = loginResult.username,
                        otpToken = ""
                    )
                }
                ToastManager.showInfo("OTP token required. Please enter your 2FA code.")
            }

            is LoginExResult.AuthRespAuthErr -> {
                _uiState.update { it.copy(isLoading = false) }
                ToastManager.showError("Invalid username or password")
            }

            is LoginExResult.AuthRespAuthExpired -> {
                _uiState.update { it.copy(isLoading = false) }
                ToastManager.showError("Session expired. Please try again.")
            }

            is LoginExResult.AuthRespAuthRedirect -> {
                _uiState.update { it.copy(isLoading = false) }
                ToastManager.showError("Authentication redirect required: ${loginResult.urls.joinToString()}")
            }
        }
    }

    private fun submitOtpToken() {
        val state = _uiState.value
        if (state.otpToken.isBlank()) {
            ToastManager.showWarning("Please enter the OTP token")
            return
        }
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                withTimeout(15000L) {
                    val mechanism = LoginMechanisms.AuthOTPToken(
                        otp_token = state.otpToken,
                        login_options = LoginMechanisms.LoginOptions(user_info = true)
                    )
                    val result = manager!!.auth.loginEx(mechanism, includeUserInfo = true)
                    when (result) {
                        is ApiResult.Success -> {
                            when (val lr = result.data) {
                                is LoginExResult.AuthRespSuccess -> {
                                    val tokenResult = manager!!.auth.generateTokenWithResult(
                    com.imnotndesh.truehub.data.models.Auth.TokenRequest(
                        ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                    )
                )
                                    when (tokenResult) {
                                        is ApiResult.Success -> {
                                            saveBaseInfo(
                                                application,
                                                tokenResult.data,
                                                "totp"
                                            )
                                            saveDetailsForAutoLogin(
                                                application, "totp", null,
                                                state.username, state.password,
                                                state.saveDetailsForAutoLogin
                                            )
                                            _uiState.update {
                                                it.copy(
                                                    isLoading = false,
                                                    isLoginSuccessful = true,
                                                    showOtpField = false
                                                )
                                            }
                                        }
                                        is ApiResult.Error -> {
                                            _uiState.update { it.copy(isLoading = false) }
                                            ToastManager.showError("Token failed: ${tokenResult.message}")
                                        }
                                        is ApiResult.Loading -> {}
                                    }
                                }
                                is LoginExResult.AuthRespAuthErr -> {
                                    _uiState.update { it.copy(isLoading = false) }
                                    ToastManager.showError("Invalid OTP token")
                                }
                                else -> {
                                    _uiState.update { it.copy(isLoading = false) }
                                    ToastManager.showError("OTP authentication failed")
                                }
                            }
                        }
                        is ApiResult.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            ToastManager.showError("OTP failed: ${result.message}")
                        }
                        is ApiResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                ToastManager.showError("OTP error: ${e.message}")
            }
        }
    }

    private suspend fun performApiKeyLogin(context: Context, state: LoginUiState) {
        ToastManager.showInfo("Validating API key...")
        try {
            withTimeout(10000L) {
                val mechanism = LoginMechanisms.AuthApiKeyPlain(
                    username = state.username.ifBlank { "api-key" },
                    api_key = state.apiKey,
                    login_options = LoginMechanisms.LoginOptions(user_info = true)
                )
                val result = manager!!.auth.loginEx(mechanism, includeUserInfo = true)

                when (result) {
                    is ApiResult.Success -> {
                        when (val lr = result.data) {
                            is LoginExResult.AuthRespSuccess -> {
                                val tokenResult = manager!!.auth.generateTokenWithResult(
                    com.imnotndesh.truehub.data.models.Auth.TokenRequest(
                        ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                    )
                )
                                when (tokenResult) {
                                    is ApiResult.Success -> {
                                        saveBaseInfo(context, tokenResult.data, "api_key")
                                        saveDetailsForAutoLogin(
                                            context, "api_key", state.apiKey,
                                            null, null, state.saveDetailsForAutoLogin
                                        )
                                        _uiState.update {
                                            it.copy(isLoading = false, isLoginSuccessful = true)
                                        }
                                    }
                                    is ApiResult.Error -> {
                                        _uiState.update { it.copy(isLoading = false) }
                                        ToastManager.showError("Token generation failed: ${tokenResult.message}")
                                    }
                                    is ApiResult.Loading -> {}
                                }
                            }
                            is LoginExResult.AuthRespAuthErr -> {
                                _uiState.update { it.copy(isLoading = false) }
                                ToastManager.showError("Invalid API key")
                            }
                            else -> {
                                _uiState.update { it.copy(isLoading = false) }
                                ToastManager.showError("API key authentication failed")
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        ToastManager.showError("API key validation failed: ${result.message}")
                    }
                    is ApiResult.Loading -> {}
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            when (e) {
                is kotlinx.coroutines.TimeoutCancellationException ->
                    ToastManager.showError("Validation timeout. Please check your connection.")
                else -> ToastManager.showError("Validation error: ${e.message}")
            }
        }
    }

    /**
     * Token Storage and IsLoggedIn Status only
     */
    private suspend fun saveBaseInfo(context: Context, token: String, method: String) {
        EncryptedPrefs.saveAuthToken(context, token)
        EncryptedPrefs.saveIsLoggedIn(context)
        EncryptedPrefs.saveLoginMethod(context, method)
    }

    /**
     * Saved details Storage - ALWAYS saves account, auto-login preference controls credential storage
     */
    private suspend fun saveDetailsForAutoLogin(
        context: Context,
        method: String,
        apiKey: String? = null,
        username: String? = null,
        password: String? = null,
        autoLoginEnabled: Boolean = true
    ) {
        val (serverUrl, serverInsecure) = Prefs.load(context)

        if (serverUrl == null) {
            ToastManager.showError("Server URL not configured")
            return
        }

        // Check if server already exists
        val existingServers = MultiAccountPrefs.getServers(context)
        val server = existingServers.find { it.serverUrl == serverUrl } ?: SavedServer(
            serverUrl = serverUrl,
            insecure = serverInsecure
        )

        // Save server (will update lastUsed if already exists)
        MultiAccountPrefs.saveServer(context, server)

        // Check if account already exists for this server and username
        val existingAccounts = MultiAccountPrefs.getAccounts(context)
        val accountUsername = username ?: "API Key User"
        val existingAccount = existingAccounts.find {
            it.serverId == server.id && it.username == accountUsername
        }

        val account = existingAccount?.copy(
            loginMethod = when (method) {
                "api_key" -> LoginMethod.API_KEY
                "totp" -> LoginMethod.TOTP
                else -> LoginMethod.PASSWORD
            },
            autoLoginEnabled = autoLoginEnabled,
            lastUsed = System.currentTimeMillis()
        ) ?: SavedAccount(
            serverId = server.id,
            username = accountUsername,
            loginMethod = when (method) {
                "api_key" -> LoginMethod.API_KEY
                "totp" -> LoginMethod.TOTP
                else -> LoginMethod.PASSWORD
            },
            autoLoginEnabled = autoLoginEnabled
        )

        // Save account
        MultiAccountPrefs.saveAccount(context, account)

        // Save credentials only if auto-login is enabled
        if (autoLoginEnabled) {
            MultiAccountPrefs.saveAccountCredentials(
                context = context,
                accountId = account.id,
                loginMethod = account.loginMethod,
                apiKey = apiKey,
                username = username,
                password = password
            )
        }

        // Mark as last used
        MultiAccountPrefs.saveLastUsedProfile(context, server.id, account.id)
    }
}

class LoginViewModelFactory(
    private val manager: TrueNASApiManager?,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginScreenViewModel::class.java)) {
            return LoginScreenViewModel(manager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}