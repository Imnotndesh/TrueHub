package com.imnotndesh.truehub.ui.services.vm.devices

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

enum class VmDeviceType(val wireName: String, val label: String) {
    Disk("DISK", "Disk"),
    Cdrom("CDROM", "CD-ROM"),
    Raw("RAW", "Raw file"),
    Nic("NIC", "NIC"),
    Display("DISPLAY", "Display"),
    Pci("PCI", "PCI passthrough"),
    Usb("USB", "USB passthrough")
}

data class VmDeviceAddUiState(
    val diskChoices: Map<String, String> = emptyMap(),
    val iotypeChoices: Map<String, String> = emptyMap(),
    val nicAttachChoices: Map<String, String> = emptyMap(),
    val bindChoices: Map<String, String> = emptyMap(),
    val resolutionChoices: Map<String, String> = emptyMap(),
    val usbControllerChoices: Map<String, String> = emptyMap(),
    val usbPassthroughChoices: Map<String, String> = emptyMap(),
    val passthroughChoices: Map<String, Vm.PassthroughInfo> = emptyMap(),
    val iommuEnabled: Boolean? = null,
    val portWizard: Map<String, Int> = emptyMap(),
    val editingDevice: Vm.DeviceEntry? = null,
    val editingDeviceId: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VmDeviceAddViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val vmId: Int = savedStateHandle["vmId"] ?: -1
    private val _uiState = MutableStateFlow(VmDeviceAddUiState())
    val uiState: StateFlow<VmDeviceAddUiState> = _uiState.asStateFlow()

    init {
        loadChoices()
    }

    fun loadChoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val disk = manager.vmService.deviceDiskChoicesWithResult()
            val iotype = manager.vmService.deviceIotypeChoicesWithResult()
            val nicAttach = manager.vmService.deviceNicAttachChoicesWithResult()
            val bind = manager.vmService.deviceBindChoicesWithResult()
            val resolution = manager.vmService.resolutionChoicesWithResult()
            val usbController = manager.vmService.deviceUsbControllerChoicesWithResult()
            val usbPassthrough = manager.vmService.deviceUsbPassthroughChoicesWithResult()
            val passthrough = manager.vmService.devicePassthroughDeviceChoicesWithResult()
            val pptdev = manager.vmService.devicePptdevChoicesWithResult()
            val iommu = manager.vmService.deviceIommuEnabledWithResult()
            val portWizard = manager.vmService.portWizardWithResult()

            val passthroughMap = ((passthrough as? ApiResult.Success)?.data ?: emptyMap())
                .ifEmpty { (pptdev as? ApiResult.Success)?.data ?: emptyMap() }

            _uiState.update {
                it.copy(
                    diskChoices = (disk as? ApiResult.Success)?.data ?: it.diskChoices,
                    iotypeChoices = (iotype as? ApiResult.Success)?.data ?: it.iotypeChoices,
                    nicAttachChoices = (nicAttach as? ApiResult.Success)?.data ?: it.nicAttachChoices,
                    bindChoices = (bind as? ApiResult.Success)?.data ?: it.bindChoices,
                    resolutionChoices = (resolution as? ApiResult.Success)?.data ?: it.resolutionChoices,
                    usbControllerChoices = (usbController as? ApiResult.Success)?.data ?: it.usbControllerChoices,
                    usbPassthroughChoices = (usbPassthrough as? ApiResult.Success)?.data ?: it.usbPassthroughChoices,
                    passthroughChoices = passthroughMap,
                    iommuEnabled = (iommu as? ApiResult.Success)?.data ?: it.iommuEnabled,
                    portWizard = (portWizard as? ApiResult.Success)?.data ?: it.portWizard,
                    isLoading = false
                )
            }
        }
    }

    fun loadDevice(deviceId: Int) {
        viewModelScope.launch {
            when (val result = manager.vmService.deviceGetInstanceWithResult(deviceId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(editingDevice = result.data, editingDeviceId = result.data.id ?: deviceId)
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun save(dtype: String, attributes: Map<String, Any?>, order: Int?) {
        if (vmId < 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val payload = buildMap<String, Any?> {
                put("vm", vmId)
                put("attributes", attributes + mapOf("dtype" to dtype))
                order?.let { put("order", it) }
            }
            val deviceId = _uiState.value.editingDeviceId
            val result = if (deviceId != null) {
                manager.vmService.deviceUpdateWithResult(deviceId, payload)
            } else {
                manager.vmService.deviceCreateWithResult(payload)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                ApiResult.Loading -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    suspend fun randomMac(): String? =
        (manager.vmService.randomMacWithResult() as? ApiResult.Success)?.data

    suspend fun virtualSize(path: String): Long? =
        (manager.vmService.deviceVirtualSizeWithResult(mapOf("path" to path)) as? ApiResult.Success)?.data

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
