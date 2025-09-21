package com.example.truehub.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onDummyAction: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section: Account
        SettingsSection(
            title = "Account",
            items = listOf(
                SettingItem(
                    icon = Icons.Default.AccountCircle,
                    name = "Profile",
                    description = "Manage your profile information",
                    onClick = { onDummyAction("Profile") }
                ),
                SettingItem(
                    icon = Icons.Default.Security,
                    name = "Password",
                    description = "Change your password",
                    onClick = { onDummyAction("Password") }
                ),
                SettingItem(
                    icon = Icons.Default.VerifiedUser,
                    name = "Two-Factor Authentication",
                    description = "Enable or disable 2FA",
                    onClick = { onDummyAction("2FA") }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Application
        SettingsSection(
            title = "Application",
            items = listOf(
                SettingItem(
                    icon = Icons.Default.Apps,
                    name = "Theme",
                    description = "Choose light or dark mode",
                    onClick = { onDummyAction("Theme") }
                ),
                SettingItem(
                    icon = Icons.Default.PrivacyTip,
                    name = "Privacy",
                    description = "Control data sharing & permissions",
                    onClick = { onDummyAction("Privacy") }
                ),
                SettingItem(
                    icon = Icons.Default.Settings,
                    name = "Advanced",
                    description = "Developer and experimental features",
                    onClick = { onDummyAction("Advanced") }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Session
        SettingsSection(
            title = "Session",
            items = listOf(
                SettingItem(
                    icon = Icons.Default.Timer,
                    name = "Session Timeout",
                    description = "Configure auto logout time",
                    onClick = { onDummyAction("Session Timeout") }
                ),
                SettingItem(
                    icon = Icons.Default.Logout,
                    name = "Log Out",
                    description = "Sign out of your account",
                    onClick = { onDummyAction("Logout") }
                )
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            items.forEachIndexed { index, item ->
                SettingRow(item)
                if (index < items.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Texts
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Caret
        Text(
            text = ">",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SettingItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val name: String,
    val description: String,
    val onClick: () -> Unit
)
