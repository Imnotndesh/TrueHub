package com.imnotndesh.truehub.ui

import android.net.Uri

sealed class Screen(val route:String, val title:String) {
    object Home : Screen("home", "Home")
    object Apps : Screen("apps","Apps")
    object ServicesScreen : Screen("services", "TrueNAS Services")
    object ServicesDetailScreen: Screen("service_detail", "Details about TrueNAS services")
    object Containers : Screen ("containers", "Containers")
    object Vms : Screen("vms","VMs")
    object Login : Screen("login","Login")
    object Main: Screen("main","Main")
    object Performance : Screen("performance", "Performance")
    object Settings : Screen("settings","Settings")
    object Licenses : Screen("licenses","Licenses")
    object ShareInfo : Screen ("share_info","Exposed shares details")
    object CreateShare : Screen ("create_share","Create share")
    object About : Screen("about","About")
    object AppLogging : Screen("app_logging","App Logging")
    object Theme : Screen("theme","Theme")
    object AppUpdate : Screen("app_update", "App Updates")
    object AccountSwitcher : Screen("account_switcher","account_switcher")
    object PoolDetails : Screen("pool_details", "Pool Details")
    object Files : Screen("file_explorer","file_explorer")
    object Marketplace : Screen("marketplace", "Marketplace")
    object MarketplaceAppDetails : Screen("marketplace_app_details", "Marketplace App Details")
    object CreateContainer : Screen("create_container", "Create Container")
    object ContainerEdit : Screen("container_edit/{containerId}", "Edit Container") {
        fun createRoute(containerId: String): String = "container_edit/$containerId"
    }
    object ContainerDevices : Screen("container_devices/{containerId}", "Container Devices") {
        fun createRoute(containerId: String): String = "container_devices/$containerId"
    }
    object ContainerDeviceAdd : Screen("container_devices/{containerId}/add?deviceName={deviceName}", "Add Container Device") {
        fun createRoute(containerId: String, deviceName: String? = null): String {
            return if (deviceName.isNullOrBlank()) {
                "container_devices/$containerId/add"
            } else {
                "container_devices/$containerId/add?deviceName=${Uri.encode(deviceName)}"
            }
        }
    }
    object ContainerVolumes : Screen("container_volumes/{containerId}", "Container Volumes") {
        fun createRoute(containerId: String): String = "container_volumes/$containerId"
    }
    object ContainerInfo : Screen("container_info/{containerId}", "Container Info") {
        fun createRoute(containerId: String): String = "container_info/$containerId"
    }
    object VmDetails : Screen("vm_details/{vmId}", "VM Details") {
        fun createRoute(vmId: Int) = "vm_details/$vmId"
    }
    object VmCreate : Screen("vm_create", "Create VM")
    object VmEdit : Screen("vm_edit/{vmId}", "Edit VM") {
        fun createRoute(vmId: Int) = "vm_edit/$vmId"
    }
    object VmDevices : Screen("vm_devices/{vmId}", "VM Devices") {
        fun createRoute(vmId: Int) = "vm_devices/$vmId"
    }
    object VmConsole : Screen("vm_console/{vmId}", "VM Console") {
        fun createRoute(vmId: Int) = "vm_console/$vmId"
    }
    object VmDeviceAdd : Screen("vm_devices/{vmId}/add?deviceId={deviceId}", "Add VM Device") {
        fun createRoute(vmId: Int, deviceId: Int? = null) =
            if (deviceId == null) "vm_devices/$vmId/add" else "vm_devices/$vmId/add?deviceId=$deviceId"
    }
    object ChangePassword : Screen("change_password", "Change Password")
    object DiskInfo : Screen("disk_info", "Disk information")
    object AppConfigScreen : Screen("app_config", "Application config")
    object InstanceConfigScreen : Screen("instance_config", "TrueNAS Instance Configuration")
    object AppDetailsScreen : Screen("app_details", "Application details page")
    object AppAdvancedInfoScreen : Screen("app_advanced_info/{appId}", "Advanced Application Information") {
        fun createRoute(appId: String): String {
            return "app_advanced_info/$appId"
        }
    }
    object AppResourceUsage : Screen("app_resource_usage/{appId}", "App Resource Usage") {
        fun createRoute(appId: String): String {
            return "app_resource_usage/$appId"
        }
    }
    object SystemUpdateScreen : Screen("system_update", "TrueNAS Version Update")
    object MarketplaceCategory : Screen("marketplace?category={category}", "Marketplace Category") {
        fun createRoute(category: String): String {
            return "marketplace?category=$category"
        }
    }

