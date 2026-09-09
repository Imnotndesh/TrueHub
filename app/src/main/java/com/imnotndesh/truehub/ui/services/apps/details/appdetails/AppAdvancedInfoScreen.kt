package com.imnotndesh.truehub.ui.services.apps.details.appdetails

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.utils.ScreenshotViewer
import com.imnotndesh.truehub.ui.utils.withRoutableServerHost
import dev.jeziellago.compose.markdowntext.MarkdownText

/**
 * Deep-dive screen for a single installed app, backed by `app.get_instance`.
 *
 * Shows the extra `get_instance` information (workloads, containers, ports, networks,
 * storage, security context, capabilities, screenshots, notes, …). The Screenshots and
 * Notes sections moved here from the main [com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppInfoScreen]
 * so the main info page/pane stay decluttered.
 */
@Composable
fun AppAdvancedInfoScreen(
    manager: TrueNASApiManager,
    appId: String,
    onNavigateBack: () -> Unit
) {
    var instance by remember { mutableStateOf<Apps.AppQueryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeScreenshotIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(appId) {
        when (val result = manager.apps.getAppInstanceWithResult(appId)) {
            is ApiResult.Success -> instance = result.data
            is ApiResult.Error -> error = result.message ?: "Failed to load instance"
            is ApiResult.Loading -> {}
        }
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = "Advanced Info",
                subtitle = instance?.metadata?.title ?: appId,
                isLoading = isLoading,
                isRefreshing = false,
                error = error,
                onDismissError = { error = null },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> LoadingScreen("Loading application details…")
            error != null && instance == null -> NullContent()
            instance != null -> AdvancedInstanceContent(
                instance = instance!!,
                serverBaseHttpUrl = manager.serverBaseHttpUrl,
                modifiers = Modifier.padding(innerPadding),
                onScreenshotClick = { activeScreenshotIndex = it }
            )
        }
    }

    val screenshots = instance?.metadata?.screenshots
    if (activeScreenshotIndex != null && !screenshots.isNullOrEmpty()) {
        ScreenshotViewer(
            screenshots = screenshots,
            initialIndex = activeScreenshotIndex!!,
            onDismiss = { activeScreenshotIndex = null }
        )
    }
}

