package com.example.truehub.ui.services.containers.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.data.models.Virt
import com.example.truehub.ui.background.WavyGradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerInfoBottomSheet(
    container: Virt.ContainerResponse,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section with container name and status
            ContainerInfoHeader(container = container, onDismiss = onDismiss)
            ContainerDetailsContent(container = container)
        }
    }
}

@Composable
private fun ContainerInfoHeader(
    container: Virt.ContainerResponse,
    onDismiss: () -> Unit
) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
        WavyGradientBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Container Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = container.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${container.type.name} • ${container.status.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ContainerDetailsContent(container: Virt.ContainerResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Status & Resources Section
        ContainerInfoSection(
            title = "Status & Resources",
            icon = Icons.Default.DeveloperBoard
        ) {
            val statusColor = when (container.status.name.uppercase()) {
                "RUNNING" -> MaterialTheme.colorScheme.primaryContainer
                "STOPPED" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
            val onStatusColor = when (container.status.name.uppercase()) {
                "RUNNING" -> MaterialTheme.colorScheme.onPrimaryContainer
                "STOPPED" -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }

            InfoCard(
                title = "Status",
                value = container.status.name.replaceFirstChar { it.uppercase() },
                icon = Icons.Default.PowerSettingsNew,
                containerColor = statusColor,
                contentColor = onStatusColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            ContainerInfoRow("CPU", container.cpu ?: "N/A")
            ContainerInfoRow("Memory", container.memory?.let {
                val memoryInMB = it.toDouble() / 1024.0 / 1024.0
                "%.2f MB".format(memoryInMB)
            } ?: "N/A")
        }


        // Basic Information Section
        ContainerInfoSection(
            title = "Basic Information",
            icon = Icons.Default.Info
        ) {
            ContainerInfoRow("ID", container.id)
            ContainerInfoRow("Type", container.type.name)
            container.image.os?.let { os ->
                ContainerInfoRow("Operating System", os)
            }
            container.image.release?.let { release ->
                ContainerInfoRow("Release", release)
            }
        }

        // Configuration Section
        ContainerInfoSection(
            title = "Configuration",
            icon = Icons.Default.Settings
        ) {
            ContainerInfoRow("Autostart", if (container.autostart) "Enabled" else "Disabled")
            ContainerInfoRow("Storage Pool", container.storage_pool ?: "N/A")
        }

        // VNC / Remote Access Section (only if enabled)
        if (container.vnc_enabled) {
            ContainerInfoSection(
                title = "Remote Access",
                icon = Icons.Default.DesktopWindows
            ) {
                InfoCard(
                    title = "VNC",
                    value = "Enabled",
                    icon = Icons.Default.ConnectedTv,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                ContainerInfoRow("VNC Port", container.vnc_port?.toString() ?: "N/A")
            }
        }
    }
}


@Composable
private fun ContainerInfoSection(
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
private fun ContainerInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
private fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}