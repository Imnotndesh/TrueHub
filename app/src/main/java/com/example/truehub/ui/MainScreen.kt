package com.example.truehub.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.ui.homepage.HomeScreen
import com.example.truehub.ui.services.apps.AppsScreen
import com.example.truehub.ui.services.containers.ContainersScreen
import com.example.truehub.ui.services.vm.VmsScreen

@Composable
fun MainScreen(manager: TrueNASApiManager,rootNavController: NavController) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Apps, Screen.Containers, Screen.Vms)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        label = { Text(screen.title) },
                        icon = { Icon(when (screen){
                            Screen.Home -> Icons.Filled.Home
                            Screen.Apps -> Icons.Filled.Apps
                            Screen.Vms -> Icons.Filled.Computer
                            Screen.Containers -> Icons.Filled.Inventory
                            else -> Icons.Filled.Home
                        }, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(innerPadding)
        ) {

            /**
             * Link to home screen
             * @see HomeScreen
             */
            composable(Screen.Home.route) { HomeScreen(manager,
                // TODO: DEPRECATE THESE LINKS OR FIND A MORE CLEVER WAY TO HANDLE THEM AND COMPLETELY REMOVE PROFILE
                onNavigateToSettings = {rootNavController.navigate(Screen.Settings.route)})
            }

            /**
             * Link to apps screen
             * @see AppsScreen
             */
            composable(Screen.Apps.route) {
                AppsScreen(manager)
            }

            /**
             * Link to Containers Screen
             * @see ContainersScreen
             */
            composable(Screen.Containers.route) {
                ContainersScreen(manager)
            }

            /**
             * Link to Vms Screen
             * @see VmsScreen
             */
            composable(Screen.Vms.route) {
                VmsScreen(manager)
            }
        }
    }
}

