package com.imnotndesh.truehub

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.NetworkConnectivityObserver
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.ui.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class AppState {
    object Initializing : AppState()
    object CheckingConnection : AppState()
    object ValidatingToken : AppState()
    object NoInternet : AppState()
    object AttemptingAutoLogin : AppState()
    data class Ready(val startRoute: String) : AppState()
    data class Error(val message: String, val fallbackRoute: String) : AppState()
}

class MainViewModel : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _manager = MutableStateFlow<TrueNASApiManager?>(null)
    val manager: StateFlow<TrueNASApiManager?> = _manager.asStateFlow()

    private var hasInitialized = false

    fun initializeApp(context: Context) {
        if (hasInitialized) return
        hasInitialized = true
        viewModelScope.launch {
            try {
                _appState.value = AppState.Initializing
                val networkUtils = NetworkConnectivityObserver(context)
                if (!networkUtils.isNetworkAvailable()) {
                    _appState.value = AppState.NoInternet
                    return@launch
                }

                val hasSavedAccounts = MultiAccountPrefs.getAccounts(context).isNotEmpty()

                if (hasSavedAccounts) {
                    _appState.value = AppState.ValidatingToken

                    val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context) ?: run {
                        _appState.value = AppState.Ready(Screen.AccountSwitcher.route)
                        return@launch
                    }

                    val server = MultiAccountPrefs.getServer(context, serverId)
                    val account = MultiAccountPrefs.getAccount(context, accountId)
                    val token = MultiAccountPrefs.getTokenForLastUsed(context)

                    if (server != null && account != null && token != null) {
                        val manager = attemptLoginWithToken(context, server, account, token)

                        if (manager != null) {
                            _manager.value = manager
                            _appState.value = AppState.Ready(Screen.Main.route)
                            return@launch
                        }
                    }
                    _appState.value = AppState.Ready(Screen.AccountSwitcher.route)

                } else {
                    _appState.value = AppState.Ready(Screen.Login.route)
                }

            } catch (e: Exception) {
                _appState.value = AppState.Error(
                    "Initialization failed: ${e.message}",
                    Screen.Login.route
                )
            }
        }
    }

    fun updateManager(newManager: TrueNASApiManager) {
        _manager.value = newManager
    }

    fun startPeriodicPing(context: Context) {
        viewModelScope.launch {
            while (true) {
                try {
                    val authToken = MultiAccountPrefs.getTokenForLastUsed(context)
                    val currentManager = _manager.value

                    if (authToken != null && currentManager?.isConnected() == true) {
                        currentManager.connection.pingConnectionWithResult()
                    }
                } catch (_: Exception) {
                }
                delay(30000L)
            }
        }
    }

    suspend fun attemptLoginWithProfile(
        context: Context,
        server: SavedServer,
        account: SavedAccount
    ): TrueNASApiManager? {
        return try {
            val config = ClientConfig(
                serverUrl = server.serverUrl,
                insecure = server.insecure,
                connectionTimeoutMs = 10000,
                enablePing = true,
                enableDebugLogging = true
            )

            val client = TrueNASClient(config)
            val manager = TrueNASApiManager(client, context.applicationContext)

            if (!manager.connect()) return null

            val (cred1, cred2) = MultiAccountPrefs.getAccountCredentials(
                context,
                account.id,
                account.loginMethod
            )

            val loginSuccess = when (account.loginMethod) {
                LoginMethod.API_KEY -> {
                    cred1?.let {
                        val result = manager.auth.loginWithApiKeyWithResult(it)
                        result is ApiResult.Success && result.data
                    } ?: false
                }
                LoginMethod.PASSWORD -> {
                    if (cred1 != null && cred2 != null) {
                        val result = manager.auth.loginUserWithResult(
                            AuthService.DefaultAuth(cred1, cred2)
                        )
                        result is ApiResult.Success && result.data
                    } else false
                }
            }

            if (loginSuccess) {
                val tokenResult = manager.auth.generateTokenWithResult()
                if (tokenResult is ApiResult.Success) {
                    MultiAccountPrefs.saveCurrentSession(
                        context,
                        server.id,
                        account.id,
                        tokenResult.data
                    )
                    manager
                } else null
            } else null

        } catch (_: Exception) {
            null
        }
    }

    private suspend fun attemptLoginWithToken(
        context: Context,
        server: SavedServer,
        account: SavedAccount,
        token: String
    ): TrueNASApiManager? {
        return withTimeoutOrNull(10000L) {
            try {
                val config = ClientConfig(
                    serverUrl = server.serverUrl,
                    insecure = server.insecure,
                    connectionTimeoutMs = 5000,
                    enablePing = true,
                    enableDebugLogging = true
                )

                val client = TrueNASClient(config)
                val manager = TrueNASApiManager(client, context)

                if (!manager.connect()) return@withTimeoutOrNull null

                val tryLogin = manager.auth.loginWithTokenAndResult(token)
                if (tryLogin is ApiResult.Error) return@withTimeoutOrNull null

                val newTokenResult = manager.auth.generateTokenWithResult()
                if (newTokenResult is ApiResult.Success) {
                    MultiAccountPrefs.saveTokenForLastUsed(
                        context,
                        newTokenResult.data
                    )
                    manager
                } else {
                    null
                }

            } catch (_: Exception) {
                null
            }
        }
    }
}