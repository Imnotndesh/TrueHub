package com.example.truehub.ui.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.truehub.data.api.TrueNASApiManager
import com.example.truehub.data.models.System
import com.example.truehub.data.models.Shares
import com.example.truehub.ui.alerts.AlertsBellButton
import com.example.truehub.ui.background.WavyGradientBackground
import com.example.truehub.ui.components.LoadingScreen
import com.example.truehub.ui.homepage.details.DiskInfoBottomSheet
import com.example.truehub.ui.homepage.details.MetricType
import com.example.truehub.ui.homepage.details.PerformanceBottomSheet
import com.example.truehub.ui.homepage.details.ShareInfoBottomSheet
import com.example.truehub.ui.homepage.details.ShareType
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    manager: TrueNASApiManager,
    onNavigateToSettings: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.HomeViewModelFactory(manager)
    )
    val uiState by viewModel.uiState.collectAsState()
    val loadAveragesState by viewModel.loadAverages.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header is always visible
            HeaderSection(
                manager = manager,
                onRefresh = { viewModel.refresh() },
                onShutdown = { viewModel.shutdownSystem(it) },
                isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing ?: false,
                onNavigateToSettings = onNavigateToSettings
            )

            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingScreen("Loading Homescreen")
                }
                is HomeUiState.Error -> ErrorScreen(
                    error = state.message,
                    canRetry = state.canRetry,
                    onRetry = { viewModel.retryLoad() },
                    onDismiss = { viewModel.clearError() }
                )
                is HomeUiState.Success -> HomeContent(
                    state = state,
                    onRefresh = { viewModel.refresh() },
                    onShutdown = { reason -> viewModel.shutdownSystem(reason) },
                    onRefreshGraph = { viewModel.loadPerformanceData() },
                    isConnectedStatus = isConnected,
                    loadAveragesState = loadAveragesState
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onRefresh: () -> Unit,
    onNavigateToSettings : () -> Unit,
    onShutdown: (String) -> Unit,
    manager : TrueNASApiManager,
    isRefreshing: Boolean
) {
    var showShutdownDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome back",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Connection status indicator
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertsBellButton(manager = manager)
                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings
                ){
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                // Power Menu
                IconButton(
                    onClick = { showShutdownDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Refresh Progress Indicator
        if (isRefreshing) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

    // Shutdown Dialog
    if (showShutdownDialog) {
        ShutdownDialog(
            onConfirm = { reason ->
                onShutdown(reason)
                showShutdownDialog = false
            },
            onDismiss = { showShutdownDialog = false }
        )
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Connection Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )

                if (canRetry) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    isConnectedStatus :Boolean,
    state: HomeUiState.Success,
    loadAveragesState : LoadAveragesState,
    onRefresh: () -> Unit,
    onRefreshGraph: () -> Unit,
    onShutdown: (String) -> Unit,
) {
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showPerformanceDialog by remember { mutableStateOf(false) }
    var currentMetricType by remember { mutableStateOf(MetricType.ALL) }
    var selectedShare by remember { mutableStateOf<ShareType?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // System Overview Card
        SystemOverviewCard(
            isConnectedStatus = isConnectedStatus,
            systemInfo = state.systemInfo,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Load Averages Section
        // Replace the LoadAveragesGrid call (around line 100) with:
        LoadAveragesGrid(
            loadAveragesState = loadAveragesState,
            modifier = Modifier.padding(bottom = 16.dp),
            onCpuClick = {
                currentMetricType = MetricType.CPU
                showPerformanceDialog = true
            },
            onMemoryClick = {
                currentMetricType = MetricType.MEMORY
                showPerformanceDialog = true
            },
            onLoadClick = {
                currentMetricType = MetricType.ALL
                showPerformanceDialog = true
            }
        )

        SystemStatsSection(
            systemInfo = state.systemInfo,
            poolDetails = state.poolDetails.firstOrNull(),
            diskCount = state.diskDetails.size,
            modifier = Modifier.padding(bottom = 16.dp),
            onDiskClick = { showMemoryDialog = true }
        )

        // Storage Information - show each pool
        if (state.poolDetails.isNotEmpty()) {
            state.poolDetails.forEach { pool ->
                StorageCard(
                    pool = pool,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        } else {
            NoStorageCard(modifier = Modifier.padding(bottom = 16.dp))
        }
        // SMB Shares Section
        SharesCard(
            smbShares = state.smbShares,
            nfsShares = state.nfsShares,
            onSmbShareClick = { share ->
                selectedShare = ShareType.Smb(share)
            },
            onNfsShareClick = { share ->
                selectedShare = ShareType.Nfs(share)
            }
        )
    }

    if (showMemoryDialog) {
        DiskInfoBottomSheet(
            disks = state.diskDetails,
            onDismiss = { showMemoryDialog = false },
        )
    }
    if (showPerformanceDialog) {
        PerformanceBottomSheet(
            cpuData = state.cpuData,
            memoryData = state.memoryData,
            temperatureData = state.temperatureData,
            metricType = currentMetricType,
            isLoading = state.isRefreshing,
            onDismiss = {
                showPerformanceDialog = false
                currentMetricType = MetricType.ALL
            },
            onRefresh = onRefresh
        )
    }
    selectedShare?.let { shareType ->
        ShareInfoBottomSheet(
            shareType = shareType,
            onDismiss = { selectedShare = null }
        )
    }
}

@Composable
private fun SystemOverviewCard(
    isConnectedStatus : Boolean,
    systemInfo: System.SystemInfo,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        WavyGradientBackground {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = systemInfo.hostname,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = systemInfo.version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Uptime: ${systemInfo.uptime.toShortUptime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status indicator
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background( if (isConnectedStatus) Color(0xFF2E7D32) else Color(0xFFF57C00))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnectedStatus) "Online" else "Offline",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Uptime Formatter, might be used later
 */
fun String.toShortUptime(): String {

    val lastColonIndex = this.lastIndexOf(':')
    if (lastColonIndex == -1) {
        return this
    }
    val commaIndex = this.indexOf(',')

    return if (commaIndex != -1) {
        val timePartStart = commaIndex + 2
        val dayPart = this.substring(0, timePartStart)
        val shortTimePart = this.substring(timePartStart, lastColonIndex)
        "$dayPart$shortTimePart"

    } else {
        this.substring(0, lastColonIndex)
    }
}
// REPLACE the entire LoadAveragesGrid function with:
@Composable
private fun LoadAveragesGrid(
    loadAveragesState: LoadAveragesState,
    modifier: Modifier = Modifier,
    onCpuClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onLoadClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Load Averages",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        when (loadAveragesState) {
            is LoadAveragesState.Loading -> {
                // Show loading cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingStatCard(
                        title = "CPU",
                        icon = Icons.Default.Memory,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    LoadingStatCard(
                        title = "Memory",
                        icon = Icons.Default.Storage,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingStatCard(
                        title = "Load",
                        icon = Icons.Default.Timeline,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    LoadingStatCard(
                        title = "Temperature",
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is LoadAveragesState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "CPU",
                        value = loadAveragesState.cpuAverage?.let {
                            DecimalFormat("#.##").format(it) + "%"
                        } ?: "N/A",
                        subtitle = "Average usage",
                        icon = Icons.Default.Memory,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onCpuClick
                    )
                    StatCard(
                        title = "Memory",
                        value = loadAveragesState.memoryAverage?.let {
                            DecimalFormat("#.##").format(it) + "%"
                        } ?: "N/A",
                        subtitle = "Average usage",
                        icon = Icons.Default.Storage,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onMemoryClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Load",
                        value = loadAveragesState.loadAverage?.let {
                            DecimalFormat("#.##").format(it)
                        } ?: "N/A",
                        subtitle = "System load",
                        icon = Icons.Default.Timeline,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = onLoadClick
                    )
                    StatCard(
                        title = "Temperature",
                        value = loadAveragesState.tempAverage?.let {
                            DecimalFormat("#.#").format(it) + "°C"
                        } ?: "N/A",
                        subtitle = "CPU temp",
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = null
                    )
                }
            }
            is LoadAveragesState.Error -> {
                // Show error state
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Failed to load averages",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingStatCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = color,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SystemStatsSection(
    systemInfo: System.SystemInfo,
    poolDetails: System.Pool?,
    diskCount: Int,
    modifier: Modifier = Modifier,
    onDiskClick: () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = "System Stats",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        // Full width cards
        StatCard(
            title = "CPU Cores",
            value = "${systemInfo.cores.toInt()} Total (${systemInfo.physical_cores ?: 0} Physical)",
            icon = Icons.Default.Memory,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            subtitle = "Processing units"
        )

        StatCard(
            title = "Memory",
            value = "${DecimalFormat("#.#").format(systemInfo.physmem / (1024.0 * 1024.0 * 1024.0))} GB ${if (systemInfo.ecc_memory) "(ECC)" else "(Non-ECC)"}",
            subtitle = "Total system memory",
            icon = Icons.Default.Storage,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )

        StatCard(
            title = "Disks",
            value = "$diskCount ${if (diskCount == 1) "Disk" else "Disks"}",
            subtitle = poolDetails?.let { if (it.healthy) "All pools healthy" else "Pool issues detected" } ?: "No pools configured",
            icon = Icons.Default.Storage,
            color = poolDetails?.let { pool ->
                if (pool.healthy) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            } ?: MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
            onClick = onDiskClick
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    ) {
        Row(  // CHANGED FROM Column to Row for full-width support
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StorageCard(
    pool: System.Pool,
    modifier: Modifier = Modifier
) {
    // Helper function to format bytes to human readable string
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }

        return "${DecimalFormat("#.#").format(size)} ${units[unitIndex]}"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Storage Pools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pool.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = if (pool.healthy)
                        Color(0xFF2E7D32).copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pool.healthy) {
                            if (pool.warning) "Warning" else "Healthy"
                        } else "Error",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (pool.healthy) {
                            if (pool.warning) Color(0xFFF57C00) else Color(0xFF2E7D32)
                        } else MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage progress bar
            val usedPercentage = if (pool.size > 0) (pool.allocated.toFloat() / pool.size.toFloat()) else 0f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Used: ${formatBytes(pool.allocated)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Free: ${formatBytes(pool.free)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { usedPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        usedPercentage > 0.8f -> MaterialTheme.colorScheme.error
                        usedPercentage > 0.6f -> Color(0xFFF57C00)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: ${formatBytes(pool.size)} • Fragmentation: ${pool.fragmentation}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status details if there are warnings
                if (pool.warning && pool.status_detail.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pool.status_detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoStorageCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Storage Pools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "No storage pools are currently configured on this system.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SharesCard(
    smbShares: List<Shares.SmbShare>,
    nfsShares: List<Shares.NfsShare>,
    modifier: Modifier = Modifier,
    onSmbShareClick: (Shares.SmbShare) -> Unit = {},
    onNfsShareClick: (Shares.NfsShare) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // SMB Shares Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMB Shares",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${smbShares.size} ${if (smbShares.size == 1) "Share" else "Shares"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (smbShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderShared,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No SMB shares configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                smbShares.take(5).forEach { share ->
                    SmbShareItem(
                        share = share,
                        onShareClick = onSmbShareClick
                    )
                    if (share != smbShares.take(5).last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (smbShares.size > 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "And ${smbShares.size - 5} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Divider between sections
            if (smbShares.isNotEmpty() || nfsShares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // NFS Shares Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NFS Shares",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${nfsShares.size} ${if (nfsShares.size == 1) "Share" else "Shares"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (nfsShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No NFS shares configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                nfsShares.take(5).forEach { share ->
                    NfsShareItem(
                        share = share,
                        onShareClick = onNfsShareClick
                    )
                    if (share != nfsShares.take(5).last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (nfsShares.size > 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "And ${nfsShares.size - 5} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmbShareItem(
    share: Shares.SmbShare,
    onShareClick: (Shares.SmbShare) -> Unit
) {
    Surface(
        onClick = { onShareClick(share) },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (share.timemachine) Icons.Default.Backup
                    else if (share.home) Icons.Default.Home
                    else Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = share.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = share.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = if (share.enabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (share.enabled) "Active" else "Disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (share.enabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun NfsShareItem(
    share: Shares.NfsShare,
    onShareClick: (Shares.NfsShare) -> Unit
) {
    Surface(
        onClick = { onShareClick(share) },
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = share.path.substringAfterLast('/').ifEmpty { share.path },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = share.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = if (share.enabled)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (share.enabled) "Active" else "Disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (share.enabled)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ShutdownDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var shutdownReason by remember { mutableStateOf("User requested shutdown") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Shutdown TrueNAS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to shutdown the TrueNAS system?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = shutdownReason,
                    onValueChange = { shutdownReason = it },
                    label = { Text("Shutdown reason") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(shutdownReason) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Shutdown", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}