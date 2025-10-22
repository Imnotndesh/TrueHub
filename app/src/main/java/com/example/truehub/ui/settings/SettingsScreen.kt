package com.example.truehub.ui.settings

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.ui.settings.sheets.ChangePasswordBottomSheet
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    manager: TrueNASApiManager?,
    onNavigateToLogin: () -> Unit = {},
    onDummyAction: (String) -> Unit = {}
) {
    val viewModel : SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModel.SettingsViewModelFactory(manager, LocalContext.current.applicationContext as Application)
    )
    val scope = rememberCoroutineScope()
    var isAutoLoginChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAutoLoginChecked = viewModel.getUseAutoLogin() ?: false
    }

    val uiState by viewModel.uiState.collectAsState()
    var showPassChangeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.isAutoLoginSaving, uiState.showAutoLoginDialog) {
        if (!uiState.isLoading && !uiState.isAutoLoginSaving && !uiState.showAutoLoginDialog) {
            isAutoLoginChecked = viewModel.getUseAutoLogin() ?: false
        }
    }

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
                        name = "Auto Login",
                        description = "Automatically log in with saved credentials.",
                        onClick = {},
                        isLoading = uiState.isLoading,
                        onToggle = { newValue ->
                            viewModel.handleEvent(SettingsEvent.ToggleAutoLogin(newValue))
                        },
                        isChecked = isAutoLoginChecked,
                    ),
                    SettingItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
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
            if (uiState.showAutoLoginDialog) {
                AutoLoginConfigDialog(
                    dialogType = uiState.autoLoginDialogType,
                    isSaving = uiState.isAutoLoginSaving,
                    onConfirmToggle = { apiKey, username, userPass ->
                        viewModel.handleEvent(
                            SettingsEvent.SaveAutoLoginCredentials(
                                apiKey = apiKey,
                                username = username,
                                userPass = userPass
                            )
                        )
                    },
                    onCancel = {
                        viewModel.handleEvent(SettingsEvent.DismissAutoLoginDialog)
                    },
                    onClearAutoLogin = {
                        scope.launch {
                            viewModel.clearUseAutoLogin()
                            viewModel.handleEvent(SettingsEvent.DismissAutoLoginDialog)
                        }
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
    val showSwitch = item.onToggle != null && item.isChecked != null

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
@Composable
fun AutoLoginConfigDialog(
    dialogType: AutoLoginDialogType,
    isSaving: Boolean,
    onConfirmToggle: (apiKey: String?, username: String?, userPass: String?) -> Unit,
    onCancel: () -> Unit,
    onClearAutoLogin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = when (dialogType) {
                    AutoLoginDialogType.OFF_WARNING -> "Disable Auto Login?"
                    AutoLoginDialogType.PROMPT_API_KEY -> "Save API Key for Auto Login"
                    AutoLoginDialogType.PROMPT_PASSWORD -> "Save Credentials for Auto Login"
                }
            )
        },
        text = {
            AutoLoginDialogContent(
                dialogType = dialogType,
                isSaving = isSaving,
                onConfirmToggle = onConfirmToggle,
                onClearAutoLogin = onClearAutoLogin,
                onCancel = onCancel
            )
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun AutoLoginDialogContent(
    dialogType: AutoLoginDialogType,
    isSaving: Boolean,
    onConfirmToggle: (apiKey: String?, username: String?, userPass: String?) -> Unit,
    onClearAutoLogin: () -> Unit,
    onCancel : () -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        when (dialogType) {
            AutoLoginDialogType.OFF_WARNING -> {
                Text("Your login credentials (username/password or API key) will remain persistent in the app until you explicitly log out. They will be cleared upon successful logout.")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onClearAutoLogin) { Text("Disable") }
                }
            }
            AutoLoginDialogType.PROMPT_API_KEY -> {
                Text("Please provide your API key to enable auto login.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )
                // Buttons are now exclusively here
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmToggle(apiKey, null, null) },
                        enabled = !isSaving && apiKey.isNotBlank()
                    ) {
                        Text(if (isSaving) "Saving..." else "Submit")
                    }
                }
            }
            AutoLoginDialogType.PROMPT_PASSWORD -> {
                Text("Please provide your username and password to enable auto login.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )
                // Buttons are now exclusively here
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmToggle(null, username, password) },
                        enabled = !isSaving && username.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(if (isSaving) "Saving..." else "Submit")
                    }
                }
            }
        }
    }
}

data class SettingItem(
    val icon: ImageVector,
    val name: String,
    val description: String,
    val onClick: () -> Unit,
    val isLoading: Boolean = false,
    val onToggle: ((Boolean) -> Unit)? = null,
    val isChecked: Boolean? = null
)
