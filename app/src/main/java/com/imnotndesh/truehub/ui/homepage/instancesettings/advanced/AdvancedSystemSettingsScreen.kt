package com.imnotndesh.truehub.ui.homepage.instancesettings.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.SdStorage
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.ExpressiveFAB
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun AdvancedSystemSettingsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    val vm: AdvancedSystemSettingsViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isFabVisible = !scrollState.isScrollInProgress

    LaunchedEffect(Unit) { vm.loadAll() }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Advanced Settings",
                subtitle = "System advanced configuration",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExpressiveFAB(
                onClick = onNavigateToEdit,
                visible = isFabVisible,
                initiallyExpanded = true,
                expandedDurationMillis = 2500
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingScreen("Loading advanced settings...")
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
                        .verticalScroll(scrollState)
                        .padding(start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (config.login_banner.isNotBlank() || config.motd.isNotBlank()) {
                        ExpressiveSection(title = "Messaging", icon = Icons.Default.Mail) {
                            MessageBannerCard(loginBanner = config.login_banner, motd = config.motd)
                        }
                    }

                    ExpressiveSection(title = "Console & Serial", icon = Icons.Default.Menu) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Console Menu", value = if (config.consolemenu) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Console Messages", value = if (config.consolemsg) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Serial Console", value = if (config.serialconsole) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Serial Port", value = config.serialport.ifEmpty { "—" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Serial Speed", value = config.serialspeed.ifEmpty { "—" })
                        }
                        // ❌ SectionEditButton removed
                    }

                    ExpressiveSection(title = "Kernel & Debug", icon = Icons.Default.BugReport) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Debug Kernel", value = if (config.debugkernel) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Kdump", value = if (config.kdump_enabled) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Autotune", value = if (config.autotune) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Advanced Mode", value = if (config.advancedmode) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Extra Kernel Options", value = config.kernel_extra_options.ifEmpty { "None" })
                        }
                    }

                    ExpressiveSection(title = "Syslog", icon = Icons.Default.Dns) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Syslog Level", value = config.sysloglevel.ifEmpty { "—" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "FQDN in Syslog", value = if (config.fqdn_syslog) "Yes" else "No")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(
                                label = "Syslog Audit",
                                value = when (config.syslog_audit) {
                                    true -> "Enabled"
                                    false -> "Disabled"
                                    null -> "Not configured"
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            val serverCount = config.syslogservers?.size ?: 0
                            InfoRow(label = "Syslog Servers", value = if (serverCount > 0) "$serverCount configured" else "None")
                        }
                    }

                    ExpressiveSection(title = "Crash Reporting", icon = Icons.Default.Report) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Traceback", value = if (config.traceback) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Upload Crashes", value = if (config.uploadcrash) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Anonymous Stats", value = if (config.anonstats) "Enabled" else "Disabled")
                        }
                    }

                    ExpressiveSection(title = "GPU Isolation", icon = Icons.Default.DeveloperBoard) {
                        ExpressiveInfoCard {
                            val gpuCount = config.isolated_gpu_pci_ids.size
                            InfoRow(label = "Isolated GPUs", value = if (gpuCount > 0) "$gpuCount GPU(s) isolated" else "None")
                        }
                    }

                    ExpressiveSection(title = "Power", icon = Icons.Default.PowerSettingsNew) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Power Daemon", value = if (config.powerdaemon) "Enabled" else "Disabled")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "Boot Scrub", value = "${config.boot_scrub}")
                        }
                    }

                    ExpressiveSection(title = "SED", icon = Icons.Default.Key) {
                        ExpressiveInfoCard {
                            InfoRow(label = "SED User", value = config.sed_user.ifEmpty { "—" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow(label = "SED Password Set", value = if (uiState.sedPasswordIsSet) "Yes" else "No")
                        }
                    }

                    ExpressiveSection(title = "Storage", icon = Icons.Default.SdStorage) {
                        ExpressiveInfoCard {
                            InfoRow(label = "Overprovision", value = config.overprovision?.toString() ?: "Not set")
                        }
                    }
                    Spacer(modifier = Modifier.height(64.dp))
                }
                }
            }
        }
    }
}

@Composable
private fun MessageBannerCard(loginBanner: String, motd: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loginBanner.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Default.Info, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Login Banner",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            loginBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            if (motd.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        Icons.Default.Mail, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Message of the Day",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            motd,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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