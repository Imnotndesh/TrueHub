package com.example.truehub.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object System {
    @Serializable
    data class SystemInfo(
        @SerialName("version")
        val version: String,
        @SerialName( "buildtime")
        val buildTime: Long?,

        @SerialName( "hostname")
        val hostname: String,

        @SerialName( "physmem")
        val physMem: Double,

        @SerialName( "model")
        val model: String,

        @SerialName( "cores")
        val cores: Double,

        @SerialName( "physical_cores")
        val physicalCores: Int,

        @SerialName( "loadavg")
        val loadAvg: List<Double>,

        @SerialName( "uptime")
        val uptime: String,

        @SerialName( "uptime_seconds")
        val uptimeSeconds: Double,

        @SerialName( "system_serial")
        val systemSerial: String?,

        @SerialName( "system_product")
        val systemProduct: String?,

        @SerialName( "system_product_version")
        val systemProductVersion: String?,

        @SerialName( "license")
        val license: String?,

        @SerialName( "boottime")
        val bootTime: Long?,

        @SerialName( "datetime")
        val dateTime: Long?,

        @SerialName( "timezone")
        val timeZone: String,

        @SerialName( "system_manufacturer")
        val systemManufacturer: String?,

        @SerialName( "ecc_memory")
        val eccMemory: Boolean
    )

}