    object AppUpgrade : Screen("app_upgrade/{appName}", "App Upgrade") {
        fun createRoute(appName: String): String {
            return "app_upgrade/$appName"
        }
    }

    object CatalogInstall : Screen("catalog_install/{appName}/{train}", "Catalog Install") {
        fun createRoute(appName: String, train: String): String {
            return "catalog_install/$appName/$train"
        }
    }

    object DatasetExplorer : Screen("${Files.route}/{poolName}", "Dataset Explorer") {
        fun createRoute(poolName: String): String {
            return "${Files.route}/$poolName"
        }
    }
    object RollbackVersion : Screen("rollback/{appName}", "Rollback App") {
        fun createRoute(appName: String): String {
            return "rollback/$appName"
        }
    }
    object AlertServicesList : Screen("alert_services_list","alert_services_list")
    object AlertServiceDetail : Screen("alert_service_detail/{serviceId}","alert_service_detail/{serviceId}") {
        fun createRoute(serviceId: Int) = "alert_service_detail/$serviceId"
    }

    object AlertServiceCreate : Screen("alert_service_create","alert_service_create")
    object AlertClassesConfig : Screen("alert_classes_config","\"alert_classes_config\"")
    object UserListScreen : Screen("user_list", "Users")
    object UserDetailScreen : Screen("user_detail/{userId}", "User Details") {
        fun createRoute(userId: Int) = "user_detail/$userId"
    }
    object UserCreateScreen : Screen("user_create", "Create User")
    object LocalAdminSetupScreen : Screen("local_admin_setup", "Setup Local Administrator")
    object ApiKeyListScreen : Screen("api_key_list", "API Keys")
    object ApiKeyDetailScreen : Screen("api_key_detail/{keyId}", "API Key Details") {
        fun createRoute(keyId: Int) = "api_key_detail/$keyId"
    }
    object ApiKeyCreateScreen : Screen("api_key_create", "Create API Key")
    object GeneralSystemSettingsScreen : Screen("general_system_settings", "General System Settings")
    object GeneralSystemSettingsEditScreen : Screen("general_system_settings_edit", "Edit General System Settings")
    object AdvancedSystemSettingsScreen : Screen("advanced_system_settings", "Advanced System Settings")
    object AdvancedSystemSettingsEditScreen : Screen("advanced_system_settings_edit", "Edit Advanced Settings")
    object AuditConfigScreen : Screen("audit_config", "Audit Configuration")
    object AuditLogsScreen : Screen("audit_logs", "Audit Logs")
    object NetworkScreen : Screen("network", "Network")
    object NetworkEditScreen : Screen("network_edit", "Edit Network Configuration")
    object BootScreen : Screen("boot", "Boot")
    object BootPoolScreen : Screen("boot_pool", "Boot Pool")
    object BootEnvironmentsScreen : Screen("boot_environments", "Boot Environments")
    object BootEnvironmentDetailScreen : Screen("boot_environment/{environmentId}", "Boot Environment") {
        fun createRoute(environmentId: String) = "boot_environment/$environmentId"
    }
    object SystemInformationScreen : Screen("system_information", "System Information")
    object CloudSync : Screen("cloud_sync", "Cloud Sync")
    object CloudSyncDetail : Screen("cloud_sync_detail/{taskId}", "Cloud Sync Task") {
        fun createRoute(taskId: Int) = "cloud_sync_detail/$taskId"
    }
    object CloudSyncForm : Screen("cloud_sync_form/{taskId}", "Cloud Sync Task") {
        fun createRoute(taskId: Int = -1) = "cloud_sync_form/$taskId"
    }
    object SoftwareInformationScreen : Screen("software_information", "Software Information")
    object HardwareInformationScreen : Screen("hardware_information", "Hardware Information")
    object TrueNasConnectScreen : Screen("truenas_connect", "TrueNAS Connect")
    object TrueCommandScreen : Screen("truecommand", "TrueCommand")
    object AppImageManagementScreen : Screen("app_image_management", "App Image Management")
    object GlobalVirtSettings : Screen("global_virt_settings", "Global Virt Settings")
    object IxVolumeListScreen : Screen("ix_volume_list", "iX Volumes")
    object DockerImageListScreen : Screen("docker_image_list", "Docker Images")

}