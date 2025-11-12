package com.imnotndesh.truehub.ui.topbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// Sealed class for different searchable items
sealed class SearchResult {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val category: SearchCategory
    abstract val relevanceScore: Float

    data class PoolResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val pool: System.Pool
    ) : SearchResult() {
        override val category = SearchCategory.STORAGE
    }

    data class DiskResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val disk: System.DiskDetails
    ) : SearchResult() {
        override val category = SearchCategory.STORAGE
    }

    data class ServiceResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
    ) : SearchResult() {
        override val category = SearchCategory.SERVICES
    }

    data class ShareResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val share: Shares.SmbShare
    ) : SearchResult() {
        override val category = SearchCategory.SHARES
    }

    data class SystemInfoResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val info: String
    ) : SearchResult() {
        override val category = SearchCategory.SYSTEM
    }

    data class ActionResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val action: AppAction
    ) : SearchResult() {
        override val category = SearchCategory.ACTIONS
    }
    data class NavigationResult(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val relevanceScore: Float,
        val destination: AppDestination
    ) : SearchResult() {
        override val category = SearchCategory.NAVIGATION
    }
}

enum class SearchCategory(val displayName: String) {
    ALL("All"),
    NAVIGATION("Navigation"),
    STORAGE("Storage"),
    SERVICES("Services"),
    SHARES("Shares"),
    SYSTEM("System Info"),
    ACTIONS("Actions")
}

enum class AppDestination {
    DASHBOARD,
    APPS,
    SETTINGS,
    PROFILE,
    STORAGE,
    POOLS,
    DISKS,
    SHARES,
    USERS,
    GROUPS,
    NETWORK,
    SYSTEM_INFO,
    TASKS,
    ALERTS
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

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val apiManager: TrueNASApiManager
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    // Cached data for searching
    private var cachedPools: List<System.Pool> = emptyList()
    private var cachedDisks: List<System.DiskDetails> = emptyList()
    private var cachedShares: List<Shares.SmbShare> = emptyList()
    private var cachedSystemInfo: System.SystemInfo? = null

    init {
        // Debounce search to avoid too many operations
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
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
        _searchState.value = SearchState()
    }

    fun addToRecentSearches(query: String) {
        if (query.isBlank()) return
        val current = _searchState.value.recentSearches.toMutableList()
        current.remove(query) // Remove if already exists
        current.add(0, query) // Add to front
        _searchState.value = _searchState.value.copy(
            recentSearches = current.take(10) // Keep only last 10
        )
    }

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
            val lowerQuery = query.lowercase()
            val selectedCategory = _searchState.value.selectedCategory

            // Search in pools
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.STORAGE) {
                cachedPools.forEach { pool ->
                    val relevance = calculateRelevance(lowerQuery, pool.name, pool.status_detail?:"Missing Status")
                    if (relevance > 0) {
                        results.add(
                            SearchResult.PoolResult(
                                id = "pool_${pool.id}",
                                title = pool.name,
                                subtitle = "Pool • ${formatBytes(pool.size)}",
                                relevanceScore = relevance,
                                pool = pool
                            )
                        )
                    }
                }
            }

            // Search in disks
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.STORAGE) {
                cachedDisks.forEach { disk ->
                    val relevance = calculateRelevance(lowerQuery, disk.name, disk.model?: "No model", disk.serial)
                    if (relevance > 0) {
                        results.add(
                            SearchResult.DiskResult(
                                id = "disk_${disk.name}",
                                title = disk.name,
                                subtitle = "Disk • ${disk.model}",
                                relevanceScore = relevance,
                                disk = disk
                            )
                        )
                    }
                }
            }

            // Search in shares
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SHARES) {
                cachedShares.forEach { share ->
                    val relevance = calculateRelevance(lowerQuery, share.name, share.path, share.comment?:"No Comments")
                    if (relevance > 0) {
                        results.add(
                            SearchResult.ShareResult(
                                id = "share_${share.id}",
                                title = share.name,
                                subtitle = "Share • ${share.path}",
                                relevanceScore = relevance,
                                share = share
                            )
                        )
                    }
                }
            }

            // Search in system info
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.SYSTEM) {
                cachedSystemInfo?.let { info ->
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

            // Search in actions
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
            // Search in navigation destinations
            if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.NAVIGATION) {
                val destinations = listOf(
                    Triple("dashboard", "Go to Dashboard", AppDestination.DASHBOARD),
                    Triple("apps", "Go to Apps", AppDestination.APPS),
                    Triple("settings", "Go to Settings", AppDestination.SETTINGS),
                    Triple("profile", "Go to Profile", AppDestination.PROFILE),
                    Triple("storage", "Go to Storage", AppDestination.STORAGE),
                    Triple("pools", "Go to Pools", AppDestination.POOLS),
                    Triple("disks", "Go to Disks", AppDestination.DISKS),
                    Triple("shares", "Go to Shares", AppDestination.SHARES),
                    Triple("users", "Go to Users", AppDestination.USERS),
                    Triple("groups", "Go to Groups", AppDestination.GROUPS),
                    Triple("network", "Go to Network", AppDestination.NETWORK),
                    Triple("system info", "Go to System Info", AppDestination.SYSTEM_INFO),
                    Triple("tasks", "Go to Tasks", AppDestination.TASKS),
                    Triple("alerts", "Go to Alerts", AppDestination.ALERTS)
                )

                destinations.forEach { (key, description, destination) ->
                    val relevance = calculateRelevance(lowerQuery, key, description)
                    if (relevance > 0) {
                        results.add(
                            SearchResult.NavigationResult(
                                id = "nav_$key",
                                title = description,
                                subtitle = "Navigate",
                                relevanceScore = relevance,
                                destination = destination
                            )
                        )
                    }
                }
            }
            // Sort by relevance
            val sortedResults = results.sortedByDescending { it.relevanceScore }

            _searchState.value = _searchState.value.copy(
                results = sortedResults,
                isSearching = false
            )
        }
    }

    private fun calculateRelevance(query: String, vararg fields: String): Float {
        var score = 0f

        fields.forEach { field ->
            val lowerField = field.lowercase()
            when {
                // Exact match - highest score
                lowerField == query -> score += 10f
                // Starts with query - high score
                lowerField.startsWith(query) -> score += 7f
                // Contains query as word - medium score
                lowerField.split(" ", "-", "_").any { it == query } -> score += 5f
                // Contains query - lower score
                lowerField.contains(query) -> score += 3f
                // Fuzzy match (all characters present in order) - lowest score
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