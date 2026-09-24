package com.imnotndesh.truehub.ui.services.containers.details

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.models.Virt.ContainerResponse

@Composable
fun ContainerInfoPane(
    container: ContainerResponse,
    onClose: () -> Unit,
    showInlineHeader: Boolean = true,
    bottomActions: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (showInlineHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close Pane")
                    }
                }
            }

            ExpressiveSection(title = "Status", icon = Icons.Default.Info) {
                val statusStr = container.status.toString().uppercase()
                val isRunning = statusStr == "RUNNING" || statusStr == "ACTIVE"
                val statusColor = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                val onStatusColor = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current State",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onStatusColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = statusStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onStatusColor
                            )
                        }
                    }
                }
            }

            ExpressiveSection(title = "System Details", icon = Icons.Default.Computer) {
                ExpressiveInfoCard {
                    InfoRow(label = "Container ID", value = container.id.take(12))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Type", value = container.type.toString())
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(label = "Autostart", value = if (container.autostart) "Enabled" else "Disabled")

                    container.secure_boot?.let { secureBoot ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(label = "Secure Boot", value = if (secureBoot) "Enabled" else "Disabled")
                    }

                    if (container.aliases.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        InfoRow(label = "Aliases", value = container.aliases.joinToString { it.toString() })
                    }
                }
            }

            if (container.cpu != null || container.memory != null) {
                ExpressiveSection(title = "Resources", icon = Icons.Default.Memory) {
                    ExpressiveInfoCard {
                        container.cpu?.let { cpu ->
                            InfoRow(label = "CPU Allocation", value = cpu)
                        }

                        if (container.cpu != null && container.memory != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }

                        container.memory?.let { memory ->
                            val memoryStr = if (memory >= 1024) {
                                "${String.format(Locale.toString(), memory / 1024.0)} GB"
                            } else {
                                "$memory MB"
                            }
                            InfoRow(label = "Memory Allocation", value = memoryStr)
                        }
                    }
                }
            }

            if (container.storage_pool != null || container.root_disk_size != null) {
                ExpressiveSection(title = "Storage", icon = Icons.Default.Storage) {
                    ExpressiveInfoCard {
                        container.storage_pool?.let { pool ->
                            InfoRow(label = "Storage Pool", value = pool)
                        }

                        if (container.storage_pool != null && container.root_disk_size != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        }

                        container.root_disk_size?.let { size ->
                            InfoRow(label = "Root Disk Size", value = "$size GB")
                        }

                        container.root_disk_io_bus?.let { bus ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "IO Bus", value = bus.toString())
                        }
                    }
                }
            }

            if (container.vnc_enabled) {
                ExpressiveSection(title = "Remote Access (VNC)", icon = Icons.Default.DesktopWindows) {
                    ExpressiveInfoCard {
                        InfoRow(label = "VNC Status", value = "Enabled")
                        container.vnc_port?.let { port ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Port", value = port.toString())
                        }
                        if (container.vnc_password != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            InfoRow(label = "Password", value = "••••••••")
                        }
                    }
                }
            }

            if (container.environment.isNotEmpty()) {
                ExpressiveSection(title = "Environment Variables", icon = Icons.Default.DeveloperBoard) {
                    ExpressiveInfoCard {
                        val entries = container.environment.entries.toList()
                        entries.forEachIndexed { index, entry ->
                            InfoRow(label = entry.key, value = entry.value)
                            if (index < entries.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            }
                        }
                    }
                }
            }

            if (bottomActions != null) {
                bottomActions()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExpressiveSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
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
private fun ExpressiveInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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