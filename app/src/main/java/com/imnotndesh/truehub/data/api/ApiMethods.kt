package com.imnotndesh.truehub.data.api

object ApiMethods {
    object Auth {
        const val AUTH_LOGIN = "auth.login"
        const val AUTH_API_LOGIN = "auth.login_with_api_key"
        const val AUTH_TOKEN_LOGIN = "auth.login_with_token"
        const val AUTH_LOGOUT = "auth.logout"
        const val AUTH_ME = "auth.me"
        const val GEN_AUTH_TOKEN = "auth.generate_token"

        const val GEN_ONETIME_PASSWORD = "auth.generate_onetime_password"
        const val GET_MECHANISM_CHOICES ="auth.mechanism_choices"
        const val LOGIN_EX = "auth.login_ex"
        const val GET_AUTH_SESSIONS = "auth.sessions"
        const val TERMINATE_OTHER_SESSION = "auth.terminate_other_session"
        const val TERMINATE_SESSION = "auth.terminate_session"
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
        const val SYSTEM_BOOT_ID = "system.boot_id"
        const val SYSTEM_HOST_ID = "system.host_id"
        const val SYSTEM_STATE = "system.state"
        const val SYSTEM_READY = "system.ready"
        const val SYSTEM_VERSION = "system.version"
        const val SYSTEM_VERSION_SHORT = "system.version_short"
        const val SYSTEM_PRODUCT_TYPE = "system.product_type"
        const val SYSTEM_FEATURE_ENABLED = "system.feature_enabled"
        const val SYSTEM_RELEASE_NOTES_URL = "system.release_notes_url"
        const val SYSTEM_LICENSE_UPDATE = "system.license_update"
        const val SYSTEM_DEBUG = "system.debug"
        const val SYSTEM_REBOOT = "system.reboot"
        const val SYSTEM_REBOOT_INFO = "system.reboot.info"
        const val GET_JOB_STATUS = "core.get_jobs"
        const val SHUTDOWN = "system.shutdown"
        const val GET_DISK_DETAILS = "disk.query"
        const val GET_POOL_DETAILS = "pool.query"
        const val GET_SYSTEM_UPDATE_VERSIONS = "update.available_versions"
        const val GET_SYSTEM_UPDATE_CONFIG = "update.config"
        const val DOWNLOAD_SYSTEM_UPDATE_VERSION = "update.download"
        const val GET_UPDATE_PROFILES = "update.profile_choices"
        const val RUN_SYSTEM_UPDATES = "update.run"
        const val GET_UPDATE_STATUS = "update.status"
        const val UPDATE_SYSTEM_UPDATE_CONFIGURATION = "update.update"

        // Reporting Stuff
        const val GET_GRAPHS = "reporting.graphs"
        const val GET_GRAPH_DATA = "reporting.get_data"
        const val STOP_JOB = "core.job_abort"

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

        // Service Stuff
        const val GET_SERVICES = "service.query"
        const val GET_SERVICE_INSTANCE = "service.get_instance"
        const val CONTROL_SERVICE = "service.control"
        const val IS_SERVICE_STARTED = "service.started"
        const val IS_SERVICE_STARTED_OR_ENABLED = "service.started_or_enabled"
        const val UPDATE_SERVICE = "service.update"
        /**
         * Create a new user account.
         * @see com.imnotndesh.truehub.data.models.System.UserCreate
         */
        const val USER_CREATE = "user.create"

        /**
         * Delete a user account by id.
         * @see com.imnotndesh.truehub.data.models.System.UserDeleteOptions
         */
        const val USER_DELETE = "user.delete"

        /**
         * Returns a single user instance matching id.
         */
        const val USER_GET_INSTANCE = "user.get_instance"

        /**
         * Get the next available/free UID.
         */
        const val USER_GET_NEXT_UID = "user.get_next_uid"

        /**
         * Returns struct passwd info for a user (by username or uid).
         * @see com.imnotndesh.truehub.data.models.System.UserGetUserObjArgs
         */
        const val USER_GET_USER_OBJ = "user.get_user_obj"

        /**
         * Returns whether a local administrator with a valid password exists.
         */
        const val USER_HAS_LOCAL_ADMINISTRATOR_SET_UP = "user.has_local_administrator_set_up"

        /**
         * Query users with query-filters and query-options.
         */
        const val USER_QUERY = "user.query"

        /**
         * Renew a user's two-factor authentication secret.
         */
        const val USER_RENEW_2FA_SECRET = "user.renew_2fa_secret"

        /**
         * Set the password of a user.
         * @see com.imnotndesh.truehub.data.models.System.UserSetPasswordArgs
         */
        const val USER_SET_PASSWORD = "user.set_password"

        /**
         * Set up local administrator (no auth required if not already set up).
         */
        const val USER_SETUP_LOCAL_ADMINISTRATOR = "user.setup_local_administrator"

        /**
         * Return available shell choices.
         */
        const val USER_SHELL_CHOICES = "user.shell_choices"

