package com.example.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


object System {
    @Suppress("PropertyName")
    data class SystemInfo(
        @field:Json(name = "version")
        val version: String,

        // buildTime from JSON is like {"$date": 1753934385000}
        // Easiest is to map to Map<String, Long> or a specific data class
        @field:Json("buildtime")    val buildTime: Map<String, Long>?, // Or define a DateWrapper class: data class DateWrapper(@Json("\$date") val date: Long)

        @field:Json("hostname")
        val hostname: String,

        @field:Json("physmem") // JSON has 7806451712 (integer) in one log, 7.806451712E9 in another. Long is safer.
        val physMem: Long = 1, // Default is fine for testing

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
        val progress: JobProgress?,
        val result: Any?,
        val error: Any?,
        val exception: String?,
        val exc_info: String?,
        val state: String,
        val time_started: Map<String, Long>?,
        val time_finished: Any?
    )

    @JsonClass(generateAdapter = true)
    data class JobProgress(
        val percent: Int,
        val description: String?,
        val extra: Any?
    )
    data class UpgradeJobState(
        val state: String,
        val progress: Int = 0,
        val description: String? = null
    )
}