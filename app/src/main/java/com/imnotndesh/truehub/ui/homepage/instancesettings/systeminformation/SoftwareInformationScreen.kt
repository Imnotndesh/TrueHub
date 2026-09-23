package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Verified
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
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

@Composable
fun SoftwareInformationScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: SystemInformationViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = "Software Information",
                subtitle = "Version and features",
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
                item {
                    ExpressiveSection(title = "Operating System", icon = Icons.Default.Build) {
                        InfoCard {
                            InfoRow("Version", uiState.versionShort ?: uiState.version ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Full Version", uiState.version ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Product Type", uiState.productType ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("State", uiState.state ?: "—")
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Ready", booleanLabel(uiState.ready))
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Features", icon = Icons.Default.Verified) {
                        InfoCard {
                            InfoRow("Deduplication", booleanLabel(uiState.dedupEnabled))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Fibre Channel", booleanLabel(uiState.fibreChannelEnabled))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Virtual Machines", booleanLabel(uiState.vmEnabled))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Production", booleanLabel(uiState.isProduction))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("EULA Accepted", booleanLabel(uiState.isEulaAccepted))
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Managed by TrueCommand", booleanLabel(uiState.managedByTruecommand))
                        }
                    }
                }

                item {
                    ExpressiveSection(title = "Release Notes", icon = Icons.Default.Link) {
                        InfoCard {
                            Text(
                                uiState.releaseNotesUrl ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
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
