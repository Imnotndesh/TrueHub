package com.example.truehub

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.truehub.data.Client
import com.example.truehub.data.api.Auth
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.ui.LoginScreen
import com.example.truehub.ui.MainScreen
import com.example.truehub.ui.Screen
import com.example.truehub.ui.SetupScreen
import com.example.truehub.ui.settings.SettingsScreen
import com.example.truehub.ui.splash.SplashController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var client: Client? = null
    private var auth: Auth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (savedUrl, savedInsecure) = Prefs.load(this)

        // TODO: Think of this idea 'https://claude.ai/chat/58d0b5c7-b84b-4e3a-8098-39386c96b9a5'

        setContent{
            val navController = rememberNavController()
            if (savedUrl != null) {
                client = Client(savedUrl, savedInsecure)
                client?.connect()
            }
            LaunchedEffect(Unit) {
                while (true) {
                    try {
                        auth?.keepConnection()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    kotlinx.coroutines.delay(30 * 1000L)
                }
            }
            val isLoggedIn by EncryptedPrefs.isLoggedInFlow(this).collectAsState(initial = false)
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn && client != null) {
                SplashController.show("Authenticating")
                    val token = EncryptedPrefs.getAuthToken(this@MainActivity)
                    if (token != null) {
                        try {
                            auth = Auth(client!!)
                            val success = auth?.loginWithToken(token)
                            success?.let {
                                if (!it) {
                                    Toast.makeText(this@MainActivity,"Token invalid. Please login again",Toast.LENGTH_SHORT).show()
                                    EncryptedPrefs.clear(this@MainActivity)
                                    navController.navigate(Screen.Login.route) {     popUpTo(navController.graph.startDestinationId) { inclusive = true }}
                                }
                            }
                            SplashController.hide()
                            navController.navigate(Screen.Main.route) {     popUpTo(navController.graph.startDestinationId) { inclusive = true }}
                        } catch (e: Exception) {
                            // connection or RPC failed
                            navController.navigate(Screen.Login.route) {     popUpTo(navController.graph.startDestinationId) { inclusive = true }}
                        }
                    }
                    if (token == null) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            }
            val startDest = when{
                savedUrl == null -> Screen.Setup.route
                isLoggedIn -> Screen.Main.route
                else -> Screen.Login.route
            }
            NavHost(
                navController = navController,
                startDestination = startDest
            ){
                composable(Screen.Setup.route){
                    SetupScreen { url, insecure ->
                        Prefs.save(this@MainActivity, url, insecure)
                        client = Client(url, insecure)
                        client?.connect()
                        navController.navigate(Screen.Login.route){
                            popUpTo(Screen.Setup.route){
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                composable (Screen.Login.route) {
                    client?.let{
                        LoginScreen(Auth(client!!),navController)
                    }
                }

                composable(Screen.Main.route){
                    client?.let{
                    MainScreen(Auth(client!!),navController)
                    }
                }
                composable(Screen.Settings.route){
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onDummyAction = { action ->
                            when (action) {
                                "Logout" -> {
                                    lifecycleScope.launch {
                                        EncryptedPrefs.clear(this@MainActivity)
                                        client?.disconnect()
                                        client = null
                                        navController.navigate(Screen.Login.route)
                                    }
                                }
                                else -> {
                                    Toast.makeText(this@MainActivity, "Dummy action: $action", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        client?.disconnect()
    }
}