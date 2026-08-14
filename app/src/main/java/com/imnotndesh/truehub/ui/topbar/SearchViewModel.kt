package com.imnotndesh.truehub.ui.topbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.Screen
import com.imnotndesh.truehub.ui.utils.AppCache
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Defines all types of searchable items in the app.
 * Navigation: the [route] property tells the navigation handler where to go.
 * Data holders: relevant data is carried in the result for pre-loading before navigation.
 */
sealed class SearchResult {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val category: SearchCategory
    abstract val relevanceScore: Float
    /** The base route for navigation (without arguments). Arguments are carried in the result itself. */
    abstract val route: String

    // ── Storage ──────────────────────────────────────────────
    data class PoolResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val pool: System.Pool
    ) : SearchResult() {
        override val category = SearchCategory.STORAGE
        override val route = Screen.PoolDetails.route
    }

    data class DiskResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val disk: System.DiskDetails
    ) : SearchResult() {
        override val category = SearchCategory.STORAGE
        override val route = Screen.DiskInfo.route
    }

    // ── Services ─────────────────────────────────────────────
    data class ServiceResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val service: System.ServiceQueryResponse
    ) : SearchResult() {
        override val category = SearchCategory.SERVICES
        override val route = Screen.ServicesScreen.route
    }

    // ── Shares ───────────────────────────────────────────────
    data class ShareResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val share: Shares.SmbShare
    ) : SearchResult() {
        override val category = SearchCategory.SHARES
        override val route = Screen.ShareInfo.route
    }

    // ── System Info ──────────────────────────────────────────
    data class SystemInfoResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val info: String,
        override val route: String = Screen.SystemInformationScreen.route
    ) : SearchResult() {
        override val category = SearchCategory.SYSTEM
    }

    // ── Actions ──────────────────────────────────────────────
    data class ActionResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val action: AppAction,
        override val route: String = ""  // actions don't navigate to a screen
    ) : SearchResult() {
        override val category = SearchCategory.ACTIONS
    }

    // ── Navigation / Screen shortcuts ────────────────────────
    data class NavigationResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val destinationRoute: String,
        /** Optional data setter lambda — called before navigation to pre-load any needed data. */
        val preNavigate: (suspend (TrueNASApiManager) -> Unit)? = null
    ) : SearchResult() {
        override val category = SearchCategory.NAVIGATION
        override val route = destinationRoute
    }

    // ── Installed Apps ───────────────────────────────────────
    data class InstalledAppResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val app: Apps.AppQueryResponse
    ) : SearchResult() {
        override val category = SearchCategory.APPS
        override val route = Screen.AppDetailsScreen.route
    }

    // ── Marketplace / Catalog Apps ───────────────────────────
    data class MarketplaceAppResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val app: Apps.AppAvailableItem
    ) : SearchResult() {
        override val category = SearchCategory.MARKETPLACE
        override val route = Screen.MarketplaceAppDetails.route
    }

    // ── Containers ───────────────────────────────────────────
    data class ContainerResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val container: Virt.ContainerResponse
    ) : SearchResult() {
        override val category = SearchCategory.CONTAINERS
        override val route = Screen.ContainerInfo.route
    }

    // ── VMs ──────────────────────────────────────────────────
    data class VmResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val vm: Vm.VmQueryResponse
    ) : SearchResult() {
        override val category = SearchCategory.VMS
        override val route = Screen.VmDetails.route
    }

    // ── Instance Settings ────────────────────────────────────
    data class InstanceSettingsResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        override val route: String
    ) : SearchResult() {
        override val category = SearchCategory.INSTANCE_SETTINGS
    }
}

enum class SearchCategory(val displayName: String) {
    ALL("All"),
    NAVIGATION("Screens"),
    STORAGE("Storage"),
    SERVICES("Services"),
    SHARES("Shares"),
    SYSTEM("System Info"),
    ACTIONS("Actions"),
    APPS("Installed Apps"),
    MARKETPLACE("Marketplace"),
    CONTAINERS("Containers"),
    VMS("VMs"),
    INSTANCE_SETTINGS("Instance Settings")
}

enum class AppAction {
    SHUTDOWN,
    RESTART,
    REFRESH_DATA
}

