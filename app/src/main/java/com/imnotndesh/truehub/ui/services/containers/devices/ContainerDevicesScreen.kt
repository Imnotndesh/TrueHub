package com.imnotndesh.truehub.ui.services.containers.devices

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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun ContainerDevicesScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onAddDevice: () -> Unit,
    onEditDevice: (String) -> Unit,
    viewModel: ContainerDevicesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Container devices",
                subtitle = "Attached hardware and devices",
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
                    state.isLoading && state.devices.isEmpty() -> LoadingScreen("Loading devices")
                    state.devices.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Devices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "No devices attached",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                "Add a disk, NIC, GPU, or passthrough device.",
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
                            items(state.devices, key = { deviceIdentifier(it) ?: it.hashCode().toString() }) { device ->
                                val identifier = deviceIdentifier(device)
                                DeviceRow(
                                    device = device,
                                    isDeleting = state.deletingDeviceName == device["name"]?.toString(),
                                    onEdit = { identifier?.let(onEditDevice) },
                                    canEdit = identifier != null,
                                    onDelete = { pendingDelete = device["name"]?.toString() }
                                )
                            }
                        }
                    }
                }
            }
            ExpressiveActionFab(
                icon = Icons.Default.Add,
                label = "Add device",
                onClick = onAddDevice,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }
    }

    pendingDelete?.let { deviceName ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove device?") },
            text = { Text("This removes $deviceName from the container configuration.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(deviceName)
                        pendingDelete = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DeviceRow(
    device: Map<String, Any?>,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    canEdit: Boolean,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                    device["name"]?.toString()?.ifBlank { "Unnamed device" } ?: "Unnamed device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val type = device["dev_type"]?.toString() ?: "DEVICE"
                val source = device["source"]?.toString()
                Text(
                    listOfNotNull(type, source).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEdit, enabled = canEdit && !isDeleting) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit device",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete, enabled = !isDeleting) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove device",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
