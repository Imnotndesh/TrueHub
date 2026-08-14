package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.ui.Screen

/**
 * Catalog of destinations that can appear on the bottom navigation bar.
 *
 * Home is required and cannot be hidden or reordered away from first position.
 * All other destinations are optional, reorderable, and may be added/removed
 * independently per user.
 *
 * To add a future destination, add a new enum entry (with its route and labels)
 * and include it in [PersonalizationManager.availableDestinations].
 */
enum class NavbarDestination(
    val title: String,
    val route: String,
    val isRequired: Boolean = false
) {
    HOME("Home", Screen.Home.route, isRequired = true),
    APPS("Apps", Screen.Apps.route),
    CONTAINERS("Containers", Screen.Containers.route),
    VMS("VMs", Screen.Vms.route),
    SETTINGS("Settings", Screen.Settings.route),
    INSTANCE_SETTINGS("Instance Settings", Screen.InstanceConfigScreen.route),
    UPDATES("Updates", Screen.SystemUpdateScreen.route),
    MARKETPLACE("Marketplace", Screen.Marketplace.route);

    companion object {
        val required get() = entries.filter { it.isRequired }
        val optional get() = entries.filter { !it.isRequired }
    }
}
