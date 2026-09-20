package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Vm
import com.squareup.moshi.Types

class VmService(var manager: TrueNASApiManager) {
    suspend fun queryAllVmsWithResult(): ApiResult<List<Vm.VmQueryResponse>>{
        val type = Types.newParameterizedType(List::class.java, Vm.VmQueryResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_ALL_VM_INSTANCES,
            params = listOf(),
            resultType = type
        )
    }
    // Get a specific instance
    suspend fun queryVmInstanceWithResult(id: Int): ApiResult<Vm.VmQueryResponse> {
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_INSTANCE,
            params = listOf(id),
            resultType = Vm.VmQueryResponse::class.java
        )
    }

    // Start Vm
    suspend fun startVmInstanceWithResult(id : Int,overcommit: Boolean = false): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.START_VM_INSTANCE,
            params = listOf(id, Vm.StartOptions(overcommit)),
            resultType = Int::class.java
        )
    }

    // Restart VM
    suspend fun restartVmInstanceWithResult(id : Int): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.RESTART_INSTANCE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Stop Vm

    suspend fun stopVmInstanceWithResult(id : Int, force: Boolean? = false, forceAfterTimeout : Boolean? = false): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.STOP_INSTANCE,
            params = listOf(id, Vm.StopOptions(force, forceAfterTimeout)),
            resultType = Int::class.java
        )
    }

    // Suspend Vm
    suspend fun suspendVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.SUSPEND_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    // Power-off VM
    suspend fun powerOffVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.POWER_OFF_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    // Resume VM
    suspend fun resumeVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.RESUME_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    // Clone VM
    suspend fun cloneVmInstanceWithResult(id: Int, cloneName :String? = null): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.Vm.CLONE_VM,
            params = listOf(id,cloneName),
            resultType = Boolean::class.java
        )
    }
    // Get Memory usage
    suspend fun getVmMemoryUsageWithResult(id: Int): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_VM_MEMORY_USAGE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Delete VM
    suspend fun deleteVmInstanceWithResult(id: Int,deleteZvols: Boolean? = false, forceDelete: Boolean? = false): ApiResult<Boolean>{
        return manager.callWithResult(
            method = ApiMethods.Vm.DELETE_INSTANCE,
            params = listOf(id, Vm.DeleteOptions(deleteZvols,forceDelete)),
            resultType = Boolean::class.java
        )
    }

    /**
     * Get vm Display
     * responds with:
     * @see Vm.VmDisplayUriQueryResponse
     */

    suspend fun getVmDisplayWithResult(id: Int): ApiResult<Vm.VmDisplayUriQueryResponse>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_DISPLAY_URL,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    /**
     * [Get VM Status]
     * Responds with
     * @see Vm.VmStatus
     */
    suspend fun getVmInstanceStatusWithResult(id: Int): ApiResult<Vm.VmStatus>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_VM_STATUS,
            params = listOf(id),
            resultType = Vm.VmStatus::class.java
        )
    }


    suspend fun bootloaderOptionsWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_BOOTLOADER_OPTIONS, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun bootloaderOvmfChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_BOOTLOADER_OVMF_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun cpuModelChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_CPU_MODEL_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun createWithResult(vmCreate: Any?): ApiResult<Vm.VmQueryResponse> =
        manager.callWithResult(ApiMethods.Vm.VM_CREATE, listOf(vmCreate), Vm.VmQueryResponse::class.java)

    suspend fun deviceWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE, listOf(), Any::class.java)

    suspend fun deviceBindChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_BIND_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceConvertWithResult(vmConvert: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_CONVERT, listOf(vmConvert), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceCreateWithResult(vmDeviceCreate: Any?): ApiResult<Vm.DeviceEntry> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_CREATE, listOf(vmDeviceCreate), Vm.DeviceEntry::class.java)

    suspend fun deviceDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_DELETE, listOf(id, options), Unit::class.java)

    suspend fun deviceDiskChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_DISK_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Vm.DeviceEntry> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_GET_INSTANCE, listOf(id, options), Vm.DeviceEntry::class.java)

    suspend fun deviceIommuEnabledWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_IOMMU_ENABLED, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceIotypeChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_IOTYPE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceNicAttachChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_NIC_ATTACH_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun devicePassthroughDeviceWithResult(device: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_PASSTHROUGH_DEVICE, listOf(device), Any::class.java)

    suspend fun devicePassthroughDeviceChoicesWithResult(): ApiResult<Vm.PassthroughInfo> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_PASSTHROUGH_DEVICE_CHOICES, listOf(), Vm.PassthroughInfo::class.java)

    suspend fun devicePptdevChoicesWithResult(): ApiResult<Vm.PassthroughInfo> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_PPTDEV_CHOICES, listOf(), Vm.PassthroughInfo::class.java)

    suspend fun deviceQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Vm.DeviceEntry>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Vm.DeviceEntry::class.java))

    suspend fun deviceUpdateWithResult(id: Any?, vmDeviceUpdate: Any?): ApiResult<Vm.DeviceEntry> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_UPDATE, listOf(id, vmDeviceUpdate), Vm.DeviceEntry::class.java)

    suspend fun deviceUsbControllerChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_USB_CONTROLLER_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun deviceUsbPassthroughChoicesWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_USB_PASSTHROUGH_CHOICES, listOf(), Any::class.java)

    suspend fun deviceUsbPassthroughDeviceWithResult(device: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_USB_PASSTHROUGH_DEVICE, listOf(device), Any::class.java)

    suspend fun deviceVirtualSizeWithResult(vmVirtualSize: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_DEVICE_VIRTUAL_SIZE, listOf(vmVirtualSize), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun exportDiskImageWithResult(vmExportDiskImage: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_EXPORT_DISK_IMAGE, listOf(vmExportDiskImage), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun flagsWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_FLAGS, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getAvailableMemoryWithResult(overcommit: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GET_AVAILABLE_MEMORY, listOf(overcommit), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getConsoleWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GET_CONSOLE, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getDisplayDevicesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GET_DISPLAY_DEVICES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getVmMemoryInfoWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GET_VM_MEMORY_INFO, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getVmemoryInUseWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GET_VMEMORY_IN_USE, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun guestArchitectureAndMachineChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_GUEST_ARCHITECTURE_AND_MACHINE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun importDiskImageWithResult(vmImportDiskImage: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_IMPORT_DISK_IMAGE, listOf(vmImportDiskImage), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun logFileDownloadWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_LOG_FILE_DOWNLOAD, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun logFilePathWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_LOG_FILE_PATH, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun maximumSupportedVcpusWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_MAXIMUM_SUPPORTED_VCPUS, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun portWizardWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_PORT_WIZARD, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun randomMacWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_RANDOM_MAC, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun resolutionChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_RESOLUTION_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun supportsVirtualizationWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_SUPPORTS_VIRTUALIZATION, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(id: Any?, vmUpdate: Any?): ApiResult<Vm.VmQueryResponse> =
        manager.callWithResult(ApiMethods.Vm.VM_UPDATE, listOf(id, vmUpdate), Vm.VmQueryResponse::class.java)

    suspend fun virtualizationDetailsWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Vm.VM_VIRTUALIZATION_DETAILS, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

}
