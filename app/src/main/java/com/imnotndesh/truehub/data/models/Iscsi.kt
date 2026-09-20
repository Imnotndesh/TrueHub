package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Iscsi {
    @JsonClass(generateAdapter = true)
    data class AuthEntry(
        val id: Int? = null,
        val tag: Int? = null,
        val user: String? = null,
        val secret: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class ExtentEntry(
        val id: Int? = null,
        val name: String? = null,
        val naa: String? = null,
        val vendor: String? = null,
        val locked: Boolean? = null
    )

    @JsonClass(generateAdapter = true)
    data class InitiatorEntry(
        val id: Int? = null,
        val initiators: List<String>? = null,
        val comment: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class PortalEntry(
        val id: Int? = null,
        val listen: List<Map<String, Any?>>? = null,
        val ip: String? = null,
        val port: Int? = null,
        val tag: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class TargetEntry(
        val id: Int? = null,
        val name: String? = null,
        val portal: Int? = null,
        @field:Json("rel_tgt_id") val relTgtId: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class TargetExtentEntry(
        val id: Int? = null,
        val target: Int? = null,
        val lunid: Int? = null,
        val extent: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class GlobalEntry(
        val id: Int? = null,
        val basename: String? = null,
        @field:Json("isns_servers") val isnsServers: List<String>? = null,
        val alua: Boolean? = null,
        val iser: Boolean? = null
    )
}
