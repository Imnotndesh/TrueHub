package com.imnotndesh.truehub.ui.services.vm.form

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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveToggleRow

private val CPU_MODES = listOf("HOST-MODEL", "HOST-PASSTHROUGH", "CUSTOM")
private val TIME_ZONES = listOf("LOCAL", "UTC")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmFormScreen(
    vmId: Int?,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: VmFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var form by remember { mutableStateOf(VmFormState()) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) form = state.form
    }
    LaunchedEffect(state.completed) {
        if (state.completed) onSaved()
    }

    var picker by remember { mutableStateOf<VmFormPicker?>(null) }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = if (state.isEdit) "Edit VM" else "Create VM",
                subtitle = if (state.isEdit) form.name else "New virtual machine",
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
                        onClick = { viewModel.save(form) },
                        enabled = !state.isSaving && !state.isLoading,
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
                            Text(if (state.isEdit) "Save" else "Create")
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
                LoadingScreen("Loading VM options")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ExpressiveSection(title = "Identity", icon = Icons.Default.Info) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                ExpressiveTextField(
                                    value = form.name,
                                    onValueChange = { form = form.copy(name = it) },
                                    label = "Name"
                                )
                                ExpressiveTextField(
                                    value = form.description,
                                    onValueChange = { form = form.copy(description = it) },
                                    label = "Description",
                                    singleLine = false
                                )
                            }
                        }
                    }

                    ExpressiveSection(title = "Resources", icon = Icons.Default.Memory) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                ExpressiveTextField(
                                    value = form.memory,
                                    onValueChange = { form = form.copy(memory = it) },
                                    label = "Memory (MB)",
                                    supportingText = state.availableMemoryMb?.let {
                                        { Text("Up to ${it / 1024} GB available") }
                                    }
                                )
                                ExpressiveTextField(
                                    value = form.minMemory,
                                    onValueChange = { form = form.copy(minMemory = it) },
                                    label = "Minimum memory (MB)",
                                    placeholder = "Optional (ballooning)"
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ExpressiveTextField(
                                        value = form.vcpus,
                                        onValueChange = { form = form.copy(vcpus = it) },
                                        label = "vCPUs",
                                        supportingText = state.maxVcpus?.let { { Text("Max $it") } },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ExpressiveTextField(
                                        value = form.cores,
                                        onValueChange = { form = form.copy(cores = it) },
                                        label = "Cores",
                                        modifier = Modifier.weight(1f)
                                    )
                                    ExpressiveTextField(
                                        value = form.threads,
                                        onValueChange = { form = form.copy(threads = it) },
                                        label = "Threads",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                ExpressiveTextField(
                                    value = form.cpuset,
                                    onValueChange = { form = form.copy(cpuset = it) },
                                    label = "CPU set",
                                    placeholder = "Optional (e.g. 0-3)"
                                )
                                ExpressiveTextField(
                                    value = form.nodeset,
                                    onValueChange = { form = form.copy(nodeset = it) },
                                    label = "NUMA node set",
                                    placeholder = "Optional"
                                )
                            }
                        }
                    }

                    ExpressiveSection(title = "Firmware & CPU", icon = Icons.Default.Computer) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                ExpressiveChoiceField(
                                    label = "Bootloader",
                                    value = form.bootloader,
                                    placeholder = "Select bootloader",
                                    onClick = { picker = VmFormPicker.Bootloader }
                                )
                                ExpressiveChoiceField(
                                    label = "OVMF firmware",
                                    value = form.bootloaderOvmf.ifBlank { "Default" },
                                    placeholder = "Select OVMF",
                                    onClick = { picker = VmFormPicker.Ovmf }
                                )
                                ExpressiveChoiceField(
                                    label = "CPU mode",
                                    value = form.cpuMode,
                                    placeholder = "Select CPU mode",
                                    onClick = { picker = VmFormPicker.CpuMode }
                                )
                                if (form.cpuMode == "CUSTOM") {
                                    ExpressiveChoiceField(
                                        label = "CPU model",
                                        value = form.cpuModel.ifBlank { "Select model" },
                                        placeholder = "Select CPU model",
                                        onClick = { picker = VmFormPicker.CpuModel }
                                    )
                                }
                                ExpressiveChoiceField(
                                    label = "Architecture",
                                    value = form.archType.ifBlank { "Default" },
                                    placeholder = "Select architecture",
                                    onClick = { picker = VmFormPicker.Arch }
                                )
                                ExpressiveChoiceField(
                                    label = "Machine type",
                                    value = form.machineType.ifBlank { "Default" },
                                    placeholder = "Select machine",
                                    onClick = { picker = VmFormPicker.Machine }
                                )
                                ExpressiveToggleRow(
                                    title = "Secure boot",
                                    description = "Enable UEFI Secure Boot",
                                    checked = form.enableSecureBoot,
                                    onCheckedChange = { form = form.copy(enableSecureBoot = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "Trusted Platform Module",
                                    description = "Enable a virtual TPM",
                                    checked = form.trustedPlatformModule,
                                    onCheckedChange = { form = form.copy(trustedPlatformModule = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "CPU topology extension",
                                    description = "Expose detailed CPU topology to the guest",
                                    checked = form.enableCpuTopologyExtension,
                                    onCheckedChange = { form = form.copy(enableCpuTopologyExtension = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "Pin vCPUs",
                                    description = "Pin virtual CPUs to host cores",
                                    checked = form.pinVcpus,
                                    onCheckedChange = { form = form.copy(pinVcpus = it) }
                                )
                            }
                        }
                    }

                    ExpressiveSection(title = "Behaviour", icon = Icons.Default.Settings) {
                        ExpressiveInfoCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                ExpressiveToggleRow(
                                    title = "Autostart",
                                    description = "Start the VM when TrueNAS starts",
                                    checked = form.autostart,
                                    onCheckedChange = { form = form.copy(autostart = it) }
                                )
                                ExpressiveChoiceField(
                                    label = "Time zone",
                                    value = form.time,
                                    placeholder = "Select time zone",
                                    onClick = { picker = VmFormPicker.Time }
                                )
                                ExpressiveTextField(
                                    value = form.shutdownTimeout,
                                    onValueChange = { form = form.copy(shutdownTimeout = it) },
                                    label = "Shutdown timeout (s)"
                                )
                                ExpressiveToggleRow(
                                    title = "Hyper-V enlightenments",
                                    description = "Improve Windows guest performance",
                                    checked = form.hypervEnlightenments,
                                    onCheckedChange = { form = form.copy(hypervEnlightenments = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "Hide from MSR",
                                    description = "Hide hypervisor signatures",
                                    checked = form.hideFromMsr,
                                    onCheckedChange = { form = form.copy(hideFromMsr = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "Ensure display device",
                                    description = "Always configure a display device",
                                    checked = form.ensureDisplayDevice,
                                    onCheckedChange = { form = form.copy(ensureDisplayDevice = it) }
                                )
                                ExpressiveToggleRow(
                                    title = "Suspend on snapshot",
                                    description = "Suspend the VM when taking snapshots",
                                    checked = form.suspendOnSnapshot,
                                    onCheckedChange = { form = form.copy(suspendOnSnapshot = it) }
                                )
                                ExpressiveTextField(
                                    value = form.commandLineArgs,
                                    onValueChange = { form = form.copy(commandLineArgs = it) },
                                    label = "Additional command line args",
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

    picker?.let { current ->
        val (title, choices, selected) = when (current) {
            VmFormPicker.Bootloader -> Triple("Choose bootloader", state.bootloaderOptions, form.bootloader)
            VmFormPicker.Ovmf -> Triple("Choose OVMF firmware", state.ovmfChoices, form.bootloaderOvmf)
            VmFormPicker.CpuMode -> Triple("Choose CPU mode", CPU_MODES.associateWith { it }, form.cpuMode)
            VmFormPicker.CpuModel -> Triple("Choose CPU model", state.cpuModels, form.cpuModel)
            VmFormPicker.Arch -> Triple("Choose architecture", state.archMachines.keys.associateWith { it }, form.archType)
            VmFormPicker.Machine -> Triple(
                "Choose machine type",
                (state.archMachines[form.archType] ?: state.archMachines.values.firstOrNull().orEmpty()).associateWith { it },
                form.machineType
            )
            VmFormPicker.Time -> Triple("Choose time zone", TIME_ZONES.associateWith { it }, form.time)
        }
        ExpressiveChoiceSheet(
            title = title,
            choices = choices.map { (key, value) -> ExpressiveChoice(key = key, label = value, supporting = key) },
            selectedKey = selected.ifBlank { null },
            searchable = choices.size > 8,
            height = 360,
            onDismiss = { picker = null },
            onSelect = { value ->
                form = when (current) {
                    VmFormPicker.Bootloader -> form.copy(bootloader = value)
                    VmFormPicker.Ovmf -> form.copy(bootloaderOvmf = value)
                    VmFormPicker.CpuMode -> form.copy(cpuMode = value)
                    VmFormPicker.CpuModel -> form.copy(cpuModel = value)
                    VmFormPicker.Arch -> form.copy(archType = value, machineType = "")
                    VmFormPicker.Machine -> form.copy(machineType = value)
                    VmFormPicker.Time -> form.copy(time = value)
                }
                picker = null
            }
        )
    }
}

private enum class VmFormPicker {
    Bootloader,
    Ovmf,
    CpuMode,
    CpuModel,
    Arch,
    Machine,
    Time
}
