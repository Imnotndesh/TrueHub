package com.imnotndesh.truehub.data.models

import androidx.compose.foundation.lazy.layout.IntervalList
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object Storage {
    /**
     * Filesytem Work
     */
    /**
     * Section for directory creation operations
     * @see filesystem.mkdir
     */
    data class FilesystemMkdirArgs(
        val path: String,
        val options : MkdirOptions
    )
    @Suppress("PropertyName")
    data class MkdirOptions(
        val mode : String = "755",
        val raise_chmod_errors : Boolean = true
    )
    @Suppress("PropertyName")
    data class FilesystemMkdirResult(
        val name: String,
        val path: String,
        val realpath : String,
        val type : String,
        val size : Int,
        val allocation_size : Int,
        val mode: Int,
        val mount_id :Int,
        val acl : Boolean,
        val uid : Int,
        val gid :Int,
        val is_mountpoint: Boolean,
        val is_ctldir: Boolean,
        val attributes: List<String>,
        val xattrs : List<String>,
        val zfs_attrs: List<String> ?=null,
    )

    /**
     * Section that gets filesystem information about a specific directory
     * @see filesystem.stat
     */
    data class FilesystemStatArgs(
        val path: String
    )
    @Suppress("PropertyName")
    data class FilesystemStatResult(
        val realpath: String,
        val type: String,
        val size: Long,
        val allocation_size: Long,
        val mode: Int,
        val mount_id: Long,
        val uid: Int,
        val gid: Int,
        val atime: Double,
        val mtime: Double,
        val ctime: Double,
        val btime: Double,
        val dev: Long,
        val inode: Long,
        val nlink: Int,
        val acl: Boolean,
        val is_mountpoint: Boolean,
        val is_ctldir: Boolean,
        val attributes: List<String>,
        val user: String,
        val group: String
    )

    /**
     * Section for returning stats of the filesystem given a specific path
     * @see filesystem.statfs
     */
    data class FilesystemStatfsArgs(
        val path: String
    )
    @Suppress("PropertyName")
    data class FilesystemStatfsResult(
        val flags: List<String>,
        val fsid: String,
        val fstype: String,
        val source: String,
        val dest: String,
        val blocksize: Long,
        val total_blocks: Long,
        val free_blocks: Long,
        val avail_blocks: Long,
        val total_blocks_str: String,
        val free_blocks_str: String,
        val avail_blocks_str: String,
        val files: Long,
        val free_files: Long,
        val name_max: Int,
        val total_bytes: Long,
        val free_bytes: Long,
        val avail_bytes: Long,
        val total_bytes_str: String,
        val free_bytes_str: String,
        val avail_bytes_str: String
    )


    /**
     * Dataset Work
     */
    /**
     * Dataset snapshot removal
     * @see pool.dataset.destroy_snapshots
     */

    data class DestroySnapshotsArgs(
        val name: String,
        val snapshots: SnapShotOptions
    )
    data class SnapShotOptions(
        val all : Boolean = false,
        val recursive : Boolean = false,
        val snapshots : List<SnapShotsOptions> ?= emptyList()
    )
    data class SnapShotsOptions(
        val start : String ? = null,
        val end :String ? = null
    )

    /**
     * Dataset details functions
     * @see pool.dataset.details
     */
    @Suppress("PropertyName")
    data class DatasetDetailsResponse(
        val id: String,
        val type: String,
        val name: String,
        val pool: String,
        val encrypted: Boolean,
        val encryption_root: String?,
        val key_loaded: Boolean,
        val children: List<DatasetDetailsResponse>,
        val snapshot_count: Int,
        val deduplication: ZfsSettingProperty,
        val mountpoint: String,
        val sync: ZfsSettingProperty,
        val compression: ZfsSettingProperty,
        val compressratio: ZfsSettingProperty,
        val origin: ZfsSettingProperty,
        val quota: ZfsSizeProperty,
        val refquota: ZfsSizeProperty,
        val reservation: ZfsSizeProperty,
        val refreservation: ZfsSizeProperty,
        val key_format: ZfsSettingProperty,
        val encryption_algorithm: ZfsSettingProperty,
        val used: ZfsSizeProperty,
        val usedbychildren: ZfsSizeProperty,
        val usedbydataset: ZfsSizeProperty,
        val usedbysnapshots: ZfsSizeProperty,
        val available: ZfsSizeProperty,
        val user_properties: Map<String, String>,
        val locked: Boolean,
        val atime: Boolean,
        val casesensitive: Boolean,
        val readonly: Boolean,
        val thick_provisioned: Boolean,
        val nfs_shares: List<String>,
        val smb_shares: List<String>,
        val iscsi_shares: List<String>,
        val vms: List<String>,
        val apps: List<String>,
        val virt_instances: List<String>,
        val replication_tasks_count: Int,
        val snapshot_tasks_count: Int,
        val cloudsync_tasks_count: Int,
        val rsync_tasks_count: Int
    )
    @Suppress("PropertyName")
    data class ZfsSettingProperty(
        val parsed: String?,
        val rawvalue: String,
        val value: String?,
        val source: String,
        val source_info: String?
    )

    @Suppress("PropertyName")
    data class ZfsSizeProperty(
        val parsed: Long?,
        val rawvalue: String,
        val value: String?,
        val source: String,
        val source_info: String?
    )

    /**
     * Dataset fetch Snapshot count
     */
    data class DatasetFetchSnapshotCountArgs(
        val name: String,
    )

    /** Query all Datasets
     * @see pool.dataset.query
     */
    @Suppress("PropertyName")

    data class ZfsGenericProperty(
        val parsed: Any?,
        val rawvalue: String,
        val value: String?,
        val source: String,
        val source_info: String?
    )
    @Suppress("PropertyName")
    data class ZfsCreationProperty(
        val parsed: MongoDate,
        val rawvalue: String,
        val value: String?,
        val source: String,
        val source_info: String?
    )
    @JsonClass(generateAdapter = true)
    data class MongoDate(
        @Json(name = "\$date") val date: Long
    )
    @Suppress("PropertyName")
    data class ZfsDataset(
        val id: String,
        val type: String,
        val name: String,
        val pool: String,
        val encrypted: Boolean,
        val encryption_root: String?,
        val key_loaded: Boolean,
        val children: List<ZfsDataset>,
        val comments: ZfsGenericProperty? = null,
        val deduplication: ZfsGenericProperty,
        val mountpoint: String,
        val aclmode: ZfsGenericProperty,
        val acltype: ZfsGenericProperty,
        val xattr: ZfsGenericProperty,
        val atime: ZfsGenericProperty,
        val casesensitivity: ZfsGenericProperty,
        val checksum: ZfsGenericProperty,
        val exec: ZfsGenericProperty,
        val sync: ZfsGenericProperty,
        val compression: ZfsGenericProperty,
        val compressratio: ZfsGenericProperty,
        val origin: ZfsGenericProperty,
        val quota: ZfsGenericProperty,
        val refquota: ZfsGenericProperty,
        val reservation: ZfsGenericProperty,
        val refreservation: ZfsGenericProperty,
        val copies: ZfsGenericProperty,
        val snapdir: ZfsGenericProperty,
        val readonly: ZfsGenericProperty,
        val recordsize: ZfsGenericProperty,
        val key_format: ZfsGenericProperty,
        val encryption_algorithm: ZfsGenericProperty,
        val used: ZfsGenericProperty,
        val usedbychildren: ZfsGenericProperty,
        val usedbydataset: ZfsGenericProperty,
        val usedbyrefreservation: ZfsGenericProperty,
        val usedbysnapshots: ZfsGenericProperty,
        val available: ZfsGenericProperty,
        val special_small_block_size: ZfsGenericProperty,
        val pbkdf2iters: ZfsGenericProperty,
        val creation: ZfsCreationProperty,
        val snapdev: ZfsGenericProperty,
        val user_properties: Map<String, Any?>,
        val locked: Boolean
    )


    /**
     * Scrub Work
     */
    /**
     * Pool Scrub details query
     * @see pool.scrub.query
     */
    data class PoolScrubQueryArgs(
        val filters : List<Map<String,String>> ?= emptyList(),
        val options : PoolScrubQueryOptions = PoolScrubQueryOptions()
    )

    @Suppress("PropertyName")
    data class PoolScrubQueryOptions(
        val relationships: Boolean = true,
        val count: Boolean = false,
        val get: Boolean = false,
        val force_sql_filters: Boolean = false,
        val extend: List<String>? = null,
        val extend_context: String? = null,
        val prefix: String? = null,
        val extra: Map<String, Any?> = emptyMap(),
        val order_by: List<String> = emptyList(),
        val select: List<String> = emptyList(),
        val offset: Int = 0,
        val limit: Int = 0
    )
    data class DeletionSchedule(
        val minute: String,
        val hour: String,
        val dom: String,
        val month: String,
        val dow: String
    )
    @Suppress("PropertyName")
    @JsonClass(generateAdapter = true)
    data class PoolScrubQueryResponse(
        val pool: Long,
        val threshold: Int,
        val description: String,
        val schedule: DeletionSchedule,
        val enabled: Boolean,
        val id: Long,
        val pool_name: String
    )

    /**
     * Pool scrub query a single instance
     * @see pool.scrub.get_instance
     * @see PoolScrubQueryResponse
     */
    data class PoolScrubQuerySingleArgs(
        val id: Long
    )

    /**
     * Run a pool scrub (if last pool scrub was done more than threshold days before)
     * Should return an integer for job tracking
     * @see pool.scrub.run
     */
    data class RunPoolScrubArgs(
        val name: String,
        val threshold: Int = 35
    )

    /**
     * Take action on a pool scrub job
     * @see pool.scrub.scrub
     */
    data class TakeActionOnPoolScrubArgs(
        val name: String,
        val action: PoolScrubAction = PoolScrubAction.START
    )
    enum class PoolScrubAction {
        START,
        STOP,
        PAUSE
    }

    /**
     * Update a scrub task
     *
     * Should return an integer for job tracking
     * @see pool.scrub.update
     */
    @Suppress("PropertyName")
    data class UpdatePoolScrubArgs(
        val id_: Int,
        val data : UpdatePoolScrubDetails
    )
    data class UpdatePoolScrubDetails(
        val pool: Long,
        val threshold: Int,
        val description: String,
        val schedule: DeletionSchedule,
        val enabled: Boolean,
    )

    /**
     * Delete a pool scrub task
     *
     * Should return an integer for job tracking
     * @see pool.scrub.delete
     */
    data class DeletePoolScrubArgs(
        val id: Int
    )

    /**
     * Snapshottask Work
     */
    /**
     * Create Periodic Snapshot tasks per dataset at a schedule
     * @see pool.snapshottask.create
     */
    @Suppress("PropertyName")
    data class SnapshotTaskCreateArgs(
        val dataset: String,
        val recursive : Boolean = false,
        val lifetime_value : Int = 2,
        val lifetime_unit : LifetimeUnits = LifetimeUnits.WEEK,
        val enabled : Boolean = true,
        val exclude : List<String> = emptyList(),
        val naming_schema : String = "auto-%Y-%m-%d_%H-%M",
        val allow_empty: Boolean = true,
        val schedule: SnapshotSchedule
    )
    enum class LifetimeUnits {
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
    data class SnapshotSchedule(
        val minute: String = "00",
        val hour: String = "*", // "00" -> "23"
        val dom: String = "*", // "1" -> "31"
        val month: String = "*", // "1" -> "12"
        val dow: String = "*", // "1" -> "7" (maps to day of the week starting with monday)
        val begin : String = "00:00", // Defaults to 24Hr format for both
        val end : String = "23:59"
    )

    @Suppress("PropertyName")
    data class SnapshotCreationResponse(
        val dataset: String,
        val recursive : Boolean = false,
        val lifetime_value : Int = 2,
        val lifetime_unit : LifetimeUnits = LifetimeUnits.WEEK,
        val enabled : Boolean = true,
        val exclude : List<String> = emptyList(),
        val naming_schema : String = "auto-%Y-%m-%d_%H-%M",
        val allow_empty: Boolean = true,
        val schedule: SnapshotSchedule,
        val id: Int,
        val vmware_sync : Boolean,
        val state : Map<Any?,Any?>
    )
    /**
     * Delete a snapshot task
     *
     * NOTE : Should return an int for job tracking
     * @see pool.snapshottask.delete
     */
    data class DeleteSnapshotTaskArgs(
        val id: Int,
        val options : DeleteSnapshotTaskOptions = DeleteSnapshotTaskOptions()
    )
    @Suppress("PropertyName")
    data class DeleteSnapshotTaskOptions(
        val fixate_removal_date : Boolean = false
    )

    /**
     * Returns a list of snapshots which will change the retention if periodic snapshot task id is deleted
     * Should return: List<Map<String, List<String>>>
     *
     * @see pool.snapshottask.delete_will_change_retention_for
     */
    data class DeleteWillChangeRetentionForArgs(
        val id: Int
    )

    /**
     * Get an instance of a Snapshottask
     *
     * Response should map to:
     *  `SnapshotCreationResponse`
     *
     * @see pool.snapshottask.get_instance
     * @see SnapshotCreationResponse
     */
    data class GetSnapshotTaskInstanceArgs(
        val id: Int
    )



    /**
     * Execute a periodic snapshot task of `id`
     *
     * Should return an ID or nothing
     * @see pool.snapshottask.run
     */
    data class ExecuteSnapshotTaskArgs(
        val id: Int
    )

    /**
     * Update a Periodic Snapshot Task with specific id
     * Should return `SnapshotCreationResponse`
     * @see pool.snapshottask.update
     * @see SnapshotCreationResponse
     */
    data class UpdateSnapshotTaskArgs(
        val id: Int,
        val data : UpdateSnapshotTaskDetails
    )
    @Suppress("PropertyName")
    data class UpdateSnapshotTaskDetails(
        val dataset: String,
        val recursive : Boolean = false,
        val lifetime_value : Int = 2,
        val lifetime_unit : LifetimeUnits = LifetimeUnits.WEEK,
        val enabled : Boolean = true,
        val exclude : List<String> = emptyList(),
        val naming_schema : String = "auto-%Y-%m-%d_%H-%M",
        val allow_empty: Boolean = true,
        val schedule: SnapshotSchedule
    )
    /**
     * Returns a list of snapshots which will change the retention if periodic snapshot task id is updated with data.
     * Should return: List<Map<String, List<String>>>
     *
     * @see pool.snapshottask.update_will_change_retention_for
     */
    data class UpdateWillChangeRetentionForArgs(
        val id: Int,
        val data: UpdateSnapshotTaskDetails
    )

}