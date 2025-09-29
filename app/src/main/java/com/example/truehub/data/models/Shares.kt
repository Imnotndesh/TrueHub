package com.example.truehub.data.models

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
}