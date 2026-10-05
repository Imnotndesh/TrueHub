package com.imnotndesh.truehub.ui.homepage.instancesettings.systeminformation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.adaptive.AdaptiveTokens
import com.imnotndesh.truehub.ui.adaptive.InfoCardGrid
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.TrueHubWindow
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveDivider
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.InfoRow

@Composable
fun SoftwareInformationScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val window = LocalTrueHubWindow.current
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
            SoftwareInformationContent(
                uiState = uiState,
                window = window,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SoftwareInformationContent(
    uiState: SystemInformationUiState,
    window: TrueHubWindow,
    modifier: Modifier = Modifier
) {
    if (window.isAtLeastExpanded) {
        InfoCardGrid(modifier = modifier, maxContentWidth = 1100.dp) {
            item {
                OperatingSystemSection(uiState)
            }
            item {
                FeaturesSection(uiState)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ReleaseNotesSection(uiState)
            }
        }
    } else {
        val contentModifier = if (window.isCompactWidth) {
            Modifier.fillMaxSize().padding(horizontal = 16.dp)
        } else {
            Modifier.fillMaxHeight().widthIn(max = AdaptiveTokens.ReadingMaxWidth)
        }
        Box(modifier, contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = contentModifier,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { OperatingSystemSection(uiState) }
                item { FeaturesSection(uiState) }
                item { ReleaseNotesSection(uiState) }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun OperatingSystemSection(uiState: SystemInformationUiState) {
    ExpressiveSection(title = "Operating System", icon = Icons.Default.Build) {
        ExpressiveInfoCard {
            InfoRow("Version", uiState.versionShort ?: uiState.version ?: "—")
            ExpressiveDivider()
            InfoRow("Full Version", uiState.version ?: "—")
            ExpressiveDivider()
            InfoRow("Product Type", uiState.productType ?: "—")
            ExpressiveDivider()
            InfoRow("State", uiState.state ?: "—")
            ExpressiveDivider()
            InfoRow("Ready", booleanLabel(uiState.ready))
        }
    }
}

@Composable
private fun FeaturesSection(uiState: SystemInformationUiState) {
    ExpressiveSection(title = "Features", icon = Icons.Default.Verified) {
        ExpressiveInfoCard {
            InfoRow("Deduplication", booleanLabel(uiState.dedupEnabled))
            ExpressiveDivider()
            InfoRow("Fibre Channel", booleanLabel(uiState.fibreChannelEnabled))
            ExpressiveDivider()
            InfoRow("Virtual Machines", booleanLabel(uiState.vmEnabled))
            ExpressiveDivider()
            InfoRow("Production", booleanLabel(uiState.isProduction))
            ExpressiveDivider()
            InfoRow("EULA Accepted", booleanLabel(uiState.isEulaAccepted))
            ExpressiveDivider()
            InfoRow("Managed by TrueCommand", booleanLabel(uiState.managedByTruecommand))
        }
    }
}

@Composable
private fun ReleaseNotesSection(uiState: SystemInformationUiState) {
    ExpressiveSection(title = "Release Notes", icon = Icons.Default.Link) {
        ExpressiveInfoCard {
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

private fun booleanLabel(value: Boolean?): String = when (value) {
    true -> "Yes"
    false -> "No"
    null -> "—"
}
