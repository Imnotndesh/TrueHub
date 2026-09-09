package com.imnotndesh.truehub.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.imnotndesh.truehub.MainViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.NavbarDestination
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.canUpgradeNow
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PillNavBar
import com.imnotndesh.truehub.ui.components.rememberHideOnScrollState
import com.imnotndesh.truehub.ui.homepage.HomeScreen
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetExplorerScreen
import com.imnotndesh.truehub.ui.homepage.details.DiskInfoScreen
import com.imnotndesh.truehub.ui.homepage.details.PerformanceScreen
import com.imnotndesh.truehub.ui.homepage.details.ShareInfoScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.InstanceConfigScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.AdvancedSystemSettingsEditScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.AdvancedSystemSettingsScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertClassesConfigScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServiceCreateScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServiceDetailScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice.AlertServicesListScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys.ApiKeyCreateScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys.ApiKeyDetailScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys.ApiKeyListScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.audit.AuditConfigScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.audit.AuditConfigViewModel
import com.imnotndesh.truehub.ui.homepage.instancesettings.audit.AuditLogsScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.audit.AuditLogsViewModel
import com.imnotndesh.truehub.ui.homepage.instancesettings.boot.BootEnvironmentDetailScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.boot.BootEnvironmentsScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.boot.BootPoolScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.boot.BootScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.GeneralSystemSettingsEditScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.appimages.AppImageManagementScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.appimages.DockerImageListScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.appimages.IxVolumeListScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.GeneralSystemSettingsScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.network.NetworkEditScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.network.NetworkScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.service.ServicesScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation.HardwareInformationScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation.SoftwareInformationScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation.SystemInformationScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation.TrueCommandScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation.TrueNasConnectScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.users.LocalAdminSetupScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.users.UserCreateScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.users.UserDetailScreen
import com.imnotndesh.truehub.ui.homepage.instancesettings.users.UserListScreen
import com.imnotndesh.truehub.ui.homepage.pools.PoolDataHolder
import com.imnotndesh.truehub.ui.homepage.pools.PoolDetailsScreen
import com.imnotndesh.truehub.ui.homepage.update.SystemUpdateScreen
import com.imnotndesh.truehub.ui.services.apps.AppsScreen
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppConfigPageValues
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppConfigScreen
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppDataHolder
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppAdvancedInfoScreen
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppInfoScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceAppDetailsScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceAppInstallScreen
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.MarketplaceScreen
import com.imnotndesh.truehub.ui.services.apps.details.rollback.RollbackVersionScreen
import com.imnotndesh.truehub.ui.services.apps.details.upgrade.UpgradeSummaryScreen
import com.imnotndesh.truehub.ui.services.containers.ContainersScreen
import com.imnotndesh.truehub.ui.services.containers.details.ContainerDataHolder
import com.imnotndesh.truehub.ui.services.containers.details.ContainerInfoScreen
import com.imnotndesh.truehub.ui.services.system.services.ServiceDetailScreen
import com.imnotndesh.truehub.ui.services.vm.VmsScreen
import com.imnotndesh.truehub.ui.services.vm.details.VmDataHolder
import com.imnotndesh.truehub.ui.services.vm.details.VmInfoScreen
import com.imnotndesh.truehub.ui.topbar.ExpressiveSearchAppBar
import com.imnotndesh.truehub.ui.topbar.SearchResultNavigation
import com.imnotndesh.truehub.ui.utils.AppCache


