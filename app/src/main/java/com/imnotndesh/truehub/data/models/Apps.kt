package com.imnotndesh.truehub.data.models
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Apps {
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
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
        val portals: Map<String, String>? = null,
        @field:Json("migrated_from_kubernetes") val migratedFromKubernetes: Boolean = false,
        @field:Json("version_info") val versionInfo: VersionInfo? = null,
        @field:Json("config") val config: Map<String, Any?>? = null
    )
    @JsonClass(generateAdapter = true)
    data class VersionInfo(
        val changelog: String? = null,
        @field:Json("upgrade_notes") val upgradeNotes: String? = null
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
    @JsonClass(generateAdapter = true)
    data class TruenasDate(
        @Json(name = "\$date") val `$date`: Long? = null
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AppAvailableItem(
        val app_readme : String ?= null,
        val categories: List<String>?,
        val description: String,
        val healthy: Boolean?,
        val healthy_error: String? = null,
        val home : String,
        val location : String,
        val latest_version: String ?= null,
        val latest_app_version : String ?= null,
        val latest_human_version : String ?= null,
        val last_update : TruenasDate ?= null,
        val name : String,
        val recommended: Boolean,
        val title: String,
        val maintainers: List<Maintainer>,
        val tags : List<String> ?= emptyList(),
        val screenshots: List<String>?,
        val sources: List<String>?,
        val icon_url : String ?=null,
        val catalog: String?,
        val installed: Boolean,
        val train: String,
    ){
        val lastUpdateString: String
            get() = last_update?.`$date`?.toString() ?: "Unknown"
        val tagsString: String
            get() = tags?.joinToString(", ") ?: ""
    }

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
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class DeleteAppOptions(
        val remove_images : Boolean ? = true,
        val remove_ix_volumes : Boolean ?= false,
        val force_remove_ix_volumes :Boolean ?= false,
        val force_remove_custom_app : Boolean ?= false
    )

    @JsonClass(generateAdapter = true)
    data class AppQueryExtra(
        @field:Json(name = "host_ip") val hostIp: String? = null,
        @field:Json(name = "include_app_schema") val includeAppSchema: Boolean = false,
        @field:Json(name = "retrieve_config") val retrieveConfig: Boolean = false
    )
    @JsonClass(generateAdapter = true)
    data class CertificateChoiceResponse(
        val id : Int,
        val name : String
    )

    @JsonClass(generateAdapter = true)
    data class AppQueryOptions(
        @field:Json(name = "extra") val extra: AppQueryExtra = AppQueryExtra(),
        @field:Json(name = "order_by") val orderBy: List<String> = emptyList(),
        @field:Json(name = "select") val select: List<String> = emptyList(),
        @field:Json(name = "count") val count: Boolean = false,
        @field:Json(name = "get") val get: Boolean = false,
        @field:Json(name = "offset") val offset: Int = 0,
        @field:Json(name = "limit") val limit: Int = 0,
        @field:Json(name = "force_sql_filters") val forceSqlFilters: Boolean = false
    )
    /**
     * Response item for app.similar
     */
    @JsonClass(generateAdapter = true)
    data class AppSimilarResponse(
        @field:Json(name = "app_readme") val appReadme: String? = null,
        val categories: List<String>? = null,
        val description: String? = null,
        val healthy: Boolean? = null,
        @field:Json(name = "healthy_error") val healthyError: String? = null,
        val home: String? = null,
        val location: String? = null,
        @field:Json(name = "latest_version") val latestVersion: String? = null,
        @field:Json(name = "latest_app_version") val latestAppVersion: String? = null,
        @field:Json(name = "human_version") val humanVersion: String? = null,
        @field:Json(name = "last_update") val lastUpdate: String? = null,
        val name: String,
        val recommended: Boolean = false,
        val title: String? = null,
        val maintainers: List<Maintainer>? = null,
        val tags: List<String>? = null,
        val screenshots: List<String>? = null,
        val sources: List<String>? = null,
        @field:Json(name = "icon_url") val iconUrl: String? = null,
        val catalog: String? = null,
        val installed: Boolean = false,
        val train: String? = null,
        val popularity: Int? = null,
        @field:Json(ignore = true) val additionalProperties: Map<String, Any?> = emptyMap()
    )
    @JsonClass(generateAdapter = true)
    data class CatalogAppDetails(
        @field:Json(name = "app_readme") val appReadme: String? = null,
        val categories: List<String>? = null,
        val description: String? = null,
        val healthy: Boolean? = null,
        @field:Json(name = "healthy_error") val healthyError: String? = null,
        val home: String? = null,
        val location: String? = null,
        @field:Json(name = "latest_version") val latestVersion: String? = null,
        @field:Json(name = "latest_app_version") val latestAppVersion: String? = null,
        @field:Json(name = "latest_human_version") val latestHumanVersion: String? = null,
        @field:Json(name = "last_update") val lastUpdate: TruenasDate? = null,
        val name: String,
        val recommended: Boolean = false,
        val title: String? = null,
        val maintainers: List<Maintainer>? = null,
        val tags: List<String>? = null,
        val screenshots: List<String>? = null,
        val sources: List<String>? = null,
        @field:Json(name = "icon_url") val iconUrl: String? = null,
        val capabilities: List<Capability>? = null,
        @field:Json(name = "run_as_context") val runAsContext: List<RunAsContext>? = null,
        val versions: Map<String, CatalogAppVersion>? = null
    )

    @JsonClass(generateAdapter = true)
    data class CatalogAppVersion(
        val healthy: Boolean? = null,
        val supported: Boolean? = null,
        @field:Json(name = "healthy_error") val healthyError: String? = null,
        val location: String? = null,
        @field:Json(name = "last_update") val lastUpdate: String? = null,
        @field:Json(name = "human_version") val humanVersion: String? = null,
        val version: String? = null,
        @field:Json(name = "app_metadata") val appMetadata: CatalogAppMetadata? = null,
        val schema: CatalogAppSchema? = null,
        val readme: String? = null,
        val changelog: String? = null,
        val values: Map<String, Any?>? = null
    )

    @JsonClass(generateAdapter = true)
    data class CatalogAppMetadata(
        @field:Json(name = "app_version") val appVersion: String? = null,
        val categories: List<String>? = null,
        val description: String? = null,
        val home: String? = null,
        val icon: String? = null,
        val keywords: List<String>? = null,
        val title: String? = null,
        val train: String? = null,
        val name: String? = null,
        val version: String? = null,
        val maintainers: List<Maintainer>? = null,
        val capabilities: List<Capability>? = null,
        @field:Json(name = "run_as_context") val runAsContext: List<RunAsContext>? = null,
        @field:Json(name = "date_added") val dateAdded: String? = null,
        @field:Json(name = "changelog_url") val changelogUrl: String? = null,
        @field:Json(name = "lib_version") val libVersion: String? = null,
        @field:Json(name = "lib_version_hash") val libVersionHash: String? = null,
        @field:Json(name = "host_mounts") val hostMounts: List<HostMount>? = null,
        val screenshots: List<String>? = null,
        val sources: List<String>? = null
    )

    @JsonClass(generateAdapter = true)
    data class CatalogAppSchema(
        val groups: List<SchemaGroup>? = null,
        val questions: List<SchemaQuestion>? = null
    )

    @JsonClass(generateAdapter = true)
    data class SchemaGroup(
        val name: String,
        val description: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class SchemaQuestion(
        val variable: String,
        val label: String? = null,
        val group: String? = null,
        val description: String? = null,
        val schema: SchemaDefinition? = null
    )

    @JsonClass(generateAdapter = true)
    data class SchemaDefinition(
        val type: String? = null,
        val default: Any? = null,
        val required: Boolean? = null,
        val hidden: Boolean? = null,
        val `null`: Boolean? = null,
        val min: Int? = null,
        val max: Int? = null,
        val private: Boolean? = null,
        @field:Json(name = "show_if") val showIf: List<List<Any?>>? = null,
        @field:Json(name = "\$ref") val ref: List<String>? = null,
        val enum: List<SchemaEnum>? = null,
        val attrs: List<SchemaQuestion>? = null,
        val items: List<SchemaQuestion>? = null
    )

    @JsonClass(generateAdapter = true)
    data class SchemaEnum(
        val value: Any?,
        val description: String? = null
    )
    enum class AppInstanceState{
        CRASHED,
        DEPLOYING,
        RUNNING,
        STOPPED,
        STOPPING
    }

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AppInstanceResponse(
        val name : String,
        val id : Int,
        val state : AppInstanceState,
        val upgrade_available: Boolean,
        val latest_version: String? = null,
        val image_Updates_available : Boolean,
        val custom_app : Boolean,
        val migrated : Boolean,
        val human_version: String,
        val version : String,
        val metadata : Map<String?,Any?> ?= emptyMap(),
        val activeWorkloads: ActiveWorkloads ? = null,
        val notes :String ?= null,
        val portals: Map<String, String>? = emptyMap(),
        val version_details : Map<String,Any>? = null,
        val config : Map<String,Any>?= null
    )
    @Suppress("PropertyName")
    data class UpdateAppConfigOptions(
        val values : Map<String, Any>?= emptyMap(),
        val custom_compose_config : Map<String,Any> ?= emptyMap(),
        val custom_compose_config_string : String = ""
    )
}

/**
 * App states in which an upgrade can be safely triggered. TrueNAS rejects
 * upgrades while an app is in a STOPPED state, so we gate the UI on these.
 */
val AppUpdatableStates: Set<String> = setOf("RUNNING", "DEPLOYING")

/**
 * Whether this app is in a state that permits an upgrade
 * (i.e. it is not STOPPED / asleep).
 */
fun Apps.AppQueryResponse.isUpgradableState(): Boolean =
    state.uppercase() in AppUpdatableStates

/**
 * Whether this app has an update pending AND is currently in a state that
 * permits an upgrade to be triggered.
 */
fun Apps.AppQueryResponse.canUpgradeNow(): Boolean =
    upgrade_available && isUpgradableState()

/**
 * Whether this app is asleep (STOPPED) and would need to be started before
 * an upgrade can be triggered.
 */
fun Apps.AppQueryResponse.isAsleep(): Boolean =
    state.equals("STOPPED", ignoreCase = true)
