package com.imnotndesh.truehub.ui.services.containers.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerEditScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ContainerEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var autostart by rememberSaveable { mutableStateOf(false) }
    var cpu by rememberSaveable { mutableStateOf("") }
    var memory by rememberSaveable { mutableStateOf("") }
    var vncEnabled by rememberSaveable { mutableStateOf(false) }
    var vncPort by rememberSaveable { mutableStateOf("") }
    var vncPassword by rememberSaveable { mutableStateOf("") }
    var secureBoot by rememberSaveable { mutableStateOf(false) }
    var rootDiskSize by rememberSaveable { mutableStateOf("") }
    var rootDiskIoBus by remember { mutableStateOf<Virt.RootDiskIOBus?>(null) }
    var showIoBusPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.container?.id) {
        state.container?.let { container ->
            autostart = container.autostart
            cpu = container.cpu.orEmpty()
            memory = container.memory?.toString().orEmpty()
            vncEnabled = container.vnc_enabled
            vncPort = container.vnc_port?.toString().orEmpty()
            secureBoot = container.secure_boot == true
            rootDiskSize = container.root_disk_size?.toString().orEmpty()
            rootDiskIoBus = container.root_disk_io_bus
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = state.container?.name ?: "Edit container",
                subtitle = "Container configuration",
                isLoading = state.isLoading,
                isRefreshing = false,
                error = state.error,
                onRefresh = viewModel::load,
                onDismissError = viewModel::load,
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading && state.container == null) {
            LoadingScreen("Loading Container")
        } else if (state.container == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error ?: "Container is unavailable", color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::load, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingSwitch(
                    title = "Autostart",
                    description = "Start the container when TrueNAS starts",
                    checked = autostart,
                    onCheckedChange = { autostart = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cpu,
                        onValueChange = { cpu = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("CPU") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = memory,
                        onValueChange = { memory = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Memory MB") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rootDiskSize,
                        onValueChange = { rootDiskSize = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Root disk GB") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    ChoiceField(
                        label = "I/O bus",
                        value = rootDiskIoBus?.name.orEmpty(),
                        placeholder = "Select",
                        onClick = { showIoBusPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                SettingSwitch(
                    title = "VNC",
                    description = "Enable remote graphical access",
                    checked = vncEnabled,
                    onCheckedChange = { vncEnabled = it }
                )
                if (vncEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = vncPort,
                            onValueChange = { vncPort = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("VNC port") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = vncPassword,
                            onValueChange = { vncPassword = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("VNC password") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
                SettingSwitch(
                    title = "Secure boot",
                    description = "Enable secure boot when supported",
                    checked = secureBoot,
                    onCheckedChange = { secureBoot = it }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        viewModel.save(
                            autostart = autostart,
                            cpu = cpu,
                            memory = memory,
                            vncEnabled = vncEnabled,
                            vncPort = vncPort,
                            vncPassword = vncPassword,
                            secureBoot = secureBoot,
                            rootDiskSize = rootDiskSize,
                            rootDiskIoBus = rootDiskIoBus
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save changes")
                }
            }
        }
    }

    if (showIoBusPicker) {
        ModalBottomSheet(
            onDismissRequest = { showIoBusPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Choose root disk I/O bus", style = MaterialTheme.typography.headlineSmall)
                Virt.RootDiskIOBus.entries.forEach { bus ->
                    DropdownMenuItem(
                        text = { Text(bus.name) },
                        onClick = {
                            rootDiskIoBus = bus
                            showIoBusPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChoiceField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ExpandMore, contentDescription = "Choose $label")
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}
