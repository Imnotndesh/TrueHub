@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.imnotndesh.truehub.ui.services.vm

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
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
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveFilterBar
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveIconActionButton
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusDot
import com.imnotndesh.truehub.ui.services.vm.details.VmInfoPane
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper

enum class VmFilterCategory(val label: String) {
    ALL("All"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    SUSPENDED("Suspended")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmsScreen(
    manager: TrueNASApiManager,
    onNavigateToVmInfo: (Vm.VmQueryResponse) -> Unit = {},
    onCreateVm: () -> Unit = {},
    onSearchClick: (() -> Unit)? = null,
    viewModel: VmsScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadVms()
    }
    val uiState by viewModel.uiState.collectAsState()
    val operations by VirtualizationJobTracker.operations.collectAsState()
    val isCompact = AdaptiveLayoutHelper.isCompact()

    var selectedCategory by remember { mutableStateOf(VmFilterCategory.ALL) }
    var selectedVmForPane by remember { mutableStateOf<Vm.VmQueryResponse?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedVmIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(true) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    BackHandler(isSelectionMode) {
        isSelectionMode = false
        selectedVmIds = emptySet()
        fabMenuExpanded = false
    }

    val filteredVms by remember(uiState.vms, selectedCategory) {
        derivedStateOf {
            when (selectedCategory) {
                VmFilterCategory.ALL -> uiState.vms
                VmFilterCategory.RUNNING -> uiState.vms.filter { it.status.state.equals("running", true) }
                VmFilterCategory.STOPPED -> uiState.vms.filter { it.status.state.equals("stopped", true) }
                VmFilterCategory.SUSPENDED -> uiState.vms.filter { it.status.state.equals("suspended", true) }
            }
        }
    }

    val selectedVms = remember(selectedVmIds, uiState.vms) {
        uiState.vms.filter { it.id in selectedVmIds }
    }
    val startableVms = remember(selectedVms) { selectedVms.filter { it.status.state.equals("stopped", true) } }
    val stoppableVms = remember(selectedVms) { selectedVms.filter { it.status.state.equals("running", true) } }
    val resumableVms = remember(selectedVms) { selectedVms.filter { it.status.state.equals("suspended", true) } }

    LaunchedEffect(isSelectionMode) {
        if (isSelectionMode) fabMenuExpanded = true
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedVmIds = emptySet()
        fabMenuExpanded = false
    }

    fun toggleSelection(id: Int) {
        selectedVmIds = if (selectedVmIds.contains(id)) selectedVmIds - id else selectedVmIds + id
    }

    if (showDeleteSelectedDialog && selectedVms.isNotEmpty()) {
        DeleteVmConfirmationDialog(
            vmName = "${selectedVms.size} selected VM(s)",
            onConfirm = { deleteZvols, forceDelete ->
                showDeleteSelectedDialog = false
                selectedVms
                    .filter { it.status.state.equals("stopped", true) }
                    .forEach { viewModel.deleteVm(it.id, deleteZvols, forceDelete) }
                exitSelectionMode()
            },
            onDismiss = { showDeleteSelectedDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Virtual Machines",
            subtitle = if (isSelectionMode) "${selectedVmIds.size} Selected" else "${filteredVms.size} VMs",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onDismissError = { viewModel.clearError() },
            manager = manager,
            onSearchClick = onSearchClick
        )

        ExpressiveFilterBar(
            items = VmFilterCategory.entries,
            selected = selectedCategory,
            label = { it.label },
            onSelect = { selectedCategory = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshContent(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.vms.isEmpty() && uiState.isLoading -> LoadingScreen("Loading Virtual Machines")
                    uiState.vms.isEmpty() && !uiState.isLoading -> EmptyVmContent()
                    filteredVms.isEmpty() && !uiState.isLoading && uiState.vms.isNotEmpty() -> EmptyVmFilterContent()
                    else -> {
                        if (isCompact) {
                            VmsContent(
                                vms = filteredVms,
                                onStartVm = { id, overcommit -> viewModel.startVm(id, overcommit) },
                                onStopVm = { id, force, timeout -> viewModel.stopVm(id, force, timeout) },
                                onRestartVm = { id -> viewModel.restartVm(id) },
                                onSuspendVm = { id -> viewModel.suspendVm(id) },
                                onResumeVm = { id -> viewModel.resumeVm(id) },
                                onPowerOffVm = { id -> viewModel.powerOffVm(id) },
                                onDeleteVm = { id, zvols, force -> viewModel.deleteVm(id, zvols, force) },
                                operations = operations,
                                onVmInfoClick = onNavigateToVmInfo,
                                selectedVm = null,
                                isSelectionMode = isSelectionMode,
                                selectedVmIds = selectedVmIds,
                                onToggleSelection = ::toggleSelection,
                                onLongPress = { id ->
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedVmIds = setOf(id)
                                    }
                                }
                            )
                        } else {
                            VmsSplitPaneContent(
                                vms = filteredVms,
                                selectedVm = selectedVmForPane,
                                onStartVm = { id, overcommit -> viewModel.startVm(id, overcommit) },
                                onStopVm = { id, force, timeout -> viewModel.stopVm(id, force, timeout) },
                                onRestartVm = { id -> viewModel.restartVm(id) },
                                onSuspendVm = { id -> viewModel.suspendVm(id) },
                                onResumeVm = { id -> viewModel.resumeVm(id) },
                                onPowerOffVm = { id -> viewModel.powerOffVm(id) },
                                onDeleteVm = { id, zvols, force -> viewModel.deleteVm(id, zvols, force) },
                                operations = operations,
                                onVmInfoClick = onNavigateToVmInfo,
                                onVmPaneSelected = { selectedVmForPane = it },
                                onClosePane = { selectedVmForPane = null },
                                isSelectionMode = isSelectionMode,
                                selectedVmIds = selectedVmIds,
                                onToggleSelection = ::toggleSelection,
                                onLongPress = { id ->
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedVmIds = setOf(id)
                                    }
                                }
                            )
                        }
                    }
                }
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
                                onCheckedChange = { fabMenuExpanded = it }
                            ) {
                                Icon(
                                    imageVector = if (fabMenuExpanded) Icons.Default.Close else Icons.Default.Settings,
                                    contentDescription = if (fabMenuExpanded) "Close menu" else "Selection menu"
                                )
                            }
                        }
                    ) {
                        if (startableVms.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    startableVms.forEach { viewModel.startVm(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                text = { Text("Start (${startableVms.size})") }
                            )
                        }
                        if (stoppableVms.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    stoppableVms.forEach { viewModel.stopVm(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                                text = { Text("Stop (${stoppableVms.size})") }
                            )
                        }
                        if (resumableVms.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    resumableVms.forEach { viewModel.resumeVm(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                text = { Text("Resume (${resumableVms.size})") }
                            )
                        }
                        if (stoppableVms.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    stoppableVms.forEach { viewModel.suspendVm(it.id) }
                                    exitSelectionMode()
                                },
                                icon = { Icon(Icons.Default.Pause, contentDescription = null) },
                                text = { Text("Suspend (${stoppableVms.size})") }
                            )
                        }
                        if (selectedVms.any { it.status.state.equals("stopped", true) }) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    showDeleteSelectedDialog = true
                                },
                                icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                text = { Text("Delete") }
                            )
                        }
                        if (selectedVmIds.size < filteredVms.size) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    selectedVmIds = filteredVms.map { it.id }.toSet()
                                },
                                icon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                                text = { Text("Select All") }
                            )
                        }
                        if (selectedVmIds.isNotEmpty()) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    fabMenuExpanded = false
                                    selectedVmIds = emptySet()
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
                    label = "Create VM",
                    onClick = onCreateVm,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun VmsSplitPaneContent(
    vms: List<Vm.VmQueryResponse>,
    selectedVm: Vm.VmQueryResponse?,
    onStartVm: (Int, Boolean) -> Unit,
    onStopVm: (Int, Boolean, Boolean) -> Unit,
    onRestartVm: (Int) -> Unit,
    onSuspendVm: (Int) -> Unit,
    onResumeVm: (Int) -> Unit,
    onPowerOffVm: (Int) -> Unit,
    onDeleteVm: (Int, Boolean, Boolean) -> Unit,
    operations: Map<String, VirtualizationOperation>,
    onVmInfoClick: (Vm.VmQueryResponse) -> Unit,
    onVmPaneSelected: (Vm.VmQueryResponse) -> Unit,
    onClosePane: () -> Unit,
    isSelectionMode: Boolean,
    selectedVmIds: Set<Int>,
    onToggleSelection: (Int) -> Unit,
    onLongPress: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedVm != null && !isSelectionMode) 0.55f else 1f)
                .fillMaxSize()
        ) {
            VmsContent(
                vms = vms,
                onStartVm = onStartVm,
                onStopVm = onStopVm,
                onRestartVm = onRestartVm,
                onSuspendVm = onSuspendVm,
                onResumeVm = onResumeVm,
                onPowerOffVm = onPowerOffVm,
                onDeleteVm = onDeleteVm,
                operations = operations,
                onVmInfoClick = onVmInfoClick,
                selectedVm = selectedVm,
                onVmPaneSelected = onVmPaneSelected,
                isSelectionMode = isSelectionMode,
                selectedVmIds = selectedVmIds,
                onToggleSelection = onToggleSelection,
                onLongPress = onLongPress
            )
        }
        AnimatedVisibility(
            visible = selectedVm != null && !isSelectionMode,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            selectedVm?.let { vm ->
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    VmInfoPane(vm = vm, onClose = onClosePane)
                }
            }
        }
    }
}

