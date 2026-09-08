package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Storage
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

enum class DatasetViewMode { TREE, LIST, GRID }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DatasetExplorerScreen(
    manager: TrueNASApiManager,
    poolName: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: DatasetExplorerViewModel =
        viewModel(factory = DatasetExplorerViewModel.Factory(manager))
    val uiState by viewModel.uiState.collectAsState()
    val selectedDataset by viewModel.selectedDataset.collectAsState()

    var viewMode by remember { mutableStateOf(DatasetViewMode.TREE) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentSelectedDataset by remember { mutableStateOf("") }
    var isMobileSheetExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(poolName) { viewModel.loadDatasets(poolName) }

    val allNodes = remember(uiState) {
        when (val s = uiState) {
            is DatasetExplorerViewModel.UiState.Success -> flattenTree(s.rootNode)
            is DatasetExplorerViewModel.UiState.LoadingWithCache -> flattenTree(s.rootNode)
            else -> emptyList()
        }
    }
    val filteredNodes = remember(allNodes, searchQuery) {
        if (searchQuery.isBlank()) allNodes
        else allNodes.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    if (showCreateDialog) {
        CreateDatasetDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type ->
                val parentPath = selectedDataset?.name ?: poolName
                viewModel.createDataset("$parentPath/$name", type, poolName)
                showCreateDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        DeleteDatasetDialog(
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                viewModel.deleteDataset(currentSelectedDataset, poolName)
                showDeleteDialog = false
                currentSelectedDataset = ""
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                DatasetFabGroup(
                    isMenuExpanded = showFabMenu,
                    isDetailsPanelExpanded = isMobileSheetExpanded,
                    selectedDataset = selectedDataset,
                    onToggleMenu = { showFabMenu = !showFabMenu },
                    onToggleDetailsPanel = {
                        if (selectedDataset != null) {
                            isMobileSheetExpanded = !isMobileSheetExpanded
                        }
                    },
                    onCreateDataset = {
                        showFabMenu = false
                        showCreateDialog = true
                    },
                    onDeleteDataset = {
                        showFabMenu = false
                        selectedDataset?.name?.let {
                            currentSelectedDataset = it
                            showDeleteDialog = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                UnifiedScreenHeader(
                    title = "Datasets",
                    subtitle = poolName,
                    isLoading = uiState is DatasetExplorerViewModel.UiState.Loading,
                    isRefreshing = false,
                    error = (uiState as? DatasetExplorerViewModel.UiState.Error)?.message,
                    onRefresh = { viewModel.loadDatasets(poolName) },
                    onDismissError = { },
                    manager = manager,
                    onBackPressed = onNavigateBack
                )
                DatasetToolbar(
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    viewMode = viewMode,
                    onSearchToggle = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    onQueryChange = { searchQuery = it },
                    onViewModeChange = { viewMode = it }
                )
                when (val state = uiState) {
                    is DatasetExplorerViewModel.UiState.Loading -> LoadingScreen("Loading Datasets…")
                    is DatasetExplorerViewModel.UiState.Error -> {}
                    is DatasetExplorerViewModel.UiState.Success,
                    is DatasetExplorerViewModel.UiState.LoadingWithCache -> {
                        val rootNode = when (val s = uiState) {
                            is DatasetExplorerViewModel.UiState.Success -> s.rootNode
                            is DatasetExplorerViewModel.UiState.LoadingWithCache -> s.rootNode
                            else -> null
                        }

                        if (rootNode == null || (rootNode.children.isEmpty() && allNodes.size <= 1)) {
                            NoDatasetsCard(poolName = poolName)
                        } else {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val isTablet = maxWidth > 840.dp

                                if (isTablet) {
                                    TabletDatasetLayout(
                                        rootNode = rootNode,
                                        allNodes = if (isSearchActive) filteredNodes else allNodes,
                                        isSearchActive = isSearchActive,
                                        viewMode = viewMode,
                                        expandedIds = viewModel.expandedNodeIds,
                                        selectedDataset = selectedDataset,
                                        onToggle = { viewModel.toggleExpansion(it) },
                                        onSelect = { viewModel.selectNode(it) },
                                        onCreateChild = { showCreateDialog = true }
                                    )
                                } else {
                                    PhoneDatasetLayout(
                                        rootNode = rootNode,
                                        allNodes = if (isSearchActive) filteredNodes else allNodes,
                                        isSearchActive = isSearchActive,
                                        viewMode = viewMode,
                                        expandedIds = viewModel.expandedNodeIds,
                                        selectedDataset = selectedDataset,
                                        onToggle = { viewModel.toggleExpansion(it) },
                                        onSelect = { viewModel.selectNode(it) } // FIXED: Deliberately select node only. No auto-pop up activation here anymore.
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Slide-Over Panel for Mobile viewports
        AnimatedVisibility(
            visible = isMobileSheetExpanded && selectedDataset != null,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            if (selectedDataset != null) {
                MobileSlideOverPanel(
                    dataset = selectedDataset!!,
                    onDismiss = { isMobileSheetExpanded = false }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatasetToolbar(
    isSearchActive: Boolean,
    searchQuery: String,
    viewMode: DatasetViewMode,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    onViewModeChange: (DatasetViewMode) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        AnimatedContent(
            targetState = isSearchActive,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "toolbar"
        ) { searching ->
            if (searching) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search datasets…") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search, null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onSearchToggle) {
                            Icon(Icons.Default.Close, "Close search")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SingleChoiceSegmentedButtonRow {
                        DatasetViewMode.entries.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = viewMode == mode,
                                onClick = { onViewModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    idx, DatasetViewMode.entries.size
                                ),
                                icon = {
                                    Icon(
                                        imageVector = when (mode) {
                                            DatasetViewMode.TREE -> Icons.Default.FolderSpecial
                                            DatasetViewMode.LIST -> Icons.Default.List
                                            DatasetViewMode.GRID -> Icons.Default.GridView
                                        },
                                        contentDescription = mode.name,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            ) {
                                Text(
                                    text = when (mode) {
                                        DatasetViewMode.TREE -> "Tree"
                                        DatasetViewMode.LIST -> "List"
                                        DatasetViewMode.GRID -> "Grid"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    FilledIconToggleButton(
                        checked = false,
                        onCheckedChange = { onSearchToggle() }
                    ) {
                        Icon(Icons.Default.Search, "Search", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun TabletDatasetLayout(
    rootNode: Storage.ZfsDataset?,
    allNodes: List<Storage.ZfsDataset>,
    isSearchActive: Boolean,
    viewMode: DatasetViewMode,
    expandedIds: List<String>,
    selectedDataset: Storage.ZfsDataset?,
    onToggle: (String) -> Unit,
    onSelect: (Storage.ZfsDataset) -> Unit,
    onCreateChild: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            DatasetContentArea(
                rootNode = rootNode,
                nodes = allNodes,
                isSearchActive = isSearchActive,
                viewMode = viewMode,
                expandedIds = expandedIds,
                selectedId = selectedDataset?.id,
                onToggle = onToggle,
                onSelect = onSelect
            )
        }

        Box(
            modifier = Modifier
                .width(400.dp)
                .fillMaxHeight()
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            if (selectedDataset != null) {
                DatasetDetailsPanel(
                    dataset = selectedDataset,
                    isTablet = true,
                    onCreateDatasetClicked = onCreateChild
                )
            } else {
                EmptySelectionState()
            }
        }
    }
}

@Composable
private fun PhoneDatasetLayout(
    rootNode: Storage.ZfsDataset?,
    allNodes: List<Storage.ZfsDataset>,
    isSearchActive: Boolean,
    viewMode: DatasetViewMode,
    expandedIds: List<String>,
    selectedDataset: Storage.ZfsDataset?,
    onToggle: (String) -> Unit,
    onSelect: (Storage.ZfsDataset) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            DatasetContentArea(
                rootNode = rootNode,
                nodes = allNodes,
                isSearchActive = isSearchActive,
                viewMode = viewMode,
                expandedIds = expandedIds,
                selectedId = selectedDataset?.id,
                onToggle = onToggle,
                onSelect = onSelect,
                extraBottomPadding = 110.dp
            )
        }
    }
}

@Composable
private fun DatasetContentArea(
    rootNode: Storage.ZfsDataset?,
    nodes: List<Storage.ZfsDataset>,
    isSearchActive: Boolean,
    viewMode: DatasetViewMode,
    expandedIds: List<String>,
    selectedId: String?,
    onToggle: (String) -> Unit,
    onSelect: (Storage.ZfsDataset) -> Unit,
    extraBottomPadding: Dp = 96.dp
) {
    Crossfade(
        targetState = viewMode,
        animationSpec = tween(250),
        label = "viewMode"
    ) { mode ->
        when {
            isSearchActive -> {
                FlatListView(
                    nodes = nodes,
                    selectedId = selectedId,
                    onSelect = onSelect,
                    extraBottomPadding = extraBottomPadding
                )
            }
            mode == DatasetViewMode.TREE && rootNode != null -> {
                DatasetTreeView(
                    rootNode = rootNode,
                    expandedIds = expandedIds,
                    selectedId = selectedId,
                    onToggle = onToggle,
                    onSelect = onSelect,
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = extraBottomPadding
                    )
                )
            }
            mode == DatasetViewMode.LIST -> {
                FlatListView(
                    nodes = nodes,
                    selectedId = selectedId,
                    onSelect = onSelect,
                    extraBottomPadding = extraBottomPadding
                )
            }
            mode == DatasetViewMode.GRID -> {
                GridView(
                    nodes = nodes,
                    selectedId = selectedId,
                    onSelect = onSelect,
                    extraBottomPadding = extraBottomPadding
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun FlatListView(
    nodes: List<Storage.ZfsDataset>,
    selectedId: String?,
    onSelect: (Storage.ZfsDataset) -> Unit,
    extraBottomPadding: Dp = 96.dp
) {
    if (nodes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No datasets found",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(nodes, key = { it.id }) { ds ->
            val isSelected = ds.id == selectedId
            Surface(
                onClick = { onSelect(ds) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (ds.children.isEmpty()) Icons.Default.Folder else Icons.Default.Storage,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ds.name.substringAfterLast("/").ifEmpty { ds.name },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = ds.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ds.used!!.value?.let { used ->
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(0.2f)
                            else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = used,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridView(
    nodes: List<Storage.ZfsDataset>,
    selectedId: String?,
    onSelect: (Storage.ZfsDataset) -> Unit,
    extraBottomPadding: Dp = 96.dp
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 8.dp, bottom = extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(nodes, key = { it.id }) { ds ->
            val isSelected = ds.id == selectedId
            val elevation by animateDpAsState(
                targetValue = if (isSelected) 6.dp else 1.dp,
                label = "gridElevation"
            )
            Card(
                onClick = { onSelect(ds) },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(0.15f)
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ds.children.isEmpty()) Icons.Default.Folder else Icons.Default.Storage,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = ds.name.substringAfterLast("/").ifEmpty { ds.name },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    ds.used!!.value?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DatasetFabGroup(
    isMenuExpanded: Boolean,
    isDetailsPanelExpanded: Boolean,
    selectedDataset: Storage.ZfsDataset?,
    onToggleMenu: () -> Unit,
    onToggleDetailsPanel: () -> Unit,
    onCreateDataset: () -> Unit,
    onDeleteDataset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                expandFrom = Alignment.Bottom
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                shrinkTowards = Alignment.Bottom
            ) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
            ) {
                if (selectedDataset != null) {
                    ExtendedFloatingActionButton(
                        onClick = onDeleteDataset,
                        icon = { Icon(Icons.Default.Delete, null) },
                        text = { Text("Delete Dataset") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    )
                }

                ExtendedFloatingActionButton(
                    onClick = onCreateDataset,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = {
                        Text(
                            if (selectedDataset != null) "Create Child Dataset"
                            else "Create Dataset"
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (selectedDataset != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(64.dp)
            ) {
                if (selectedDataset != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { onToggleDetailsPanel() }
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDetailsPanelExpanded) Icons.Default.FolderOpen else Icons.Default.Info,
                            contentDescription = "Toggle Details",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isDetailsPanelExpanded) "Hide Info" else "Show Info",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.5f)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    )
                }

                val arrowRotation by animateFloatAsState(
                    targetValue = if (isMenuExpanded) 180f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "arrowRotation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(56.dp)
                        .clickable { onToggleMenu() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ExpandLess,
                        contentDescription = "More Actions",
                        tint = if (selectedDataset != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }
            }
        }
    }
}


@Composable
fun DatasetTreeView(
    rootNode: Storage.ZfsDataset,
    expandedIds: List<String>,
    selectedId: String?,
    onToggle: (String) -> Unit,
    onSelect: (Storage.ZfsDataset) -> Unit,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item {
            DatasetTreeItem(
                dataset = rootNode,
                level = 0,
                expandedIds = expandedIds,
                selectedId = selectedId,
                onToggle = onToggle,
                onSelect = onSelect
            )
        }
    }
}

@Composable
fun DatasetTreeItem(
    dataset: Storage.ZfsDataset,
    level: Int,
    expandedIds: List<String>,
    selectedId: String?,
    onToggle: (String) -> Unit,
    onSelect: (Storage.ZfsDataset) -> Unit
) {
    val isExpanded = expandedIds.contains(dataset.id)
    val isSelected = selectedId == dataset.id
    val hasChildren = dataset.children.isNotEmpty()
    val isRoot = level == 0

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "treeChevron"
    )

    Column {
        Surface(
            onClick = { onSelect(dataset) },
            shape = if (isRoot) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp),
            color = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isRoot -> MaterialTheme.colorScheme.surfaceContainerHighest
                else -> Color.Transparent
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (level * 22).dp, top = if (isRoot) 2.dp else 1.dp, bottom = if (isRoot) 2.dp else 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = if (isRoot) 16.dp else 10.dp,
                        vertical = if (isRoot) 14.dp else 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            enabled = hasChildren,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggle(dataset.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (hasChildren) {
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Icon(
                    imageVector = when {
                        isRoot -> Icons.Default.Storage
                        isExpanded -> Icons.Default.FolderOpen
                        else -> Icons.Default.Folder
                    },
                    contentDescription = null,
                    tint = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isRoot -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(if (isRoot) 24.dp else 20.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = if (isRoot) dataset.name
                    else dataset.name.substringAfterLast("/"),
                    style = if (isRoot) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isRoot || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                dataset.used!!.value?.let { used ->
                    if (used != "0" && used != "-") {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(0.2f)
                            else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = used,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Child node tree animation frame
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column {
                dataset.children.forEach { child ->
                    DatasetTreeItem(
                        dataset = child,
                        level = level + 1,
                        expandedIds = expandedIds,
                        selectedId = selectedId,
                        onToggle = onToggle,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}


@Composable
fun DatasetDetailsPanel(
    dataset: Storage.ZfsDataset,
    isTablet: Boolean,
    onCreateDatasetClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(if (isTablet) 16.dp else 0.dp)) {
        if (isTablet) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilledTonalButton(
                    onClick = onCreateDatasetClicked,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        DatasetDetailsContent(dataset = dataset, isTablet = isTablet)
    }
}

// NEW: Expressive Donut Storage space utilization Chart Component
@Composable
fun DatasetSpaceDonutChart(
    usedStr: String,
    availableStr: String,
    modifier: Modifier = Modifier
) {
    val parseSize = { s: String -> s.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0 }
    val usedVal = parseSize(usedStr)
    val availVal = parseSize(availableStr)
    val total = usedVal + availVal
    val sweptAngle = ((usedVal / total) * 360f).toFloat().coerceIn(10f, 350f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = sweptAngle,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = usedStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            ChartLegendRow(color = primaryColor, title = "Allocated Used Space", value = usedStr)
            ChartLegendRow(color = trackColor, title = "Available Free Space", value = availableStr)
        }
    }
}

@Composable
private fun ChartLegendRow(color: Color, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// NEW: Expressive Layout Refactor with Elegant Cards & Visual Analytics
@Composable
private fun DatasetDetailsContent(
    dataset: Storage.ZfsDataset,
    isTablet: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Path Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column {
                    Text("Mount Path", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    dataset.mountpoint?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Storage Usage Visualization Group
        Text(
            text = "Storage Capacity Breakdown",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            DatasetSpaceDonutChart(
                usedStr = dataset.used!!.value ?: "0B",
                availableStr = dataset.available!!.value ?: "0B"
            )
        }

        // Grid of Advanced System Parameter Cards
        Text(
            text = "Dataset Parameters",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ParamCard("Compression", dataset.compression.value ?: "N/A", Modifier.weight(1f))
            ParamCard("Ratio", dataset.compressratio.value ?: "N/A", Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ParamCard("Deduplication", dataset.deduplication.value ?: "N/A", Modifier.weight(1f))
            ParamCard("Sync Mode", dataset.sync.value ?: "N/A", Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ParamCard("Read Only", dataset.readonly!!.value ?: "N/A", Modifier.weight(1f))
            ParamCard("Encrypted", if (dataset.encrypted) "Yes" else "No", Modifier.weight(1f))
        }
    }
}

@Composable
private fun ParamCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}


@Composable
fun MobileSlideOverPanel(
    dataset: Storage.ZfsDataset,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close Info Page"
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Dataset Analytics",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dataset.name.substringAfterLast("/").ifEmpty { dataset.name },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                DatasetDetailsContent(dataset = dataset, isTablet = false)
            }
        }
    }
}


@Composable
fun CreateDatasetDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: Storage.DatasetOptions) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf(Storage.DatasetOptions.GENERIC) }
    val options = Storage.DatasetOptions.entries.toTypedArray()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Dataset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dataset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Dataset Type", style = MaterialTheme.typography.bodyMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            onClick = { selectedOption = option },
                            selected = option == selectedOption
                        ) {
                            Text(option.name.lowercase().replaceFirstChar { it.titlecase() })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, selectedOption) },
                enabled = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteDatasetDialog(onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Dataset") },
        text = { Text("Are you sure you want to delete this dataset? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmptySelectionState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FolderOpen, null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Select a dataset",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoDatasetsCard(poolName: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Folder, null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No Datasets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pool '$poolName' has no child datasets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun flattenTree(root: Storage.ZfsDataset?): List<Storage.ZfsDataset> {
    if (root == null) return emptyList()
    val result = mutableListOf<Storage.ZfsDataset>()
    fun visit(ds: Storage.ZfsDataset) {
        result.add(ds)
        ds.children.forEach(::visit)
    }
    visit(root)
    return result
}