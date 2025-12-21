package com.imnotndesh.truehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imnotndesh.truehub.data.helpers.Prefs
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var currentTheme by rememberSaveable { mutableStateOf(Prefs.loadTheme(this)) }
            val viewModel: MainViewModel = viewModel()

            TrueHubAppTheme(theme = currentTheme) {
                MainActivityContent(
                    viewModel = viewModel,
                    currentTheme = currentTheme,
                    onThemeChanged = { newTheme ->
                        currentTheme = newTheme
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun MainActivityContent(
    viewModel: MainViewModel,
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit
) {
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsState()
    val manager by viewModel.manager.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.initializeApp(context)
    }

    LaunchedEffect(manager) {
        manager?.let {
            viewModel.startPeriodicPing(context)
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
                    viewModel = viewModel,
                    manager = manager,
                    onThemeChanged = onThemeChanged,
                    currentTheme = currentTheme
                )
            }
            is AppState.Error -> {
                LaunchedEffect((appState as AppState.Error).message) {
                    ToastManager.showError((appState as AppState.Error).message)
                }
                AppNavigation(
                    startRoute = (appState as AppState.Error).fallbackRoute,
                    navController = navController,
                    viewModel = viewModel,
                    manager = manager,
                    onThemeChanged = onThemeChanged,
                    currentTheme = currentTheme
                )
            }
            is AppState.NoInternet -> {
                TrueHubAppTheme {
                    NoInternetScreen(
                        message = "No internet connection.",
                        onRetry = {
                            viewModel.initializeApp(context)
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

@Composable
private fun AppNavigation(
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    startRoute: String,
    navController: androidx.navigation.NavHostController,
    viewModel: MainViewModel,
    manager: com.imnotndesh.truehub.data.api.TrueNASApiManager?
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                existingManager = manager,
                navController = navController,
                onManagerInitialized = { newManager ->
                    viewModel.updateManager(newManager)
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
                    (context as? ComponentActivity)?.lifecycleScope?.launch {
                        val loginManager = viewModel.attemptLoginWithProfile(context, server, account)
                        if (loginManager != null) {
                            viewModel.updateManager(loginManager)
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.AccountSwitcher.route) { inclusive = true }
                            }
                        } else {
                            ToastManager.showError("Failed to login with saved account")
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.AccountSwitcher.route) { inclusive = true }
                            }
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
                    navController.navigate(Screen.AccountSwitcher.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Licenses.route) {
            LicensesScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Settings.route) {
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