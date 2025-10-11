package com.example.truehub.data.models

import com.squareup.moshi.JsonClass

object Shares {
    @Suppress("PropertyName")
    data class SmbShare(
        val id: Int,
        val purpose: String,
        val path: String,
        val path_suffix: String,
        val home: Boolean,
        val name: String,
        val comment: String,
        val ro: Boolean,
        val browsable: Boolean,
        val recyclebin: Boolean,
        val guestok: Boolean,
        val hostsallow: List<String>,
        val hostsdeny: List<String>,
        val auxsmbconf: String,
        val aapl_name_mangling: Boolean,
        val abe: Boolean,
        val acl: Boolean,
        val durablehandle: Boolean,
        val streams: Boolean,
        val timemachine: Boolean,
        val timemachine_quota: Int,
        val vuid: String,
        val shadowcopy: Boolean,
        val fsrvp: Boolean,
        val enabled: Boolean,
        val afp: Boolean,
        val audit: Audit,
        val path_local: String,
        val locked: Boolean
    ) {
        @Suppress("PropertyName")
        data class Audit(
            val enable: Boolean,
            val watch_list: List<String>,
            val ignore_list: List<String>
        )
    }
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class NfsShare(
        val id: Int,
        val path: String,
        val aliases: List<String> = emptyList(),
        val comment: String = "",
        val networks: List<String> = emptyList(),
        val hosts: List<String> = emptyList(),
        val ro: Boolean = false,
        val maproot_user: String ?= "",
        val maproot_group: String ?= "",
        val mapall_user: String ?= "",
        val mapall_group: String ?= "",
        val security: List<String> = emptyList(),
        val enabled: Boolean = false,
        val locked: Boolean = false,
        val expose_snapshots: Boolean = false
    )
//    @JsonClass(generateAdapter = true)
//    enum class NfsSecurity{
//        SYS,
//        KRB5,
//        KRB5I,
//        KRB5P
//    }


}