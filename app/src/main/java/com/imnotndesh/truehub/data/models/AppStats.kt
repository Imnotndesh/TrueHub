package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json

data class AppStats(
    @Json(name = "app_name") val appName: String,
    @Json(name = "cpu_usage") val cpuUsage: Double,
    val memory: Long,
    val networks: List<AppStatsNetwork> = emptyList(),
    val blkio: AppStatsBlkio = AppStatsBlkio()
)

data class AppStatsNetwork(
    @Json(name = "interface_name") val interfaceName: String,
    @Json(name = "rx_bytes") val rxBytes: Long,
    @Json(name = "tx_bytes") val txBytes: Long
)

data class AppStatsBlkio(
    val read: Long = 0,
    val write: Long = 0
)

data class AppStatsEvent(
    val msg: String? = null,
    val collection: String? = null,
    val fields: List<AppStats> = emptyList()
)
