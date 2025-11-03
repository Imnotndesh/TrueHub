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
}