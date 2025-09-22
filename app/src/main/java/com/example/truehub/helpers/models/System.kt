package com.example.truehub.helpers.models

import com.squareup.moshi.Json

object System {
    data class SystemInfo(
        @field:Json(name = "version")
        val version: String,

        @field:Json(name = "buildtime")
        val buildTime: Long?,

        @field:Json(name = "hostname")
        val hostname: String,

        @field:Json(name = "physmem")
        val physMem: Double,

        @field:Json(name = "model")
        val model: String,

        @field:Json(name = "cores")
        val cores: Double,

        @field:Json(name = "physical_cores")
        val physicalCores: Int,

        @field:Json(name = "loadavg")
        val loadAvg: List<Double>,

        @field:Json(name = "uptime")
        val uptime: String,

        @field:Json(name = "uptime_seconds")
        val uptimeSeconds: Double,

        @field:Json(name = "system_serial")
        val systemSerial: String?,

        @field:Json(name = "system_product")
        val systemProduct: String?,

        @field:Json(name = "system_product_version")
        val systemProductVersion: String?,

        @field:Json(name = "license")
        val license: String?,

        @field:Json(name = "boottime")
        val bootTime: Long?,

        @field:Json(name = "datetime")
        val dateTime: Long?,

        @field:Json(name = "timezone")
        val timeZone: String,

        @field:Json(name = "system_manufacturer")
        val systemManufacturer: String?,

        @field:Json(name = "ecc_memory")
        val eccMemory: Boolean
    )

}