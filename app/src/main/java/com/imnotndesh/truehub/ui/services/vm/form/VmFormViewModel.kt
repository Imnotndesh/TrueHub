package com.imnotndesh.truehub.ui.services.vm.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Vm
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VmFormState(
    val name: String = "",
    val description: String = "",
    val vcpus: String = "1",
    val cores: String = "1",
    val threads: String = "1",
    val memory: String = "",
    val minMemory: String = "",
    val cpuset: String = "",
    val nodeset: String = "",
    val cpuMode: String = "HOST-MODEL",
    val cpuModel: String = "",
    val bootloader: String = "UEFI",
    val bootloaderOvmf: String = "",
    val archType: String = "",
    val machineType: String = "",
    val autostart: Boolean = false,
    val enableSecureBoot: Boolean = false,
    val trustedPlatformModule: Boolean = false,
    val hypervEnlightenments: Boolean = false,
    val hideFromMsr: Boolean = false,
    val ensureDisplayDevice: Boolean = true,
    val suspendOnSnapshot: Boolean = false,
    val enableCpuTopologyExtension: Boolean = false,
    val pinVcpus: Boolean = false,
    val time: String = "LOCAL",
    val shutdownTimeout: String = "90",
    val commandLineArgs: String = ""
)

internal fun Vm.VmQueryResponse.toFormState() = VmFormState(
    name = name,
    description = description,
    vcpus = vcpus.toString(),
    cores = cores.toString(),
    threads = threads.toString(),
    memory = memory.toString(),
    minMemory = min_memory?.toString().orEmpty(),
    cpuset = cpuset.orEmpty(),
    nodeset = nodeset.orEmpty(),
    cpuMode = cpu_mode?.name ?: "HOST-MODEL",
    cpuModel = cpu_model.orEmpty(),
    bootloader = bootloader,
    bootloaderOvmf = bootloader_ovmf.orEmpty(),
    archType = arch_type.orEmpty(),
    machineType = machine_type.orEmpty(),
    autostart = autostart,
    enableSecureBoot = enable_secure_boot,
    trustedPlatformModule = trusted_platform_module,
    hypervEnlightenments = hyperv_enlightenments,
    hideFromMsr = hide_from_msr,
    ensureDisplayDevice = ensure_display_device,
    suspendOnSnapshot = suspend_on_snapshot,
    enableCpuTopologyExtension = enable_cpu_topology_extension,
    pinVcpus = pin_vcpus,
    time = time,
    shutdownTimeout = shutdown_timeout.toString(),
    commandLineArgs = command_line_args.orEmpty()
)

internal fun VmFormState.toPayload(): Map<String, Any?> = buildMap {
    put("name", name.trim())
    if (description.isNotBlank()) put("description", description.trim())
    vcpus.trim().toIntOrNull()?.let { put("vcpus", it) }
    cores.trim().toIntOrNull()?.let { put("cores", it) }
    threads.trim().toIntOrNull()?.let { put("threads", it) }
    memory.trim().toIntOrNull()?.let { put("memory", it) }
    minMemory.trim().toIntOrNull()?.let { put("min_memory", it) }
    cpuset.trim().takeIf { it.isNotBlank() }?.let { put("cpuset", it) }
    nodeset.trim().takeIf { it.isNotBlank() }?.let { put("nodeset", it) }
    put("cpu_mode", cpuMode)
    cpuModel.trim().takeIf { it.isNotBlank() }?.let { put("cpu_model", it) }
    put("bootloader", bootloader)
    bootloaderOvmf.trim().takeIf { it.isNotBlank() }?.let { put("bootloader_ovmf", it) }
    archType.trim().takeIf { it.isNotBlank() }?.let { put("arch_type", it) }
    machineType.trim().takeIf { it.isNotBlank() }?.let { put("machine_type", it) }
    put("autostart", autostart)
    put("enable_secure_boot", enableSecureBoot)
    put("trusted_platform_module", trustedPlatformModule)
    put("hyperv_enlightenments", hypervEnlightenments)
    put("hide_from_msr", hideFromMsr)
    put("ensure_display_device", ensureDisplayDevice)
    put("suspend_on_snapshot", suspendOnSnapshot)
    put("enable_cpu_topology_extension", enableCpuTopologyExtension)
    put("pin_vcpus", pinVcpus)
    put("time", time)
    shutdownTimeout.trim().toIntOrNull()?.let { put("shutdown_timeout", it) }
    commandLineArgs.trim().takeIf { it.isNotBlank() }?.let { put("command_line_args", it) }
}

