package com.imnotndesh.truehub.ui.services.containers.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDeviceAddScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: ContainerDeviceAddViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var type by rememberSaveable { mutableStateOf(ContainerDeviceType.Disk) }
    var gpuTypePicker by remember { mutableStateOf(false) }
    var nicTypePicker by remember { mutableStateOf(false) }
    var diskSource by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var readOnly by rememberSaveable { mutableStateOf(false) }
    var network by rememberSaveable { mutableStateOf("") }
    var gpu by rememberSaveable { mutableStateOf("") }
    var usb by rememberSaveable { mutableStateOf("") }
    var pci by rememberSaveable { mutableStateOf("") }
    var tpmPath by rememberSaveable { mutableStateOf("") }
    var sourceProtocol by rememberSaveable { mutableStateOf("TCP") }
    var sourcePort by rememberSaveable { mutableStateOf("") }
    var destinationProtocol by rememberSaveable { mutableStateOf("TCP") }
    var destinationPort by rememberSaveable { mutableStateOf("") }
    var choicePicker by remember { mutableStateOf<DeviceChoicePicker?>(null) }

    LaunchedEffect(state.editingDevice) {
        val device = state.editingDevice ?: return@LaunchedEffect
        val deviceType = device["dev_type"]?.toString()?.uppercase()
        type = ContainerDeviceType.entries.firstOrNull { it.name == deviceType } ?: ContainerDeviceType.Disk
        diskSource = device["source"]?.toString().orEmpty()
        destination = device["destination"]?.toString().orEmpty()
        readOnly = device["readonly"] == true
        val selectedNicType = device["nic_type"]?.toString()?.takeIf { it.isNotBlank() }
            ?: device["type"]?.toString()?.takeIf { it.isNotBlank() }
            ?: "BRIDGED"
        viewModel.setNicType(selectedNicType)
        network = device["network"]?.toString().orEmpty()
        val selectedGpuType = device["gpu_type"]?.toString()?.takeIf { it.isNotBlank() } ?: "PHYSICAL"
        viewModel.setGpuType(selectedGpuType)
        gpu = device["id"]?.toString() ?: device["name"]?.toString().orEmpty()
        val deviceUsb = device["name"]?.toString()?.takeIf { state.usbChoices.containsKey(it) }
        usb = deviceUsb ?: state.usbChoices.entries.firstOrNull { (_, choice) ->
            choice.vendorId == device["vendor_id"]?.toString() && choice.productId == device["product_id"]?.toString()
        }?.key.orEmpty()
        pci = device["address"]?.toString().orEmpty()
        tpmPath = device["path"]?.toString().orEmpty()
        sourceProtocol = device["source_proto"]?.toString() ?: "TCP"
        sourcePort = device["source_port"]?.toString().orEmpty()
        destinationProtocol = device["dest_proto"]?.toString() ?: "TCP"
        destinationPort = device["dest_port"]?.toString().orEmpty()
    }

    LaunchedEffect(state.added) {
        if (state.added) onAdded()
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                 title = if (state.editingDeviceName == null) "Add device" else "Edit device",
                 subtitle = type.name,
                 isLoading = state.isLoadingChoices || state.isSaving,
                 isRefreshing = false,
                 error = state.error,
                 onDismissError = { viewModel.loadChoices() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoadingChoices) {
                LoadingScreen("Loading device choices")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContainerDeviceType.entries.forEach { deviceType ->
                            FilterChip(
                                selected = type == deviceType,
                                onClick = { type = deviceType },
                                label = { Text(deviceType.name) }
                            )
                        }
                    }
                    when (type) {
                        ContainerDeviceType.Disk -> {
                            ChoiceField("Disk source", diskSource, "Select disk") { choicePicker = DeviceChoicePicker.Disk }
                            OutlinedTextField(
                                value = destination,
                                onValueChange = { destination = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Destination path") },
                                singleLine = true
                            )
                            ToggleField("Read-only", readOnly) { readOnly = it }
                        }
                        ContainerDeviceType.Nic -> {
                            ChoiceField(
                                label = "NIC type",
                                value = state.nicType,
                                placeholder = if (state.isLoadingNicChoices) "Loading types" else "Select type"
                            ) { nicTypePicker = true }
                            ChoiceField(
                                label = "Network",
                                value = network,
                                placeholder = if (state.isLoadingNicChoices) "Loading networks" else "Select network"
                            ) { choicePicker = DeviceChoicePicker.Nic }
                        }
                        ContainerDeviceType.Gpu -> {
                            ChoiceField("GPU type", state.gpuType, "Select type") { gpuTypePicker = true }
                            ChoiceField(
                                label = "GPU",
                                value = gpu,
                                placeholder = if (state.isLoadingGpuChoices) "Loading GPUs" else "Select GPU"
                            ) { choicePicker = DeviceChoicePicker.Gpu }
                        }
                        ContainerDeviceType.Usb -> ChoiceField("USB device", usb, "Select USB device") { choicePicker = DeviceChoicePicker.Usb }
                        ContainerDeviceType.Pci -> ChoiceField("PCI device", pci, "Select PCI device") { choicePicker = DeviceChoicePicker.Pci }
                        ContainerDeviceType.Tpm -> OutlinedTextField(
                            value = tpmPath,
                            onValueChange = { tpmPath = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("TPM device path") },
                            singleLine = true
                        )
                        ContainerDeviceType.Proxy -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(sourceProtocol, { sourceProtocol = it }, Modifier.weight(1f), label = { Text("Source protocol") }, singleLine = true)
                                OutlinedTextField(sourcePort, { sourcePort = it }, Modifier.weight(1f), label = { Text("Source port") }, singleLine = true)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(destinationProtocol, { destinationProtocol = it }, Modifier.weight(1f), label = { Text("Dest protocol") }, singleLine = true)
                                OutlinedTextField(destinationPort, { destinationPort = it }, Modifier.weight(1f), label = { Text("Dest port") }, singleLine = true)
                            }
                        }
                    }
                }
            }

            ExpressiveActionFab(
                icon = Icons.Default.Save,
                label = if (state.editingDeviceName == null) "Add device" else "Save device",
                isBusy = state.isSaving,
                onClick = {
                    if (!state.isSaving) {
                        viewModel.add(
                            type, diskSource, destination, readOnly, state.nicType, network, state.gpuType, gpu, usb, pci,
                            tpmPath, sourceProtocol, sourcePort, destinationProtocol, destinationPort
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }
    }

    if (gpuTypePicker) {
        SimpleChoiceSheet(
            title = "Choose GPU type",
            choices = listOf("PHYSICAL", "MDEV", "MIG", "SRIOV"),
            onDismiss = { gpuTypePicker = false },
            onSelect = {
                viewModel.setGpuType(it)
                gpuTypePicker = false
            }
        )
    }

    if (nicTypePicker) {
        SimpleChoiceSheet(
            title = "Choose NIC type",
            choices = listOf("BRIDGED", "MACVLAN"),
            onDismiss = { nicTypePicker = false },
            onSelect = {
                viewModel.setNicType(it)
                nicTypePicker = false
            }
        )
    }

    choicePicker?.let { picker ->
        DeviceMapChoiceSheet(
            title = picker.title,
            choices = when (picker) {
                DeviceChoicePicker.Disk -> state.diskChoices.map { (key, value) -> DeviceChoiceOption(key, value) }
                DeviceChoicePicker.Gpu -> state.gpuChoices.map { (key, choice) ->
                    DeviceChoiceOption(key, listOfNotNull(choice.description, choice.vendor, choice.pci).joinToString(" · "))
                }
                DeviceChoicePicker.Nic -> state.nicChoices.map { (key, value) -> DeviceChoiceOption(key, value) }
                DeviceChoicePicker.Usb -> state.usbChoices.map { (key, choice) ->
                    DeviceChoiceOption(key, listOfNotNull(choice.product, choice.manufacturer, key).joinToString(" · "))
                }
                DeviceChoicePicker.Pci -> state.pciChoices.map { (key, value) -> DeviceChoiceOption(key, value) }
            },
            onDismiss = { choicePicker = null },
            onSelect = { value ->
                when (picker) {
                    DeviceChoicePicker.Disk -> diskSource = value
                    DeviceChoicePicker.Gpu -> gpu = value
                    DeviceChoicePicker.Nic -> network = value
                    DeviceChoicePicker.Usb -> usb = value
                    DeviceChoicePicker.Pci -> pci = value
                }
                choicePicker = null
            }
        )
    }
}

private data class DeviceChoiceOption(
    val key: String,
    val label: String
)

private enum class DeviceChoicePicker(val title: String) {
    Disk("Choose disk"),
    Gpu("Choose GPU"),
    Nic("Choose network"),
    Usb("Choose USB device"),
    Pci("Choose PCI device")
}

@Composable
private fun ChoiceField(label: String, value: String, placeholder: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun ToggleField(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleChoiceSheet(
    title: String,
    choices: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = { onSelect(choice) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceMapChoiceSheet(
    title: String,
    choices: List<DeviceChoiceOption>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = choices.filter { option ->
        query.isBlank() || option.key.contains(query, true) || option.label.contains(query, true)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text("Search") },
                singleLine = true
            )
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(filtered, key = { it.key }) { option ->
                    DropdownMenuItem(
                        text = { Text("${option.key} · ${option.label}") },
                        onClick = { onSelect(option.key) }
                    )
                }
            }
        }
    }
}