private data class NavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private fun destinationToNavItem(destination: NavbarDestination): NavItem? {
    return when (destination) {
        NavbarDestination.HOME -> NavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home)
        NavbarDestination.APPS -> NavItem(Screen.Apps, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps)
        NavbarDestination.CONTAINERS -> NavItem(Screen.Containers, "Containers", Icons.Filled.Inventory, Icons.Outlined.Inventory2)
        NavbarDestination.VMS -> NavItem(Screen.Vms, "VMs", Icons.Filled.Computer, Icons.Outlined.Computer)
        NavbarDestination.INSTANCE_SETTINGS -> NavItem(Screen.InstanceConfigScreen, "Instance", Icons.Filled.Tune, Icons.Outlined.Tune)
        NavbarDestination.UPDATES -> NavItem(Screen.SystemUpdateScreen, "Updates", Icons.Filled.SystemUpdateAlt, Icons.Outlined.SystemUpdateAlt)
        NavbarDestination.MARKETPLACE -> NavItem(Screen.Marketplace, "Marketplace", Icons.Filled.Storefront, Icons.Outlined.Storefront)
    }
}

@Composable
fun MainScreen(
    manager: TrueNASApiManager,
    rootNavController: NavController,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val personalization by PersonalizationManager.state.collectAsState()
    val isCompactNav = personalization.compactNav
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val navItems = remember(personalization.navbarDestinations) {
        personalization.navbarDestinations.mapNotNull { destination ->
            destinationToNavItem(destination)
        }
    }
    val navRoutes = remember(navItems) { navItems.map { it.screen.route }.toSet() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pendingNav by viewModel.pendingNavigation.collectAsState()
    LaunchedEffect(pendingNav) {
        val target = pendingNav
        when (target) {
            Screen.Apps.route,
            Screen.Marketplace.route,
            Screen.InstanceConfigScreen.route,
            Screen.SystemUpdateScreen.route -> navController.navigate(
                target
            ) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            else -> return@LaunchedEffect
        }
        viewModel.clearPendingNavigation()
    }

    var showSearch by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            Row {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight(),
                    header = {}
                ) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onNavClick(navController, item.screen.route) },
                            label = if (isCompactNav) null else {
                                { Text(item.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                            },
                            icon = {
                                Crossfade(targetState = selected, label = "iconFade") { isSelected ->
                                    Icon(
                                        if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        item.title,
                                        modifier = if (isCompactNav) Modifier.size(28.dp) else Modifier
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
                TrueHubNavGraph(
                    navController = navController,
                    manager = manager,
                    rootNavController = rootNavController,
                    onSearchClick = { showSearch = true },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            val hideOnScroll = rememberHideOnScrollState()
            Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .nestedScroll(hideOnScroll.connection)
                ) {
                    TrueHubNavGraph(
                        navController = navController,
                        manager = manager,
                        rootNavController = rootNavController,
                        onSearchClick = { showSearch = true },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (currentRoute in navRoutes) {
                        PillNavBar(
                            hidden = hideOnScroll.offset < -0.5f,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                navItems.forEach { item ->
                                    val selected = currentRoute == item.screen.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { onNavClick(navController, item.screen.route) },
                                        label = if (isCompactNav) null else {
                                            {
                                                Text(
                                                    item.title,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        icon = {
                                            Crossfade(targetState = selected, label = "iconFade") { isSelected ->
                                                Icon(
                                                    if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    item.title,
                                                    modifier = if (isCompactNav) Modifier.size(28.dp) else Modifier
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
                    }
                }
            }
        }

        // Full-screen search overlay
        if (showSearch) {
            ExpressiveSearchAppBar(
                title = "Search",
                manager = manager,
                startSearchActive = true,
                onCloseSearch = {
                    showSearch = false
                    // Navigate to Home tab on search close
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSearchResultClick = { result ->
                    SearchResultNavigation.navigate(result, navController)
                    showSearch = false
                }
            )
        }
    }
}

@Composable
private fun TrueHubNavGraph(
    navController: NavHostController,
    manager: TrueNASApiManager,
    rootNavController: NavController,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
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
        composable(Screen.Home.route) {
            HomeScreen(
                manager,
                onNavigateToSettings = { rootNavController.navigate(Screen.Settings.route) },
                onPoolClick = { pool: System.Pool ->
                    PoolDataHolder.currentPool = pool
                    navController.navigate(Screen.PoolDetails.route)
                },
                onNavigateToShareInfo = { shareType ->
                    AppDataHolder.selectedShareType = shareType
                    navController.navigate(Screen.ShareInfo.route)
                },
                onDisksClick = {
                    navController.navigate(Screen.DiskInfo.route)
                },
                onNavigateToPerformance = {metricType ->
                    AppDataHolder.initialMetricType = metricType
                    navController.navigate(Screen.Performance.route)
                },
                onUpdateClick = {
                    navController.navigate(Screen.SystemUpdateScreen.route)
                },
                onInstanceConfigClick = {
                    navController.navigate(Screen.InstanceConfigScreen.route)
                },
                onSystemInfoClick = {
                    navController.navigate(Screen.SystemInformationScreen.route)
                },
                onSearchClick = onSearchClick
            )
        }
        composable(Screen.LocalAdminSetupScreen.route) {
            LocalAdminSetupScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.InstanceConfigScreen.route) {
            InstanceConfigScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGeneralSystemSettings = {
                    navController.navigate(Screen.GeneralSystemSettingsScreen.route)
                },
                onNavigateToAdvancedSettings = {
                    navController.navigate(Screen.AdvancedSystemSettingsScreen.route)
                },
                onNavigateToServices = {
                    navController.navigate(Screen.ServicesScreen.route)
                },
                onNavigateToAlertSettings = {
                    navController.navigate(Screen.AlertServicesList.route)
                },
                onNavigateToUsers = {
                    navController.navigate(Screen.UserListScreen.route)
                },
                onNavigateToApiKeys = {
                    navController.navigate(Screen.ApiKeyListScreen.route)
                },
                onNavigateToAuditConfig = {
                    navController.navigate(Screen.AuditConfigScreen.route)
                },
                onNavigateToAuditLogs = {
                    navController.navigate(Screen.AuditLogsScreen.route)
                },
                onNavigateToNetwork = {
                    navController.navigate(Screen.NetworkScreen.route)
                },
                onNavigateToBoot = {
                    navController.navigate(Screen.BootScreen.route)
                },
                onNavigateToSystemInformation = {
                    navController.navigate(Screen.SystemInformationScreen.route)
                },
                onNavigateToTrueNasConnect = {
                    navController.navigate(Screen.TrueNasConnectScreen.route)
                },
                onNavigateToTrueCommand = {
                    navController.navigate(Screen.TrueCommandScreen.route)
                },
                onNavigateToAppImageManagement = {
                    navController.navigate(Screen.AppImageManagementScreen.route)
                }
            )
        }
        composable(Screen.AppImageManagementScreen.route) {
            AppImageManagementScreen(
                manager = manager,
                onNavigateToIxVolumes = {
                    navController.navigate(Screen.IxVolumeListScreen.route)
                },
                onNavigateToDockerImages = {
                    navController.navigate(Screen.DockerImageListScreen.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.IxVolumeListScreen.route) {
            IxVolumeListScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DockerImageListScreen.route) {
            DockerImageListScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SystemInformationScreen.route) {
            SystemInformationScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSoftwareInformation = {
                    navController.navigate(Screen.SoftwareInformationScreen.route)
                },
                onNavigateToHardwareInformation = {
                    navController.navigate(Screen.HardwareInformationScreen.route)
                }
            )
        }
        composable(Screen.SoftwareInformationScreen.route) {
            SoftwareInformationScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.HardwareInformationScreen.route) {
            HardwareInformationScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDisks = {
                    navController.navigate(Screen.DiskInfo.route)
                }
            )
        }
        composable(Screen.TrueNasConnectScreen.route) {
            TrueNasConnectScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TrueCommandScreen.route) {
            TrueCommandScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BootScreen.route) {
            BootScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBootPool = {
                    navController.navigate(Screen.BootPoolScreen.route)
                },
                onNavigateToBootEnvironments = {
                    navController.navigate(Screen.BootEnvironmentsScreen.route)
                }
            )
        }
        composable(Screen.BootPoolScreen.route) {
            BootPoolScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BootEnvironmentsScreen.route) {
            BootEnvironmentsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { envId ->
                    navController.navigate(Screen.BootEnvironmentDetailScreen.createRoute(envId))
                }
            )
        }
        composable(
            Screen.BootEnvironmentDetailScreen.route,
            arguments = listOf(navArgument("environmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val envId = backStackEntry.arguments?.getString("environmentId").orEmpty()
            BootEnvironmentDetailScreen(
                manager = manager,
                environmentId = envId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NetworkScreen.route) {
            NetworkScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.NetworkEditScreen.route)
                }
            )
        }
        composable(Screen.NetworkEditScreen.route) {
            NetworkEditScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AuditConfigScreen.route) {
            val vm: AuditConfigViewModel = viewModel(factory = AuditConfigViewModel.AuditConfigViewModelFactory(manager))
            AuditConfigScreen(
                manager = manager,
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AuditLogsScreen.route) {
            val vm: AuditLogsViewModel = viewModel(factory = AuditLogsViewModel.AuditLogsViewModelFactory(manager))
            AuditLogsScreen(
                manager = manager,
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.UserListScreen.route) {
            UserListScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserDetail = { userId ->
                    navController.navigate(Screen.UserDetailScreen.createRoute(userId))
                },
                onNavigateToCreateUser = {
                    navController.navigate(Screen.UserCreateScreen.route)
                }
            )
        }
        composable(Screen.ApiKeyListScreen.route) {
            ApiKeyListScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { keyId ->
                    navController.navigate(Screen.ApiKeyDetailScreen.createRoute(keyId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.ApiKeyCreateScreen.route)
                }
            )
        }

        composable(
            route = Screen.ApiKeyDetailScreen.route,
            arguments = listOf(navArgument("keyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val keyId = backStackEntry.arguments?.getInt("keyId") ?: 0
            ApiKeyDetailScreen(
                keyId = keyId,
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ApiKeyCreateScreen.route) {
            ApiKeyCreateScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GeneralSystemSettingsScreen.route) {
            GeneralSystemSettingsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.GeneralSystemSettingsEditScreen.route)
                }
            )
        }

        composable(Screen.GeneralSystemSettingsEditScreen.route) {
            GeneralSystemSettingsEditScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onCheckinNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdvancedSystemSettingsScreen.route) {
            AdvancedSystemSettingsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.AdvancedSystemSettingsEditScreen.route)
                }
            )
        }

        composable(Screen.AdvancedSystemSettingsEditScreen.route) {
            AdvancedSystemSettingsEditScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UserDetailScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserDetailScreen(
                userId = userId,
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UserCreateScreen.route) {
            UserCreateScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ServicesScreen.route) {
            ServicesScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToServiceDetail = { service ->
                    AppDataHolder.selectedService = service
                    navController.navigate(Screen.ServicesDetailScreen.route)
                }
            )
        }
        composable(Screen.ServicesDetailScreen.route) {
            val service = AppDataHolder.selectedService
            if (service != null) {
                ServiceDetailScreen(
                    service = service,
                    manager = manager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.SystemUpdateScreen.route) {
            val updateVersions by AppCache.cachedUpdateVersions.collectAsState()
            val systemInfo by AppCache.cachedSystemInfo.collectAsState()

            SystemUpdateScreen(
                manager = manager,
                versions = updateVersions,
                currentVersion = systemInfo?.version,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AlertServicesList.route) {
            AlertServicesListScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { service ->
                    navController.navigate(Screen.AlertServiceDetail.createRoute(service.id))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.AlertServiceCreate.route)
                },
                onNavigateToClassesConfig = {
                    navController.navigate(Screen.AlertClassesConfig.route)
                }
            )
        }
        composable(
            route = Screen.AlertServiceDetail.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
            AlertServiceDetailScreen(
                serviceId = serviceId,
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AlertServiceCreate.route) {
            AlertServiceCreateScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AlertClassesConfig.route) {
            AlertClassesConfigScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DiskInfo.route){
            val disks = AppDataHolder.disks
            DiskInfoScreen(
                disks,
                manager
            ) {
                navController.popBackStack()
            }
        }
        composable(Screen.AppConfigScreen.route){
            val currAppValues = AppDataHolder.selectedAppValues
            AppConfigScreen(
                manager,
                currAppValues,
                {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ShareInfo.route) {
            val shareType = AppDataHolder.selectedShareType
            if (shareType != null) {
                ShareInfoScreen(
                    shareType = shareType,
                    manager = manager,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Performance.route) {
            PerformanceScreen(
                cpuData = AppDataHolder.cpuData,
                memoryData = AppDataHolder.memoryData,
                temperatureData = AppDataHolder.temperatureData,
                initialMetricType = AppDataHolder.initialMetricType,
                isLoading = false,
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onRefresh = {}
            )
        }

        composable(Screen.Apps.route) {
            AppsScreen(
                manager = manager,
                onNavigateToAppInfo = { app ->
                    AppDataHolder.selectedApp = app
                    navController.navigate(Screen.AppDetailsScreen.route)
                },
                onOpenAdvanced = { appId ->
                    navController.navigate(Screen.AppAdvancedInfoScreen.createRoute(appId))
                },
                onNavigateToUpgrade = { appName ->
                    navController.navigate(Screen.AppUpgrade.createRoute(appName)) },
                onNavigateToRollback = { navController.navigate(Screen.RollbackVersion.createRoute(it)) },
                onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                onSearchClick = onSearchClick
            )
        }

        composable(Screen.PoolDetails.route) {
            PoolDetailsScreen(
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFiles = { poolName: String -> navController.navigate(Screen.DatasetExplorer.createRoute(poolName)) }
            )
        }
        composable(
            route = Screen.RollbackVersion.route,
            arguments = listOf(navArgument("appName") { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val context = LocalContext.current
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by appsViewModel.uiState.collectAsState()

            LaunchedEffect(appName) {
                appsViewModel.loadRollbackVersions(appName)
            }

            RollbackVersionScreen(
                appName = appName,
                versions = uiState.rollbackVersions,
                isLoadingVersions = uiState.isLoadingRollbackVersions,
                fetchError = uiState.error,
                onConfirmRollback = { targetVersion, rollbackSnapshot ->
                    appsViewModel.rollbackApp(context, appName, targetVersion, rollbackSnapshot)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AppDetailsScreen.route) {
            val app = AppDataHolder.selectedApp
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            LaunchedEffect(Unit) {
                if (appsViewModel.uiState.value.marketplaceApps.isEmpty()) appsViewModel.loadMarketplaceApps()
            }
            AppInfoScreen(
                app = app!!,
                manager = manager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMarketplaceCategory = { categoryName -> navController.navigate(Screen.MarketplaceCategory.createRoute(categoryName)) },
                onNavigateToMarketplaceAppDetails = { appName ->
                    val matchedAvailableItem = appsViewModel.uiState.value.marketplaceApps.find { it.name == appName }
                    if (matchedAvailableItem != null) {
                        AppDataHolder.selectedMarketplaceApp = matchedAvailableItem
                        navController.navigate("marketplace_app_details")
                    } else {
                        navController.navigate("marketplace?category=")
                    }
                },
                onDeleteSuccess = { navController.navigate(Screen.Apps.route) },
                onEditClick = {appName , appTrain->
                    AppDataHolder.selectedAppValues = AppConfigPageValues(
                        appName,
                        appTrain
                    )
                    navController.navigate(Screen.AppConfigScreen.route)
                },
                onOpenAdvanced = { appId ->
                    navController.navigate(Screen.AppAdvancedInfoScreen.createRoute(appId))
                }
            )
        }

        composable(
            route = Screen.AppAdvancedInfoScreen.route,
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId").orEmpty()
            AppAdvancedInfoScreen(
                manager = manager,
                appId = appId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MarketplaceCategory.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType; defaultValue = ""; nullable = true })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")?.takeIf { it.isNotBlank() }
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            MarketplaceScreen(manager = manager, initialCategory = category, onNavigateBack = { navController.popBackStack() }, onMarketplaceApplicationClicked = { app -> AppDataHolder.selectedMarketplaceApp = app; navController.navigate("marketplace_app_details") }, onInstallApplication = { app -> appsViewModel.loadCatalogAppDetails(app.name, app.train); navController.navigate(Screen.CatalogInstall.createRoute(app.name, app.train)) })
        }

        composable(Screen.Marketplace.route) {
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            MarketplaceScreen(manager = manager, onNavigateBack = { navController.popBackStack() }, onMarketplaceApplicationClicked = { app -> AppDataHolder.selectedMarketplaceApp = app; navController.navigate("marketplace_app_details") }, onInstallApplication = { app -> appsViewModel.loadCatalogAppDetails(app.name, app.train); navController.navigate(Screen.CatalogInstall.createRoute(app.name, app.train)) })
        }

        composable(
            route = Screen.AppUpgrade.route,
            arguments = listOf(navArgument("appName")
            { type = NavType.StringType })
        ) { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val context = LocalContext.current
            val viewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(appName) { viewModel.clearUpgradeSummary(); viewModel.loadUpgradeSummary(appName) }
            val currentApp = uiState.apps.find { it.name == appName }
            val currentVersion = currentApp?.version ?: "Unknown"
            val currentHumanVersion = currentApp?.metadata?.appVersion
            val summary = uiState.upgradeSummaryResult
            val isLoading = (uiState.isLoadingUpgradeSummaryForApp == appName) && (summary == null)
            if (isLoading) {
                LoadingScreen("Checking upgrades...")
            } else if (summary != null) {
                UpgradeSummaryScreen(
                    appName = appName,
                    summary = summary,
                    currentVersion = currentVersion,
                    currentHumanVersion = currentHumanVersion,
                    manager = manager,
                    canUpgrade = currentApp?.canUpgradeNow() ?: false,
                    appState = currentApp?.state ?: "",
                    onConfirmUpgrade = { version, backup ->
                        viewModel.upgradeApp(appName, context, version, backup)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else if (uiState.error != null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Screen.MarketplaceAppDetails.route) {
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val app = AppDataHolder.selectedMarketplaceApp
            if (app != null) {
                MarketplaceAppDetailsScreen(app = app, onNavigateBack = { navController.popBackStack() }, manager = manager, onInstallClick = { appName, train -> appsViewModel.loadCatalogAppDetails(appName, train); navController.navigate(Screen.CatalogInstall.createRoute(appName,train)) })
            }
        }

        composable(route = Screen.CatalogInstall.route, arguments = listOf(navArgument("appName") { type = NavType.StringType }, navArgument("train") { type = NavType.StringType })) { backStackEntry ->
            val appsViewModel: AppsScreenViewModel = viewModel(factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager))
            val appName = backStackEntry.arguments?.getString("appName") ?: ""
            val train = backStackEntry.arguments?.getString("train") ?: ""
            MarketplaceAppInstallScreen(appName = appName, train = train, viewModel = appsViewModel, manager = manager, onBack = { navController.popBackStack() }, onInstallSuccess = { navController.popBackStack() })
        }

        composable(Screen.Containers.route) {
            ContainersScreen(
                manager = manager,
                onNavigateToContainerInfo = { container -> ContainerDataHolder.selectedContainer = container; navController.navigate("container_info") },
                onSearchClick = onSearchClick
            )
        }

        composable(Screen.ContainerInfo.route) {
            val container = ContainerDataHolder.selectedContainer
            ContainerInfoScreen(manager = manager, container = container!!, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Vms.route) {
            VmsScreen(
                manager = manager,
                onNavigateToVmInfo = { vmInfo -> VmDataHolder.selectedVm = vmInfo; navController.navigate("vm_details") },
                onSearchClick = onSearchClick
            )
        }

        composable(Screen.VmDetails.route) {
            val vm = VmDataHolder.selectedVm
            VmInfoScreen(vm = vm!!, manager = manager, onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.DatasetExplorer.route, arguments = listOf(navArgument("poolName") { type = NavType.StringType })) { backStackEntry ->
            val poolName = backStackEntry.arguments?.getString("poolName") ?: ""
            DatasetExplorerScreen(manager = manager, onNavigateBack = { navController.popBackStack() }, poolName = poolName)
        }
    }
}

private fun onNavClick(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}