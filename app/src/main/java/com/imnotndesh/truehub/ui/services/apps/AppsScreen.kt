package com.imnotndesh.truehub.ui.services.apps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.canUpgradeNow
import com.imnotndesh.truehub.data.models.isAsleep
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppInfoPane
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppsScreen(
    manager: TrueNASApiManager,
    onNavigateToAppInfo: (Apps.AppQueryResponse) -> Unit = {},
    onNavigateToUpgrade: (String) -> Unit,
    onNavigateToRollback: (String) -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    onSearchClick: (() -> Unit)? = null
) {
    val appsScreenViewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    val uiState by appsScreenViewModel.uiState.collectAsState()
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeJobs by JobRepository.activeJobs.collectAsState()

    // Selection state for multiple app selection
    var selectedAppIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showUpdateAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(true) } // Start expanded when entering mode

    var selectedAppForInfo by remember { mutableStateOf<Apps.AppQueryResponse?>(null) }

    val upgradableAppsCount = remember(uiState.apps, activeJobs) {
        uiState.apps.count { app ->
            app.upgrade_available &&
                    activeJobs.values.none { it.appName == app.name && it.type == "UPGRADE" && it.state !in listOf("SUCCESS", "FAILED", "ABORTED") }
        }
    }

    // Apps that can be upgraded right now (in a non-stopped state).
    val updatableAppsCount = remember(uiState.apps, activeJobs) {
        uiState.apps.count { app ->
            app.canUpgradeNow() &&
                    activeJobs.values.none { it.appName == app.name && it.type == "UPGRADE" && it.state !in listOf("SUCCESS", "FAILED", "ABORTED") }
        }
    }

    // Apps that have updates but are asleep and would need to be started first.
    val sleepingUpdatableAppsCount = remember(uiState.apps, activeJobs) {
        uiState.apps.count { app ->
            app.upgrade_available && app.isAsleep() &&
                    activeJobs.values.none { it.appName == app.name && it.type == "UPGRADE" && it.state !in listOf("SUCCESS", "FAILED", "ABORTED") }
        }
    }

    var isUpdatingAll by remember { mutableStateOf(false) }
    var wakeStoppedBeforeUpdate by remember { mutableStateOf(true) }

    // Handle back button when in selection mode
    BackHandler(isSelectionMode) {
        isSelectionMode = false
        selectedAppIds = emptySet()
        fabMenuExpanded = false
    }

    LaunchedEffect(activeJobs, isUpdatingAll) {
        if (isUpdatingAll) {
            val activeUpgrades = activeJobs.values.count {
                it.type == "UPGRADE" &&
                        it.state !in listOf("SUCCESS", "FAILED", "ABORTED")
            }

            if (activeUpgrades == 0) {
                delay(1000.milliseconds)
                isUpdatingAll = false
                appsScreenViewModel.refresh()
            }
        }
    }

    LaunchedEffect(isSelectionMode) {
        if (isSelectionMode) {
            fabMenuExpanded = true
        }
    }

    val selectedAppsForDeletion = remember(selectedAppIds, uiState.apps) {
        uiState.apps.filter { it.id in selectedAppIds }
    }

    val selectedAppsForUpdate = remember(selectedAppIds, uiState.apps) {
        uiState.apps.filter { it.id in selectedAppIds && it.canUpgradeNow() }
    }

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

    // Update All Confirmation Dialog
    if (showUpdateAllDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateAllDialog = false },
            title = { Text("Update All Applications") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "There ${
                            if (upgradableAppsCount == 1) "is" else "are"
                        } $upgradableAppsCount update(s) available in total."
                    )

                    if (updatableAppsCount > 0) {
                        Text(
                            "• $updatableAppsCount running app(s) will be updated to their latest versions.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (sleepingUpdatableAppsCount > 0) {
                        Text(
                            "• $sleepingUpdatableAppsCount sleeping app(s) cannot be updated while powered off.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "All apps with updates are running, so none need to be started first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (sleepingUpdatableAppsCount > 0) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { wakeStoppedBeforeUpdate = !wakeStoppedBeforeUpdate }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = wakeStoppedBeforeUpdate,
                                onCheckedChange = { wakeStoppedBeforeUpdate = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Start sleeping apps before updating",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Wakes all powered-off apps with updates, waits for them to start, then updates them too.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        "You can monitor progress in each app card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateAllDialog = false
                        isUpdatingAll = true
                        appsScreenViewModel.upgradeAllApps(context, wakeStoppedBeforeUpdate)
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedAppIds = emptySet()
                        }
                    }
                ) {
                    Text(
                        if (wakeStoppedBeforeUpdate && sleepingUpdatableAppsCount > 0) {
                            "Start & Update All"
                        } else {
                            "Update Running Apps"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Selected Confirmation Dialog
    if (showDeleteSelectedDialog && selectedAppsForDeletion.isNotEmpty()) {
        DeleteAppsConfirmationDialog(
            appNames = selectedAppsForDeletion.map { it.metadata?.title ?: it.name },
            onConfirm = { options ->
                showDeleteSelectedDialog = false
                selectedAppsForDeletion.forEach { app ->
                    appsScreenViewModel.deleteApp(context, app.name, options)
                }
                isSelectionMode = false
                selectedAppIds = emptySet()
                fabMenuExpanded = false
            },
            onDismiss = { showDeleteSelectedDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Applications",
            subtitle = if (isSelectionMode) "${selectedAppIds.size} Selected" else "${filteredApps.size} Applications",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onDismissError = { appsScreenViewModel.clearError() },
            manager = manager,
            onSearchClick = onSearchClick,
            trailingActions = {
                // Only Marketplace button in header
                IconButton(
                    onClick = { onNavigateToMarketplace() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Open App Marketplace"
                    )
                }
            }
        )

        AppFilterBar(
            currentCategory = uiState.selectedCategory,
            onCategorySelected = { appsScreenViewModel.updateCategory(it) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PullToRefreshContent(
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isCompact) {
                            AppsContent(
                                apps = filteredApps,
                                onStartApp = { appName -> appsScreenViewModel.startApp(appName) },
                                onStopApp = { appName -> appsScreenViewModel.stopApp(appName) },
                                onShowUpgradeSummary = { appName -> onNavigateToUpgrade(appName) },
                                onRollbackClick = { onNavigateToRollback(it) },
                                onAppInfoClick = { app -> onNavigateToAppInfo(app) },
                                selectedApp = null,
                                loadingSummaryForApp = uiState.isLoadingUpgradeSummaryForApp,
                                isSelectionMode = isSelectionMode,
                                selectedAppIds = selectedAppIds,
                                onToggleSelection = { appId ->
                                    selectedAppIds = if (selectedAppIds.contains(appId)) {
                                        selectedAppIds - appId
                                    } else {
                                        selectedAppIds + appId
                                    }
                                },
                                onLongPress = { appId ->
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedAppIds = setOf(appId)
                                    }
                                }
                            )
                        } else {
                            AppsSplitPaneContent(
                                apps = filteredApps,
                                selectedApp = selectedAppForInfo,
                                onStartApp = { appName -> appsScreenViewModel.startApp(appName) },
                                loadingSummaryForApp = uiState.isLoadingUpgradeSummaryForApp,
                                onStopApp = { appName -> appsScreenViewModel.stopApp(appName) },
                                onShowUpgradeSummary = { appName -> onNavigateToUpgrade(appName) },
                                onShowRollbackDialog = { appName -> onNavigateToRollback(appName) },
                                onAppInfoClick = { app ->
                                    if (isSelectionMode) {
                                        selectedAppIds = if (selectedAppIds.contains(app.id)) {
                                            selectedAppIds - app.id
                                        } else {
                                            selectedAppIds + app.id
                                        }
                                    } else {
                                        selectedAppForInfo = app
                                    }
                                },
                                onCloseInfoPane = { selectedAppForInfo = null },
                                isSelectionMode = isSelectionMode,
                                selectedAppIds = selectedAppIds,
                                onToggleSelection = { appId ->
                                    selectedAppIds = if (selectedAppIds.contains(appId)) {
                                        selectedAppIds - appId
                                    } else {
                                        selectedAppIds + appId
                                    }
                                },
                                onLongPress = { appId ->
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedAppIds = setOf(appId)
                                    }
                                }
                            )
                        }

                        if (isSelectionMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                FloatingActionButtonMenu(
                                    expanded = fabMenuExpanded,
                                    button = {
                                        ToggleFloatingActionButton(
                                            checked = fabMenuExpanded,
                                            onCheckedChange = { isChecked -> fabMenuExpanded = isChecked }
                                        ) {
                                            Icon(
                                                imageVector = if (fabMenuExpanded) Icons.Default.Close else Icons.Default.Settings,
                                                contentDescription = if (fabMenuExpanded) "Close menu" else "Selection Menu"
                                            )
                                        }
                                    }
                                ) {
                                    if (selectedAppsForUpdate.isNotEmpty()) {
                                        FloatingActionButtonMenuItem(
                                            onClick = {
                                                fabMenuExpanded = false
                                                selectedAppsForUpdate.forEach { app ->
                                                    appsScreenViewModel.upgradeApp(app.name, context, "latest", true)
                                                }
                                                coroutineScope.launch {
                                                    delay(500.milliseconds)
                                                    isSelectionMode = false
                                                    selectedAppIds = emptySet()
                                                }
                                            },
                                            icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) },
                                            text = { Text("Update (${selectedAppsForUpdate.size})") }
                                        )
                                    }

                                    if (selectedAppsForDeletion.isNotEmpty()) {
                                        FloatingActionButtonMenuItem(
                                            onClick = {
                                                fabMenuExpanded = false
                                                showDeleteSelectedDialog = true
                                            },
                                            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                            text = { Text("Delete (${selectedAppsForDeletion.size})") }
                                        )
                                    }

                                    if (selectedAppIds.size < filteredApps.size) {
                                        FloatingActionButtonMenuItem(
                                            onClick = {
                                                fabMenuExpanded = false
                                                selectedAppIds = filteredApps.map { it.id }.toSet()
                                            },
                                            icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                                            text = { Text("Select All") }
                                        )
                                    }

                                    if (selectedAppIds.isNotEmpty()) {
                                        FloatingActionButtonMenuItem(
                                            onClick = {
                                                fabMenuExpanded = false
                                                selectedAppIds = emptySet()
                                            },
                                            icon = { Icon(Icons.Default.ClearAll, contentDescription = null) },
                                            text = { Text("Clear Selection") }
                                        )
                                    }

                                    FloatingActionButtonMenuItem(
                                        onClick = {
                                            fabMenuExpanded = false
                                            isSelectionMode = false
                                            selectedAppIds = emptySet()
                                        },
                                        icon = { Icon(Icons.Default.Close, contentDescription = null) },
                                        text = { Text("Cancel") }
                                    )
                                }
                            }
                        } else {
                            if (isUpdatingAll) {
                                FloatingActionButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else if (upgradableAppsCount > 0) {
                                FloatingActionButton(
                                    onClick = { showUpdateAllDialog = true },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Box {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = "Update All Apps"
                                        )
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Text(
                                                text = if (upgradableAppsCount > 99) "99+" else upgradableAppsCount.toString(),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
                shape = RoundedCornerShape(12.dp)
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
    onShowRollbackDialog: (String) -> Unit,
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    onCloseInfoPane: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedAppIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onLongPress: (String) -> Unit = {}
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedApp != null && !isSelectionMode) 0.55f else 1f)
                .fillMaxSize()
        ) {
            AppsContent(
                apps = apps,
                onStartApp = onStartApp,
                loadingSummaryForApp = loadingSummaryForApp,
                onStopApp = onStopApp,
                onShowUpgradeSummary = onShowUpgradeSummary,
                onRollbackClick = onShowRollbackDialog,
                onAppInfoClick = onAppInfoClick,
                selectedApp = selectedApp,
                isSelectionMode = isSelectionMode,
                selectedAppIds = selectedAppIds,
                onToggleSelection = onToggleSelection,
                onLongPress = onLongPress
            )
        }
        AnimatedVisibility(
            visible = selectedApp != null && !isSelectionMode,
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
    onRollbackClick: (String) -> Unit,
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    selectedApp: Apps.AppQueryResponse?,
    isSelectionMode: Boolean = false,
    selectedAppIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onLongPress: (String) -> Unit = {}
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(
        compact = 1,
        medium = 2,
        expanded = 3
    )
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    if (isCompact) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        onRollbackClick = onRollbackClick,
                        onAppInfoClick = onAppInfoClick,
                        isSelected = selectedApp?.id == app.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedAppIds.contains(app.id),
                        onToggleSelection = { onToggleSelection(app.id) },
                        onLongPress = { onLongPress(app.id) }
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
                        onShowUpgradeSummary = { appName -> onShowUpgradeSummary(appName)},
                        onRollbackClick = onRollbackClick,
                        onAppInfoClick = onAppInfoClick,
                        isSelected = selectedApp?.id == app.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedAppIds.contains(app.id),
                        onToggleSelection = { onToggleSelection(app.id) },
                        onLongPress = { onLongPress(app.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ServiceCard(
    app: Apps.AppQueryResponse,
    isLoadingSummary: Boolean,
    onStartApp: (String) -> Unit,
    onStopApp: (String) -> Unit,
    onShowUpgradeSummary: (String) -> Unit,
    onRollbackClick: (String) -> Unit,
    onAppInfoClick: (Apps.AppQueryResponse) -> Unit,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isChecked: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeJobs by JobRepository.activeJobs.collectAsState()
    val persistentJob = activeJobs.values.find { it.appName == app.name }

    var showMoreOptions by remember { mutableStateOf(false) }
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(

            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelectionMode && isChecked) 3.dp else if (isSelected) 2.dp else 0.dp,
            color = if (isSelectionMode && isChecked || isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardHorizontalPadding)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else {
                        onAppInfoClick(app)
                    }
                },
                onLongClick = {
                    onLongPress()
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            if (isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Check,
                        contentDescription = if (isChecked) "Selected" else "Not selected",
                        tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (isCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!app.metadata?.icon.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(app.metadata.icon)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "${app.metadata.title ?: app.name} icon",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit,
                            placeholder = rememberVectorPainter(Icons.Default.Apps),
                            error = rememberVectorPainter(Icons.Default.Apps)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.metadata?.title ?: app.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "v${app.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(state = app.state, icon = getStatusIcon(app.state))
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!app.metadata?.icon.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(app.metadata.icon)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${app.metadata.title ?: app.name} icon",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Fit,
                                    placeholder = rememberVectorPainter(Icons.Default.Apps),
                                    error = rememberVectorPainter(Icons.Default.Apps)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(getStatusColor(app.state))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = app.metadata?.title ?: app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "v${app.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val isJobRunning = persistentJob != null

            if (app.upgrade_available || isJobRunning) {
                Spacer(modifier = Modifier.height(16.dp))
                if (!isJobRunning) {
                    val canUpgrade = app.canUpgradeNow()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Update available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (canUpgrade) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Medium
                            )
                            if (!canUpgrade) {
                                Text(
                                    text = "Start the app to update",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        UpgradeButton(
                            onClick = { if (canUpgrade) onShowUpgradeSummary(app.name) },
                            isLoading = isLoadingSummary,
                            enabled = canUpgrade
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isJobRunning,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    persistentJob?.let { job ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = job.description ?: "Upgrading...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${job.progress}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { job.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isRunning = app.state.equals("running", ignoreCase = true)
                val primaryActionLabel = if (isRunning) "Stop" else "Start"
                val primaryActionIcon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow

                if (isCompact) {
                    ActionButton(
                        text = primaryActionLabel,
                        icon = primaryActionIcon,
                        isPrimary = true,
                        onClick = { if (isRunning) onStopApp(app.name) else onStartApp(app.name) },
                        modifier = Modifier.weight(1f),
                        enabled = true
                    )
                    ActionButton(
                        text = "Details",
                        icon = Icons.Default.Info,
                        isPrimary = false,
                        onClick = { onAppInfoClick(app) },
                        modifier = Modifier.weight(1f),
                        enabled = true
                    )
                } else {
                    CompactActionButton(
                        icon = primaryActionIcon,
                        contentDescription = primaryActionLabel,
                        isPrimary = true,
                        onClick = { if (isRunning) onStopApp(app.name) else onStartApp(app.name) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactActionButton(
                        icon = Icons.Default.Info,
                        contentDescription = "View Info",
                        isPrimary = false,
                        onClick = { onAppInfoClick(app) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactActionButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "More",
                        isPrimary = false,
                        onClick = { showMoreOptions = !showMoreOptions },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isCompact) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showMoreOptions = !showMoreOptions },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, null, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("More Options", style = MaterialTheme.typography.labelLarge)
                        }
                        Icon(
                            imageVector = if (showMoreOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showMoreOptions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    ActionButton(
                        text = "Rollback Version",
                        icon = Icons.Default.Refresh,
                        isPrimary = false,
                        onClick = { onRollbackClick(app.name) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
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
        shape = RoundedCornerShape(16.dp),
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
    isLoading: Boolean,
    enabled: Boolean = true
) {
    val container = if (enabled && !isLoading) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = if (enabled && !isLoading) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = container,
        enabled = enabled && !isLoading
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
                    tint = content
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Update",
                style = MaterialTheme.typography.labelMedium,
                color = content,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusChip(state: String, icon: ImageVector) {
    Surface(
        color = getStatusColor(state).copy(alpha = 0.12f),
        shape = RoundedCornerShape(100.dp),
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
        shape = RoundedCornerShape(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorphingRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean
) {
    val scale = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1f)
    val cornerSize = (16 + (34 * scale)).dp
    val rotation = state.distanceFraction * 180f

    if (state.distanceFraction > 0.1f || isRefreshing) {
        Surface(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .scale(scale)
                .graphicsLayer {
                    rotationZ = if (isRefreshing) 0f else rotation
                },
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(cornerSize),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteAppsConfirmationDialog(
    appNames: List<String>,
    onConfirm: (Apps.DeleteAppOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var removeImages by remember { mutableStateOf(true) }
    var removeIxVolumes by remember { mutableStateOf(false) }
    var forceRemoveIxVolumes by remember { mutableStateOf(false) }
    var forceRemoveCustomApp by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Uninstall ${appNames.size} Application(s)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to uninstall:",
                    style = MaterialTheme.typography.bodyMedium
                )
                appNames.take(5).forEach { name ->
                    Text(
                        text = "• $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (appNames.size > 5) {
                    Text(
                        text = "...and ${appNames.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()

                DeleteOptionToggle(
                    title = "Remove Docker/Container Images",
                    description = "Wipes cached containers from the system storage pool if no other apps rely on them.",
                    checked = removeImages,
                    onCheckedChange = { removeImages = it }
                )
                DeleteOptionToggle(
                    title = "Delete ixVolumes Data",
                    description = "Permanently deletes application persistent datasets.",
                    checked = removeIxVolumes,
                    onCheckedChange = { removeIxVolumes = it }
                )
                DeleteOptionToggle(
                    title = "Force Remove ixVolumes Data",
                    description = "Forces data dataset destruction even if busy.",
                    checked = forceRemoveIxVolumes,
                    onCheckedChange = { forceRemoveIxVolumes = it }
                )
                DeleteOptionToggle(
                    title = "Force Remove Custom App Context",
                    description = "Overrides safety validations for custom charts.",
                    checked = forceRemoveCustomApp,
                    onCheckedChange = { forceRemoveCustomApp = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Apps.DeleteAppOptions(
                            remove_images = removeImages,
                            remove_ix_volumes = removeIxVolumes,
                            force_remove_ix_volumes = forceRemoveIxVolumes,
                            force_remove_custom_app = forceRemoveCustomApp
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Uninstall Selected")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteOptionToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onError,
                checkedTrackColor = MaterialTheme.colorScheme.error
            )
        )
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