package com.imnotndesh.truehub.ui.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveSearchAppBar(
    title: String,
    manager: TrueNASApiManager,
    onNavigateBack: (() -> Unit)? = null,
    onSearchResultClick: (SearchResult) -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    startSearchActive: Boolean = false,
    onCloseSearch: (() -> Unit)? = null
) {
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(manager)
    )
    var isSearchActive by remember { mutableStateOf(startSearchActive) }
    val searchState by searchViewModel.searchState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    // ── Entire-screen search mode ─────────────────────────────
    if (isSearchActive) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status bar spacer
                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))

                // Search input row
                SearchTopBar(
                    query = searchState.query,
                    onQueryChange = { searchViewModel.updateSearchQuery(it) },
                    onClose = {
                        searchViewModel.clearSearch()
                        isSearchActive = false
                        onCloseSearch?.invoke()
                    },
                    focusRequester = focusRequester
                )

                // Horizontally scrolling filter chips — no bottom padding
                SearchCategoryChipsRow(
                    selectedCategory = searchState.selectedCategory,
                    onCategorySelected = { searchViewModel.selectCategory(it) }
                )

                // Results fill remaining space
                SearchResultsContent(
                    searchState = searchState,
                    onResultClick = { result ->
                        searchViewModel.addToRecentSearches(searchState.query)
                        onSearchResultClick(result)
                        isSearchActive = false
                        searchViewModel.clearSearch()
                        onCloseSearch?.invoke()
                    },
                    onRecentSearchClick = { query ->
                        searchViewModel.updateSearchQuery(query)
                    },
                    onDeleteRecentSearch = { query ->
                        searchViewModel.removeRecentSearch(query)
                    },
                    onClearRecentSearches = {
                        searchViewModel.clearRecentSearches()
                    }
                )
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        return
    }

    // ── Normal top bar (search inactive) ──────────────────────
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            onNavigateBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { isSearchActive = true }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ═══════════════════════════════════════════════════════════════
//  Search input row
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Close search"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search TrueHub...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            },
            singleLine = true
        )

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search"
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Horizontally scrolling filter chips (M3 Expressive style)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCategoryChipsRow(
    selectedCategory: SearchCategory,
    onCategorySelected: (SearchCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(SearchCategory.entries.toList()) { category ->
            val isSelected = category == selectedCategory
            val containerColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "chipColor"
            )
            val labelColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                label = "chipLabel"
            )

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor,
                    selectedLabelColor = labelColor,
                    containerColor = containerColor,
                    labelColor = labelColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent
                    else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Quick actions grid (shown when search is empty)
// ═══════════════════════════════════════════════════════════════

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val result: SearchResult.NavigationResult
)

