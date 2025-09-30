package com.example.truehub.ui.services.containers

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.data.models.Virt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    manager: TrueNASApiManager,
    viewModel: ContainerScreenViewModel = viewModel(
        factory = ContainerScreenViewModel.ContainerViewModelFactory(manager)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Content based on state
    when {
        uiState.isLoading -> {
            LoadingContent()
        }
        uiState.containers.isEmpty() && !uiState.isLoading -> {
            EmptyContainerContent()
        }
        else -> {
            ContainersContent(
                containers = uiState.containers,
                isRefreshing = uiState.isRefreshing,
                onStartContainer = { id -> viewModel.startContainer(id) },
                onStopContainer = { id -> viewModel.stopContainer(id) },
                onRestartContainer = { id -> viewModel.restartContainer(id) },
                onDeleteContainer = { id -> viewModel.deleteContainer(id) },
                operationJobs = uiState.operationJobs
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
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading containers...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyContainerContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No containers found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create containers to see them here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContainersContent(
    containers: List<Virt.ContainerResponse>,
    isRefreshing: Boolean,
    onStartContainer: (String) -> Unit,
    onStopContainer: (String) -> Unit,
    onRestartContainer: (String) -> Unit,
    onDeleteContainer: (String) -> Unit,
    operationJobs: Map<String, System.Job>
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

        items(containers) { container ->
            ContainerCard(
                container = container,
                onStartContainer = { onStartContainer(container.id) },
                onStopContainer = { onStopContainer(container.id) },
                onRestartContainer = { onRestartContainer(container.id) },
                onDeleteContainer = { onDeleteContainer(container.id) },
                operationJob = operationJobs[container.id]
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContainerCard(
    container: Virt.ContainerResponse,
    onStartContainer: () -> Unit,
    onStopContainer: () -> Unit,
    onRestartContainer: () -> Unit,
    onDeleteContainer: () -> Unit,
    operationJob: System.Job? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

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
                // Container Icon/Type
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when (container.type) {
                                Virt.Type.CONTAINER -> MaterialTheme.colorScheme.primaryContainer
                                Virt.Type.VM -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (container.type) {
                            Virt.Type.CONTAINER -> "C"
                            Virt.Type.VM -> "VM"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (container.type) {
                            Virt.Type.CONTAINER -> MaterialTheme.colorScheme.onPrimaryContainer
                            Virt.Type.VM -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${container.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(
                    status = container.status,
                    icon = getStatusIcon(container.status)
                )
            }

            // Container Details
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "CPU",
                    value = container.cpu ?: "N/A"
                )
                DetailItem(
                    label = "Memory",
                    value = container.memory?.let { "$it MB" } ?: "N/A"
                )
                DetailItem(
                    label = "Autostart",
                    value = if (container.autostart) "Yes" else "No"
                )
            }

            if (container.vnc_enabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VNC Port: ${container.vnc_port ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (container.status) {
                    Virt.Status.STOPPED -> {
                        ActionButton(
                            text = "Start",
                            icon = Icons.Default.PlayArrow,
                            enabled = true,
                            isPrimary = true,
                            onClick = onStartContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Virt.Status.RUNNING -> {
                        ActionButton(
                            text = "Stop",
                            icon = Icons.Default.Stop,
                            enabled = true,
                            isPrimary = true,
                            onClick = onStopContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            text = "Restart",
                            icon = Icons.Default.RestartAlt,
                            enabled = true,
                            isPrimary = false,
                            onClick = onRestartContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    else -> {
                        ActionButton(
                            text = container.status.name,
                            icon = Icons.Default.Refresh,
                            enabled = false,
                            isPrimary = false,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

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
                    enabled = container.status == Virt.Status.STOPPED,
                    isPrimary = false,
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    isDanger = true
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            containerName = container.name,
            onConfirm = {
                onDeleteContainer()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showInfoDialog) {
        ContainerInfoDialog(
            container = container,
            onDismiss = { showInfoDialog = false }
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
    status: Virt.Status,
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
                text = status.name.lowercase().replaceFirstChar { it.uppercase() },
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
private fun getStatusColor(status: Virt.Status): Color {
    return when (status) {
        Virt.Status.RUNNING -> Color(0xFF2E7D32)
        Virt.Status.STOPPED -> Color(0xFF757575)
        Virt.Status.ERROR -> MaterialTheme.colorScheme.error
        Virt.Status.FROZEN -> Color(0xFF1976D2)
        Virt.Status.STARTING, Virt.Status.STOPPING,
        Virt.Status.FREEZING, Virt.Status.THAWED,
        Virt.Status.ABORTING -> Color(0xFFF57C00)
        Virt.Status.UNKNOWN -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(status: Virt.Status): ImageVector {
    return when (status) {
        Virt.Status.RUNNING -> Icons.Default.PlayArrow
        Virt.Status.STOPPED -> Icons.Default.Stop
        Virt.Status.ERROR -> Icons.Default.Error
        Virt.Status.FROZEN -> Icons.Default.Pause
        else -> Icons.Default.Refresh
    }
}

@Composable
private fun DeleteConfirmationDialog(
    containerName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Container")
        },
        text = {
            Text(text = "Are you sure you want to delete '$containerName'? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
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
private fun ContainerInfoDialog(
    container: Virt.ContainerResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = container.name)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    InfoRow("ID", container.id)
                }
                item {
                    InfoRow("Type", container.type.name)
                }
                item {
                    InfoRow("Status", container.status.name)
                }
                item {
                    InfoRow("CPU", container.cpu ?: "N/A")
                }
                item {
                    InfoRow("Memory", container.memory?.let { "$it MB" } ?: "N/A")
                }
                item {
                    InfoRow("Autostart", if (container.autostart) "Yes" else "No")
                }
                item {
                    InfoRow("VNC Enabled", if (container.vnc_enabled) "Yes" else "No")
                }
                if (container.vnc_enabled) {
                    item {
                        InfoRow("VNC Port", container.vnc_port?.toString() ?: "N/A")
                    }
                }
                item {
                    InfoRow("Storage Pool", container.storage_pool ?: "N/A")
                }
                container.image.os?.let { os ->
                    item {
                        InfoRow("OS", os)
                    }
                }
                container.image.release?.let { release ->
                    item {
                        InfoRow("Release", release)
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
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