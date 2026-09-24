package com.imnotndesh.truehub.ui.services.vm.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerSheet
import kotlinx.coroutines.launch

private enum class VmDevicePicker { None, Zvol, Iotype, NicAttach, Bind, Resolution, UsbController, UsbDevice, Passthrough, File }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmDeviceAddScreen(
    vmId: Int,
    deviceId: Int?,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: VmDeviceAddViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var type by rememberSaveable { mutableStateOf(VmDeviceType.Disk) }
    var createZvol by rememberSaveable { mutableStateOf(true) }
    var zvolPath by rememberSaveable { mutableStateOf("") }
    var zvolName by rememberSaveable { mutableStateOf("") }
    var zvolSizeGb by rememberSaveable { mutableStateOf("10") }
    var iotype by rememberSaveable { mutableStateOf("") }
    var isoPath by rememberSaveable { mutableStateOf("") }
    var rawPath by rememberSaveable { mutableStateOf("") }
    var nicAttach by rememberSaveable { mutableStateOf("") }
    var nicType by rememberSaveable { mutableStateOf("E1000") }
    var mac by rememberSaveable { mutableStateOf("") }
    var resolution by rememberSaveable { mutableStateOf("") }
    var displayPort by rememberSaveable { mutableStateOf("") }
    var bind by rememberSaveable { mutableStateOf("0.0.0.0") }
    var vncPassword by rememberSaveable { mutableStateOf("") }
    var pptdev by rememberSaveable { mutableStateOf("") }
    var usbController by rememberSaveable { mutableStateOf("") }
    var usbDevice by rememberSaveable { mutableStateOf("") }
    var order by rememberSaveable { mutableStateOf("") }
    var rawSizeHint by remember { mutableStateOf<Long?>(null) }
    var picker by remember { mutableStateOf(VmDevicePicker.None) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }
    LaunchedEffect(deviceId) {
        if (deviceId != null) viewModel.loadDevice(deviceId)
    }
    LaunchedEffect(state.editingDeviceId) {
        val device = state.editingDevice ?: return@LaunchedEffect
        device.attributes?.let { attrs ->
            attrs["path"]?.toString()?.let {
                when (device.dtype) {
                    "CDROM" -> isoPath = it
                    "RAW" -> rawPath = it
                    else -> zvolPath = it
                }
            }
            attrs["zvol_name"]?.toString()?.let { zvolName = it }
            attrs["iotype"]?.toString()?.let { iotype = it }
            attrs["nic_attach"]?.toString()?.let { nicAttach = it }
            attrs["mac"]?.toString()?.let { mac = it }
            attrs["resolution"]?.toString()?.let { resolution = it }
            attrs["port"]?.toString()?.let { displayPort = it }
            attrs["bind"]?.toString()?.let { bind = it }
            attrs["pptdev"]?.toString()?.let { pptdev = it }
            attrs["controller_type"]?.toString()?.let { usbController = it }
            attrs["device"]?.toString()?.let { usbDevice = it }
            if (attrs["create_zvol"] == true) createZvol = true
        }
        device.order?.let { order = it.toString() }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = if (state.editingDeviceId == null) "Add device" else "Edit device",
                subtitle = type.label,
                isLoading = state.isLoading,
                isRefreshing = false,
                error = state.error,
                onDismissError = viewModel::clearError,
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
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val attributes = buildAttributes(
                                type, createZvol, zvolPath, zvolName, zvolSizeGb, iotype,
                                isoPath, rawPath, nicAttach, nicType, mac,
                                resolution, displayPort, bind, vncPassword, pptdev, usbController, usbDevice
                            )
                            viewModel.save(type.wireName, attributes, order.trim().toIntOrNull())
                        },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (state.editingDeviceId == null) "Add device" else "Save")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                LoadingScreen("Loading device options")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ExpressiveFilterBar(
                        items = VmDeviceType.entries,
                        selected = type,
                        label = { it.label },
                        onSelect = { type = it }
                    )

                    ExpressiveSection(title = "Configuration", icon = Icons.Default.Info) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                when (type) {
                                    VmDeviceType.Disk -> {
                                        ExpressiveToggleRow(
                                            title = "Create new ZFS volume",
                                            description = "Off to attach an existing zvol",
                                            checked = createZvol,
                                            onCheckedChange = { createZvol = it }
                                        )
                                        if (createZvol) {
                                            ExpressiveTextField(
                                                value = zvolName,
                                                onValueChange = { zvolName = it },
                                                label = "ZFS volume name"
                                            )
                                            ExpressiveTextField(
                                                value = zvolSizeGb,
                                                onValueChange = { zvolSizeGb = it },
                                                label = "Size (GB)"
                                            )
                                        } else {
                                            ExpressiveChoiceField(
                                                label = "Existing ZFS volume",
                                                value = zvolPath.ifBlank { "Select zvol" },
                                                placeholder = "Select zvol",
                                                onClick = { picker = VmDevicePicker.Zvol }
                                            )
                                        }
                                        ExpressiveChoiceField(
                                            label = "Disk I/O type",
                                            value = iotype.ifBlank { "Default" },
                                            placeholder = "Select I/O type",
                                            onClick = { picker = VmDevicePicker.Iotype }
                                        )
                                    }
                                    VmDeviceType.Cdrom -> {
                                        PathField(
                                            label = "ISO path",
                                            value = isoPath,
                                            onValueChange = { isoPath = it },
                                            onBrowse = { picker = VmDevicePicker.File }
                                        )
                                    }
                                    VmDeviceType.Raw -> {
                                        PathField(
                                            label = "Raw file path",
                                            value = rawPath,
                                            onValueChange = { rawPath = it },
                                            onBrowse = { picker = VmDevicePicker.File }
                                        )
                                        ExpressiveChoiceField(
                                            label = "Disk I/O type",
                                            value = iotype.ifBlank { "Default" },
                                            placeholder = "Select I/O type",
                                            onClick = { picker = VmDevicePicker.Iotype }
                                        )
                                        if (rawPath.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    scope.launch {
                                                        viewModel.virtualSize(rawPath)?.let { rawSizeHint = it }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = MaterialTheme.shapes.large
                                            ) { Text("Calculate virtual size") }
                                        }
                                        rawSizeHint?.let {
                                            Text(
                                                text = "Virtual size: ${it / (1024 * 1024 * 1024)} GB",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    VmDeviceType.Nic -> {
                                        ExpressiveChoiceField(
                                            label = "Attach to",
                                            value = nicAttach.ifBlank { "Select network" },
                                            placeholder = "Select network",
                                            onClick = { picker = VmDevicePicker.NicAttach }
                                        )
                                        ExpressiveTextField(
                                            value = nicType,
                                            onValueChange = { nicType = it },
                                            label = "NIC type",
                                            supportingText = { Text("E1000 or VirtIO") }
                                        )
                                        ExpressiveTextField(
                                            value = mac,
                                            onValueChange = { mac = it },
                                            label = "MAC address",
                                            trailingIcon = {
                                                IconButton(onClick = {
                                                    scope.launch {
                                                        viewModel.randomMac()?.let { mac = it }
                                                    }
                                                }) { Icon(Icons.Default.Refresh, contentDescription = "Generate MAC") }
                                            }
                                        )
                                    }
                                    VmDeviceType.Display -> {
                                        ExpressiveChoiceField(
                                            label = "Resolution",
                                            value = resolution.ifBlank { "Select resolution" },
                                            placeholder = "Select resolution",
                                            onClick = { picker = VmDevicePicker.Resolution }
                                        )
                                        ExpressiveTextField(
                                            value = displayPort,
                                            onValueChange = { displayPort = it },
                                            label = "Port",
                                            supportingText = state.portWizard["port"]?.let { p -> { Text("Suggested: $p") } }
                                        )
                                        ExpressiveChoiceField(
                                            label = "Bind",
                                            value = bind,
                                            placeholder = "Select bind address",
                                            onClick = { picker = VmDevicePicker.Bind }
                                        )
                                        ExpressiveTextField(
                                            value = vncPassword,
                                            onValueChange = { vncPassword = it },
                                            label = "Password",
                                            placeholder = "Optional"
                                        )
                                    }
                                    VmDeviceType.Pci -> {
                                        if (state.iommuEnabled == false) {
                                            Text(
                                                "IOMMU is not enabled on this system; PCI passthrough may not work.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        ExpressiveChoiceField(
                                            label = "PCI device",
                                            value = pptdev.ifBlank { "Select device" },
                                            placeholder = "Select device",
                                            onClick = { picker = VmDevicePicker.Passthrough }
                                        )
                                    }
                                    VmDeviceType.Usb -> {
                                        ExpressiveChoiceField(
                                            label = "Controller",
                                            value = usbController.ifBlank { "Select controller" },
                                            placeholder = "Select controller",
                                            onClick = { picker = VmDevicePicker.UsbController }
                                        )
                                        ExpressiveChoiceField(
                                            label = "USB device",
                                            value = usbDevice.ifBlank { "Select device" },
                                            placeholder = "Select device",
                                            onClick = { picker = VmDevicePicker.UsbDevice }
                                        )
                                    }
                                }
                                ExpressiveTextField(
                                    value = order,
                                    onValueChange = { order = it },
                                    label = "Boot order",
                                    placeholder = "Optional"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    ChoicePickerSheet(
        picker = picker,
        state = state,
        onDismiss = { picker = VmDevicePicker.None },
        onSelect = { value ->
            when (picker) {
                VmDevicePicker.Zvol -> zvolPath = value
                VmDevicePicker.Iotype -> iotype = value
                VmDevicePicker.NicAttach -> nicAttach = value
                VmDevicePicker.Bind -> bind = value
                VmDevicePicker.Resolution -> resolution = value
                VmDevicePicker.UsbController -> usbController = value
                VmDevicePicker.UsbDevice -> usbDevice = value
                VmDevicePicker.Passthrough -> pptdev = value
                else -> Unit
            }
            picker = VmDevicePicker.None
        }
    )

    if (picker == VmDevicePicker.File) {
        ServerPathPickerSheet(
            manager = manager,
            config = ServerPathPickerConfig(
                title = "Choose file",
                confirmLabel = "Use file",
                startPath = "/mnt",
                showFiles = true,
                fileExtensions = if (type == VmDeviceType.Cdrom) listOf("iso") else listOf("raw", "img")
            ),
            onConfirm = {
                if (type == VmDeviceType.Cdrom) isoPath = it else rawPath = it
                picker = VmDevicePicker.None
            },
            onDismiss = { picker = VmDevicePicker.None }
        )
    }
}

@Composable
private fun PathField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onBrowse: () -> Unit
) {
    ExpressiveTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        trailingIcon = {
            IconButton(onClick = onBrowse) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Browse server files")
            }
        }
    )
}

@Composable
private fun ChoicePickerSheet(
    picker: VmDevicePicker,
    state: VmDeviceAddUiState,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    if (picker == VmDevicePicker.None || picker == VmDevicePicker.File) return
    val (title, choices) = when (picker) {
        VmDevicePicker.Zvol -> "Choose ZFS volume" to state.diskChoices
        VmDevicePicker.Iotype -> "Choose I/O type" to state.iotypeChoices
        VmDevicePicker.NicAttach -> "Choose network" to state.nicAttachChoices
        VmDevicePicker.Bind -> "Choose bind address" to state.bindChoices
        VmDevicePicker.Resolution -> "Choose resolution" to state.resolutionChoices
        VmDevicePicker.UsbController -> "Choose USB controller" to state.usbControllerChoices
        VmDevicePicker.UsbDevice -> "Choose USB device" to state.usbPassthroughChoices
        VmDevicePicker.Passthrough -> "Choose PCI device" to
            state.passthroughChoices.mapValues { (_, info) ->
                listOfNotNull(info.description, info.vendor, info.product).joinToString(" · ").ifBlank { "Device" }
            }
        else -> "Choose" to emptyMap()
    }
    ExpressiveChoiceSheet(
        title = title,
        choices = choices.map { (key, value) -> ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key) },
        selectedKey = null,
        searchable = choices.size > 8,
        height = 360,
        onDismiss = onDismiss,
        onSelect = onSelect
    )
}

