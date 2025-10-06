package com.example.truehub.ui.services.vm

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.data.models.Vm
import com.example.truehub.ui.components.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmScreen(
    manager: TrueNASApiManager,
    viewModel: VmScreenViewModel = viewModel(
        factory = VmScreenViewModel.VmViewModelFactory(manager)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Content based on state
    when {
        uiState.isLoading -> {
            LoadingScreen("Loading Virtual Machines")
        }
        uiState.vms.isEmpty() && !uiState.isLoading -> {
            EmptyVmContent()
        }
        else -> {
            VmsContent(
                vms = uiState.vms,
                isRefreshing = uiState.isRefreshing,
                onStartVm = { id,overcommit -> viewModel.startVm(id,overcommit) },
                onStopVm = { id,force,timeout -> viewModel.stopVm(id, force,timeout) },
                onRestartVm = { id -> viewModel.restartVm(id) },
                onSuspendVm = { id -> viewModel.suspendVm(id) },
                onResumeVm = { id -> viewModel.resumeVm(id) },
                onPowerOffVm = { id -> viewModel.powerOffVm(id) },
                onDeleteVm = { id -> viewModel.deleteVm(id) },
                operationJob = uiState.operationJobs
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading virtual machines...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    onStartVm: (Int,Boolean) -> Unit,
    onStopVm: (Int, Boolean, Boolean) -> Unit,
    onRestartVm: (Int) -> Unit,
    onSuspendVm: (Int) -> Unit,
    onResumeVm: (Int) -> Unit,
    onPowerOffVm: (Int) -> Unit,
    onDeleteVm: (Int) -> Unit,
    operationJob: Map<Int, System.Job>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (isRefreshing) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        items(vms) { vm ->
            VmCard(
                vm = vm,
                onStartVm = {overcommit -> onStartVm(vm.id,overcommit) },
                onStopVm = {force, timeout -> onStopVm(vm.id,force,timeout) },
                onRestartVm = { onRestartVm(vm.id) },
                onSuspendVm = { onSuspendVm(vm.id) },
                onResumeVm = { onResumeVm(vm.id) },
                onPowerOffVm = { onPowerOffVm(vm.id) },
                onDeleteVm = { onDeleteVm(vm.id) },
                operationJob = operationJob[vm.id]
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VmCard(
    vm: Vm.VmQueryResponse,
    onStartVm: (Boolean) -> Unit,
    onStopVm: (Boolean,Boolean) -> Unit,
    onRestartVm: () -> Unit,
    onSuspendVm: () -> Unit,
    onResumeVm: () -> Unit,
    onPowerOffVm: () -> Unit,
    onDeleteVm: () -> Unit,
    operationJob: System.Job? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showStartDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // VM Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (vm.description.isNotEmpty()) {
                        Text(
                            text = vm.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "ID: ${vm.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                StatusChip(
                    status = vm.status,
                    icon = getStatusIcon(vm.status)
                )
            }

            // VM Details
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "vCPUs",
                    value = "${vm.vcpus}"
                )
                DetailItem(
                    label = "Cores",
                    value = "${vm.cores}"
                )
                DetailItem(
                    label = "Memory",
                    value = "${vm.memory} MB"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Bootloader",
                    value = vm.bootloader
                )
                DetailItem(
                    label = "Autostart",
                    value = if (vm.autostart) "Yes" else "No"
                )
                if (vm.enable_secure_boot) {
                    DetailItem(
                        label = "Secure Boot",
                        value = "Enabled"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            // Loading bar
            operationJob?.let { job ->
                Spacer(modifier = Modifier.height(12.dp))

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
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }

            // Action Buttons - First Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (vm.status.state.lowercase()) {
                    "stopped" -> {
                        // Add a dialog to force shutdown after timeout and or force it off
                        ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = {showStartDialog = true},
                            modifier = Modifier.weight(1f)
                        )
                    }
                    "running" -> {
                        ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true,
                            onClick = {showStopDialog = true},
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            text = "Restart",
                            icon = Icons.Default.RestartAlt,
                            enabled = true,
                            isPrimary = false,
                            onClick = onRestartVm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    "suspended" -> {
                        ActionButton(
                            text = "Resume",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = onResumeVm,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        ActionButton(
                            text = vm.status.state,
                            icon = Icons.Default.Error,
                            enabled = false,
                            isPrimary = false,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Second Row - More Actions
            if (vm.status.state.lowercase() == "running") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        text = "Suspend",
                        icon = Icons.Default.Pause,
                        enabled = true,
                        isPrimary = false,
                        onClick = onSuspendVm,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Power Off",
                        icon = Icons.Default.PowerOff,
                        enabled = true,
                        isPrimary = false,
                        onClick = onPowerOffVm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Third Row - Info and Delete
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "View Info",
                    icon = Icons.Default.Info,
                    enabled = true,
                    isPrimary = false,
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "Delete",
                    icon = Icons.Default.Delete,
                    enabled = vm.status.state.lowercase() == "stopped",
                    isPrimary = false,
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    isDanger = true
                )
            }
        }
    }
    if (showStartDialog){
        StartConfirmationDialog(
            vmName = vm.name,
            onConfirm = {overcommit ->
                onStartVm(overcommit)
                showStartDialog = false
            },
            onDismiss = { showStartDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            vmName = vm.name,
            onConfirm = {
                onDeleteVm()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showInfoDialog) {
        VmInfoDialog(
            vm = vm,
            onDismiss = { showInfoDialog = false }
        )
    }
    if (showStopDialog){
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
private fun DetailItem(
    label: String,
    value: String
) {
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
private fun StatusChip(
    status: Vm.VmStatus,
    icon: ImageVector
) {
    Surface(
        color = getStatusColor(status).copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(0.dp)
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
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            isDanger -> MaterialTheme.colorScheme.errorContainer
            isPrimary -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
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
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isDanger -> MaterialTheme.colorScheme.onErrorContainer
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    isDanger -> MaterialTheme.colorScheme.onErrorContainer
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Virtual Machine")
        },
        text = {
            Text(text = "Are you sure you want to delete '$vmName'? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
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
private fun StopConfirmationDialog(
    vmName: String,
    onConfirm: (forceShutoff: Boolean, forceShutoffAfterTimeout: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var forceShutoffChecked by remember { mutableStateOf(false) }
    var forceShutoffAfterTimeoutChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Stop Virtual Machine") // Title changed for consistency
        },
        text = {
            // Use a Column to stack the switches and the main text
            Column {
                Text(text = "Are you sure you want to stop '$vmName'? This action can be complex.")

                Spacer(modifier = Modifier.height(16.dp))

                // Switch 1: Force Shutoff
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Force shutoff")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = forceShutoffChecked,
                        onCheckedChange = { forceShutoffChecked = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Switch 2: Force Shutoff After Timeout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Force shutoff after timeout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = forceShutoffAfterTimeoutChecked,
                        onCheckedChange = { forceShutoffAfterTimeoutChecked = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(forceShutoffChecked, forceShutoffAfterTimeoutChecked)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Start Dialog
@Composable
private fun StartConfirmationDialog(
    vmName: String,
    onConfirm: (overcommit: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var overcommit by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Start Virtual Machine")
        },
        text = {
            Column {
                Text(text = "Are you sure you want to start '$vmName'?")

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Overcommit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = overcommit,
                        onCheckedChange = { overcommit = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(overcommit)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop")
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
private fun VmInfoDialog(
    vm: Vm.VmQueryResponse,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = vm.name)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    InfoRow("ID", vm.id.toString())
                }
                item {
                    InfoRow("Status", vm.status.state)
                }
                if (vm.description.isNotEmpty()) {
                    item {
                        InfoRow("Description", vm.description)
                    }
                }
                item {
                    InfoRow("vCPUs", "${vm.vcpus}")
                }
                item {
                    InfoRow("Cores", "${vm.cores}")
                }
                item {
                    InfoRow("Threads", "${vm.threads}")
                }
                item {
                    InfoRow("Memory", "${vm.memory} MB")
                }
                vm.min_memory?.let {
                    item {
                        InfoRow("Min Memory", "$it MB")
                    }
                }
                item {
                    InfoRow("CPU Mode", vm.cpu_mode.name.replace("_", " "))
                }
                vm.cpu_model?.let {
                    item {
                        InfoRow("CPU Model", it)
                    }
                }
                item {
                    InfoRow("Bootloader", vm.bootloader)
                }
                item {
                    InfoRow("Autostart", if (vm.autostart) "Yes" else "No")
                }
                item {
                    InfoRow("Secure Boot", if (vm.enable_secure_boot) "Enabled" else "Disabled")
                }
                item {
                    InfoRow("TPM", if (vm.trusted_platform_module) "Enabled" else "Disabled")
                }
                item {
                    InfoRow("Display Available", if (vm.display_available) "Yes" else "No")
                }
                vm.uuid?.let {
                    item {
                        InfoRow("UUID", it)
                    }
                }
                vm.arch_type?.let {
                    item {
                        InfoRow("Architecture", it)
                    }
                }
                vm.machine_type?.let {
                    item {
                        InfoRow("Machine Type", it)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
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