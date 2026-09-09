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
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMechanisms
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.data.workers.AppsRefreshWorker
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.utils.AppCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

sealed class AppState {
    object Initializing : AppState()
    object CheckingConnection : AppState()
    object ValidatingToken : AppState()
    object NoInternet : AppState()
    object AttemptingAutoLogin : AppState()
    data class Ready(val startRoute: String) : AppState()
    data class Error(val message: String, val fallbackRoute: String) : AppState()
    data class TotpRequired(val username: String) : AppState()
}

enum class TotpResult { OTP_REQUIRED, SUCCESS }

class MainViewModel : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _manager = MutableStateFlow<TrueNASApiManager?>(null)
    val manager: StateFlow<TrueNASApiManager?> = _manager.asStateFlow()

    // Active user key used to scope personalization (theme, navbar, etc.).
    private val _currentUserKey = MutableStateFlow<String?>(null)
    val currentUserKey: StateFlow<String?> = _currentUserKey.asStateFlow()

    private var hasInitialized = false
    private val _pendingNavigation = MutableStateFlow<String?>(null)
    val pendingNavigation: StateFlow<String?> = _pendingNavigation.asStateFlow()

    fun requestNavigateTo(route: String) {
        _pendingNavigation.value = route
    }

    fun clearPendingNavigation() {
        _pendingNavigation.value = null
    }

    /** Set the active user and load their personalization from disk. */
    fun setActiveUser(context: Context, accountId: String?) {
        val key = accountId ?: PersonalizationManager.DEFAULT_USER_KEY
        _currentUserKey.value = key
        PersonalizationManager.loadForUser(context, key)
        viewModelScope.launch {
            MultiAccountPrefs.getCurrentSession(context)?.let { (serverId, _, _) ->
                AppCache.bindServer(context, serverId)
            } ?: MultiAccountPrefs.getLastUsedProfile(context)?.let { (serverId, _) ->
                AppCache.bindServer(context, serverId)
            }
        }
    }
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

                    if (server != null && account != null) {
                        var manager: TrueNASApiManager? = null

                        // Phase 1: try saved token
                        if (token != null) {
                            manager = attemptLoginWithToken(context, server, account, token)
                            if (manager != null) {
                                _manager.value = manager
                                setActiveUser(context, accountId)
                                _appState.value = AppState.Ready(Screen.Main.route)
                                return@launch
                            }
                        }

                        // Phase 2: token failed or missing — try loginEx with saved password
                        val totpManager = attemptTotpAutoLogin(context, server, account)
                        if (totpManager != null) {
                            when (totpManager.second) {
                                TotpResult.OTP_REQUIRED -> {
                                    _appState.value = AppState.TotpRequired(account.username)
                                    return@launch
                                }
                                TotpResult.SUCCESS -> {
                                    _manager.value = totpManager.first
                                    setActiveUser(context, accountId)
                                    _appState.value = AppState.Ready(Screen.Main.route)
                                    return@launch
                                }
                            }
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
    fun startPeriodicAppSync(context: Context) {
        AppsRefreshWorker.scheduleRecurring(context)
        AppsRefreshWorker.scheduleImmediate(context)
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
                delay(30000.milliseconds)
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
                enableDebugLogging = false
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
                LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                    if (cred1 != null && cred2 != null) {
                        val result = manager.auth.loginUserWithResult(
                            AuthService.DefaultAuth(cred1, cred2)
                        )
                        result is ApiResult.Success && result.data
                    } else false
                }
            }

            if (loginSuccess) {
                val tokenResult = manager.auth.generateTokenWithResult(
                    Auth.TokenRequest(ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS)
                )
                if (tokenResult is ApiResult.Success) {
                    MultiAccountPrefs.saveCurrentSession(
                        context,
                        server.id,
                        account.id,
                        tokenResult.data,
                        MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                    )
                    setActiveUser(context, account.id)
                    manager
                } else null
            } else null

        } catch (_: Exception) {
            null
        }
    }

    /**
     * Connects to server, fires loginEx with saved password credentials.
     * Returns Pair(manager, TotpResult.OTP_REQUIRED) if the server asks for TOTP.
     * Returns Pair(manager, TotpResult.SUCCESS) if login succeeds and a fresh token is generated.
     * Returns null on any other failure.
     */
    private suspend fun attemptTotpAutoLogin(
        context: Context,
        server: SavedServer,
        account: SavedAccount
    ): Pair<TrueNASApiManager, TotpResult>? {
        return withTimeoutOrNull(15000.milliseconds) {
            try {
                val config = ClientConfig(
                    serverUrl = server.serverUrl,
                    insecure = server.insecure,
                    connectionTimeoutMs = 5000,
                    enablePing = true,
                    enableDebugLogging = false
                )
                val client = TrueNASClient(config)
                val manager = TrueNASApiManager(client, context)
                if (!manager.connect()) return@withTimeoutOrNull null

                val (cred1, cred2) = MultiAccountPrefs.getAccountCredentials(
                    context, account.id, account.loginMethod
                )
                if (cred1 == null || cred2 == null) return@withTimeoutOrNull null

                val mechanism = LoginMechanisms.AuthPasswordPlain(
                    username = cred1,
                    password = cred2,
                    login_options = LoginMechanisms.LoginOptions(user_info = true)
                )
                val result = manager.auth.loginEx(mechanism, includeUserInfo = true)

                when {
                    result is ApiResult.Success && result.data is LoginExResult.AuthRespOTPRequired -> {
                        _manager.value = manager
                        Pair(manager, TotpResult.OTP_REQUIRED)
                    }
                    result is ApiResult.Success && result.data is LoginExResult.AuthRespSuccess -> {
                        // loginEx succeeded directly — generate token and save
                        val tokenResult = manager.auth.generateTokenWithResult(
                            Auth.TokenRequest(ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS)
                        )
                        if (tokenResult is ApiResult.Success) {
                            MultiAccountPrefs.saveCurrentSession(
                                context, server.id, account.id, tokenResult.data,
                                MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                            )
                            // Also save credentials to make sure they're persisted
                            MultiAccountPrefs.saveAccountCredentials(
                                context, account.id, account.loginMethod,
                                username = cred1, password = cred2
                            )
                            _manager.value = manager
                            Pair(manager, TotpResult.SUCCESS)
                        } else null
                    }
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun attemptLoginWithToken(
        context: Context,
        server: SavedServer,
        account: SavedAccount,
        token: String
    ): TrueNASApiManager? {
        return withTimeoutOrNull(10000.milliseconds) {
            try {
                val config = ClientConfig(
                    serverUrl = server.serverUrl,
                    insecure = server.insecure,
                    connectionTimeoutMs = 5000,
                    enablePing = true,
                    enableDebugLogging = false
                )

                val client = TrueNASClient(config)
                val manager = TrueNASApiManager(client, context)

                if (!manager.connect()) return@withTimeoutOrNull null

                val tryLogin = manager.auth.loginWithTokenAndResult(token)
                if (tryLogin is ApiResult.Error) return@withTimeoutOrNull null

                val newTokenResult = manager.auth.generateTokenWithResult(
                    Auth.TokenRequest(ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS)
                )
                if (newTokenResult is ApiResult.Success) {
                    MultiAccountPrefs.saveTokenForLastUsed(
                        context,
                        newTokenResult.data,
                        MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
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