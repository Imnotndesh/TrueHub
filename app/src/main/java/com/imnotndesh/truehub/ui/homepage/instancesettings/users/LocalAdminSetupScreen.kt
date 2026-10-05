package com.imnotndesh.truehub.ui.homepage.instancesettings.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.adaptive.AdaptiveFormScreen
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.ProvideTrueHubWindowForPreview
import com.imnotndesh.truehub.ui.adaptive.WidthClass
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.theme.TrueHubAppTheme

@Composable
fun LocalAdminSetupScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: UserSettingsViewModel = hiltViewModel()
    val uiState by viewModel.createState.collectAsState()

    var username by remember { mutableStateOf("truenas_admin") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val passwordsMatch = password == confirmPassword && password.isNotBlank()

    LaunchedEffect(uiState.setupAdminResult) {
        if (uiState.setupAdminResult == true) onNavigateBack()
    }

    val window = LocalTrueHubWindow.current

    val onSetup: () -> Unit = {
        viewModel.setupLocalAdministrator(username, password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        if (window.isCompactWidth) {
            // Compact keeps the original single-column layout, unchanged.
            LocalAdminSetupHeader(
                manager = manager,
                error = uiState.error,
                onDismissError = { viewModel.clearSetupAdminResult() },
                onBack = onNavigateBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LocalAdminInfoCard()
                LocalAdminFormCard(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                )
                LocalAdminActionsRow(
                    isCreating = uiState.isCreating,
                    passwordsMatch = passwordsMatch,
                    onCancel = onNavigateBack,
                    onSetup = onSetup,
                )
            }
        } else {
            // Medium and up: form column capped at 640dp and centered.
            AdaptiveFormScreen(
                header = {
                    LocalAdminSetupHeader(
                        manager = manager,
                        error = uiState.error,
                        onDismissError = { viewModel.clearSetupAdminResult() },
                        onBack = onNavigateBack,
                    )
                },
            ) {
                LocalAdminInfoCard()
                LocalAdminFormCard(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                )
                LocalAdminActionsRow(
                    isCreating = uiState.isCreating,
                    passwordsMatch = passwordsMatch,
                    onCancel = onNavigateBack,
                    onSetup = onSetup,
                )
            }
        }
    }
}

@Composable
private fun LocalAdminSetupHeader(
    manager: TrueNASApiManager,
    error: String?,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
) {
    UnifiedScreenHeader(
        title = "Setup Local Administrator",
        subtitle = "Configure initial admin account",
        isLoading = false,
        isRefreshing = false,
        error = error,
        onDismissError = onDismissError,
        manager = manager,
        onBackPressed = onBack
    )
}

@Composable
private fun LocalAdminInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    "About Local Administrator Setup",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Use this to set up or reset the local administrator account. " +
                            "This is typically done on first login when no admin password is set. " +
                            "Choose 'root' or 'truenas_admin' as the username.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LocalAdminFormCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Administrator Account",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Passwords do not match")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LocalAdminActionsRow(
    isCreating: Boolean,
    passwordsMatch: Boolean,
    onCancel: () -> Unit,
    onSetup: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) { Text("Cancel") }
        Button(
            onClick = onSetup,
            enabled = !isCreating && passwordsMatch,
            modifier = Modifier.weight(1f)
        ) {
            if (isCreating) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("Setup Admin")
        }
    }
}

@Preview(name = "Local admin setup — empty (Compact)", widthDp = 412, heightDp = 900)
@Composable
private fun LocalAdminSetupEmptyCompactPreview() {
    TrueHubAppTheme {
        ProvideTrueHubWindowForPreview(width = WidthClass.Compact) {
            LocalAdminSetupPreviewContent(mismatch = false)
        }
    }
}

@Preview(name = "Local admin setup — mismatch (Compact)", widthDp = 412, heightDp = 900)
@Composable
private fun LocalAdminSetupMismatchCompactPreview() {
    TrueHubAppTheme {
        ProvideTrueHubWindowForPreview(width = WidthClass.Compact) {
            LocalAdminSetupPreviewContent(mismatch = true)
        }
    }
}

@Preview(name = "Local admin setup — empty (Expanded)", widthDp = 1280, heightDp = 900)
@Composable
private fun LocalAdminSetupEmptyExpandedPreview() {
    TrueHubAppTheme {
        ProvideTrueHubWindowForPreview(width = WidthClass.Expanded) {
            LocalAdminSetupPreviewContent(mismatch = false)
        }
    }
}

@Composable
private fun LocalAdminSetupPreviewContent(mismatch: Boolean) {
    var username by remember { mutableStateOf("truenas_admin") }
    var password by remember { mutableStateOf(if (mismatch) "secret123" else "") }
    var confirmPassword by remember { mutableStateOf(if (mismatch) "secret124" else "") }
    val window = LocalTrueHubWindow.current

    if (window.isCompactWidth) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LocalAdminInfoCard()
            LocalAdminFormCard(
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
            )
            LocalAdminActionsRow(
                isCreating = false,
                passwordsMatch = password == confirmPassword && password.isNotBlank(),
                onCancel = {},
                onSetup = {},
            )
        }
    } else {
        AdaptiveFormScreen(header = {}) {
            LocalAdminInfoCard()
            LocalAdminFormCard(
                username = username,
                onUsernameChange = { username = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
            )
            LocalAdminActionsRow(
                isCreating = false,
                passwordsMatch = password == confirmPassword && password.isNotBlank(),
                onCancel = {},
                onSetup = {},
            )
        }
    }
}
