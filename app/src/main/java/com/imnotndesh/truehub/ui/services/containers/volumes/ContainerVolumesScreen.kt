package com.imnotndesh.truehub.ui.services.containers.volumes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.imnotndesh.truehub.data.models.sizeInMb
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView
import com.imnotndesh.truehub.ui.services.containers.MIN_VOLUME_SIZE_MB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerVolumesScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    viewModel: ContainerVolumesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var showImportSheet by remember { mutableStateOf(false) }
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
            if (state.isSaving && state.importJob != null) {
                JobProgressView(
                    title = "Importing ISO",
                    description = state.importJob?.progress?.description?.takeIf { it.isNotBlank() }
                        ?: "Copying the ISO into a new volume",
                    progress = state.importJob?.progress?.percent ?: 0,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                PullToRefreshContent(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when {
                        state.isLoading && state.volumes.isEmpty() -> LoadingScreen("Loading volumes")
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item(key = "import") {
                                    ExpressiveEntryCard(
                                        icon = Icons.Default.Upload,
                                        title = "Import ISO image",
                                        subtitle = "Import an ISO file already on the server",
                                        onClick = { showImportSheet = true }
                                    )
                                }
                                if (state.volumes.isEmpty()) {
                                    item(key = "empty") {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 48.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
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
                                } else {
                                    items(state.volumes, key = { it.id }) { volume ->
                                        VolumeRow(
                                            volume = volume,
                                            onEdit = { viewModel.beginEdit(volume) },
                                            onDelete = { pendingDelete = volume }
                                        )
                                    }
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
    }

    if (showCreateSheet) {
        CreateVolumeSheet(
            poolChoices = state.poolChoices,
            onDismiss = { showCreateSheet = false },
            onCreate = { name, pool, size ->
                viewModel.create(name, pool, size)
                showCreateSheet = false
            }
        )
    }

    state.editingVolume?.let { volume ->
        VolumeEditSheet(
            volume = volume,
            isSaving = state.isSaving,
            onDismiss = viewModel::cancelEdit,
            onSave = { size -> viewModel.resize(volume.id, size) }
        )
    }

    if (showImportSheet) {
        ImportIsoSheet(
            manager = manager,
            poolChoices = state.poolChoices,
            onDismiss = { showImportSheet = false },
            onImport = { name, isoLocation, pool ->
                showImportSheet = false
                viewModel.importIso(name, isoLocation, pool)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VolumeRow(
    volume: Virt.VolumeEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sizeLabel = volume.sizeInMb()?.let { "$it MB" }
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExpressiveShapeBox(
                shape = MaterialShapes.Cookie7Sided,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = volume.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOfNotNull(volume.storagePool, volume.contentType, sizeLabel).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (volume.usedBy.isNotEmpty()) {
                    Text(
                        text = "In use",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onEdit, enabled = volume.usedBy.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, enabled = volume.usedBy.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete volume",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateVolumeSheet(
    poolChoices: Map<String, String>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var pool by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableStateOf("1024") }
    var showPoolPicker by remember { mutableStateOf(false) }
    val sizeValue = size.trim().toIntOrNull()
    val sizeValid = sizeValue != null && sizeValue >= MIN_VOLUME_SIZE_MB

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create volume",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name"
            )
            ExpressiveChoiceField(
                label = "Storage pool",
                value = poolChoices[pool] ?: pool,
                placeholder = poolChoices.keys.firstOrNull().orEmpty(),
                onClick = { showPoolPicker = true }
            )
            ExpressiveTextField(
                value = size,
                onValueChange = { size = it },
                label = "Size (MB)",
                supportingText = {
                    Text("Minimum $MIN_VOLUME_SIZE_MB MB")
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = { onCreate(name, pool, sizeValue ?: 0) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = name.isNotBlank() && pool.isNotBlank() && sizeValid
                ) { Text("Create") }
            }
        }
    }

    if (showPoolPicker) {
        ExpressiveChoiceSheet(
            title = "Choose storage pool",
            choices = poolChoices.map { (key, value) ->
                ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
            },
            selectedKey = pool.ifBlank { null },
            onDismiss = { showPoolPicker = false },
            onSelect = {
                pool = it
                showPoolPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeEditSheet(
    volume: Virt.VolumeEntry,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var size by rememberSaveable(volume.id) { mutableStateOf(volume.sizeInMb()?.toString().orEmpty()) }
    val sizeValue = size.trim().toIntOrNull()
    val sizeValid = sizeValue != null && sizeValue >= MIN_VOLUME_SIZE_MB

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resize volume",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = volume.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ExpressiveTextField(
                value = size,
                onValueChange = { size = it },
                label = "Size (MB)",
                supportingText = {
                    Text("Minimum $MIN_VOLUME_SIZE_MB MB")
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = { onSave(sizeValue ?: 0) },
                    enabled = sizeValid && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Save") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportIsoSheet(
    manager: TrueNASApiManager,
    poolChoices: Map<String, String>,
    onDismiss: () -> Unit,
    onImport: (String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var isoLocation by rememberSaveable { mutableStateOf("") }
    var pool by rememberSaveable { mutableStateOf("") }
    var showPoolPicker by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Import ISO image",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = name,
                onValueChange = { name = it },
                label = "Volume name"
            )
            ExpressiveTextField(
                value = isoLocation,
                onValueChange = { isoLocation = it },
                label = "ISO path on server",
                supportingText = { Text("Absolute path on the server, e.g. /mnt/Main/isos/debian.iso") },
                trailingIcon = {
                    IconButton(onClick = { showFilePicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Browse server files")
                    }
                }
            )
            ExpressiveChoiceField(
                label = "Storage pool",
                value = poolChoices[pool] ?: pool,
                placeholder = poolChoices.keys.firstOrNull().orEmpty(),
                onClick = { showPoolPicker = true }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = { onImport(name, isoLocation, pool) },
                    enabled = name.isNotBlank() && isoLocation.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Import") }
            }
        }
    }

    if (showPoolPicker) {
        ExpressiveChoiceSheet(
            title = "Choose storage pool",
            choices = poolChoices.map { (key, value) ->
                ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
            },
            selectedKey = pool.ifBlank { null },
            onDismiss = { showPoolPicker = false },
            onSelect = {
                pool = it
                showPoolPicker = false
            }
        )
    }

    if (showFilePicker) {
        com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerSheet(
            manager = manager,
            config = com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerConfig(
                title = "Choose ISO file",
                confirmLabel = "Use ISO",
                startPath = "/mnt",
                showFiles = true,
                fileExtensions = listOf("iso")
            ),
            onConfirm = {
                isoLocation = it
                showFilePicker = false
            },
            onDismiss = { showFilePicker = false }
        )
    }
}
