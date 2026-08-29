package com.imnotndesh.truehub.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


object System {
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class SystemInfo(
        @field:Json(name = "version")
        val version: String,
        @field:Json("buildtime")
        val buildTime: Map<String, Long>?,

        @field:Json("hostname")
        val hostname: String,
        @field:Json("physmem")
        val physmem: Long = 1,

        @field:Json("model")
        val model: String,

        @field:Json("cores")
        val cores: Double,

        @field:Json("physical_cores")
        val physical_cores: Int? = null,

        @field:Json("loadavg")
        val loadavg: List<Double>,

        @field:Json("uptime")
        val uptime: String,

        @field:Json("system_serial")
        val systemSerial: String?,

        @field:Json("system_product")
        val systemProduct: String?,

        @field:Json("system_product_version")
        val systemProductVersion: String?,

        @field:Json("license")
        val license: String?,

        @field:Json("boottime")
        val bootTime: Map<String, Long>?,

        @field:Json("datetime")
        val dateTime: Map<String, Long>?,

        @field:Json("timezone")
        val timezone: String,

        @field:Json("system_manufacturer")
        val system_manufacturer: String?,

        @field:Json("ecc_memory")
        val ecc_memory: Boolean,

        @field:Json("uptime_seconds")
        val uptime_seconds: Double? = null
    )

    /** Values returned by system.state. */
    enum class SystemState(val apiValue: String) {
        BOOTING("BOOTING"),
        READY("READY"),
        SHUTTING_DOWN("SHUTTING_DOWN");

        companion object {
            fun fromApi(value: String?): SystemState? = entries.firstOrNull { it.apiValue == value }
        }
    }

    /** Values returned by system.product_type. */
    enum class ProductType(val apiValue: String) {
        COMMUNITY_EDITION("COMMUNITY_EDITION"),
        ENTERPRISE("ENTERPRISE");

        companion object {
            fun fromApi(value: String?): ProductType? = entries.firstOrNull { it.apiValue == value }
        }
    }

    /** Valid feature names accepted by system.feature_enabled. */
    enum class FeatureName(val apiValue: String) {
        DEDUP("DEDUP"),
        FIBRECHANNEL("FIBRECHANNEL"),
        VM("VM");

        companion object {
            fun fromApi(value: String?): FeatureName? = entries.firstOrNull { it.apiValue == value }
        }
    }

    /** Options for system.license_update. */
    @JsonClass(generateAdapter = true)
    data class LicenseUpdateArgs(
        @field:Json(name = "license") val license: String
    )

    // Jobs Stuff
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class Job(
        val id: Int,
        val method: String,
        val arguments: List<Any>,
        val logs_path: String?,
        val logs_excerpt: String?,
        val result_encoding_error: String?,
        val progress: JobProgress?,
        val result: Any?,
        val error: Any?,
        val exception: String?,
        val exc_info: ExcInfo?,
        val state: String,
        val time_started: Map<String, Long>?,
        val time_finished: Any?,
        val credentials: Credentials?
    )

