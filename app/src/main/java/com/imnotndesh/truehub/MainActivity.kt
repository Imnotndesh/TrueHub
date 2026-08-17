package com.imnotndesh.truehub

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.ui.MainScreen
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.account.AccountSwitcherScreen
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ModernToastHost
import com.imnotndesh.truehub.ui.components.NoInternetScreen
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertClassesConfigScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServiceCreateScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServiceDetailScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServicesListScreen
import com.imnotndesh.truehub.ui.login.LoginScreen
import com.imnotndesh.truehub.ui.settings.SettingsEvent
import com.imnotndesh.truehub.ui.settings.SettingsScreen
import com.imnotndesh.truehub.ui.settings.SettingsScreenViewModel
import com.imnotndesh.truehub.ui.settings.screens.AboutScreen
import com.imnotndesh.truehub.ui.settings.logging.AppLoggingScreen
import com.imnotndesh.truehub.ui.settings.screens.LicensesScreen
import com.imnotndesh.truehub.ui.settings.screens.ThemeScreen
import com.imnotndesh.truehub.ui.settings.sheets.ChangePasswordScreen
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleWidgetIntent(intent)

        setContent {
            val personalization by PersonalizationManager.state.collectAsState()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            TrueHubAppTheme(
                theme = personalization.theme,
                darkTheme = systemDark || personalization.blackMode,
                isBlackMode = personalization.blackMode
            ) {
                MainActivityContent(
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        when (intent?.action) {
            "com.imnotndesh.truehub.OPEN_APPS" ->
                viewModel.requestNavigateTo(Screen.Apps.route)
            "com.imnotndesh.truehub.OPEN_SYSTEM_UPDATE" ->
                viewModel.requestNavigateTo(Screen.SystemUpdateScreen.route)
            "com.imnotndesh.truehub.OPEN_INSTALL_APP" ->
                viewModel.requestNavigateTo(Screen.Marketplace.route)
            "com.imnotndesh.truehub.OPEN_UPDATE_APPS" ->
                viewModel.requestNavigateTo(Screen.Apps.route)
            "com.imnotndesh.truehub.OPEN_CONFIGURE_INSTANCE" ->
                viewModel.requestNavigateTo(Screen.InstanceConfigScreen.route)
            "com.imnotndesh.truehub.OPEN_UPDATE_INSTANCE" ->
                viewModel.requestNavigateTo(Screen.SystemUpdateScreen.route)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainActivityContent(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val appState by viewModel.appState.collectAsState()
    val manager by viewModel.manager.collectAsState()
    val navController = rememberNavController()
    val notifPermission = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )

    LaunchedEffect(Unit) {
        viewModel.initializeApp(context)
    }
    LaunchedEffect(manager) {
        manager?.let {
            viewModel.startPeriodicPing(context)
            viewModel.startPeriodicAppSync(context)
        }
    }
    LaunchedEffect(Unit) {
        if (!notifPermission.status.isGranted) {
            notifPermission.launchPermissionRequest()
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
                    viewModel = viewModel,
                    manager = manager
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
            is AppState.TotpRequired -> {
                AppNavigation(
                    startRoute = Screen.Login.route,
                    navController = navController,
                    viewModel = viewModel,
                    manager = manager,
                    totpUsername = (appState as AppState.TotpRequired).username
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

@Composable
private fun AppNavigation(
    startRoute: String,
    navController: NavHostController,
    viewModel: MainViewModel,
    manager: TrueNASApiManager?,
    totpUsername: String? = null
) {
    val context = LocalContext.current
    val pendingNav by viewModel.pendingNavigation.collectAsState()
    val personalization by PersonalizationManager.state.collectAsState()
    val userKey by viewModel.currentUserKey.collectAsState()
    val personalizationUserKey = userKey ?: PersonalizationManager.DEFAULT_USER_KEY
    LaunchedEffect(pendingNav) {
        val route = pendingNav ?: return@LaunchedEffect
        if (navController.currentDestination?.route != Screen.Main.route) {
            navController.navigate(Screen.Main.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
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
                        anim {
                            enter = 0
                            exit = 0
                            popEnter = 0
                            popExit = 0
                        }
                    }
                },
                startInOtpMode = totpUsername != null,
                totpUsername = totpUsername
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
                MainScreen(
                    manager = validManager,
                    rootNavController = navController,
                    viewModel = viewModel
                )
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
                onNavigateToLogging = {
                    navController.navigate(Screen.AppLogging.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.AccountSwitcher.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.AppLogging.route) {
            manager?.let {
                AppLoggingScreen(
                    manager = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.ChangePassword.route) {
            val context = LocalContext.current
            val settingsViewModel: SettingsScreenViewModel = viewModel(
                factory = SettingsScreenViewModel.SettingsViewModelFactory(
                    manager = manager!!,
                    application = context.applicationContext as Application
                )
            )
            ChangePasswordScreen(
                manager,
                { oldPassword, newPassword ->
                    settingsViewModel.handleEvent(
                        SettingsEvent.ChangePassword(oldPassword, newPassword)
                    )
                },
                { navController.popBackStack() }
            )
        }
        composable(Screen.AlertServicesList.route) {
            manager?.let { validManager ->
                AlertServicesListScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { service ->
                        navController.navigate(Screen.AlertServiceDetail.createRoute(service.id))
                    },
                    onNavigateToCreate = {
                        navController.navigate(Screen.AlertServiceCreate.route)
                    },
                    onNavigateToClassesConfig = { navController.navigate(Screen.AlertClassesConfig.route) }
                )
            }
        }
        composable(
            Screen.AlertServiceDetail.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: return@composable
            manager?.let { validManager ->
                AlertServiceDetailScreen(
                    serviceId = serviceId,
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.AlertServiceCreate.route) {
            manager?.let { validManager ->
                AlertServiceCreateScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.AlertClassesConfig.route) {
            manager?.let {
                AlertClassesConfigScreen(
                    manager = manager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.About.route) {
            manager?.let { validManager ->
                AboutScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            } ?: run {
                LoadingScreen("Redirecting...")
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.Licenses.route) {
            manager?.let { validManager ->
                LicensesScreen(
                    manager = validManager,
                    onNavigateBack = { navController.popBackStack() }
                )
            } ?: run {
                LoadingScreen("Redirecting...")
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.Theme.route) {
            ThemeScreen(
                manager = manager,
                userKey = personalizationUserKey,
                currentTheme = personalization.theme,
                onThemeSelected = { newTheme ->
                    val userKey = viewModel.currentUserKey.value ?: PersonalizationManager.DEFAULT_USER_KEY
                    PersonalizationManager.saveTheme(context, userKey, newTheme)
                },
                onNavigateBack = { navController.popBackStack() },
                isBlackModeEnabled = personalization.blackMode,
                onBlackModeToggled = { isToggled ->
                    val userKey = viewModel.currentUserKey.value ?: PersonalizationManager.DEFAULT_USER_KEY
                    PersonalizationManager.saveBlackMode(context, userKey, isToggled)
                }
            )
        }
    }
}