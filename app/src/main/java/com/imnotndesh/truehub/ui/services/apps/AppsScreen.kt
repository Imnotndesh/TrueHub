package com.imnotndesh.truehub.ui.services.apps

import AppCategory
import AppsScreenViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.details.AppInfoDialog
import com.imnotndesh.truehub.ui.services.apps.details.AppInfoPane
import com.imnotndesh.truehub.ui.services.apps.details.RollbackVersionDialog
import com.imnotndesh.truehub.ui.services.apps.details.UpgradeSummaryBottomSheet
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(manager: TrueNASApiManager) {
    val appsScreenViewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    val uiState by appsScreenViewModel.uiState.collectAsState()
    var appForUpgradeSummary by remember { mutableStateOf<String?>(null) }
    var showRollbackDialog by remember { mutableStateOf<String?>(null) }
    var selectedAppForInfo by remember { mutableStateOf<Apps.AppQueryResponse?>(null) }

    val isCompact = AdaptiveLayoutHelper.isCompact()

    val filteredApps by remember(uiState.apps, uiState.selectedCategory) {
        derivedStateOf {
            when (uiState.selectedCategory) {
                AppCategory.ALL -> uiState.apps
                AppCategory.RUNNING -> uiState.apps.filter { it.state.equals("running", ignoreCase = true) }
                AppCategory.STOPPED -> uiState.apps.filter { !it.state.equals("running", ignoreCase = true) }
                AppCategory.UPDATES -> uiState.apps.filter { it.upgrade_available }
            }
        }
    }

    // Dialogs & Sheets (unchanged logic)
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

    if (isCompact && selectedAppForInfo != null) {
        AppInfoDialog(
            app = selectedAppForInfo!!,
            onDismiss = { selectedAppForInfo = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Applications",
            subtitle = "${filteredApps.size} Applications",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onRefresh = { appsScreenViewModel.refresh() },
            onDismissError = { appsScreenViewModel.clearError() },
            manager = manager
        )

        // New Filter Bar
        AppFilterBar(
            currentCategory = uiState.selectedCategory,
            onCategorySelected = {appsScreenViewModel.updateCategory(it)},
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { appsScreenViewModel.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            when {
                uiState.apps.isEmpty() && uiState.isLoading && !uiState.isRefreshing -> {
                    LoadingScreen("Loading Apps")
                }
                uiState.apps.isEmpty() && !uiState.isLoading && uiState.error != null -> {
                    EmptyContent()
                }
                filteredApps.isEmpty() && !uiState.isLoading && uiState.apps.isNotEmpty() -> {
                    // Show "No results" for filter
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No apps in this category",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    if (isCompact) {
                        AppsContent(
                            apps = filteredApps, // Use filtered list
                            onStartApp = { appName -> appsScreenViewModel.startApp(appName) },
                            loadingSummaryForApp = uiState.isLoadingUpgradeSummaryForApp,
                            onStopApp = { appName -> appsScreenViewModel.stopApp(appName) },
                            onShowUpgradeSummary = { appName ->
                                appForUpgradeSummary = appName
                                appsScreenViewModel.loadUpgradeSummary(appName)
                            },
                            upgradeJobs = uiState.upgradeJobs,
                            onShowRollbackDialog = { appName -> showRollbackDialog = appName },
                            onAppInfoClick = { app -> selectedAppForInfo = app },
                            selectedApp = null
                        )
                    } else {
                        AppsSplitPaneContent(
                            apps = filteredApps, // Use filtered list
                            selectedApp = selectedAppForInfo,
                            onStartApp = { appName -> appsScreenViewModel.startApp(appName) },
                            loadingSummaryForApp = uiState.isLoadingUpgradeSummaryForApp,
                            onStopApp = { appName -> appsScreenViewModel.stopApp(appName) },
                            onShowUpgradeSummary = { appName ->
                                appForUpgradeSummary = appName
                                appsScreenViewModel.loadUpgradeSummary(appName)
                            },
                            upgradeJobs = uiState.upgradeJobs,
                            onShowRollbackDialog = { appName -> showRollbackDialog = appName },
                            onAppInfoClick = { app -> selectedAppForInfo = app },
                            onCloseInfoPane = { selectedAppForInfo = null }
                        )
                    }
                }
            }
        }
    }
}

// New Filter Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterBar(
    currentCategory: AppCategory,
    onCategorySelected: (AppCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AppCategory.entries.toTypedArray()) { category ->
            val isSelected = currentCategory == category
            val animColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                label = "color"
            )

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = animColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp) // Expressive shape
            )
        }
    }
}


