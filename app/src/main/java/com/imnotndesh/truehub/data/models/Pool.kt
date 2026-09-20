package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Pool {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val id: Int? = null,
        val name: String? = null,
        val guid: String? = null,
        val status: String? = null,
        val path: String? = null,
        val healthy: Boolean? = null,
        val warning: String? = null,
        @field:Json("status_code") val statusCode: String? = null,
        @field:Json("status_detail") val statusDetail: String? = null,
        val size: Long? = null,
        val allocated: Long? = null,
        val free: Long? = null,
        val freeing: Long? = null,
        val fragmentation: String? = null,
        @field:Json("size_str") val sizeStr: String? = null,
        @field:Json("allocated_str") val allocatedStr: String? = null,
        @field:Json("free_str") val freeStr: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class DatasetEntry(
        val id: String? = null,
        val name: String? = null,
        val type: String? = null,
        val used: Long? = null,
        val available: Long? = null,
        val mountpoint: String? = null,
        val encryption: String? = null,
        val quota: Long? = null
    )

    @JsonClass(generateAdapter = true)
    data class ResilverEntry(
        val id: Int? = null,
        val pool: String? = null,
        val state: String? = null,
        val progress: Double? = null
    )

    @JsonClass(generateAdapter = true)
    data class SnapshotEntry(
        val id: String? = null,
        val name: String? = null,
        val dataset: String? = null,
        @field:Json("snapshot_name") val snapshotName: String? = null,
        val pool: String? = null,
        val type: String? = null,
        val createtxg: String? = null,
        val datetime: Map<String, Any?>? = null,
        val properties: Map<String, Any?>? = null
    )

    @JsonClass(generateAdapter = true)
    data class SnapshotTaskEntry(
        val id: Int? = null,
        val dataset: String? = null,
        val recursive: Boolean? = null,
        val enabled: Boolean? = null,
        @field:Json("naming_schema") val namingSchema: String? = null,
        val schedule: Map<String, Any?>? = null,
        @field:Json("lifetime_value") val lifetimeValue: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class ScrubEntry(
        val id: Int? = null,
        val pool: String? = null,
        val threshold: Int? = null,
        val schedule: Map<String, Any?>? = null,
        val enabled: Boolean? = null
    )

    @JsonClass(generateAdapter = true)
    data class DdtEntry(
        val id: String? = null
    )
}
