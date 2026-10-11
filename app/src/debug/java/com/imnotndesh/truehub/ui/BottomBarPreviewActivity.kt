package com.imnotndesh.truehub.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.ui.components.FloatingNavMetrics
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme

/**
 * Debug-only harness that renders the floating bottom bar with sample data so it can be
 * exercised on an emulator without a reachable TrueNAS server or a login session.
 */
class BottomBarPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrueHubAppTheme {
                BottomBarPreviewScreen()
            }
        }
    }
}

private val sampleItems = listOf(
    NavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.Apps, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
    NavItem(Screen.Containers, "Containers", Icons.Filled.Inventory, Icons.Outlined.Inventory2),
    NavItem(Screen.Vms, "VMs", Icons.Filled.Computer, Icons.Outlined.Computer),
    NavItem(Screen.InstanceConfigScreen, "Instance", Icons.Filled.Tune, Icons.Outlined.Tune),
    NavItem(Screen.SystemUpdateScreen, "Updates", Icons.Filled.SystemUpdateAlt, Icons.Outlined.SystemUpdateAlt),
    NavItem(Screen.Marketplace, "Marketplace", Icons.Filled.Storefront, Icons.Outlined.Storefront)
)

@Composable
private fun BottomBarPreviewScreen() {
    var selectedRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }
    var compact by rememberSaveable { mutableStateOf(false) }
    var showMax by rememberSaveable { mutableStateOf(false) }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }

    val items = if (showMax) sampleItems.take(PersonalizationManager.MAX_NAVBAR_ITEMS) else sampleItems.take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bottom bar preview", style = MaterialTheme.typography.headlineSmall)
            Text(
                "No server required. Toggle the states below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ToggleRow("Compact labels", compact) { compact = it }
            ToggleRow("${PersonalizationManager.MAX_NAVBAR_ITEMS} destinations (max)", showMax) { showMax = it }
            ToggleRow("Search pill", searchExpanded) { searchExpanded = it }
            Text("Selected: $selectedRoute", style = MaterialTheme.typography.bodySmall)
        }

        FloatingBottomBar(
            items = items,
            currentRoute = selectedRoute,
            isCompactNav = compact,
            searchExpanded = searchExpanded,
            onNavClick = { selectedRoute = it },
            onSearchClick = { searchExpanded = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = FloatingNavMetrics.barMargin, vertical = 12.dp)
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(200.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(name = "Bottom bar - 3 items", showBackground = true, widthDp = 420, heightDp = 200)
@Composable
private fun BottomBarThreePreview() {
    BottomBarPreviewContent(items = sampleItems.take(3), selected = Screen.Home.route)
}

@Preview(name = "Bottom bar - 4 items (max)", showBackground = true, widthDp = 420, heightDp = 200)
@Composable
private fun BottomBarFourPreview() {
    BottomBarPreviewContent(items = sampleItems.take(4), selected = Screen.Containers.route)
}

@Composable
private fun BottomBarPreviewContent(items: List<NavItem>, selected: String) {
    TrueHubAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FloatingBottomBar(
                items = items,
                currentRoute = selected,
                isCompactNav = false,
                searchExpanded = false,
                onNavClick = {},
                onSearchClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = FloatingNavMetrics.barMargin, vertical = 12.dp)
            )
        }
    }
}
