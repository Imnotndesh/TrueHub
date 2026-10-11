@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.imnotndesh.truehub.ui.services.containers

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.helpers.VirtualizationOperation
import com.imnotndesh.truehub.data.helpers.virtualizationOperationLabel
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.ServiceListSkeleton
import com.imnotndesh.truehub.ui.components.floatingNavBottomClearance
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveActionButton
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveFilterBar
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveIconActionButton
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusDot
import com.imnotndesh.truehub.ui.services.containers.details.ContainerInfoPane
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper

enum class ContainerFilterCategory(val label: String) {
    ALL("All"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    OTHER("Other")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainersScreen(
    manager: TrueNASApiManager,
    onNavigateToContainerInfo: (Virt.ContainerResponse) -> Unit = {},
    onCreateContainer: () -> Unit = {},
    viewModel: ContainerScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadContainers()
    }
    val uiState by viewModel.uiState.collectAsState()
    val operations by VirtualizationJobTracker.operations.collectAsState()
    val isCompact = AdaptiveLayoutHelper.isCompact()

    var selectedCategory by remember { mutableStateOf(ContainerFilterCategory.ALL) }
    var selectedContainerForPane by remember { mutableStateOf<Virt.ContainerResponse?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedContainerIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(true) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    BackHandler(isSelectionMode) {
        isSelectionMode = false
        selectedContainerIds = emptySet()
        fabMenuExpanded = false
    }

    val filteredContainers by remember(uiState.containers, selectedCategory) {
        derivedStateOf {
            when (selectedCategory) {
                ContainerFilterCategory.ALL -> uiState.containers
                ContainerFilterCategory.RUNNING -> uiState.containers.filter { it.status == Virt.Status.RUNNING }
                ContainerFilterCategory.STOPPED -> uiState.containers.filter { it.status == Virt.Status.STOPPED }
                ContainerFilterCategory.OTHER -> uiState.containers.filter {
                    it.status != Virt.Status.RUNNING && it.status != Virt.Status.STOPPED
                }
            }
        }
    }

    val selectedContainers = remember(selectedContainerIds, uiState.containers) {
        uiState.containers.filter { it.id in selectedContainerIds }
    }
    val startableContainers = remember(selectedContainers) {
        selectedContainers.filter { it.status == Virt.Status.STOPPED }
    }
    val stoppableContainers = remember(selectedContainers) {
        selectedContainers.filter { it.status == Virt.Status.RUNNING }
    }

    LaunchedEffect(isSelectionMode) {
        if (isSelectionMode) fabMenuExpanded = true
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedContainerIds = emptySet()
        fabMenuExpanded = false
    }

    fun toggleSelection(id: String) {
        selectedContainerIds = if (selectedContainerIds.contains(id)) {
            selectedContainerIds - id
        } else {
            selectedContainerIds + id
        }
    }

    val deletableContainers = remember(selectedContainers) {
        selectedContainers.filter { it.status == Virt.Status.STOPPED }
    }

    if (showDeleteSelectedDialog && deletableContainers.isNotEmpty()) {
        val skipped = selectedContainers.size - deletableContainers.size
        DeleteConfirmationDialog(
            containerName = "${deletableContainers.size} stopped container(s)",
            message = if (skipped > 0) {
                "$skipped running container(s) will be skipped. Delete the ${deletableContainers.size} stopped container(s)? This action cannot be undone."
            } else {
                "Are you sure you want to delete the selected containers? This action cannot be undone."
            },
            onConfirm = {
                showDeleteSelectedDialog = false
                deletableContainers.forEach { viewModel.deleteContainer(it.id) }
                exitSelectionMode()
            },
            onDismiss = { showDeleteSelectedDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Containers",
            subtitle = if (isSelectionMode) {
                "${selectedContainerIds.size} Selected"
            } else {
                "${filteredContainers.size} containers"
            },
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onDismissError = { viewModel.clearError() },
            manager = manager,
        )

        ExpressiveFilterBar(
            items = ContainerFilterCategory.entries,
            selected = selectedCategory,
            label = { it.label },
            onSelect = { selectedCategory = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshContent(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.loadContainers() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        ServiceListSkeleton()
                    }
                    uiState.containers.isEmpty() && !uiState.isLoading -> {
                        EmptyContainerContent()
                    }
                    filteredContainers.isEmpty() && !uiState.isLoading && uiState.containers.isNotEmpty() -> {
                        EmptyFilterContent()
                    }
                    else -> {
                        ContainersContent(
                            containers = filteredContainers,
                            isRefreshing = uiState.isRefreshing,
                            onStartContainer = { id -> viewModel.startContainer(id) },
                            onStopContainer = { id -> viewModel.stopContainer(id) },
                            onRestartContainer = { id -> viewModel.restartContainer(id) },
                            operations = operations,
                            onContainerInfoClick = { container -> onNavigateToContainerInfo(container) },
                            selectedContainer = null,
                            isSelectionMode = isSelectionMode,
                            selectedContainerIds = selectedContainerIds,
                            onToggleSelection = ::toggleSelection,
                            onLongPress = { id ->
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedContainerIds = setOf(id)
                                }
                            }
                        )
                    }
                }
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = floatingNavBottomClearance())
                ) {
                    FloatingActionButtonMenu(
                        expanded = fabMenuExpanded,
                        button = {
                            ToggleFloatingActionButton(
                                checked = fabMenuExpanded,
                                onCheckedChange = { fabMenuExpanded = it }
                            ) {
                                Icon(
                                    imageVector = if (fabMenuExpanded) Icons.Default.Close else Icons.Default.Settings,
                                    contentDescription = if (fabMenuExpanded) "Close menu" else "Selection menu"
                                )
                            }
                        }
                    ) {
                        if (startableContainers.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    startableContainers.forEach { viewModel.startContainer(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                text = { Text("Start (${startableContainers.size})") }
                            )
                        }
                        if (stoppableContainers.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    stoppableContainers.forEach { viewModel.stopContainer(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                                text = { Text("Stop (${stoppableContainers.size})") }
                            )
                        }
                        if (selectedContainers.any { it.status == Virt.Status.STOPPED }) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    showDeleteSelectedDialog = true
                                },
                                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                text = { Text("Delete") }
                            )
                        }
                        if (selectedContainerIds.size < filteredContainers.size) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    selectedContainerIds = filteredContainers.map { it.id }.toSet()
                                },
                                icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                                text = { Text("Select All") }
                            )
                        }
                        if (selectedContainerIds.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    selectedContainerIds = emptySet()
                                },
                                icon = { Icon(Icons.Default.ClearAll, contentDescription = null) },
                                text = { Text("Clear Selection") }
                            )
                        }
                        FloatingActionButtonMenuItem(
                            onClick = { exitSelectionMode() },
                            icon = { Icon(Icons.Default.Close, contentDescription = null) },
                            text = { Text("Cancel") }
                        )
                    }
                }
            } else {
                ExpressiveActionFab(
                    icon = Icons.Default.Add,
                    label = "Create container",
                    onClick = onCreateContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = floatingNavBottomClearance())
                )
            }
        }
    }
}