    @JsonClass(generateAdapter = true)
    data class JobProgress(
        val percent: Int,
        val description: String?,
        val extra: Any?
    )
    @JsonClass(generateAdapter = true)
    data class Credentials(
        val type: String?,
        val data: CredentialsData?
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class CredentialsData(
        val username: String?,
        val login_at: Map<String, Long>?
    )
    @JsonClass(generateAdapter = true)
    data class ExcInfo(
        val repr: String?,
        val type: String?,
        val errno: Double?,
        val extra: Any?
    )
    data class UpgradeJobState(
        val state: String,
        val progress: Int = 0,
        val description: String? = null
    )

    data class TrackedJob(
        val jobId: Int,
        val appName: String,
        val state: String = "PENDING",
        val progress: Int = 0,
        val description: String? = null,
        val showNotification: Boolean = false
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class GraphResult(
        val name :String,
        val title :String,
        val vertical_label : String,
        val identifiers : List<String>?,
    )
    // Graph request
    @JsonClass(generateAdapter = true)
    data class ReportingGraphQuery(
        val unit: ReportingUnit? = null,
        val page: Int? = 1,
        val aggregate: Boolean? = true,
        val start: Long? = null,
        val end: Long? = null
    )
    @JsonClass(generateAdapter = true)
    data class ReportingGraphRequest(
        val name: String,
        val identifier: String? = null
    ) {
        constructor(name: ReportingGraphName, identifier: String? = null) :
                this(name.value, identifier)
    }

    // Graph response
    @JsonClass(generateAdapter = true)
    data class ReportingGraphResponse(
        val name: String,
        val identifier: String?,
        val aggregations: Map<String, Map<String, Double>>?,
        val data: List<List<Double>>,
        val start: Int,
        val end: Int,
        val legend: List<String>
    )

    enum class ReportingGraphName(val value: String) {
        CPU("cpu"),
        CPUTEMP("cputemp"),
        DISK("disk"),
        INTERFACE("interface"),
        LOAD("load"),
        PROCESSES("processes"),
        MEMORY("memory"),
        UPTIME("uptime"),
        ARCACTUALRATE("arcactualrate"),
        ARCRATE("arcrate"),
        ARCSIZE("arcsize"),
        ARCRESULT("arcresult"),
        DISKTEMP("disktemp"),
        UPSCHARGE("upscharge"),
        UPSRUNTIME("upsruntime"),
        UPSVOLTAGE("upsvoltage"),
        UPSCURRENT("upscurrent"),
        UPSFREQUENCY("upsfrequency"),
        UPSLOAD("upsload"),
        UPSTEMPERATURE("upstemperature");
    }
    enum class ReportingUnit(val value: String) {
        HOUR("HOUR"),
        DAY("DAY"),
        WEEK("WEEK"),
        MONTH("MONTH"),
        YEAR("YEAR");
    }


    // Device stuff
    enum class DeviceType {
        DISK,
        SERIAL,
        GPU
    }
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class DeviceInfoQuery(
        val type: DeviceType? = null,
        val get_partitions: Boolean? = null,
        val serials_only: Boolean? = null
    )

    // Disk Details
    @Suppress("PropertyName")
    data class DiskDetails(
        val identifier: String,
        val name: String,
        val subsystem: String,
        val number: Int,
        val serial: String,
        val lunid: String?,
        val size: Long,
        val description: String,
        val transfermode: String,
        val hddstandby: String,
        val advpowermgmt: String,
        val togglesmart: Boolean?,
        val smartoptions: String?,
        val expiretime: String?,
        val critical: String?,
        val difference: String?,
        val informational: String?,
        val model: String?= null,
        val rotationrate: Int?,
        val type: String,
        val zfs_guid: String?,
        val bus: String,
        val devname: String,
        val enclosure: String?,
        val supports_smart: Boolean?,
        val pool: String?
    )

    // Pool Info
    @Suppress("PropertyName")
    data class Pool(
        val id: Int,
        val name: String,
        val guid: String,
        val status: String,
        val path: String,
        val scan: PoolScan? = null,
        val expand: PoolExpand? = null,
        val is_upgraded : Boolean ?= false,
        val healthy: Boolean,
        val warning: Boolean,
        val status_code: String ?= null,
        val status_detail: String? = null,
        val size: Long ?= null,
        val allocated: Long ?= null,
        val free: Long ?= null,
        val freeing: Long ?= null,
        val dedup_table_size: Long ?= null,
        val dedup_table_quota: String? = null,
        val fragmentation: String ?= null,
        val size_str: String? = null,
        val allocated_str: String ? = null,
        val free_str: String ? = null,
        val freeing_str: String ?= null,
        val autotrim: AutoTrim,
        val topology: PoolTopology ?= null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolScan(
        val function: String?,
        val state: String?,
        val start_time: Map<String, Long>?,
        val end_time: Map<String, Long>?,
        val percentage: Double?,
        val bytes_to_process: Long?,
        val bytes_processed: Long?,
        val bytes_issued: Long?,
        val pause: String?,
        val errors: Int?,
        val total_secs_left: Long?
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolExpand(
        val state: String?,
        val expanding_vdev: Any?,
        val start_time: Map<String, Long>?,
        val end_time: Map<String, Long>?,
        val bytes_to_reflow: Long?,
        val bytes_reflowed: Long?,
        val waiting_for_resilver: Boolean?,
        val total_secs_left: Long?,
        val percentage: Double?
    )

    data class PoolTopology(
        val data: List<PoolDevice>,
        val log: List<PoolDevice>,
        val cache: List<PoolDevice>,
        val spare: List<PoolDevice>,
        val special: List<PoolDevice>,
        val dedup: List<PoolDevice>
    )

    @Suppress("PropertyName")
    data class PoolDevice(
        val name: String,
        val type: String,
        val path: String?,
        val guid: String,
        val status: String,
        val stats: PoolStats?,
        val children: List<PoolDevice>,
        val device: String?,
        val disk: String?,
        val unavail_disk: String?
    )

    @Suppress("PropertyName")
    data class PoolStats(
        val timestamp: Long,
        val read_errors: Int,
        val write_errors: Int,
        val checksum_errors: Int,
        val ops: List<Long>,
        val bytes: List<Long>,
        val size: Long,
        val allocated: Long,
        val fragmentation: Int,
        val self_healed: Int,
        val configured_ashift: Int,
        val logical_ashift: Int,
        val physical_ashift: Int
    )

    data class AutoTrim(
        val value: String,
        val rawvalue: String,
        val parsed: String,
        val source: String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class AlertResponse(
        val uuid: String,
        val source: String,
        val klass: String,
        val args: Any? = null,
        val node: String,
        val key: String,
        val datetime: MongoDate,
        val last_occurrence: MongoDate,
        val dismissed: Boolean,
        val mail: Any? = null,
        val text: String,
        val id: String,
        val level: String,
        val formatted: String? = null,
        val one_shot: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class MongoDate(
        @Json(name = "\$date") val date: Long
    )

    @JsonClass(generateAdapter = true)
    data class AlertCategoriesResponse(
        val id : String,
        val title : String,
        val classes : List<AlertCategoriesClasses>,
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AlertCategoriesClasses(
        val id : String,
        val title:String,
        val level : String,
        val proactive_support: Boolean
    )
    @JsonClass(generateAdapter = true)
    data class UpdateAvailableVersionsResponse(
        val train : String,
        val version : UpdateAvailableVersionsVersions
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateAvailableVersionsVersions(
        val version: String,
        val manifest: Map<String, Any>,
        val release_notes : String ?= null,
        val release_notes_url : String
    )

    @JsonClass(generateAdapter = true)
    data class UpdateConfigResponse(
        val id : Int,
        val autocheck: Boolean,
        val profile: String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateDownloadParams(
        val train : String = "null",
        val version : String = "null"
    )
    @JsonClass(generateAdapter = true)
    data class UpdateProfileChoicesResponse(
        val name : String,
        val footnote :String,
        val description: String,
        val available : Boolean
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateRunDefaults(
        val dataset_name : String ?= "null",
        val resume : Boolean ?= false,
        val train :String ?= "null",
        val version : String ?= "null",
        val reboot : Boolean ?= false
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class UpdateStatusResponse(
        val code :String,
        val status : UpdateStatusOptions ?= null,
        val error : UpdateStatusErrorOptions ?= null,
        val upgrade_download_progress : UpdateDownloadProgress ?= null
    )
    @Suppress("PropertyName")
    data class UpdateStatusOptions(
        val current_version: CurrentVersionOptions ?= null,
        val new_version : NewVersionOptions ?= null
    )
    @Suppress("PropertyName")
    data class CurrentVersionOptions(
        val train :String,
        val profile : String,
        val matches_profile: Boolean
    )
    @Suppress("PropertyName")
    data class NewVersionOptions(
        val version :String,
        val manifest :String,
        val release_notes :String ?= null,
        val release_notes_url: String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateStatusErrorOptions(
        val errname : String,
        val reason : String
    )
    @JsonClass(generateAdapter = true)
    data class UpdateDownloadProgress(
        val percent : Int,
        val description: String,
        val version : String
    )
    @JsonClass(generateAdapter = true)
    data class ProfileUpdateResult(
        val id : Int,
        val autocheck : Boolean,
        val profile : String
    )

    // ──────────────────────────────────────────────
    // audit.* models
    // ──────────────────────────────────────────────

    @JsonClass(generateAdapter = true)
    data class AuditEntry(
        @Json(name = "id") val id: Int,
        @Json(name = "retention") val retention: Int,
        @Json(name = "reservation") val reservation: Int,
        @Json(name = "quota") val quota: Int,
        @Json(name = "quota_fill_warning") val quotaFillWarning: Int,
        @Json(name = "quota_fill_critical") val quotaFillCritical: Int,
        @Json(name = "remote_logging_enabled") val remoteLoggingEnabled: Boolean,
        @Json(name = "space") val space: AuditEntrySpace,
        @Json(name = "enabled_services") val enabledServices: AuditEntryEnabledServices
    )

    @JsonClass(generateAdapter = true)
    data class AuditEntrySpace(
        @Json(name = "used") val used: Long,
        @Json(name = "used_by_dataset") val usedByDataset: Long,
        @Json(name = "used_by_reservation") val usedByReservation: Long,
        @Json(name = "used_by_snapshots") val usedBySnapshots: Long,
        @Json(name = "available") val available: Long
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AuditEntryEnabledServices(
        @field:Json(name = "MIDDLEWARE") val middleware: List<Any> = emptyList(),
        @field:Json(name = "SMB") val smb: List<Any> = emptyList(),
        @field:Json(name = "SUDO") val sudo: List<String> = emptyList()
    )

    /** Parameters for audit.update */
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AuditUpdateArgs(
        @field:Json(name = "retention") val retention: Int? = null,
        @field:Json(name = "reservation") val reservation: Int? = null,
        @field:Json(name = "quota") val quota: Int? = null,
        @field:Json(name = "quota_fill_warning") val quotaFillWarning: Int? = null,
        @field:Json(name = "quota_fill_critical") val quotaFillCritical: Int? = null
    )

    @JsonClass(generateAdapter = true)
    data class AuditQueryArgs(
        @Json(name = "services") val services: List<String>? = listOf("MIDDLEWARE"),
        @Json(name = "query-filters") val queryFilters: List<Any>? = emptyList(),
        @Json(name = "query-options") val queryOptions: AuditQueryOptions? = null,
        @Json(name = "remote_controller") val remoteController: Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    data class AuditExportArgs(
        @Json(name = "services") val services: List<String>? = listOf("MIDDLEWARE"),
        @Json(name = "query-filters") val queryFilters: List<Any>? = emptyList(),
        @Json(name = "query-options") val queryOptions: AuditQueryOptions? = null,
        @Json(name = "remote_controller") val remoteController: Boolean? = false,
        @Json(name = "export_format") val exportFormat: String? = "JSON"
    )

    @JsonClass(generateAdapter = true)
    data class AuditQueryOptions(
        @Json(name = "extra") val extra: Map<String, Any>? = null,
        @Json(name = "order_by") val orderBy: List<String>? = null,
        @Json(name = "select") val select: List<Any>? = null,
        @Json(name = "count") val count: Boolean = false,
        @Json(name = "get") val get: Boolean = false,
        @Json(name = "offset") val offset: Int = 0,
        @Json(name = "limit") val limit: Int = 0,
        @Json(name = "force_sql_filters") val forceSqlFilters: Boolean = false
    )
    /** Single result item from audit.query */
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AuditQueryResultItem(
        @field:Json(name = "audit_id") val auditId: Any? = null,
        @field:Json(name = "message_timestamp") val messageTimestamp: Long? = null,
        @field:Json(name = "timestamp") val timestamp: String? = null,
        @field:Json(name = "address") val address: String? = null,
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "session") val session: Any? = null,
        @field:Json(name = "service") val service: String? = null,
        @field:Json(name = "service_data") val serviceData: Any? = null,
        @field:Json(name = "event") val event: String? = null,
        @field:Json(name = "event_data") val eventData: Any? = null,
        @field:Json(name = "success") val success: Boolean? = null
    )

    /** Parameters for audit.download_report */
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class AuditDownloadReportArgs(
        @field:Json(name = "report_name") val reportName: String
    )

    /**
     * Parameters for core.download.
     *
     * Executes the target job method and returns a tuple [jobId, downloadUrl] where
     * downloadUrl is an HTTP path such as "/_download/{job_id}?auth_token={token}".
     *
     * @param method   Name of the job method to execute (e.g. "audit.download_report").
     * @param args     Arguments to pass to the job method.
     * @param filename Suggested filename for the downloaded file (sets Content-Disposition).
     * @param buffered When true the job output is buffered in RAM until completion, then
     *                 made available for download (URL valid for 3600s). When false the job
     *                 streams immediately to the client (valid for 60s).
     */
    @JsonClass(generateAdapter = true)
    data class CoreDownloadArgs(
        @field:Json(name = "method") val method: String,
        @field:Json(name = "args") val args: List<Any> = emptyList(),
        @field:Json(name = "filename") val filename: String? = null,
        @field:Json(name = "buffered") val buffered: Boolean = false
    )

    /**
     * Result tuple from core.download.
     *
     * @property jobId       The job ID, usable with core.get_jobs to monitor progress.
     * @property downloadUrl The download URL path (e.g. "/_download/{job_id}?auth_token={token}").
     */
    data class CoreDownloadResult(
        val jobId: Int,
        val downloadUrl: String
    )

    // Service structs
    @JsonClass(generateAdapter = true)
    data class ServiceQueryResponse(
        val id : Int,
        val service : String,
        val enable : Boolean,
        val state :String,
        val pids : List<Int>
    )
    enum class ServiceControlOptions{
        START,
        STOP,
        RESTART,
        RELOAD
    }
    data class ServiceUpdate(
        var enable : Boolean
    )
    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class ServiceControlCallOptions(
        val ha_propagate : Boolean = true,
        val silent : Boolean = true,
        val timeout : Int ?= 120
    )

    /** Configuration for creating a new user account. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserCreate(
        @field:Json(name = "uid") val uid: Int? = null,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: Int? = null,
        @field:Json(name = "groups") val groups: List<Int>? = null,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "group_create") val group_create: Boolean = false,
        @field:Json(name = "home_create") val home_create: Boolean = false,
        @field:Json(name = "home_mode") val home_mode: String = "700",
        @field:Json(name = "password") val password: String? = null,
        @field:Json(name = "random_password") val random_password: Boolean = false
    )

    /** Fields that can be updated on an existing user account. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserUpdate(
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "home") val home: String? = null,
        @field:Json(name = "shell") val shell: String? = null,
        @field:Json(name = "full_name") val full_name: String? = null,
        @field:Json(name = "smb") val smb: Boolean? = null,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: Int? = null,
        @field:Json(name = "groups") val groups: List<Int>? = null,
        @field:Json(name = "password_disabled") val password_disabled: Boolean? = null,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean? = null,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean? = null,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "home_create") val home_create: Boolean? = null,
        @field:Json(name = "home_mode") val home_mode: String? = null,
        @field:Json(name = "password") val password: String? = null,
        @field:Json(name = "random_password") val random_password: Boolean? = null
    )

    /** Result returned by user.create and user.update. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserCreateUpdateResult(
        @field:Json(name = "id") val id: Int ?= null ,
        @field:Json(name = "uid") val uid: Int,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "unixhash") val unixhash: String? = null,
        @field:Json(name = "smbhash") val smbhash: String? = null,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "builtin") val builtin: Boolean,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: UserGroupEntry ?= null,
        @field:Json(name = "groups") val groups: List<Int>,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "immutable") val immutable: Boolean,
        @field:Json(name = "twofactor_auth_configured") val twofactor_auth_configured: Boolean,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "last_password_change") val last_password_change: MongoDate? = null,
        @field:Json(name = "password_age") val password_age: Int? = null,
        @field:Json(name = "password_history") val password_history: List<String>? = null,
        @field:Json(name = "password_change_required") val password_change_required: Boolean,
        @field:Json(name = "roles") val roles: List<String>,
        @field:Json(name = "api_keys") val api_keys: List<Int>,
        @field:Json(name = "password") val password: String? = null
    )


    /** Primary group info embedded in user entries. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGroupEntry(
        @field:Json(name = "id") val id: Int ?= null,
        @field:Json(name = "bsdgrp_gid") val bsdgrp_gid: Int? = null,
        @field:Json(name = "bsdgrp_group") val bsdgrp_group: String? = null
    )


    /** Options for user.delete. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserDeleteOptions(
        @field:Json(name = "delete_group") val delete_group: Boolean = true
    )

    /** Arguments for user.get_user_obj. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGetUserObjArgs(
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "uid") val uid: Int? = null,
        @field:Json(name = "get_groups") val get_groups: Boolean = false,
        @field:Json(name = "sid_info") val sid_info: Boolean = false
    )

    /** Response from user.get_user_obj (struct passwd info). */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserGetUserObj(
        @field:Json(name = "pw_name") val pw_name: String,
        @field:Json(name = "pw_gecos") val pw_gecos: String,
        @field:Json(name = "pw_dir") val pw_dir: String,
        @field:Json(name = "pw_shell") val pw_shell: String,
        @field:Json(name = "pw_uid") val pw_uid: Int,
        @field:Json(name = "pw_gid") val pw_gid: Int,
        @field:Json(name = "grouplist") val grouplist: List<Int>? = null,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "source") val source: String,
        @field:Json(name = "local") val local: Boolean
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserSetPasswordArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "old_password") val old_password: String? = null,
        @field:Json(name = "new_password") val new_password: String
    )

    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "password") val password: String,
        @field:Json(name = "options") val options: UserSetupLocalAdministratorOptions? = null
    )

    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorOptions(
        @field:Json(name = "ec2") val ec2: UserSetupLocalAdministratorEC2Options? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserSetupLocalAdministratorEC2Options(
        @field:Json(name = "instance_id") val instance_id: String
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserRenew2faSecretResult(
        @field:Json(name = "id") val id: Int?,
        @field:Json(name = "uid") val uid: Int,
        @field:Json(name = "username") val username: String,
        @field:Json(name = "unixhash") val unixhash: String? = null,
        @field:Json(name = "smbhash") val smbhash: String? = null,
        @field:Json(name = "home") val home: String = "/var/empty",
        @field:Json(name = "shell") val shell: String = "/usr/bin/zsh",
        @field:Json(name = "full_name") val full_name: String,
        @field:Json(name = "builtin") val builtin: Boolean,
        @field:Json(name = "smb") val smb: Boolean = true,
        @field:Json(name = "userns_idmap") val userns_idmap: Any? = null,
        @field:Json(name = "group") val group: UserGroupEntry,
        @field:Json(name = "groups") val groups: List<Int>,
        @field:Json(name = "password_disabled") val password_disabled: Boolean = false,
        @field:Json(name = "ssh_password_enabled") val ssh_password_enabled: Boolean = false,
        @field:Json(name = "sshpubkey") val sshpubkey: String? = null,
        @field:Json(name = "locked") val locked: Boolean = false,
        @field:Json(name = "sudo_commands") val sudo_commands: List<String>? = null,
        @field:Json(name = "sudo_commands_nopasswd") val sudo_commands_nopasswd: List<String>? = null,
        @field:Json(name = "email") val email: String? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "immutable") val immutable: Boolean,
        @field:Json(name = "twofactor_auth_configured") val twofactor_auth_configured: Boolean,
        @field:Json(name = "sid") val sid: String? = null,
        @field:Json(name = "last_password_change") val last_password_change: MongoDate? = null,
        @field:Json(name = "password_age") val password_age: Int? = null,
        @field:Json(name = "password_history") val password_history: List<String>? = null,
        @field:Json(name = "password_change_required") val password_change_required: Boolean,
        @field:Json(name = "roles") val roles: List<String>,
        @field:Json(name = "api_keys") val api_keys: List<Int>,
        @field:Json(name = "twofactor_config") val twofactor_config: UserTwofactorConfigEntry
    )


    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserTwofactorConfigEntry(
        @field:Json(name = "provisioning_uri") val provisioning_uri: String? = null,
        @field:Json(name = "secret_configured") val secret_configured: Boolean,
        @field:Json(name = "interval") val interval: Int,
        @field:Json(name = "otp_digits") val otp_digits: Int
    )

    /** Arguments for user.renew_2fa_secret. */
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserRenew2faSecretArgs(
        @field:Json(name = "username") val username: String,
        @field:Json(name = "twofactor_options") val twofactor_options: UserTwofactorOptions
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserTwofactorOptions(
        @field:Json(name = "otp_digits") val otp_digits: Int = 6,
        @field:Json(name = "interval") val interval: Int = 30
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class UserQueryOptions(
        @field:Json(name = "extra") val extra: Map<String, Any>? = null,
        @field:Json(name = "order_by") val order_by: List<String>? = null,
        @field:Json(name = "select") val select: List<Any>? = null,
        @field:Json(name = "count") val count: Boolean = false,
        @field:Json(name = "get") val get: Boolean = false,
        @field:Json(name = "offset") val offset: Int = 0,
        @field:Json(name = "limit") val limit: Int = 0,
        @field:Json(name = "force_sql_filters") val force_sql_filters: Boolean = false
    )

    @JsonClass(generateAdapter = true)
    data class UserShellChoice(
        @field:Json(name = "path") val path: String,
        @field:Json(name = "name") val name: String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyCreate(
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String,
        @field:Json(name = "expires_at") val expires_at: String? = null
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "user_identifier") val user_identifier: Any? = null,
        @field:Json(name = "keyhash") val keyhash: String,
        @field:Json(name = "created_at") val created_at: MongoDate? = null,
        @field:Json(name = "expires_at") val expires_at: MongoDate? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "revoked") val revoked: Boolean,
        @field:Json(name = "revoked_reason") val revoked_reason: String? = null
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyEntryWithKey(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "name") val name: String = "nobody",
        @field:Json(name = "username") val username: String? = null,
        @field:Json(name = "user_identifier") val user_identifier: Any? = null,
        @field:Json(name = "keyhash") val keyhash: String,
        @field:Json(name = "created_at") val created_at: MongoDate? = null,
        @field:Json(name = "expires_at") val expires_at: MongoDate? = null,
        @field:Json(name = "local") val local: Boolean,
        @field:Json(name = "revoked") val revoked: Boolean,
        @field:Json(name = "revoked_reason") val revoked_reason: String? = null,
        @field:Json(name = "key") val key: String
    )

    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class ApiKeyUpdate(
        @field:Json(name = "name") val name: String? = null,
        @field:Json(name = "expires_at") val expires_at: String? = null,
        @field:Json(name = "reset") val reset: Boolean? = null
    )
    // System General Settings
    @JsonClass(generateAdapter = true)
    data class SystemGeneralEntry(
        @field:Json(name = "id")
        val id: Int,
        @field:Json(name = "ui_certificate")
        val uiCertificate: Any? = null,
        @field:Json(name = "ui_httpsport")
        val uiHttpsPort: Int? = null,
        @field:Json(name = "ui_httpsredirect")
        val uiHttpsRedirect: Boolean? = null,
        @field:Json(name = "ui_httpsprotocols")
        val uiHttpsProtocols: List<String>? = emptyList(),
        @field:Json(name = "ui_port")
        val uiPort: Int? = null,
        @field:Json(name = "ui_address")
        val uiAddress: List<String>? = emptyList(),
        @field:Json(name = "ui_v6address")
        val uiV6Address: List<String> ?= emptyList(),
        @field:Json(name = "ui_allowlist")
        val uiAllowlist: List<String> ?= emptyList(),
        @field:Json(name = "ui_consolemsg")
        val uiConsoleMsg: Boolean ?= false,
        @field:Json(name = "ui_x_frame_options")
        val uiXFrameOptions: String ? = null,
        @field:Json(name = "kbdmap")
        val kbdMap: String ?= null,
        @field:Json(name = "timezone")
        val timezone: String ?= "Not Configured",
        @field:Json(name = "usage_collection")
        val usageCollection: Any? = null,
        @field:Json(name = "wizardshown")
        val wizardShown: Boolean ?= null,
        @field:Json(name = "usage_collection_is_set")
        val usageCollectionIsSet: Boolean ?= false,
        @field:Json(name = "ds_auth")
        val dsAuth: Boolean ?= false
    )
    // ──────────────────────────────────────────────
// system.advanced.* models
// ──────────────────────────────────────────────

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class SystemAdvancedEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "advancedmode") val advancedmode: Boolean,
        @field:Json(name = "autotune") val autotune: Boolean,
        @field:Json(name = "kdump_enabled") val kdump_enabled: Boolean,
        @field:Json(name = "boot_scrub") val boot_scrub: Int,
        @field:Json(name = "consolemenu") val consolemenu: Boolean,
        @field:Json(name = "consolemsg") val consolemsg: Boolean,
        @field:Json(name = "debugkernel") val debugkernel: Boolean,
        @field:Json(name = "fqdn_syslog") val fqdn_syslog: Boolean,
        @field:Json(name = "motd") val motd: String,
        @field:Json(name = "login_banner") val login_banner: String,
        @field:Json(name = "powerdaemon") val powerdaemon: Boolean,
        @field:Json(name = "serialconsole") val serialconsole: Boolean,
        @field:Json(name = "serialport") val serialport: String,
        @field:Json(name = "serialspeed") val serialspeed: String,
        @field:Json(name = "anonstats_token") val anonstats_token: String,
        @field:Json(name = "overprovision") val overprovision: Int? = null,
        @field:Json(name = "traceback") val traceback: Boolean,
        @field:Json(name = "uploadcrash") val uploadcrash: Boolean,
        @field:Json(name = "anonstats") val anonstats: Boolean,
        @field:Json(name = "sed_user") val sed_user: String,
        @field:Json(name = "sysloglevel") val sysloglevel: String,
        @field:Json(name = "syslogservers") val syslogservers: List<SyslogServer>? = null,
        @field:Json(name = "syslog_audit") val syslog_audit: Boolean? = null,
        @field:Json(name = "isolated_gpu_pci_ids") val isolated_gpu_pci_ids: List<String>,
        @field:Json(name = "kernel_extra_options") val kernel_extra_options: String
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class SyslogServer(
        @field:Json(name = "host") val host: String,
        @field:Json(name = "transport") val transport: String? = "UDP",
        @field:Json(name = "tls_certificate") val tls_certificate: Int? = null
    )

    @JsonClass(generateAdapter = true)
    @Suppress("PropertyName")
    data class SystemAdvancedUpdateArgs(
        @field:Json(name = "advancedmode") val advancedmode: Boolean? = null,
        @field:Json(name = "autotune") val autotune: Boolean? = null,
        @field:Json(name = "kdump_enabled") val kdump_enabled: Boolean? = null,
        @field:Json(name = "boot_scrub") val boot_scrub: Int? = null,
        @field:Json(name = "consolemenu") val consolemenu: Boolean? = null,
        @field:Json(name = "consolemsg") val consolemsg: Boolean? = null,
        @field:Json(name = "debugkernel") val debugkernel: Boolean? = null,
        @field:Json(name = "fqdn_syslog") val fqdn_syslog: Boolean? = null,
        @field:Json(name = "motd") val motd: String? = null,
        @field:Json(name = "login_banner") val login_banner: String? = null,
        @field:Json(name = "powerdaemon") val powerdaemon: Boolean? = null,
        @field:Json(name = "serialconsole") val serialconsole: Boolean? = null,
        @field:Json(name = "serialport") val serialport: String? = null,
        @field:Json(name = "serialspeed") val serialspeed: String? = null,
        @field:Json(name = "overprovision") val overprovision: Int? = null,
        @field:Json(name = "traceback") val traceback: Boolean? = null,
        @field:Json(name = "uploadcrash") val uploadcrash: Boolean? = null,
        @field:Json(name = "anonstats") val anonstats: Boolean? = null,
        @field:Json(name = "sed_user") val sed_user: String? = null,
        @field:Json(name = "sed_passwd") val sed_passwd: String? = null,
        @field:Json(name = "sysloglevel") val sysloglevel: String? = null,
        @field:Json(name = "syslogservers") val syslogservers: List<SyslogServer>? = null,
        @field:Json(name = "syslog_audit") val syslog_audit: Boolean? = null,
        @field:Json(name = "kernel_extra_options") val kernel_extra_options: String? = null
    )

    // ──────────────────────────────────────────────
    // network.general.* / network.configuration.* models
    // ──────────────────────────────────────────────

    @JsonClass(generateAdapter = true)
    data class NetworkGeneralSummaryResult(
        @field:Json(name = "ips") val ips: Map<String, NetworkGeneralSummaryIP>,
        @field:Json(name = "default_routes") val defaultRoutes: List<String> ?= emptyList(),
        @field:Json(name = "nameservers") val nameservers: List<String>
    )

    @JsonClass(generateAdapter = true)
    data class NetworkGeneralSummaryIP(
        @field:Json(name = "IPV4") val ipv4: List<String>? = null,
        @field:Json(name = "IPV6") val ipv6: List<String>? = null
    )

    @JsonClass(generateAdapter = true)
    data class NetworkConfigurationEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "hostname") val hostname: String,
        @field:Json(name = "domain") val domain: String,
        @field:Json(name = "ipv4gateway") val ipv4Gateway: String?,
        @field:Json(name = "ipv6gateway") val ipv6Gateway: String?,
        @field:Json(name = "nameserver1") val nameserver1: String?,
        @field:Json(name = "nameserver2") val nameserver2: String?,
        @field:Json(name = "nameserver3") val nameserver3: String?,
        @field:Json(name = "httpproxy") val httpProxy: String? ,
        @field:Json(name = "hosts") val hosts: List<String>,
        @field:Json(name = "domains") val domains: List<String>,
        @field:Json(name = "service_announcement") val serviceAnnouncement: NetworkConfigurationServiceAnnouncement?,
        @field:Json(name = "activity") val activity: NetworkConfigurationActivity,
        @field:Json(name = "hostname_local") val hostnameLocal: String?,
        @field:Json(name = "hostname_b") val hostnameB: String? = null,
        @field:Json(name = "hostname_virtual") val hostnameVirtual: String? = null,
        @field:Json(name = "state") val state: NetworkConfigurationState
    )

    @JsonClass(generateAdapter = true)
    data class NetworkConfigurationServiceAnnouncement(
        @field:Json(name = "netbios") val netbios: Boolean? = false,
        @field:Json(name = "mdns") val mdns: Boolean? = false,
        @field:Json(name = "wsd") val wsd: Boolean? = false
    )

    @JsonClass(generateAdapter = true)
    data class NetworkConfigurationActivity(
        @field:Json(name = "type") val type: String,
        @field:Json(name = "activities") val activities: List<String> = emptyList()
    )

    @JsonClass(generateAdapter = true)
    data class NetworkConfigurationState(
        @field:Json(name = "ipv4gateway") val ipv4Gateway: String?,
        @field:Json(name = "ipv6gateway") val ipv6Gateway: String?,
        @field:Json(name = "nameserver1") val nameserver1: String?,
        @field:Json(name = "nameserver2") val nameserver2: String?,
        @field:Json(name = "nameserver3") val nameserver3: String?,
        @field:Json(name = "hosts") val hosts: List<String>
    )

    /** Arguments for network.configuration.update */
    @JsonClass(generateAdapter = true)
    data class NetworkConfigurationUpdateArgs(
        @field:Json(name = "hostname") val hostname: String? = null,
        @field:Json(name = "domain") val domain: String? = null,
        @field:Json(name = "ipv4gateway") val ipv4Gateway: String? = null,
        @field:Json(name = "ipv6gateway") val ipv6Gateway: String? = null,
        @field:Json(name = "nameserver1") val nameserver1: String? = null,
        @field:Json(name = "nameserver2") val nameserver2: String? = null,
        @field:Json(name = "nameserver3") val nameserver3: String? = null,
        @field:Json(name = "httpproxy") val httpProxy: String? = null,
        @field:Json(name = "hosts") val hosts: List<String>? = null,
        @field:Json(name = "domains") val domains: List<String>? = null,
        @field:Json(name = "service_announcement") val serviceAnnouncement: NetworkConfigurationServiceAnnouncement? = null,
        @field:Json(name = "activity") val activity: NetworkConfigurationActivity? = null,
        @field:Json(name = "hostname_b") val hostnameB: String? = null,
        @field:Json(name = "hostname_virtual") val hostnameVirtual: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class SystemGeneralUpdateArgs(
        @field:Json(name = "ui_certificate")
        val uiCertificate: Any? = null,
        @field:Json(name = "ui_httpsport")
        val uiHttpsPort: Int? = null,
        @field:Json(name = "ui_httpsredirect")
        val uiHttpsRedirect: Boolean? = null,
        @field:Json(name = "ui_httpsprotocols")
        val uiHttpsProtocols: List<String>? = null,
        @field:Json(name = "ui_port")
        val uiPort: Int? = null,
        @field:Json(name = "ui_address")
        val uiAddress: List<String>? = null,
        @field:Json(name = "ui_v6address")
        val uiV6Address: List<String>? = null,
        @field:Json(name = "ui_allowlist")
        val uiAllowlist: List<String>? = null,
        @field:Json(name = "ui_consolemsg")
        val uiConsoleMsg: Boolean? = null,
        @field:Json(name = "ui_x_frame_options")
        val uiXFrameOptions: String? = null,
        @field:Json(name = "kbdmap")
        val kbdMap: String? = null,
        @field:Json(name = "timezone")
        val timezone: String? = null,
        @field:Json(name = "usage_collection")
        val usageCollection: Any? = null,
        @field:Json(name = "ds_auth")
        val dsAuth: Boolean? = null,
        @field:Json(name = "ui_restart_delay")
        val uiRestartDelay: Any? = null,
        @field:Json(name = "rollback_timeout")
        val rollbackTimeout: Any? = null
    )


    // ──────────────────────────────────────────────
    // Boot Pool Stuff (boot.*)
    // ──────────────────────────────────────────────

    /** Parameters for boot.attach. */
    @JsonClass(generateAdapter = true)
    data class BootAttachOptions(
        @field:Json(name = "expand") val expand: Boolean = false
    )

    /**
     * Current state and configuration of the boot pool returned by boot.get_state.
     */
    @JsonClass(generateAdapter = true)
    data class BootGetState(
        @field:Json(name = "name") val name: String,
        @field:Json(name = "status") val status: String,
        @field:Json(name = "path") val path: String,
        @field:Json(name = "scan") val scan: BootScan? = null,
        @field:Json(name = "expand") val expand: BootExpand? = null,
        @field:Json(name = "is_upgraded") val isUpgraded: Boolean = false,
        @field:Json(name = "healthy") val healthy: Boolean,
        @field:Json(name = "warning") val warning: Boolean,
        @field:Json(name = "status_code") val statusCode: String? = null,
        @field:Json(name = "status_detail") val statusDetail: String? = null,
        @field:Json(name = "size") val size: Long? = null,
        @field:Json(name = "allocated") val allocated: Long? = null,
        @field:Json(name = "free") val free: Long? = null,
        @field:Json(name = "freeing") val freeing: Long? = null,
        @field:Json(name = "dedup_table_size") val dedupTableSize: Long? = null,
        @field:Json(name = "dedup_table_quota") val dedupTableQuota: String? = null,
        @field:Json(name = "fragmentation") val fragmentation: String? = null,
        @field:Json(name = "size_str") val sizeStr: String? = null,
        @field:Json(name = "allocated_str") val allocatedStr: String? = null,
        @field:Json(name = "free_str") val freeStr: String? = null,
        @field:Json(name = "freeing_str") val freeingStr: String? = null,
        @field:Json(name = "autotrim") val autotrim: BootAutotrim? = null,
        @field:Json(name = "topology") val topology: BootPoolTopology? = null
    )

    /** Information about an active scrub or resilver operation. */
    @JsonClass(generateAdapter = true)
    data class BootScan(
        @field:Json(name = "bytes_issued") val bytesIssued: Long? = null,
        @field:Json(name = "bytes_processed") val bytesProcessed: Long? = null,
        @field:Json(name = "bytes_to_process") val bytesToProcess: Long? = null,
        @field:Json(name = "end_time") val endTime: String? = null,
        @field:Json(name = "errors") val errors: Long? = null,
        @field:Json(name = "function") val function: String? = null,
        @field:Json(name = "pause") val pause: Boolean? = null,
        @field:Json(name = "percentage") val percentage: Double? = null,
        @field:Json(name = "start_time") val startTime: String? = null,
        @field:Json(name = "state") val state: String? = null,
        @field:Json(name = "total_secs_left") val totalSecsLeft: Long? = null
    )

    /** Information about an active pool expansion operation. */
    @JsonClass(generateAdapter = true)
    data class BootExpand(
        @field:Json(name = "bytes_reflowed") val bytesReflowed: Long? = null,
        @field:Json(name = "bytes_to_reflow") val bytesToReflow: Long? = null,
        @field:Json(name = "end_time") val endTime: String? = null,
        @field:Json(name = "expanding_vdev") val expandingVdev: Int? = null,
        @field:Json(name = "percentage") val percentage: Double? = null,
        @field:Json(name = "start_time") val startTime: String? = null,
        @field:Json(name = "state") val state: String? = null,
        @field:Json(name = "total_secs_left") val totalSecsLeft: Long? = null,
        @field:Json(name = "waiting_for_resilver") val waitingForResilver: Int? = null
    )

    /** Auto-trim configuration for the pool. */
    @JsonClass(generateAdapter = true)
    data class BootAutotrim(
        @field:Json(name = "parsed") val parsed: String? = null,
        @field:Json(name = "rawvalue") val rawValue: String? = null,
        @field:Json(name = "source") val source: String? = null,
        @field:Json(name = "value") val value: String? = null
    )

    /** Physical topology and structure of the pool including vdevs. */
    @JsonClass(generateAdapter = true)
    data class BootPoolTopology(
        @field:Json(name = "data") val data: List<Any> = emptyList(),
        @field:Json(name = "log") val log: List<Any> = emptyList(),
        @field:Json(name = "cache") val cache: List<Any> = emptyList(),
        @field:Json(name = "spare") val spare: List<Any> = emptyList(),
        @field:Json(name = "special") val special: List<Any> = emptyList(),
        @field:Json(name = "dedup") val dedup: List<Any> = emptyList()
    )

    // ──────────────────────────────────────────────
    // Boot Environment Stuff (boot.environment.*)
    // ──────────────────────────────────────────────

    /** Base query options shared across query methods. */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentQueryOptions(
        @field:Json(name = "extra") val extra: Map<String, Any?> = emptyMap(),
        @field:Json(name = "order_by") val orderBy: List<String> = emptyList(),
        @field:Json(name = "select") val select: List<Any> = emptyList(),
        @field:Json(name = "count") val count: Boolean = false,
        @field:Json(name = "get") val get: Boolean = false,
        @field:Json(name = "offset") val offset: Int = 0,
        @field:Json(name = "limit") val limit: Int = 0,
        @field:Json(name = "force_sql_filters") val forceSqlFilters: Boolean = false
    )

    /** Arguments for boot.environment.activate. */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentActivateArgs(
        @field:Json(name = "id") val id: String
    )

    /** Arguments for boot.environment.clone. */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentCloneArgs(
        @field:Json(name = "id") val id: String,
        @field:Json(name = "target") val target: String
    )

    /** Arguments for boot.environment.destroy. */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentDestroyArgs(
        @field:Json(name = "id") val id: String
    )

    /** Arguments for boot.environment.keep. */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentKeepArgs(
        @field:Json(name = "id") val id: String,
        @field:Json(name = "value") val value: Boolean
    )

    /**
     * A boot environment entry returned by boot.environment.* methods.
     */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentEntry(
        @field:Json(name = "id") val id: String,
        @field:Json(name = "dataset") val dataset: String,
        @field:Json(name = "active") val active: Boolean,
        @field:Json(name = "activated") val activated: Boolean,
        @field:Json(name = "created") val created: String,
        @field:Json(name = "used_bytes") val usedBytes: Long,
        @field:Json(name = "used") val used: String,
        @field:Json(name = "keep") val keep: Boolean,
        @field:Json(name = "can_activate") val canActivate: Boolean
    )

    /**
     * A boot environment entry returned by boot.environment.query.
     */
    @JsonClass(generateAdapter = true)
    data class BootEnvironmentQueryResultItem(
        @field:Json(name = "id") val id: String?,
        @field:Json(name = "dataset") val dataset: String?,
        @field:Json(name = "active") val active: Boolean?,
        @field:Json(name = "activated") val activated: Boolean?,
        @field:Json(name = "created") val created: String?,
        @field:Json(name = "used_bytes") val usedBytes: Long?,
        @field:Json(name = "used") val used: String?,
        @field:Json(name = "keep") val keep: Boolean?,
        @field:Json(name = "can_activate") val canActivate: Boolean?
    )

    // ──────────────────────────────────────────────
    // TrueNAS Connect (tn_connect.*)
    // ──────────────────────────────────────────────

    /**
     * Configuration entry returned by tn_connect.config and tn_connect.update.
     */
    @JsonClass(generateAdapter = true)
    data class TNCEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "enabled") val enabled: Boolean,
        @field:Json(name = "registration_details") val registrationDetails: Map<String, Any?>? = null,
        @field:Json(name = "ips") val ips: List<String> = emptyList(),
        @field:Json(name = "interfaces") val interfaces: List<String> = emptyList(),
        @field:Json(name = "interfaces_ips") val interfacesIps: List<String> = emptyList(),
        @field:Json(name = "use_all_interfaces") val useAllInterfaces: Boolean ?= false,
        @field:Json(name = "status") val status: String,
        @field:Json(name = "status_reason") val statusReason: String?= "Missing",
        @field:Json(name = "certificate") val certificate: Int? = null,
        @field:Json(name = "account_service_base_url") val accountServiceBaseUrl: String ?= "Not Configured",
        @field:Json(name = "leca_service_base_url") val lecaServiceBaseUrl: String ?= "Not Configured",
        @field:Json(name = "tnc_base_url") val tncBaseUrl: String ?= "Not Configured",
        @field:Json(name = "heartbeat_url") val heartbeatUrl: String ?= "Not Configured"
    )

    /**
     * Parameters for tn_connect.update.
     */
    @JsonClass(generateAdapter = true)
    data class TNConnectUpdateArgs(
        @field:Json(name = "enabled") val enabled: Boolean? = null,
        @field:Json(name = "ips") val ips: List<String>? = null,
        @field:Json(name = "interfaces") val interfaces: List<String>? = null,
        @field:Json(name = "use_all_interfaces") val useAllInterfaces: Boolean? = null
    )

    // ──────────────────────────────────────────────
    // TrueCommand (truecommand.*)
    // ──────────────────────────────────────────────

    /**
     * Configuration entry returned by truecommand.config and truecommand.update.
     */
    @JsonClass(generateAdapter = true)
    data class TruecommandEntry(
        @field:Json(name = "id") val id: Int,
        @field:Json(name = "api_key") val apiKey: String? = null,
        @field:Json(name = "status") val status: String,
        @field:Json(name = "status_reason") val statusReason: String? = null,
        @field:Json(name = "remote_url") val remoteUrl: String? = null,
        @field:Json(name = "remote_ip_address") val remoteIpAddress: String? = null,
        @field:Json(name = "enabled") val enabled: Boolean
    )

    /**
     * Parameters for truecommand.update.
     */
    @JsonClass(generateAdapter = true)
    data class TruecommandUpdateArgs(
        @field:Json(name = "enabled") val enabled: Boolean? = null,
        @field:Json(name = "api_key") val apiKey: String? = null
    )

    fun MongoDate?.formatDate(): String {
        if (this == null) return "—"
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getDefault()
        return sdf.format(java.util.Date(this.date))
    }


}