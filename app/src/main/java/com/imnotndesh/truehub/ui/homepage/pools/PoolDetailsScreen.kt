package com.imnotndesh.truehub.ui.homepage.pools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System.Pool
import com.imnotndesh.truehub.data.models.System.PoolDevice
import com.imnotndesh.truehub.data.models.System.PoolScan
import com.imnotndesh.truehub.data.models.System.PoolTopology
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PoolDetailsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit
) {
    val viewModel: PoolDetailsViewModel = viewModel(
        factory = PoolDetailsViewModel.PoolDetailsViewModelFactory(manager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPool: Pool? by remember { mutableStateOf(null) }

    when (val state = uiState) {
        is PoolDetailsUiState.Loading -> {
            isLoading = true
            isRefreshing = false
            error = null
        }
        is PoolDetailsUiState.Success -> {
            isLoading = false
            isRefreshing = state.isRefreshing
            error = null
            currentPool = state.pool
        }
        is PoolDetailsUiState.Error -> {
            isLoading = false
            isRefreshing = false
            error = state.message
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            UnifiedScreenHeader(
                title = "Pool Details",
                subtitle = "${currentPool?.name ?: "Current"} Details",
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                error = error,
                onRefresh = { viewModel.refreshPool() },
                onDismissError = { error = null },
                manager = manager,
                onBackPressed = onNavigateBack
            )

            when (val state = uiState) {
                is PoolDetailsUiState.Loading -> LoadingScreen("Loading Pool Details...")
                is PoolDetailsUiState.Error -> {} // Error shown in header
                is PoolDetailsUiState.Success -> PoolDetailsContent(pool = state.pool)
            }
        }
    }
}

@Composable
private fun PoolDetailsContent(pool: Pool) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        PoolInfoHeader(pool = pool)
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PoolStorageUsageCard(pool = pool)
            PoolStatusSection(pool = pool)
            pool.scan?.let {
                PoolScanSection(scan = it)
            }
            PoolTopologySection(topology = pool.topology)
        }
    }
}

@Composable
private fun PoolInfoHeader(pool: Pool) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 24.dp, topEnd = 24.dp))
            .padding(horizontal = 10.dp)
    ) {
        WavyGradientBackground {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = pool.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${pool.status} • Total: ${pool.size.toGigabytesString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
fun Long.toGigabytesString(): String {
    if (this == 0L) {
        return "0 GB"
    }

    val gibibyte = 1024.0 * 1024.0 * 1024.0
    val gbValue = this.toDouble() / gibibyte

    val decimalFormat = DecimalFormat("#.#")

    return "${decimalFormat.format(gbValue)} GB"
}

@Composable
private fun PoolStorageUsageCard(pool: Pool) {
    val usedPercentage = if (pool.size > 0) (pool.allocated.toFloat() / pool.size.toFloat()) else 0f
    val progressColor = when {
        usedPercentage > 0.9f -> MaterialTheme.colorScheme.error
        usedPercentage > 0.75f -> Color(0xFFF57C00) // Orange
        else -> MaterialTheme.colorScheme.primary
    }

    // Animated progress
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
        label = "storageProgress"
    )

    LaunchedEffect(usedPercentage) {
        targetProgress = usedPercentage
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage Usage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${DecimalFormat("#.#").format(usedPercentage * 100)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Used: ${pool.allocated.toGigabytesString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Free: ${pool.free.toGigabytesString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PoolInfoSection(
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
private fun PoolInfoRow(label: String, value: String) {
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
private fun PoolStatusSection(pool: Pool) {
    PoolInfoSection(title = "Status & Health", icon = Icons.Default.Info) {
        val statusColor = when {
            !pool.healthy -> MaterialTheme.colorScheme.error
            pool.warning -> Color(0xFFF57C00) // Orange
            else -> Color(0xFF2E7D32) // Green
        }
        val statusIcon = when {
            !pool.healthy -> Icons.Default.Error
            pool.warning -> Icons.Default.Warning
            else -> Icons.Default.CheckCircle
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pool.status,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    if (!pool.status_detail.isNullOrEmpty()) {
                        Text(
                            text = pool.status_detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        PoolInfoRow("Fragmentation", pool.fragmentation)
        PoolInfoRow("Autotrim", pool.autotrim.value)
    }
}

@Composable
private fun PoolScanSection(scan: PoolScan) {
    fun formatEpoch(timeMap: Map<String, Long>?): String {
        val epochSeconds = timeMap?.get("\$date")?.div(1000) ?: return "N/A"
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault())
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }

    // Animated scan progress
    var targetScanProgress by remember { mutableFloatStateOf(0f) }
    val animatedScanProgress by animateFloatAsState(
        targetValue = targetScanProgress,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
        label = "scanProgress"
    )

    LaunchedEffect(scan.percentage) {
        targetScanProgress = (scan.percentage ?: 0.0).toFloat() / 100f
    }

    PoolInfoSection(title = "Last Scan", icon = Icons.Default.Scanner) {
        PoolInfoRow("Function", scan.function ?: "None Found")
        PoolInfoRow("State", scan.state ?: "None Found")
        PoolInfoRow("Started", formatEpoch(scan.start_time))
        PoolInfoRow("Ended", formatEpoch(scan.end_time))
        PoolInfoRow("Errors", scan.errors.toString())

        if (scan.state == "SCANNING") {
            Spacer(modifier = Modifier.height(8.dp))
            if (scan.percentage != null){
                LinearProgressIndicator(
                    progress = { animatedScanProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Progress: ${"%.2f".format(scan.percentage)}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    scan.total_secs_left?.let {
                        Text(
                            text = "Time left: ${it / 60}m ${it % 60}s",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }else{
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Cannot Find Storage Usage statistic",
                        style = MaterialTheme.typography.bodySmall
                            .copy(color = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

@Composable
private fun PoolTopologySection(topology: PoolTopology) {
    PoolInfoSection(title = "Topology", icon = Icons.Default.DataObject) {
        DeviceGroup("Data", topology.data)
        DeviceGroup("Log", topology.log)
        DeviceGroup("Cache", topology.cache)
        DeviceGroup("Spare", topology.spare)
        DeviceGroup("Special", topology.special)
        DeviceGroup("Dedup", topology.dedup)
    }
}

@Composable
private fun DeviceGroup(title: String, devices: List<PoolDevice>) {
    if (devices.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )
        devices.forEach { device ->
            DeviceItem(device = device, level = 0)
        }
    }
}

@Composable
private fun DeviceItem(device: PoolDevice, level: Int) {
    val statusColor = if (device.status == "ONLINE") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 12).dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val icon = if (device.type == "DISK") Icons.Default.Storage else Icons.Default.FolderZip
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (device.type == "DISK") device.disk ?: device.name else device.type,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = device.status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            device.stats?.let { stats ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatChip("Reads", stats.read_errors.toString(), stats.read_errors > 0)
                    StatChip("Writes", stats.write_errors.toString(), stats.write_errors > 0)
                    StatChip("Checksum", stats.checksum_errors.toString(), stats.checksum_errors > 0)
                }
            }
        }
    }

    device.children.forEach { child ->
        DeviceItem(device = child, level = level + 1)
    }
}

@Composable
fun StatChip(label: String, value: String, isError: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}