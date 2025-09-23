package com.example.truehub

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.truehub.ui.login.LoginScreen
import com.example.truehub.ui.settings.SettingsScreen
import com.example.truehub.ui.splash.SplashController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private var trueNASClient: TrueNASClient? = null
    private var manager: TrueNASApiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (savedUrl, savedInsecure) = Prefs.load(this)

        // TODO: Think of this idea 'https://claude.ai/chat/58d0b5c7-b84b-4e3a-8098-39386c96b9a5'

        setContent {
            val navController = rememberNavController()

            if (savedUrl != null) {
                val config = ClientConfig(
                    serverUrl = savedUrl,
                    insecure = savedInsecure,
                    connectionTimeoutMs = 30000,
                    enablePing = true,
                    enableDebugLogging = true
                )
                trueNASClient = TrueNASClient(config)
                manager = TrueNASApiManager(trueNASClient!!)

                // Connect in a coroutine
                runBlocking {
                    manager?.connect()
                }
            }

            // Keep connection alive with periodic ping
            LaunchedEffect(manager) {
                manager?.let { mgr ->
                    while (true) {
                        try {
                            if (mgr.isConnected()) {
                                // Use the connection service or auth service to keep connection alive
                                // This replaces the old auth?.keepConnection() call
                                mgr.connection.pingConnection() // Assuming you have a ping method in ConnectionService
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        kotlinx.coroutines.delay(30 * 1000L)
                    }
                }
            }

            val isLoggedIn by EncryptedPrefs.isLoggedInFlow(this).collectAsState(initial = false)
            val token = runBlocking{ EncryptedPrefs.getAuthToken(this@MainActivity) }

            runBlocking {
                if (isLoggedIn && manager != null) {
                    if (token != null) {
                        val result = manager?.auth?.loginWithTokenAndResult(token)
                        result?.let { authResult ->
                            when (authResult) {
                                is ApiResult.Success -> {
                                    if (authResult.data) {
                                        // Token is valid, proceed to main screen
                                        navController.navigate(Screen.Main.route) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    } else {
                                        // Token is invalid
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Token invalid. Please login again",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EncryptedPrefs.clear(this@MainActivity)
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                }
                                is ApiResult.Error -> {
                                    // Connection or RPC failed
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Authentication failed: ${authResult.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    SplashController.hide()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                                ApiResult.Loading -> setContent { "Checking Api Key".LoadingScreen() }
                            }
                        } ?: run {
                            // Manager is null
                            SplashController.hide()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    } else {
                        SplashController.hide()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }

            val startDest = when {
                savedUrl == null -> Screen.Setup.route
                isLoggedIn -> Screen.Main.route
                else -> Screen.Login.route
            }

            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                composable(Screen.Setup.route) {
                    SetupScreen { url, insecure ->
                        Prefs.save(this@MainActivity, url, insecure)
                        val config = ClientConfig(
                            serverUrl = url,
                            insecure = insecure,
                            connectionTimeoutMs = 30000,
                            enablePing = true,
                            enableDebugLogging = true
                        )
                        trueNASClient = TrueNASClient(config)
                        manager = TrueNASApiManager(trueNASClient!!)

                        // Connect in background
                        lifecycleScope.launch {
                            manager?.connect()
                        }

                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Setup.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                composable(Screen.Login.route) {
                    manager?.let {
                        LoginScreen(it, navController)
                    }
                }

                composable(Screen.Main.route) {
                    manager?.let {
                        MainScreen(it, navController)
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
                                        manager = null
                                        navController.navigate(Screen.Login.route)
                                    }
                                }
                                else -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Dummy action: $action",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    @Composable
    private fun String.LoadingScreen() {
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
                        text = this@LoadingScreen,
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