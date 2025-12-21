// CREATE NEW FILE: AppInfoPane.kt
// Place this in the same package as AppInfoDialog

package com.imnotndesh.truehub.ui.services.apps.details

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun AppInfoPane(
    app: Apps.AppQueryResponse,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Section (similar to bottom sheet but adapted for pane)
                AppInfoPaneHeader(
                    app = app,
                    onClose = onClose
                )
                // Basic Information Section
                ServiceInfoSection(
                    title = "Basic Information",
                    icon = Icons.Default.Info
                ) {
                    ServiceInfoRow("App Name", app.metadata?.title ?: app.name)
                    ServiceInfoRow("ID", app.id)
                    ServiceInfoRow("Version", app.humanVersion ?: app.version ?: "Unknown")
                    ServiceInfoRow("Status", app.state.replaceFirstChar { it.uppercase() })
                    if (app.upgrade_available) {
                        ServiceInfoRow("Latest Version", app.latestVersion ?: "Available")
                    }
                    if (app.customApp) {
                        ServiceInfoRow("Type", "Custom Application")
                    }
                }

                // Description Section
                app.metadata?.description?.let { description ->
                    ServiceInfoSection(
                        title = "Description",
                        icon = Icons.Default.Description
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Categories and Keywords
                if (!app.metadata?.categories.isNullOrEmpty() || !app.metadata?.keywords.isNullOrEmpty()) {
                    ServiceInfoSection(
                        title = "Categories & Tags",
                        icon = Icons.Default.Tag
                    ) {
                        app.metadata.categories?.let { categories ->
                            ServiceInfoChipGroup(
                                title = "Categories",
                                items = categories,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        app.metadata.keywords?.let { keywords ->
                            if (keywords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                ServiceInfoChipGroup(
                                    title = "Keywords",
                                    items = keywords,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Network & Ports Section
                app.activeWorkloads?.usedPorts?.let { ports ->
                    if (ports.isNotEmpty()) {
                        ServiceInfoSection(
                            title = "Network & Ports",
                            icon = Icons.Default.NetworkCheck
                        ) {
                            ports.forEach { port ->
                                ServicePortCard(port = port)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Portals Section
                app.portals?.let { portals ->
                    if (portals.isNotEmpty()) {
                        ServiceInfoSection(
                            title = "Web Portals",
                            icon = Icons.AutoMirrored.Filled.Launch
                        ) {
                            portals.forEach { (name, url) ->
                                ServicePortalCard(name = name, url = url)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Container Details Section
                app.activeWorkloads?.containerDetails?.let { containers ->
                    if (containers.isNotEmpty()) {
                        ServiceInfoSection(
                            title = "Containers",
                            icon = Icons.Default.Apps
                        ) {
                            containers.forEach { container ->
                                ServiceContainerCard(container = container)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Volume Mounts Section
                app.activeWorkloads?.volumes?.let { volumes ->
                    if (volumes.isNotEmpty()) {
                        ServiceInfoSection(
                            title = "Volume Mounts",
                            icon = Icons.Default.Storage
                        ) {
                            volumes.forEach { volume ->
                                ServiceVolumeCard(volume = volume)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Maintainers Section
                app.metadata?.maintainers?.let { maintainers ->
                    if (maintainers.isNotEmpty()) {
                        ServiceInfoSection(
                            title = "Maintainers",
                            icon = Icons.Default.Person
                        ) {
                            maintainers.forEach { maintainer ->
                                ServiceMaintainerCard(maintainer = maintainer)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Links Section
                if (app.metadata?.home != null || !app.metadata?.sources.isNullOrEmpty()) {
                    ServiceInfoSection(
                        title = "Links",
                        icon = Icons.Default.Link
                    ) {
                        app.metadata.home?.let { home ->
                            ServiceLinkCard(
                                name = "Homepage",
                                url = home,
                                icon = Icons.Default.Home
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        app.metadata.sources?.forEach { source ->
                            ServiceLinkCard(
                                name = "Source Code",
                                url = source,
                                icon = Icons.Default.Code
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Notes Section
                app.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        ServiceInfoSection(
                            title = "Notes",
                            icon = Icons.AutoMirrored.Filled.Note
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                MarkdownText(
                                    markdown = notes,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppInfoPaneHeader(
    app: Apps.AppQueryResponse,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        WavyGradientBackground {
            // Close button (top-end)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Title + version + optional icon grouped together
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App icon
                app.metadata?.icon?.let { iconUrl ->
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Title
                Text(
                    text = app.metadata?.title ?: app.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Version
                Text(
                    text = "Version ${app.humanVersion ?: app.version ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Reuse all the helper composables from AppInfoDialog.kt:
// ServiceInfoSection, ServiceInfoRow, ServiceInfoChipGroup, etc.
// They should be accessible if they're in the same package

@Composable
private fun ServiceInfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(content = content)
    }
}

@Composable
private fun ServiceInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ServiceInfoChipGroup(
    title: String,
    items: List<String>,
    containerColor: Color,
    contentColor: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                FilterChip(
                    onClick = { },
                    label = { Text(text = item) },
                    selected = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = containerColor,
                        labelColor = contentColor
                    )
                )
            }
        }
    }
}

@Composable
private fun ServicePortCard(port: Apps.UsedPort) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Port ${port.containerPort} (${port.protocol.uppercase()})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            port.hostPorts.forEach { hostPort ->
                Text(
                    text = "→ ${hostPort.hostIp}:${hostPort.hostPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePortalCard(name: String, url: String) {
    val uriHandler = LocalUriHandler.current

    Card(
        onClick = { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Launch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ServiceContainerCard(container: Apps.ContainerDetail) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = container.serviceName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Image: ${container.image}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "State: ${container.state}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ServiceVolumeCard(volume: Apps.Volume) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${volume.source} → ${volume.destination}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            volume.mode?.let { mode ->
                Text(
                    text = "Mode: $mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceMaintainerCard(maintainer: Apps.Maintainer) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = maintainer.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                maintainer.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceLinkCard(name: String, url: String, icon: ImageVector) {
    val uriHandler = LocalUriHandler.current

    Card(
        onClick = { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}