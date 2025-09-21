package com.example.truehub.ui

sealed class Screen(val route:String, val title:String) {
    object Home : Screen("home", "Home")
    object Services : Screen("services","Services")
    object Profile : Screen("profile","Profile")
    object Login : Screen("login","Login")
    object Setup : Screen("setup","Setup")
    object Main: Screen("main","Main")
    object Settings : Screen("settings","Settings")
}