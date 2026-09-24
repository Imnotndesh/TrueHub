package com.imnotndesh.truehub.ui.services.containers.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.helpers.virtualizationOperationLabel
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveIconActionButton
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerSheet
import com.imnotndesh.truehub.ui.homepage.dataset.toFullMountPath
import com.imnotndesh.truehub.ui.services.containers.containerShape
import com.imnotndesh.truehub.ui.services.containers.containerStatusIcon
import com.imnotndesh.truehub.ui.services.containers.containerStatusLabel
import com.imnotndesh.truehub.ui.services.containers.containerStatusTone
import com.imnotndesh.truehub.ui.services.containers.containerTypeContainerColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeContentColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeIcon

@Composable
fun ContainerInfoScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit = onNavigateBack,
    onManageDevices: () -> Unit = onNavigateBack,
    onManageVolumes: () -> Unit = onNavigateBack,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val operations by VirtualizationJobTracker.operations.collectAsState()
    val operation = operations[containerId]
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(containerId) {
        viewModel.load()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete container?") },
            text = { Text("This permanently removes the container and its configuration. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete { onNavigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportSheet) {
        ExportImageSheet(
            manager = manager,
            availableZvols = state.availableZvols,
            isBusy = operation != null,
            onDismiss = { showExportSheet = false },
            onExport = { format, directory, zvol ->
                showExportSheet = false
                viewModel.exportDiskImage(format, directory, zvol)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { _ ->
            val container = state.container
            when {
                state.isLoading && container == null -> LoadingScreen("Loading Container")
                container == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Container is unavailable",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = viewModel::load,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    PullToRefreshContent(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::load,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ContainerInfoPane(
                            container = container,
                            onClose = onNavigateBack,
                            showInlineHeader = false,
                            contentTopPadding = 96.dp,
                            topContent = {
                                ContainerHeroCard(
                                    container = container,
                                    operationLabel = operation?.operation,
                                    progressText = operation?.job?.progress?.description
                                        ?.takeIf { it.isNotBlank() },
                                    progressPercent = operation?.job?.progress?.percent,
                                    onStart = viewModel::start,
                                    onStop = viewModel::stop,
                                    onEdit = onEdit
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Devices,
                                    title = "Devices",
                                    subtitle = "Attached hardware, NICs and passthrough",
                                    onClick = onManageDevices
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Storage,
                                    title = "Volumes",
                                    subtitle = "Virtual disks and importable media",
                                    onClick = onManageVolumes
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Edit,
                                    title = "Edit configuration",
                                    subtitle = "CPU, memory, VNC and boot settings",
                                    onClick = onEdit
                                )
                            },
                            bottomActions = {
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Upload,
                                    title = "Export boot image",
                                    subtitle = "Export a container disk/volume to a disk image file",
                                    onClick = { showExportSheet = true },
                                    enabled = operation == null
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ContainerDangerZone(
                                    isDeleting = operation?.operation == "DELETING",
                                    deletingPercent = operation?.job?.progress?.percent,
                                    canDelete = operation == null && container.status == Virt.Status.STOPPED,
                                    onDelete = { showDeleteDialog = true }
                                )
                            }
                        )
                    }
                }
            }
        }
        MinimalBackHeader(
            onBackPressed = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        )
    }
}

