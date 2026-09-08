package com.imnotndesh.truehub.ui.services.vm

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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
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
    onSearchClick: (() -> Unit)? = null,
    viewModel: VmsScreenViewModel = viewModel(
        factory = VmsScreenViewModel.VmViewModelFactory(manager)
    )
) {
    LaunchedEffect(Unit) {
        viewModel.loadVms()
    }
    val uiState by viewModel.uiState.collectAsState()
    val isCompact = AdaptiveLayoutHelper.isCompact()

    var selectedCategory by remember { mutableStateOf(VmFilterCategory.ALL) }
    var selectedVmForPane by remember { mutableStateOf<Vm.VmQueryResponse?>(null) }

    val filteredVms by remember(uiState.vms, selectedCategory) {
        derivedStateOf {
            when (selectedCategory) {
                VmFilterCategory.ALL -> uiState.vms
                VmFilterCategory.RUNNING -> uiState.vms.filter { it.status.state.equals("running", ignoreCase = true) }
                VmFilterCategory.STOPPED -> uiState.vms.filter { it.status.state.equals("stopped", ignoreCase = true) }
                VmFilterCategory.SUSPENDED -> uiState.vms.filter { it.status.state.equals("suspended", ignoreCase = true) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Virtual Machines",
            subtitle = "${filteredVms.size} VMs",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onRefresh = { viewModel.refresh() },
            onDismissError = { viewModel.clearError() },
            manager = manager,
            onSearchClick = onSearchClick
        )

        VmFilterBar(
            currentCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PullToRefreshContent(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            when {
                uiState.vms.isEmpty() && uiState.isLoading -> {
                    LoadingScreen("Loading Virtual Machines")
                }
                uiState.vms.isEmpty() && !uiState.isLoading -> {
                    EmptyVmContent()
                }
                filteredVms.isEmpty() && !uiState.isLoading && uiState.vms.isNotEmpty() -> {
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
                else -> {
                    if (isCompact) {
                        VmsContent(
                            vms = filteredVms,
                            isRefreshing = uiState.isRefreshing,
                            onStartVm = { id, overcommit -> viewModel.startVm(id, overcommit) },
                            onStopVm = { id, force, timeout -> viewModel.stopVm(id, force, timeout) },
                            onRestartVm = { id -> viewModel.restartVm(id) },
                            onSuspendVm = { id -> viewModel.suspendVm(id) },
                            onResumeVm = { id -> viewModel.resumeVm(id) },
                            onPowerOffVm = { id -> viewModel.powerOffVm(id) },
                            onDeleteVm = { id, deleteZvols, forceDelete -> viewModel.deleteVm(id, deleteZvols, forceDelete) },
                            operationJob = uiState.operationJobs,
                            onVmInfoClicked = { vm -> onNavigateToVmInfo(vm) },
                            selectedVm = null
                        )
                    } else {
                        VmsSplitPaneContent(
                            vms = filteredVms,
                            selectedVm = selectedVmForPane,
                            isRefreshing = uiState.isRefreshing,
                            onStartVm = { id, overcommit -> viewModel.startVm(id, overcommit) },
                            onStopVm = { id, force, timeout -> viewModel.stopVm(id, force, timeout) },
                            onRestartVm = { id -> viewModel.restartVm(id) },
                            onSuspendVm = { id -> viewModel.suspendVm(id) },
                            onResumeVm = { id -> viewModel.resumeVm(id) },
                            onPowerOffVm = { id -> viewModel.powerOffVm(id) },
                            onDeleteVm = { id, deleteZvols, forceDelete -> viewModel.deleteVm(id, deleteZvols, forceDelete) },
                            operationJob = uiState.operationJobs,
                            onVmInfoClicked = { vm -> onNavigateToVmInfo(vm) },
                            onVmPaneSelected = { vm -> selectedVmForPane = vm },
                            onClosePane = { selectedVmForPane = null }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmFilterBar(
    currentCategory: VmFilterCategory,
    onCategorySelected: (VmFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(VmFilterCategory.entries.toTypedArray()) { category ->
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
private fun VmsSplitPaneContent(
    vms: List<Vm.VmQueryResponse>,
    selectedVm: Vm.VmQueryResponse?,
    isRefreshing: Boolean,
    onStartVm: (Int, Boolean) -> Unit,
    onStopVm: (Int, Boolean, Boolean) -> Unit,
    onRestartVm: (Int) -> Unit,
    onSuspendVm: (Int) -> Unit,
    onResumeVm: (Int) -> Unit,
    onPowerOffVm: (Int) -> Unit,
    onDeleteVm: (Int, Boolean, Boolean) -> Unit,
    operationJob: Map<Int, System.Job>,
    onVmInfoClicked: (Vm.VmQueryResponse) -> Unit,
    onVmPaneSelected: (Vm.VmQueryResponse) -> Unit,
    onClosePane: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedVm != null) 0.55f else 1f)
                .fillMaxSize()
        ) {
            VmsContent(
                vms = vms,
                isRefreshing = isRefreshing,
                onStartVm = onStartVm,
                onStopVm = onStopVm,
                onRestartVm = onRestartVm,
                onSuspendVm = onSuspendVm,
                onResumeVm = onResumeVm,
                onPowerOffVm = onPowerOffVm,
                onDeleteVm = onDeleteVm,
                operationJob = operationJob,
                onVmInfoClicked = onVmInfoClicked,
                selectedVm = selectedVm,
                onVmPaneSelected = onVmPaneSelected
            )
        }
        AnimatedVisibility(
            visible = selectedVm != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeOut()
        ) {
            selectedVm?.let { vm ->
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    VmInfoPane(
                        vm = vm,
                        onClose = onClosePane
                    )
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
            imageVector = Icons.Default.Computer,
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
private fun VmsContent(
    vms: List<Vm.VmQueryResponse>,
    isRefreshing: Boolean,
    onStartVm: (Int, Boolean) -> Unit,
    onStopVm: (Int, Boolean, Boolean) -> Unit,
    onRestartVm: (Int) -> Unit,
    onSuspendVm: (Int) -> Unit,
    onResumeVm: (Int) -> Unit,
    onPowerOffVm: (Int) -> Unit,
    onDeleteVm: (Int, Boolean, Boolean) -> Unit,
    onVmInfoClicked: (Vm.VmQueryResponse) -> Unit,
    operationJob: Map<Int, System.Job>,
    selectedVm: Vm.VmQueryResponse? = null,
    onVmPaneSelected: ((Vm.VmQueryResponse) -> Unit)? = null
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
                Box(modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                )) {
                    VmCard(
                        vm = vm,
                        onStartVm = { overcommit -> onStartVm(vm.id, overcommit) },
                        onStopVm = { force, timeout -> onStopVm(vm.id, force, timeout) },
                        onRestartVm = { onRestartVm(vm.id) },
                        onSuspendVm = { onSuspendVm(vm.id) },
                        onResumeVm = { onResumeVm(vm.id) },
                        onPowerOffVm = { onPowerOffVm(vm.id) },
                        onDeleteVm = { deleteZvols, forceDelete -> onDeleteVm(vm.id, deleteZvols, forceDelete) },
                        operationJob = operationJob[vm.id],
                        onVmInfoClick = { onVmInfoClicked(vm) },
                        isSelected = selectedVm?.id == vm.id
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
                Box(modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                )) {
                    VmCard(
                        vm = vm,
                        onStartVm = { overcommit -> onStartVm(vm.id, overcommit) },
                        onStopVm = { force, timeout -> onStopVm(vm.id, force, timeout) },
                        onRestartVm = { onRestartVm(vm.id) },
                        onSuspendVm = { onSuspendVm(vm.id) },
                        onResumeVm = { onResumeVm(vm.id) },
                        onPowerOffVm = { onPowerOffVm(vm.id) },
                        onDeleteVm = { deleteZvols, forceDelete -> onDeleteVm(vm.id, deleteZvols, forceDelete) },
                        operationJob = operationJob[vm.id],
                        onVmInfoClick = { onVmPaneSelected?.invoke(vm) ?: onVmInfoClicked(vm) },
                        isSelected = selectedVm?.id == vm.id
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    operationJob: System.Job? = null,
    isSelected: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    val isCompact = AdaptiveLayoutHelper.isCompact()
    val cardPadding = if (isCompact) 20.dp else 16.dp
    val cardHorizontalPadding = if (isCompact) 4.dp else 0.dp

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
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
            if (isCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
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
                            text = if (vm.description.isNotEmpty()) vm.description else "ID: ${vm.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusChip(status = vm.status, icon = getStatusIcon(vm.status))
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
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getStatusColor(vm.status))
                        )
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
                        text = if (vm.description.isNotEmpty()) vm.description else "ID: ${vm.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(label = "vCPUs", value = "${vm.vcpus}")
                DetailItem(label = "Cores", value = "${vm.cores}")
                DetailItem(label = "Memory", value = "${vm.memory} MB")
            }

            operationJob?.let { job ->
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = job.progress?.description ?: getOperationText(job.state),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (vm.status.state.lowercase()) {
                        "stopped" -> ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = { showStartDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        "running" -> ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true,
                            onClick = { showStopDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        "suspended" -> ActionButton(
                            text = "Resume",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = onResumeVm,
                            modifier = Modifier.weight(1f)
                        )
                        else -> ActionButton(
                            text = vm.status.state,
                            icon = Icons.Default.Error,
                            enabled = false,
                            isPrimary = false,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                    ActionButton(
                        text = "View Info",
                        icon = Icons.Default.Info,
                        enabled = true,
                        isPrimary = false,
                        onClick = { onVmInfoClick(vm) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (vm.status.state.lowercase()) {
                        "stopped" -> CompactActionButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            isPrimary = true,
                            onClick = { showStartDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        "running" -> CompactActionButton(
                            icon = Icons.Default.Stop,
                            contentDescription = "Stop",
                            isPrimary = true,
                            onClick = { showStopDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        "suspended" -> CompactActionButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            isPrimary = true,
                            onClick = onResumeVm,
                            modifier = Modifier.weight(1f)
                        )
                        else -> CompactActionButton(
                            icon = Icons.Default.Error,
                            contentDescription = vm.status.state,
                            isPrimary = false,
                            enabled = false,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                    CompactActionButton(
                        icon = Icons.Default.Info,
                        contentDescription = "View Info",
                        isPrimary = false,
                        onClick = { onVmInfoClick(vm) },
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

            if (isCompact) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { showMoreOptions = !showMoreOptions },
                    shape = RoundedCornerShape(16.dp),
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
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (vm.status.state.lowercase() == "running") {
                        ActionButton(
                            text = "Restart",
                            icon = Icons.Default.RestartAlt,
                            enabled = true,
                            isPrimary = false,
                            onClick = onRestartVm,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ActionButton(
                            text = "Suspend",
                            icon = Icons.Default.Pause,
                            enabled = true,
                            isPrimary = false,
                            onClick = onSuspendVm,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ActionButton(
                            text = "Power Off",
                            icon = Icons.Default.PowerOff,
                            enabled = true,
                            isPrimary = false,
                            onClick = onPowerOffVm,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ActionButton(
                        text = "Delete",
                        icon = Icons.Default.Delete,
                        enabled = vm.status.state.lowercase() == "stopped",
                        isPrimary = false,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        isDanger = true
                    )
                }
            }
        }
    }

    if (showStartDialog) {
        StartConfirmationDialog(
            vmName = vm.name,
            onConfirm = { overcommit ->
                onStartVm(overcommit)
                showStartDialog = false
            },
            onDismiss = { showStartDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            vmName = vm.name,
            onConfirm = { deleteZvols, forceDelete ->
                onDeleteVm(deleteZvols, forceDelete)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showStopDialog) {
        StopConfirmationDialog(
            vmName = vm.name,
            onConfirm = { forceShutoff, forceShutoffAfterTimeout ->
                onStopVm(forceShutoff, forceShutoffAfterTimeout)
                showStopDialog = false
            },
            onDismiss = { showStopDialog = false }
        )
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    contentDescription: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "container"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isPrimary -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "content"
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
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
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusChip(status: Vm.VmStatus, icon: ImageVector) {
    Surface(
        color = getStatusColor(status).copy(alpha = 0.12f),
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = getStatusColor(status)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.state.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = getStatusColor(status),
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
    modifier: Modifier = Modifier,
    isDanger: Boolean = false
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh
            isDanger -> MaterialTheme.colorScheme.errorContainer
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "btnColor"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            isDanger -> MaterialTheme.colorScheme.onErrorContainer
            isPrimary -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "contentColor"
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
private fun getStatusColor(status: Vm.VmStatus): Color {
    return when (status.state.lowercase()) {
        "running" -> Color(0xFF2E7D32)
        "stopped" -> Color(0xFF757575)
        "suspended" -> Color(0xFF1976D2)
        "error" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(status: Vm.VmStatus): ImageVector {
    return when (status.state.lowercase()) {
        "running" -> Icons.Default.PlayArrow
        "stopped" -> Icons.Default.Stop
        "suspended" -> Icons.Default.Pause
        "error" -> Icons.Default.Error
        else -> Icons.Default.PowerSettingsNew
    }
}

@Composable
private fun DeleteConfirmationDialog(
    vmName: String,
    onConfirm: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var deleteZvol by remember { mutableStateOf(false) }
    var forceDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Virtual Machine") },
        text = {
            Column {
                Text(text = "Are you sure you want to delete '$vmName'? This action cannot be undone.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Delete Zvols")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = deleteZvol, onCheckedChange = { deleteZvol = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Force Delete")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = forceDelete, onCheckedChange = { forceDelete = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteZvol, forceDelete) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StopConfirmationDialog(
    vmName: String,
    onConfirm: (forceShutoff: Boolean, forceShutoffAfterTimeout: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var forceShutoffChecked by remember { mutableStateOf(false) }
    var forceShutoffAfterTimeoutChecked by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Stop Virtual Machine") },
        text = {
            Column {
                Text(text = "Are you sure you want to stop '$vmName'?")
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Force shutoff")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = forceShutoffChecked, onCheckedChange = { forceShutoffChecked = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Force shutoff after timeout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = forceShutoffAfterTimeoutChecked, onCheckedChange = { forceShutoffAfterTimeoutChecked = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(forceShutoffChecked, forceShutoffAfterTimeoutChecked) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Stop") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StartConfirmationDialog(
    vmName: String,
    onConfirm: (overcommit: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var overcommit by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Start Virtual Machine") },
        text = {
            Column {
                Text(text = "Are you sure you want to start '$vmName'?")
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Overcommit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = overcommit, onCheckedChange = { overcommit = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(overcommit) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MorphingRefreshIndicator(
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun getOperationText(state: String): String {
    return when (state) {
        "RUNNING" -> "Processing..."
        "SUCCESS" -> "Completed"
        "FAILED" -> "Failed"
        else -> state.lowercase().replaceFirstChar { it.uppercase() }
    }
}