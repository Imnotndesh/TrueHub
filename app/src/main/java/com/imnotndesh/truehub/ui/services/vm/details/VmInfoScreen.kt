package com.imnotndesh.truehub.ui.services.vm.details
import com.imnotndesh.truehub.ui.components.expressive.DetailHeroCard
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.VirtualizationJobTracker
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveDivider
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveIconActionButton
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.InfoRow
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerSheet
import com.imnotndesh.truehub.ui.homepage.dataset.toFullMountPath
import kotlinx.coroutines.launch
import com.imnotndesh.truehub.ui.services.vm.vmShape
import com.imnotndesh.truehub.ui.services.vm.vmStatusIcon
import com.imnotndesh.truehub.ui.services.vm.vmStatusLabel
import com.imnotndesh.truehub.ui.services.vm.vmStatusTone

@Composable
fun VmInfoScreen(
    vmId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onManageDevices: () -> Unit = onNavigateBack,
    onEdit: () -> Unit = onNavigateBack,
    onOpenConsole: () -> Unit = onNavigateBack,
    viewModel: VmDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val operations by VirtualizationJobTracker.operations.collectAsState()
    val operation = operations[vmId.toString()]
    var showStartDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }
    var showPowerOffDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var isDownloadingLog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null && state.vm != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logFileName = "vm-" + (state.vm?.name ?: "log").replace(Regex("[^A-Za-z0-9._-]"), "_") + ".log"
    val logLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            isDownloadingLog = true
            scope.launch {
                val stream = context.contentResolver.openOutputStream(uri)
                val ok = if (stream != null) {
                    stream.use { viewModel.downloadLog(logFileName, it) }
                } else {
                    false
                }
                isDownloadingLog = false
                if (ok) ToastManager.showSuccess("Log saved") else ToastManager.showError("Failed to save log")
            }
        }
    }

    if (showStartDialog) {
        StartVmDialog(
            vmName = state.vm?.name ?: "VM",
            onConfirm = { overcommit ->
                viewModel.start(overcommit)
                showStartDialog = false
            },
            onDismiss = { showStartDialog = false }
        )
    }
    if (showStopDialog) {
        StopVmDialog(
            vmName = state.vm?.name ?: "VM",
            onConfirm = { force, forceAfterTimeout ->
                viewModel.stop(force, forceAfterTimeout)
                showStopDialog = false
            },
            onDismiss = { showStopDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteVmDialog(
            vmName = state.vm?.name ?: "VM",
            onConfirm = { deleteZvols, forceDelete ->
                showDeleteDialog = false
                viewModel.delete(deleteZvols, forceDelete) { onNavigateBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showCloneDialog) {
        CloneVmDialog(
            vmName = state.vm?.name ?: "VM",
            onConfirm = { name ->
                showCloneDialog = false
                viewModel.clone(name) { }
            },
            onDismiss = { showCloneDialog = false }
        )
    }
    if (showExportSheet) {
        VmExportImageSheet(
            manager = manager,
            vm = state.vm,
            isBusy = operation != null,
            onDismiss = { showExportSheet = false },
            onExport = { format, directory, zvol ->
                showExportSheet = false
                viewModel.exportDiskImage(format, directory, zvol)
            }
        )
    }
    if (showPowerOffDialog) {
        AlertDialog(
            onDismissRequest = { showPowerOffDialog = false },
            title = { Text("Power off ${state.vm?.name ?: "VM"}?") },
            text = {
                Text("This immediately cuts power to the VM, like pulling the plug. Unsaved data may be lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPowerOffDialog = false
                        viewModel.powerOff()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Power Off") }
            },
            dismissButton = {
                TextButton(onClick = { showPowerOffDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { _ ->
            val vm = state.vm
            when {
                state.isLoading && vm == null -> LoadingScreen("Loading Virtual Machine")
                vm == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Virtual machine is unavailable",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = viewModel::load, modifier = Modifier.padding(top = 16.dp)) {
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
                        VmInfoPane(
                            vm = vm,
                            onClose = onNavigateBack,
                            showInlineHeader = false,
                            contentTopPadding = 96.dp,
                            operation = operation,
                            topContent = {
                                VmHeroCard(
                                    vm = vm,
                                    isBusy = operation != null,
                                    onStart = { showStartDialog = true },
                                    onStop = { showStopDialog = true },
                                    onRestart = viewModel::restart,
                                    onSuspend = viewModel::suspend,
                                    onResume = viewModel::resume,
                                    onPowerOff = { showPowerOffDialog = true }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Devices,
                                    title = "Devices",
                                    subtitle = "Disks, NICs, display and passthrough",
                                    onClick = onManageDevices
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Edit,
                                    title = "Edit configuration",
                                    subtitle = "CPU, memory, boot and behaviour",
                                    onClick = onEdit
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Monitor,
                                    title = "Open console",
                                    subtitle = when {
                                        !vm.status.state.equals("running", ignoreCase = true) ->
                                            "Start the VM to use the console"
                                        !vm.display_available ->
                                            "No display device configured"
                                        else ->
                                            "Connect to the VM display in-app"
                                    },
                                    onClick = onOpenConsole,
                                    enabled = vm.status.state.equals("running", ignoreCase = true) &&
                                        vm.display_available && operation == null
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.ContentCopy,
                                    title = "Clone VM",
                                    subtitle = "Create a copy of this virtual machine",
                                    onClick = { showCloneDialog = true },
                                    enabled = operation == null
                                )
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Upload,
                                    title = "Export boot image",
                                    subtitle = "Export a disk volume to a disk image file",
                                    onClick = { showExportSheet = true },
                                    enabled = operation == null
                                )
                            },
                            bottomActions = {
                                VmDiagnosticsSection(
                                    state = state,
                                    isDownloadingLog = isDownloadingLog,
                                    onDownloadLog = { logLauncher.launch(logFileName) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                VmDangerZone(
                                    enabled = operation == null,
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
private fun VmHeroCard(
    vm: Vm.VmQueryResponse,
    isBusy: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onSuspend: () -> Unit,
    onResume: () -> Unit,
    onPowerOff: () -> Unit
) {
    val state = vm.status.state.lowercase()
    DetailHeroCard(
        title = vm.name,
        subtitle = vm.description,
        leading = {
            ExpressiveShapeBox(
                shape = vmShape(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        badge = {
            ExpressiveStatusChip(
                label = vmStatusLabel(vm.status.state),
                tone = vmStatusTone(vm.status.state),
                icon = vmStatusIcon(vm.status.state)
            )
        },
        actions = {
            when (state) {
                "running" -> {
                    ExpressiveIconActionButton(
                        icon = Icons.Default.Stop,
                        contentDescription = "Stop",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = onStop,
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveIconActionButton(
                        icon = Icons.Default.RestartAlt,
                        contentDescription = "Restart",
                        isPrimary = false,
                        enabled = !isBusy,
                        onClick = onRestart,
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveIconActionButton(
                        icon = Icons.Default.Pause,
                        contentDescription = "Suspend",
                        isPrimary = false,
                        enabled = !isBusy,
                        onClick = onSuspend,
                        modifier = Modifier.weight(1f)
                    )
                    ExpressiveIconActionButton(
                        icon = Icons.Default.PowerOff,
                        contentDescription = "Power Off",
                        isPrimary = false,
                        isDanger = true,
                        enabled = !isBusy,
                        onClick = onPowerOff,
                        modifier = Modifier.weight(1f)
                    )
                }
                "suspended" -> {
                    ExpressiveIconActionButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = onResume,
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    ExpressiveIconActionButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        isPrimary = true,
                        enabled = !isBusy,
                        onClick = onStart,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun VmDangerZone(
    enabled: Boolean,
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
                    text = "Removing this virtual machine permanently deletes it. You can also delete its associated zvols.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = onDelete,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Virtual Machine", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StartVmDialog(
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
                ToggleLine("Overcommit memory", overcommit) { overcommit = it }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(overcommit) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StopVmDialog(
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
private fun DeleteVmDialog(
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
            .padding(top = 12.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun VmDiagnosticsSection(
    state: VmDetailUiState,
    isDownloadingLog: Boolean,
    onDownloadLog: () -> Unit
) {
    ExpressiveSection(title = "Diagnostics", icon = Icons.Default.Info) {
        ExpressiveInfoCard {
            Column {
                state.memoryUsageMb?.let {
                    InfoRow(label = "Memory usage", value = "${it} MB")
                    ExpressiveDivider()
                }
                state.memoryInfo?.let { info ->
                    (info["minimum_memory_requested"] as? Number)?.let {
                        InfoRow(label = "Minimum memory requested", value = "$it MB")
                        ExpressiveDivider()
                    }
                    (info["total_memory_requested"] as? Number)?.let {
                        InfoRow(label = "Total memory requested", value = "$it MB")
                        ExpressiveDivider()
                    }
                }
                state.vmemoryInUse?.let { values ->
                    values.forEach { (key, value) ->
                        InfoRow(label = "In use · $key", value = formatVmMemory(value.toInt()))
                        ExpressiveDivider()
                    }
                }
                state.virtualizationDetails?.let { details ->
                    (details["supported"] as? Boolean)?.let {
                        InfoRow(label = "Virtualization supported", value = if (it) "Yes" else "No")
                        ExpressiveDivider()
                    }
                }
                state.flags?.let { flags ->
                    flags.forEach { (key, value) ->
                        InfoRow(label = key, value = if (value) "Yes" else "No")
                        ExpressiveDivider()
                    }
                }
                state.displayDevices.forEachIndexed { index, device ->
                    InfoRow(label = "Display ${index + 1}", value = device["resolution"]?.toString() ?: "Connected")
                    ExpressiveDivider()
                }
                state.logPath?.let {
                    InfoRow(label = "Log file", value = it)
                    ExpressiveDivider()
                    Button(
                        onClick = onDownloadLog,
                        enabled = !isDownloadingLog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (isDownloadingLog) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save log to device")
                        }
                    }
                    ExpressiveDivider()
                }
                state.liveStatus?.let {
                    InfoRow(label = "Live status", value = it)
                    ExpressiveDivider()
                }
                state.consoleString?.let {
                    InfoRow(label = "Console", value = it)
                }
                if (
                    state.memoryUsageMb == null && state.memoryInfo == null && state.vmemoryInUse == null &&
                    state.flags == null && state.virtualizationDetails == null &&
                    state.displayDevices.isEmpty() && state.logPath == null && state.consoleString == null
                ) {
                    Text(
                        text = "No diagnostics available for this VM.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CloneVmDialog(
    vmName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("$vmName-clone") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clone $vmName") },
        text = {
            Column {
                Text("Create a copy of this virtual machine.")
                ExpressiveTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Clone name",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text("Clone") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private val VM_EXPORT_FORMATS = listOf("QCOW2", "RAW", "VDI", "VPC", "VMDK", "QED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VmExportImageSheet(
    manager: TrueNASApiManager,
    vm: Vm.VmQueryResponse?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onExport: (String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var format by remember { mutableStateOf("QCOW2") }
    var directory by remember { mutableStateOf("") }
    var zvol by remember { mutableStateOf("") }
    var showFormatPicker by remember { mutableStateOf(false) }
    var showZvolPicker by remember { mutableStateOf(false) }
    var showDatasetPicker by remember { mutableStateOf(false) }

    val zvolChoices = remember(vm) {
        vm?.devices.orEmpty()
            .filter { it.attributes?.get("dtype")?.toString().equals("DISK", ignoreCase = true) }
            .mapNotNull { device ->
                device.attributes?.get("zvol_name")?.toString()
                    ?: device.attributes?.get("path")?.toString()
            }
            .distinct()
    }

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
            if (zvolChoices.isNotEmpty()) {
                ExpressiveChoiceField(
                    label = "Source ZFS volume",
                    value = zvol.ifBlank { "Select a volume" },
                    placeholder = "Select a volume",
                    onClick = { showZvolPicker = true }
                )
            } else {
                ExpressiveTextField(
                    value = zvol,
                    onValueChange = { zvol = it },
                    label = "Source ZFS volume",
                    supportingText = { Text("No volumes found; enter the path manually") }
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
            choices = VM_EXPORT_FORMATS.map { ExpressiveChoice(key = it, label = it) },
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
            choices = zvolChoices.map { ExpressiveChoice(key = it, label = it) },
            selectedKey = zvol.ifBlank { null },
            searchable = zvolChoices.size > 6,
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
            config = DatasetPickerConfig(title = "Choose target directory", confirmLabel = "Use directory"),
            onConfirm = { selected ->
                selected.firstOrNull()?.toFullMountPath()?.let { directory = it }
                showDatasetPicker = false
            },
            onDismiss = { showDatasetPicker = false }
        )
    }
}
