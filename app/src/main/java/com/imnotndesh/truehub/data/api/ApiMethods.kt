package com.imnotndesh.truehub.data.api

object ApiMethods {
    object Auth {
        const val AUTH_LOGIN = "auth.login"
        const val AUTH_API_LOGIN = "auth.login_with_api_key"
        const val AUTH_TOKEN_LOGIN = "auth.login_with_token"
        const val AUTH_LOGOUT = "auth.logout"
        const val AUTH_ME = "auth.me"
        const val GEN_AUTH_TOKEN = "auth.generate_token"

    }
    object User{
        const val CHANGE_PASSWORD = "user.set_password"
        const val USER_UPDATE = "user.update"
        const val GET_USER_OBJ = "user.get_obj"
    }
    object Connection{
        const val CONNECTION_KEEP_ALIVE = "core.ping"
    }
    object System {
        const val SYSTEM_INFO = "system.info"
        const val GET_JOB_STATUS = "core.get_jobs"
        const val SHUTDOWN = "system.shutdown"
        const val GET_DISK_DETAILS = "disk.query"
        const val GET_POOL_DETAILS = "pool.query"

        // Reporting Stuff
        const val GET_GRAPHS = "reporting.graphs"
        const val GET_GRAPH_DATA = "reporting.get_data"

        // Alert Stuff
        /**
         * Dismiss alert based on uuid(String)
         * @param uuid
         */
        const val DISMISS_ALERT = "alert.dismiss"
        /**
         * List all alerts from server
         * @see com.imnotndesh.truehub.data.models.System.AlertResponse
         */
        const val LIST_ALERTS = "alert.list"
        /**
         * List available alert categories
         * @see com.imnotndesh.truehub.data.models.System.AlertCategoriesResponse
         * and
         * @see com.imnotndesh.truehub.data.models.System.AlertCategoriesClasses
         */
        const val LIST_CATEGORIES = "alert.list_categories"
        /**
         * List all category policies
         */
        const val LIST_POLICIES = "alert.list_policies"
        /**
         * Restore a cleared alert based on uuid
         * @param uuid
         */
        const val RESTORE_ALERTS = "alert.restore"
    }
    object Apps {
        const val QUERY_APPS = "app.query"
        const val START_APP = "app.start"
        const val STOP_APP = "app.stop"
        const val UPGRADE_APP = "app.upgrade"
        const val GET_UPGRADE_SUMMARY = "app.upgrade_summary"

        /**
         * App rollback Method
         * @see com.imnotndesh.truehub.data.models.Apps.RollbackOptions
         */
        const val ROLLBACK_APP = "app.rollback"
        const val APP_ROLLBACK_VERSIONS ="app.rollback_versions"
        const val DELETE_APP = "app.delete"
    }
    object Virt{
        const val GET_ALL_INSTANCES = "virt.instance.query"
        const val START_INSTANCE = "virt.instance.start"
        const val STOP_INSTANCE = "virt.instance.stop"
        const val RESTART_INSTANCE = "virt.instance.restart"
        const val DELETE_INSTANCE = "virt.instance.delete"
        const val UPDATE_INSTANCE = "virt.instance.update"
        const val DELETE_INSTANCE_DEVICE = "virt.instance.device_delete"

        const val GET_IMAGE_CHOICES = "virt.instance.image_choice"
    }
    object Vm{
        const val GET_ALL_VM_INSTANCES = "vm.query"
        const val START_VM_INSTANCE = "vm.start"
        const val STOP_INSTANCE = "vm.stop"
        const val RESTART_INSTANCE = "vm.restart"
        const val DELETE_INSTANCE = "vm.delete"
        const val SUSPEND_VM = "vm.suspend"
        const val RESUME_VM = "vm.resume"
        const val POWER_OFF_VM = "vm.poweroff"
        const val CLONE_VM = "vm.clone"
        const val GET_VM_MEMORY_USAGE = "vm.get_memory_usage"
        const val GET_INSTANCE = "vm.get_instance"
        const val GET_VM_STATUS = "vm.status"
        // TODO : maybe implement this in a webview?
        const val GET_DISPLAY_URL = "vm.get_display_web_uri"
    }
    object Shares{
        const val GET_NFS_SHARES = "sharing.nfs.query"
        const val GET_SMB_SHARES = "sharing.smb.query"
    }
    object Storage {
        /**
         * Creates a new directory at the specified path.
         * @see com.imnotndesh.truehub.data.models.Storage.FilesystemMkdirArgs
         */
        const val FILESYSTEM_MKDIR = "filesystem.mkdir"

        /**
         * Retrieves filesystem information for a specific directory.
         * @see com.imnotndesh.truehub.data.models.Storage.FilesystemStatArgs
         */
        const val FILESYSTEM_STAT = "filesystem.stat"

