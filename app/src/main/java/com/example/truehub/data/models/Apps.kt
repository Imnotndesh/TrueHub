package com.example.truehub.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Apps {

    @Serializable
    data class AppQueryResponse(
        val name: String,
        val id: String,
        val state: String,
        @SerialName("upgrade_available") val upgradeAvailable: Boolean = false,
        @SerialName("latest_version") val latestVersion: String? = null,
        @SerialName("image_updates_available") val imageUpdatesAvailable: Boolean = false,
        @SerialName("custom_app") val customApp: Boolean = false,
        val migrated: Boolean = false,
        @SerialName("human_version") val humanVersion: String? = null,
        val version: String? = null,
        val metadata: Metadata? = null,
        @SerialName("active_workloads") val activeWorkloads: ActiveWorkloads? = null,
        val notes: String? = null,
        val portals: Map<String, String>? = null
    )

    @Serializable
    data class Metadata(
        @SerialName("app_version") val appVersion: String? = null,
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
        @SerialName("date_added") val dateAdded: String? = null,
        @SerialName("last_update") val lastUpdate: String? = null,
        @SerialName("changelog_url") val changelogUrl: String? = null,
        @SerialName("lib_version") val libVersion: String? = null,
        @SerialName("lib_version_hash") val libVersionHash: String? = null,
        @SerialName("run_as_context") val runAsContext: List<RunAsContext>? = null,
        @SerialName("host_mounts") val hostMounts: List<HostMount>? = null
    )

    @Serializable
    data class Capability(
        val name: String,
        val description: String
    )

    @Serializable
    data class Maintainer(
        val name: String,
        val email: String? = null,
        val url: String? = null
    )

    @Serializable
    data class RunAsContext(
        val description: String? = null,
        val uid: Int? = null,
        val gid: Int? = null,
        @SerialName("user_name") val userName: String? = null,
        @SerialName("group_name") val groupName: String? = null
    )

    @Serializable
    data class HostMount(
        @SerialName("host_path") val hostPath: String? = null,
        val description: String? = null
    )

    @Serializable
    data class ActiveWorkloads(
        val containers: Int = 0,
        @SerialName("used_ports") val usedPorts: List<UsedPort>? = null,
        @SerialName("container_details") val containerDetails: List<ContainerDetail>? = null,
        val volumes: List<Volume>? = null,
        val images: List<String>? = null,
        val networks: List<Network>? = null
    )

    @Serializable
    data class UsedPort(
        @SerialName("container_port") val containerPort: Int,
        val protocol: String,
        @SerialName("host_ports") val hostPorts: List<HostPort>
    )

    @Serializable
    data class HostPort(
        @SerialName("host_port") val hostPort: Int,
        @SerialName("host_ip") val hostIp: String
    )

    @Serializable
    data class ContainerDetail(
        val id: String,
        @SerialName("service_name") val serviceName: String,
        val image: String,
        @SerialName("port_config") val portConfig: List<UsedPort>? = null,
        val state: String,
        @SerialName("volume_mounts") val volumeMounts: List<Volume>? = null
    )

    @Serializable
    data class Volume(
        val source: String,
        val destination: String,
        val mode: String? = null,
        val type: String? = null
    )

    @Serializable
    data class Network(
        @SerialName("Name") val name: String,
        @SerialName("Id") val id: String,
        @SerialName("Labels") val labels: Map<String, String>? = null,
        @SerialName("Created") val created: String? = null,
        @SerialName("Scope") val scope: String? = null,
        @SerialName("Driver") val driver: String? = null,
        @SerialName("EnableIPv6") val enableIPv6: Boolean? = null,
        @SerialName("IPAM") val ipam: IPAM? = null
    )

    @Serializable
    data class IPAM(
        val driver: String? = null,
        val options: Map<String, String>? = null,
        val config: List<IPAMConfig>? = null
    )

    @Serializable
    data class IPAMConfig(
        val subnet: String? = null,
        val gateway: String? = null
    )
}