package com.imnotndesh.truehub.ui

import AppsScreenViewModel
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.homepage.HomeScreen
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetExplorerScreen
import com.imnotndesh.truehub.ui.homepage.pools.PoolDataHolder
import com.imnotndesh.truehub.ui.homepage.pools.PoolDetailsScreen
import com.imnotndesh.truehub.ui.services.apps.AppDataHolder
import com.imnotndesh.truehub.ui.services.apps.AppsScreen
import com.imnotndesh.truehub.ui.services.apps.details.AppInfoScreen
import com.imnotndesh.truehub.ui.services.apps.details.RollbackVersionScreen
import com.imnotndesh.truehub.ui.services.apps.details.UpgradeSummaryScreen
import com.imnotndesh.truehub.ui.services.containers.ContainersScreen
import com.imnotndesh.truehub.ui.services.vm.VmsScreen

private data class NavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(manager: TrueNASApiManager, rootNavController: NavController) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Define navigation items with selected/unselected icon states
    val navItems = remember {
        listOf(
            NavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
            NavItem(Screen.Apps, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
            NavItem(Screen.Containers, "Containers", Icons.Filled.Inventory, Icons.Outlined.Inventory2),
            NavItem(Screen.Vms, "VMs", Icons.Filled.Computer, Icons.Outlined.Computer)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (isLandscape) {
        Row {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight(),
                header = {
                    // Placeholder for Logo
                }
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavClick(navController, item.screen.route) },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Crossfade(targetState = selected, label = "iconFade") { isSelected ->
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Shared Navigation Host
            TrueHubNavGraph(
                navController = navController,
                manager = manager,
                rootNavController = rootNavController,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavClick(navController, item.screen.route) },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Crossfade(targetState = selected, label = "iconFade") { isSelected ->
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            TrueHubNavGraph(
                navController = navController,
                manager = manager,
                rootNavController = rootNavController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun TrueHubNavGraph(
    navController: NavHostController,
    manager: TrueNASApiManager,
    rootNavController: NavController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                manager,
                onNavigateToSettings = { rootNavController.navigate(Screen.Settings.route) },
                onPoolClick = { pool: System.Pool ->
                    PoolDataHolder.currentPool = pool
                    navController.navigate(Screen.PoolDetails.route)
                }
            )
        }

        composable(Screen.Apps.route) {
            AppsScreen(
                manager = manager,
                onNavigateToAppInfo = { app ->
                    AppDataHolder.selectedApp = app
                    navController.navigate("app_details")
                },
                onNavigateToUpgrade = { appName ->
                    navController.navigate("app_upgrade/$appName")
                },
                onNavigateToRollback = { appName ->
                    navController.navigate("app_rollback/$appName")
                }
            )
        }

        composable(Screen.PoolDetails.route) {
            PoolDetailsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFiles = { poolName: String ->
                    navController.navigate("${Screen.Files.route}/$poolName")
                }
            )
        }

        composable("app_details") {
            val app = AppDataHolder.selectedApp
            if (app != null) {
                AppInfoScreen(
                    app = app,
                    manager = manager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = "app_upgrade/{appName}",
            arguments = listOf(navArgument("appName") { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val viewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by viewModel.uiState.collectAsState()
            androidx.compose.runtime.LaunchedEffect(appName) {
                viewModel.clearUpgradeSummary()
                viewModel.loadUpgradeSummary(appName)
            }

            val summary = uiState.upgradeSummaryResult
            val isLoading = uiState.isLoadingUpgradeSummaryForApp == appName

            if (isLoading) {
                com.imnotndesh.truehub.ui.components.LoadingScreen("Checking upgrades...")
            } else if (summary != null) {
                UpgradeSummaryScreen(
                    appName = appName,
                    summary = summary,
                    manager = manager,
                    isUpgrading = uiState.upgradeJobs[appName]?.state == "UPGRADING", // Matches ViewModel "UPGRADING" state
                    onConfirmUpgrade = { viewModel.upgradeApp(appName) },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else if (uiState.error != null) {

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        // App Rollback
        composable(
            route = "app_rollback/{appName}",
            arguments = listOf(navArgument("appName") { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val viewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by viewModel.uiState.collectAsState()

            RollbackVersionScreen(
                appName = appName,
                versions = uiState.rollbackVersions,
                isLoadingVersions = uiState.isLoadingRollbackVersions,
                rollbackJobState = uiState.rollbackJobs[appName],
                manager = manager,
                onLoadVersions = { viewModel.loadRollbackVersions(appName) },
                onConfirmRollback = { version, snapshot ->
                    viewModel.rollbackApp(appName, version, snapshot)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Containers.route) {
            ContainersScreen(manager)
        }

        composable(Screen.Vms.route) {
            VmsScreen(manager)
        }

        composable(
            route = "${Screen.Files.route}/{poolName}",
            arguments = listOf(
                navArgument("poolName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val poolName = backStackEntry.arguments?.getString("poolName") ?: ""
            DatasetExplorerScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                poolName = poolName
            )
        }
    }
}

private fun onNavClick(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}