        /**
         * Returns statistics of the filesystem for a given path.
         * @see com.imnotndesh.truehub.data.models.Storage.FilesystemStatfsArgs
         */
        const val FILESYSTEM_STATFS = "filesystem.statfs"

        /**
         * Removes snapshots from a dataset.
         * @see com.imnotndesh.truehub.data.models.Storage.DestroySnapshotsArgs
         */
        const val DATASET_CREATE = "pool.dataset.create"
        const val DATASET_DESTROY_SNAPSHOTS = "pool.dataset.destroy_snapshots"

        /**
         * Fetches detailed information for a specific dataset.
         * @see com.imnotndesh.truehub.data.models.Storage.DatasetDetailsResponse
         */
        const val DATASET_DETAILS = "pool.dataset.details"

        /**
         * Queries all datasets on the system.
         * @see com.imnotndesh.truehub.data.models.Storage.ZfsDataset
         */
        const val DATASET_QUERY = "pool.dataset.query"

        const val DATASET_DELETE = "pool.dataset.query"

        /**
         * Queries for pool scrub tasks.
         * @see com.imnotndesh.truehub.data.models.Storage.PoolScrubQueryArgs
         */
        const val POOL_SCRUB_QUERY = "pool.scrub.query"
        const val POOL_SCRUB_CREATE = "pool.scrub.create"
        /**
         * Retrieves a single pool scrub task instance.
         * @see com.imnotndesh.truehub.data.models.Storage.PoolScrubQuerySingleArgs
         */
        const val POOL_SCRUB_GET_INSTANCE = "pool.scrub.get_instance"

        /**
         * Initiates a pool scrub if the threshold has been met. Returns a job ID.
         * @see com.imnotndesh.truehub.data.models.Storage.RunPoolScrubArgs
         */
        const val POOL_SCRUB_RUN = "pool.scrub.run"

        /**
         * Performs an action (START, STOP, PAUSE) on a pool scrub job.
         * @see com.imnotndesh.truehub.data.models.Storage.TakeActionOnPoolScrubArgs
         */
        const val POOL_SCRUB_ACTION = "pool.scrub.scrub"

        /**
         * Updates an existing pool scrub task. Returns a job ID.
         * @see com.imnotndesh.truehub.data.models.Storage.UpdatePoolScrubArgs
         */
        const val POOL_SCRUB_UPDATE = "pool.scrub.update"

        /**
         * Deletes a pool scrub task. Returns a job ID.
         * @see com.imnotndesh.truehub.data.models.Storage.DeletePoolScrubArgs
         */
        const val POOL_SCRUB_DELETE = "pool.scrub.delete"

        /**
         * Creates a periodic snapshot task for a dataset.
         * @see com.imnotndesh.truehub.data.models.Storage.SnapshotTaskCreateArgs
         */
        const val SNAPSHOT_TASK_CREATE = "pool.snapshottask.create"

        /**
         * Deletes a periodic snapshot task.
         * Should return an Int for Job Tracking
         * @param com.imnotndesh.truehub.data.models.Storage.DeleteSnapshotTaskArgs
         */
        const val SNAPSHOT_TASK_DELETE = "pool.snapshottask.delete"

        /**
         * Returns a list of snapshots which will change the retention if periodic snapshot task id is deleted
         * @param com.imnotndesh.truehub.data.models.Storage.DeleteWillChangeRetentionForArgs
         */
        const val SNAPSHOT_TASK_DELETE_WILL_CHANGE_RETENTION = "pool.snapshottask.delete_will_change_retention_for"

        /**
         * Fetch an instance of a periodic snapshot task.
         * @param com.imnotndesh.truehub.data.models.Storage.GetSnapshotTaskInstanceArgs
         */
        const val SNAPSHOT_TASK_GET_INSTANCE = "pool.snapshottask.get_instance"
        /**
         * Query All Snapshottasks and return a list of SnapshotCreationResponse
         * @param emptyList
         * @see com.imnotndesh.truehub.data.models.Storage.SnapshotCreationResponse
         */
        const val SNAPSHOT_TASK_QUERY = "pool.snapshottask.query"
        /**
         * Execute a periodic snapshot task of `id`
         * @param com.imnotndesh.truehub.data.models.Storage.ExecuteSnapshotTaskArgs
         */
        const val SNAPSHOT_TASK_RUN = "pool.snapshottask.run"
        /**
         * Updates a periodic snapshot task.
         * @param com.imnotndesh.truehub.data.models.Storage.SnapshotCreationResponse
         */
        const val SNAPSHOT_TASK_UPDATE = "pool.snapshottask.update"
        /**
         * Returns a list of snapshots which will change the retention if periodic snapshot task `id` is updated with `data`.
         * @param com.imnotndesh.truehub.data.models.Storage.UpdateWillChangeRetentionForArgs
         */
        const val SNAPSHOT_TASK_UPDATE_WILL_CHANGE_RETENTION = "pool.snapshottask.update_will_change_retention_for"
    }

}