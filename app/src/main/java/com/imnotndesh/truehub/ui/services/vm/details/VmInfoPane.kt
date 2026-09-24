package com.imnotndesh.truehub.ui.services.vm.details

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Settings
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
import com.imnotndesh.truehub.data.helpers.VirtualizationOperation
import com.imnotndesh.truehub.data.helpers.virtualizationOperationLabel
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveBadge
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveDivider
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveStatusChip
import com.imnotndesh.truehub.ui.components.expressive.InfoRow
import com.imnotndesh.truehub.ui.services.vm.vmShape
import com.imnotndesh.truehub.ui.services.vm.vmStatusIcon
import com.imnotndesh.truehub.ui.services.vm.vmStatusLabel
import com.imnotndesh.truehub.ui.services.vm.vmStatusTone

@Composable
fun VmInfoPane(
    vm: Vm.VmQueryResponse,
    onClose: () -> Unit,
    showInlineHeader: Boolean = true,
    contentTopPadding: Dp = 24.dp,
    operation: VirtualizationOperation? = null,
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
                VmPaneHeader(vm = vm, onClose = onClose)
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
                        Column {
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
                                    label = vmStatusLabel(vm.status.state),
                                    tone = vmStatusTone(vm.status.state),
                                    icon = vmStatusIcon(vm.status.state)
                                )
                            }
                            operation?.let { current ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = current.job.progress?.description?.takeIf { it.isNotBlank() }
                                            ?: virtualizationOperationLabel(current.operation),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${current.job.progress?.percent ?: 0}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                ExpressiveSection(title = "System Configuration", icon = Icons.Default.Settings) {
                    ExpressiveInfoCard {
                        InfoRow(label = "Bootloader", value = vm.bootloader)
                        ExpressiveDivider()
                        InfoRow(label = "Autostart", value = if (vm.autostart) "Enabled" else "Disabled")
                        ExpressiveDivider()
                        InfoRow(label = "CPU Mode", value = vm.cpu_mode?.name ?: "Unknown")
                        vm.cpu_model?.let {
                            ExpressiveDivider()
                            InfoRow(label = "CPU Model", value = it)
                        }
                        vm.arch_type?.let {
                            ExpressiveDivider()
                            InfoRow(label = "Architecture", value = it)
                        }
                        vm.machine_type?.let {
                            ExpressiveDivider()
                            InfoRow(label = "Machine Type", value = it)
                        }
                        ExpressiveDivider()
                        InfoRow(label = "Secure Boot", value = if (vm.enable_secure_boot) "Enabled" else "Disabled")
                        ExpressiveDivider()
                        InfoRow(
                            label = "Trusted Platform Module",
                            value = if (vm.trusted_platform_module) "Enabled" else "Disabled"
                        )
                        ExpressiveDivider()
                        InfoRow(label = "Time", value = vm.time)
                        ExpressiveDivider()
                        InfoRow(label = "Shutdown Timeout", value = "${vm.shutdown_timeout}s")
                        vm.description.takeIf { it.isNotBlank() }?.let {
                            ExpressiveDivider()
                            InfoRow(label = "Description", value = it)
                        }
                    }
                }

                ExpressiveSection(title = "Hardware Resources", icon = Icons.Default.Memory) {
                    ExpressiveInfoCard {
                        InfoRow(label = "Virtual CPUs", value = vm.vcpus.toString())
                        ExpressiveDivider()
                        InfoRow(label = "Cores", value = vm.cores.toString())
                        ExpressiveDivider()
                        InfoRow(label = "Threads", value = vm.threads.toString())
                        ExpressiveDivider()
                        InfoRow(label = "Allocated Memory", value = formatVmMemory(vm.memory))
                        vm.min_memory?.let {
                            ExpressiveDivider()
                            InfoRow(label = "Minimum Memory", value = formatVmMemory(it))
                        }
                        vm.cpuset?.takeIf { it.isNotBlank() }?.let {
                            ExpressiveDivider()
                            InfoRow(label = "CPU Set", value = it)
                        }
                        vm.nodeset?.takeIf { it.isNotBlank() }?.let {
                            ExpressiveDivider()
                            InfoRow(label = "NUMA Node Set", value = it)
                        }
                    }
                }

                ExpressiveSection(title = "Display", icon = Icons.Default.Monitor) {
                    ExpressiveInfoCard {
                        InfoRow(
                            label = "Display",
                            value = if (vm.display_available) "Available" else "Not available"
                        )
                    }
                }

                bottomActions?.invoke(this)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VmPaneHeader(vm: Vm.VmQueryResponse, onClose: () -> Unit) {
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
                    shape = vmShape(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = vm.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ExpressiveBadge(
                    label = vmStatusLabel(vm.status.state),
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

internal fun formatVmMemory(memoryMb: Int): String {
    return if (memoryMb >= 1024) {
        String.format(java.util.Locale.US, "%.1f GB", memoryMb / 1024.0)
    } else {
        "$memoryMb MB"
    }
}
