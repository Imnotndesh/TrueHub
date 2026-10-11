package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Vm {

    enum class CpuMode {
        @field:Json(name = "CUSTOM")
        CUSTOM,
        @field:Json(name = "HOST-MODEL")
        HOST_MODEL,
        @field:Json(name = "HOST-PASSTHROUGH")
        HOST_PASSTHROUGH
    }

    @JsonClass(generateAdapter = true)
    data class VmDevice(
        val id: Int? = null,
        val attributes: Map<String, Any?>? = null,
        val vm: Int? = null,
        val order: Int? = null
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class VmStatus(
        val state : String = "UNKNOWN",
        val pid : Int? = null,
        val domain_state : String? = null,
    )

    /**
     * @see vm.query
     */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class VmQueryResponse(
        val command_line_args: String? = null,
        val cpu_mode: CpuMode? = null,
        val cpu_model: String? = null,
        val name : String = "",
        val description: String = "",
        val vcpus : Int = 0,
        val cores : Int = 0,
        val threads : Int = 0,
        val cpuset : String? = null,
        val nodeset : String? = null,
        val enable_cpu_topology_extension : Boolean = false,
        val pin_vcpus: Boolean = false,
        val suspend_on_snapshot: Boolean = false,
        val trusted_platform_module : Boolean = false,
        val memory: Int = 0,
        val min_memory: Int? = null,
        val hyperv_enlightenments : Boolean = false,
        val bootloader : String = "UEFI",
        val bootloader_ovmf : String? = null,
        val autostart : Boolean = false,
        val hide_from_msr : Boolean = false,
        val ensure_display_device : Boolean = false,
        val time : String = "LOCAL",
        val shutdown_timeout : Int = 90,
        val arch_type : String? = null,
        val machine_type : String? = null,
        val uuid : String? = null,
        val devices: List<VmDevice>? = null,
        val display_available : Boolean = false,
        val id : Int = -1,
        val status : VmStatus = VmStatus(),
        val enable_secure_boot : Boolean = false,
    )

    @JsonClass(generateAdapter = true)
    data class VmDisplayUriQueryResponse(
        val uri : String? = null,
        val error : String? = null
    )

    @JsonClass(generateAdapter = true)
    data class StopOptions(
        val force: Boolean? = false,
        val force_after_timeout: Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    data class StartOptions(
        val overcommit: Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    data class DeleteOptions(
        val zvols : Boolean? = false,
        val force : Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    data class DeviceEntry(
        val id: Int? = null,
        val vm: Int? = null,
        val order: Int? = null,
        val dtype: String? = null,
        val attributes: Map<String, Any?>? = null
    )

    @JsonClass(generateAdapter = true)
    data class PassthroughInfo(
        val capability: String? = null,
        val product: String? = null,
        @field:Json("product_id") val productId: String? = null,
        val vendor: String? = null,
        @field:Json("vendor_id") val vendorId: String? = null,
        val available: Boolean? = null,
        val error: String? = null,
        val description: String? = null
    )

}