@Composable
private fun AppsSplitPaneContent(
    apps: List<Apps.AppQueryResponse>,
    selectedApp: Apps.AppQueryResponse?,
    loadingSummaryForApp: String?,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onShowUpgradeSummary: (String) -> Unit,
    upgradeJobs: Map<String, System.UpgradeJobState>,
    onShowRollbackDialog: (String) -> Unit,
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    onCloseInfoPane: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedApp != null) 0.55f else 1f)
                .fillMaxSize()
        ) {
            AppsContent(
                apps = apps,
                onStartApp = onStartApp,
                loadingSummaryForApp = loadingSummaryForApp,
                onStopApp = onStopApp,
                onShowUpgradeSummary = onShowUpgradeSummary,
                upgradeJobs = upgradeJobs,
                onShowRollbackDialog = onShowRollbackDialog,
                onAppInfoClick = onAppInfoClick,
                selectedApp = selectedApp
            )
        }
        AnimatedVisibility(
            visible = selectedApp != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeOut()
        ) {
            selectedApp?.let { app ->
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    AppInfoPane(
                        app = app,
                        onClose = onCloseInfoPane
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
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    selectedApp: Apps.AppQueryResponse?
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(
        compact = 1,
        medium = 2,
        expanded = 3
    )
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    // Using key for animations
    if (isCompact) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(
                items = apps,
                key = { it.id } // Key is required for animateItem
            ) { app ->
                Box(modifier = Modifier.animateItem()) {
                    ServiceCard(
                        app = app,
                        isLoadingSummary = app.name == loadingSummaryForApp,
                        onStartApp = onStartApp,
                        onStopApp = onStopApp,
                        onShowUpgradeSummary = onShowUpgradeSummary,
                        upgradeJobs = upgradeJobs,
                        onShowRollbackDialog = onShowRollbackDialog,
                        onAppInfoClick = onAppInfoClick,
                        isSelected = selectedApp?.id == app.id
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(
                items = apps,
                key = { it.id }
            ) { app ->
                Box(modifier = Modifier.animateItem()) {
                    ServiceCard(
                        app = app,
                        isLoadingSummary = app.name == loadingSummaryForApp,
                        onStartApp = onStartApp,
                        onStopApp = onStopApp,
                        onShowUpgradeSummary = onShowUpgradeSummary,
                        upgradeJobs = upgradeJobs,
                        onShowRollbackDialog = onShowRollbackDialog,
                        onAppInfoClick = onAppInfoClick,
                        isSelected = selectedApp?.id == app.id
                    )
                }
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
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    isSelected: Boolean = false
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp

    // Expressive: Animate border width or color
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "border"
    )

    Card(
        shape = RoundedCornerShape(24.dp), // Expressive: Larger corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardHorizontalPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            // Header Section
            if (isCompact) {
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
                                .size(52.dp) // Expressive: slightly larger
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = app.metadata?.title ?: app.name,
                            style = MaterialTheme.typography.titleMedium, // Expressive: TitleMedium
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "v${app.version}", // Showing Version is often more useful than ID
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusChip(state = app.state, icon = getStatusIcon(app.state))
                }
            } else {
                // Desktop/Tablet Header
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Status dot for dense layout
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getStatusColor(app.state))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = app.metadata?.title ?: app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "v${app.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Update Section
            if (app.upgrade_available || upgradeJobs[app.name] != null) {
                Spacer(modifier = Modifier.height(16.dp))
                // ... (Existing update logic, essentially unchanged visually) ...
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
                        // ... (Existing progress logic) ...
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
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)), // Expressive: Rounder caps
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            if (isCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!app.state.equals("running", ignoreCase = true)) {
                        ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = { onStartApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true, // Stop is also a primary action in context
                            onClick = { onStopApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ActionButton(
                        text = "Details",
                        icon = Icons.Default.Info,
                        enabled = true,
                        isPrimary = false,
                        onClick = { onAppInfoClick(app) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!app.state.equals("running", ignoreCase = true)) {
                        CompactActionButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            isPrimary = true,
                            onClick = { onStartApp(app.name) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
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
                        onClick = { onAppInfoClick(app) },
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

            // More options (Expandable)
            if (isCompact) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showMoreOptions = !showMoreOptions },
                    shape = RoundedCornerShape(16.dp), // Expressive
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                enter = expandVertically(animationSpec = spring()) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring()) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    ActionButton(
                        text = "Rollback Version",
                        icon = Icons.Default.Refresh,
                        enabled = true,
                        isPrimary = false,
                        onClick = { onShowRollbackDialog(app.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
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
    // Expressive: Animate color change on state
    val containerColor by animateColorAsState(
        if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "container"
    )
    val contentColor by animateColorAsState(
        if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp), // Expressive: Rounder
        color = containerColor,
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
                modifier = Modifier.size(24.dp),
                tint = contentColor
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
        shape = RoundedCornerShape(100.dp), // Expressive: Pill shape
        color = MaterialTheme.colorScheme.primaryContainer,
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Update",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UpgradeStatusChip(upgradeState: System.UpgradeJobState) {
    val containerColor = when (upgradeState.state.lowercase()) {
        "success" -> MaterialTheme.colorScheme.primaryContainer
        "failed", "aborted" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(100.dp) // Expressive: Pill shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (upgradeState.state.lowercase()) {
                "success" -> Icon(
                    Icons.Default.CloudUpload,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                "failed", "aborted" -> Icon(
                    Icons.Default.Error,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                else -> CircularProgressIndicator(
                    Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
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
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusChip(state: String, icon: ImageVector) {
    Surface(
        color = getStatusColor(state).copy(alpha = 0.12f),
        shape = RoundedCornerShape(100.dp), // Expressive: Pill shape
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
                fontWeight = FontWeight.Bold
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
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainer
        }, label = "btnColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isPrimary -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }, label = "contentColor"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp), // Expressive
        color = containerColor,
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
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
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