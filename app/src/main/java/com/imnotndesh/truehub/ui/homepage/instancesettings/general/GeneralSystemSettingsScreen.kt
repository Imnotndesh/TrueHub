package com.imnotndesh.truehub.ui.homepage.instancesettings.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.ExpressiveFAB
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun GeneralSystemSettingsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    val vm: GeneralSystemSettingsViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isFabVisible = !scrollState.isScrollInProgress

    LaunchedEffect(Unit) { vm.loadAll() }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "General Settings",
                subtitle = "System general configuration",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        floatingActionButton = {
            ExpressiveFAB(
                modifier = Modifier.offset(y = (-20).dp),
                onClick = onNavigateToEdit,
                visible = isFabVisible,
                initiallyExpanded = true,
                expandedDurationMillis = 2500
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingScreen("Loading system settings...")
            uiState.config != null -> {
                val config = uiState.config!!
                PullToRefreshContent(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    uiState.checkinWaiting?.let { seconds ->
                        RollbackWaitingCard(seconds = seconds, onCheckIn = { vm.doCheckin() })
                    }
                    ExpressiveSection(title = "Web Interface", icon = Icons.Default.Visibility) {
                        ExpressiveInfoCard {
                            InfoRow(label = "HTTP Port", value = config.uiPort.toString())
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "HTTPS Port", value = config.uiHttpsPort.toString())
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "HTTPS Redirect", value = if (config.uiHttpsRedirect == true) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "HTTPS Protocols", value = config.uiHttpsProtocols?.joinToString(", ").orEmpty().ifEmpty { "Default" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Console Messages", value = if (config.uiConsoleMsg == true) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "X-Frame-Options", value = config.uiXFrameOptions ?: "SAMEORIGIN")
                        }

                    }

                    ExpressiveSection(title = "Network Addresses", icon = Icons.Default.SettingsEthernet) {
                        ExpressiveInfoCard {
                            InfoRow(label = "IPv4 Addresses", value = config.uiAddress?.joinToString(", ").orEmpty().ifEmpty { "All (0.0.0.0)" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "IPv6 Addresses", value = config.uiV6Address?.joinToString(", ").orEmpty().ifEmpty { "All (::)" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Allow List", value = config.uiAllowlist?.joinToString(", ").orEmpty().ifEmpty { "None" })
                        }

                    }

                    uiState.localUrl?.let { url ->
                        ExpressiveSection(title = "Local URL", icon = Icons.Default.Link) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    ExpressiveSection(title = "Localization", icon = Icons.Default.Language) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Timezone", value = config.timezone.orEmpty().ifEmpty { "Not Configured" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Keyboard Map", value = config.kbdMap.orEmpty().ifEmpty { "Not Configured" })
                        }

                    }

                    ExpressiveSection(title = "Other", icon = Icons.Default.Security) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Wizard Shown", value = if (config.wizardShown == true) "Yes" else "No")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "DS Auth", value = if (config.dsAuth == true) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Usage Collection", value = if (config.usageCollectionIsSet == true) "Configured" else "Not configured")
                        }

                    }
                }
                }
            }
        }
    }
}
@Composable
private fun RollbackWaitingCard(seconds: Int, onCheckIn: () -> Unit) {
    ExpressiveSection(title = "Rollback Pending", icon = Icons.Default.Timer) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Automatic rollback in ${seconds}s. Check in to confirm these changes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onCheckIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun ExpressiveSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
internal fun ExpressiveInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
internal fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}