package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    if (state.rootNode != null) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item {
                                        DatasetTreeItem(
                                            dataset = state.rootNode,
                                            level = 0,
                                            expandedIds = viewModel.expandedNodeIds,
                                            selectedId = selectedDataset?.id,
                                            onToggle = { viewModel.toggleExpansion(it) },
                                            onSelect = { viewModel.selectNode(it) }
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Box(
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                selectedDataset?.let { dataset ->
                                    DatasetDetailsPanel(dataset)
                                } ?: Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Select a dataset to view details", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Pool '$poolName' not found.", color = MaterialTheme.colorScheme.error)
                        }
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

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        Color.Transparent

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .clickable {
                    onSelect(dataset)
                    if (hasChildren) onToggle(dataset.id)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .padding(start = (level * 20).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasChildren) {
                    if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight
                } else {
                    Icons.Default.ChevronRight
                },
                contentDescription = "Expand",
                modifier = Modifier.size(20.dp),
                tint = if (hasChildren) MaterialTheme.colorScheme.onSurface else Color.Transparent
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = if (isRoot) Icons.Default.Storage else if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = if (isRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isRoot) dataset.name else dataset.name.substringAfterLast("/"),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
fun DatasetDetailsPanel(dataset: Storage.ZfsDataset) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Details: ${dataset.name.substringAfterLast("/")}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Full Path", dataset.name)
                DetailRow("Mountpoint", dataset.mountpoint)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow("Used", dataset.used.value ?: "N/A")
                DetailRow("Available", dataset.available.value ?: "N/A")
                DetailRow("Compression", dataset.compression.value ?: "N/A")
                DetailRow("Ratio", dataset.compressratio.value ?: "N/A")
                DetailRow("Deduplication", dataset.deduplication.value ?: "N/A")
                DetailRow("Sync", dataset.sync.value ?: "N/A")
                DetailRow("Case Sensitivity", dataset.casesensitivity.value ?: "N/A")
                DetailRow("Read Only", dataset.readonly.value ?: "N/A")
                DetailRow("Encryption", if(dataset.encrypted) "Yes" else "No")
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp),
            textAlign = TextAlign.End
        )
    }
}