@Composable
private fun NullContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No application data returned.\nCheck that the app is installed and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AdvancedInstanceContent(
    instance: Apps.AppQueryResponse,
    serverBaseHttpUrl: String,
    modifiers: Modifier,
    onScreenshotClick: (Int) -> Unit
) {
    Column(
        modifier = modifiers
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header summary card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = instance.metadata?.title ?: instance.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: ${instance.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Version: ${instance.humanVersion ?: instance.version ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = runStateContainerColor(instance.state),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = instance.state,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Workloads overview — top-level get_instance summary (exhausts active_workloads).
        val workloads = instance.activeWorkloads
        ExpressiveSection(title = "Workloads Overview", icon = Icons.Default.NetworkCheck) {
            ExpressiveInfoCard {
                InfoRow(label = "Containers", value = "${workloads?.containers ?: 0}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                workloads?.usedHostIps?.let { ips ->
                    InfoRow(label = "Host IPs", value = ips.size.toString())
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                InfoRow(label = "Image Updates", value = if (instance.image_Updates_available) "Available" else "Up to date")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                InfoRow(label = "Type", value = if (instance.customApp) "Custom Application" else "Catalog")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                InfoRow(label = "Migrated", value = if (instance.migratedFromKubernetes) "From Kubernetes" else "No")
            }
        }

        // Screenshots (moved here to declutter the main screen)
        instance.metadata?.screenshots?.let { screenshots ->
            if (screenshots.isNotEmpty()) {
                ExpressiveSection(title = "Screenshots", icon = Icons.Default.Photo) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(screenshots) { index, url ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(140.dp)
                                    .clickable { onScreenshotClick(index) }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Screenshot $index",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Network & Ports
        instance.activeWorkloads?.let { workloads ->
            val hasPorts = !workloads.usedPorts.isNullOrEmpty()
            val hasNetworks = !workloads.networks.isNullOrEmpty()
            val hasHostIps = !workloads.usedHostIps.isNullOrEmpty()
            if (hasPorts || hasNetworks || hasHostIps) {
                ExpressiveSection(title = "Network & Ports", icon = Icons.Default.NetworkCheck) {
                    if (hasPorts) {
                        Text(
                            text = "Exposed Ports",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        workloads.usedPorts!!.forEach { port ->
                            AdvancedPortCard(port = port)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    if (hasHostIps) {
                        if (hasPorts) Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Host IPs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                workloads.usedHostIps!!.forEach { ip ->
                                    Text(
                                        text = ip,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    if (hasNetworks) {
                        if (hasPorts || hasHostIps) Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Docker Networks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        workloads.networks!!.forEach { network ->
                            AdvancedNetworkCard(network = network)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Containers
        instance.activeWorkloads?.containerDetails?.let { containers ->
            if (containers.isNotEmpty()) {
                ExpressiveSection(title = "Containers", icon = Icons.Default.Apps) {
                    containers.forEach { container ->
                        AdvancedContainerCard(container = container)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Container Images
        instance.activeWorkloads?.images?.let { images ->
            if (images.isNotEmpty()) {
                ExpressiveSection(title = "Container Images", icon = Icons.Default.Image) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            images.forEachIndexed { index, image ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = image,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (index < images.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Storage & Mounts
        val volumes = instance.activeWorkloads?.volumes
        val hostMounts = instance.metadata?.hostMounts
        if (!volumes.isNullOrEmpty() || !hostMounts.isNullOrEmpty()) {
            ExpressiveSection(title = "Storage & Mounts", icon = Icons.Default.Storage) {
                volumes?.forEach { volume ->
                    AdvancedVolumeCard(volume = volume)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                hostMounts?.forEach { mount ->
                    AdvancedHostMountCard(mount = mount)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Security Context
        instance.metadata?.runAsContext?.let { contexts ->
            if (contexts.isNotEmpty()) {
                ExpressiveSection(title = "Security Context", icon = Icons.Default.AccountBox) {
                    contexts.forEach { ctx ->
                        AdvancedRunAsContextCard(context = ctx)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Capabilities
        instance.metadata?.capabilities?.let { capabilities ->
            if (capabilities.isNotEmpty()) {
                ExpressiveSection(title = "Capabilities", icon = Icons.Default.Build) {
                    capabilities.forEach { capability ->
                        AdvancedCapabilityCard(capability = capability)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Links
        if (instance.metadata?.home != null || !instance.metadata?.sources.isNullOrEmpty() || instance.metadata?.changelogUrl != null) {
            ExpressiveSection(title = "Links", icon = Icons.Default.Link) {
                instance.metadata.home?.let { home ->
                    AdvancedLinkCard(name = "Homepage", url = home, icon = Icons.Default.Home)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                instance.metadata.sources?.forEach { source ->
                    AdvancedLinkCard(name = "Source Code", url = source, icon = Icons.Default.Code)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                instance.metadata.changelogUrl?.let { changelog ->
                    AdvancedLinkCard(name = "Changelog", url = changelog, icon = Icons.Default.Description)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Web Portals
        instance.portals?.let { portals ->
            if (portals.isNotEmpty()) {
                ExpressiveSection(title = "Web Portals", icon = Icons.AutoMirrored.Filled.Launch) {
                    portals.forEach { (name, url) ->
                        AdvancedPortalCard(
                            name = name,
                            url = url.withRoutableServerHost(serverBaseHttpUrl)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Notes (moved here to declutter the main screen)
        instance.notes?.let { notes ->
            if (notes.isNotBlank()) {
                ExpressiveSection(title = "Notes", icon = Icons.AutoMirrored.Filled.Note) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
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
    }
}

@Composable
private fun runStateContainerColor(state: String): Color {
    return when (state.lowercase()) {
        "running" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
        "stopped", "exited" -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    }
}

@Composable
private fun AdvancedPortCard(port: Apps.UsedPort) {
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
                    modifier = Modifier.size(18.dp),
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
                    text = "→ ${hostPort.hostIp}:${hostPort.hostPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdvancedNetworkCard(network: Apps.Network) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(network.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                network.scope?.let { scope ->
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                        Text(scope, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            network.driver?.let { DetailLine("Driver", it) }
            network.scope?.let { DetailLine("Scope", it) }
            network.created?.let { DetailLine("Created", it) }
            network.id?.let { DetailLine("ID", it) }
            network.enableIPv6?.let { DetailLine("IPv6", if (it) "Enabled" else "Disabled") }
            network.ipam?.driver?.let { DetailLine("IPAM Driver", it) }
            network.ipam?.config?.let { configs ->
                configs.forEach { config ->
                    if (config.subnet != null || config.gateway != null) {
                        DetailLine(
                            "Subnet",
                            listOfNotNull(config.subnet, config.gateway?.let { "GW: $it" }).joinToString("  ·  ")
                        )
                    }
                }
            }
            val labels = network.labels
            if (!labels.isNullOrEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(
                    text = "Labels",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                labels.forEach { (k, v) ->
                    Text(
                        text = "$k: $v",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AdvancedContainerCard(container: Apps.ContainerDetail) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(container.serviceName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(
                    color = when (container.state.lowercase()) {
                        "running" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                        "stopped", "exited" -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(container.state.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                        color = when (container.state.lowercase()) {
                            "running" -> Color(0xFF2E7D32)
                            "stopped", "exited" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            Text("Image: ${container.image}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            container.portConfig?.let { ports ->
                if (ports.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ports.forEach { port ->
                        Text(
                            text = "${port.containerPort}/${port.protocol.uppercase()} → ${port.hostPorts.firstOrNull()?.let { "${it.hostIp}:${it.hostPort}" } ?: "unbound"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            container.volumeMounts?.let { mounts ->
                if (mounts.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    mounts.forEach { mount ->
                        Text("${mount.source} → ${mount.destination}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedVolumeCard(volume: Apps.Volume) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(volume.source, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                volume.type?.let { type ->
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(6.dp)) {
                        Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            Text("→ ${volume.destination}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            volume.mode?.let { Text("Mode: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) }
        }
    }
}

@Composable
private fun AdvancedHostMountCard(mount: Apps.HostMount) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Host Mount", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            mount.hostPath?.let { Text(it, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface) }
            mount.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun AdvancedRunAsContextCard(context: Apps.RunAsContext) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    context.uid?.let { Text("UID: $it", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) }
                    context.gid?.let { Text("GID: $it", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) }
                }
                val identity = listOfNotNull(context.userName, context.groupName).joinToString(" / ")
                if (identity.isNotEmpty()) Text(identity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                context.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) }
            }
        }
    }
}

@Composable
private fun AdvancedCapabilityCard(capability: Apps.Capability) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(capability.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (capability.description.isNotBlank()) Text(capability.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdvancedLinkCard(name: String, url: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdvancedPortalCard(name: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
