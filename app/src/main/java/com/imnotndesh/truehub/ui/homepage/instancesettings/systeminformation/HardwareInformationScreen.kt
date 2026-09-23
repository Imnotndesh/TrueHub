package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.DecimalFormat
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppDataHolder

@Composable
fun HardwareInformationScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDisks: () -> Unit = {}
) {
    val vm: HardwareInformationViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = "Hardware Information",
                subtitle = "CPU, memory and platform",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        PullToRefreshContent(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val info = uiState.systemInfo

                if (info != null) {
                    item {
                        ExpressiveSection(title = "CPU", icon = Icons.Default.Build) {
                            InfoCard {
                                InfoRow("Model", info.model.ifEmpty { "—" })
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                InfoRow("Physical Cores", info.physical_cores?.toString() ?: "—")
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                InfoRow("Total Cores", info.cores.toInt().toString())
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "Memory", icon = Icons.Default.Memory) {
                            InfoCard {
                                InfoRow(
                                    "Total Memory",
                                    "${DecimalFormat("#.#").format(info.physmem / (1024.0 * 1024.0 * 1024.0))} GB"
                                )
                                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                InfoRow("ECC", if (info.ecc_memory) "Yes" else "No")
                            }
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Storage", icon = Icons.Default.Storage) {
                        InfoCard {
                            InfoRow("Disks", uiState.diskCount.toString())
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Pools", if (uiState.poolsHealthy == null) "—" else if (uiState.poolsHealthy == true) "Healthy" else "Issues detected")
                            Spacer(Modifier.height(16.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    AppDataHolder.disks = uiState.disks
                                    onNavigateToDisks()
                                },
                                enabled = uiState.disks.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Storage, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.padding(start = 6.dp))
                                Text("View Disks", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Platform", icon = Icons.Default.Hardware) {
                        InfoCard {
                            InfoRow("Chassis", uiState.chassisHardware ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("iX Systems Hardware", booleanLabel(uiState.isIxHardware))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Host ID", uiState.hostId ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Boot ID", uiState.bootId ?: "—")
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun booleanLabel(value: Boolean?): String = when (value) {
    true -> "Yes"
    false -> "No"
    null -> "—"
}
