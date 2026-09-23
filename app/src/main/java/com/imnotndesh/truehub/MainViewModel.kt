package com.imnotndesh.truehub

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.ConnectionState
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.SessionCoordinator
import com.imnotndesh.truehub.data.helpers.SessionProvider
import com.imnotndesh.truehub.data.helpers.NetworkConnectivityObserver
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.LoginExResult
import com.imnotndesh.truehub.data.models.LoginMechanisms
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.data.workers.AppsRefreshWorker
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.utils.AppCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionCoordinator: SessionCoordinator
) : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _manager = MutableStateFlow<TrueNASApiManager?>(null)
    val manager: StateFlow<TrueNASApiManager?> = _manager.asStateFlow()

    // Active user key used to scope personalization (theme, navbar, etc.).
    private val _currentUserKey = MutableStateFlow<String?>(null)
    val currentUserKey: StateFlow<String?> = _currentUserKey.asStateFlow()

    private var initializationJob: Job? = null
    private var connectivityRecoveryJob: Job? = null
    private var periodicPingJob: Job? = null
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
        val applicationContext = context.applicationContext
        startConnectivityRecovery(applicationContext)
        if (initializationJob?.isActive == true) return
        initializationJob = viewModelScope.launch {
            try {
                _appState.value = AppState.Initializing
                val networkUtils = NetworkConnectivityObserver(applicationContext)
                if (!networkUtils.isNetworkAvailable()) {
                    _appState.value = AppState.NoInternet
                    return@launch
                }

                val hasSavedAccounts = MultiAccountPrefs.getAccounts(applicationContext).isNotEmpty()

                if (hasSavedAccounts) {
                    _appState.value = AppState.ValidatingToken

                    val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(applicationContext) ?: run {
                        _appState.value = AppState.Ready(Screen.AccountSwitcher.route)
                        return@launch
                    }

                    val server = MultiAccountPrefs.getServer(applicationContext, serverId)
                    val account = MultiAccountPrefs.getAccount(applicationContext, accountId)

                    if (server != null && account != null) {
                        if (!account.autoLoginEnabled) {
                            _appState.value = AppState.Ready(Screen.AccountSwitcher.route)
                            return@launch
                        }
                        val token = MultiAccountPrefs.getTokenForLastUsed(applicationContext)
                        var manager: TrueNASApiManager? = null

                        // Phase 1: try saved token
                        if (token != null) {
                            manager = attemptLoginWithToken(applicationContext, server, account, token)
                            if (manager != null) {
                                _manager.value = manager
                                sessionCoordinator.publishAuthenticated(
                                    manager,
                                    server.id,
                                    account.id,
                                    tokenPersisted = true
                                )
                                setActiveUser(applicationContext, accountId)
                                _appState.value = AppState.Ready(Screen.Main.route)
                                return@launch
                            }
                        }

                        // Phase 2: token failed or missing — try loginEx with saved password
                        val totpManager = attemptTotpAutoLogin(applicationContext, server, account)
                        if (totpManager != null) {
                            when (totpManager.second) {
                                TotpResult.OTP_REQUIRED -> {
                                    _manager.value = totpManager.first
                                    sessionCoordinator.publishPending(totpManager.first)
                                    _appState.value = AppState.TotpRequired(account.username)
                                    return@launch
                                }
                                TotpResult.SUCCESS -> {
                                    _manager.value = totpManager.first
                                    sessionCoordinator.publishAuthenticated(
                                        totpManager.first,
                                        server.id,
                                        account.id,
                                        tokenPersisted = true
                                    )
                                    setActiveUser(applicationContext, accountId)
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

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _appState.value = AppState.Error(
                    "Initialization failed: ${error.message}",
                    Screen.Login.route
                )
            }
        }
    }

    private fun startConnectivityRecovery(context: Context) {
        if (connectivityRecoveryJob != null) return
        val observer = NetworkConnectivityObserver(context)
        connectivityRecoveryJob = viewModelScope.launch {
            try {
                observer.observe().collect { state ->
                    when (state) {
                        ConnectionState.Connected,
                        ConnectionState.Connecting -> recoverActiveSession(context)
                        else -> Unit
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun recoverActiveSession(context: Context) {
        val currentManager = sessionCoordinator.currentAuthenticatedManager ?: return
        try {
            if (!currentManager.isConnected() && !currentManager.ensureConnected()) return
            if (_manager.value !== currentManager) return

            val snapshot = MultiAccountPrefs.getSessionSnapshot(context) ?: return
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context) ?: return
            if (snapshot.serverId != serverId || snapshot.accountId != accountId) return

            currentManager.recoverAfterDisconnect()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
        }
    }

    fun updateManager(newManager: TrueNASApiManager) {
        _manager.value = newManager
        sessionCoordinator.publishPending(newManager)
    }

    suspend fun activateSession(context: Context) {
        val currentManager = _manager.value ?: return
        val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(context) ?: return
        val snapshot = MultiAccountPrefs.getSessionSnapshot(context)
        val tokenPersisted = EncryptedPrefs.getAuthToken(context) != null ||
            snapshot?.let { it.serverId == serverId && it.accountId == accountId } == true
        sessionCoordinator.publishAuthenticated(
            currentManager,
            serverId,
            accountId,
            tokenPersisted
        )
    }

    fun clearSession() {
        sessionCoordinator.clear()
        _manager.value = null
        _currentUserKey.value = null
        periodicPingJob?.cancel()
        periodicPingJob = null
        clearPendingNavigation()
    }

    suspend fun replaceManager(
        newManager: TrueNASApiManager,
        serverId: String,
        accountId: String,
        tokenPersisted: Boolean
    ) {
        val previousManager = _manager.value
        if (previousManager != null && previousManager !== newManager) {
            previousManager.disconnect()
        }
        _manager.value = newManager
        sessionCoordinator.publishAuthenticated(
            newManager,
            serverId,
            accountId,
            tokenPersisted
        )
    }

    fun startPeriodicAppSync(context: Context) {
        AppsRefreshWorker.scheduleRecurring(context)
        AppsRefreshWorker.scheduleImmediate(context)
    }

    fun startPeriodicPing(context: Context) {
        periodicPingJob?.cancel()
        val currentManager = _manager.value ?: return
        val applicationContext = context.applicationContext
        periodicPingJob = viewModelScope.launch {
            while (isActive) {
                if (_manager.value !== currentManager || !currentManager.isConnected()) break
                try {
                    if (MultiAccountPrefs.getTokenForLastUsed(applicationContext) != null) {
                        currentManager.connection.pingConnectionWithResult()
                    }
                } catch (error: CancellationException) {
                    throw error
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
        return when (val outcome = SessionProvider.open(context, server, account)) {
            is SessionProvider.OpenResult.Ready -> {
                setActiveUser(context, account.id)
                outcome.manager
            }
            is SessionProvider.OpenResult.TemporaryAuthenticated -> {
                setActiveUser(context, account.id)
                outcome.manager
            }
            else -> null
        }
    }

    suspend fun isTokenPersisted(
        context: Context,
        serverId: String,
        accountId: String
    ): Boolean {
        val snapshot = MultiAccountPrefs.getSessionSnapshot(context) ?: return false
        return snapshot.serverId == serverId && snapshot.accountId == accountId
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
                    result is ApiResult.Success && result.data is LoginExResult.AuthRespOTPRequired ->
                        Pair(manager, TotpResult.OTP_REQUIRED)
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
            val config = ClientConfig(
                serverUrl = server.serverUrl,
                insecure = server.insecure,
                connectionTimeoutMs = 5000,
                enablePing = true,
                enableDebugLogging = false
            )
            val client = TrueNASClient(config)
            val manager = TrueNASApiManager(client, context)
            var keepConnected = false
            try {
                if (!manager.connect()) return@withTimeoutOrNull null

                val tryLogin = manager.auth.loginWithTokenAndResult(token)
                if (!SessionProvider.isSuccessfulBooleanResult(tryLogin)) {
                    return@withTimeoutOrNull null
                }

                val newTokenResult = manager.auth.generateTokenWithResult(
                    Auth.TokenRequest(ttl = MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS)
                )
                if (newTokenResult !is ApiResult.Success) {
                    return@withTimeoutOrNull null
                }

                MultiAccountPrefs.saveTokenForLastUsed(
                    context,
                    newTokenResult.data,
                    MultiAccountPrefs.LONG_TOKEN_TTL_SECONDS
                )
                keepConnected = true
                manager
            } catch (_: Exception) {
                null
            } finally {
                if (!keepConnected) client.disconnect()
            }
        }
    }
}