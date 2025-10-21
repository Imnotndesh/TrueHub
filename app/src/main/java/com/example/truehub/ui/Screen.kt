package com.example.truehub.ui

sealed class Screen(val route:String, val title:String) {
    object Home : Screen("home", "Home")
    object Apps : Screen("apps","Apps")
    object Containers : Screen ("containers", "Containers")
    object Vms : Screen("vms","VMs")
    object Profile : Screen("profile","Profile")
    object Login : Screen("login","Login")
    object Main: Screen("main","Main")
    object Settings : Screen("settings","Settings")
}