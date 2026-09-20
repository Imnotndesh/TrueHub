package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Interface {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val id: String? = null,
        val name: String? = null,
        val fake: Boolean? = null,
        val type: String? = null,
        val state: Map<String, Any?>? = null,
        @field:Json("orig_name") val origName: String? = null,
        val description: String? = null,
        val mtu: Int? = null,
        val cloned: Boolean? = null,
        @field:Json("link_state") val linkState: String? = null,
        @field:Json("active_media_subtype") val activeMediaSubtype: String? = null,
        @field:Json("link_address") val linkAddress: String? = null,
        @field:Json("permanent_link_address") val permanentLinkAddress: String? = null
    )
}
