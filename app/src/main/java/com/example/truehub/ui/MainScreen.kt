package com.example.truehub.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.ui.profile.ProfileScreen
import com.example.truehub.ui.services.ServicesScreen

@Composable
fun MainScreen(manager: TrueNASApiManager,rootNavController: NavController) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Services, Screen.Profile)

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
                            Screen.Services -> Icons.Filled.HomeRepairService
                            Screen.Profile -> Icons.Filled.Person
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
            composable(Screen.Home.route) { PageContent("Home Page") }
            composable(Screen.Services.route) { ServicesScreen(manager) }
            composable(Screen.Profile.route) { ProfileScreen(manager) {
                rootNavController.navigate(Screen.Settings.route)
            }
            }
        }
    }
}

@Composable
fun PageContent(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium)
    }
}
