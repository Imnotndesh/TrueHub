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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.models.Virt.ContainerResponse
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveBadge
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveDivider
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.InfoRow
import com.imnotndesh.truehub.ui.services.containers.containerShape
import com.imnotndesh.truehub.ui.services.containers.containerStatusIcon
import com.imnotndesh.truehub.ui.services.containers.containerStatusLabel
import com.imnotndesh.truehub.ui.services.containers.containerStatusTone
import com.imnotndesh.truehub.ui.services.containers.containerTypeContainerColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeContentColor
import com.imnotndesh.truehub.ui.services.containers.containerTypeIcon
import com.imnotndesh.truehub.ui.services.containers.formatMemory

@Composable
fun ContainerInfoPane(
    container: ContainerResponse,
    onClose: () -> Unit,
    showInlineHeader: Boolean = true,
    contentTopPadding: Dp = 24.dp,
    topContent: (@Composable ColumnScope.() -> Unit)? = null,
    bottomActions: (@Composable ColumnScope.() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (showInlineHeader) 2.dp else 0.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showInlineHeader) {
                ContainerPaneHeader(container = container, onClose = onClose)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = contentTopPadding, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                topContent?.invoke(this)

                ExpressiveSection(title = "Status", icon = Icons.Default.Info) {
                    ExpressiveInfoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current State",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            ExpressiveStatusChip(
                                label = containerStatusLabel(container.status),
                                tone = containerStatusTone(container.status),
                                icon = containerStatusIcon(container.status)
                            )
                        }
                    }
                }

                ExpressiveSection(title = "System Details", icon = Icons.Default.Computer) {
                    ExpressiveInfoCard {
                        InfoRow(label = "Container ID", value = container.id.take(12))
                        ExpressiveDivider()
                        InfoRow(label = "Type", value = container.type.toString())
                        ExpressiveDivider()
                        InfoRow(label = "Autostart", value = if (container.autostart) "Enabled" else "Disabled")

                        container.secure_boot?.let { secureBoot ->
                            ExpressiveDivider()
                            InfoRow(label = "Secure Boot", value = if (secureBoot) "Enabled" else "Disabled")
                        }

                        if (container.aliases.isNotEmpty()) {
                            ExpressiveDivider()
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
                                ExpressiveDivider()
                            }

                            container.memory?.let { memory ->
                                InfoRow(label = "Memory Allocation", value = formatMemory(memory))
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
                                ExpressiveDivider()
                            }

                            container.root_disk_size?.let { size ->
                                InfoRow(label = "Root Disk Size", value = "$size GB")
                            }

                            container.root_disk_io_bus?.let { bus ->
                                ExpressiveDivider()
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
                                ExpressiveDivider()
                                InfoRow(label = "Port", value = port.toString())
                            }
                            if (container.vnc_password != null) {
                                ExpressiveDivider()
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
                                    ExpressiveDivider()
                                }
                            }
                        }
                    }
                }

                bottomActions?.invoke(this)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ContainerPaneHeader(
    container: ContainerResponse,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        WavyGradientBackground {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveShapeBox(
                    shape = containerShape(container.type),
                    containerColor = containerTypeContainerColor(container.type),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = containerTypeIcon(container.type),
                        contentDescription = null,
                        tint = containerTypeContentColor(container.type),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = container.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ExpressiveBadge(
                    label = containerStatusLabel(container.status),
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
