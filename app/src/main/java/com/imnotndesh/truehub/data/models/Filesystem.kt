package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Filesystem {
    @JsonClass(generateAdapter = true)
    data class AclTemplateEntry(
        val id: Int? = null,
        val builtin: Boolean? = null,
        val name: String? = null,
        val acltype: String? = null,
        val acl: List<Map<String, Any?>>? = null
    )

    @JsonClass(generateAdapter = true)
    data class ZfsFileAttrs(
        val properties: Map<String, Any?>? = null
    )
}
