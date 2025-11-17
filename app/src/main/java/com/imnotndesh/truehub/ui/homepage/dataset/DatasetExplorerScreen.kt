package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Storage
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun DatasetExplorerScreen(
    manager: TrueNASApiManager,
    poolName: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: DatasetExplorerViewModel = viewModel(factory = DatasetExplorerViewModel.Factory(manager))
    val uiState by viewModel.uiState.collectAsState()
    val selectedDataset by viewModel.selectedDataset.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var currentSelectedDataset by remember { mutableStateOf("") }

    if (showCreateDialog) {
        CreateDatasetDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type ->
                val parentPath = selectedDataset?.name ?: poolName
                val fullPath = "$parentPath/$name"

                viewModel.createDataset(fullPath, type, poolName)
                showCreateDialog = false
            }
        )
    }
    if (showDeleteDialog){
        DeleteDatasetDialog(
            onDismiss = {showDeleteDialog = false},
            onDelete = {
                val fullPath = currentSelectedDataset
                viewModel.deleteDataset(fullPath,poolName)
                showDeleteDialog = false
                currentSelectedDataset = ""
            }
        )
    }

    LaunchedEffect(poolName) {
        viewModel.loadDatasets(poolName)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            UnifiedScreenHeader(
                title = "Dataset Explorer",
                subtitle = poolName,
                isLoading = uiState is DatasetExplorerViewModel.UiState.Loading,
                isRefreshing = false,
                error = (uiState as? DatasetExplorerViewModel.UiState.Error)?.message,
                onRefresh = { viewModel.loadDatasets(poolName) },
                onDismissError = { },
                manager = manager,
                onBackPressed = onNavigateBack
            )

            when (val state = uiState) {
                is DatasetExplorerViewModel.UiState.Loading -> LoadingScreen("Loading Datasets...")
                is DatasetExplorerViewModel.UiState.Error -> { }
                is DatasetExplorerViewModel.UiState.Success -> {
                    if (state.rootNode != null && state.rootNode.children.isNotEmpty()) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val isTablet = maxWidth > 840.dp

                            if (isTablet) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        DatasetTreeView(
                                            rootNode = state.rootNode,
                                            expandedIds = viewModel.expandedNodeIds,
                                            selectedId = selectedDataset?.id,
                                            onToggle = { viewModel.toggleExpansion(it) },
                                            onSelect = { viewModel.selectNode(it) }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(400.dp)
                                            .fillMaxHeight()
                                            .padding(16.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                    ) {
                                        if (selectedDataset != null) {
                                            DatasetDetailsPanel(
                                                selectedDataset!!, isTablet = true,
                                                onCreateDatasetClicked = { showCreateDialog = true }                                            )
                                        } else {
                                            EmptySelectionState()
                                        }
                                    }
                                }
                            } else {
                                var isMobileSheetExpanded by remember { mutableStateOf(false) }
                                val bottomPadding = if (selectedDataset != null) {
                                    if (isMobileSheetExpanded) 380.dp else 120.dp
                                } else 16.dp

                                Column(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        DatasetTreeView(
                                            rootNode = state.rootNode,
                                            expandedIds = viewModel.expandedNodeIds,
                                            selectedId = selectedDataset?.id,
                                            onToggle = { viewModel.toggleExpansion(it) },
                                            onSelect = { viewModel.selectNode(it) },
                                            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding)
                                        )
                                    }

                                    if (selectedDataset != null) {
                                        MobileDetailsSheet(
                                            dataset = selectedDataset!!,
                                            isExpanded = isMobileSheetExpanded,
                                            onToggleExpand = {
                                                isMobileSheetExpanded = !isMobileSheetExpanded
                                            },
                                            onCreateDatasetClicked = { showCreateDialog = true },
                                            onDeleteDatasetClicked = {datasetName ->
                                                showDeleteDialog = true
                                                currentSelectedDataset = datasetName
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else if (state.rootNode != null && state.rootNode.children.isEmpty()) {
                        NoDatasetsCard(poolName = poolName)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Pool '$poolName' not found.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                is DatasetExplorerViewModel.UiState.LoadingWithCache -> {
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
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

                Text(
                    "Dataset Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
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
            ) {
                Text("Create")
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
fun MobileDetailsSheet(
    dataset: Storage.ZfsDataset,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCreateDatasetClicked: () -> Unit,
    onDeleteDatasetClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleExpand() }
                ) {
                    Text(
                        text = "Dataset Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dataset.name.substringAfterLast("/"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onCreateDatasetClicked,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Child Dataset",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = {
                        onDeleteDatasetClicked(
                            dataset.name
                        )
                    },
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Dataset",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    Box(
                        modifier = Modifier
                            .height(320.dp)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        DatasetDetailsContent(
                            dataset = dataset,
                            isTablet = false
                        )
                    }
                }
            }
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

    val rowHeight = if (isRoot) 68.dp else 54.dp
    val iconSize = if (isRoot) 28.dp else 22.dp
    val textStyle = if (isRoot) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium

    val backgroundColor = when {
        isRoot -> MaterialTheme.colorScheme.surfaceContainerHighest
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val shape = if (isRoot) RoundedCornerShape(14.dp) else RoundedCornerShape(10.dp)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .padding(start = (level * 20).dp)
                .background(backgroundColor, shape)
                .clip(shape)
                .clickable {
                    onSelect(dataset)
                    if (hasChildren) onToggle(dataset.id)
                }
                .padding(end = 12.dp)
                .height(rowHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .clickable { if (hasChildren) onToggle(dataset.id) },
                contentAlignment = Alignment.Center
            ) {
                if (hasChildren) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Icon(
                imageVector = if (isRoot) Icons.Default.Storage else if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = if (isRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isRoot) dataset.name else dataset.name.substringAfterLast("/"),
                style = textStyle,
                fontWeight = if (isRoot || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isTablet) 16.dp else 0.dp)
    ) {
        if (isTablet) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilledTonalButton(
                    onClick = onCreateDatasetClicked,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Child Dataset",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        DatasetDetailsContent(
            dataset = dataset,
            isTablet = isTablet
        )
    }
}

@Composable
private fun DatasetDetailsContent(
    dataset: Storage.ZfsDataset,
    isTablet: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .then(
                    if (isTablet) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow("Full Path", dataset.name)
            DetailRow("Mountpoint", dataset.mountpoint)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            DetailRow("Used", dataset.used.value ?: "N/A")
            DetailRow("Available", dataset.available.value ?: "N/A")
            DetailRow("Compression", dataset.compression.value ?: "N/A")
            DetailRow("Ratio", dataset.compressratio.value ?: "N/A")
            DetailRow("Deduplication", dataset.deduplication.value ?: "N/A")
            DetailRow("Sync", dataset.sync.value ?: "N/A")
            DetailRow("Case Sensitivity", dataset.casesensitivity.value ?: "N/A")
            DetailRow("Read Only", dataset.readonly.value ?: "N/A")
            DetailRow("Encryption", if (dataset.encrypted) "Yes" else "No")
        }
    }
}

@Composable
fun EmptySelectionState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Select a dataset",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoDatasetsCard(
    poolName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Datasets Found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "No datasets are currently available in the '$poolName' storage pool.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun DeleteDatasetDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete New Dataset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Are you Sure you want to delete This dataset",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDelete() },
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}