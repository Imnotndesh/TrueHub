package com.example.truehub.data.models
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Apps {
    @Suppress("PropertyName")
    data class AppQueryResponse(
        val name: String,
        val id: String,
        val state: String,
        @field:Json("upgrade_available") val upgrade_available: Boolean = false,
        @field:Json("latest_version") val latestVersion: String? = null,
        @field:Json("image_updates_available") val image_Updates_available: Boolean = false,
        @field:Json("custom_app") val customApp: Boolean = false,
        val migrated: Boolean = false,
        @field:Json("human_version") val humanVersion: String? = null,
        val version: String? = null,
        val metadata: Metadata? = null,
        @field:Json("active_workloads") val activeWorkloads: ActiveWorkloads? = null,
        val notes: String? = null,
        val portals: Map<String, String>? = null
    )

    data class Metadata(
        @field:Json("app_version") val appVersion: String? = null,
        val categories: List<String>? = null,
        val description: String? = null,
        val home: String? = null,
        val icon: String? = null,
        val keywords: List<String>? = null,
        val title: String? = null,
        val train: String? = null,
        val screenshots: List<String>? = null,
        val sources: List<String>? = null,
        val maintainers: List<Maintainer>? = null,
        val capabilities: List<Capability>? = null,
        @field:Json("date_added") val dateAdded: String? = null,
        @field:Json("last_update") val lastUpdate: String? = null,
        @field:Json("changelog_url") val changelogUrl: String? = null,
        @field:Json("lib_version") val libVersion: String? = null,
        @field:Json("lib_version_hash") val libVersionHash: String? = null,
        @field:Json("run_as_context") val runAsContext: List<RunAsContext>? = null,
        @field:Json("host_mounts") val hostMounts: List<HostMount>? = null
    )

    data class Capability(
        val name: String,
        val description: String
    )

    data class Maintainer(
        val name: String,
        val email: String? = null,
        val url: String? = null
    )

    data class RunAsContext(
        val description: String? = null,
        val uid: Int? = null,
        val gid: Int? = null,
        @field:Json("user_name") val userName: String? = null,
        @field:Json("group_name") val groupName: String? = null
    )

    data class HostMount(
        @field:Json("host_path") val hostPath: String? = null,
        val description: String? = null
    )

    data class ActiveWorkloads(
        val containers: Int = 0,
        @field:Json("used_ports") val usedPorts: List<UsedPort>? = null,
        @field:Json("container_details") val containerDetails: List<ContainerDetail>? = null,
        val volumes: List<Volume>? = null,
        val images: List<String>? = null,
        val networks: List<Network>? = null
    )

    data class UsedPort(
        @field:Json("container_port") val containerPort: Int,
        val protocol: String,
        @field:Json("host_ports") val hostPorts: List<HostPort>
    )

    data class HostPort(
        @field:Json("host_port") val hostPort: Int,
        @field:Json("host_ip") val hostIp: String
    )

    data class ContainerDetail(
        val id: String,
        @field:Json("service_name") val serviceName: String,
        val image: String,
        @field:Json("port_config") val portConfig: List<UsedPort>? = null,
        val state: String,
        @field:Json("volume_mounts") val volumeMounts: List<Volume>? = null
    )

    data class Volume(
        val source: String,
        val destination: String,
        val mode: String? = null,
        val type: String? = null
    )

    data class Network(
        @field:Json("Name") val name: String,
        @field:Json("Id") val id: String,
        @field:Json("Labels") val labels: Map<String, String>? = null,
        @field:Json("Created") val created: String? = null,
        @field:Json("Scope") val scope: String? = null,
        @field:Json("Driver") val driver: String? = null,
        @field:Json("EnableIPv6") val enableIPv6: Boolean? = null,
        @field:Json("IPAM") val ipam: IPAM? = null
    )

    data class IPAM(
        val driver: String? = null,
        val options: Map<String, String>? = null,
        val config: List<IPAMConfig>? = null
    )

    data class IPAMConfig(
        val subnet: String? = null,
        val gateway: String? = null
    )

    // Update options
    @Suppress("PropertyName")
    data class UpgradeOptions(
        val app_version : String = "latest",
        val values: Map<String,Any> = emptyMap(),
        val snapshot_hostpaths : Boolean = false
    )


    // Upgrade Summary
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AppUpgradeSummaryResult(
        val latest_version : String,
        val latest_human_version : String,
        val upgrade_version : String,
        val upgrade_human_version : String,
        val available_versions_for_upgrade : List<AvailableVersionForUpgrade>,
        val changelog: String?= null
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AvailableVersionForUpgrade(
        val version : String,
        val human_version : String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AppUpgradeRequest(
        val app_version: String? = "latest"
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class RollbackOptions(
        val app_version: String? = "latest",
        val rollback_snapshot: Boolean = true
    )
}