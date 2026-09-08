package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.imnotndesh.truehub.R
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.boot.formatBytes
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppDetailsViewModel
import com.imnotndesh.truehub.ui.utils.ScreenshotViewer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAppDetailsScreen(
    manager: TrueNASApiManager,
    app: Apps.AppAvailableItem,
    onNavigateBack: () -> Unit,
    onInstallClick: (String, String) -> Unit,
    onUninstallSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    val appsViewModel: AppsScreenViewModel = viewModel(
        factory = AppsScreenViewModel.AppsScreenViewModelFactory(manager)
    )
    var activeScreenshotIndex by remember { mutableStateOf<Int?>(null) }
    val appDetailsViewModel: AppDetailsViewModel = viewModel(
        factory = AppDetailsViewModel.provideFactory(manager)
    )

    val deletionJobId by appDetailsViewModel.deletionJobId.collectAsStateWithLifecycle()
    val activeJobs by JobRepository.activeJobs.collectAsStateWithLifecycle()
    val currentDeletionJob = deletionJobId?.let { activeJobs[it] }
    LaunchedEffect(currentDeletionJob?.state) {
        if (currentDeletionJob?.state == "SUCCESS") {
            onUninstallSuccess()
        }
    }

    LaunchedEffect(app.name) {
        delay(1500.milliseconds)
        appsViewModel.preloadCatalogDetails(app.name, app.train)
    }

    var availableAppStorage by remember { mutableStateOf<Long?>(null) }
    var availableStorageLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        when (val result = manager.apps.getAvailableSpaceWithResult()) {
            is ApiResult.Success -> availableAppStorage = result.data
            else -> availableAppStorage = null
        }
        availableStorageLoading = false
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(0.dp)
            ) {
                item {
                    HeroHeaderSection(app = app)
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(24.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            app.categories?.forEach { category ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(category.replaceFirstChar { it.uppercase() }) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        ),
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        if (app.categories.isNullOrEmpty().not()) {
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        AppActionSection(
                            isInstalled = app.installed,
                            isDeleting = currentDeletionJob != null,
                            onInstallClick = { onInstallClick(app.name, app.train) },
                            onUninstallClick = {
                                appDetailsViewModel.deleteApp(context = context, appName = app.name)
                            },
                            onInstallAnotherClick = { onInstallClick(app.name, app.train) }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "About this application",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25f
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (!app.screenshots.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "Screenshots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            app.screenshots.forEachIndexed { index, screenshotUrl ->
                                AsyncImage(
                                    model = screenshotUrl,
                                    contentDescription = "App Screenshot",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(280.dp)
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickable {
                                            activeScreenshotIndex = index
                                        }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Technical Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        HealthStatusCard(healthy = app.healthy, errorMsg = app.healthy_error)
                        Spacer(modifier = Modifier.height(12.dp))

                        AvailableAppStorageCard(
                            availableBytes = availableAppStorage,
                            isLoading = availableStorageLoading
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ElevatedCard(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                InfoRowItem(
                                    icon = Icons.Default.Info,
                                    label = "Latest Version",
                                    value = app.latest_human_version ?: app.latest_version ?: "N/A"
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                InfoRowItem(
                                    icon = Icons.Default.Folder,
                                    label = "Catalog Train",
                                    value = "${app.catalog ?: "Unknown"} / ${app.train}"
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                InfoRowItem(
                                    icon = Icons.Default.Update,
                                    label = "Last Updated",
                                    value = app.lastUpdateString
                                )
                                if (app.tagsString.isNotBlank()) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    InfoRowItem(
                                        icon = Icons.Default.LocalOffer,
                                        label = "Tags",
                                        value = app.tagsString
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (app.maintainers.isNotEmpty()) {
                            Text(
                                text = "Maintainers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            app.maintainers.forEach { maintainer ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.4f
                                        ),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = maintainer.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!app.app_readme.isNullOrBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 16.dp)  // Only top padding; no bottom padding
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 20.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Application Documentation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val textColor = MaterialTheme.colorScheme.onSurface
                                val linkColor = MaterialTheme.colorScheme.primary

                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    factory = { context ->
                                        TextView(context).apply {
                                            movementMethod = LinkMovementMethod.getInstance()
                                            textSize = 14f
                                        }
                                    },
                                    update = { textView ->
                                        textView.setTextColor(textColor.hashCode())
                                        textView.setLinkTextColor(linkColor.hashCode())
                                        textView.text = HtmlCompat.fromHtml(
                                            app.app_readme,
                                            HtmlCompat.FROM_HTML_MODE_LEGACY
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        MinimalBackHeader(
            onBackPressed = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        )
    }
    if (activeScreenshotIndex != null && !app.screenshots.isNullOrEmpty()) {
        ScreenshotViewer(
            screenshots = app.screenshots,
            initialIndex = activeScreenshotIndex!!,
            onDismiss = { activeScreenshotIndex = null }
        )
    }
}

@Composable
private fun HeroHeaderSection(
    app: Apps.AppAvailableItem,
) {
    val context = LocalContext.current
    val cornerRadius = (44 * 0.22f).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val firstScreenshot = app.screenshots?.firstOrNull()

        if (!firstScreenshot.isNullOrBlank()) {
            AsyncImage(
                model = firstScreenshot,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 3.dp,
                modifier = Modifier.size(84.dp)
            ) {

                Box(
                    modifier = Modifier.padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!app.icon_url.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(app.icon_url)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "${app.title} icon",
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            placeholder = painterResource(id = R.drawable.missing_app_icon),
                            error = painterResource(id = R.drawable.missing_app_icon),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(cornerRadius))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.missing_app_icon),
                                contentDescription = "${app.title} default icon",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier.size((44 * 0.7f).dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = app.title.ifBlank { app.name },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HealthStatusCard(healthy: Boolean?, errorMsg: String?) {
    val isHealthy = healthy ?: true
    val containerColor = if (isHealthy) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    val contentColor = if (isHealthy) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onErrorContainer
    val icon = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isHealthy) "Application Stable & Healthy" else "System Health Alert",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!isHealthy && !errorMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = errorMsg, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Reference pool capacity (bytes) used to normalise the available-space gauge. */
private const val APPS_POOL_REFERENCE_BYTES: Long = 1L shl 40 // 1 TiB

@Composable
private fun AvailableAppStorageCard(
    availableBytes: Long?,
    isLoading: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Available App Storage",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Space in the apps pool consumable by applications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Checking available space…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (availableBytes == null) {
                Text(
                    text = "Unable to retrieve available space",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val reference = APPS_POOL_REFERENCE_BYTES.coerceAtLeast(availableBytes)
                val fillFraction = (availableBytes.toFloat() / reference).coerceIn(0f, 1f)
                val barColor = storageBarColor(availableBytes = availableBytes)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatBytes(availableBytes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Text(
                        text = "available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fillFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Low",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "High",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun storageBarColor(availableBytes: Long): Color {
    return when {
        availableBytes <= 10L shl 30 -> MaterialTheme.colorScheme.error          // < ~10 GiB → red (mostly full)
        availableBytes <= 20L shl 30 -> MaterialTheme.colorScheme.tertiary       // 10–20 GiB → amber-ish
        else -> MaterialTheme.colorScheme.primary                                // plenty → calm/primary
    }
}

@Composable
private fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppActionSection(
    isInstalled: Boolean,
    isDeleting: Boolean,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit,
    onInstallAnotherClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isInstalled) {
            Button(
                onClick = onInstallClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Install Application", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            OutlinedButton(
                onClick = onUninstallClick,
                shape = RoundedCornerShape(16.dp),
                enabled = !isDeleting,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uninstalling...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Uninstall", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onInstallAnotherClick,
                shape = RoundedCornerShape(16.dp),
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Install Another", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0

        placeables.forEach { placeable ->
            if (currentWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + horizontalArrangement.spacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val rowHeights = rows.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val totalHeight = rowHeights.sum() + verticalArrangement.spacing.roundToPx() * (rows.size - 1)

        layout(constraints.maxWidth, totalHeight.coerceAtMost(constraints.maxHeight)) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalArrangement.spacing.roundToPx()
                }
                y += rowHeights[index] + verticalArrangement.spacing.roundToPx()
            }
        }
    }
}