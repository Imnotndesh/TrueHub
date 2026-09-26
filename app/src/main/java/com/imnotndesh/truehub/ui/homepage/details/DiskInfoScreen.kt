package com.imnotndesh.truehub.ui.homepage.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.R.drawable.ic_drive
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiskInfoScreen(
    disks: List<System.DiskDetails>,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentDisk = disks[selectedTab]

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Disks",
            subtitle = currentDisk.model ?: currentDisk.name,
            isLoading = false,
            isRefreshing = false,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        if (disks.size > 1) {
            DiskSelectorRow(
                disks = disks,
                selectedIndex = selectedTab,
                onDiskSelected = { selectedTab = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DiskHeroCard(disk = currentDisk)
            }

            item {
                DiskHealthBanner(disk = currentDisk)
            }

            item {
                DiskInfoSection(title = "Basic Information", icon = Icons.Default.Info) {
                    ExpressiveInfoGrid(
                        items = listOf(
                            "Name" to currentDisk.name,
                            "Model" to (currentDisk.model ?: "Generic"),
                            "Serial" to currentDisk.serial,
                            "Bus" to currentDisk.bus,
                            "Size" to "${currentDisk.size / (1024 * 1024 * 1024)} GB",
                            "Type" to currentDisk.type
                        )
                    )
                }
            }

            item {
                DiskInfoSection(title = "Status & Health", icon = Icons.Default.HealthAndSafety) {
                    SmartStatusCard(disk = currentDisk)
                    currentDisk.supports_smart?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        DiskInfoRow("Supports SMART", it.toString())
                    }
                    if (!currentDisk.smartoptions.isNullOrEmpty()) {
                        DiskInfoRow("SMART Options", currentDisk.smartoptions)
                    }
                }
            }

            item {
                DiskInfoSection(title = "Power Management", icon = Icons.Default.Bolt) {
                    PowerManagementCard(disk = currentDisk)
                }
            }

            currentDisk.pool?.let { pool ->
                item {
                    DiskInfoSection(title = "Pool Association", icon = Icons.Default.Storage) {
                        DiskPoolCard(poolName = pool, zfsGuid = currentDisk.zfs_guid)
                    }
                }
            }

            item {
                DiskInfoSection(title = "Technical Details", icon = Icons.Default.Settings) {
                    DiskTechnicalCard(disk = currentDisk)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiskSelectorRow(
    disks: List<System.DiskDetails>,
    selectedIndex: Int,
    onDiskSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(disks.size) { index ->
            val disk = disks[index]
            val isSelected = selectedIndex == index
            val animColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                label = "chipColor"
            )
            FilterChip(
                selected = isSelected,
                onClick = { onDiskSelected(index) },
                label = {
                    Text(
                        text = disk.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = animColor,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun DiskHeroCard(disk: System.DiskDetails) {
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = ic_drive),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disk.model ?: "Generic Drive",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = disk.serial,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiskBadge(
                        label = disk.type,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    DiskBadge(
                        label = "${disk.size / (1024 * 1024 * 1024)} GB",
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    DiskBadge(
                        label = disk.bus,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DiskBadge(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DiskHealthBanner(disk: System.DiskDetails) {
    val isHealthy = disk.togglesmart != false
    val bannerColor by animateColorAsState(
        targetValue = if (isHealthy) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "bannerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isHealthy) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "bannerContent"
    )
    val healthProgress by animateFloatAsState(
        targetValue = if (isHealthy) 1f else 0.3f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "healthProgress"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isHealthy) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Drive Health",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                Text(
                    text = if (isHealthy) "Healthy" else "Attention Needed",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
            LinearProgressIndicator(
                progress = { healthProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SmartStatusCard(disk: System.DiskDetails) {
    val isEnabled = disk.togglesmart ?: true
    val isUnknown = disk.togglesmart == null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUnknown -> MaterialTheme.colorScheme.surfaceContainerHigh
                isEnabled -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when {
                        isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant
                        isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SMART Monitoring",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant
                        isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Text(
                    text = when {
                        isUnknown -> "Status unavailable"
                        isEnabled -> "Active and monitoring"
                        else -> "Disabled — consider enabling"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    }
                )
            }
            Surface(
                color = when {
                    isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                    isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = if (isUnknown) "Unknown" else if (isEnabled) "ON" else "OFF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isUnknown -> MaterialTheme.colorScheme.onSurfaceVariant
                        isEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PowerManagementCard(disk: System.DiskDetails) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            PowerManagementRow(
                icon = Icons.Default.Bolt,
                label = "Transfer Mode",
                value = disk.transfermode
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            PowerManagementRow(
                icon = Icons.Default.Settings,
                label = "Standby Mode",
                value = disk.hddstandby
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            PowerManagementRow(
                icon = Icons.Default.Memory,
                label = "APM Level",
                value = disk.advpowermgmt
            )
        }
    }
}

@Composable
private fun PowerManagementRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExpressiveInfoGrid(items: List<Pair<String, String>>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            items.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiskInfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
private fun DiskInfoRow(label: String, value: String) {
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
private fun DiskPoolCard(poolName: String, zfsGuid: String?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poolName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                zfsGuid?.let {
                    Text(
                        text = "ZFS GUID: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiskTechnicalCard(disk: System.DiskDetails) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Technical Specifications",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TechnicalDetailRow("Bus Interface", disk.bus)
                TechnicalDetailRow("Disk Type", disk.type)
                TechnicalDetailRow("Transfer Mode", disk.transfermode)
            }
        }
    }
}

@Composable
private fun TechnicalDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}