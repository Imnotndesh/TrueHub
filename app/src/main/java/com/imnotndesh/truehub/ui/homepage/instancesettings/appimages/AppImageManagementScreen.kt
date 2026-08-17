package com.imnotndesh.truehub.ui.homepage.instancesettings.appimages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import kotlinx.coroutines.launch

/**
 * Container-image + iX-volume management page, backed by the `app.image.*` and
 * `app.ix_volume.*` APIs. Matches the visual language of the other instance-config
 * pages (gradient background + [UnifiedScreenHeader] + scaffolded pull-to-refresh list).
 */
@Composable
fun AppImageManagementScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    // ── paged image list state ──────────────────────────────────
    val pageSize = 60
    var images by remember { mutableStateOf<List<Apps.AppImageQueryResultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(true) }
    var total by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("" ) }

    var ixVolumes by remember { mutableStateOf<List<Apps.AppIxVolumeQueryResultItem>?>(null) }
    var rateLimit by remember { mutableStateOf<Apps.ContainerImagesDockerhubRateLimitResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showPullDialog by remember { mutableStateOf(false) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    val appScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    suspend fun loadImages(reset: Boolean) {
        val offset = if (reset) 0 else images.size
        val result = manager.apps.queryImagesWithResult(
            parseTags = true,
            offset = offset,
            limit = pageSize
        )
        when (result) {
            is ApiResult.Success -> {
                val page = result.data
                images = if (reset) page else images + page
                hasMore = page.size >= pageSize
                if (reset) total = page.size // size of first page; refined below
            }
            is ApiResult.Error -> error = result.message ?: "Failed to load images"
            is ApiResult.Loading -> {}
        }
    }

    suspend fun refresh() {
        error = null
        ixVolumes = null
        rateLimit = null
        isLoading = true
        loadImages(reset = true)
        when (val result = manager.apps.queryIxVolumesWithResult()) {
            is ApiResult.Success -> ixVolumes = result.data
            is ApiResult.Error -> error = result.message ?: "Failed to load iX volumes"
            is ApiResult.Loading -> {}
        }
        when (val result = manager.apps.getDockerHubRateLimitWithResult()) {
            is ApiResult.Success -> rateLimit = result.data
            is ApiResult.Error -> error = result.message ?: "Failed to load rate limit"
            is ApiResult.Loading -> {}
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { refresh() }

    // Locally filter the loaded images by the search query.
    val filtered = remember(searchQuery, images) {
        if (searchQuery.isBlank()) images
        else images.filter { img ->
            val label = img.parsedRepoTags?.firstOrNull()?.completeTag
                ?: img.repoTags.firstOrNull()
                ?: img.id
            label.contains(searchQuery, ignoreCase = true) ||
                img.id.contains(searchQuery, ignoreCase = true)
        }
    }

    // Load the next page as the user scrolls toward the end of the list.
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val loaded = images.size
                val threshold = (loaded - pageSize / 2).coerceAtLeast(0)
                if (hasMore && !isLoadingMore && loaded > 0 && lastVisible != null && lastVisible >= threshold) {
                    isLoadingMore = true
                    loadImages(reset = false)
                    isLoadingMore = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        UnifiedScreenHeader(
            title = "App Image Management",
            subtitle = "${total ?: images.size} image(s) · ${images.size} loaded",
            isLoading = isLoading,
            isRefreshing = false,
            error = error,
            onDismissError = { error = null },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            if (isLoading && images.isEmpty()) {
                LoadingScreen("Loading images…")
            } else {
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { appScope.launch { refresh() } },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPullDialog = true },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pull Image")
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search images…") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        rateLimit?.let { rl -> item { DockerHubRateLimitCard(rl) } }

                        val vols = ixVolumes
                        if (!vols.isNullOrEmpty()) {
                            item {
                                Text(
                                    text = "iX Volumes (${vols.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(vols, key = { it.name ?: it.appName ?: "" }) { volume ->
                                IxVolumeCard(volume)
                            }
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }

                        item {
                                Text(
                                    text = "Docker Images",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (filtered.isEmpty()) {
                                item {
                                    Text(
                                        text = if (searchQuery.isBlank()) "No images found. Pull one from a registry." else "No images match your search.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                items(filtered, key = { it.id }) { image ->
                                    ImageRow(
                                        image = image,
                                        onDetail = { selectedImageId = image.id },
                                        onDelete = { selectedImageId = image.id }
                                    )
                                }
                            }
                            if (hasMore) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoadingMore) {
                                            CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                                        } else {
                                            Text(
                                                text = "Scroll for more image(s)…",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showPullDialog) {
        PullImageDialog(
            onSubmit = { ref ->
                showPullDialog = false
                if (ref.isNotBlank()) {
                    appScope.launch {
                        when (val result = manager.apps.pullImageWithResult(ref.trim())) {
                            is ApiResult.Error -> error = result.message ?: "Pull failed"
                            is ApiResult.Success -> error = null
                            is ApiResult.Loading -> {}
                        }
                        refresh()
                    }
                }
            },
            onDismiss = { showPullDialog = false }
        )
    }

    selectedImageId?.let { id ->
        ImageDetailOrDeleteDialog(
            manager = manager,
            imageId = id,
            onDismiss = { selectedImageId = null },
            onDeleted = {
                selectedImageId = null
                appScope.launch { refresh() }
            }
        )
    }
}

@Composable
private fun DockerHubRateLimitCard(rateLimit: Apps.ContainerImagesDockerhubRateLimitResult) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Docker Hub Rate Limit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (rateLimit.error != null) {
                Text(rateLimit.error.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            } else {
                rateLimit.totalPullLimit?.let {
                    RateLimitRow("Total Pull Limit", it)
                }
                rateLimit.remainingPullLimit?.let {
                    RateLimitRow("Remaining Pull Limit", it)
                }
                rateLimit.totalTimeLimitInSecs?.let {
                    RateLimitRow("Resets in (secs)", it)
                }
                rateLimit.remainingTimeLimitInSecs?.let {
                    RateLimitRow("Remaining time (secs)", it)
                }
            }
        }
    }
}

@Composable
private fun RateLimitRow(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun IxVolumeCard(volume: Apps.AppIxVolumeQueryResultItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(volume.name ?: "Unknown", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                volume.appName?.let { Text("App: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun ImageRow(
    image: Apps.AppImageQueryResultItem,
    onDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val label = image.parsedRepoTags?.firstOrNull()?.completeTag
        ?: image.repoTags.firstOrNull()
        ?: image.id.take(19)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onDetail),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (image.dangling) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (image.dangling) "dangling" else "image",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (image.dangling) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatSize(image.size)}  ·  ${image.id.take(19)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
            androidx.compose.material3.IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete image", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PullImageDialog(onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
    var ref by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pull Image") },
        text = {
            Column {
                Text(
                    "Enter the image reference, e.g. nginx:latest or ghcr.io/owner/repo:tag.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("Image reference") },
                    placeholder = { Text("nginx:latest") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(ref) }, enabled = ref.isNotBlank()) { Text("Pull") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/** Shows a full image summary (via get_instance) with the option to delete it. */
@Composable
private fun ImageDetailOrDeleteDialog(
    manager: TrueNASApiManager,
    imageId: String,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    var detail by remember { mutableStateOf<Apps.AppImageEntry?>(null) }
    var loading by remember { mutableStateOf(true) }
    var deleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val localScope = rememberCoroutineScope()

    LaunchedEffect(imageId) {
        when (val result = manager.apps.getImageWithResult(imageId)) {
            is ApiResult.Success -> detail = result.data
            is ApiResult.Error -> error = result.message ?: "Failed to load image"
            is ApiResult.Loading -> {}
        }
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image Details") },
        text = {
            when {
                loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                }
                error != null -> Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                detail != null -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val d = detail!!
                    Text(d.parsedRepoTags?.firstOrNull()?.completeTag ?: d.repoTags.firstOrNull() ?: d.id,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("ID: ${d.id}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    Text("Size: ${formatSize(d.size)}", style = MaterialTheme.typography.bodySmall)
                    d.created?.let { Text("Created: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    d.author?.takeIf { it.isNotBlank() }?.let { Text("Author: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (d.updateAvailable) Text("Update available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    if (d.repoTags.isNotEmpty()) Text("Tags: ${d.repoTags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (deleting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Button(onClick = {
                    deleting = true
                    localScope.launch {
                        when (val r = manager.apps.deleteImageWithResult(imageId, force = false)) {
                            is ApiResult.Error -> { error = r.message ?: "Delete failed"; deleting = false }
                            is ApiResult.Success -> onDeleted()
                            is ApiResult.Loading -> {}
                        }
                    }
                }) { Text("Delete") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var i = 0
    while (value >= 1024 && i < units.size - 1) { value /= 1024; i++ }
    return "%.1f %s".format(value, units[i])
}
