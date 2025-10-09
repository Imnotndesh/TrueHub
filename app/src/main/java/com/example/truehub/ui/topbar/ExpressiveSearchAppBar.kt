package com.example.truehub.ui.topbar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveSearchAppBar(
    title: String,
    manager: TrueNASApiManager,
    onNavigateBack: (() -> Unit)? = null,
    onSearchResultClick: (SearchResult) -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    var searchViewModel : SearchViewModel = viewModel(
        factory = SearchViewModelFactory(manager)
    )
    var isSearchActive by remember { mutableStateOf(false) }
    val searchState by searchViewModel.searchState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSearchActive) 0.dp else 4.dp
    ) {
        Column {
            // Main App Bar
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { searchActive ->
                if (searchActive) {
                    // Search Mode
                    SearchTopBar(
                        query = searchState.query,
                        onQueryChange = { searchViewModel.updateSearchQuery(it) },
                        onClose = {
                            searchViewModel.clearSearch()
                            isSearchActive = false
                        },
                        focusRequester = focusRequester
                    )
                } else {
                    // Normal Mode
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
                            // Search Icon
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
            }

            // Category Chips (only show when searching)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SearchCategoryChips(
                    selectedCategory = searchState.selectedCategory,
                    onCategorySelected = { searchViewModel.selectCategory(it) }
                )
            }
        }
    }

    // Search Results Overlay
    AnimatedVisibility(
        visible = isSearchActive,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        SearchResultsOverlay(
            searchState = searchState,
            onResultClick = { result ->
                searchViewModel.addToRecentSearches(searchState.query)
                onSearchResultClick(result)
                isSearchActive = false
                searchViewModel.clearSearch()
            },
            onRecentSearchClick = { query ->
                searchViewModel.updateSearchQuery(query)
            }
        )
    }

    // Request focus when search becomes active
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }
}

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

        // Search Input
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

@Composable
private fun SearchCategoryChips(
    selectedCategory: SearchCategory,
    onCategorySelected: (SearchCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (category == selectedCategory) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun SearchResultsOverlay(
    searchState: SearchState,
    onResultClick: (SearchResult) -> Unit,
    onRecentSearchClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Show recent searches if no query
            if (searchState.query.isEmpty() && searchState.recentSearches.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(searchState.recentSearches) { recentQuery ->
                    RecentSearchItem(
                        query = recentQuery,
                        onClick = { onRecentSearchClick(recentQuery) }
                    )
                }
            }

            // Show search results
            if (searchState.query.isNotEmpty()) {
                if (searchState.isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (searchState.results.isEmpty()) {
                    item {
                        NoResultsView(query = searchState.query)
                    }
                } else {
                    // Group results by category
                    val groupedResults = searchState.results.groupBy { it.category }

                    groupedResults.forEach { (category, results) ->
                        item {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(results) { result ->
                            SearchResultItem(
                                result = result,
                                onClick = { onResultClick(result) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    val icon = when (result) {
        is SearchResult.PoolResult -> Icons.Default.Storage
        is SearchResult.DiskResult -> Icons.Default.Storage
        is SearchResult.ServiceResult -> Icons.Default.PlayArrow
        is SearchResult.ShareResult -> Icons.Default.FolderShared
        is SearchResult.SystemInfoResult -> Icons.Default.Info
        is SearchResult.ActionResult -> Icons.Default.TouchApp
        is SearchResult.NavigationResult -> when (result.destination) {
            AppDestination.DASHBOARD -> Icons.Default.Dashboard
            AppDestination.APPS -> Icons.Default.Apps
            AppDestination.SETTINGS -> Icons.Default.Settings
            AppDestination.PROFILE -> Icons.Default.Person
            AppDestination.STORAGE -> Icons.Default.Storage
            AppDestination.POOLS -> Icons.Default.Storage
            AppDestination.DISKS -> Icons.Default.Storage
            AppDestination.SHARES -> Icons.Default.FolderShared
            AppDestination.USERS -> Icons.Default.Group
            AppDestination.GROUPS -> Icons.Default.Group
            AppDestination.NETWORK -> Icons.Default.NetworkWifi
            AppDestination.SYSTEM_INFO -> Icons.Default.Info
            AppDestination.TASKS -> Icons.AutoMirrored.Filled.List
            AppDestination.ALERTS -> Icons.Default.Notifications
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoResultsView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try searching for something else",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}