package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.imnotndesh.truehub.R
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import kotlinx.coroutines.delay
import coil.request.ImageRequest
import coil.decode.SvgDecoder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MarketplaceScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onMarketplaceApplicationClicked: (Apps.AppAvailableItem) -> Unit,
    onInstallApplication: (Apps.AppAvailableItem) -> Unit = {},
    initialCategory: String? = null
) {
    val viewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    // Handle back press
    BackHandler(enabled = true) {
        when {
            isSearchFocused -> {
                // Clear search focus and query
                focusManager.clearFocus()
                isSearchFocused = false
                searchQuery = ""
            }
            selectedCategory != null -> {
                selectedCategory = null
            }
            else -> onNavigateBack()
        }
    }

    // Load marketplace data
    LaunchedEffect(Unit) {
        if (uiState.marketplaceApps.isEmpty()) {
            viewModel.loadMarketplaceApps()
        }
    }

    // Search filtering
    val searchFilteredApps by remember(searchQuery, uiState.marketplaceApps) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                uiState.marketplaceApps
            } else {
                uiState.marketplaceApps.filter { app ->
                    app.title.contains(searchQuery, ignoreCase = true) ||
                            app.name.contains(searchQuery, ignoreCase = true) ||
                            app.tagsString.contains(searchQuery, ignoreCase = true) ||
                            app.description.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    // Derive categories and recommendations
    val recommendedApps by remember(searchFilteredApps) {
        derivedStateOf {
            searchFilteredApps.filter { it.recommended }.ifEmpty { searchFilteredApps.take(5) }
        }
    }

    val localCategories by remember(searchFilteredApps) {
        derivedStateOf {
            searchFilteredApps.flatMap { it.categories ?: emptyList() }.distinct().sorted()
        }
    }

    // UI
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Animated header – hides when search is focused
            AnimatedVisibility(
                visible = !isSearchFocused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                UnifiedScreenHeader(
                    title = selectedCategory ?: "Marketplace",
                    subtitle = if (selectedCategory != null) "Discover $selectedCategory apps" else "Discover chart applications",
                    isLoading = uiState.isLoading,
                    isRefreshing = uiState.isRefreshing,
                    error = uiState.error,
                    onDismissError = { viewModel.clearError() },
                    manager = manager,
                    onBackPressed = {
                        if (selectedCategory != null) {
                            selectedCategory = null
                        } else {
                            onNavigateBack()
                        }
                    }
                )
            }

            // Search Field – elevated and full-width when focused
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = if (isSearchFocused) 8.dp else 0.dp,
                tonalElevation = if (isSearchFocused) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button when search is focused – to exit search
                    if (isSearchFocused) {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            isSearchFocused = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search apps, utilities, tools...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                isSearchFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    // When focused, auto-show keyboard
                                }
                            }
                            .focusRequester(focusRequester)
                    )
                }
            }

            // Main content – pulls to refresh
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.marketplaceApps.isEmpty() && !uiState.isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PullToRefreshContent(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.loadMarketplaceApps() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    // Detect taps on the list to dismiss keyboard
                                    detectTapGestures(onTap = {
                                        focusManager.clearFocus()
                                    })
                                },
                            contentPadding = PaddingValues(bottom = 40.dp)
                        ) {
                            // Search results mode (if search query not empty)
                            if (searchQuery.isNotBlank()) {
                                item {
                                    Text(
                                        text = "${searchFilteredApps.size} result${if (searchFilteredApps.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            start = 20.dp, top = 16.dp, bottom = 6.dp
                                        )
                                    )
                                }
                                items(searchFilteredApps, key = { "search-${it.name}" }) { app ->
                                    SearchResultRow(
                                        app = app,
                                        onClick = {
                                            onMarketplaceApplicationClicked(app)
                                            // Optionally clear focus and keyboard
                                            focusManager.clearFocus()
                                        },
                                        onInstallClick = { onInstallApplication(app) },
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            } else {
                                // Normal browsing mode
                                if (selectedCategory == null) {
                                    // Home – show featured carousel and categories
                                    if (recommendedApps.isNotEmpty()) {
                                        item {
                                            SectionHeader(title = "Featured")
                                            Spacer(Modifier.height(10.dp))
                                            RecommendedCarousel(
                                                apps = recommendedApps,
                                                onAppClick = onMarketplaceApplicationClicked
                                            )
                                            Spacer(Modifier.height(28.dp))
                                        }
                                    }

                                    items(localCategories) { categoryName ->
                                        val categoryApps = searchFilteredApps.filter {
                                            it.categories?.contains(categoryName) == true
                                        }
                                        if (categoryApps.isNotEmpty()) {
                                            SectionHeaderWithAction(
                                                title = categoryName.replaceFirstChar { it.uppercase() },
                                                onShowMoreClick = { selectedCategory = categoryName }
                                            )
                                            Spacer(Modifier.height(10.dp))
                                            LazyRow(
                                                contentPadding = PaddingValues(horizontal = 20.dp),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                items(
                                                    categoryApps.take(5),
                                                    key = { "cat-$categoryName-${it.name}" }
                                                ) { app ->
                                                    AppGridItem(app = app, onClick = { onMarketplaceApplicationClicked(app) })
                                                }

                                                if (categoryApps.size > 5) {
                                                    item {
                                                        ShowMoreGridItem(onClick = { selectedCategory = categoryName })
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(24.dp))
                                        }
                                    }
                                } else {
                                    // Category view
                                    val categoryApps = searchFilteredApps.filter {
                                        it.categories?.contains(selectedCategory) == true
                                    }

                                    val categoryRecommended = categoryApps.filter { it.recommended }
                                        .ifEmpty { categoryApps.take(5) }

                                    if (categoryRecommended.isNotEmpty()) {
                                        item {
                                            SectionHeader(title = "Featured in $selectedCategory")
                                            Spacer(Modifier.height(10.dp))
                                            RecommendedCarousel(
                                                apps = categoryRecommended,
                                                onAppClick = onMarketplaceApplicationClicked
                                            )
                                            Spacer(Modifier.height(28.dp))
                                        }
                                    }

                                    item {
                                        Text(
                                            text = "All ${selectedCategory?.replaceFirstChar { it.uppercase() }} Apps (${categoryApps.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp)
                                        )
                                    }

                                    items(categoryApps, key = { "cat-page-${selectedCategory}-${it.name}" }) { app ->
                                        SearchResultRow(
                                            app = app,
                                            onClick = { onMarketplaceApplicationClicked(app) },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 20.dp)
    )
}

@Composable
private fun SectionHeaderWithAction(title: String, onShowMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        TextButton(onClick = onShowMoreClick) {
            Text("Show More", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShowMoreGridItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape((60 * 0.22f).dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Show More",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "See All",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RecommendedCarousel(
    apps: List<Apps.AppAvailableItem>,
    onAppClick: (Apps.AppAvailableItem) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { apps.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(4_000)
            val next = (pagerState.currentPage + 1) % apps.size
            pagerState.animateScrollToPage(next, animationSpec = tween(durationMillis = 500))
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val app = apps[page]
            HeroCarouselCard(app = app, onClick = { onAppClick(app) })
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(apps.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (selected) 20.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
fun HeroCarouselCard(
    app: Apps.AppAvailableItem,
    onClick: () -> Unit
) {
    val screenshot = app.screenshots?.firstOrNull()
    val hasScreenshot = !screenshot.isNullOrBlank()
    val contentColor = if (hasScreenshot) Color.White else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasScreenshot) {
                AsyncImage(
                    model = screenshot,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.45f to Color.Black.copy(alpha = 0.15f),
                                    1.0f to Color.Black.copy(alpha = 0.80f)
                                )
                            )
                        )
                )
            }

            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        "Featured",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.Star, null, Modifier.size(12.dp)) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = null
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(iconUrl = app.icon_url, title = app.title, size = 44)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.title.ifBlank { app.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.installed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle, null,
                                        Modifier.size(12.dp),
                                        tint = Color(0xFF69F0AE)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        "Installed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF69F0AE)
                                    )
                                }
                            }
                            app.categories?.firstOrNull()?.let { cat ->
                                Text(
                                    text = cat.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AppGridItem(app: Apps.AppAvailableItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AppIcon(iconUrl = app.icon_url, title = app.title, size = 60)
            if (app.installed) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Installed",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.title.ifBlank { app.name },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SearchResultRow(
    app: Apps.AppAvailableItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInstallClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(iconUrl = app.icon_url, title = app.title, size = 52)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = app.title.ifBlank { app.name },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (app.installed) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Installed",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                text = app.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!app.categories.isNullOrEmpty()) {
                Text(
                    text = app.categories.joinToString(" · ") { it.replaceFirstChar { c -> c.uppercase() } },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onInstallClick != null) {
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onInstallClick,
                shape = RoundedCornerShape(14.dp),
                colors = if (app.installed) {
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    ButtonDefaults.filledTonalButtonColors()
                },
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = if (app.installed) Icons.Default.Add else Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (app.installed) "Install again" else "Install",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier
            .padding(start = 70.dp)
            .padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
fun AppIcon(iconUrl: String?, title: String, size: Int) {
    val context = LocalContext.current
    val cornerRadius = (size * 0.22f).dp

    if (!iconUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(iconUrl)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "$title icon",
            contentScale = ContentScale.Fit,
            placeholder = painterResource(id = R.drawable.missing_app_icon),
            error = painterResource(id = R.drawable.missing_app_icon),
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(cornerRadius))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.missing_app_icon),
                contentDescription = "$title default icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.size((size * 0.7f).dp)
            )
        }
    }
}