@Composable
private fun ContainersSplitPaneContent(
    containers: List<Virt.ContainerResponse>,
    selectedContainer: Virt.ContainerResponse?,
    isRefreshing: Boolean,
    onStartContainer: (String) -> Unit,
    onStopContainer: (String) -> Unit,
    onRestartContainer: (String) -> Unit,
    operations: Map<String, VirtualizationOperation>,
    onContainerInfoClick: (Virt.ContainerResponse) -> Unit,
    onContainerPaneSelected: (Virt.ContainerResponse) -> Unit,
    onClosePane: () -> Unit,
    isSelectionMode: Boolean,
    selectedContainerIds: Set<String>,
    onToggleSelection: (String) -> Unit,
    onLongPress: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedContainer != null && !isSelectionMode) 0.55f else 1f)
                .fillMaxSize()
        ) {
            ContainersContent(
                containers = containers,
                isRefreshing = isRefreshing,
                onStartContainer = onStartContainer,
                onStopContainer = onStopContainer,
                onRestartContainer = onRestartContainer,
                operations = operations,
                onContainerInfoClick = onContainerInfoClick,
                selectedContainer = selectedContainer,
                onContainerPaneSelected = onContainerPaneSelected,
                isSelectionMode = isSelectionMode,
                selectedContainerIds = selectedContainerIds,
                onToggleSelection = onToggleSelection,
                onLongPress = onLongPress
            )
        }
        AnimatedVisibility(
            visible = selectedContainer != null && !isSelectionMode,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            selectedContainer?.let { container ->
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    ContainerInfoPane(
                        container = container,
                        onClose = onClosePane,
                        bottomActions = {
                            Button(
                                onClick = { onContainerInfoClick(container) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large
                            ) { Text("Open full details") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContainerContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No containers found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create containers to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyFilterContent() {
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
            text = "No containers in this category",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContainersContent(
    containers: List<Virt.ContainerResponse>,
    isRefreshing: Boolean,
    onStartContainer: (String) -> Unit,
    onStopContainer: (String) -> Unit,
    onRestartContainer: (String) -> Unit,
    onContainerInfoClick: (Virt.ContainerResponse) -> Unit,
    operations: Map<String, VirtualizationOperation>,
    selectedContainer: Virt.ContainerResponse? = null,
    onContainerPaneSelected: ((Virt.ContainerResponse) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    selectedContainerIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onLongPress: (String) -> Unit = {}
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(compact = 1, medium = 2, expanded = 3)
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    if (isCompact) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = contentPadding.dp, end = contentPadding.dp, top = 8.dp, bottom = 8.dp + floatingNavBottomClearance())
        ) {
            items(items = containers, key = { it.id }) { container ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    )
                ) {
                    ContainerCard(
                        container = container,
                        onStartContainer = { onStartContainer(container.id) },
                        onStopContainer = { onStopContainer(container.id) },
                        onRestartContainer = { onRestartContainer(container.id) },
                        operation = operations[container.id],
                        onContainerInfoClicked = { onContainerInfoClick(container) },
                        isSelected = selectedContainer?.id == container.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedContainerIds.contains(container.id),
                        onToggleSelection = { onToggleSelection(container.id) },
                        onLongPress = { onLongPress(container.id) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = contentPadding.dp, end = contentPadding.dp, top = 8.dp, bottom = 8.dp + floatingNavBottomClearance())
        ) {
            items(items = containers, key = { it.id }) { container ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    )
                ) {
                    ContainerCard(
                        container = container,
                        onStartContainer = { onStartContainer(container.id) },
                        onStopContainer = { onStopContainer(container.id) },
                        onRestartContainer = { onRestartContainer(container.id) },
                        operation = operations[container.id],
                        onContainerInfoClicked = {
                            onContainerPaneSelected?.invoke(container) ?: onContainerInfoClick(container)
                        },
                        isSelected = selectedContainer?.id == container.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedContainerIds.contains(container.id),
                        onToggleSelection = { onToggleSelection(container.id) },
                        onLongPress = { onLongPress(container.id) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ContainerCard(
    container: Virt.ContainerResponse,
    onStartContainer: () -> Unit,
    onStopContainer: () -> Unit,
    onRestartContainer: () -> Unit,
    onContainerInfoClicked: (Virt.ContainerResponse) -> Unit,
    operation: VirtualizationOperation? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isChecked: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp
    val tone = containerStatusTone(container.status)
    val isBusy = operation != null
    val isRunning = container.status == Virt.Status.RUNNING

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isSelectionMode && isChecked) 3.dp else if (isSelected) 2.dp else 1.dp,
            color = if ((isSelectionMode && isChecked) || isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardHorizontalPadding)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelection() else onContainerInfoClicked(container)
                },
                onLongClick = onLongPress
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
                        imageVector = Icons.Default.Check,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveShapeBox(
                        shape = containerShape(container.type),
                        containerColor = containerTypeContainerColor(container.type),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = containerTypeIcon(container.type),
                            contentDescription = null,
                            tint = containerTypeContentColor(container.type),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = container.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = container.id.take(12),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ExpressiveStatusChip(
                        label = containerStatusLabel(container.status),
                        tone = tone,
                        icon = containerStatusIcon(container.status)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        ExpressiveShapeBox(
                            shape = containerShape(container.type),
                            containerColor = containerTypeContainerColor(container.type),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = containerTypeIcon(container.type),
                                contentDescription = null,
                                tint = containerTypeContentColor(container.type),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        ExpressiveStatusDot(tone = tone, label = containerStatusLabel(container.status))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = container.id.take(12),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(label = "CPU", value = container.cpu ?: "Default")
                DetailItem(label = "Memory", value = formatMemory(container.memory))
                DetailItem(label = "Autostart", value = if (container.autostart) "Yes" else "No")
            }

            operation?.let { current ->
                Spacer(modifier = Modifier.height(16.dp))
                val job = current.job
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.progress?.description?.takeIf { it.isNotBlank() }
                                ?: virtualizationOperationLabel(current.operation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${job.progress?.percent ?: 0}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (job.progress?.percent?.coerceIn(0, 100)?.toFloat() ?: 0f) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCompact) {
                    ExpressiveActionButton(
                        text = if (isRunning) "Stop" else "Start",
                        icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        enabled = !isBusy,
                        isPrimary = true,
                        onClick = { if (isRunning) onStopContainer() else onStartContainer() },
                        modifier = Modifier.weight(1.4f)
                    )
                } else {
                    ExpressiveIconActionButton(
                        icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Stop" else "Start",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = { if (isRunning) onStopContainer() else onStartContainer() },
                        modifier = Modifier.weight(1f)
                    )
                }
                ExpressiveIconActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "View Info",
                    isPrimary = false,
                    onClick = { onContainerInfoClicked(container) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveIconActionButton(
                    icon = Icons.Default.RestartAlt,
                    contentDescription = "Restart",
                    isPrimary = false,
                    enabled = isRunning && !isBusy,
                    onClick = onRestartContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    containerName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String = "Are you sure you want to delete '$containerName'? This action cannot be undone."
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Container") },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
