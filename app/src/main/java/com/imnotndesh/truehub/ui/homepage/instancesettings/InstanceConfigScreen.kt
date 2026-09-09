package com.imnotndesh.truehub.ui.homepage.instancesettings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

/**
 * Lists TrueNAS-instance-specific settings and configuration, separate from
 * the app-level [SettingsScreen]. Mirrors its visual language (same header,
 * same SettingsSection/SettingCard pattern) but only covers server-side
 * configuration areas.
 *
 * All onClick callbacks are currently empty placeholders — wire each one to
 * its real destination screen as it's built.
 */
@Composable
fun InstanceConfigScreen(
    manager: TrueNASApiManager?,
    onNavigateBack: () -> Unit = {},
    onNavigateToGeneralSettings: () -> Unit = {},
    onNavigateToAdvancedSettings: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToBoot: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToAlertSettings: () -> Unit = {},
    onNavigateToApiKeys: () -> Unit = {},
    onNavigateToGeneralSystemSettings: () -> Unit = {},
    onNavigateToAuditConfig: () -> Unit = {},
    onNavigateToAuditLogs: () -> Unit = {},
    onNavigateToNetwork: () -> Unit = {},
    onNavigateToSystemInformation: () -> Unit = {},
    onNavigateToTrueNasConnect: () -> Unit = {},
    onNavigateToTrueCommand: () -> Unit = {},
    onNavigateToAppImageManagement: () -> Unit = {},

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // True OLED black in black mode — solid canvas instead of a grey-ish gradient.
            .background(MaterialTheme.colorScheme.background)
    ) {
        UnifiedScreenHeader(
            title = "Configuration",
            subtitle = "Current TrueNAS instance Settings",
            isLoading = false,
            isRefreshing = false,
            error = null,
            onDismissError = {},
            manager = manager!!,
            onBackPressed = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            InstanceConfigSection(
                title = "Core configuration",
                items = listOf(
                    InstanceConfigItem(
                        icon = Icons.Default.Tune,
                        name = "General settings",
                        description = "Hostname, timezone, and base options",
                        onClick = onNavigateToGeneralSystemSettings
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Bolt,
                        name = "Advanced settings",
                        description = "Sysctl, tunables, and developer options",
                        onClick = onNavigateToAdvancedSettings
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Wifi,
                        name = "Network",
                        description = "Interfaces, routes, DNS, and configuration",
                        onClick = onNavigateToNetwork
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.PowerSettingsNew,
                        name = "Boot",
                        description = "Boot environments and boot pool",
                        onClick = onNavigateToBoot
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Dns,
                        name = "Services",
                        description = "Manage and configure running services",
                        onClick = onNavigateToServices
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.People,
                        name = "Users",
                        description = "Manage local user accounts",
                        onClick = onNavigateToUsers
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Key,
                        name = "API Keys",
                        description = "Manage API key access",
                        onClick = onNavigateToApiKeys
                    ),

                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InstanceConfigSection(
                title = "App Management",
                items = listOf(
                    InstanceConfigItem(
                        icon = Icons.Default.Image,
                        name = "App Image Management",
                        description = "View, pull, and delete Docker images",
                        onClick = onNavigateToAppImageManagement
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InstanceConfigSection(
                title = "Monitoring & compliance",
                items = listOf(
                    InstanceConfigItem(
                        icon = Icons.Default.NotificationsActive,
                        name = "Alert settings",
                        description = "Notification levels and delivery",
                        onClick = onNavigateToAlertSettings
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.FactCheck,
                        name = "Audit config",
                        description = "Audit logging and retention settings",
                        onClick = onNavigateToAuditConfig
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.FactCheck,
                        name = "Audit logs",
                        description = "View and export audit entries",
                        onClick = onNavigateToAuditLogs
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InstanceConfigSection(
                title = "System information",
                items = listOf(
                    InstanceConfigItem(
                        icon = Icons.Default.Cloud,
                        name = "TrueNAS Connect",
                        description = "Configure the TrueNAS cloud connection",
                        onClick = onNavigateToTrueNasConnect
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Insights,
                        name = "TrueCommand",
                        description = "Centralized instance management",
                        onClick = onNavigateToTrueCommand
                    ),
                    InstanceConfigItem(
                        icon = Icons.Default.Info,
                        name = "System information",
                        description = "Version, identifiers, state and features",
                        onClick = onNavigateToSystemInformation
                    )
                )
            )
        }
    }
}

@Composable
private fun InstanceConfigSection(
    title: String,
    items: List<InstanceConfigItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items.forEach { item ->
            InstanceConfigCard(item)
        }
    }
}

@Composable
private fun InstanceConfigCard(item: InstanceConfigItem) {
    val showSwitch = item.onToggle != null && item.isChecked != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !item.isLoading && !showSwitch) { item.onClick() }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (item.isLoading) "Processing..." else item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!item.isLoading) {
                if (showSwitch) {
                    Switch(
                        checked = item.isChecked,
                        onCheckedChange = item.onToggle,
                        modifier = Modifier.clickable(enabled = false) { }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class InstanceConfigItem(
    val icon: ImageVector,
    val name: String,
    val description: String,
    val onClick: () -> Unit,
    val isLoading: Boolean = false,
    val onToggle: ((Boolean) -> Unit)? = null,
    val isChecked: Boolean? = null
)