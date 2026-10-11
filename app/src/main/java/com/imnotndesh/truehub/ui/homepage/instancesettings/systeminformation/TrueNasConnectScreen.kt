package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.adaptive.AdaptiveFormScreen
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.ProvideTrueHubWindowForPreview
import com.imnotndesh.truehub.ui.adaptive.WidthClass
import com.imnotndesh.truehub.ui.adaptive.fakeTrueHubWindow
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.InfoRow

@Composable
fun TrueNasConnectScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: TrueNasConnectViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val window = LocalTrueHubWindow.current
    val snackbarHostState = remember { SnackbarHostState() }
    var enabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    LaunchedEffect(uiState.config?.enabled) {
        enabled = uiState.config?.enabled ?: false
    }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearActionMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UnifiedScreenHeader(
                title = "TrueNAS Connect",
                subtitle = "Cloud connection and registration",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.config == null ->
                LoadingScreen("Loading TrueNAS Connect...")
            else ->
                PullToRefreshContent(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    val onToggleEnabled: (Boolean) -> Unit = {
                        vm.updateConfig(System.TNConnectUpdateArgs(enabled = it))
                    }
                    if (window.isCompactWidth) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                TrueNasConnectContent(
                                    uiState = uiState,
                                    enabled = enabled,
                                    onToggleEnabled = onToggleEnabled,
                                    onGenerateClaimToken = { vm.generateClaimToken() },
                                    onLoadRegistrationUri = { vm.loadRegistrationUri() }
                                )
                            }
                        }
                    } else {
                        // Medium and up: single 640dp form column, centered (R-8/pattern F).
                        AdaptiveFormScreen(header = {}) {
                            TrueNasConnectContent(
                                uiState = uiState,
                                enabled = enabled,
                                onToggleEnabled = onToggleEnabled,
                                onGenerateClaimToken = { vm.generateClaimToken() },
                                onLoadRegistrationUri = { vm.loadRegistrationUri() }
                            )
                        }
                    }
                }
        }
    }
}

/** Shared body: status card + Connection/Interfaces/Registration sections, at every width. */
@Composable
private fun TrueNasConnectContent(
    uiState: TrueNasConnectUiState,
    enabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onGenerateClaimToken: () -> Unit,
    onLoadRegistrationUri: () -> Unit,
) {
    val config = uiState.config
    if (config != null) {
        StatusCard(config = config, enabled = enabled, onToggleEnabled = onToggleEnabled)

        ExpressiveSection(title = "Connection", icon = Icons.Default.Cloud) {
            ExpressiveInfoCard {
                InfoRow("Status", config.status)
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("Account Base URL", config.accountServiceBaseUrl!!)
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("TNC Base URL", config.tncBaseUrl!!)
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                InfoRow("Heartbeat URL", config.heartbeatUrl!!)
            }
        }

        if (config.interfaces.isNotEmpty()) {
            ExpressiveSection(title = "Interfaces", icon = Icons.Default.Share) {
                ExpressiveInfoCard {
                    InfoRow("Use All Interfaces", if (config.useAllInterfaces!!) "Yes" else "No")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    InfoRow("Interfaces", config.interfaces.joinToString(", "))
                    if (config.interfacesIps.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        InfoRow("Interface IPs", config.interfacesIps.joinToString(", "))
                    }
                }
            }
        }
    }

    ExpressiveSection(title = "Registration", icon = Icons.Default.Key) {
        ExpressiveInfoCard {
            Text(
                "Generate a claim token, then fetch the registration URI to claim this system.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onGenerateClaimToken,
                    enabled = !uiState.isActing,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Key, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Generate Claim", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onLoadRegistrationUri,
                    enabled = !uiState.isActing,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Registration", fontWeight = FontWeight.SemiBold)
                }
            }
            uiState.claimToken?.let {
                Spacer(Modifier.height(16.dp))
                InfoRow("Claim Token", it)
            }
            uiState.registrationUri?.let {
                Spacer(Modifier.height(8.dp))
                InfoRow("Registration URI", it)
            }
        }
    }
}

@Composable
private fun StatusCard(config: System.TNCEntry, enabled: Boolean, onToggleEnabled: (Boolean) -> Unit) {    Card(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = onToggleEnabled
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (config.enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    config.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(checked = enabled, onCheckedChange = null)
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private fun previewState(): TrueNasConnectUiState = TrueNasConnectUiState(
    config = System.TNCEntry(
        id = 1,
        enabled = true,
        interfaces = listOf("enp0s1", "enp0s2"),
        interfacesIps = listOf("10.0.0.4/24", "10.0.0.5/24"),
        useAllInterfaces = false,
        status = "Connected to the cloud",
        accountServiceBaseUrl = "https://account.truenas.com",
        tncBaseUrl = "https://tnc.truenas.com",
        heartbeatUrl = "https://tnc.truenas.com/heartbeat"
    )
)

@Composable
private fun TrueNasConnectPreviewShell(width: WidthClass) {
    ProvideTrueHubWindowForPreview(width = width) {
        Column(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer)
                )
            )
        ) {
            UnifiedScreenHeader(
                title = "TrueNAS Connect",
                subtitle = "Cloud connection and registration",
                isLoading = false,
                isRefreshing = false,
                onDismissError = {},
                manager = null,
                onBackPressed = {}
            )
            val state = previewState()
            if (fakeTrueHubWindow(width).isCompactWidth) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        TrueNasConnectContent(
                            uiState = state, enabled = true,
                            onToggleEnabled = {}, onGenerateClaimToken = {}, onLoadRegistrationUri = {}
                        )
                    }
                }
            } else {
                AdaptiveFormScreen(header = {}) {
                    TrueNasConnectContent(
                        uiState = state, enabled = true,
                        onToggleEnabled = {}, onGenerateClaimToken = {}, onLoadRegistrationUri = {}
                    )
                }
            }
        }
    }
}

@Preview(name = "TrueNAS Connect — compact", widthDp = 400, heightDp = 800)
@Composable
private fun TrueNasConnectCompactPreview() = TrueNasConnectPreviewShell(WidthClass.Compact)

@Preview(name = "TrueNAS Connect — compact landscape", widthDp = 800, heightDp = 400)
@Composable
private fun TrueNasConnectCompactLandscapePreview() = TrueNasConnectPreviewShell(WidthClass.Compact)

@Preview(name = "TrueNAS Connect — medium", widthDp = 800, heightDp = 1000)
@Composable
private fun TrueNasConnectMediumPreview() = TrueNasConnectPreviewShell(WidthClass.Medium)

@Preview(name = "TrueNAS Connect — expanded", widthDp = 1000, heightDp = 800)
@Composable
private fun TrueNasConnectExpandedPreview() = TrueNasConnectPreviewShell(WidthClass.Expanded)

@Preview(name = "TrueNAS Connect — large", widthDp = 1280, heightDp = 900)
@Composable
private fun TrueNasConnectLargePreview() = TrueNasConnectPreviewShell(WidthClass.Large)

@Preview(name = "TrueNAS Connect — xlarge", widthDp = 1600, heightDp = 1000)
@Composable
private fun TrueNasConnectXLargePreview() = TrueNasConnectPreviewShell(WidthClass.ExtraLarge)
