package com.imnotndesh.truehub.ui.homepage.instancesettings.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ExpressiveFAB
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.InfoRow
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

@Composable
fun NetworkScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    val vm: NetworkConfigViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val isFabVisible = !scrollState.isScrollInProgress

    LaunchedEffect(Unit) { vm.loadAll() }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Network",
                subtitle = "Configuration and status",
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
                modifier = Modifier.offset(y=10.dp),
                initiallyExpanded = true,
                expandedDurationMillis = 2500
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingScreen("Loading network...")
            uiState.summary != null && uiState.config != null -> {
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

                    if (uiState.summary!!.ips.isNotEmpty()) {
                        ExpressiveSection("Interfaces", Icons.Default.SettingsEthernet) {
                            uiState.summary!!.ips.forEach { (iface, ipInfo) ->
                                InterfaceCard(iface, ipInfo)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }


                    if (uiState.summary!!.defaultRoutes!!.isNotEmpty()) {
                        ExpressiveSection("Default Routes", Icons.Default.Router) {
                            ExpressiveInfoCard {
                                uiState.summary!!.defaultRoutes?.forEachIndexed { idx, route ->
                                    InfoRow("Route $idx", route)
                                    if (idx < uiState.summary!!.defaultRoutes!!.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }


                    if (uiState.summary!!.nameservers.isNotEmpty()) {
                        ExpressiveSection("Nameservers", Icons.Default.Dns) {
                            ExpressiveInfoCard {
                                uiState.summary!!.nameservers.forEachIndexed { idx, ns ->
                                    InfoRow("Server ${idx + 1}", ns)
                                    if (idx < uiState.summary!!.nameservers.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }


                    ExpressiveSection("Identity", Icons.Default.Public) {
                        ExpressiveInfoCard {
                            InfoRow("Hostname", uiState.config!!.hostname)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Hostname (local)", uiState.config!!.hostnameLocal?.ifEmpty { "Not configured" } ?: "Not configured")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Domain", uiState.config!!.domain)
                        }
                    }


                    ExpressiveSection("Gateways", Icons.Default.Router) {
                        ExpressiveInfoCard {
                            InfoRow("IPv4 Gateway", uiState.config!!.ipv4Gateway ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("IPv6 Gateway", uiState.config!!.ipv6Gateway ?: "Not set")
                        }
                    }


                    ExpressiveSection("DNS Servers", Icons.Default.Language) {
                        ExpressiveInfoCard {
                            InfoRow("Nameserver 1", uiState.config!!.nameserver1 ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Nameserver 2", uiState.config!!.nameserver2 ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Nameserver 3", uiState.config!!.nameserver3 ?: "Not set")
                        }
                    }


                    if (!uiState.config!!.httpProxy.isNullOrBlank()) {
                        ExpressiveSection("HTTP Proxy", Icons.Default.Visibility) {
                            ExpressiveInfoCard {
                                InfoRow("Proxy URL", uiState.config!!.httpProxy ?: "")
                            }
                        }
                    }


                    if (uiState.config!!.domains.isNotEmpty()) {
                        ExpressiveSection("Search Domains", Icons.Default.Language) {
                            ExpressiveInfoCard {
                                uiState.config!!.domains.forEachIndexed { idx, domain ->
                                    InfoRow("Domain", domain)
                                    if (idx < uiState.config!!.domains.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }


                    if (uiState.config!!.hosts.isNotEmpty()) {
                        ExpressiveSection("Static Hosts", Icons.Default.Shield) {
                            ExpressiveInfoCard {
                                uiState.config!!.hosts.forEachIndexed { idx, host ->
                                    InfoRow("Entry", host)
                                    if (idx < uiState.config!!.hosts.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }

                    ExpressiveSection("Service Announcement", Icons.Default.Visibility) {
                        ExpressiveInfoCard {
                            val sa = uiState.config!!.serviceAnnouncement
                            if (sa != null) {
                                InfoRow("NetBIOS", if (sa.netbios == true) "Enabled" else "Disabled")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                InfoRow("mDNS", if (sa.mdns == true) "Enabled" else "Disabled")
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                InfoRow("WSD", if (sa.wsd == true) "Enabled" else "Disabled")
                            } else {
                                InfoRow("Status", "Not available")
                            }
                        }
                    }

                    ExpressiveSection("Activity Filtering", Icons.Default.Shield) {
                        ExpressiveInfoCard {
                            InfoRow("Type", uiState.config!!.activity.type)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Activities", if (uiState.config!!.activity.activities.isEmpty()) "None" else uiState.config!!.activity.activities.joinToString(", "))
                        }
                    }

                    if (uiState.config!!.hostnameB != null || uiState.config!!.hostnameVirtual != null) {
                        ExpressiveSection("HA Hostnames", Icons.Default.Public) {
                            ExpressiveInfoCard {
                                uiState.config!!.hostnameB?.let { InfoRow("Hostname B", it) }
                                if (uiState.config!!.hostnameB != null && uiState.config!!.hostnameVirtual != null) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                                uiState.config!!.hostnameVirtual?.let { InfoRow("Virtual Hostname", it) }
                            }
                        }
                    }

                    ExpressiveSection("Current State", Icons.Default.CheckCircle) {
                        ExpressiveInfoCard {
                            val st = uiState.config!!.state
                            InfoRow("IPv4 Gateway", st.ipv4Gateway ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("IPv6 Gateway", st.ipv6Gateway ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Nameserver 1", st.nameserver1 ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Nameserver 2", st.nameserver2 ?: "Not set")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            InfoRow("Nameserver 3", st.nameserver3 ?: "Not set")
                            if (st.hosts.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                InfoRow("Hosts", "${st.hosts.size} entries")
                            }
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
private fun InterfaceCard(iface: String, ipInfo: System.NetworkGeneralSummaryIP) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SettingsEthernet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    iface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            ipInfo.ipv4?.forEach { addr ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "IPv4",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        addr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            ipInfo.ipv6?.forEach { addr ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "IPv6",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        addr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}