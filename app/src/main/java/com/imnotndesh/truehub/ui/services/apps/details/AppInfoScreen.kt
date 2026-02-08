package com.imnotndesh.truehub.ui.services.apps.details

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun AppInfoScreen(
    app: Apps.AppQueryResponse,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = app.metadata?.title ?: app.name,
                subtitle = "App Details",
                isLoading = false,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExpressiveSection(title = "Basic Information", icon = Icons.Default.Info) {
                ExpressiveInfoCard {
                    InfoRow(label = "App Name", value = app.metadata?.title ?: app.name)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "ID", value = app.id)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(
                        label = "Version",
                        value = app.humanVersion ?: app.version ?: "Unknown"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Status", value = app.state.replaceFirstChar { it.uppercase() })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Catalog", value = app.metadata?.train ?: "Unknown") // Using metadata.train as per struct

                    if (app.upgrade_available) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(label = "Latest Version", value = app.latestVersion ?: "Update Available")
                    }

                    app.metadata?.dateAdded?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(label = "Date Added", value = formatDate(it))
                    }
                    app.metadata?.lastUpdate?.let {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(label = "Last Updated", value = formatDate(it))
                    }
                }
            }

            app.metadata?.description?.let { description ->
                ExpressiveSection(title = "Description", icon = Icons.Default.Description) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            if (!app.metadata?.categories.isNullOrEmpty() || !app.metadata?.keywords.isNullOrEmpty()) {
                ExpressiveSection(title = "Categories & Tags", icon = Icons.Default.Tag) {
                    app.metadata?.categories?.let { categories ->
                        ServiceInfoChipGroup(
                            title = "Categories",
                            items = categories,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    app.metadata?.keywords?.let { keywords ->
                        if (keywords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
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
            app.portals?.let { portals ->
                if (portals.isNotEmpty()) {
                    ExpressiveSection(title = "Web Portals", icon = Icons.AutoMirrored.Filled.Launch) {
                        portals.forEach { (name, url) ->
                            ServicePortalCard(name = name, url = url)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            app.activeWorkloads?.usedPorts?.let { ports ->
                if (ports.isNotEmpty()) {
                    ExpressiveSection(title = "Network & Ports", icon = Icons.Default.NetworkCheck) {
                        ports.forEach { port ->
                            ServicePortCard(port = port)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            app.activeWorkloads?.containerDetails?.let { containers ->
                if (containers.isNotEmpty()) {
                    ExpressiveSection(title = "Containers", icon = Icons.Default.Apps) {
                        containers.forEach { container ->
                            ServiceContainerCard(container = container)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            val volumes = app.activeWorkloads?.volumes
            val hostMounts = app.metadata?.hostMounts

            if (!volumes.isNullOrEmpty() || !hostMounts.isNullOrEmpty()) {
                ExpressiveSection(title = "Storage & Mounts", icon = Icons.Default.Storage) {
                    volumes?.forEach { volume ->
                        ServiceVolumeCard(volume = volume)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    hostMounts?.forEach { mount ->
                        ServiceHostMountCard(mount = mount)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            app.metadata?.runAsContext?.let { contexts ->
                if (contexts.isNotEmpty()) {
                    ExpressiveSection(title = "Security Context", icon = Icons.Default.AccountBox) {
                        contexts.forEach { context ->
                            ServiceRunAsContextCard(context = context)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            app.metadata?.capabilities?.let { capabilities ->
                if (capabilities.isNotEmpty()) {
                    ExpressiveSection(title = "Capabilities", icon = Icons.Default.Build) {
                        capabilities.forEach { capability ->
                            ServiceCapabilityCard(capability = capability)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            app.metadata?.maintainers?.let { maintainers ->
                if (maintainers.isNotEmpty()) {
                    ExpressiveSection(title = "Maintainers", icon = Icons.Default.Person) {
                        maintainers.forEach { maintainer ->
                            ServiceMaintainerCard(maintainer = maintainer)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            if (app.metadata?.home != null || !app.metadata?.sources.isNullOrEmpty()) {
                ExpressiveSection(title = "Resources", icon = Icons.Default.Link) {
                    app.metadata.home?.let { home ->
                        LinkButton(name = "Homepage", url = home, icon = Icons.Default.Home)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    app.metadata.sources?.forEach { source ->
                        LinkButton(name = "Source Code", url = source, icon = Icons.Default.Code)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    app.metadata.changelogUrl?.let { changelog ->
                        LinkButton(name = "Changelog", url = changelog, icon = Icons.Default.Description)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            app.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    ExpressiveSection(title = "Notes", icon = Icons.AutoMirrored.Filled.Note) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                MarkdownText(
                                    markdown = notes,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        dateString
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun ExpressiveSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
fun ExpressiveInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
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
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
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
                        labelColor = contentColor,
                        disabledContainerColor = containerColor,
                        disabledLabelColor = contentColor
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePortCard(port: Apps.UsedPort) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NetworkCheck,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Port ${port.containerPort} (${port.protocol.uppercase()})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            port.hostPorts.forEach { hostPort ->
                Text(
                    text = "→ Host: ${hostPort.hostIp}:${hostPort.hostPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 2.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ServicePortalCard(name: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Surface(
        onClick = { uriHandler.openUri(url) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Launch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = container.serviceName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Image: ${container.image}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "State: ${container.state}",
                style = MaterialTheme.typography.bodySmall,
                color = if (container.state.equals("running", true))
                    Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ServiceVolumeCard(volume: Apps.Volume) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${volume.source} → ${volume.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                volume.mode?.let { mode ->
                    Text(
                        text = "Mode: $mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceHostMountCard(mount: Apps.HostMount) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Host Path: ${mount.hostPath ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                mount.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceRunAsContextCard(context: Apps.RunAsContext) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User: ${context.userName ?: context.uid ?: "Root"}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Group: ${context.groupName ?: context.gid ?: "Root"}",
                style = MaterialTheme.typography.bodySmall
            )
            context.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceCapabilityCard(capability: Apps.Capability) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = capability.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = capability.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ServiceMaintainerCard(maintainer: Apps.Maintainer) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = maintainer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
fun LinkButton(name: String, url: String, icon: ImageVector) {
    val uriHandler = LocalUriHandler.current
    Surface(
        onClick = { uriHandler.openUri(url) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}