        /**
         * Unset two-factor authentication secret for a user.
         */
        const val USER_UNSET_2FA_SECRET = "user.unset_2fa_secret"

        /**
         * Update attributes of an existing user account.
         * @see com.imnotndesh.truehub.data.models.System.UserUpdate
         */
        const val USER_UPDATE = "user.update"
        /**
         * API Key management (most are self-explanatory)
         */
        const val API_KEY_CREATE = "api_key.create"
        const val API_KEY_DELETE = "api_key.delete"
        const val API_KEY_GET_INSTANCE = "api_key.get_instance"
        const val API_KEY_MY_KEYS = "api_key.my_keys"
        const val API_KEY_QUERY = "api_key.query"
        const val API_KEY_UPDATE = "api_key.update"
        // System General Settings
        const val GENERAL_CHECKIN = "system.general.checkin"
        const val GENERAL_CHECKIN_WAITING = "system.general.checkin_waiting"
        const val GENERAL_CONFIG = "system.general.config"
        const val GENERAL_COUNTRY_CHOICES = "system.general.country_choices"
        const val GENERAL_KBDMAP_CHOICES = "system.general.kbdmap_choices"
        const val GENERAL_LOCAL_URL = "system.general.local_url"
        const val GENERAL_TIMEZONE_CHOICES = "system.general.timezone_choices"
        const val GENERAL_UI_ADDRESS_CHOICES = "system.general.ui_address_choices"
        const val GENERAL_UI_CERTIFICATE_CHOICES = "system.general.ui_certificate_choices"
        const val GENERAL_UI_HTTPSPROTOCOLS_CHOICES = "system.general.ui_httpsprotocols_choices"
        const val GENERAL_UI_RESTART = "system.general.ui_restart"
        const val GENERAL_UI_V6ADDRESS_CHOICES = "system.general.ui_v6address_choices"
        const val GENERAL_UPDATE = "system.general.update"
        // Advanced Stuff
        const val GET_ADVANCED_CONFIG = "system.advanced.config"
        const val GET_GPU_PCI_CHOICES = "system.advanced.get_gpu_pci_choices"
        const val GET_LOGIN_BANNER = "system.advanced.login_banner"
        const val GET_SED_GLOBAL_PASSWORD = "system.advanced.sed_global_password"
        const val GET_SED_GLOBAL_PASSWORD_IS_SET = "system.advanced.sed_global_password_is_set"
        const val GET_SERIAL_PORT_CHOICES = "system.advanced.serial_port_choices"
        const val GET_SYSLOG_CERTIFICATE_AUTHORITY_CHOICES = "system.advanced.syslog_certificate_authority_choices"
        const val GET_SYSLOG_CERTIFICATE_CHOICES = "system.advanced.syslog_certificate_choices"
        const val UPDATE_ADVANCED_CONFIG = "system.advanced.update"
        const val UPDATE_GPU_PCI_IDS = "system.advanced.update_gpu_pci_ids"
        // Network Stuff
        const val NETWORK_GENERAL_SUMMARY = "network.general.summary"
        const val NETWORK_CONFIGURATION_ACTIVITY_CHOICES = "network.configuration.activity_choices"
        const val NETWORK_CONFIGURATION_CONFIG = "network.configuration.config"
        const val NETWORK_CONFIGURATION_UPDATE = "network.configuration.update"

        // ──────────────────────────────────────────────
        // Boot Pool Stuff (boot.*)
        // ──────────────────────────────────────────────

        /**
         * Attach a disk to the boot pool, turning a stripe into a mirror.
         * This method is a job.
         * Required role: DISK_WRITE
         */
        const val BOOT_ATTACH = "boot.attach"

        /**
         * Detach given `dev` from boot pool.
         * Required role: DISK_WRITE
         */
        const val BOOT_DETACH = "boot.detach"

        /**
         * Returns disks of the boot pool.
         * Required role: DISK_READ
         */
        const val BOOT_GET_DISKS = "boot.get_disks"

        /**
         * Returns the current state of the boot pool, including all vdevs,
         * properties and datasets.
         * Required role: READONLY_ADMIN
         */
        const val BOOT_GET_STATE = "boot.get_state"

        /**
         * Replace device `label` on boot pool with `dev`.
         * This method is a job.
         * Required role: DISK_WRITE
         */
        const val BOOT_REPLACE = "boot.replace"

        /**
         * Scrub on boot pool.
         * This method is a job.
         * Required role: BOOT_ENV_WRITE
         */
        const val BOOT_SCRUB = "boot.scrub"

        /**
         * Set Automatic Scrub Interval value in days.
         * Required role: BOOT_ENV_WRITE
         */
        const val BOOT_SET_SCRUB_INTERVAL = "boot.set_scrub_interval"

        // ──────────────────────────────────────────────
        // Boot Environment Stuff (boot.environment.*)
        // ──────────────────────────────────────────────
        const val BOOT_ENV_ACTIVATE = "boot.environment.activate"
        const val BOOT_ENV_CLONE = "boot.environment.clone"
        const val BOOT_ENV_DESTROY = "boot.environment.destroy"
        const val BOOT_ENV_KEEP = "boot.environment.keep"
        const val BOOT_ENV_QUERY = "boot.environment.query"

