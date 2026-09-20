package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Reporting {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val id: Int? = null,
        @field:Json("tier0_days") val tier0Days: Int? = null,
        @field:Json("tier1_days") val tier1Days: Int? = null,
        @field:Json("tier1_update_interval") val tier1UpdateInterval: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class ExporterEntry(
        val id: Int? = null,
        val enabled: Boolean? = null,
        val attributes: Map<String, Any?>? = null,
        @field:Json("exporter_type") val exporterType: String? = null,
        @field:Json("destination_ip") val destinationIp: String? = null,
        @field:Json("destination_port") val destinationPort: Int? = null,
        val namespace: String? = null,
        val name: String? = null
    )
}