private fun buildAttributes(
    type: VmDeviceType,
    createZvol: Boolean,
    zvolPath: String,
    zvolName: String,
    zvolSizeGb: String,
    iotype: String,
    isoPath: String,
    rawPath: String,
    nicAttach: String,
    nicType: String,
    mac: String,
    resolution: String,
    displayPort: String,
    bind: String,
    vncPassword: String,
    pptdev: String,
    usbController: String,
    usbDevice: String
): Map<String, Any?> = buildMap {
    when (type) {
        VmDeviceType.Disk -> {
            if (createZvol) {
                put("create_zvol", true)
                put("zvol_name", zvolName.trim())
                zvolSizeGb.trim().toLongOrNull()?.let { put("zvol_volsize", it * 1024 * 1024 * 1024) }
            } else {
                put("path", zvolPath)
            }
            iotype.takeIf { it.isNotBlank() }?.let { put("iotype", it) }
            put("type", "AHCI")
        }
        VmDeviceType.Cdrom -> put("path", isoPath.trim())
        VmDeviceType.Raw -> {
            put("path", rawPath.trim())
            put("type", "AHCI")
            iotype.takeIf { it.isNotBlank() }?.let { put("iotype", it) }
        }
        VmDeviceType.Nic -> {
            put("type", nicType.ifBlank { "E1000" })
            nicAttach.takeIf { it.isNotBlank() }?.let { put("nic_attach", it) }
            mac.takeIf { it.isNotBlank() }?.let { put("mac", it) }
        }
        VmDeviceType.Display -> {
            put("type", "SPICE")
            resolution.takeIf { it.isNotBlank() }?.let { put("resolution", it) }
            displayPort.trim().toIntOrNull()?.let { put("port", it) }
            bind.takeIf { it.isNotBlank() }?.let { put("bind", it) }
            vncPassword.takeIf { it.isNotBlank() }?.let { put("password", it) }
            put("wait", true)
            put("web", true)
        }
        VmDeviceType.Pci -> put("pptdev", pptdev)
        VmDeviceType.Usb -> {
            usbController.takeIf { it.isNotBlank() }?.let { put("controller_type", it) }
            usbDevice.takeIf { it.isNotBlank() }?.let { put("device", it) }
        }
    }
}