@Composable
private fun EmptyVmContent() {
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
            text = "No virtual machines found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create VMs to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyVmFilterContent() {
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
            text = "No VMs in this category",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VmsContent(
    vms: List<Vm.VmQueryResponse>,
    onStartVm: (Int, Boolean) -> Unit,
    onStopVm: (Int, Boolean, Boolean) -> Unit,
    onRestartVm: (Int) -> Unit,
    onSuspendVm: (Int) -> Unit,
    onResumeVm: (Int) -> Unit,
    onPowerOffVm: (Int) -> Unit,
    onDeleteVm: (Int, Boolean, Boolean) -> Unit,
    operations: Map<String, VirtualizationOperation>,
    onVmInfoClick: (Vm.VmQueryResponse) -> Unit,
    selectedVm: Vm.VmQueryResponse? = null,
    onVmPaneSelected: ((Vm.VmQueryResponse) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    selectedVmIds: Set<Int> = emptySet(),
    onToggleSelection: (Int) -> Unit = {},
    onLongPress: (Int) -> Unit = {}
) {
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val columnCount = AdaptiveLayoutHelper.getColumnCount(compact = 1, medium = 2, expanded = 3)
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()
    val horizontalSpacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    if (isCompact) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(items = vms, key = { it.id }) { vm ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    )
                ) {
                    VmCard(
                        vm = vm,
                        onStartVm = { overcommit -> onStartVm(vm.id, overcommit) },
                        onStopVm = { force, timeout -> onStopVm(vm.id, force, timeout) },
                        onRestartVm = { onRestartVm(vm.id) },
                        onSuspendVm = { onSuspendVm(vm.id) },
                        onResumeVm = { onResumeVm(vm.id) },
                        onPowerOffVm = { onPowerOffVm(vm.id) },
                        onDeleteVm = { zvols, force -> onDeleteVm(vm.id, zvols, force) },
                        operation = operations[vm.id.toString()],
                        onVmInfoClick = { onVmInfoClick(vm) },
                        isSelected = selectedVm?.id == vm.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedVmIds.contains(vm.id),
                        onToggleSelection = { onToggleSelection(vm.id) },
                        onLongPress = { onLongPress(vm.id) }
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
            contentPadding = PaddingValues(horizontal = contentPadding.dp, vertical = 8.dp)
        ) {
            items(items = vms, key = { it.id }) { vm ->
                Box(
                    modifier = Modifier.animateItem(
                        fadeInSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
                        placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    )
                ) {
                    VmCard(
                        vm = vm,
                        onStartVm = { overcommit -> onStartVm(vm.id, overcommit) },
                        onStopVm = { force, timeout -> onStopVm(vm.id, force, timeout) },
                        onRestartVm = { onRestartVm(vm.id) },
                        onSuspendVm = { onSuspendVm(vm.id) },
                        onResumeVm = { onResumeVm(vm.id) },
                        onPowerOffVm = { onPowerOffVm(vm.id) },
                        onDeleteVm = { zvols, force -> onDeleteVm(vm.id, zvols, force) },
                        operation = operations[vm.id.toString()],
                        onVmInfoClick = { onVmPaneSelected?.invoke(vm) ?: onVmInfoClick(vm) },
                        isSelected = selectedVm?.id == vm.id,
                        isSelectionMode = isSelectionMode,
                        isChecked = selectedVmIds.contains(vm.id),
                        onToggleSelection = { onToggleSelection(vm.id) },
                        onLongPress = { onLongPress(vm.id) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun VmCard(
    vm: Vm.VmQueryResponse,
    onStartVm: (Boolean) -> Unit,
    onStopVm: (Boolean, Boolean) -> Unit,
    onRestartVm: () -> Unit,
    onSuspendVm: () -> Unit,
    onResumeVm: () -> Unit,
    onPowerOffVm: () -> Unit,
    onDeleteVm: (Boolean, Boolean) -> Unit,
    onVmInfoClick: (Vm.VmQueryResponse) -> Unit,
    operation: VirtualizationOperation? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isChecked: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp
    val state = vm.status.state.lowercase()
    val isBusy = operation != null
    val tone = vmStatusTone(vm.status.state)

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
                onClick = { if (isSelectionMode) onToggleSelection() else onVmInfoClick(vm) },
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
                        shape = vmShape(),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vm.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = vm.description.ifEmpty { "ID: ${vm.id}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ExpressiveStatusChip(
                        label = vmStatusLabel(vm.status.state),
                        tone = tone,
                        icon = vmStatusIcon(vm.status.state)
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
                            shape = vmShape(),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        ExpressiveStatusDot(tone = tone)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = vm.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vm.description.ifEmpty { "ID: ${vm.id}" },
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
                VmDetailItem(label = "vCPUs", value = vm.vcpus.toString())
                VmDetailItem(label = "Cores", value = vm.cores.toString())
                VmDetailItem(label = "Memory", value = "${vm.memory} MB")
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
                when (state) {
                    "running" -> ExpressiveIconActionButton(
                        icon = Icons.Default.Stop,
                        contentDescription = "Stop",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = { showStopDialog = true },
                        modifier = Modifier.weight(if (isCompact) 1.4f else 1f)
                    )
                    "suspended" -> ExpressiveIconActionButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = onResumeVm,
                        modifier = Modifier.weight(if (isCompact) 1.4f else 1f)
                    )
                    else -> ExpressiveIconActionButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = { showStartDialog = true },
                        modifier = Modifier.weight(if (isCompact) 1.4f else 1f)
                    )
                }
                ExpressiveIconActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "View Info",
                    isPrimary = false,
                    onClick = { onVmInfoClick(vm) },
                    modifier = Modifier.weight(1f)
                )
                ExpressiveIconActionButton(
                    icon = Icons.Default.RestartAlt,
                    contentDescription = "Restart",
                    isPrimary = false,
                    enabled = state == "running",
                    onClick = onRestartVm,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state == "running" || state == "stopped") {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.Surface(
                    onClick = { showMoreOptions = !showMoreOptions },
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                text = "More options",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showMoreOptions) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showMoreOptions,
                enter = androidx.compose.animation.expandVertically(
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                ) + fadeIn(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = androidx.compose.animation.shrinkVertically(
                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
                ) + fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state == "running") {
                        ExpressiveIconActionButton(
                            icon = Icons.Default.Pause,
                            contentDescription = "Suspend",
                            isPrimary = false,
                            enabled = !isBusy,
                            onClick = onSuspendVm,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ExpressiveIconActionButton(
                            icon = Icons.Default.PowerOff,
                            contentDescription = "Power Off",
                            isPrimary = false,
                            enabled = !isBusy,
                            onClick = onPowerOffVm,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (state == "stopped") {
                        ExpressiveIconActionButton(
                            icon = Icons.Default.Delete,
                            contentDescription = "Delete",
                            isPrimary = false,
                            isDanger = true,
                            enabled = !isBusy,
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showStartDialog) {
        StartVmConfirmationDialog(
            vmName = vm.name,
            onConfirm = { overcommit ->
                onStartVm(overcommit)
                showStartDialog = false
            },
            onDismiss = { showStartDialog = false }
        )
    }
    if (showStopDialog) {
        StopVmConfirmationDialog(
            vmName = vm.name,
            onConfirm = { force, forceAfterTimeout ->
                onStopVm(force, forceAfterTimeout)
                showStopDialog = false
            },
            onDismiss = { showStopDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteVmConfirmationDialog(
            vmName = vm.name,
            onConfirm = { zvols, force ->
                onDeleteVm(zvols, force)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun VmDetailItem(label: String, value: String) {
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
private fun StartVmConfirmationDialog(
    vmName: String,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var overcommit by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start $vmName?") },
        text = {
            Column {
                Text("The VM will boot with its configured settings.")
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Overcommit memory", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "Allow starting even if memory is not fully available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = overcommit, onCheckedChange = { overcommit = it })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(overcommit) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StopVmConfirmationDialog(
    vmName: String,
    onConfirm: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var force by remember { mutableStateOf(false) }
    var forceAfterTimeout by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stop $vmName?") },
        text = {
            Column {
                Text("TrueNAS will request a graceful shutdown first.")
                ToggleLine("Force shutdown immediately", force) { force = it }
                ToggleLine("Force after timeout", forceAfterTimeout) { forceAfterTimeout = it }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(force, forceAfterTimeout) }) { Text("Stop") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeleteVmConfirmationDialog(
    vmName: String,
    onConfirm: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var deleteZvols by remember { mutableStateOf(false) }
    var forceDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Virtual Machine") },
        text = {
            Column {
                Text("Are you sure you want to delete '$vmName'? This action cannot be undone.")
                ToggleLine("Delete associated Zvols", deleteZvols) { deleteZvols = it }
                ToggleLine("Force delete", forceDelete) { forceDelete = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteZvols, forceDelete) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
