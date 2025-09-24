package com.example.truehub

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import com.example.truehub.ui.SetupScreen
import com.example.truehub.ui.components.ModernToastHost
import com.example.truehub.ui.components.ToastManager
import com.example.truehub.ui.login.LoginScreen
import com.example.truehub.ui.settings.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// App initialization states
sealed class AppState {
    object Initializing : AppState()
    object CheckingConnection : AppState()
    object ValidatingToken : AppState()
    data class Ready(val startRoute: String) : AppState()
    data class Error(val message: String, val fallbackRoute: String) : AppState()
}

class MainActivity : ComponentActivity() {
    private var trueNASClient: TrueNASClient? = null
    private var manager: TrueNASApiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var appState by remember { mutableStateOf<AppState>(AppState.Initializing) }
            val navController = rememberNavController()

            // Initialize app state
            LaunchedEffect(Unit) {
                initializeApp { state ->
                    appState = state
                }
            }

            // Keep connection alive
            LaunchedEffect(manager) {
                manager?.let { mgr ->
                    while (true) {
                        try {
                            if (mgr.isConnected()) {
                                mgr.connection.pingConnection()
                            }
                        } catch (e: Exception) {
                            // Connection lost - could show toast or handle gracefully
                        }
                        delay(30000L)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (appState) {
                    is AppState.Initializing -> {
                        LoadingScreen("Initializing...")
                    }
                    is AppState.CheckingConnection -> {
                        LoadingScreen("Connecting to server...")
                    }
                    is AppState.ValidatingToken -> {
                        LoadingScreen("Validating credentials...")
                    }
                    is AppState.Ready -> {
                        AppNavigation(
                            startRoute = (appState as AppState.Ready).startRoute,
                            navController = navController,
                            manager = manager
                        )
                    }
                    is AppState.Error -> {
                        LaunchedEffect((appState as AppState.Error).message) {
                            ToastManager.showError((appState as AppState.Error).message)
                        }
                        AppNavigation(
                            startRoute = (appState as AppState.Error).fallbackRoute,
                            navController = navController,
                            manager = manager
                        )
                    }
                }

                // Toast host at top level
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

                // No saved URL - go to setup
                if (savedUrl == null) {
                    onStateChange(AppState.Ready(Screen.Setup.route))
                    return@launch
                }

                onStateChange(AppState.CheckingConnection)

                // Check internet connectivity first
                if (!isNetworkAvailable()) {
                    onStateChange(
                        AppState.Error(
                            "No internet connection. Please check your network.",
                            Screen.Login.route
                        )
                    )
                    return@launch
                }

                // Initialize client and manager
                val config = ClientConfig(
                    serverUrl = savedUrl,
                    insecure = savedInsecure,
                    connectionTimeoutMs = 15000, // Reduced timeout
                    enablePing = true,
                    enableDebugLogging = true
                )

                trueNASClient = TrueNASClient(config)
                manager = TrueNASApiManager(trueNASClient!!)

                // Attempt connection with timeout
                val connectionResult = try {
                    manager!!.connect()
                    true
                } catch (e: Exception) {
                    false
                }

                if (!connectionResult) {
                    onStateChange(
                        AppState.Error(
                            "Unable to connect to TrueNAS server. Please check server status.",
                            Screen.Login.route
                        )
                    )
                    return@launch
                }

                // Check if user should be logged in
                val isLoggedIn = EncryptedPrefs.getIsLoggedIn(this@MainActivity)
                if (!isLoggedIn) {
                    onStateChange(AppState.Ready(Screen.Login.route))
                    return@launch
                }

                onStateChange(AppState.ValidatingToken)

                // Validate existing credentials
                val token = EncryptedPrefs.getAuthToken(this@MainActivity)
                val apiKey = EncryptedPrefs.getApiKey(this@MainActivity)
                val loginMethod = EncryptedPrefs.getLoginMethod(this@MainActivity)

                val isValid = when (loginMethod) {
                    "api_key" -> {
                        if (apiKey != null) {
                            validateApiKey(apiKey)
                        } else false
                    }
                    "password" -> {
                        if (token != null) {
                            validateToken(token)
                        } else false
                    }
                    else -> false
                }

                if (isValid) {
                    onStateChange(AppState.Ready(Screen.Main.route))
                } else {
                    // Clear invalid credentials
                    EncryptedPrefs.clear(this@MainActivity)
                    onStateChange(
                        AppState.Error(
                            "Session expired. Please log in again.",
                            Screen.Login.route
                        )
                    )
                }

            } catch (e: Exception) {
                onStateChange(
                    AppState.Error(
                        "Initialization failed: ${e.message}",
                        Screen.Setup.route
                    )
                )
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

    @Composable
    private fun AppNavigation(
        startRoute: String,
        navController: androidx.navigation.NavHostController,
        manager: TrueNASApiManager?
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute
        ) {
            composable(Screen.Setup.route) {
                SetupScreen { url, insecure ->
                    Prefs.save(this@MainActivity, url, insecure)

                    // Reinitialize with new settings
                    lifecycleScope.launch {
                        val config = ClientConfig(
                            serverUrl = url,
                            insecure = insecure,
                            connectionTimeoutMs = 15000,
                            enablePing = true,
                            enableDebugLogging = true
                        )

                        trueNASClient = TrueNASClient(config)
                        this@MainActivity.manager = TrueNASApiManager(trueNASClient!!)

                        try {
                            this@MainActivity.manager!!.connect()
                            ToastManager.showSuccess("Connected to server successfully!")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Setup.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            ToastManager.showError("Failed to connect: ${e.message}")
                        }
                    }
                }
            }

            composable(Screen.Login.route) {
                manager?.let {
                    LoginScreen(it, navController)
                } ?: run {
                    // Manager not ready - show error and go back to setup
                    LaunchedEffect(Unit) {
                        ToastManager.showError("Connection not ready. Please reconfigure.")
                        navController.navigate(Screen.Setup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.Main.route) {
                manager?.let {
                    MainScreen(it, navController)
                } ?: run {
                    LaunchedEffect(Unit) {
                        ToastManager.showError("Session invalid. Please log in again.")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onDummyAction = { action ->
                        when (action) {
                            "Logout" -> {
                                lifecycleScope.launch {
                                    EncryptedPrefs.clear(this@MainActivity)
                                    manager?.disconnect()
                                    trueNASClient = null
                                    this@MainActivity.manager = null
                                    ToastManager.showInfo("Logged out successfully")
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            }
                            else -> {
                                ToastManager.showInfo("Action: $action")
                            }
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun LoadingScreen(message: String = "Loading...") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager?.disconnect()
        trueNASClient = null
        manager = null
    }
}