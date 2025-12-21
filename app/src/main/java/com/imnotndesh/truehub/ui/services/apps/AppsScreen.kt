package com.imnotndesh.truehub.ui.services.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.details.AppInfoDialog
import com.imnotndesh.truehub.ui.services.apps.details.RollbackVersionDialog
import com.imnotndesh.truehub.ui.services.apps.details.UpgradeSummaryBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(manager: TrueNASApiManager) {
    val appsScreenViewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    // Tracked App States
    val uiState by appsScreenViewModel.uiState.collectAsState()
    var appForUpgradeSummary by remember { mutableStateOf<String?>(null) }
    var showRollbackDialog by remember { mutableStateOf<String?>(null) }

    if (appForUpgradeSummary != null && uiState.upgradeSummaryResult != null) {
        UpgradeSummaryBottomSheet(
            appName = appForUpgradeSummary!!,
            upgradeSummary = uiState.upgradeSummaryResult!!,
            onDismiss = {
                appsScreenViewModel.clearUpgradeSummary()
                appForUpgradeSummary = null
            },
            onConfirmUpgrade = {
                appsScreenViewModel.upgradeApp(appForUpgradeSummary!!)
                appsScreenViewModel.clearUpgradeSummary()
                appForUpgradeSummary = null
            },
            isUpgrading = uiState.upgradeJobs.containsKey(appForUpgradeSummary)
        )
    }
    if (showRollbackDialog != null) {
        RollbackVersionDialog(
            appName = showRollbackDialog!!,
            versions = uiState.rollbackVersions,
            isLoadingVersions = uiState.isLoadingRollbackVersions,
            rollbackJobState = uiState.rollbackJobs[showRollbackDialog],
            onDismiss = {
                appsScreenViewModel.clearRollbackVersions()
                showRollbackDialog = null
            },
            onConfirmRollback = { version, rollbackSnapshot ->
                appsScreenViewModel.rollbackApp(showRollbackDialog!!, version, rollbackSnapshot)
            },
            onLoadVersions = {
                appsScreenViewModel.loadRollbackVersions(showRollbackDialog!!)
            }
        )
    }
    Column{
        UnifiedScreenHeader(
            title = "Applications",
            subtitle = "${uiState.apps.size} Applications",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onRefresh = { appsScreenViewModel.refresh() },
            onDismissError = { appsScreenViewModel.clearError() },
            manager = manager
        )
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { appsScreenViewModel.refresh() }

        ) {
            when {
                uiState.apps.isEmpty() && uiState.isLoading && !uiState.isRefreshing -> {
                    LoadingScreen("Loading Apps")
                }
                uiState.apps.isEmpty() && !uiState.isLoading && uiState.error != null -> {
                    EmptyContent()
                }
                else -> {
                    AppsContent(
                        apps = uiState.apps,
                        onStartApp = {appName ->appsScreenViewModel.startApp(appName)},
                        loadingSummaryForApp = uiState.isLoadingUpgradeSummaryForApp,
                        onStopApp = {appName -> appsScreenViewModel.stopApp(appName)},
                        onShowUpgradeSummary = { appName ->
                            appForUpgradeSummary = appName
                            appsScreenViewModel.loadUpgradeSummary(appName)
                        },
                        upgradeJobs = uiState.upgradeJobs,
                        onShowRollbackDialog = { appName -> showRollbackDialog = appName }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No services found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Install apps from the catalog to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppsContent(
    apps: List<Apps.AppQueryResponse>,
    loadingSummaryForApp: String?,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onShowUpgradeSummary: (String) -> Unit,
    upgradeJobs: Map<String, System.UpgradeJobState>,
    onShowRollbackDialog: (String) -> Unit,
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(
        compact = 1,    // 1 column on phones
        medium = 2,     // 2 columns on small tablets
        expanded = 3    // 3 columns on large tablets
    )
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    if (isCompact) {
        // Original list layout for phones
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(apps) { app ->
                ServiceCard(
                    app = app,
                    isLoadingSummary = app.name == loadingSummaryForApp,
                    onStartApp = onStartApp,
                    onStopApp = onStopApp,
                    onShowUpgradeSummary = onShowUpgradeSummary,
                    upgradeJobs = upgradeJobs,
                    onShowRollbackDialog = onShowRollbackDialog
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        // Adaptive grid layout for tablets/landscape
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(apps) { app ->
                ServiceCard(
                    app = app,
                    isLoadingSummary = app.name == loadingSummaryForApp,
                    onStartApp = onStartApp,
                    onStopApp = onStopApp,
                    onShowUpgradeSummary = onShowUpgradeSummary,
                    upgradeJobs = upgradeJobs,
                    onShowRollbackDialog = onShowRollbackDialog
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCard(
    app: Apps.AppQueryResponse,
    isLoadingSummary: Boolean,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onShowUpgradeSummary: (String) -> Unit,
    upgradeJobs: Map<String, System.UpgradeJobState>,
    onShowRollbackDialog: (String) -> Unit,
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    // Adaptive padding based on screen size
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            // Header section - Adaptive layout
            if (isCompact) {
                // Phone layout - Horizontal row with all info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    app.metadata?.icon.let { iconUrl ->
                        AsyncImage(
                            model = iconUrl,
                            contentDescription = "App icon",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = app.metadata?.title ?: app.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ID: ${app.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusChip(state = app.state, icon = getStatusIcon(app.state))
                }
            } else {
                // Tablet layout - Vertical stack with compact status indicator
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        app.metadata?.icon.let { iconUrl ->
                            AsyncImage(
                                model = iconUrl,
                                contentDescription = "App icon",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Compact status indicator - just a colored dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getStatusColor(app.state))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = app.metadata?.title ?: app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ID: ${app.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (app.upgrade_available || upgradeJobs[app.name] != null) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Update available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    if (upgradeJobs[app.name] == null) {
                        UpgradeButton(
                            onClick = { onShowUpgradeSummary(app.name) },
                            isLoading = isLoadingSummary
                        )
                    } else {
                        UpgradeStatusChip(upgradeState = upgradeJobs[app.name]!!)
                    }
                }

                upgradeJobs[app.name]?.let { jobState ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = jobState.description ?: "Upgrading...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${jobState.progress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (jobState.progress.coerceIn(0, 100).toFloat() / 100f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary action row - Adaptive button style
            if (isCompact) {
                // Phone layout - Full buttons with text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!app.state.equals("running", ignoreCase = true)){
                        ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = { onStartApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }else{
                        ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true,
                            onClick = { onStopApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ActionButton(
                        text = "View Info",
                        icon = Icons.Default.Info,
                        enabled = true,
                        isPrimary = false,
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Tablet layout - Icon-only buttons in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!app.state.equals("running", ignoreCase = true)){
                        CompactActionButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            isPrimary = true,
                            onClick = { onStartApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }else{
                        CompactActionButton(
                            icon = Icons.Default.Stop,
                            contentDescription = "Stop",
                            isPrimary = true,
                            onClick = { onStopApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CompactActionButton(
                        icon = Icons.Default.Info,
                        contentDescription = "View Info",
                        isPrimary = false,
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    CompactActionButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "More Options",
                        isPrimary = false,
                        onClick = { showMoreOptions = !showMoreOptions },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // More options section - only show on phones
            if (isCompact) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showMoreOptions = !showMoreOptions },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "More Options",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = if (showMoreOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showMoreOptions) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showMoreOptions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    ActionButton(
                        text = "Rollback Version",
                        icon = Icons.Default.Refresh,
                        enabled = true,
                        isPrimary = false,
                        onClick = { onShowRollbackDialog(app.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Less Important Options go in here
                }
            }
        }
    }

    if (showInfoDialog) {
        AppInfoDialog(
            app = app,
            onDismiss = { showInfoDialog = false }
        )
    }
}
@Composable
private fun CompactActionButton(
    icon: ImageVector,
    contentDescription: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (isPrimary)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
private fun UpgradeButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading){
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }else{
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Update",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun UpgradeStatusChip(
    upgradeState: System.UpgradeJobState
) {
    Surface(
        color = when (upgradeState.state.lowercase()) {
            "success" -> MaterialTheme.colorScheme.primaryContainer
            "failed", "aborted" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (upgradeState.state.lowercase()) {
                "success" -> {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                "failed", "aborted" -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                else -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = when (upgradeState.state.lowercase()) {
                    "upgrading" -> "Upgrading..."
                    "success" -> "Updated"
                    "failed" -> "Failed"
                    "aborted" -> "Aborted"
                    else -> upgradeState.state.lowercase().replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.labelMedium,
                color = when (upgradeState.state.lowercase()) {
                    "success" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "failed", "aborted" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusChip(
    state: String,
    icon: ImageVector
) {
    Surface(
        color = getStatusColor(state).copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = getStatusColor(state)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = state.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = getStatusColor(state),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun getStatusColor(state: String): Color {
    return when (state.lowercase()) {
        "running", "active" -> Color(0xFF2E7D32)
        "stopped", "inactive" -> Color(0xFF757575)
        "error", "failed" -> MaterialTheme.colorScheme.error
        "paused" -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(state: String): ImageVector {
    return when (state.lowercase()) {
        "running", "active" -> Icons.Default.PlayArrow
        "stopped", "inactive" -> Icons.Default.Stop
        "error", "failed" -> Icons.Default.Error
        "paused" -> Icons.Default.Pause
        else -> Icons.Default.Stop
    }
}