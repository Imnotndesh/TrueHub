package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Kerberos {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val id: Int? = null,
        @field:Json("appdefaults_aux") val appdefaultsAux: String? = null,
        @field:Json("libdefaults_aux") val libdefaultsAux: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class KeytabEntry(
        val id: Int? = null,
        val name: String? = null,
        val file: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class RealmEntry(
        val id: Int? = null,
        val realm: String? = null
    )
}
