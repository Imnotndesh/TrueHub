package com.imnotndesh.truehub.ui.services.vm.devices
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView
import androidx.lifecycle.compose.LifecycleResumeEffect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmDevicesScreen(
    vmId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onAddDevice: () -> Unit,
    onEditDevice: (Int) -> Unit,
    viewModel: VmDevicesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<Vm.DeviceEntry?>(null) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showConvertSheet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "VM devices",
                subtitle = "Disks, NICs, display and passthrough",
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                error = state.error,
                onDismissError = viewModel::clearError,
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (state.isLoading && state.devices.isEmpty()) {
                    LoadingScreen("Loading devices")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item(key = "import") {
                            ExpressiveEntryCard(
                                icon = Icons.Default.Download,
                                title = "Import disk image",
                                subtitle = "Convert a disk image file into a ZFS volume",
                                onClick = { showImportSheet = true }
                            )
                        }
                        item(key = "convert") {
                            ExpressiveEntryCard(
                                icon = Icons.Default.SwapHoriz,
                                title = "Convert image / ZFS volume",
                                subtitle = "Convert between a disk image file and a ZFS volume",
                                onClick = { showConvertSheet = true }
                            )
                        }
                        if (state.devices.isEmpty()) {
                            item(key = "empty") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "No devices attached",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                    Text(
                                        "Add a disk, NIC, display, or passthrough device.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        } else {
                            items(state.devices, key = { it.id ?: it.hashCode() }) { device ->
                                VmDeviceRow(
                                    device = device,
                                    isDeleting = state.deletingDeviceId == device.id,
                                    onEdit = { device.id?.let(onEditDevice) },
                                    onDelete = { pendingDelete = device }
                                )
                            }
                        }
                    }
                }
            }
            ExpressiveActionFab(
                icon = Icons.Default.Add,
                label = "Add device",
                onClick = { if (!state.isImporting) onAddDevice() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
            if (state.isImporting) {
                JobProgressView(
                    title = "Importing disk image",
                    description = "Preparing the import",
                    progress = 0,
                    indeterminate = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showImportSheet) {
        ImportVmDiskImageSheet(
            manager = manager,
            onDismiss = { showImportSheet = false },
            onImport = { diskimg, zvol ->
                showImportSheet = false
                viewModel.importDiskImage(diskimg, zvol)
            }
        )
    }

    if (showConvertSheet) {
        ConvertVmDiskImageSheet(
            onDismiss = { showConvertSheet = false },
            onConvert = { source, destination ->
                showConvertSheet = false
                viewModel.convertDiskImage(source, destination)
            }
        )
    }

    pendingDelete?.let { device ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove device?") },
            text = { Text("This removes ${device.dtype ?: "the device"} from the VM configuration.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        device.id?.let(viewModel::delete)
                        pendingDelete = null
                    }
                ) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun VmDeviceRow(
    device: Vm.DeviceEntry,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val attributes = device.attributes.orEmpty()
    val summary = buildList {
        device.order?.let { add("Order $it") }
        attributes["path"]?.toString()?.let { add(it) }
        attributes["zvol_name"]?.toString()?.let { add(it) }
        attributes["nic_attach"]?.toString()?.let { add(it) }
        attributes["resolution"]?.toString()?.let { add(it) }
        attributes["port"]?.toString()?.let { add("Port $it") }
        attributes["pptdev"]?.toString()?.let { add(it) }
        attributes["device"]?.toString()?.let { add(it) }
    }.joinToString(" · ")
    ExpressiveEntryCard(
        icon = Icons.Default.Memory,
        title = device.dtype ?: "Device",
        subtitle = summary.ifBlank { "No details" },
        onClick = onEdit,
        enabled = !isDeleting && device.id != null,
        trailing = {
            IconButton(onClick = onDelete, enabled = !isDeleting) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove device",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportVmDiskImageSheet(
    manager: TrueNASApiManager,
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var diskimg by rememberSaveable { mutableStateOf("") }
    var zvol by rememberSaveable { mutableStateOf("") }
    var showFilePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Import disk image",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = diskimg,
                onValueChange = { diskimg = it },
                label = "Disk image path",
                supportingText = { Text("Path to the image on the server (QCOW2, RAW, VDI, VPC, VMDK)") },
                trailingIcon = {
                    IconButton(onClick = { showFilePicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Browse server files")
                    }
                }
            )
            ExpressiveTextField(
                value = zvol,
                onValueChange = { zvol = it },
                label = "Target ZFS volume",
                supportingText = { Text("ZFS volume (zvol) the image will be imported into") }
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
                    onClick = { onImport(diskimg, zvol) },
                    enabled = diskimg.isNotBlank() && zvol.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Import") }
            }
        }
    }

    if (showFilePicker) {
        ServerPathPickerSheet(
            manager = manager,
            config = ServerPathPickerConfig(
                title = "Choose disk image",
                confirmLabel = "Use image",
                startPath = "/mnt",
                showFiles = true,
                fileExtensions = listOf("qcow2", "raw", "img", "vdi", "vpc", "vmdk", "qed")
            ),
            onConfirm = {
                diskimg = it
                showFilePicker = false
            },
            onDismiss = { showFilePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConvertVmDiskImageSheet(
    onDismiss: () -> Unit,
    onConvert: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var source by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Convert image / ZFS volume",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = source,
                onValueChange = { source = it },
                label = "Source",
                supportingText = { Text("Disk image file or ZFS volume path") }
            )
            ExpressiveTextField(
                value = destination,
                onValueChange = { destination = it },
                label = "Destination",
                supportingText = { Text("The direction is inferred from the extension") }
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
                    onClick = { onConvert(source, destination) },
                    enabled = source.isNotBlank() && destination.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Convert") }
            }
        }
    }
}
