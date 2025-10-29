package com.example.truehub

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.api.AuthService
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.data.helpers.NetworkConnectivityObserver
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.data.models.Config.ClientConfig
import com.example.truehub.ui.MainScreen
import com.example.truehub.ui.Screen
import com.example.truehub.ui.components.LoadingScreen
import com.example.truehub.ui.components.ModernToastHost
import com.example.truehub.ui.components.NoInternetScreen
import com.example.truehub.ui.components.ToastManager
import com.example.truehub.ui.login.LoginScreen
import com.example.truehub.ui.settings.SettingsScreen
import com.example.truehub.ui.settings.screens.AboutScreen
import com.example.truehub.ui.settings.screens.LicensesScreen
import com.example.truehub.ui.settings.screens.ThemeScreen
import com.example.truehub.ui.theme.AppTheme
import com.example.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.material3.Typography as M3Typography

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
                                // Connection lost - this is normal if not authenticated
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
                val (savedUrl, savedInsecure) = Prefs.load(this@MainActivity)

                if (savedUrl == null) {
                    onStateChange(AppState.Ready(Screen.Login.route), null)
                    return@launch
                }
                // Initialize connection
                val config = ClientConfig(
                    serverUrl = savedUrl,
                    insecure = savedInsecure,
                    connectionTimeoutMs = 10000,
                    enablePing = false,
                    enableDebugLogging = false
                )

                val localTrueNasClient = TrueNASClient(config)
                val localManager = TrueNASApiManager(localTrueNasClient,this@MainActivity)

                val connectionResult = try {
                    localManager.connect()
                    true
                } catch (_: Exception) {
                    false
                }

                if (!connectionResult) {
                    onStateChange(
                        AppState.Error(
                            "Unable to connect to server.",
                            Screen.Login.route
                        ),
                        localManager
                    )
                    return@launch
                }

                // Validate credentials
                onStateChange(AppState.ValidatingToken, localManager)

                val token = EncryptedPrefs.getAuthToken(this@MainActivity)
                val apiKey = EncryptedPrefs.getApiKey(this@MainActivity)
                val loginMethod = EncryptedPrefs.getLoginMethod(this@MainActivity)

                val isValid = when (loginMethod) {
                    "api_key" -> apiKey?.let { validateApiKey(it, localManager) } ?: false
                    "password" -> token?.let { validateToken(it, localManager) } ?: false
                    else -> false
                }

                if (isValid) {
                    val result = withTimeoutOrNull(1000L) {
                        localManager.auth.generateTokenWithResult()
                    }
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.saveAuthToken(
                        this@MainActivity,
                        (result as ApiResult.Success).data
                    )
                    onStateChange(AppState.Ready(Screen.Main.route), localManager)
                } else {
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.clearIsLoggedIn(this@MainActivity)

                    val autoLoginEnabled = EncryptedPrefs.getUseAutoLogin(this@MainActivity)?:false

                    if (autoLoginEnabled) {
                        onStateChange(AppState.AttemptingAutoLogin, localManager)
                        val autoLoginSuccessful = attemptAutoLogin(localManager, loginMethod)

                        if (autoLoginSuccessful) {
                            val result = withTimeoutOrNull(1000L) {
                                localManager.auth.generateTokenWithResult()
                            }
                            EncryptedPrefs.clearAuthToken(this@MainActivity)
                            EncryptedPrefs.saveAuthToken(
                                this@MainActivity,
                                (result as ApiResult.Success).data
                            )
                            EncryptedPrefs.saveIsLoggedIn(this@MainActivity)
                            onStateChange(AppState.Ready(Screen.Main.route), localManager)
                        } else {
                            EncryptedPrefs.clearApiKey(this@MainActivity)
                            onStateChange(
                                AppState.Error(
                                    "Auto login failed",
                                    Screen.Login.route
                                ), localManager
                            )
                        }
                    } else {
                        onStateChange(
                            AppState.Error(
                                "Session expired",
                                Screen.Login.route
                            ), localManager
                        )
                    }
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
    private suspend fun attemptAutoLogin(manager: TrueNASApiManager, loginMethod: String?): Boolean {
        return try {
            when (loginMethod) {
                "api_key" -> {
                    val apiKey = EncryptedPrefs.getApiKey(this)
                    if (apiKey != null){
                        val loginResult = withTimeoutOrNull(10000L) {
                            manager.auth.loginWithApiKeyWithResult(apiKey)
                        }
                        loginResult is ApiResult.Success && loginResult.data
                    }else{
                        false
                    }
                }
                "password" -> {
                    val username = EncryptedPrefs.getUsername(this)
                    val password = EncryptedPrefs.getUserPass(this)

                    if (username != null && password != null) {
                        val loginResult = withTimeoutOrNull(10000L) {
                            manager.auth.loginUserWithResult(AuthService.DefaultAuth(username,password))
                        }
                        loginResult is ApiResult.Success && loginResult.data
                    } else {
                        false
                    }
                }
                else -> false
            }
        } catch (_: Exception) {
            false
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

    private suspend fun validateToken(token: String, manager: TrueNASApiManager): Boolean {
        return try {
            val result = withTimeoutOrNull(10000L){
                manager.auth.loginWithTokenAndResult(token)
            }
            return result is ApiResult.Success && result.data
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun validateApiKey(apiKey: String, manager: TrueNASApiManager): Boolean {
        return try {
            val result = withTimeoutOrNull(10000L){
                manager.auth.loginWithApiKeyWithResult(apiKey)
            }
            result is ApiResult.Success && result.data
        } catch (_: Exception) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
