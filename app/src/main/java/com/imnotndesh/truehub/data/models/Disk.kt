package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Disk {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val identifier: String? = null,
        val name: String? = null,
        val subsystem: String? = null,
        val number: Int? = null,
        val serial: String? = null,
        val lunid: String? = null,
        val size: Long? = null,
        val description: String? = null,
        @field:Json("transfermode") val transferMode: String? = null,
        @field:Json("hddstandby") val hddStandby: String? = null,
        @field:Json("advpowermgmt") val advPowerMgmt: String? = null,
        val expiretime: String? = null,
        val model: String? = null,
        val rotationrate: Int? = null,
        val type: String? = null,
        @field:Json("zfs_guid") val zfsGuid: String? = null,
        val bus: String? = null,
        val devname: String? = null,
        val enclosure: String? = null
    )
}
