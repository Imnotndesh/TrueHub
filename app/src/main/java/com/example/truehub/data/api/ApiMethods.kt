package com.example.truehub.data.api

object ApiMethods {
    object Auth {
        const val AUTH_LOGIN = "auth.login"
        const val AUTH_API_LOGIN = "auth.login_with_api_key"
        const val AUTH_TOKEN_LOGIN = "auth.login_with_token"
        const val AUTH_LOGOUT = "auth.logout"
        const val AUTH_ME = "auth.me"
        const val GEN_AUTH_TOKEN = "auth.generate_token"

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
    }
    object Apps {
        const val QUERY_APPS = "app.query"
        const val START_APP = "app.start"
        const val STOP_APP = "app.stop"
        const val UPGRADE_APP = "app.upgrade"
    }
    object Virt{
        const val GET_ALL_INSTANCES = "virt.instance.query"
        const val START_INSTANCE = "virt.instance.start"
        const val STOP_INSTANCE = "virt.instance.stop"
        const val RESTART_INSTANCE = "virt.instance.restart"
        const val DELETE_INSTANCE = "virt.instance.delete"
        const val UPDATE_INSTANCE = "virt.instance.update"
        const val DELETE_INSTANCE_DEVICE = "virt.instance.device_delete"
    }
    object Vm{
        const val GET_ALL_VM_INSTANCES = "vm.query"
        const val START_VM_INSTANCE = "vm.instance.start"
        const val STOP_INSTANCE = "vm.stop"
        const val RESTART_INSTANCE = "vm.restart"
        const val DELETE_INSTANCE = "vm.instance.delete"
        const val SUSPEND_VM = "vm.suspend"
        const val RESUME_VM = "vm.resume"
        const val POWER_OFF_VM = "vm.poweroff"
        const val CLONE_VM = "vm.clone"
        const val GET_VM_MEMORY_USAGE = "vm.get_memory_usage"
        const val GET_INSTANCE = "vm.get_instance"
        const val GET_VM_STATUS = "vm.status"
        const val GET_DISPLAY_URL = "vm.get_display_web_uri"
    }
    object Shares{
        const val GET_SMB_SHARES = "vm.sharing.smb.query"
    }
}