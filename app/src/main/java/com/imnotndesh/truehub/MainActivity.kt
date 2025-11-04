package com.imnotndesh.truehub

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.EncryptedPrefs
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.NetworkConnectivityObserver
import com.imnotndesh.truehub.data.helpers.Prefs
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.ui.MainScreen
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.account.AccountSwitcherScreen
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ModernToastHost
import com.imnotndesh.truehub.ui.components.NoInternetScreen
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.login.LoginScreen
import com.imnotndesh.truehub.ui.settings.SettingsScreen
import com.imnotndesh.truehub.ui.settings.screens.AboutScreen
import com.imnotndesh.truehub.ui.settings.screens.LicensesScreen
import com.imnotndesh.truehub.ui.settings.screens.ThemeScreen
import com.imnotndesh.truehub.ui.theme.AppTheme
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class AppState {
    object Initializing : AppState()
    object CheckingConnection : AppState()
    object ValidatingToken : AppState()
    object NoInternet :AppState()
    object AttemptingAutoLogin : AppState()
    data class Ready(val startRoute: String) : AppState()
    data class Error(val message: String, val fallbackRoute: String) : AppState()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var currentTheme by remember { mutableStateOf(Prefs.loadTheme(this)) }
            TrueHubAppTheme(theme = currentTheme) {
                var appState by remember { mutableStateOf<AppState>(AppState.Initializing) }
                val navController = rememberNavController()


                var manager by remember { mutableStateOf<TrueNASApiManager?>(null) }
                LaunchedEffect(Unit) {
                    initializeApp { state, newManager ->
                        appState = state
                        if (newManager != null) {
                            manager = newManager
                        }
                    }
                }
                LaunchedEffect(manager) {
                    manager?.let { mgr ->
                        while (true) {
                            try {
                                val isLoggedIn = EncryptedPrefs.getIsLoggedIn(this@MainActivity)
                                val hasToken =
                                    EncryptedPrefs.getAuthToken(this@MainActivity) != null
                                val hasApiKey = EncryptedPrefs.getApiKey(this@MainActivity) != null

                                if (isLoggedIn && (hasToken || hasApiKey) && mgr.isConnected()) {
                                    mgr.connection.pingConnectionWithResult()
                                }
                            } catch (_: Exception) {
                            }
                            delay(30000L)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (appState) {
                        is AppState.Initializing -> LoadingScreen("Initializing...")
                        is AppState.CheckingConnection -> LoadingScreen("Connecting to server...")
                        is AppState.ValidatingToken -> LoadingScreen("Validating credentials...")
                        is AppState.AttemptingAutoLogin -> LoadingScreen("Attempting auto sign-in...")
                        is AppState.Ready -> {
                            AppNavigation(
                                startRoute = (appState as AppState.Ready).startRoute,
                                navController = navController,
                                manager = manager,
                                onThemeChanged = { newTheme ->
                                    currentTheme = newTheme
                                },
                                currentTheme = currentTheme,
                                onManagerUpdate = { newManager ->
                                    manager = newManager
                                }
                            )
                        }

                        is AppState.Error -> {
                            LaunchedEffect((appState as AppState.Error).message) {
                                ToastManager.showError((appState as AppState.Error).message)
                            }
                            AppNavigation(
                                startRoute = (appState as AppState.Error).fallbackRoute,
                                navController = navController,
                                manager = manager,
                                onManagerUpdate = { newManager ->
                                    manager = newManager
                                },
                                onThemeChanged = { newTheme ->
                                    currentTheme = newTheme
                                },
                                currentTheme = currentTheme,
                            )
                        }

                        is AppState.NoInternet -> {
                            TrueHubAppTheme {
                                NoInternetScreen(
                                    message = "No internet connection.",
                                    onRetry = {
                                        lifecycleScope.launch {
                                            initializeApp { state, newManager ->
                                                appState = state
                                                if (newManager != null) {
                                                    manager = newManager
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                    ) {
                        ModernToastHost()
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun initializeApp(onStateChange: (AppState, TrueNASApiManager?) -> Unit) {
        lifecycleScope.launch {
            try {
                onStateChange(AppState.Initializing, null)
                val networkUtils = NetworkConnectivityObserver(this@MainActivity)
                if (!networkUtils.isNetworkAvailable()) {
                    onStateChange(AppState.NoInternet, null)
                    return@launch
                }

                val hasSavedAccounts = MultiAccountPrefs.getAccounts(this@MainActivity).isNotEmpty()

                if (hasSavedAccounts) {
                    onStateChange(AppState.Ready(Screen.AccountSwitcher.route), null)
                } else {
                    onStateChange(AppState.Ready(Screen.Login.route), null)
                }

            } catch (e: Exception) {
                onStateChange(
                    AppState.Error(
                        "Initialization failed: ${e.message}",
                        Screen.Login.route
                    ),
                    null
                )
            }
        }
    }
    private suspend fun attemptAutoLoginWithProfile(
        server: SavedServer,
        account: SavedAccount
    ): TrueNASApiManager? {
        return try {
            val config = ClientConfig(
                serverUrl = server.serverUrl,
                insecure = server.insecure,
                connectionTimeoutMs = 10000,
                enablePing = false,
                enableDebugLogging = false
            )

            val client = TrueNASClient(config)
            val manager = TrueNASApiManager(client, this@MainActivity)

            if (!manager.connect()) return null

            val (cred1, cred2) = MultiAccountPrefs.getAccountCredentials(
                this@MainActivity,
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
                        this@MainActivity,
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

    @Composable
    private fun AppNavigation(
        currentTheme: AppTheme,
        onThemeChanged: (AppTheme) -> Unit,
        startRoute: String,
        navController: androidx.navigation.NavHostController,
        manager: TrueNASApiManager?,
        onManagerUpdate: (TrueNASApiManager) -> Unit
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute
        ) {
            composable(Screen.Login.route) {
                    LoginScreen(
                        existingManager = manager,
                        navController = navController,
                        onManagerInitialized = { newManager ->
                            onManagerUpdate(newManager)
                        },
                        onLoginSuccess = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
            }
            composable(Screen.AccountSwitcher.route) {
                AccountSwitcherScreen(
                    onAccountSelected = { server, account ->
                        lifecycleScope.launch {
                            val manager = attemptLoginWithProfile(server, account)
                            if (manager != null) {
                                onManagerUpdate(manager)
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.AccountSwitcher.route) { inclusive = true }
                                }
                            } else {
                                ToastManager.showError("Failed to login with saved account")
                            }
                        }
                    },
                    onAddNewAccount = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Main.route) {
                manager?.let { validManager ->
                MainScreen(validManager, navController)
                } ?: run {
                    LaunchedEffect(Unit) {
                        ToastManager.showError("Session invalid. Please log in again.")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                    LoadingScreen("Redirecting to login...")
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    manager = manager,
                    onDummyAction = { settingAction ->
                        ToastManager.showInfo("Work in progress for: $settingAction")
                    },
                    onNavigateToTheme = {
                        navController.navigate(Screen.Theme.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToLicenses = {
                        navController.navigate(Screen.Licenses.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Settings.route) { inclusive = true }
                        }
                    }
                )
            }

            composable( Screen.About.route) {
                    AboutScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
            }
            composable( Screen.Licenses.route) {
                LicensesScreen(
                    onNavigateBack = {
                        navController.navigate(Screen.Settings.route){
                            popUpTo(Screen.Settings.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Theme.route) {
                ThemeScreen(
                    currentTheme = currentTheme,
                    onThemeSelected = { newTheme ->
                        onThemeChanged(newTheme)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
    private suspend fun attemptLoginWithProfile(
        server: SavedServer,
        account: SavedAccount
    ): TrueNASApiManager? {
        return attemptAutoLoginWithProfile(server, account)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
