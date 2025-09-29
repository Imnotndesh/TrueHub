package com.example.truehub.data.models

import com.example.truehub.data.api.ApiMethods
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Vm {

    @JsonClass(generateAdapter = true)
    enum class CpuMode{
        @field:Json(name = "CUSTOM")
        CUSTOM,
        @field:Json(name = "HOST-MODEL")
        HOST_MODEL,
        @field:Json(name = "HOST-PASSTHROUGH")
        HOST_PASSTHROUGH
    }
    @JsonClass(generateAdapter = true)

    data class VmDevice(
        val id: Int,
        val attributes: Map<String,String>,
        val vm :Int,
        val order : Int
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class VmStatus(
        val state : String,
        val pid : Int? = null,
        val domain_state : String,
    )

    /**
     * @see vm.query
     */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class VmQueryResponse(
        val command_line_args: String,
        val cpu_mode: CpuMode,
        val cpu_model: String? = null,
        val name :String,
        val description: String,
        val vcpus : Int,
        val cores : Int,
        val threads :Int,
        val cpuset :String? = null,
        val nodeset : String? = null,
        val enable_cpu_topology_extension : Boolean,
        val pin_cpus: Boolean,
        val suspend_on_snapshot: Boolean,
        val trusted_platform_module :Boolean,
        val memory: Int,
        val min_memory: Int? = null,
        val hyperv_enlightenments : Boolean,
        val bootloader : String,
        val bootloader_ovmf : String,
        val autostart : Boolean,
        val hide_from_msr : Boolean,
        val ensure_display_device : Boolean,
        val time : String,
        val shutdown_timeout :Int,
        val arch_type : String?= null,
        val machine_type : String ?= null,
        val uuid : String? = null,
        val devices: VmDevice? = null,
        val display_available : Boolean,
        val id : Int,
        val status : VmStatus,
        val enable_secure_boot : Boolean,
    )

    @JsonClass(generateAdapter = true)
    data class VmDisplayUriQueryResponse(
        val uri : String? = null,
        val error : String? = null
    )
}