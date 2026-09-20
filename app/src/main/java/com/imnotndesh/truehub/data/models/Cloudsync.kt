package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Cloudsync {
    @JsonClass(generateAdapter = true)
    data class Entry(
        val id: Int? = null,
        val description: String? = null,
        val path: String? = null,
        val credentials: CredentialEntry? = null,
        val direction: String? = null,
        @field:Json("transfer_mode") val transferMode: String? = null,
        val enabled: Boolean? = null,
        val schedule: Map<String, Any?>? = null,
        @field:Json("pre_script") val preScript: String? = null,
        @field:Json("post_script") val postScript: String? = null,
        val snapshot: Boolean? = null,
        val bwlimit: List<Map<String, Any?>>? = null,
        val encryption: Boolean? = null,
        @field:Json("filename_encryption") val filenameEncryption: Boolean? = null
    )

    @JsonClass(generateAdapter = true)
    data class CredentialEntry(
        val id: Int? = null,
        val name: String? = null,
        val provider: Map<String, Any?>? = null
    )

    @JsonClass(generateAdapter = true)
    data class CredentialsVerifyResult(
        val valid: Boolean? = null
    )

    @JsonClass(generateAdapter = true)
    data class Provider(
        val name: String? = null,
        val title: String? = null,
        @field:Json("credentials_oauth") val credentialsOauth: Boolean? = null,
        val buckets: Boolean? = null,
        @field:Json("bucket_title") val bucketTitle: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class OnedriveDrive(
        @field:Json("drive_id") val driveId: String? = null,
        @field:Json("drive_type") val driveType: String? = null,
        val name: String? = null,
        val description: String? = null
    )
}
