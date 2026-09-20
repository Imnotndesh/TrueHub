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
        const val AUTH_LOGIN_EX_CONTINUE = "auth.login_ex_continue"
        const val AUTH_SET_ATTRIBUTE = "auth.set_attribute"
        const val AUTH_TERMINATE_OTHER_SESSIONS = "auth.terminate_other_sessions"
        const val AUTH_TWOFACTOR_CONFIG = "auth.twofactor.config"
        const val AUTH_TWOFACTOR_UPDATE = "auth.twofactor.update"
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
        const val SERVICE_RELOAD = "service.reload"
        const val SERVICE_RESTART = "service.restart"
        const val SERVICE_START = "service.start"
        const val SERVICE_STOP = "service.stop"
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
        const val SYSTEM_GLOBAL_ID = "system.global.id"
        const val SYSTEM_NTPSERVER_CREATE = "system.ntpserver.create"
        const val SYSTEM_NTPSERVER_DELETE = "system.ntpserver.delete"
        const val SYSTEM_NTPSERVER_GET_INSTANCE = "system.ntpserver.get_instance"
        const val SYSTEM_NTPSERVER_QUERY = "system.ntpserver.query"
        const val SYSTEM_NTPSERVER_UPDATE = "system.ntpserver.update"
        const val SYSTEM_SECURITY_CONFIG = "system.security.config"
        const val SYSTEM_SECURITY_INFO_FIPS_AVAILABLE = "system.security.info.fips_available"
        const val SYSTEM_SECURITY_INFO_FIPS_ENABLED = "system.security.info.fips_enabled"
        const val SYSTEM_SECURITY_UPDATE = "system.security.update"

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
        const val CATALOG_APPS = "catalog.apps"
        const val CATALOG_CONFIG = "catalog.config"
        const val CATALOG_SYNC = "catalog.sync"
        const val CATALOG_TRAINS = "catalog.trains"
        const val CATALOG_UPDATE = "catalog.update"
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

        const val APP_CATEGORIES = "app.categories"
        const val APP_GPU_CHOICES = "app.gpu_choices"
        const val APP_IP_CHOICES = "app.ip_choices"
        const val APP_LATEST = "app.latest"
        const val APP_OUTDATED_DOCKER_IMAGES = "app.outdated_docker_images"
        const val APP_PULL_IMAGES = "app.pull_images"
        const val APP_REDEPLOY = "app.redeploy"
        const val APP_USED_HOST_IPS = "app.used_host_ips"
        const val APP_CONTAINER_CONSOLE_CHOICES = "app.container_console_choices"
        const val APP_CONTAINER_IDS = "app.container_ids"
        const val APP_CONVERT_TO_CUSTOM = "app.convert_to_custom"

        const val REGISTRY_CREATE = "app.registry.create"
        const val REGISTRY_DELETE = "app.registry.delete"
        const val REGISTRY_GET_INSTANCE = "app.registry.get_instance"
        const val REGISTRY_QUERY = "app.registry.query"
        const val REGISTRY_UPDATE = "app.registry.update"

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
    object Events {
        const val APP_STATS = "app.stats"
        const val COLLECTION_UPDATE = "collection_update"
    }
    object Docker {
        const val CONFIG = "docker.config"
        const val STATUS = "docker.status"
        const val UPDATE = "docker.update"
        const val NETWORK_QUERY = "docker.network.query"
        const val NETWORK_GET_INSTANCE = "docker.network.get_instance"
        const val NVIDIA_PRESENT = "docker.nvidia_present"
        const val BACKUP = "docker.backup"
        const val BACKUP_TO_POOL = "docker.backup_to_pool"
        const val LIST_BACKUPS = "docker.list_backups"
        const val DELETE_BACKUP = "docker.delete_backup"
        const val RESTORE_BACKUP = "docker.restore_backup"
    }
    object Virt{
        const val GET_ALL_INSTANCES = "virt.instance.query"
        const val START_INSTANCE = "virt.instance.start"
        const val STOP_INSTANCE = "virt.instance.stop"
        const val RESTART_INSTANCE = "virt.instance.restart"
        const val DELETE_INSTANCE = "virt.instance.delete"
        const val UPDATE_INSTANCE = "virt.instance.update"
        const val DELETE_INSTANCE_DEVICE = "virt.instance.device_delete"

        const val GET_IMAGE_CHOICES = "virt.instance.image_choices"
        const val CREATE_INSTANCE = "virt.instance.create"
        const val GET_INSTANCE = "virt.instance.get_instance"
        const val DEVICE_ADD = "virt.instance.device_add"
        const val DEVICE_LIST = "virt.instance.device_list"
        const val DEVICE_UPDATE = "virt.instance.device_update"
        const val SET_BOOTABLE_DISK = "virt.instance.set_bootable_disk"
        const val DEVICE_DISK_CHOICES = "virt.device.disk_choices"
        const val DEVICE_GPU_CHOICES = "virt.device.gpu_choices"
        const val DEVICE_NIC_CHOICES = "virt.device.nic_choices"
        const val DEVICE_PCI_CHOICES = "virt.device.pci_choices"
        const val DEVICE_USB_CHOICES = "virt.device.usb_choices"
        const val DEVICE_EXPORT_DISK_IMAGE = "virt.device.export_disk_image"
        const val DEVICE_IMPORT_DISK_IMAGE = "virt.device.import_disk_image"
        const val GLOBAL_CONFIG = "virt.global.config"
        const val GLOBAL_UPDATE = "virt.global.update"
        const val GLOBAL_GET_NETWORK = "virt.global.get_network"
        const val GLOBAL_BRIDGE_CHOICES = "virt.global.bridge_choices"
        const val GLOBAL_POOL_CHOICES = "virt.global.pool_choices"
        const val VOLUME_QUERY = "virt.volume.query"
        const val VOLUME_CREATE = "virt.volume.create"
        const val VOLUME_GET_INSTANCE = "virt.volume.get_instance"
        const val VOLUME_UPDATE = "virt.volume.update"
        const val VOLUME_DELETE = "virt.volume.delete"
        const val VOLUME_IMPORT_ISO = "virt.volume.import_iso"
        const val VOLUME_IMPORT_ZVOL = "virt.volume.import_zvol"
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
        const val VM_BOOTLOADER_OPTIONS = "vm.bootloader_options"
        const val VM_BOOTLOADER_OVMF_CHOICES = "vm.bootloader_ovmf_choices"
        const val VM_CPU_MODEL_CHOICES = "vm.cpu_model_choices"
        const val VM_CREATE = "vm.create"
        const val VM_DEVICE = "vm.device"
        const val VM_DEVICE_BIND_CHOICES = "vm.device.bind_choices"
        const val VM_DEVICE_CONVERT = "vm.device.convert"
        const val VM_DEVICE_CREATE = "vm.device.create"
        const val VM_DEVICE_DELETE = "vm.device.delete"
        const val VM_DEVICE_DISK_CHOICES = "vm.device.disk_choices"
        const val VM_DEVICE_GET_INSTANCE = "vm.device.get_instance"
        const val VM_DEVICE_IOMMU_ENABLED = "vm.device.iommu_enabled"
        const val VM_DEVICE_IOTYPE_CHOICES = "vm.device.iotype_choices"
        const val VM_DEVICE_NIC_ATTACH_CHOICES = "vm.device.nic_attach_choices"
        const val VM_DEVICE_PASSTHROUGH_DEVICE = "vm.device.passthrough_device"
        const val VM_DEVICE_PASSTHROUGH_DEVICE_CHOICES = "vm.device.passthrough_device_choices"
        const val VM_DEVICE_PPTDEV_CHOICES = "vm.device.pptdev_choices"
        const val VM_DEVICE_QUERY = "vm.device.query"
        const val VM_DEVICE_UPDATE = "vm.device.update"
        const val VM_DEVICE_USB_CONTROLLER_CHOICES = "vm.device.usb_controller_choices"
        const val VM_DEVICE_USB_PASSTHROUGH_CHOICES = "vm.device.usb_passthrough_choices"
        const val VM_DEVICE_USB_PASSTHROUGH_DEVICE = "vm.device.usb_passthrough_device"
        const val VM_DEVICE_VIRTUAL_SIZE = "vm.device.virtual_size"
        const val VM_EXPORT_DISK_IMAGE = "vm.export_disk_image"
        const val VM_FLAGS = "vm.flags"
        const val VM_GET_AVAILABLE_MEMORY = "vm.get_available_memory"
        const val VM_GET_CONSOLE = "vm.get_console"
        const val VM_GET_DISPLAY_DEVICES = "vm.get_display_devices"
        const val VM_GET_VM_MEMORY_INFO = "vm.get_vm_memory_info"
        const val VM_GET_VMEMORY_IN_USE = "vm.get_vmemory_in_use"
        const val VM_GUEST_ARCHITECTURE_AND_MACHINE_CHOICES = "vm.guest_architecture_and_machine_choices"
        const val VM_IMPORT_DISK_IMAGE = "vm.import_disk_image"
        const val VM_LOG_FILE_DOWNLOAD = "vm.log_file_download"
        const val VM_LOG_FILE_PATH = "vm.log_file_path"
        const val VM_MAXIMUM_SUPPORTED_VCPUS = "vm.maximum_supported_vcpus"
        const val VM_PORT_WIZARD = "vm.port_wizard"
        const val VM_RANDOM_MAC = "vm.random_mac"
        const val VM_RESOLUTION_CHOICES = "vm.resolution_choices"
        const val VM_SUPPORTS_VIRTUALIZATION = "vm.supports_virtualization"
        const val VM_UPDATE = "vm.update"
        const val VM_VIRTUALIZATION_DETAILS = "vm.virtualization_details"
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
        const val CREATE_NFS_SHARE = "sharing.nfs.create"
        const val DELETE_NFS_SHARE = "sharing.nfs.delete"
        const val GET_NFS_SHARE_INSTANCE = "sharing.nfs.get_instance"
        const val UPDATE_NFS_SHARE = "sharing.nfs.update"
        const val CREATE_SMB_SHARE = "sharing.smb.create"
        const val DELETE_SMB_SHARE = "sharing.smb.delete"
        const val GET_SMB_SHARE_INSTANCE = "sharing.smb.get_instance"
        const val GET_SMB_SHARE_ACL = "sharing.smb.getacl"
        const val SMB_SHARE_PRESETS = "sharing.smb.presets"
        const val SET_SMB_SHARE_ACL = "sharing.smb.setacl"
        const val SMB_SHARE_PRECHECK = "sharing.smb.share_precheck"
        const val UPDATE_SMB_SHARE = "sharing.smb.update"
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

    object Disk {
        const val DISK_DETAILS = "disk.details"
        const val DISK_GET_INSTANCE = "disk.get_instance"
        const val DISK_GET_USED = "disk.get_used"
        const val DISK_TEMPERATURE_AGG = "disk.temperature_agg"
        const val DISK_TEMPERATURE_ALERTS = "disk.temperature_alerts"
        const val DISK_TEMPERATURES = "disk.temperatures"
        const val DISK_UPDATE = "disk.update"
        const val DISK_WIPE = "disk.wipe"
    }

    object Reporting {
        const val REPORTING_CONFIG = "reporting.config"
        const val REPORTING_EXPORTERS = "reporting.exporters"
        const val REPORTING_EXPORTERS_CREATE = "reporting.exporters.create"
        const val REPORTING_EXPORTERS_DELETE = "reporting.exporters.delete"
        const val REPORTING_EXPORTERS_EXPORTER_SCHEMAS = "reporting.exporters.exporter_schemas"
        const val REPORTING_EXPORTERS_GET_INSTANCE = "reporting.exporters.get_instance"
        const val REPORTING_EXPORTERS_QUERY = "reporting.exporters.query"
        const val REPORTING_EXPORTERS_UPDATE = "reporting.exporters.update"
        const val REPORTING_GRAPH = "reporting.graph"
        const val REPORTING_NETDATA_GET_DATA = "reporting.netdata_get_data"
        const val REPORTING_NETDATA_GRAPH = "reporting.netdata_graph"
        const val REPORTING_NETDATA_GRAPHS = "reporting.netdata_graphs"
        const val REPORTING_UPDATE = "reporting.update"
    }

    object Filesystem {
        const val FILESYSTEM_ACLTEMPLATE = "filesystem.acltemplate"
        const val FILESYSTEM_ACLTEMPLATE_BY_PATH = "filesystem.acltemplate.by_path"
        const val FILESYSTEM_ACLTEMPLATE_CREATE = "filesystem.acltemplate.create"
        const val FILESYSTEM_ACLTEMPLATE_DELETE = "filesystem.acltemplate.delete"
        const val FILESYSTEM_ACLTEMPLATE_GET_INSTANCE = "filesystem.acltemplate.get_instance"
        const val FILESYSTEM_ACLTEMPLATE_QUERY = "filesystem.acltemplate.query"
        const val FILESYSTEM_ACLTEMPLATE_UPDATE = "filesystem.acltemplate.update"
        const val FILESYSTEM_CHOWN = "filesystem.chown"
        const val FILESYSTEM_GET = "filesystem.get"
        const val FILESYSTEM_GET_ZFS_ATTRIBUTES = "filesystem.get_zfs_attributes"
        const val FILESYSTEM_GETACL = "filesystem.getacl"
        const val FILESYSTEM_LISTDIR = "filesystem.listdir"
        const val FILESYSTEM_PUT = "filesystem.put"
        const val FILESYSTEM_SET_ZFS_ATTRIBUTES = "filesystem.set_zfs_attributes"
        const val FILESYSTEM_SETACL = "filesystem.setacl"
        const val FILESYSTEM_SETPERM = "filesystem.setperm"
    }

    object Ipmi {
        const val IPMI_CHASSIS = "ipmi.chassis"
        const val IPMI_CHASSIS_IDENTIFY = "ipmi.chassis.identify"
        const val IPMI_CHASSIS_INFO = "ipmi.chassis.info"
        const val IPMI_IS_LOADED = "ipmi.is_loaded"
        const val IPMI_LAN = "ipmi.lan"
        const val IPMI_LAN_CHANNELS = "ipmi.lan.channels"
        const val IPMI_LAN_QUERY = "ipmi.lan.query"
        const val IPMI_LAN_UPDATE = "ipmi.lan.update"
        const val IPMI_SEL = "ipmi.sel"
        const val IPMI_SEL_CLEAR = "ipmi.sel.clear"
        const val IPMI_SEL_ELIST = "ipmi.sel.elist"
        const val IPMI_SEL_INFO = "ipmi.sel.info"
    }

    object Iscsi {
        const val ISCSI_AUTH = "iscsi.auth"
        const val ISCSI_AUTH_CREATE = "iscsi.auth.create"
        const val ISCSI_AUTH_DELETE = "iscsi.auth.delete"
        const val ISCSI_AUTH_GET_INSTANCE = "iscsi.auth.get_instance"
        const val ISCSI_AUTH_QUERY = "iscsi.auth.query"
        const val ISCSI_AUTH_UPDATE = "iscsi.auth.update"
        const val ISCSI_EXTENT = "iscsi.extent"
        const val ISCSI_EXTENT_CREATE = "iscsi.extent.create"
        const val ISCSI_EXTENT_DELETE = "iscsi.extent.delete"
        const val ISCSI_EXTENT_DISK_CHOICES = "iscsi.extent.disk_choices"
        const val ISCSI_EXTENT_GET_INSTANCE = "iscsi.extent.get_instance"
        const val ISCSI_EXTENT_QUERY = "iscsi.extent.query"
        const val ISCSI_EXTENT_UPDATE = "iscsi.extent.update"
        const val ISCSI_GLOBAL = "iscsi.global"
        const val ISCSI_GLOBAL_ALUA_ENABLED = "iscsi.global.alua_enabled"
        const val ISCSI_GLOBAL_CLIENT_COUNT = "iscsi.global.client_count"
        const val ISCSI_GLOBAL_CONFIG = "iscsi.global.config"
        const val ISCSI_GLOBAL_ISER_ENABLED = "iscsi.global.iser_enabled"
        const val ISCSI_GLOBAL_SESSIONS = "iscsi.global.sessions"
        const val ISCSI_GLOBAL_UPDATE = "iscsi.global.update"
        const val ISCSI_INITIATOR = "iscsi.initiator"
        const val ISCSI_INITIATOR_CREATE = "iscsi.initiator.create"
        const val ISCSI_INITIATOR_DELETE = "iscsi.initiator.delete"
        const val ISCSI_INITIATOR_GET_INSTANCE = "iscsi.initiator.get_instance"
        const val ISCSI_INITIATOR_QUERY = "iscsi.initiator.query"
        const val ISCSI_INITIATOR_UPDATE = "iscsi.initiator.update"
        const val ISCSI_PORTAL = "iscsi.portal"
        const val ISCSI_PORTAL_CREATE = "iscsi.portal.create"
        const val ISCSI_PORTAL_DELETE = "iscsi.portal.delete"
        const val ISCSI_PORTAL_GET_INSTANCE = "iscsi.portal.get_instance"
        const val ISCSI_PORTAL_LISTEN_IP_CHOICES = "iscsi.portal.listen_ip_choices"
        const val ISCSI_PORTAL_QUERY = "iscsi.portal.query"
        const val ISCSI_PORTAL_UPDATE = "iscsi.portal.update"
        const val ISCSI_TARGET = "iscsi.target"
        const val ISCSI_TARGET_CREATE = "iscsi.target.create"
        const val ISCSI_TARGET_DELETE = "iscsi.target.delete"
        const val ISCSI_TARGET_GET_INSTANCE = "iscsi.target.get_instance"
        const val ISCSI_TARGET_QUERY = "iscsi.target.query"
        const val ISCSI_TARGET_UPDATE = "iscsi.target.update"
        const val ISCSI_TARGET_VALIDATE_NAME = "iscsi.target.validate_name"
        const val ISCSI_TARGETEXTENT = "iscsi.targetextent"
        const val ISCSI_TARGETEXTENT_CREATE = "iscsi.targetextent.create"
        const val ISCSI_TARGETEXTENT_DELETE = "iscsi.targetextent.delete"
        const val ISCSI_TARGETEXTENT_GET_INSTANCE = "iscsi.targetextent.get_instance"
        const val ISCSI_TARGETEXTENT_QUERY = "iscsi.targetextent.query"
        const val ISCSI_TARGETEXTENT_UPDATE = "iscsi.targetextent.update"
    }

    object Kerberos {
        const val KERBEROS_CONFIG = "kerberos.config"
        const val KERBEROS_KEYTAB = "kerberos.keytab"
        const val KERBEROS_KEYTAB_CREATE = "kerberos.keytab.create"
        const val KERBEROS_KEYTAB_DELETE = "kerberos.keytab.delete"
        const val KERBEROS_KEYTAB_GET_INSTANCE = "kerberos.keytab.get_instance"
        const val KERBEROS_KEYTAB_QUERY = "kerberos.keytab.query"
        const val KERBEROS_KEYTAB_UPDATE = "kerberos.keytab.update"
        const val KERBEROS_REALM = "kerberos.realm"
        const val KERBEROS_REALM_CREATE = "kerberos.realm.create"
        const val KERBEROS_REALM_DELETE = "kerberos.realm.delete"
        const val KERBEROS_REALM_GET_INSTANCE = "kerberos.realm.get_instance"
        const val KERBEROS_REALM_QUERY = "kerberos.realm.query"
        const val KERBEROS_REALM_UPDATE = "kerberos.realm.update"
        const val KERBEROS_UPDATE = "kerberos.update"
    }

    object Core {
        const val PING = "core.ping"
        const val GET_JOBS = "core.get_jobs"
        const val JOB_ABORT = "core.job_abort"
        const val DOWNLOAD = "core.download"
        const val ARP = "core.arp"
        const val BULK = "core.bulk"
        const val DEBUG = "core.debug"
        const val GET_METHODS = "core.get_methods"
        const val GET_SERVICES = "core.get_services"
        const val JOB_DOWNLOAD_LOGS = "core.job_download_logs"
        const val JOB_WAIT = "core.job_wait"
        const val PING_REMOTE = "core.ping_remote"
        const val RESIZE_SHELL = "core.resize_shell"
        const val SET_OPTIONS = "core.set_options"
        const val SUBSCRIBE = "core.subscribe"
        const val UNSUBSCRIBE = "core.unsubscribe"
    }
    object Cloudsync {
        const val QUERY = "cloudsync.query"
        const val GET_INSTANCE = "cloudsync.get_instance"
        const val CREATE = "cloudsync.create"
        const val UPDATE = "cloudsync.update"
        const val DELETE = "cloudsync.delete"
        const val ABORT = "cloudsync.abort"
        const val SYNC = "cloudsync.sync"
        const val SYNC_ONETIME = "cloudsync.sync_onetime"
        const val RESTORE = "cloudsync.restore"
        const val LIST_DIRECTORY = "cloudsync.list_directory"
        const val LIST_BUCKETS = "cloudsync.list_buckets"
        const val CREATE_BUCKET = "cloudsync.create_bucket"
        const val ONEDRIVE_LIST_DRIVES = "cloudsync.onedrive_list_drives"
        const val PROVIDERS = "cloudsync.providers"
        const val CREDENTIALS_QUERY = "cloudsync.credentials.query"
        const val CREDENTIALS_GET_INSTANCE = "cloudsync.credentials.get_instance"
        const val CREDENTIALS_CREATE = "cloudsync.credentials.create"
        const val CREDENTIALS_UPDATE = "cloudsync.credentials.update"
        const val CREDENTIALS_DELETE = "cloudsync.credentials.delete"
        const val CREDENTIALS_VERIFY = "cloudsync.credentials.verify"
    }

    object Pool {
        const val POOL_ATTACH = "pool.attach"
        const val POOL_ATTACHMENTS = "pool.attachments"
        const val POOL_CREATE = "pool.create"
        const val POOL_DATASET = "pool.dataset"
        const val POOL_DATASET_ATTACHMENTS = "pool.dataset.attachments"
        const val POOL_DATASET_CHANGE_KEY = "pool.dataset.change_key"
        const val POOL_DATASET_CHECKSUM_CHOICES = "pool.dataset.checksum_choices"
        const val POOL_DATASET_COMPRESSION_CHOICES = "pool.dataset.compression_choices"
        const val POOL_DATASET_ENCRYPTION_ALGORITHM_CHOICES = "pool.dataset.encryption_algorithm_choices"
        const val POOL_DATASET_ENCRYPTION_SUMMARY = "pool.dataset.encryption_summary"
        const val POOL_DATASET_EXPORT_KEY = "pool.dataset.export_key"
        const val POOL_DATASET_EXPORT_KEYS = "pool.dataset.export_keys"
        const val POOL_DATASET_EXPORT_KEYS_FOR_REPLICATION = "pool.dataset.export_keys_for_replication"
        const val POOL_DATASET_GET_INSTANCE = "pool.dataset.get_instance"
        const val POOL_DATASET_GET_QUOTA = "pool.dataset.get_quota"
        const val POOL_DATASET_INHERIT_PARENT_ENCRYPTION_PROPERTIES = "pool.dataset.inherit_parent_encryption_properties"
        const val POOL_DATASET_LOCK = "pool.dataset.lock"
        const val POOL_DATASET_PROCESSES = "pool.dataset.processes"
        const val POOL_DATASET_PROMOTE = "pool.dataset.promote"
        const val POOL_DATASET_RECOMMENDED_ZVOL_BLOCKSIZE = "pool.dataset.recommended_zvol_blocksize"
        const val POOL_DATASET_RECORDSIZE_CHOICES = "pool.dataset.recordsize_choices"
        const val POOL_DATASET_RENAME = "pool.dataset.rename"
        const val POOL_DATASET_SET_QUOTA = "pool.dataset.set_quota"
        const val POOL_DATASET_SNAPSHOT_COUNT = "pool.dataset.snapshot_count"
        const val POOL_DATASET_UNLOCK = "pool.dataset.unlock"
        const val POOL_DATASET_UPDATE = "pool.dataset.update"
        const val POOL_DDT_PREFETCH = "pool.ddt_prefetch"
        const val POOL_DDT_PRUNE = "pool.ddt_prune"
        const val POOL_DETACH = "pool.detach"
        const val POOL_EXPAND = "pool.expand"
        const val POOL_EXPORT = "pool.export"
        const val POOL_FILESYSTEM_CHOICES = "pool.filesystem_choices"
        const val POOL_GET_DISKS = "pool.get_disks"
        const val POOL_GET_INSTANCE = "pool.get_instance"
        const val POOL_IMPORT_FIND = "pool.import_find"
        const val POOL_IMPORT_POOL = "pool.import_pool"
        const val POOL_IS_UPGRADED = "pool.is_upgraded"
        const val POOL_OFFLINE = "pool.offline"
        const val POOL_ONLINE = "pool.online"
        const val POOL_PROCESSES = "pool.processes"
        const val POOL_REMOVE = "pool.remove"
        const val POOL_REPLACE = "pool.replace"
        const val POOL_RESILVER = "pool.resilver"
        const val POOL_RESILVER_CONFIG = "pool.resilver.config"
        const val POOL_RESILVER_UPDATE = "pool.resilver.update"
        const val POOL_SCRUB = "pool.scrub"
        const val POOL_SNAPSHOT = "pool.snapshot"
        const val POOL_SNAPSHOT_CLONE = "pool.snapshot.clone"
        const val POOL_SNAPSHOT_CREATE = "pool.snapshot.create"
        const val POOL_SNAPSHOT_DELETE = "pool.snapshot.delete"
        const val POOL_SNAPSHOT_GET_INSTANCE = "pool.snapshot.get_instance"
        const val POOL_SNAPSHOT_HOLD = "pool.snapshot.hold"
        const val POOL_SNAPSHOT_QUERY = "pool.snapshot.query"
        const val POOL_SNAPSHOT_RELEASE = "pool.snapshot.release"
        const val POOL_SNAPSHOT_RENAME = "pool.snapshot.rename"
        const val POOL_SNAPSHOT_ROLLBACK = "pool.snapshot.rollback"
        const val POOL_SNAPSHOT_UPDATE = "pool.snapshot.update"
        const val POOL_SNAPSHOTTASK = "pool.snapshottask"
        const val POOL_SNAPSHOTTASK_MAX_COUNT = "pool.snapshottask.max_count"
        const val POOL_SNAPSHOTTASK_MAX_TOTAL_COUNT = "pool.snapshottask.max_total_count"
        const val POOL_UPDATE = "pool.update"
        const val POOL_UPGRADE = "pool.upgrade"
        const val POOL_VALIDATE_NAME = "pool.validate_name"
    }
    object Interface {
        const val INTERFACE_BRIDGE_MEMBERS_CHOICES = "interface.bridge_members_choices"
        const val INTERFACE_CANCEL_ROLLBACK = "interface.cancel_rollback"
        const val INTERFACE_CHECKIN = "interface.checkin"
        const val INTERFACE_CHECKIN_WAITING = "interface.checkin_waiting"
        const val INTERFACE_CHOICES = "interface.choices"
        const val INTERFACE_COMMIT = "interface.commit"
        const val INTERFACE_CREATE = "interface.create"
        const val INTERFACE_DELETE = "interface.delete"
        const val INTERFACE_GET_INSTANCE = "interface.get_instance"
        const val INTERFACE_HAS_PENDING_CHANGES = "interface.has_pending_changes"
        const val INTERFACE_IP_IN_USE = "interface.ip_in_use"
        const val INTERFACE_LACPDU_RATE_CHOICES = "interface.lacpdu_rate_choices"
        const val INTERFACE_LAG_PORTS_CHOICES = "interface.lag_ports_choices"
        const val INTERFACE_NETWORK_CONFIG_TO_BE_REMOVED = "interface.network_config_to_be_removed"
        const val INTERFACE_QUERY = "interface.query"
        const val INTERFACE_ROLLBACK = "interface.rollback"
        const val INTERFACE_SAVE_NETWORK_CONFIG = "interface.save_network_config"
        const val INTERFACE_SERVICES_RESTARTED_ON_SYNC = "interface.services_restarted_on_sync"
        const val INTERFACE_UPDATE = "interface.update"
        const val INTERFACE_VLAN_PARENT_INTERFACE_CHOICES = "interface.vlan_parent_interface_choices"
        const val INTERFACE_WEBSOCKET_INTERFACE = "interface.websocket_interface"
        const val INTERFACE_WEBSOCKET_LOCAL_IP = "interface.websocket_local_ip"
        const val INTERFACE_XMIT_HASH_POLICY_CHOICES = "interface.xmit_hash_policy_choices"
    }
    object Acme {
        const val ACME_DNS_AUTHENTICATOR_AUTHENTICATOR_SCHEMAS = "acme.dns.authenticator.authenticator_schemas"
        const val ACME_DNS_AUTHENTICATOR_CREATE = "acme.dns.authenticator.create"
        const val ACME_DNS_AUTHENTICATOR_DELETE = "acme.dns.authenticator.delete"
        const val ACME_DNS_AUTHENTICATOR_GET_INSTANCE = "acme.dns.authenticator.get_instance"
        const val ACME_DNS_AUTHENTICATOR_QUERY = "acme.dns.authenticator.query"
        const val ACME_DNS_AUTHENTICATOR_UPDATE = "acme.dns.authenticator.update"
    }

    object Config {
        const val CONFIG_RESET = "config.reset"
        const val CONFIG_SAVE = "config.save"
        const val CONFIG_UPLOAD = "config.upload"
    }

    object Cronjob {
        const val CRONJOB_CREATE = "cronjob.create"
        const val CRONJOB_DELETE = "cronjob.delete"
        const val CRONJOB_GET_INSTANCE = "cronjob.get_instance"
        const val CRONJOB_QUERY = "cronjob.query"
        const val CRONJOB_RUN = "cronjob.run"
        const val CRONJOB_UPDATE = "cronjob.update"
    }

    object Device {
        const val DEVICE_GET_INFO = "device.get_info"
    }

    object Directoryservices {
        const val DIRECTORYSERVICES_CACHE_REFRESH = "directoryservices.cache_refresh"
        const val DIRECTORYSERVICES_CERTIFICATE_CHOICES = "directoryservices.certificate_choices"
        const val DIRECTORYSERVICES_CONFIG = "directoryservices.config"
        const val DIRECTORYSERVICES_LEAVE = "directoryservices.leave"
        const val DIRECTORYSERVICES_STATUS = "directoryservices.status"
        const val DIRECTORYSERVICES_SYNC_KEYTAB = "directoryservices.sync_keytab"
        const val DIRECTORYSERVICES_UPDATE = "directoryservices.update"
    }

    object Dns {
        const val DNS_QUERY = "dns.query"
    }

    object Enclosure {
        const val ENCLOSURE_LABEL_SET = "enclosure.label.set"
    }

    object Failover {
        const val FAILOVER_DISABLED_REASONS = "failover.disabled.reasons"
        const val FAILOVER_REBOOT_INFO = "failover.reboot.info"
        const val FAILOVER_REBOOT_OTHER_NODE = "failover.reboot.other_node"
    }

    object Fc {
        const val FC_FC_HOST_CREATE = "fc.fc_host.create"
        const val FC_FC_HOST_DELETE = "fc.fc_host.delete"
        const val FC_FC_HOST_GET_INSTANCE = "fc.fc_host.get_instance"
        const val FC_FC_HOST_QUERY = "fc.fc_host.query"
        const val FC_FC_HOST_UPDATE = "fc.fc_host.update"
    }

    object Fcport {
        const val FCPORT_CREATE = "fcport.create"
        const val FCPORT_DELETE = "fcport.delete"
        const val FCPORT_GET_INSTANCE = "fcport.get_instance"
        const val FCPORT_PORT_CHOICES = "fcport.port_choices"
        const val FCPORT_QUERY = "fcport.query"
        const val FCPORT_STATUS = "fcport.status"
        const val FCPORT_UPDATE = "fcport.update"
    }

    object Ftp {
        const val FTP_CONFIG = "ftp.config"
        const val FTP_UPDATE = "ftp.update"
    }

    object Hardware {
        const val HARDWARE_VIRTUALIZATION_VARIANT = "hardware.virtualization.variant"
    }

    object Idmap {
        const val IDMAP_CLEAR_IDMAP_CACHE = "idmap.clear_idmap_cache"
    }

    object Initshutdownscript {
        const val INITSHUTDOWNSCRIPT_CREATE = "initshutdownscript.create"
        const val INITSHUTDOWNSCRIPT_DELETE = "initshutdownscript.delete"
        const val INITSHUTDOWNSCRIPT_GET_INSTANCE = "initshutdownscript.get_instance"
        const val INITSHUTDOWNSCRIPT_QUERY = "initshutdownscript.query"
        const val INITSHUTDOWNSCRIPT_UPDATE = "initshutdownscript.update"
    }

    object Jbof {
        const val JBOF_CREATE = "jbof.create"
        const val JBOF_DELETE = "jbof.delete"
        const val JBOF_GET_INSTANCE = "jbof.get_instance"
        const val JBOF_LICENSED = "jbof.licensed"
        const val JBOF_QUERY = "jbof.query"
        const val JBOF_REAPPLY_CONFIG = "jbof.reapply_config"
        const val JBOF_UPDATE = "jbof.update"
    }

    object Kmip {
        const val KMIP_CLEAR_SYNC_PENDING_KEYS = "kmip.clear_sync_pending_keys"
        const val KMIP_CONFIG = "kmip.config"
        const val KMIP_KMIP_SYNC_PENDING = "kmip.kmip_sync_pending"
        const val KMIP_SYNC_KEYS = "kmip.sync_keys"
        const val KMIP_UPDATE = "kmip.update"
    }

    object Mail {
        const val MAIL_CONFIG = "mail.config"
        const val MAIL_LOCAL_ADMINISTRATOR_EMAIL = "mail.local_administrator_email"
        const val MAIL_SEND = "mail.send"
        const val MAIL_UPDATE = "mail.update"
    }

    object Nfs {
        const val NFS_BINDIP_CHOICES = "nfs.bindip_choices"
        const val NFS_CLIENT_COUNT = "nfs.client_count"
        const val NFS_CONFIG = "nfs.config"
        const val NFS_GET_NFS3_CLIENTS = "nfs.get_nfs3_clients"
        const val NFS_GET_NFS4_CLIENTS = "nfs.get_nfs4_clients"
        const val NFS_UPDATE = "nfs.update"
    }

    object Privilege {
        const val PRIVILEGE_CREATE = "privilege.create"
        const val PRIVILEGE_DELETE = "privilege.delete"
        const val PRIVILEGE_GET_INSTANCE = "privilege.get_instance"
        const val PRIVILEGE_QUERY = "privilege.query"
        const val PRIVILEGE_ROLES = "privilege.roles"
        const val PRIVILEGE_UPDATE = "privilege.update"
    }

    object Route {
        const val ROUTE_IPV4GW_REACHABLE = "route.ipv4gw_reachable"
        const val ROUTE_SYSTEM_ROUTES = "route.system_routes"
    }

    object Rsynctask {
        const val RSYNCTASK_CREATE = "rsynctask.create"
        const val RSYNCTASK_DELETE = "rsynctask.delete"
        const val RSYNCTASK_GET_INSTANCE = "rsynctask.get_instance"
        const val RSYNCTASK_QUERY = "rsynctask.query"
        const val RSYNCTASK_RUN = "rsynctask.run"
        const val RSYNCTASK_UPDATE = "rsynctask.update"
    }

    object Smb {
        const val SMB_BINDIP_CHOICES = "smb.bindip_choices"
        const val SMB_CONFIG = "smb.config"
        const val SMB_UNIXCHARSET_CHOICES = "smb.unixcharset_choices"
        const val SMB_UPDATE = "smb.update"
    }

    object Snmp {
        const val SNMP_CONFIG = "snmp.config"
        const val SNMP_UPDATE = "snmp.update"
    }

    object Ssh {
        const val SSH_BINDIFACE_CHOICES = "ssh.bindiface_choices"
        const val SSH_CONFIG = "ssh.config"
        const val SSH_UPDATE = "ssh.update"
    }

    object Staticroute {
        const val STATICROUTE_CREATE = "staticroute.create"
        const val STATICROUTE_DELETE = "staticroute.delete"
        const val STATICROUTE_GET_INSTANCE = "staticroute.get_instance"
        const val STATICROUTE_QUERY = "staticroute.query"
        const val STATICROUTE_UPDATE = "staticroute.update"
    }

    object Systemdataset {
        const val SYSTEMDATASET_CONFIG = "systemdataset.config"
        const val SYSTEMDATASET_POOL_CHOICES = "systemdataset.pool_choices"
        const val SYSTEMDATASET_UPDATE = "systemdataset.update"
    }

    object Tunable {
        const val TUNABLE_CREATE = "tunable.create"
        const val TUNABLE_DELETE = "tunable.delete"
        const val TUNABLE_GET_INSTANCE = "tunable.get_instance"
        const val TUNABLE_QUERY = "tunable.query"
        const val TUNABLE_TUNABLE_TYPE_CHOICES = "tunable.tunable_type_choices"
        const val TUNABLE_UPDATE = "tunable.update"
    }

    object Update {
        const val UPDATE_FILE = "update.file"
        const val UPDATE_MANUAL = "update.manual"
    }

    object Ups {
        const val UPS_CONFIG = "ups.config"
        const val UPS_DRIVER_CHOICES = "ups.driver_choices"
        const val UPS_PORT_CHOICES = "ups.port_choices"
        const val UPS_UPDATE = "ups.update"
    }

    object Zfs {
        const val ZFS_RESOURCE_QUERY = "zfs.resource.query"
    }
    object Certificate {
        const val CERTIFICATE_ACME_SERVER_CHOICES = "certificate.acme_server_choices"
        const val CERTIFICATE_COUNTRY_CHOICES = "certificate.country_choices"
        const val CERTIFICATE_CREATE = "certificate.create"
        const val CERTIFICATE_DELETE = "certificate.delete"
        const val CERTIFICATE_EC_CURVE_CHOICES = "certificate.ec_curve_choices"
        const val CERTIFICATE_EXTENDED_KEY_USAGE_CHOICES = "certificate.extended_key_usage_choices"
        const val CERTIFICATE_GET_INSTANCE = "certificate.get_instance"
        const val CERTIFICATE_QUERY = "certificate.query"
        const val CERTIFICATE_UPDATE = "certificate.update"
    }

    object Group {
        const val GROUP_CREATE = "group.create"
        const val GROUP_DELETE = "group.delete"
        const val GROUP_GET_GROUP_OBJ = "group.get_group_obj"
        const val GROUP_GET_INSTANCE = "group.get_instance"
        const val GROUP_GET_NEXT_GID = "group.get_next_gid"
        const val GROUP_HAS_PASSWORD_ENABLED_USER = "group.has_password_enabled_user"
        const val GROUP_QUERY = "group.query"
        const val GROUP_UPDATE = "group.update"
    }

    object Keychaincredential {
        const val KEYCHAINCREDENTIAL_CREATE = "keychaincredential.create"
        const val KEYCHAINCREDENTIAL_DELETE = "keychaincredential.delete"
        const val KEYCHAINCREDENTIAL_GENERATE_SSH_KEY_PAIR = "keychaincredential.generate_ssh_key_pair"
        const val KEYCHAINCREDENTIAL_GET_INSTANCE = "keychaincredential.get_instance"
        const val KEYCHAINCREDENTIAL_QUERY = "keychaincredential.query"
        const val KEYCHAINCREDENTIAL_REMOTE_SSH_HOST_KEY_SCAN = "keychaincredential.remote_ssh_host_key_scan"
        const val KEYCHAINCREDENTIAL_REMOTE_SSH_SEMIAUTOMATIC_SETUP = "keychaincredential.remote_ssh_semiautomatic_setup"
        const val KEYCHAINCREDENTIAL_SETUP_SSH_CONNECTION = "keychaincredential.setup_ssh_connection"
        const val KEYCHAINCREDENTIAL_UPDATE = "keychaincredential.update"
        const val KEYCHAINCREDENTIAL_USED_BY = "keychaincredential.used_by"
    }

    object Support {
        const val SUPPORT_ATTACH_TICKET = "support.attach_ticket"
        const val SUPPORT_ATTACH_TICKET_MAX_SIZE = "support.attach_ticket_max_size"
        const val SUPPORT_CONFIG = "support.config"
        const val SUPPORT_FIELDS = "support.fields"
        const val SUPPORT_IS_AVAILABLE = "support.is_available"
        const val SUPPORT_IS_AVAILABLE_AND_ENABLED = "support.is_available_and_enabled"
        const val SUPPORT_NEW_TICKET = "support.new_ticket"
        const val SUPPORT_SIMILAR_ISSUES = "support.similar_issues"
        const val SUPPORT_UPDATE = "support.update"
    }

    object Vmware {
        const val VMWARE_CREATE = "vmware.create"
        const val VMWARE_DATASET_HAS_VMS = "vmware.dataset_has_vms"
        const val VMWARE_DELETE = "vmware.delete"
        const val VMWARE_GET_DATASTORES = "vmware.get_datastores"
        const val VMWARE_GET_INSTANCE = "vmware.get_instance"
        const val VMWARE_MATCH_DATASTORES_WITH_DATASETS = "vmware.match_datastores_with_datasets"
        const val VMWARE_QUERY = "vmware.query"
        const val VMWARE_UPDATE = "vmware.update"
    }
    object CloudBackup {
        const val CLOUD_BACKUP_ABORT = "cloud_backup.abort"
        const val CLOUD_BACKUP_CREATE = "cloud_backup.create"
        const val CLOUD_BACKUP_DELETE = "cloud_backup.delete"
        const val CLOUD_BACKUP_DELETE_SNAPSHOT = "cloud_backup.delete_snapshot"
        const val CLOUD_BACKUP_GET_INSTANCE = "cloud_backup.get_instance"
        const val CLOUD_BACKUP_LIST_SNAPSHOT_DIRECTORY = "cloud_backup.list_snapshot_directory"
        const val CLOUD_BACKUP_LIST_SNAPSHOTS = "cloud_backup.list_snapshots"
        const val CLOUD_BACKUP_QUERY = "cloud_backup.query"
        const val CLOUD_BACKUP_RESTORE = "cloud_backup.restore"
        const val CLOUD_BACKUP_SYNC = "cloud_backup.sync"
        const val CLOUD_BACKUP_TRANSFER_SETTING_CHOICES = "cloud_backup.transfer_setting_choices"
        const val CLOUD_BACKUP_UPDATE = "cloud_backup.update"
    }

    object Nvmet {
        const val NVMET_GLOBAL_CONFIG = "nvmet.global.config"
        const val NVMET_GLOBAL_SESSIONS = "nvmet.global.sessions"
        const val NVMET_GLOBAL_UPDATE = "nvmet.global.update"
        const val NVMET_HOST_CREATE = "nvmet.host.create"
        const val NVMET_HOST_DELETE = "nvmet.host.delete"
        const val NVMET_HOST_DHCHAP_DHGROUP_CHOICES = "nvmet.host.dhchap_dhgroup_choices"
        const val NVMET_HOST_DHCHAP_HASH_CHOICES = "nvmet.host.dhchap_hash_choices"
        const val NVMET_HOST_GENERATE_KEY = "nvmet.host.generate_key"
        const val NVMET_HOST_GET_INSTANCE = "nvmet.host.get_instance"
        const val NVMET_HOST_QUERY = "nvmet.host.query"
        const val NVMET_HOST_UPDATE = "nvmet.host.update"
        const val NVMET_HOST_SUBSYS_CREATE = "nvmet.host_subsys.create"
        const val NVMET_HOST_SUBSYS_DELETE = "nvmet.host_subsys.delete"
        const val NVMET_HOST_SUBSYS_GET_INSTANCE = "nvmet.host_subsys.get_instance"
        const val NVMET_HOST_SUBSYS_QUERY = "nvmet.host_subsys.query"
        const val NVMET_HOST_SUBSYS_UPDATE = "nvmet.host_subsys.update"
        const val NVMET_NAMESPACE_CREATE = "nvmet.namespace.create"
        const val NVMET_NAMESPACE_DELETE = "nvmet.namespace.delete"
        const val NVMET_NAMESPACE_GET_INSTANCE = "nvmet.namespace.get_instance"
        const val NVMET_NAMESPACE_QUERY = "nvmet.namespace.query"
        const val NVMET_NAMESPACE_UPDATE = "nvmet.namespace.update"
        const val NVMET_PORT_CREATE = "nvmet.port.create"
        const val NVMET_PORT_DELETE = "nvmet.port.delete"
        const val NVMET_PORT_GET_INSTANCE = "nvmet.port.get_instance"
        const val NVMET_PORT_QUERY = "nvmet.port.query"
        const val NVMET_PORT_TRANSPORT_ADDRESS_CHOICES = "nvmet.port.transport_address_choices"
        const val NVMET_PORT_UPDATE = "nvmet.port.update"
        const val NVMET_PORT_SUBSYS_CREATE = "nvmet.port_subsys.create"
        const val NVMET_PORT_SUBSYS_DELETE = "nvmet.port_subsys.delete"
        const val NVMET_PORT_SUBSYS_GET_INSTANCE = "nvmet.port_subsys.get_instance"
        const val NVMET_PORT_SUBSYS_QUERY = "nvmet.port_subsys.query"
        const val NVMET_PORT_SUBSYS_UPDATE = "nvmet.port_subsys.update"
        const val NVMET_SUBSYS_CREATE = "nvmet.subsys.create"
        const val NVMET_SUBSYS_DELETE = "nvmet.subsys.delete"
        const val NVMET_SUBSYS_GET_INSTANCE = "nvmet.subsys.get_instance"
        const val NVMET_SUBSYS_QUERY = "nvmet.subsys.query"
        const val NVMET_SUBSYS_UPDATE = "nvmet.subsys.update"
    }

    object Replication {
        const val REPLICATION_CONFIG_CONFIG = "replication.config.config"
        const val REPLICATION_CONFIG_UPDATE = "replication.config.update"
        const val REPLICATION_COUNT_ELIGIBLE_MANUAL_SNAPSHOTS = "replication.count_eligible_manual_snapshots"
        const val REPLICATION_CREATE = "replication.create"
        const val REPLICATION_CREATE_DATASET = "replication.create_dataset"
        const val REPLICATION_DELETE = "replication.delete"
        const val REPLICATION_GET_INSTANCE = "replication.get_instance"
        const val REPLICATION_LIST_DATASETS = "replication.list_datasets"
        const val REPLICATION_LIST_NAMING_SCHEMAS = "replication.list_naming_schemas"
        const val REPLICATION_QUERY = "replication.query"
        const val REPLICATION_RESTORE = "replication.restore"
        const val REPLICATION_RUN = "replication.run"
        const val REPLICATION_RUN_ONETIME = "replication.run_onetime"
        const val REPLICATION_TARGET_UNMATCHED_SNAPSHOTS = "replication.target_unmatched_snapshots"
        const val REPLICATION_UPDATE = "replication.update"
    }
}