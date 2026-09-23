package com.imnotndesh.truehub.ui.homepage.instancesettings.boot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.advanced.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.homepage.instancesettings.general.ExpressiveSection

/**
 * Detailed view of the boot pool, including the full pool state, attached disks, and
 * maintenance actions (attach / replace / detach disk, scrub, refresh).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootPoolScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: BootViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAttachDialog by remember { mutableStateOf(false) }
    var showReplaceDialog by remember { mutableStateOf(false) }
    var showScrubIntervalDialog by remember { mutableStateOf(false) }
    var detachTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadAll() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearActionMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            UnifiedScreenHeader(
                title = "Boot Pool",
                subtitle = "State and disks",
                isLoading = uiState.isLoading,
                isRefreshing = false,
                error = uiState.error,
                onDismissError = { vm.clearError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.bootState == null ->
                LoadingScreen("Loading boot pool...")
            else ->
                PullToRefreshContent(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                    item {
                        uiState.bootState?.let { PoolHealthCard(it) }
                    }

                    item {
                        ExpressiveSection(title = "Pool Details", icon = Icons.Default.Storage) {
                            ExpressiveInfoCard {
                                val state = uiState.bootState
                                state?.let {
                                    InfoRow("Pool Name", it.name)
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Status", it.status)
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Path", it.path)
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Health", if (it.healthy) "Healthy" else "Degraded")
                                    if (it.size != null) {
                                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                        InfoRow("Size", formatBytes(it.size))
                                    }
                                    if (it.allocated != null) {
                                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                        InfoRow("Allocated", formatBytes(it.allocated))
                                    }
                                    if (it.free != null) {
                                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                        InfoRow("Free", formatBytes(it.free))
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    InfoRow("Is Upgraded", if (it.isUpgraded) "Yes" else "No")
                                    if (!it.fragmentation.isNullOrBlank()) {
                                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                        InfoRow("Fragmentation", it.fragmentation)
                                    }
                                } ?: Text("Boot pool state unavailable.")
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "Attached Disks", icon = Icons.Default.Storage) {
                            if (uiState.disks.isEmpty()) {
                                ExpressiveInfoCard {
                                    Text(
                                        "No disks reported for the boot pool.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                uiState.disks.forEachIndexed { index, disk ->
                                    DiskCard(disk = disk, onDetach = {
                                        detachTarget = disk
                                    })
                                    if (index < uiState.disks.size - 1) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        ExpressiveSection(title = "Actions", icon = Icons.Default.Refresh) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Attach",
                                    icon = Icons.Default.Add,
                                    enabled = !uiState.isActing,
                                    onClick = { showAttachDialog = true }
                                )
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Replace",
                                    icon = Icons.Default.SwapVert,
                                    enabled = !uiState.isActing && uiState.disks.isNotEmpty(),
                                    onClick = { showReplaceDialog = true }
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Scrub",
                                    icon = Icons.Default.Upload,
                                    enabled = !uiState.isActing,
                                    loading = uiState.isActing,
                                    onClick = { vm.scrubBootPool() }
                                )
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    label = "Refresh",
                                    icon = Icons.Default.Refresh,
                                    enabled = !uiState.isActing,
                                    onClick = { vm.refresh() }
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            ActionButton(
                                modifier = Modifier.fillMaxWidth(),
                                label = "Set Scrub Interval",
                                icon = Icons.Default.Refresh,
                                enabled = !uiState.isActing,
                                onClick = { showScrubIntervalDialog = true }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
                    }
        }
    }

    if (showAttachDialog) {
        AttachDiskDialog(
            onDismiss = { showAttachDialog = false },
            onConfirm = { dev, expand ->
                vm.attachDisk(dev, expand)
                showAttachDialog = false
            }
        )
    }

    if (showReplaceDialog) {
        ReplaceDiskDialog(
            labels = uiState.disks,
            onDismiss = { showReplaceDialog = false },
            onConfirm = { label, dev ->
                vm.replaceDisk(label, dev)
                showReplaceDialog = false
            }
        )
    }

    if (showScrubIntervalDialog) {
        SetScrubIntervalDialog(
            onDismiss = { showScrubIntervalDialog = false },
            onConfirm = { interval ->
                vm.setScrubInterval(interval)
                showScrubIntervalDialog = false
            }
        )
    }

    detachTarget?.let { disk ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { detachTarget = null },
            title = { Text("Detach disk?") },
            text = { Text("Detach \"$disk\" from the boot pool?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.detachDisk(disk)
                    detachTarget = null
                }) { Text("Detach") }
            },
            dismissButton = {
                TextButton(onClick = { detachTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DiskCard(disk: String, onDetach: () -> Unit) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = disk,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = onDetach, shape = RoundedCornerShape(12.dp)) {
                Text("Detach", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AttachDiskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var dev by remember { mutableStateOf("") }
    var expand by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attach Disk") },
        text = {
            Column {
                Text(
                    "Attach a disk to the boot pool to turn a stripe into a mirror.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dev,
                    onValueChange = { dev = it },
                    label = { Text("Device name / path") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Expand pool after attach",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = expand, onCheckedChange = { expand = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = dev.isNotBlank(), onClick = { onConfirm(dev.trim(), expand) }) {
                Text("Attach")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ReplaceDiskDialog(
    labels: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var label by remember(labels) { mutableStateOf(labels.firstOrNull() ?: "") }
    var dev by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace Disk") },
        text = {
            Column {
                Text(
                    "Replace a disk in the boot pool with a new device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label to replace") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dev,
                    onValueChange = { dev = it },
                    label = { Text("Replacement device") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = label.isNotBlank() && dev.isNotBlank(),
                onClick = { onConfirm(label.trim(), dev.trim()) }
            ) { Text("Replace") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SetScrubIntervalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var interval by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Scrub Interval") },
        text = {
            Column {
                Text(
                    "Set the automatic scrub interval in days (must be a positive integer).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit) },
                    label = { Text("Interval (days)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = interval.toIntOrNull()?.let { it > 0 } == true,
                onClick = { interval.toIntOrNull()?.let { onConfirm(it) } }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