data class SearchState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val recentSearches: List<String> = emptyList()
)

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(private val apiManager: TrueNASApiManager) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(apiManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Comprehensive app-wide search ViewModel.
 *
 * Searches across:
 *  1. Static screen/subsection registry (instant, always available)
 *  2. Cached data from AppCache: pools, disks, shares, installed apps, containers, VMs
 *  3. Marketplace API (debounced remote queries)
 */
@OptIn(FlowPreview::class)
class SearchViewModel(
    private val apiManager: TrueNASApiManager
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    // Cached marketplace data loaded once
    private var cachedMarketplaceApps: List<Apps.AppAvailableItem> = emptyList()
    private var marketplaceLoaded = false

    init {
        // Debounce local search
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
        // Read from AppCache immediately; lazy-load marketplace on first search
        refreshFromCache()
    }

    private fun refreshFromCache() {
        val cached = AppCache.cachedMarketplaceApps.value
        if (cached.isNotEmpty()) {
            cachedMarketplaceApps = cached
            marketplaceLoaded = true
        }
    }

    private fun ensureMarketplaceLoaded() {
        if (marketplaceLoaded) return
        viewModelScope.launch {
            try {
                val result = apiManager.apps.queryMarketplaceAvailableItems()
                if (result is ApiResult.Success) {
                    cachedMarketplaceApps = result.data
                    marketplaceLoaded = true
                }
            } catch (_: Exception) { /* keep going */ }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _searchState.value = _searchState.value.copy(
            query = query,
            isSearching = query.isNotEmpty()
        )
    }

    fun selectCategory(category: SearchCategory) {
        _searchState.value = _searchState.value.copy(selectedCategory = category)
        performSearch(_searchQuery.value)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = _searchState.value.copy(
            query = "",
            results = emptyList(),
            isSearching = false
        )
    }

    fun addToRecentSearches(query: String) {
        if (query.isBlank()) return
        val current = _searchState.value.recentSearches.toMutableList()
        current.remove(query)
        current.add(0, query)
        _searchState.value = _searchState.value.copy(
            recentSearches = current.take(10)
        )
    }

    fun removeRecentSearch(query: String) {
        val current = _searchState.value.recentSearches.toMutableList()
        current.remove(query)
        _searchState.value = _searchState.value.copy(recentSearches = current)
    }

    fun clearRecentSearches() {
        _searchState.value = _searchState.value.copy(recentSearches = emptyList())
    }

    /**
     * Flush caches and re-fetch from AppCache (called when data changes).
     */
    fun refreshCaches() {
        cachedMarketplaceApps = AppCache.cachedMarketplaceApps.value
        marketplaceLoaded = cachedMarketplaceApps.isNotEmpty()
    }

    // ═══════════════════════════════════════════════════════════
    //  SEARCH ENGINE
    // ═══════════════════════════════════════════════════════════

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchState.value = _searchState.value.copy(
                results = emptyList(),
                isSearching = false
            )
            return
        }

        viewModelScope.launch {
            val results = mutableListOf<SearchResult>()
            val lowerQuery = query.lowercase().trim()
            val selectedCategory = _searchState.value.selectedCategory

            // ── 1. Static Screen / Subsection Registry ────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.NAVIGATION) {
                results.addAll(searchScreenRegistry(lowerQuery))
            }

            // ── 1.5 Instance Settings registry ────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.INSTANCE_SETTINGS) {
                results.addAll(searchInstanceSettingsRegistry(lowerQuery))
            }

            // ── 2. Pools ──────────────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.STORAGE) {
                val pools = AppCache.cachedPools.value
                pools.forEach { pool ->
                    val relevance = calculateRelevance(lowerQuery, pool.name, pool.status_detail ?: "")
                    if (relevance > 0) {
                        results.add(
                            SearchResult.PoolResult(
                                id = "pool_${pool.id}",
                                title = pool.name,
                                subtitle = "Pool • ${formatBytes(pool.size ?: 0)}",
                                relevanceScore = relevance,
                                pool = pool
                            )
                        )
                    }
                }

                // Disks
                val disks = AppCache.cachedDisks.value
                disks.forEach { disk ->
                    val relevance = calculateRelevance(lowerQuery, disk.name, disk.model ?: "", disk.serial)
                    if (relevance > 0) {
                        results.add(
                            SearchResult.DiskResult(
                                id = "disk_${disk.name}",
                                title = disk.name,
                                subtitle = "Disk • ${disk.model ?: "Unknown"}",
                                relevanceScore = relevance,
                                disk = disk
                            )
                        )
                    }
                }

                // Shares
                if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SHARES) {
                    AppCache.cachedSmbShares.value.forEach { share ->
                        val relevance = calculateRelevance(lowerQuery, share.name, share.path, share.comment ?: "")
                        if (relevance > 0) {
                            results.add(
                                SearchResult.ShareResult(
                                    id = "share_${share.id}",
                                    title = share.name,
                                    subtitle = "SMB Share • ${share.path}",
                                    relevanceScore = relevance,
                                    share = share
                                )
                            )
                        }
                    }
                }
            }

            // ── 3. System Info ───────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SYSTEM) {
                AppCache.cachedSystemInfo.value?.let { info ->
                    listOf(
                        "hostname" to info.hostname,
                        "version" to info.version,
                        "uptime" to info.uptime,
                        "cpu cores" to "${info.cores.toInt()} cores"
                    ).forEach { (key, value) ->
                        val relevance = calculateRelevance(lowerQuery, key, value)
                        if (relevance > 0) {
                            results.add(
                                SearchResult.SystemInfoResult(
                                    id = "system_$key",
                                    title = key.replaceFirstChar { it.uppercase() },
                                    subtitle = "System • $value",
                                    relevanceScore = relevance,
                                    info = value
                                )
                            )
                        }
                    }
                }
            }

            // ── 3.5 Services ─────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SERVICES) {
                AppCache.cachedServices.value.forEach { svc ->
                    val relevance = calculateRelevance(lowerQuery, svc.service, svc.state)
                    if (relevance > 0) {
                        results.add(
                            SearchResult.ServiceResult(
                                id = "svc_${svc.id}",
                                title = svc.service.replaceFirstChar { it.uppercase() },
                                subtitle = "Service • ${if (svc.enable) "Running" else "Stopped"}",
                                relevanceScore = relevance,
                                service = svc
                            )
                        )
                    }
                }
            }

            // ── 4. Installed Apps ─────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.APPS) {
                val installedApps = AppCache.cachedApps.value
                installedApps.forEach { app ->
                    val metadata = app.metadata
                    val relevance = calculateRelevance(
                        lowerQuery,
                        app.name,
                        metadata?.title ?: "",
                        metadata?.description ?: "",
                        metadata?.categories?.joinToString(" ") ?: "",
                        metadata?.keywords?.joinToString(" ") ?: ""
                    )
                    if (relevance > 0) {
                        results.add(
                            SearchResult.InstalledAppResult(
                                id = "installed_${app.id}",
                                title = metadata?.title ?: app.name,
                                subtitle = "Installed App • v${app.version ?: "?"}",
                                relevanceScore = relevance,
                                app = app
                            )
                        )
                    }
                }
            }

            // ── 5. Marketplace Apps ───────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.MARKETPLACE) {
                ensureMarketplaceLoaded()
                cachedMarketplaceApps.forEach { app ->
                    val relevance = calculateRelevance(
                        lowerQuery,
                        app.name,
                        app.title,
                        app.description,
                        app.tags?.joinToString(" ") ?: "",
                        app.categories?.joinToString(" ") ?: ""
                    )
                    if (relevance > 0) {
                        val installedTag = if (app.installed) " • Installed" else ""
                        results.add(
                            SearchResult.MarketplaceAppResult(
                                id = "marketplace_${app.name}",
                                title = app.title.ifBlank { app.name },
                                subtitle = "Marketplace$installedTag • ${app.categories?.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "App"}",
                                relevanceScore = relevance,
                                app = app
                            )
                        )
                    }
                }
            }

            // ── 6. Containers ─────────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.CONTAINERS) {
                AppCache.cachedContainers.value.forEach { container ->
                    val relevance = calculateRelevance(
                        lowerQuery,
                        container.name,
                        container.status.name,
                        container.image.os ?: "",
                        container.image.release ?: ""
                    )
                    if (relevance > 0) {
                        val statusEmoji = when (container.status) {
                            Virt.Status.RUNNING -> " ● Running"
                            Virt.Status.STOPPED -> " ○ Stopped"
                            else -> " • ${container.status.name}"
                        }
                        results.add(
                            SearchResult.ContainerResult(
                                id = "container_${container.id}",
                                title = container.name,
                                subtitle = "Container$statusEmoji",
                                relevanceScore = relevance,
                                container = container
                            )
                        )
                    }
                }
            }

            // ── 7. VMs ────────────────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.VMS) {
                AppCache.cachedVms.value.forEach { vm ->
                    val relevance = calculateRelevance(
                        lowerQuery,
                        vm.name,
                        vm.description,
                        vm.status.state,
                        vm.arch_type ?: "",
                        "${vm.vcpus} vcpus",
                        "${vm.memory}MB"
                    )
                    if (relevance > 0) {
                        val state = vm.status.state.replaceFirstChar { it.uppercase() }
                        results.add(
                            SearchResult.VmResult(
                                id = "vm_${vm.id}",
                                title = vm.name,
                                subtitle = "VM • $state • ${vm.vcpus}vCPU, ${vm.memory}MB",
                                relevanceScore = relevance,
                                vm = vm
                            )
                        )
                    }
                }
            }

            // ── 8. Actions ────────────────────────────────────
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.ACTIONS) {
                val actions = listOf(
                    Triple("shutdown", "Shutdown system", AppAction.SHUTDOWN),
                    Triple("restart", "Restart system", AppAction.RESTART),
                    Triple("refresh", "Refresh data", AppAction.REFRESH_DATA)
                )
                actions.forEach { (key, description, action) ->
                    val relevance = calculateRelevance(lowerQuery, key, description)
                    if (relevance > 0) {
                        results.add(
                            SearchResult.ActionResult(
                                id = "action_$key",
                                title = description,
                                subtitle = "Action",
                                relevanceScore = relevance,
                                action = action
                            )
                        )
                    }
                }
            }

            // Sort by relevance descending
            val sortedResults = results.sortedByDescending { it.relevanceScore }

            _searchState.value = _searchState.value.copy(
                results = sortedResults,
                isSearching = false
            )
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  SCREEN REGISTRY — static list of every navigable screen
    // ═══════════════════════════════════════════════════════════

    private data class ScreenEntry(
        val id: String,
        val title: String,
        val subtitle: String,
        val route: String,
        val keywords: List<String>
    )

    private val screenRegistry: List<ScreenEntry> = listOf(
        // Top-level tabs
        ScreenEntry("nav_home", "Dashboard", "Home tab", Screen.Home.route,
            listOf("dashboard", "home", "overview", "main")),
        ScreenEntry("nav_apps", "Applications", "Installed applications", Screen.Apps.route,
            listOf("apps", "applications", "installed")),
        ScreenEntry("nav_containers", "Containers", "Virtual containers", Screen.Containers.route,
            listOf("containers", "docker", "lxc", "container")),
        ScreenEntry("nav_vms", "Virtual Machines", "Manage VMs", Screen.Vms.route,
            listOf("vms", "virtual machines", "kvm", "vm")),

        // Marketplace
        ScreenEntry("nav_marketplace", "Marketplace", "Discover and install apps", Screen.Marketplace.route,
            listOf("marketplace", "catalog", "discover", "store", "install")),

        // Settings & sub-screens
        ScreenEntry("nav_settings", "Settings", "App preferences", Screen.Settings.route,
            listOf("settings", "preferences", "config")),
        ScreenEntry("nav_about", "About", "About TrueHub", Screen.About.route,
            listOf("about", "version", "credits")),
        ScreenEntry("nav_theme", "Theme", "Change app theme", Screen.Theme.route,
            listOf("theme", "dark mode", "light mode", "appearance", "colors")),
        ScreenEntry("nav_licenses", "Licenses", "Open source licenses", Screen.Licenses.route,
            listOf("licenses", "open source", "oss")),

        // Home sub-screens
        ScreenEntry("nav_pools", "Storage Pools", "ZFS pools list", Screen.Home.route,
            listOf("pools", "storage", "zfs", "pool")),
        ScreenEntry("nav_disks", "Disks", "Disk information", Screen.DiskInfo.route,
            listOf("disks", "drives", "hard drives", "nvme", "ssd", "hdd")),
        ScreenEntry("nav_shares", "Shares", "SMB and NFS shares", Screen.Home.route,
            listOf("shares", "smb", "nfs", "file sharing", "samba")),
        ScreenEntry("nav_performance", "Performance", "CPU, Memory, Temperature metrics", Screen.Performance.route,
            listOf("performance", "cpu", "memory", "temperature", "metrics", "stats")),
        ScreenEntry("nav_system_update", "System Update", "Check and update TrueNAS", Screen.SystemUpdateScreen.route,
            listOf("update", "upgrade", "version", "system update")),
        ScreenEntry("nav_system_info", "System Information", "Hostname, version, uptime", Screen.SystemInformationScreen.route,
            listOf("system info", "hostname", "version", "uptime", "platform")),
        ScreenEntry("nav_software_info", "Software Information", "Software details", Screen.SoftwareInformationScreen.route,
            listOf("software", "license", "os")),
        ScreenEntry("nav_hardware_info", "Hardware Information", "CPU, memory, physical", Screen.HardwareInformationScreen.route,
            listOf("hardware", "cpu model", "memory", "physical", "serial")),

        // Instance config sub-screens
        ScreenEntry("nav_instance_config", "Instance Configuration", "TrueNAS instance settings", Screen.InstanceConfigScreen.route,
            listOf("instance", "configuration", "truenas settings", "server config")),
        ScreenEntry("nav_services", "TrueNAS Services", "Manage system services", Screen.ServicesScreen.route,
            listOf("services", "ssh", "nfs", "smb", "ftp", "rsync", "iscsi")),
        ScreenEntry("nav_users", "Users", "Manage user accounts", Screen.UserListScreen.route,
            listOf("users", "accounts", "user list")),
        ScreenEntry("nav_api_keys", "API Keys", "Manage API keys", Screen.ApiKeyListScreen.route,
            listOf("api keys", "tokens", "bearer")),
        ScreenEntry("nav_network", "Network", "Network configuration", Screen.NetworkScreen.route,
            listOf("network", "interfaces", "ip", "dns", "gateway")),
        ScreenEntry("nav_boot", "Boot", "Boot environments and pool", Screen.BootScreen.route,
            listOf("boot", "boot environments", "boot pool", "startup")),
        ScreenEntry("nav_alerts", "Alert Services", "Configure alerts", Screen.AlertServicesList.route,
            listOf("alerts", "notifications", "alert services", "email")),
        ScreenEntry("nav_audit", "Audit", "Audit configuration and logs", Screen.AuditConfigScreen.route,
            listOf("audit", "logs", "audit logs")),
        ScreenEntry("nav_general_settings", "General Settings", "GUI, HTTPS, timezone", Screen.GeneralSystemSettingsScreen.route,
            listOf("general settings", "gui", "https", "timezone", "language")),
        ScreenEntry("nav_advanced_settings", "Advanced Settings", "Sysctl, kernel", Screen.AdvancedSystemSettingsScreen.route,
            listOf("advanced settings", "sysctl", "kernel", "advanced")),
        ScreenEntry("nav_truenas_connect", "TrueNAS Connect", "Connect to TrueNAS cloud", Screen.TrueNasConnectScreen.route,
            listOf("truenas connect", "cloud", "ixsystems")),
        ScreenEntry("nav_truecommand", "TrueCommand", "TrueCommand management", Screen.TrueCommandScreen.route,
            listOf("truecommand", "fleet", "management")),

        // Account
        ScreenEntry("nav_account", "Account Switcher", "Switch accounts or servers", Screen.AccountSwitcher.route,
            listOf("account", "login", "logout", "switch", "profile")),
        ScreenEntry("nav_change_password", "Change Password", "Change your password", Screen.ChangePassword.route,
            listOf("change password", "password", "credentials")),
    )

    // ── Instance Settings registry — every section reachable from InstanceConfigScreen ──
    private val instanceSettingsRegistry: List<ScreenEntry> = listOf(
        // Core configuration
        ScreenEntry("is_general", "General Settings", "Hostname, timezone, base options",
            Screen.GeneralSystemSettingsScreen.route,
            listOf("general settings", "hostname", "timezone", "gui", "https", "language", "console", "motd")),
        ScreenEntry("is_advanced", "Advanced Settings", "Sysctl, tunables, kernel, developer options",
            Screen.AdvancedSystemSettingsScreen.route,
            listOf("advanced settings", "sysctl", "kernel", "tunables", "developer", "crash reporting")),
        ScreenEntry("is_network", "Network", "Interfaces, routes, DNS, configuration",
            Screen.NetworkScreen.route,
            listOf("network", "interfaces", "ip", "dns", "gateway", "routes", "link")),
        ScreenEntry("is_boot", "Boot", "Boot environments and boot pool",
            Screen.BootScreen.route,
            listOf("boot", "boot environments", "boot pool", "startup", "recovery")),
        ScreenEntry("is_services", "Services", "Manage and configure running services",
            Screen.ServicesScreen.route,
            listOf("services", "ssh", "nfs", "smb", "ftp", "rsync", "iscsi", "snmp")),
        ScreenEntry("is_users", "Users", "Manage local user accounts",
            Screen.UserListScreen.route,
            listOf("users", "accounts", "local users", "user list")),
        ScreenEntry("is_api_keys", "API Keys", "Manage API key access",
            Screen.ApiKeyListScreen.route,
            listOf("api keys", "keys", "tokens", "bearer", "access")),

        // Monitoring & compliance
        ScreenEntry("is_alerts", "Alert Settings", "Notification levels and delivery",
            Screen.AlertServicesList.route,
            listOf("alerts", "alert services", "notifications", "email", "webhook")),
        ScreenEntry("is_audit_config", "Audit Config", "Audit logging and retention settings",
            Screen.AuditConfigScreen.route,
            listOf("audit", "audit config", "retention", "logging", "zfs dataset")),
        ScreenEntry("is_audit_logs", "Audit Logs", "View and export audit entries",
            Screen.AuditLogsScreen.route,
            listOf("audit logs", "logs", "audit entries", "export")),

        // System information
        ScreenEntry("is_truenas_connect", "TrueNAS Connect", "Configure the TrueNAS cloud connection",
            Screen.TrueNasConnectScreen.route,
            listOf("truenas connect", "cloud", "connect", "ixsystems")),
        ScreenEntry("is_truecommand", "TrueCommand", "Centralized instance management",
            Screen.TrueCommandScreen.route,
            listOf("truecommand", "fleet", "management", "cluster")),
        ScreenEntry("is_system_info", "System Information", "Version, identifiers, state and features",
            Screen.SystemInformationScreen.route,
            listOf("system information", "system info", "version", "platform", "hardware", "uuid")),
    )

    private fun searchInstanceSettingsRegistry(lowerQuery: String): List<SearchResult.InstanceSettingsResult> {
        return instanceSettingsRegistry.mapNotNull { entry ->
            val relevance = calculateRelevance(lowerQuery, entry.title, entry.subtitle, *entry.keywords.toTypedArray())
            if (relevance > 0) {
                SearchResult.InstanceSettingsResult(
                    id = entry.id,
                    title = entry.title,
                    subtitle = "Instance Setting • ${entry.subtitle}",
                    relevanceScore = relevance + 0.5f,
                    route = entry.route
                )
            } else null
        }
    }

    private fun searchScreenRegistry(lowerQuery: String): List<SearchResult.NavigationResult> {
        return screenRegistry.mapNotNull { entry ->
            val relevance = calculateRelevance(lowerQuery, entry.title, entry.subtitle, *entry.keywords.toTypedArray())
            if (relevance > 0) {
                SearchResult.NavigationResult(
                    id = entry.id,
                    title = entry.title,
                    subtitle = entry.subtitle,
                    relevanceScore = relevance + 0.5f, // slight boost for screen nav
                    destinationRoute = entry.route
                )
            } else null
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  RELEVANCE SCORING
    // ═══════════════════════════════════════════════════════════

    private fun calculateRelevance(query: String, vararg fields: String): Float {
        var score = 0f

        fields.forEach { field ->
            val lowerField = field.lowercase()
            when {
                lowerField == query -> score += 10f
                lowerField.startsWith(query) -> score += 7f
                lowerField.split(" ", "-", "_").any { it == query } -> score += 5f
                lowerField.contains(query) -> score += 3f
                isFuzzyMatch(query, lowerField) -> score += 1f
            }
        }

        return score
    }

    private fun isFuzzyMatch(query: String, text: String): Boolean {
        var queryIndex = 0
        for (char in text) {
            if (queryIndex < query.length && char == query[queryIndex]) {
                queryIndex++
            }
        }
        return queryIndex == query.length
    }

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }
        return "%.1f %s".format(size, units[unitIndex])
    }
}
