package com.example.truehub.ui.settings

import android.app.Application
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.ui.components.LoadingScreen
import com.example.truehub.ui.settings.sheets.ChangePasswordBottomSheet

@Composable
fun SettingsScreen(
    manager: TrueNASApiManager?,
    onNavigateToLogin: () -> Unit = {},
    onDummyAction: (String) -> Unit = {}
) {
    val viewModel : SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModel.SettingsViewModelFactory(manager, LocalContext.current.applicationContext as Application)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showPassChangeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onNavigateToLogin()
            viewModel.handleEvent(SettingsEvent.ClearLogoutSuccess)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(16.dp)
        ) {
            // Header Section
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Manage your account and preferences",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

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
                        onClick = { showPassChangeDialog = true}
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
                        onClick = { viewModel.handleEvent(SettingsEvent.Logout) },
                        isLoading = uiState.isLoggingOut
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (showPassChangeDialog){
                ChangePasswordBottomSheet(
                    onDismiss = { showPassChangeDialog = false },
                    onSubmit = {oldPassword, newPassword ->
                        viewModel.handleEvent(
                            SettingsEvent.ChangePassword(
                                oldPassword,
                                newPassword
                            )
                        )
                        showPassChangeDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
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

        // Settings Cards
        items.forEach { item ->
            SettingCard(item)
        }
    }
}

@Composable
private fun SettingCard(item: SettingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !item.isLoading) { item.onClick() }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
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

            // Text Content
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

            // Chevron Icon (hidden when loading)
            if (!item.isLoading) {
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

data class SettingItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val name: String,
    val description: String,
    val onClick: () -> Unit,
    val isLoading: Boolean = false
)