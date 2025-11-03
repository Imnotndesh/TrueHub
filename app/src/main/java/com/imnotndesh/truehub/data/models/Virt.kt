package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Virt {
    enum class Type {
        @field:Json(name = "CONTAINER")
        CONTAINER,
        @field:Json(name = "VM")
        VM
    }
    enum class Status{
        @field:Json(name = "RUNNING")
        RUNNING,
        @field:Json(name = "STOPPED")
        STOPPED,
        @field:Json(name = "STARTING")
        STARTING,
        @field:Json(name = "STOPPING")
        STOPPING,
        @field:Json(name = "UNKNOWN")
        UNKNOWN,
        @field:Json(name = "ERROR")
        ERROR,
        @field:Json(name = "FROZEN")
        FROZEN,
        @field:Json(name = "FREEZING")
        FREEZING,
        @field:Json(name = "THAWED")
        THAWED,
        @field:Json(name = "ABORTING")
        ABORTING
    }
    enum class AliasType{
        @field:Json(name = "INET")
        INET,
        @field:Json(name = "INET6")
        INET6
    }
    enum class RootDiskIOBus(string: String) {
        @field:Json(name = "NVME")
        NVME("NVME"),
        @field:Json(name = "VIRTIO-BLK")
        VIRTIO_BLK("VIRTIO_BLK"),
        @field:Json(name = "VIRTIO-SCSI")
        VIRTIO_SCSI("VIRTIO-SCSI")
    }
    data class Aliases(
        val type : AliasType,
        val address : String,
        val netmask: Int? = null
    )
    data class Image(
        val architecture: String? = null,
        val description: String? = null,
        val os: String? = null,
        val release :String? = null,
        val serial :String? = null,
        val type : String? = null,
        val variant: String? = null,
        val secureboot : Boolean? = false
    )
    data class Uid(
        val hostid : Int,
        val maprange : Int,
        val nsid : Int,
    )
    data class Gid(
        val hostid : Int,
        val maprange : Int,
        val nsid : Int,
    )
    data class UsernsIdmap(
        val uid: Uid,
        val gid: Gid
    )
    // virt.instance.query
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ContainerResponse(
        val id: String,
        val name: String,
        val type: Type,
        val status : Status,
        val cpu: String? = null,
        val memory: Int? = null,
        val autostart : Boolean,
        val environment: Map<String,String>,
        val aliases : List<Aliases>,
        val image : Image,
        val userns_idmap: UsernsIdmap? = null,
        val raw : Map<Any,Any>? = null,
        val vnc_enabled : Boolean,
        val vnc_port : Int? = null,
        val vnc_password : String? = null,
        val secure_boot : Boolean? = null,
        val root_disk_size : Int? = null,
        val root_disk_io_bus: RootDiskIOBus? = null,
        val storage_pool : String? = null,
    )
    // virt.instance.update
    @Suppress("PropertyName")
    data class ContainerUpdate(
        val environment: Map<String,String>? = null,
        val autostart : Boolean? = null,
        val cpu: String? = null,
        val memory: Int? = null,
        val vnc_port : Int? = null, // remember to set UI to limit this to between  greater or equal to 5900 and lesser or equal to 65535
        val vnc_enabled : Boolean,
        val vnc_password : String? = null,
        val secure_boot: Boolean,
        val root_disk_size: Int? = null, // set this to greater or equal to 5
        val root_disk_io_bus: RootDiskIOBus? = null,
    )
    enum class InstanceType(string: String){
        @field:Json(name = "CONTAINER")
        CONTAINER("CONTAINER"),
        @field:Json(name = "VM")
        VM("VM")
    }

    // Image repo Query
    // Provide choices for instance image from a remote repository.
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ImageQueryResponse(
        val label : String,
        val os : String,
        val release: String,
        val archs : List<String>,
        val variant: String,
        val instance_types: List<InstanceType>,
        val secureboot: Boolean?
    )

    // Device responses
    @Suppress("PropertyName")
    data class DiskDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val source: String? = null,
        val destination : String? = null,
        val boot_priority : Int? = null,
        val io_bus: RootDiskIOBus? = null,
        val storage_pool: String? = null
    ): Device
    @Suppress("PropertyName")
    data class GpuDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val gpu_type: String,
        val id : String? = null,
        val gid : Int? = null,
        val uid: Int? = null,
        val mode : String? = null,
        val mdev : String? = null,
        val mig_uuid: String? = null,
        val pci : String? = null,
        val productid: String? = null,
        val vendorid: String? = null,
    ): Device
    @Suppress("PropertyName")
    data class ProxyDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val source_proto: String,
        val source_port: Int,
        val dest_proto: String,
        val dest_port: Int,
    ): Device
    @Suppress("PropertyName")
    data class TPMDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val path: String,
        val pathrm : String? = null
    ): Device
    @Suppress("PropertyName")
    data class USBDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val bus: Int? = null,
        val dev: Int? = null,
        val product_id :String? = null,
        val vendor_id : String? = null,
    ): Device
    @Suppress("PropertyName")
    data class NICDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val network: String? = null,
        val nic_type: String? = null,
        val parent :String? = null,
    ): Device
    @Suppress("PropertyName")
    data class PCIDevice(
        val name: String? = null,
        val description: String? = null,
        val readonly : Boolean? = false,
        val dev_type: String,
        val address: String,
    ): Device
    sealed interface Device
    @JsonClass(generateAdapter = true)
    data class stopArgs(
        @field:Json("timeout")
        val timeout: Int? = -1,
        @field:Json("force")
        val force: Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    /**
     * Maps to call :
     * @see com.imnotndesh.truehub.data.api.ApiMethods.Virt.GET_IMAGE_CHOICES
     */
    data class ImageChoice(
        val label: String,
        val os :String,
        val release: String,
        val archs : List<String>,
        val variant: String,
        val instance_types: List<String>,
        val secureboot: Boolean? = null
    )
}