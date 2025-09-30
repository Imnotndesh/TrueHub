package com.example.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


object System {
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class SystemInfo(
        @field:Json(name = "version")
        val version: String,
        @field:Json("buildtime")
        val buildTime: Map<String, Long>?,

        @field:Json("hostname")
        val hostname: String,
        @field:Json("physmem")
        val physmem: Long = 1,

        @field:Json("model")
        val model: String,

        @field:Json("cores")
        val cores: Double,

        @field:Json("physical_cores")
        val physical_cores: Int? = null,

        @field:Json("loadavg")
        val loadavg: List<Double>,

        @field:Json("uptime")
        val uptime: String,

        @field:Json("system_serial")
        val systemSerial: String?,

        @field:Json("system_product")
        val systemProduct: String?,

        @field:Json("system_product_version")
        val systemProductVersion: String?,

        @field:Json("license")
        val license: String?,

        @field:Json("boottime")
        val bootTime: Map<String, Long>?,

        @field:Json("datetime")
        val dateTime: Map<String, Long>?,

        @field:Json("timezone")
        val timezone: String,

        @field:Json("system_manufacturer")
        val system_manufacturer: String?,

        @field:Json("ecc_memory")
        val ecc_memory: Boolean
    )

    // Jobs Stuff
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class Job(
        val id: Int,
        val method: String,
        val arguments: List<Any>,
        val logs_path: String?,
        val logs_excerpt: String?,
        val result_encoding_error: String?,
        val progress: JobProgress?,
        val result: Any?,
        val error: Any?,
        val exception: String?,
        val exc_info: ExcInfo?,
        val state: String,
        val time_started: Map<String, Long>?,
        val time_finished: Any?,
        val credentials: Credentials?
    )

    @JsonClass(generateAdapter = true)
    data class JobProgress(
        val percent: Int,
        val description: String?,
        val extra: Any?
    )
    @JsonClass(generateAdapter = true)
    data class Credentials(
        val type: String?,
        val data: CredentialsData?
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class CredentialsData(
        val username: String?,
        val login_at: Map<String, Long>?
    )
    @JsonClass(generateAdapter = true)
    data class ExcInfo(
        val repr: String?,
        val type: String?,
        val errno: Double?,
        val extra: Any?
    )
    data class UpgradeJobState(
        val state: String,
        val progress: Int = 0,
        val description: String? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class GraphResult(
        val name :String,
        val title :String,
        val vertical_label : String,
        val identifiers : List<String>?,
    )
    // Graph request
    @JsonClass(generateAdapter = true)
    data class ReportingGraphQuery(
        val unit: ReportingUnit? = null,
        val page: Int? = 1,
        val aggregate: Boolean? = true,
        val start: Long? = null,
        val end: Long? = null
    )
    @JsonClass(generateAdapter = true)
    data class ReportingGraphRequest(
        val name: String,
        val identifier: String? = null
    ) {
        constructor(name: ReportingGraphName, identifier: String? = null) :
                this(name.value, identifier)
    }

    // Graph response
    @JsonClass(generateAdapter = true)
    data class ReportingGraphResponse(
        val name: String,
        val identifier: String?,
        val aggregations: Map<String, Map<String, Double>>?,
        val data: List<List<Double>>,
        val start: Int,
        val end: Int,
        val legend: List<String>
    )

    enum class ReportingGraphName(val value: String) {
        CPU("cpu"),
        CPUTEMP("cputemp"),
        DISK("disk"),
        INTERFACE("interface"),
        LOAD("load"),
        PROCESSES("processes"),
        MEMORY("memory"),
        UPTIME("uptime"),
        ARCACTUALRATE("arcactualrate"),
        ARCRATE("arcrate"),
        ARCSIZE("arcsize"),
        ARCRESULT("arcresult"),
        DISKTEMP("disktemp"),
        UPSCHARGE("upscharge"),
        UPSRUNTIME("upsruntime"),
        UPSVOLTAGE("upsvoltage"),
        UPSCURRENT("upscurrent"),
        UPSFREQUENCY("upsfrequency"),
        UPSLOAD("upsload"),
        UPSTEMPERATURE("upstemperature");
    }
    enum class ReportingUnit(val value: String) {
        HOUR("HOUR"),
        DAY("DAY"),
        WEEK("WEEK"),
        MONTH("MONTH"),
        YEAR("YEAR");
    }


    // Device stuff
    enum class DeviceType {
        DISK,
        SERIAL,
        GPU
    }
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class DeviceInfoQuery(
        val type: DeviceType? = null,
        val get_partitions: Boolean? = null,
        val serials_only: Boolean? = null
    )

    // Disk Details
    @Suppress("PropertyName")
    data class DiskDetails(
        val identifier: String,
        val name: String,
        val subsystem: String,
        val number: Int,
        val serial: String,
        val lunid: String?,
        val size: Long,
        val description: String,
        val transfermode: String,
        val hddstandby: String,
        val advpowermgmt: String,
        val togglesmart: Boolean,
        val smartoptions: String,
        val expiretime: String?,
        val critical: String?,
        val difference: String?,
        val informational: String?,
        val model: String,
        val rotationrate: Int?,
        val type: String,
        val zfs_guid: String?,
        val bus: String,
        val devname: String,
        val enclosure: String?,
        val supports_smart: Boolean?,
        val pool: String?
    )

    // Pool Info
    @Suppress("PropertyName")
    data class Pool(
        val id: Int,
        val name: String,
        val guid: String,
        val path: String,
        val status: String,
        val scan: PoolScan?,
        val expand: String?,
        val topology: PoolTopology,
        val healthy: Boolean,
        val warning: Boolean,
        val status_code: String,
        val status_detail: String,
        val size: Long,
        val allocated: Long,
        val free: Long,
        val freeing: Long,
        val fragmentation: String,
        val size_str: String,
        val allocated_str: String,
        val free_str: String,
        val freeing_str: String,
        val dedup_table_quota: String,
        val dedup_table_size: Long,
        val autotrim: AutoTrim
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolScan(
        val function: String,
        val state: String,
        val start_time: Map<String, Long>?,
        val end_time:Map<String, Long>?,
        val percentage: Double,
        val bytes_to_process: Long,
        val bytes_processed: Long,
        val bytes_issued: Long,
        val pause: String?,
        val errors: Int,
        val total_secs_left: Long?
    )

    data class PoolTopology(
        val data: List<PoolDevice>,
        val log: List<PoolDevice>,
        val cache: List<PoolDevice>,
        val spare: List<PoolDevice>,
        val special: List<PoolDevice>,
        val dedup: List<PoolDevice>
    )
    @Suppress("PropertyName")
    data class PoolDevice(
        val name: String,
        val type: String,
        val path: String,
        val guid: String,
        val status: String,
        val stats: PoolStats?,
        val children: List<PoolDevice>,
        val device: String?,
        val disk: String?,
        val unavail_disk: String?
    )
    @Suppress("PropertyName")
    data class PoolStats(
        val timestamp: Long,
        val read_errors: Int,
        val write_errors: Int,
        val checksum_errors: Int,
        val ops: List<Long>,
        val bytes: List<Long>,
        val size: Long,
        val allocated: Long,
        val fragmentation: Int,
        val self_healed: Int,
        val configured_ashift: Int,
        val logical_ashift: Int,
        val physical_ashift: Int
    )

    data class AutoTrim(
        val value: String,
        val rawvalue: String,
        val parsed: String,
        val source: String
    )

}