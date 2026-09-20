package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Virt
import com.squareup.moshi.Types

class VirtService(val manager: TrueNASApiManager) {
    // Get all instance
    suspend fun getAllInstancesWithResult(): ApiResult<List<Virt.ContainerResponse>>{
        val type = Types.newParameterizedType(List::class.java, Virt.ContainerResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(),
            resultType = type
        )
    }

    // Get an instance
    suspend fun genAnInstanceWithResult(id: String): ApiResult<Virt.ContainerResponse>{
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    // Start Instance
    suspend fun startVirtInstanceWithResult(id : String): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.START_INSTANCE,
            params = listOf(id),
            resultType = Double::class.java
        )
    }
    // Stop Instance
    suspend fun stopVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.STOP_INSTANCE,
            params = listOf(id, Virt.stopArgs(timeout, force)),
            resultType = Double::class.java
        )
    }
    // Restart Instance
    suspend fun restartVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.RESTART_INSTANCE,
            params = listOf(id, Virt.stopArgs(timeout, force)),
            resultType = Int::class.java
        )
    }
    // Delete virt instance
    suspend fun deleteVirtInstanceWithResult(id : String): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Virt.DELETE_INSTANCE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Update Instance
    suspend fun updateVirtInstanceWithResult(id : String,newInstanceInfo: Virt.ContainerUpdate): ApiResult<Virt.ContainerResponse> {
        return manager.callWithResult(
            method = ApiMethods.Virt.UPDATE_INSTANCE,
            params = listOf(id,newInstanceInfo),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    // Get all instance devices
    suspend fun getVirtInstanceDeviceListWithResult(id:String): ApiResult<List<Virt.Device>>{
        val type = Types.newParameterizedType(List::class.java, Virt.Device::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = type::class.java
        )
    }
    // Delete virt instance device
    suspend fun deleteVirtInstanceDeviceWithResult(id: String,deviceName: String): ApiResult<Boolean>{
        return manager.callWithResult(
            method = ApiMethods.Virt.DELETE_INSTANCE_DEVICE,
            params = listOf(id,deviceName),
            resultType = Boolean::class.java
        )
    }

    // Get Virt image choices
    suspend fun getVirtImageChoicesWithResult(): ApiResult<List<Virt.ImageChoice>>{
        val type = Types.newParameterizedType(List::class.java, Virt.ImageChoice::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_IMAGE_CHOICES,
            params = listOf(),
            resultType = type
        )
    }

    private fun choiceType() = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)

    // ── virt.device ──
    suspend fun getDeviceDiskChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_DISK_CHOICES, listOf(), choiceType())

    suspend fun getDeviceGpuChoicesWithResult(gpuType: String? = null): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_GPU_CHOICES, listOfNotNull(gpuType), choiceType())

    suspend fun getDeviceNicChoicesWithResult(nicType: String? = null): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_NIC_CHOICES, listOfNotNull(nicType), choiceType())

    suspend fun getDevicePciChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_PCI_CHOICES, listOf(), choiceType())

    suspend fun getDeviceUsbChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_USB_CHOICES, listOf(), choiceType())

    suspend fun exportDiskImageWithResult(payload: Map<String, Any?>): ApiResult<Int> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_EXPORT_DISK_IMAGE, listOf(payload), Int::class.java)

    suspend fun importDiskImageWithResult(payload: Map<String, Any?>): ApiResult<Int> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_IMPORT_DISK_IMAGE, listOf(payload), Int::class.java)

    // ── virt.global ──
    suspend fun getGlobalConfigWithResult(): ApiResult<Virt.GlobalEntry> =
        manager.callWithResult(ApiMethods.Virt.GLOBAL_CONFIG, listOf(), Virt.GlobalEntry::class.java)

    suspend fun updateGlobalConfigWithResult(update: Virt.GlobalUpdate): ApiResult<Virt.GlobalEntry> =
        manager.callWithResult(ApiMethods.Virt.GLOBAL_UPDATE, listOf(update), Virt.GlobalEntry::class.java)

    suspend fun getGlobalNetworkWithResult(name: String): ApiResult<Virt.GlobalNetwork> =
        manager.callWithResult(ApiMethods.Virt.GLOBAL_GET_NETWORK, listOf(name), Virt.GlobalNetwork::class.java)

    suspend fun getGlobalBridgeChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.GLOBAL_BRIDGE_CHOICES, listOf(), choiceType())

    suspend fun getGlobalPoolChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Virt.GLOBAL_POOL_CHOICES, listOf(), choiceType())

    // ── virt.instance ──
    suspend fun createInstanceWithResult(payload: Map<String, Any?>): ApiResult<Virt.ContainerResponse> =
        manager.callWithResult(ApiMethods.Virt.CREATE_INSTANCE, listOf(payload), Virt.ContainerResponse::class.java)

    suspend fun getInstanceWithResult(id: String): ApiResult<Virt.ContainerResponse> =
        manager.callWithResult(ApiMethods.Virt.GET_INSTANCE, listOf(id), Virt.ContainerResponse::class.java)

    private fun deviceListType() = Types.newParameterizedType(List::class.java, Virt.Device::class.java)

    suspend fun addInstanceDeviceWithResult(id: String, device: Map<String, Any?>): ApiResult<List<Virt.Device>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_ADD, listOf(id, device), deviceListType())

    suspend fun getInstanceDeviceListWithResult(id: String): ApiResult<List<Virt.Device>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_LIST, listOf(id), deviceListType())

    suspend fun updateInstanceDeviceWithResult(id: String, device: Map<String, Any?>): ApiResult<List<Virt.Device>> =
        manager.callWithResult(ApiMethods.Virt.DEVICE_UPDATE, listOf(id, device), deviceListType())

    suspend fun setBootableDiskWithResult(id: String, disk: String): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Virt.SET_BOOTABLE_DISK, listOf(id, disk), Unit::class.java)

    // ── virt.volume ──
    private fun volumeListType() = Types.newParameterizedType(List::class.java, Virt.VolumeEntry::class.java)

    suspend fun queryVolumesWithResult(filters: List<Any> = emptyList()): ApiResult<List<Virt.VolumeEntry>> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_QUERY, listOf(filters), volumeListType())

    suspend fun createVolumeWithResult(payload: Virt.VolumeCreate): ApiResult<Virt.VolumeEntry> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_CREATE, listOf(payload), Virt.VolumeEntry::class.java)

    suspend fun getVolumeWithResult(id: String): ApiResult<Virt.VolumeEntry> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_GET_INSTANCE, listOf(id), Virt.VolumeEntry::class.java)

    suspend fun updateVolumeWithResult(id: String, update: Virt.VolumeUpdate): ApiResult<Virt.VolumeEntry> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_UPDATE, listOf(id, update), Virt.VolumeEntry::class.java)

    suspend fun deleteVolumeWithResult(id: String): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_DELETE, listOf(id), Unit::class.java)

    suspend fun importIsoWithResult(payload: Map<String, Any?>): ApiResult<Virt.VolumeEntry> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_IMPORT_ISO, listOf(payload), Virt.VolumeEntry::class.java)

    suspend fun importZvolWithResult(payload: Map<String, Any?>): ApiResult<Virt.VolumeEntry> =
        manager.callWithResult(ApiMethods.Virt.VOLUME_IMPORT_ZVOL, listOf(payload), Virt.VolumeEntry::class.java)
}