data class VmFormUiState(
    val bootloaderOptions: Map<String, String> = emptyMap(),
    val ovmfChoices: Map<String, String> = emptyMap(),
    val cpuModels: Map<String, String> = emptyMap(),
    val archMachines: Map<String, List<String>> = emptyMap(),
    val maxVcpus: Int? = null,
    val availableMemoryMb: Long? = null,
    val supportsVirtualization: Boolean? = null,
    val form: VmFormState = VmFormState(),
    val isEdit: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val completed: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VmFormViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val vmId: Int? = savedStateHandle.get<Int>("vmId")
    private val _uiState = MutableStateFlow(VmFormUiState(isEdit = vmId != null))
    val uiState: StateFlow<VmFormUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val bootloaders = manager.vmService.bootloaderOptionsWithResult()
            val ovmf = manager.vmService.bootloaderOvmfChoicesWithResult()
            val cpuModels = manager.vmService.cpuModelChoicesWithResult()
            val archMachines = manager.vmService.guestArchitectureAndMachineChoicesWithResult()
            val maxVcpus = manager.vmService.maximumSupportedVcpusWithResult()
            val availableMemory = manager.vmService.getAvailableMemoryWithResult(false)
            val supportsVirt = manager.vmService.supportsVirtualizationWithResult()

            val form = if (vmId != null) {
                when (val result = manager.vmService.queryVmInstanceWithResult(vmId)) {
                    is ApiResult.Success -> result.data.toFormState()
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        return@launch
                    }
                    ApiResult.Loading -> _uiState.value.form
                }
            } else {
                _uiState.value.form
            }

            _uiState.update {
                it.copy(
                    bootloaderOptions = (bootloaders as? ApiResult.Success)?.data ?: it.bootloaderOptions,
                    ovmfChoices = (ovmf as? ApiResult.Success)?.data ?: it.ovmfChoices,
                    cpuModels = (cpuModels as? ApiResult.Success)?.data ?: it.cpuModels,
                    archMachines = (archMachines as? ApiResult.Success)?.data ?: it.archMachines,
                    maxVcpus = (maxVcpus as? ApiResult.Success)?.data ?: it.maxVcpus,
                    availableMemoryMb = (availableMemory as? ApiResult.Success)?.data?.let { bytes -> bytes / (1024 * 1024) }
                        ?: it.availableMemoryMb,
                    supportsVirtualization = (supportsVirt as? ApiResult.Success)?.data ?: it.supportsVirtualization,
                    form = form,
                    isLoading = false
                )
            }
        }
    }

    fun updateForm(form: VmFormState) {
        _uiState.update { it.copy(form = form) }
    }

    fun save(form: VmFormState) {
        val validationError = when {
            form.name.isBlank() -> "Enter a VM name"
            form.memory.trim().toIntOrNull() == null -> "Memory (MB) is required"
            form.vcpus.trim().toIntOrNull() == null -> "vCPUs are required"
            form.cores.trim().toIntOrNull() == null -> "Cores are required"
            form.threads.trim().toIntOrNull() == null -> "Threads are required"
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val payload = form.toPayload()
            val result = if (vmId != null) {
                manager.vmService.updateWithResult(vmId, payload)
            } else {
                manager.vmService.createWithResult(payload)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSaving = false, completed = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
                ApiResult.Loading -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
