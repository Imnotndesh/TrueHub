package com.imnotndesh.truehub.ui.services.containers.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveFilterBar
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveToggleRow
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView

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
    var cdromSource by rememberSaveable { mutableStateOf("") }
    var bootPriority by rememberSaveable { mutableStateOf("") }
    var showCdromPicker by remember { mutableStateOf(false) }
    var sourceProtocol by rememberSaveable { mutableStateOf("TCP") }
    var sourcePort by rememberSaveable { mutableStateOf("") }
    var destinationProtocol by rememberSaveable { mutableStateOf("TCP") }
    var destinationPort by rememberSaveable { mutableStateOf("") }
    var choicePicker by remember { mutableStateOf<DeviceChoicePicker?>(null) }

    LaunchedEffect(state.editingDevice) {
        val device = state.editingDevice ?: return@LaunchedEffect
        val deviceType = device["dev_type"]?.toString()?.uppercase()
        type = ContainerDeviceType.entries.firstOrNull { it.wireName == deviceType } ?: ContainerDeviceType.Disk
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

    fun submit() {
        if (!state.isSaving) {
            viewModel.add(
                type, diskSource, destination, readOnly, state.nicType, network, state.gpuType, gpu, usb, pci,
                tpmPath, sourceProtocol, sourcePort, destinationProtocol, destinationPort,
                cdromSource, bootPriority
            )
        }
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
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { submit() },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(if (state.editingDeviceName == null) "Add device" else "Save")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isSaving -> JobProgressView(
                    title = if (state.editingDeviceName == null) "Adding device" else "Saving device",
                    description = null,
                    progress = 0,
                    indeterminate = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
                state.isLoadingChoices -> LoadingScreen("Loading device choices")
                else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ExpressiveFilterBar(
                        items = ContainerDeviceType.entries,
                        selected = type,
                        label = { it.label },
                        onSelect = { type = it }
                    )

                    ExpressiveSection(title = "Configuration", icon = Icons.Default.Devices) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                when (type) {
                                    ContainerDeviceType.Disk -> {
                                        ExpressiveChoiceField(
                                            label = "Disk source",
                                            value = diskSource,
                                            placeholder = "Select disk",
                                            onClick = { choicePicker = DeviceChoicePicker.Disk }
                                        )
                                        ExpressiveTextField(
                                            value = destination,
                                            onValueChange = { destination = it },
                                            label = "Destination path"
                                        )
                                        ExpressiveToggleRow(
                                            title = "Read-only",
                                            description = "Mount the disk without write access",
                                            checked = readOnly,
                                            onCheckedChange = { readOnly = it }
                                        )
                                    }
                                    ContainerDeviceType.Nic -> {
                                        ExpressiveChoiceField(
                                            label = "NIC type",
                                            value = state.nicType,
                                            placeholder = if (state.isLoadingNicChoices) "Loading types" else "Select type",
                                            onClick = { nicTypePicker = true }
                                        )
                                        ExpressiveChoiceField(
                                            label = "Network",
                                            value = network,
                                            placeholder = if (state.isLoadingNicChoices) "Loading networks" else "Select network",
                                            onClick = { choicePicker = DeviceChoicePicker.Nic }
                                        )
                                    }
                                    ContainerDeviceType.Gpu -> {
                                        ExpressiveChoiceField(
                                            label = "GPU type",
                                            value = state.gpuType,
                                            placeholder = "Select type",
                                            onClick = { gpuTypePicker = true }
                                        )
                                        ExpressiveChoiceField(
                                            label = "GPU",
                                            value = gpu,
                                            placeholder = if (state.isLoadingGpuChoices) "Loading GPUs" else "Select GPU",
                                            onClick = { choicePicker = DeviceChoicePicker.Gpu }
                                        )
                                    }
                                    ContainerDeviceType.Usb -> ExpressiveChoiceField(
                                        label = "USB device",
                                        value = usb,
                                        placeholder = "Select USB device",
                                        onClick = { choicePicker = DeviceChoicePicker.Usb }
                                    )
                                    ContainerDeviceType.Pci -> ExpressiveChoiceField(
                                        label = "PCI device",
                                        value = pci,
                                        placeholder = "Select PCI device",
                                        onClick = { choicePicker = DeviceChoicePicker.Pci }
                                    )
                                    ContainerDeviceType.Cdrom -> {
                                        ExpressiveTextField(
                                            value = cdromSource,
                                            onValueChange = { cdromSource = it },
                                            label = "ISO path",
                                            supportingText = { Text("Path to the ISO file on the server") },
                                            trailingIcon = {
                                                IconButton(onClick = { showCdromPicker = true }) {
                                                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse server files")
                                                }
                                            }
                                        )
                                        ExpressiveTextField(
                                            value = bootPriority,
                                            onValueChange = { bootPriority = it },
                                            label = "Boot priority",
                                            placeholder = "Optional"
                                        )
                                        ExpressiveToggleRow(
                                            title = "Read-only",
                                            description = "Mount the ISO without write access",
                                            checked = readOnly,
                                            onCheckedChange = { readOnly = it }
                                        )
                                    }
                                    ContainerDeviceType.Tpm -> ExpressiveTextField(
                                        value = tpmPath,
                                        onValueChange = { tpmPath = it },
                                        label = "TPM device path"
                                    )
                                    ContainerDeviceType.Proxy -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ExpressiveTextField(
                                                value = sourceProtocol,
                                                onValueChange = { sourceProtocol = it },
                                                label = "Source protocol",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExpressiveTextField(
                                                value = sourcePort,
                                                onValueChange = { sourcePort = it },
                                                label = "Source port",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ExpressiveTextField(
                                                value = destinationProtocol,
                                                onValueChange = { destinationProtocol = it },
                                                label = "Dest protocol",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExpressiveTextField(
                                                value = destinationPort,
                                                onValueChange = { destinationPort = it },
                                                label = "Dest port",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showCdromPicker) {
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
                cdromSource = it
                showCdromPicker = false
            },
            onDismiss = { showCdromPicker = false }
        )
    }

    if (gpuTypePicker) {
        ExpressiveChoiceSheet(
            title = "Choose GPU type",
            choices = listOf("PHYSICAL", "MDEV", "MIG", "SRIOV").map { ExpressiveChoice(key = it, label = it) },
            selectedKey = state.gpuType,
            searchable = false,
            height = 280,
            onDismiss = { gpuTypePicker = false },
            onSelect = {
                viewModel.setGpuType(it)
                gpuTypePicker = false
            }
        )
    }

    if (nicTypePicker) {
        ExpressiveChoiceSheet(
            title = "Choose NIC type",
            choices = listOf("BRIDGED", "MACVLAN").map { ExpressiveChoice(key = it, label = it) },
            selectedKey = state.nicType,
            searchable = false,
            height = 200,
            onDismiss = { nicTypePicker = false },
            onSelect = {
                viewModel.setNicType(it)
                nicTypePicker = false
            }
        )
    }

    choicePicker?.let { picker ->
        ExpressiveChoiceSheet(
            title = picker.title,
            choices = when (picker) {
                DeviceChoicePicker.Disk -> state.diskChoices.map { (key, value) ->
                    ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
                }
                DeviceChoicePicker.Gpu -> state.gpuChoices.map { (key, choice) ->
                    ExpressiveChoice(
                        key = key,
                        label = key,
                        supporting = listOfNotNull(choice.description, choice.vendor, choice.pci).joinToString(" · ")
                    )
                }
                DeviceChoicePicker.Nic -> state.nicChoices.map { (key, value) ->
                    ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
                }
                DeviceChoicePicker.Usb -> state.usbChoices.map { (key, choice) ->
                    ExpressiveChoice(
                        key = key,
                        label = listOfNotNull(choice.product, choice.manufacturer).joinToString(" · ").ifBlank { key },
                        supporting = key
                    )
                }
                DeviceChoicePicker.Pci -> state.pciChoices.map { (key, value) ->
                    ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
                }
            },
            selectedKey = when (picker) {
                DeviceChoicePicker.Disk -> diskSource
                DeviceChoicePicker.Gpu -> gpu
                DeviceChoicePicker.Nic -> network
                DeviceChoicePicker.Usb -> usb
                DeviceChoicePicker.Pci -> pci
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

private enum class DeviceChoicePicker(val title: String) {
    Disk("Choose disk"),
    Gpu("Choose GPU"),
    Nic("Choose network"),
    Usb("Choose USB device"),
    Pci("Choose PCI device")
}