@Composable
private fun ContainerHeroCard(
    container: Virt.ContainerResponse,
    operationLabel: String?,
    progressText: String?,
    progressPercent: Int?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit
) {
    val isRunning = container.status == Virt.Status.RUNNING
    val isBusy = operationLabel != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${container.id.take(12)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExpressiveStatusChip(
                        label = containerStatusLabel(container.status),
                        tone = containerStatusTone(container.status),
                        icon = containerStatusIcon(container.status)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                ExpressiveIconActionButton(
                    icon = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Stop container" else "Start container",
                    isPrimary = !isRunning,
                    enabled = !isBusy,
                    onClick = { if (isRunning) onStop() else onStart() },
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                ExpressiveIconActionButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit configuration",
                    isPrimary = false,
                    enabled = !isBusy,
                    onClick = onEdit,
                    modifier = Modifier.size(52.dp)
                )
            }

            if (isBusy) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressText ?: operationLabel?.let { virtualizationOperationLabel(it) }.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${progressPercent ?: 0}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (progressPercent?.coerceIn(0, 100)?.toFloat() ?: 0f) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
private fun ContainerDangerZone(
    isDeleting: Boolean,
    deletingPercent: Int?,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    ExpressiveSection(title = "Danger Zone", icon = Icons.Default.Delete) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Removing this container permanently deletes it and its configuration. Data in datasets is not removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDelete,
                    enabled = canDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Deleting… ${deletingPercent ?: 0}%",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Delete Container", fontWeight = FontWeight.Bold)
                    }
                }
                if (!canDelete) {
                    Text(
                        text = "Stop the container to delete it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private val EXPORT_FORMATS = listOf("QCOW2", "RAW", "VDI", "VPC", "VMDK", "QED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportImageSheet(
    manager: TrueNASApiManager,
    availableZvols: List<String>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onExport: (String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var format by rememberSaveable { mutableStateOf("QCOW2") }
    var directory by rememberSaveable { mutableStateOf("") }
    var zvol by rememberSaveable { mutableStateOf(availableZvols.firstOrNull().orEmpty()) }
    var showFormatPicker by remember { mutableStateOf(false) }
    var showZvolPicker by remember { mutableStateOf(false) }
    var showDatasetPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Export boot image",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveChoiceField(
                label = "Format",
                value = format,
                placeholder = "Select format",
                onClick = { showFormatPicker = true }
            )
            ExpressiveTextField(
                value = directory,
                onValueChange = { directory = it },
                label = "Target directory",
                supportingText = { Text("Directory on the server to write the image to") },
                trailingIcon = {
                    IconButton(onClick = { showDatasetPicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Choose directory")
                    }
                }
            )
            if (availableZvols.isNotEmpty()) {
                ExpressiveChoiceField(
                    label = "Source ZFS volume",
                    value = zvol,
                    placeholder = "Select a volume",
                    onClick = { showZvolPicker = true }
                )
            } else {
                ExpressiveTextField(
                    value = zvol,
                    onValueChange = { zvol = it },
                    label = "Source ZFS volume",
                    supportingText = { Text("No volumes found on this container; enter the path manually") }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = { onExport(format, directory, zvol) },
                    enabled = !isBusy && directory.isNotBlank() && zvol.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Export") }
            }
        }
    }

    if (showFormatPicker) {
        ExpressiveChoiceSheet(
            title = "Choose format",
            choices = EXPORT_FORMATS.map { ExpressiveChoice(key = it, label = it) },
            selectedKey = format,
            searchable = false,
            height = 320,
            onDismiss = { showFormatPicker = false },
            onSelect = {
                format = it
                showFormatPicker = false
            }
        )
    }

    if (showZvolPicker) {
        ExpressiveChoiceSheet(
            title = "Choose source ZFS volume",
            choices = availableZvols.map { ExpressiveChoice(key = it, label = it) },
            selectedKey = zvol.ifBlank { null },
            searchable = availableZvols.size > 6,
            height = 320,
            onDismiss = { showZvolPicker = false },
            onSelect = {
                zvol = it
                showZvolPicker = false
            }
        )
    }

    if (showDatasetPicker) {
        DatasetPickerSheet(
            manager = manager,
            config = DatasetPickerConfig(
                title = "Choose target directory",
                confirmLabel = "Use directory"
            ),
            onConfirm = { selected ->
                selected.firstOrNull()?.toFullMountPath()?.let { directory = it }
                showDatasetPicker = false
            },
            onDismiss = { showDatasetPicker = false }
        )
    }
}
