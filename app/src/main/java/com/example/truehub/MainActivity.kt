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
                            val hasToken = EncryptedPrefs.getAuthToken(this@MainActivity) != null
                            val hasApiKey = EncryptedPrefs.getApiKey(this@MainActivity) != null

                            if (isLoggedIn && (hasToken || hasApiKey) && mgr.isConnected()) {
                                mgr.connection.pingConnection()
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
                            }
                        )
                    }
                    is AppState.NoInternet ->{
                        NoInternetScreen(
                            message = "No internet connection.",
                            onRetry = {
                                appState = AppState.Initializing
                            }
                        )
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

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun initializeApp(onStateChange: (AppState, TrueNASApiManager?) -> Unit) {
        lifecycleScope.launch {
            try {
                onStateChange(AppState.Initializing, null)
                val (savedUrl, savedInsecure) = Prefs.load(this@MainActivity)

                if (savedUrl == null) {
                    onStateChange(AppState.Ready(Screen.Login.route), null)
                    return@launch
                }

                onStateChange(AppState.CheckingConnection, null)

                val networkUtils = NetworkConnectivityObserver(this@MainActivity)
                if (!networkUtils.isNetworkAvailable()) {
                    onStateChange(AppState.NoInternet,null)
                    return@launch
                }

                // Initialize connection
                val config = ClientConfig(
                    serverUrl = savedUrl,
                    insecure = savedInsecure,
                    connectionTimeoutMs = 10000,
                    enablePing = false,
                    enableDebugLogging = true
                )

                val localTrueNasClient = TrueNASClient(config)
                val localManager = TrueNASApiManager(localTrueNasClient)

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
                    /**
                     * Clear previous token and generate new one
                     */
                    val result = withTimeoutOrNull(1000L){
                        localManager.auth.generateTokenWithResult()
                    }
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.saveAuthToken(this@MainActivity, (result as ApiResult.Success).data)
                    onStateChange(AppState.Ready(Screen.Main.route), localManager)
                } else if (loginMethod == "api_key" ) {
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                    val autoLoginEnabled = EncryptedPrefs.getUseAutoLogin(this@MainActivity)
                    var autoLoginSuccessful = false

                    if (autoLoginEnabled) {
                        onStateChange(AppState.AttemptingAutoLogin, localManager)
                        autoLoginSuccessful = attemptAutoLogin(localManager)
                    }

                    if (autoLoginSuccessful) {
                        onStateChange(AppState.Ready(Screen.Main.route), localManager)
                    } else {
                        EncryptedPrefs.clearAuthToken(this@MainActivity)
                        EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                        EncryptedPrefs.clearApiKey(this@MainActivity)
                        onStateChange(
                            AppState.Error(
                                "Auto Login Failed: Check API Key",
                                Screen.Login.route
                            ), localManager
                        )
                    }
                }else{
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                    onStateChange(
                        AppState.Error(
                            "Token Expired, Login Again",
                            Screen.Login.route
                        ), localManager
                    )
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
    private suspend fun attemptAutoLogin(manager: TrueNASApiManager): Boolean {
        return try {
            val tokenResult = withTimeoutOrNull(10000L){
                manager.auth.generateTokenWithResult()
            }
            if (tokenResult is ApiResult.Success && tokenResult.data.isNotBlank()) {
                val newToken = tokenResult.data
                EncryptedPrefs.saveAuthToken(this, newToken)
                EncryptedPrefs.saveIsLoggedIn(this, true)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    @Composable
    private fun AppNavigation(
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
                TrueHubAppTheme {
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
            }

            composable(Screen.Main.route) {
                // Main screen requires a valid manager
                manager?.let { validManager ->
                    TrueHubAppTheme {
                        MainScreen(validManager, navController)
                    }
                } ?: run {
                    // Fallback if manager is null (shouldn't happen normally)
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
                TrueHubAppTheme {
                    SettingsScreen(
                        onDummyAction = { settingAction ->
                            ToastManager.showInfo(settingAction)
                        },
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
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
@Composable
fun TrueHubAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = M3Typography(),
        content = content
    )
}