private val quickActions: List<QuickAction> = listOf(
    QuickAction(
        title = "Instance Settings",
        icon = Icons.Default.Settings,
        result = SearchResult.NavigationResult(
            id = "quick_settings", title = "Instance Settings", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.InstanceConfigScreen.route
        )
    ),
    QuickAction(
        title = "Create API Key",
        icon = Icons.Default.Key,
        result = SearchResult.NavigationResult(
            id = "quick_apikey", title = "Create API Key", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.ApiKeyCreateScreen.route
        )
    ),
    QuickAction(
        title = "Discover Apps",
        icon = Icons.Default.Storefront,
        result = SearchResult.NavigationResult(
            id = "quick_marketplace", title = "Marketplace", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.Marketplace.route
        )
    ),
    QuickAction(
        title = "System Update",
        icon = Icons.Default.SystemUpdateAlt,
        result = SearchResult.NavigationResult(
            id = "quick_update", title = "System Update", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.SystemUpdateScreen.route
        )
    ),
    QuickAction(
        title = "Users",
        icon = Icons.Default.Group,
        result = SearchResult.NavigationResult(
            id = "quick_users", title = "Users", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.UserListScreen.route
        )
    ),
    QuickAction(
        title = "Network",
        icon = Icons.Default.NetworkWifi,
        result = SearchResult.NavigationResult(
            id = "quick_network", title = "Network", subtitle = "Navigate",
            relevanceScore = 10f, destinationRoute = Screen.NetworkScreen.route
        )
    ),
)

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Results content (recent searches, loading, empty, results)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchResultsContent(
    searchState: SearchState,
    onResultClick: (SearchResult) -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onDeleteRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit
) {
    val expandedCategories = remember { mutableMapOf<SearchCategory, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Empty query → Quick actions + Recent searches ────
        if (searchState.query.isEmpty()) {
            // Quick actions grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(quickActions.chunked(2)) { rowActions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowActions.forEach { action ->
                        QuickActionCard(
                            action = action,
                            onClick = { onResultClick(action.result) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If odd number, fill the empty slot
                    if (rowActions.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Recent searches section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (searchState.recentSearches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No search history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(searchState.recentSearches) { recentQuery ->
                    RecentSearchItem(
                        query = recentQuery,
                        onClick = { onRecentSearchClick(recentQuery) },
                        onDelete = { onDeleteRecentSearch(recentQuery) }
                    )
                }
                item {
                    TextButton(
                        onClick = onClearRecentSearches,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Clear search history",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else if (searchState.isSearching) {
            // ── Loading ───────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (searchState.results.isEmpty()) {
            // ── No results ───────────────────────────────────
            item {
                NoResultsView(query = searchState.query)
            }
        } else {
            // ── Results grouped by category with expand/collapse ──
            val grouped = searchState.results.groupBy { it.category }
            val collapsedLimit = 4

            grouped.forEach { (category, results) ->
                item {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                val isExpanded = expandedCategories[category] == true
                val visibleResults = if (results.size > collapsedLimit && !isExpanded) {
                    results.take(collapsedLimit)
                } else {
                    results
                }

                items(visibleResults) { result ->
                    SearchResultItem(
                        result = result,
                        onClick = { onResultClick(result) }
                    )
                }

                // "Show all N" expander
                if (results.size > collapsedLimit) {
                    val remaining = results.size - collapsedLimit
                    item {
                        TextButton(
                            onClick = {
                                expandedCategories[category] = !(expandedCategories[category] == true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ArrowDropDown
                                else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "Show less"
                                else "Show all ($remaining more)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Divider between categories
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Recent search row
// ═══════════════════════════════════════════════════════════════

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove from history",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Single search result row
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    val isAppResult = result is SearchResult.InstalledAppResult || result is SearchResult.MarketplaceAppResult

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon slot — use actual app icon for app results, fallback to vector icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isAppResult) {
                val iconUrl = when (result) {
                    is SearchResult.InstalledAppResult -> result.app.metadata?.icon
                    is SearchResult.MarketplaceAppResult -> result.app.icon_url
                }
                if (!iconUrl.isNullOrBlank()) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconUrl)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = "${result.title} icon",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AppFallbackIcon(result)
                }
            } else {
                AppFallbackIcon(result)
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun AppFallbackIcon(result: SearchResult) {
    val icon = when (result) {
        is SearchResult.PoolResult -> Icons.Default.Storage
        is SearchResult.DiskResult -> Icons.Default.Storage
        is SearchResult.ServiceResult -> Icons.Default.PlayArrow
        is SearchResult.ShareResult -> Icons.Default.FolderShared
        is SearchResult.SystemInfoResult -> Icons.Default.Info
        is SearchResult.ActionResult -> Icons.Default.TouchApp
        is SearchResult.InstalledAppResult -> Icons.Default.Apps
        is SearchResult.MarketplaceAppResult -> Icons.Default.Storefront
        is SearchResult.ContainerResult -> Icons.Default.Inventory
        is SearchResult.VmResult -> Icons.Default.Computer
        is SearchResult.NavigationResult -> Icons.Default.Search
        is SearchResult.InstanceSettingsResult -> Icons.Default.Settings
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(20.dp)
    )
}

// ═══════════════════════════════════════════════════════════════
//  Empty state
// ═══════════════════════════════════════════════════════════════

@Composable
private fun NoResultsView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Nothing matched \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
