package com.example.truehub

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.data.models.Config.ClientConfig
import com.example.truehub.ui.MainScreen
import com.example.truehub.ui.Screen
import com.example.truehub.ui.components.LoadingScreen
import com.example.truehub.ui.components.ModernToastHost
import com.example.truehub.ui.components.ToastManager
import com.example.truehub.ui.login.LoginScreen
import com.example.truehub.ui.settings.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Typography as M3Typography

sealed class AppState {
    object Initializing : AppState()
    object CheckingConnection : AppState()
    object ValidatingToken : AppState()
    object AttemptingAutoLogin : AppState()
    data class Ready(val startRoute: String) : AppState()
    data class Error(val message: String, val fallbackRoute: String) : AppState()
}

class MainActivity : ComponentActivity() {
    private var trueNASClient: TrueNASClient? = null
    private var manager: TrueNASApiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var appState by remember { mutableStateOf<AppState>(AppState.Initializing) }
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                initializeApp { state -> appState = state }
            }

            // Keep connection alive - ONLY if authenticated
            LaunchedEffect(manager) {
                manager?.let { mgr ->
                    while (true) {
                        try {
                            // Only ping if we're logged in AND have valid credentials
                            val isLoggedIn = EncryptedPrefs.getIsLoggedIn(this@MainActivity)
                            val hasToken = EncryptedPrefs.getAuthToken(this@MainActivity) != null
                            val hasApiKey = EncryptedPrefs.getApiKey(this@MainActivity) != null

                            if (isLoggedIn && (hasToken || hasApiKey) && mgr.isConnected()) {
                                mgr.connection.pingConnection()
                            }
                        } catch (e: Exception) {
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
                                trueNASClient = newManager.getClient()
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
                                trueNASClient = newManager.getClient()
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

    private fun initializeApp(onStateChange: (AppState) -> Unit) {
        lifecycleScope.launch {
            try {
                onStateChange(AppState.Initializing)

                val (savedUrl, savedInsecure) = Prefs.load(this@MainActivity)

                // No saved URL - go to login (will handle setup)
                if (savedUrl == null) {
                    onStateChange(AppState.Ready(Screen.Login.route))
                    return@launch
                }

                // Has saved URL - try to initialize connection
                onStateChange(AppState.CheckingConnection)

                if (!isNetworkAvailable()) {
                    // No network - go to login, let user see the error
                    onStateChange(
                        AppState.Error(
                            "No internet connection.",
                            Screen.Login.route
                        )
                    )
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

                trueNASClient = TrueNASClient(config)
                manager = TrueNASApiManager(trueNASClient!!)

                val connectionResult = try {
                    manager!!.connect()
                    true
                } catch (e: Exception) {
                    false
                }

                if (!connectionResult) {
                    onStateChange(
                        AppState.Error(
                            "Unable to connect to server.",
                            Screen.Login.route
                        )
                    )
                    return@launch
                }

                // Validate credentials
                onStateChange(AppState.ValidatingToken)

                val token = EncryptedPrefs.getAuthToken(this@MainActivity)
                val apiKey = EncryptedPrefs.getApiKey(this@MainActivity)
                val loginMethod = EncryptedPrefs.getLoginMethod(this@MainActivity)

                val isValid = when (loginMethod) {
                    "api_key" -> apiKey?.let { validateApiKey(it) } ?: false
                    "password" -> token?.let { validateToken(it) } ?: false
                    else -> false
                }

                if (isValid) {
                    /**
                     * Clear previous token and generate new one
                     */
                    val result = manager?.auth?.generateTokenWithResult()
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.saveAuthToken(this@MainActivity, (result as ApiResult.Success).data)
                    onStateChange(AppState.Ready(Screen.Main.route))
                } else if (loginMethod == "api_key" ) {
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                    val autoLoginEnabled = EncryptedPrefs.getUseAutoLogin(this@MainActivity)
                    var autoLoginSuccessful = false

                    if (autoLoginEnabled) {
                        onStateChange(AppState.AttemptingAutoLogin)
                        autoLoginSuccessful = attemptAutoLogin()
                    }

                    if (autoLoginSuccessful) {
                        onStateChange(AppState.Ready(Screen.Main.route))
                    } else {
                        EncryptedPrefs.clearAuthToken(this@MainActivity)
                        EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                        EncryptedPrefs.clearApiKey(this@MainActivity)
                        onStateChange(
                            AppState.Error(
                                "Auto Login Failed: Check API Key",
                                Screen.Login.route
                            )
                        )
                    }
                }else{
                    EncryptedPrefs.clearAuthToken(this@MainActivity)
                    EncryptedPrefs.clearIsLoggedIn(this@MainActivity)
                    onStateChange(
                        AppState.Error(
                            "Token Expired, Login Again",
                            Screen.Login.route
                        )
                    )
                }

            } catch (e: Exception) {
                onStateChange(
                    AppState.Error(
                        "Initialization failed: ${e.message}",
                        Screen.Login.route
                    )
                )
            }
        }
    }
    private suspend fun attemptAutoLogin(): Boolean {
        return try {
            val tokenResult = manager?.auth?.generateTokenWithResult()
            if (tokenResult is ApiResult.Success && tokenResult.data.isNotBlank()) {
                val newToken = tokenResult.data
                EncryptedPrefs.saveAuthToken(this, newToken)
                EncryptedPrefs.saveIsLoggedIn(this, true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
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
                manager?.let {
                    TrueHubAppTheme {
                        MainScreen(it, navController)
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

    private suspend fun validateToken(token: String): Boolean {
        return try {
            val result = manager?.auth?.loginWithTokenAndResult(token)
            result is ApiResult.Success && result.data
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun validateApiKey(apiKey: String): Boolean {
        return try {
            val result = manager?.auth?.loginWithApiKeyWithResult(apiKey)
            result is ApiResult.Success && result.data
        } catch (e: Exception) {
            false
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager?.disconnect()
        trueNASClient = null
        manager = null
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