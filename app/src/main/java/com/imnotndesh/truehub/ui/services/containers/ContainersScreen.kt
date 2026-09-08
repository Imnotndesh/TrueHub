@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.imnotndesh.truehub.ui.services.containers

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
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
    onSearchClick: (() -> Unit)? = null,
    viewModel: ContainerScreenViewModel = viewModel(
        factory = ContainerScreenViewModel.ContainerViewModelFactory(manager)
    )
) {
    LaunchedEffect(Unit) {
        viewModel.loadContainers()
    }
    val uiState by viewModel.uiState.collectAsState()
    val isCompact = AdaptiveLayoutHelper.isCompact()

    var selectedCategory by remember { mutableStateOf(ContainerFilterCategory.ALL) }
    var selectedContainerForPane by remember { mutableStateOf<Virt.ContainerResponse?>(null) }

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

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Containers",
            subtitle = "${filteredContainers.size} containers",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onRefresh = { viewModel.loadContainers() },
            onDismissError = { viewModel.clearError() },
            manager = manager,
            onSearchClick = onSearchClick
        )

        ContainerFilterBar(
            currentCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PullToRefreshContent(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadContainers() },
            modifier = Modifier.weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingScreen("Loading Containers")
                }
                uiState.containers.isEmpty() && !uiState.isLoading -> {
                    EmptyContainerContent()
                }
                filteredContainers.isEmpty() && !uiState.isLoading && uiState.containers.isNotEmpty() -> {
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
                else -> {
                    if (isCompact) {
                        ContainersContent(
                            containers = filteredContainers,
                            isRefreshing = uiState.isRefreshing,
                            onStartContainer = { id -> viewModel.startContainer(id) },
                            onStopContainer = { id -> viewModel.stopContainer(id) },
                            onRestartContainer = { id -> viewModel.restartContainer(id) },
                            onDeleteContainer = { id -> viewModel.deleteContainer(id) },
                            operationJobs = uiState.operationJobs,
                            onContainerInfoClick = { container -> onNavigateToContainerInfo(container) },
                            selectedContainer = null
                        )
                    } else {
                        ContainersSplitPaneContent(
                            containers = filteredContainers,
                            selectedContainer = selectedContainerForPane,
                            isRefreshing = uiState.isRefreshing,
                            onStartContainer = { id -> viewModel.startContainer(id) },
                            onStopContainer = { id -> viewModel.stopContainer(id) },
                            onRestartContainer = { id -> viewModel.restartContainer(id) },
                            onDeleteContainer = { id -> viewModel.deleteContainer(id) },
                            operationJobs = uiState.operationJobs,
                            onContainerInfoClick = { container -> onNavigateToContainerInfo(container) },
                            onContainerPaneSelected = { container -> selectedContainerForPane = container },
                            onClosePane = { selectedContainerForPane = null }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerFilterBar(
    currentCategory: ContainerFilterCategory,
    onCategorySelected: (ContainerFilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ContainerFilterCategory.entries.toTypedArray()) { category ->
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
private fun ContainersSplitPaneContent(
    containers: List<Virt.ContainerResponse>,
    selectedContainer: Virt.ContainerResponse?,
    isRefreshing: Boolean,
    onStartContainer: (String) -> Unit,
    onStopContainer: (String) -> Unit,
    onRestartContainer: (String) -> Unit,
    onDeleteContainer: (String) -> Unit,
    operationJobs: Map<String, System.Job>,
    onContainerInfoClick: (Virt.ContainerResponse) -> Unit,
    onContainerPaneSelected: (Virt.ContainerResponse) -> Unit,
    onClosePane: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (selectedContainer != null) 0.55f else 1f)
                .fillMaxSize()
        ) {
            ContainersContent(
                containers = containers,
                isRefreshing = isRefreshing,
                onStartContainer = onStartContainer,
                onStopContainer = onStopContainer,
                onRestartContainer = onRestartContainer,
                onDeleteContainer = onDeleteContainer,
                operationJobs = operationJobs,
                onContainerInfoClick = onContainerInfoClick,
                selectedContainer = selectedContainer,
                onContainerPaneSelected = onContainerPaneSelected
            )
        }
        AnimatedVisibility(
            visible = selectedContainer != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeOut()
        ) {
            selectedContainer?.let { container ->
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                ) {
                    ContainerInfoPane(
                        container = container,
                        onClose = onClosePane
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
private fun ContainersContent(
    containers: List<Virt.ContainerResponse>,
    isRefreshing: Boolean,
    onStartContainer: (String) -> Unit,
    onStopContainer: (String) -> Unit,
    onRestartContainer: (String) -> Unit,
    onContainerInfoClick: (Virt.ContainerResponse) -> Unit,
    onDeleteContainer: (String) -> Unit,
    operationJobs: Map<String, System.Job>,
    selectedContainer: Virt.ContainerResponse? = null,
    onContainerPaneSelected: ((Virt.ContainerResponse) -> Unit)? = null
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
            items(items = containers, key = { it.id }) { container ->
                Box(modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                )) {
                    ContainerCard(
                        container = container,
                        onStartContainer = { onStartContainer(container.id) },
                        onStopContainer = { onStopContainer(container.id) },
                        onRestartContainer = { onRestartContainer(container.id) },
                        onDeleteContainer = { onDeleteContainer(container.id) },
                        operationJob = operationJobs[container.id],
                        onContainerInfoClicked = { onContainerInfoClick(container) },
                        isSelected = selectedContainer?.id == container.id
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
            items(items = containers, key = { it.id }) { container ->
                Box(modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                )) {
                    ContainerCard(
                        container = container,
                        onStartContainer = { onStartContainer(container.id) },
                        onStopContainer = { onStopContainer(container.id) },
                        onRestartContainer = { onRestartContainer(container.id) },
                        onDeleteContainer = { onDeleteContainer(container.id) },
                        operationJob = operationJobs[container.id],
                        onContainerInfoClicked = { onContainerPaneSelected?.invoke(container) ?: onContainerInfoClick(container) },
                        isSelected = selectedContainer?.id == container.id
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContainerCard(
    container: Virt.ContainerResponse,
    onStartContainer: () -> Unit,
    onStopContainer: () -> Unit,
    onRestartContainer: () -> Unit,
    onContainerInfoClicked: (Virt.ContainerResponse) -> Unit,
    onDeleteContainer: () -> Unit,
    operationJob: System.Job? = null,
    isSelected: Boolean = false
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                            .background(
                                when (container.type) {
                                    Virt.Type.CONTAINER -> MaterialTheme.colorScheme.primaryContainer
                                    Virt.Type.VM -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (container.type) {
                                Virt.Type.CONTAINER -> "C"
                                Virt.Type.VM -> "VM"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when (container.type) {
                                Virt.Type.CONTAINER -> MaterialTheme.colorScheme.onPrimaryContainer
                                Virt.Type.VM -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
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
                    StatusChip(status = container.status, icon = getStatusIcon(container.status))
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
                                .background(
                                    when (container.type) {
                                        Virt.Type.CONTAINER -> MaterialTheme.colorScheme.primaryContainer
                                        Virt.Type.VM -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (container.type) {
                                    Virt.Type.CONTAINER -> "C"
                                    Virt.Type.VM -> "VM"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when (container.type) {
                                    Virt.Type.CONTAINER -> MaterialTheme.colorScheme.onPrimaryContainer
                                    Virt.Type.VM -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getStatusColor(container.status))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
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
                DetailItem(label = "CPU", value = container.cpu ?: "N/A")
                DetailItem(label = "Memory", value = container.memory?.let { "$it MB" } ?: "N/A")
                DetailItem(label = "Autostart", value = if (container.autostart) "Yes" else "No")
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
                    when (container.status) {
                        Virt.Status.STOPPED -> ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = onStartContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Virt.Status.RUNNING -> ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true,
                            onClick = onStopContainer,
                            modifier = Modifier.weight(1f)
                        )
                        else -> ActionButton(
                            text = container.status.name,
                            icon = Icons.Default.Refresh,
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
                        onClick = { onContainerInfoClicked(container) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (container.status) {
                        Virt.Status.STOPPED -> CompactActionButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            isPrimary = true,
                            onClick = onStartContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Virt.Status.RUNNING -> CompactActionButton(
                            icon = Icons.Default.Stop,
                            contentDescription = "Stop",
                            isPrimary = true,
                            onClick = onStopContainer,
                            modifier = Modifier.weight(1f)
                        )
                        else -> CompactActionButton(
                            icon = Icons.Default.Refresh,
                            contentDescription = container.status.name,
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
                        onClick = { onContainerInfoClicked(container) },
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
                    if (container.status == Virt.Status.RUNNING) {
                        ActionButton(
                            text = "Restart",
                            icon = Icons.Default.RestartAlt,
                            enabled = true,
                            isPrimary = false,
                            onClick = onRestartContainer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ActionButton(
                        text = "Delete",
                        icon = Icons.Default.Delete,
                        enabled = container.status == Virt.Status.STOPPED,
                        isPrimary = false,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        isDanger = true
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            containerName = container.name,
            onConfirm = {
                onDeleteContainer()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
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
private fun StatusChip(status: Virt.Status, icon: ImageVector) {
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
                text = status.name.lowercase().replaceFirstChar { it.uppercase() },
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
private fun getStatusColor(status: Virt.Status): Color {
    return when (status) {
        Virt.Status.RUNNING -> Color(0xFF2E7D32)
        Virt.Status.STOPPED -> Color(0xFF757575)
        Virt.Status.ERROR -> MaterialTheme.colorScheme.error
        Virt.Status.FROZEN -> Color(0xFF1976D2)
        Virt.Status.STARTING, Virt.Status.STOPPING,
        Virt.Status.FREEZING, Virt.Status.THAWED,
        Virt.Status.ABORTING -> Color(0xFFF57C00)
        Virt.Status.UNKNOWN -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(status: Virt.Status): ImageVector {
    return when (status) {
        Virt.Status.RUNNING -> Icons.Default.PlayArrow
        Virt.Status.STOPPED -> Icons.Default.Stop
        Virt.Status.ERROR -> Icons.Default.Error
        Virt.Status.FROZEN -> Icons.Default.Pause
        else -> Icons.Default.Refresh
    }
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
private fun DeleteConfirmationDialog(
    containerName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Container") },
        text = { Text(text = "Are you sure you want to delete '$containerName'? This action cannot be undone.") },
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

private fun getOperationText(state: String): String {
    return when (state) {
        "RUNNING" -> "Processing..."
        "SUCCESS" -> "Completed"
        "FAILED" -> "Failed"
        else -> state.lowercase().replaceFirstChar { it.uppercase() }
    }
}