package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.adaptive.AdaptiveFormScreen
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.TrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.fakeTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.ProvideTrueHubWindowForPreview
import com.imnotndesh.truehub.ui.adaptive.WidthClass
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ApiKeyCreateScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: ApiKeyViewModel = hiltViewModel()
    val uiState by vm.createState.collectAsState()
    val clipboard = LocalClipboardManager.current
    val window = LocalTrueHubWindow.current

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var expiresAt by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createResult) {
        if (uiState.createResult != null) {
            uiState.createResult?.key?.let {
                clipboard.setText(AnnotatedString(it))
            }
        }
    }

    val onCancel: () -> Unit = onNavigateBack
    val onCreate: () -> Unit = {
        vm.createKey(System.ApiKeyCreate(
            name = name.ifBlank { "nobody" },
            username = username,
            expires_at = expiresAt.ifBlank { null }
        ))
    }

    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer))
    )) {
        val header: @Composable () -> Unit = {
            UnifiedScreenHeader(
                title = "Create API Key",
                subtitle = "Generate a new API access key",
                isLoading = false, isRefreshing = false,
                error = uiState.error, onDismissError = { vm.clearCreateError() },
                manager = manager, onBackPressed = onNavigateBack
            )
        }

        if (window.isCompactWidth) {
            header()
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ApiKeyCreateContent(
                    window = window,
                    name = name,
                    onNameChange = { name = it },
                    username = username,
                    onUsernameChange = { username = it },
                    expiresAt = expiresAt,
                    onExpiresAtChange = { expiresAt = it },
                    isCreating = uiState.isCreating,
                    createResult = uiState.createResult,
                    onCancel = onCancel,
                    onCreate = onCreate,
                    onDismissReveal = { vm.clearCreateResult(); onNavigateBack() }
                )
            }
        } else {
            AdaptiveFormScreen(header = header) {
                ApiKeyCreateContent(
                    window = window,
                    name = name,
                    onNameChange = { name = it },
                    username = username,
                    onUsernameChange = { username = it },
                    expiresAt = expiresAt,
                    onExpiresAtChange = { expiresAt = it },
                    isCreating = uiState.isCreating,
                    createResult = uiState.createResult,
                    onCancel = onCancel,
                    onCreate = onCreate,
                    onDismissReveal = { vm.clearCreateResult(); onNavigateBack() }
                )
            }
        }
    }
}

/**
 * The form body (card + action row) or the one-time reveal dialog, shared by the compact
 * scroll column and the capped [AdaptiveFormScreen] form column.
 */
@Composable
private fun ColumnScope.ApiKeyCreateContent(
    window: TrueHubWindow,
    name: String,
    onNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    expiresAt: String,
    onExpiresAtChange: (String) -> Unit,
    isCreating: Boolean,
    createResult: System.ApiKeyEntryWithKey?,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    onDismissReveal: () -> Unit,
) {
    if (createResult != null) {
        CreditCardKeyDialog(
            keyName = createResult.name,
            keyValue = createResult.key,
            onDismiss = onDismissReveal
        )
    } else {
        val canCreate = !isCreating && username.isNotBlank()
        val fullWidth = window.isCompactWidth

        Column(
            modifier = Modifier
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.Enter && canCreate) {
                        onCreate()
                        true
                    } else {
                        false
                    }
                },
            verticalArrangement = Arrangement.spacedBy(if (fullWidth) 16.dp else 12.dp)
        ) {
            ApiKeyCreateCard(
                name = name, onNameChange = onNameChange,
                username = username, onUsernameChange = onUsernameChange,
                expiresAt = expiresAt, onExpiresAtChange = onExpiresAtChange,
                modifier = Modifier.fillMaxWidth()
            )
            ApiKeyCreateButtons(
                isCreating = isCreating,
                enabled = canCreate,
                onCancel = onCancel,
                onCreate = onCreate,
                fullWidth = fullWidth
            )
        }
    }
}

@Composable
private fun ApiKeyCreateCard(
    name: String,
    onNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    expiresAt: String,
    onExpiresAtChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("New API Key", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            OutlinedTextField(value = name, onValueChange = onNameChange,
                label = { Text("Key Name") }, placeholder = { Text("e.g. My App Key") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = username, onValueChange = onUsernameChange,
                label = { Text("Username *") }, placeholder = { Text("e.g. admin") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = expiresAt, onValueChange = onExpiresAtChange,
                label = { Text("Expires At (optional)") }, placeholder = { Text("YYYY-MM-DD or leave empty") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }
}

@Composable
private fun ColumnScope.ApiKeyCreateButtons(
    isCreating: Boolean,
    enabled: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    fullWidth: Boolean,
) {
    Row(
        modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier.align(Alignment.End),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val buttonModifier = if (fullWidth) Modifier.weight(1f) else Modifier
        OutlinedButton(onClick = onCancel, modifier = buttonModifier) { Text("Cancel") }
        Button(onClick = onCreate, enabled = enabled, modifier = buttonModifier) {
            if (isCreating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Icon(Icons.Default.VpnKey, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp)); Text("Create Key")
        }
    }
}

@Preview(name = "Create API Key — compact", widthDp = 400, heightDp = 800)
@Composable
private fun ApiKeyCreateFormCompactPreview() {
    PreviewShell(WidthClass.Compact)
}

@Preview(name = "Create API Key — medium", widthDp = 800, heightDp = 1000)
@Composable
private fun ApiKeyCreateFormMediumPreview() {
    PreviewShell(WidthClass.Medium)
}

@Preview(name = "Create API Key — expanded", widthDp = 1200, heightDp = 900)
@Composable
private fun ApiKeyCreateFormExpandedPreview() {
    PreviewShell(WidthClass.Expanded)
}

@Preview(name = "API Key created — reveal", widthDp = 400, heightDp = 800)
@Composable
private fun ApiKeyRevealPreview() {
    ProvideTrueHubWindowForPreview(WidthClass.Compact) {
        CreditCardKeyDialog(
            keyName = "My App Key",
            keyValue = "1-abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
            onDismiss = {}
        )
    }
}

@Composable
private fun PreviewShell(width: WidthClass) {
    val window = fakeTrueHubWindow(width)
    ProvideTrueHubWindowForPreview {
        Column(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer))
        )) {
            val header: @Composable () -> Unit = {
                UnifiedScreenHeader(
                    title = "Create API Key",
                    subtitle = "Generate a new API access key",
                    isLoading = false,
                    isRefreshing = false,
                    onDismissError = {},
                    manager = null,
                    onBackPressed = {}
                )
            }
            var name by remember { mutableStateOf("") }
            var username by remember { mutableStateOf("") }
            var expiresAt by remember { mutableStateOf("") }
            if (window.isCompactWidth) {
                header()
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ApiKeyCreateContent(
                        window = window, name = name, onNameChange = { name = it },
                        username = username, onUsernameChange = { username = it },
                        expiresAt = expiresAt, onExpiresAtChange = { expiresAt = it },
                        isCreating = false, createResult = null, onCancel = {}, onCreate = {}, onDismissReveal = {}
                    )
                }
            } else {
                AdaptiveFormScreen(header = header) {
                    ApiKeyCreateContent(
                        window = window, name = name, onNameChange = { name = it },
                        username = username, onUsernameChange = { username = it },
                        expiresAt = expiresAt, onExpiresAtChange = { expiresAt = it },
                        isCreating = false, createResult = null, onCancel = {}, onCreate = {}, onDismissReveal = {}
                    )
                }
            }
        }
    }
}
