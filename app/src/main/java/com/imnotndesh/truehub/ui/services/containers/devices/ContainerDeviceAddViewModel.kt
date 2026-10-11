package com.imnotndesh.truehub.ui.services.containers.devices
import com.imnotndesh.truehub.ui.utils.friendlyApiError

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ContainerDeviceType(val wireName: String, val label: String) {
    Disk("DISK", "Disk"),
    Cdrom("CDROM", "CD-ROM"),
    Nic("NIC", "NIC"),
    Gpu("GPU", "GPU"),
    Usb("USB", "USB"),
    Pci("PCI", "PCI"),
    Tpm("TPM", "TPM"),
    Proxy("PROXY", "Proxy")
}

data class ContainerDeviceAddUiState(
    val isLoadingChoices: Boolean = true,
    val isLoadingGpuChoices: Boolean = false,
    val isLoadingNicChoices: Boolean = false,
    val diskChoices: Map<String, String> = emptyMap(),
    val gpuChoices: Map<String, Virt.GpuChoice> = emptyMap(),
    val nicChoices: Map<String, String> = emptyMap(),
    val usbChoices: Map<String, Virt.UsbChoice> = emptyMap(),
    val pciChoices: Map<String, String> = emptyMap(),
    val gpuType: String = "PHYSICAL",
    val nicType: String = "BRIDGED",
    val editingDeviceName: String? = null,
    val editingDevice: Map<String, Any?>? = null,
    val isSaving: Boolean = false,
    val added: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContainerDeviceAddViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val editingDeviceName: String? = savedStateHandle["deviceName"]
    private val _uiState = MutableStateFlow(ContainerDeviceAddUiState(editingDeviceName = editingDeviceName))
    val uiState: StateFlow<ContainerDeviceAddUiState> = _uiState.asStateFlow()

    init {
        loadChoices()
    }

    fun loadChoices(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val server = manager.serverBaseHttpUrl
            val cached = ContainerDeviceChoiceCache.get(server)
            if (!forceRefresh && cached != null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingChoices = editingDeviceName != null,
                    diskChoices = cached.disk,
                    gpuChoices = cached.gpu("PHYSICAL").orEmpty(),
                    nicChoices = cached.nic("BRIDGED").orEmpty(),
                    usbChoices = cached.usb,
                    pciChoices = cached.pci
                )
                loadEditingDevice()
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoadingChoices = true, error = null)
            val disks = manager.virtService.getDeviceDiskChoicesWithResult()
            val gpus = manager.virtService.getDeviceGpuChoicesWithResult("PHYSICAL")
            val nics = manager.virtService.getDeviceNicChoicesWithResult("BRIDGED")
            val usbs = manager.virtService.getDeviceUsbChoicesWithResult()
            val pcis = manager.virtService.getDevicePciChoicesWithResult()
            val disk = (disks as? ApiResult.Success)?.data.orEmpty()
            val gpu = (gpus as? ApiResult.Success)?.data.orEmpty()
            val nic = (nics as? ApiResult.Success)?.data.orEmpty()
            val usb = (usbs as? ApiResult.Success)?.data.orEmpty()
            val pci = (pcis as? ApiResult.Success)?.data.orEmpty()
            ContainerDeviceChoiceCache.put(
                server,
                CachedDeviceChoices(
                    disk = disk,
                    gpus = (cached?.gpus.orEmpty() + ("PHYSICAL" to gpu)),
                    nics = (cached?.nics.orEmpty() + ("BRIDGED" to nic)),
                    usb = usb,
                    pci = pci
                )
            )
            _uiState.value = _uiState.value.copy(
                isLoadingChoices = editingDeviceName != null,
                diskChoices = disk,
                gpuChoices = gpu,
                nicChoices = nic,
                usbChoices = usb,
                pciChoices = pci,
                error = listOfNotNull(
                    (disks as? ApiResult.Error)?.message,
                    (gpus as? ApiResult.Error)?.message,
                    (nics as? ApiResult.Error)?.message,
                    (usbs as? ApiResult.Error)?.message,
                    (pcis as? ApiResult.Error)?.message
                ).firstOrNull()
            )
            loadEditingDevice()
        }
    }

    private fun loadEditingDevice() {
        val name = editingDeviceName ?: return
        viewModelScope.launch {
            when (val result = manager.virtService.getInstanceDeviceListRawWithResult(containerId)) {
                is ApiResult.Success -> {
                    val device = result.data.firstOrNull { deviceIdentifier(it) == name }
                    _uiState.value = _uiState.value.copy(
                        isLoadingChoices = false,
                        editingDevice = device,
                        error = if (device == null) "Device is no longer attached" else _uiState.value.error
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoadingChoices = false,
                    error = friendlyApiError(result.message)
                )
                ApiResult.Loading -> Unit
            }
        }
    }

    fun setGpuType(gpuType: String) {
        if (gpuType.isBlank() || gpuType == _uiState.value.gpuType) return
        val server = manager.serverBaseHttpUrl
        val cached = ContainerDeviceChoiceCache.gpu(server, gpuType)
        _uiState.value = _uiState.value.copy(
            gpuType = gpuType,
            gpuChoices = cached.orEmpty(),
            isLoadingGpuChoices = cached == null,
            error = null
        )
        if (cached != null) return
        viewModelScope.launch {
            when (val result = manager.virtService.getDeviceGpuChoicesWithResult(gpuType)) {
                is ApiResult.Success -> {
                    ContainerDeviceChoiceCache.updateGpu(server, gpuType, result.data)
                    if (_uiState.value.gpuType == gpuType) {
                        _uiState.value = _uiState.value.copy(
                            gpuChoices = result.data,
                            isLoadingGpuChoices = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    if (_uiState.value.gpuType == gpuType) {
                        _uiState.value = _uiState.value.copy(
                            isLoadingGpuChoices = false,
                            error = friendlyApiError(result.message)
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun setNicType(nicType: String) {
        if (nicType.isBlank() || nicType == _uiState.value.nicType) return
        val server = manager.serverBaseHttpUrl
        val cached = ContainerDeviceChoiceCache.nic(server, nicType)
        _uiState.value = _uiState.value.copy(
            nicType = nicType,
            nicChoices = cached.orEmpty(),
            isLoadingNicChoices = cached == null,
            error = null
        )
        if (cached != null) return
        viewModelScope.launch {
            when (val result = manager.virtService.getDeviceNicChoicesWithResult(nicType)) {
                is ApiResult.Success -> {
                    ContainerDeviceChoiceCache.updateNic(server, nicType, result.data)
                    if (_uiState.value.nicType == nicType) {
                        _uiState.value = _uiState.value.copy(
                            nicChoices = result.data,
                            isLoadingNicChoices = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    if (_uiState.value.nicType == nicType) {
                        _uiState.value = _uiState.value.copy(
                            isLoadingNicChoices = false,
                            error = friendlyApiError(result.message)
                        )
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun add(
        type: ContainerDeviceType,
        diskSource: String,
        destination: String,
        readOnly: Boolean,
        nicType: String,
        network: String,
        gpuType: String,
        gpu: String,
        usb: String,
        pci: String,
        tpmPath: String,
        sourceProtocol: String,
        sourcePort: String,
        destinationProtocol: String,
        destinationPort: String,
        cdromSource: String,
        bootPriority: String
    ) {
        val current = _uiState.value
        val usbChoice = current.usbChoices[usb]
        val payload = buildMap {
            putAll(current.editingDevice.orEmpty())
            put("dev_type", type.wireName)
            when (type) {
                ContainerDeviceType.Disk -> {
                    put("source", diskSource)
                    put("destination", destination.trim())
                    put("readonly", readOnly)
                }
                ContainerDeviceType.Cdrom -> {
                    put("source", cdromSource.trim())
                    put("readonly", readOnly)
                    bootPriority.trim().toIntOrNull()?.let { put("boot_priority", it) }
                }
                ContainerDeviceType.Nic -> {
                    put("nic_type", nicType)
                    put("network", network)
                }
                ContainerDeviceType.Gpu -> {
                    put("gpu_type", gpuType)
                    put("id", gpu)
                }
                ContainerDeviceType.Usb -> {
                    put("vendor_id", usbChoice?.vendorId)
                    put("product_id", usbChoice?.productId)
                }
                ContainerDeviceType.Pci -> put("address", pci)
                ContainerDeviceType.Tpm -> put("path", tpmPath.trim())
                ContainerDeviceType.Proxy -> {
                    put("source_proto", sourceProtocol)
                    put("source_port", sourcePort.trim().toIntOrNull() ?: 0)
                    put("dest_proto", destinationProtocol)
                    put("dest_port", destinationPort.trim().toIntOrNull() ?: 0)
                }
            }
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val result = if (current.editingDeviceName == null) {
                manager.virtService.addInstanceDeviceRawWithResult(containerId, payload)
            } else {
                manager.virtService.updateInstanceDeviceRawWithResult(containerId, payload)
            }
            when (result) {
                is ApiResult.Success -> {
                    if (result.data) {
                        _uiState.value = _uiState.value.copy(isSaving = false, added = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "The server did not confirm the device change"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = friendlyApiError(result.message))
                ApiResult.Loading -> Unit
            }
        }
    }
}

internal fun deviceIdentifier(device: Map<String, Any?>): String? {
    sequenceOf("name", "id", "address", "path", "source")
        .mapNotNull { key -> device[key]?.toString()?.takeIf { it.isNotBlank() } }
        .firstOrNull()
        ?.let { return it }
    val vendor = device["vendor_id"]?.toString()?.takeIf { it.isNotBlank() }
    val product = device["product_id"]?.toString()?.takeIf { it.isNotBlank() }
    return if (vendor != null && product != null) "$vendor:$product" else null
}

private data class CachedDeviceChoices(
    val disk: Map<String, String> = emptyMap(),
    val gpus: Map<String, Map<String, Virt.GpuChoice>> = emptyMap(),
    val nics: Map<String, Map<String, String>> = emptyMap(),
    val usb: Map<String, Virt.UsbChoice> = emptyMap(),
    val pci: Map<String, String> = emptyMap()
) {
    fun gpu(type: String): Map<String, Virt.GpuChoice>? = gpus[type]
    fun nic(type: String): Map<String, String>? = nics[type]
}

private object ContainerDeviceChoiceCache {
    private val values = ConcurrentHashMap<String, CachedDeviceChoices>()

    fun get(server: String): CachedDeviceChoices? = values[server]

    fun put(server: String, choices: CachedDeviceChoices) {
        values[server] = choices
    }

    fun gpu(server: String, type: String): Map<String, Virt.GpuChoice>? = values[server]?.gpus?.get(type)

    fun nic(server: String, type: String): Map<String, String>? = values[server]?.nics?.get(type)

    fun updateGpu(server: String, type: String, choices: Map<String, Virt.GpuChoice>) {
        values.computeIfPresent(server) { _, current -> current.copy(gpus = current.gpus + (type to choices)) }
    }

    fun updateNic(server: String, type: String, choices: Map<String, String>) {
        values.computeIfPresent(server) { _, current -> current.copy(nics = current.nics + (type to choices)) }
    }
}
