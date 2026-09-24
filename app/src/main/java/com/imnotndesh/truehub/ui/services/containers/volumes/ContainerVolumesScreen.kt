package com.imnotndesh.truehub.ui.services.containers.volumes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerVolumesScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    viewModel: ContainerVolumesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Virt.VolumeEntry?>(null) }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Container volumes",
                subtitle = "Virtual disks and importable media",
                 isLoading = state.isLoading,
                 isRefreshing = state.isRefreshing,
                 error = state.error,
                 onDismissError = viewModel::load,
                 manager = manager,
                 onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshContent(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                when {
                    state.isLoading && state.volumes.isEmpty() -> LoadingScreen("Loading volumes")
                    state.volumes.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("No virtual volumes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                            Text(
                                "Create a volume or import an ISO/ZVOL.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.volumes, key = { it.id }) { volume ->
                                VolumeRow(
                                    volume = volume,
                                    onDelete = { pendingDelete = volume }
                                )
                            }
                        }
                    }
                }
            }
            ExpressiveActionFab(
                icon = Icons.Default.Add,
                label = "Create volume",
                onClick = { showCreateSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }
    }

    if (showCreateSheet) {
        CreateVolumeSheet(
            poolChoices = state.poolChoices,
            onDismiss = { showCreateSheet = false },
            onCreate = { name, pool, content, type ->
                viewModel.create(name, pool, content, type)
                showCreateSheet = false
            }
        )
    }

    pendingDelete?.let { volume ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete volume?") },
            text = { Text("This removes ${volume.name} if it is not attached to an instance.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(volume)
                        pendingDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun VolumeRow(volume: Virt.VolumeEntry, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(volume.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    listOf(volume.storagePool, volume.contentType, volume.type).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (volume.usedBy.isNotEmpty()) {
                    Text("In use", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onDelete, enabled = volume.usedBy.isEmpty()) {
                Icon(Icons.Default.Delete, contentDescription = "Delete volume", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateVolumeSheet(
    poolChoices: Map<String, String>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var pool by rememberSaveable { mutableStateOf("") }
    var contentType by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create volume", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
            OutlinedTextField(
                value = pool,
                onValueChange = { pool = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Storage pool") },
                placeholder = { Text(poolChoices.keys.firstOrNull().orEmpty()) },
                singleLine = true
            )
            OutlinedTextField(contentType, { contentType = it }, Modifier.fillMaxWidth(), label = { Text("Content type") }, singleLine = true)
            OutlinedTextField(type, { type = it }, Modifier.fillMaxWidth(), label = { Text("Volume type (optional)") }, singleLine = true)
            Button(
                onClick = { onCreate(name, pool, contentType, type) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && pool.isNotBlank() && contentType.isNotBlank()
            ) { Text("Create") }
        }
    }
}
