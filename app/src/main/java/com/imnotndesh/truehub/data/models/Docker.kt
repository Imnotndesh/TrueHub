package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Docker {

    @JsonClass(generateAdapter = true)
    data class AddressPool(
        val base: String? = null,
        val size: Int? = null
    )

    /** `docker.config` / `docker.update` result. */
    @JsonClass(generateAdapter = true)
    data class DockerEntry(
        val id: Int,
        @field:Json("enable_image_updates") val enableImageUpdates: Boolean = false,
        val dataset: String? = null,
        val pool: String? = null,
        val nvidia: Boolean = false,
        @field:Json("address_pools") val addressPools: List<AddressPool> = emptyList(),
        @field:Json("cidr_v6") val cidrV6: String? = null,
        @field:Json("secure_registry_mirrors") val secureRegistryMirrors: List<String> = emptyList(),
        @field:Json("insecure_registry_mirrors") val insecureRegistryMirrors: List<String> = emptyList()
    )

    /** `docker.update` payload (all fields optional). */
    @JsonClass(generateAdapter = true)
    data class Update(
        @field:Json("enable_image_updates") val enableImageUpdates: Boolean? = null,
        val dataset: String? = null,
        val pool: String? = null,
        val nvidia: Boolean? = null,
        @field:Json("address_pools") val addressPools: List<AddressPool>? = null,
        @field:Json("cidr_v6") val cidrV6: String? = null,
        @field:Json("secure_registry_mirrors") val secureRegistryMirrors: List<String>? = null,
        @field:Json("insecure_registry_mirrors") val insecureRegistryMirrors: List<String>? = null
    )

    /** `docker.status` result. */
    @JsonClass(generateAdapter = true)
    data class Status(
        val description: String,
        val status: String
    )

    /** `docker.network.get_instance` / `docker.network.query` item. */
    @JsonClass(generateAdapter = true)
    data class Network(
        val ipam: Map<String, Any?>? = null,
        val labels: Map<String, Any?>? = null,
        val created: String? = null,
        val driver: String? = null,
        val id: String? = null,
        val name: String? = null,
        val scope: String? = null,
        @field:Json("short_id") val shortId: String? = null
    )

    /** One application entry inside a docker backup. */
    @JsonClass(generateAdapter = true)
    data class BackupApp(
        val id: String,
        val name: String,
        val state: String
    )

    /** A value of the `docker.list_backups` map. */
    @JsonClass(generateAdapter = true)
    data class Backup(
        val name: String,
        val apps: List<BackupApp> = emptyList(),
        @field:Json("snapshot_name") val snapshotName: String? = null,
        @field:Json("created_on") val createdOn: String? = null,
        @field:Json("backup_path") val backupPath: String? = null
    )
}