        // ──────────────────────────────────────────────
        // TrueNAS Connect (tn_connect.*)
        // ──────────────────────────────────────────────
        const val TN_CONNECT_CONFIG = "tn_connect.config"
        const val TN_CONNECT_GENERATE_CLAIM_TOKEN = "tn_connect.generate_claim_token"
        const val TN_CONNECT_GET_REGISTRATION_URI = "tn_connect.get_registration_uri"
        const val TN_CONNECT_IP_CHOICES = "tn_connect.ip_choices"
        const val TN_CONNECT_UPDATE = "tn_connect.update"

        // ──────────────────────────────────────────────
        // TrueCommand (truecommand.*)
        // ──────────────────────────────────────────────
        const val TRUECOMMAND_CONFIG = "truecommand.config"
        const val TRUECOMMAND_UPDATE = "truecommand.update"

        // ──────────────────────────────────────────────
        // TrueNAS (truenas.*)
        // ──────────────────────────────────────────────
        const val TRUENAS_ACCEPT_EULA = "truenas.accept_eula"
        const val TRUENAS_GET_CHASSIS_HARDWARE = "truenas.get_chassis_hardware"
        const val TRUENAS_GET_EULA = "truenas.get_eula"
        const val TRUENAS_IS_EULA_ACCEPTED = "truenas.is_eula_accepted"
        const val TRUENAS_IS_IX_HARDWARE = "truenas.is_ix_hardware"
        const val TRUENAS_IS_PRODUCTION = "truenas.is_production"
        const val TRUENAS_MANAGED_BY_TRUECOMMAND = "truenas.managed_by_truecommand"
        const val TRUENAS_SET_PRODUCTION = "truenas.set_production"

    }
    object Apps {
        const val QUERY_APPS = "app.query"
        const val START_APP = "app.start"
        const val STOP_APP = "app.stop"
        const val UPGRADE_APP = "app.upgrade"
        const val UPDATE_APP_CONFIG = "app.update"
        const val GET_APP_CONFIG = "app.config"
        const val GET_UPGRADE_SUMMARY = "app.upgrade_summary"
        const val QUERY_MARKETPLACE_APPS = "app.available"
        const val GET_CATALOG_APP_DETAILS = "catalog.get_app_details"
        const val APP_AVAILABLE_SPACE = "app.available_space"
        const val APP_CREATE = "app.create"
        const val CERTIFICATE_CHOICES = "app.certificate_choices"
        const val USED_APP_PORTS = "app.used_ports"
        const val APP_INSTANCE = "app.get_instance"
        /**
         * App rollback Method
         * @see com.imnotndesh.truehub.data.models.Apps.RollbackOptions
         */
        const val ROLLBACK_APP = "app.rollback"
        const val APP_ROLLBACK_VERSIONS ="app.rollback_versions"
        const val DELETE_APP = "app.delete"
        const val SIMILAR_APPS = "app.similar"

        // ── Container images (app.image.*) ──────────────────
        const val IMAGE_QUERY = "app.image.query"
        const val IMAGE_GET_INSTANCE = "app.image.get_instance"
        const val IMAGE_PULL = "app.image.pull"
        const val IMAGE_DELETE = "app.image.delete"
        const val IMAGE_DOCKERHUB_RATE_LIMIT = "app.image.dockerhub_rate_limit"

        // ── iX volumes (app.ix_volume.*) ────────────────────
        const val IX_VOLUME_EXISTS = "app.ix_volume.exists"
        const val IX_VOLUME_QUERY = "app.ix_volume.query"

        const val LATEST_APPS_TRAIN = "latest"
        const val STABLE_APPS_TRAIN = "stable"
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
    object Audit {
        const val CONFIG = "audit.config"
        const val DOWNLOAD_REPORT = "audit.download_report"
        const val EXPORT = "audit.export"
        const val QUERY = "audit.query"
        const val UPDATE = "audit.update"

        /**
         * Calls a job that produces downloadable output and returns a time-limited,
         * single-use HTTP download URL. Returns a tuple [job id, download URL].
         */
        const val CORE_DOWNLOAD = "core.download"
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

        const val DATASET_DELETE = "pool.dataset.delete"

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
    object Alerts {
        const val ALERTCLASSES_CONFIG = "alertclasses.config"
        const val ALERTCLASSES_UPDATE = "alertclasses.update"
        const val ALERT_SERVICE_GET_INSTANCE = "alertservice.get_instance"
        const val ALERT_SERVICE_DELETE = "alertservice.delete"
        const val ALERT_SERVICE_CREATE = "alertservice.create"
        const val ALERT_SERVICE_QUERY = "alertservice.query"
        const val ALERT_SERVICE_TEST = "alertservice.test"
        const val ALERT_SERVICE_UPDATE = "alertservice.update"
    }

}