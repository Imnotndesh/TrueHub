package com.imnotndesh.truehub.ui.homepage

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.details.DiskInfoBottomSheet
import com.imnotndesh.truehub.ui.homepage.details.MetricType
import com.imnotndesh.truehub.ui.homepage.details.PerformanceBottomSheet
import com.imnotndesh.truehub.ui.homepage.details.ShareInfoBottomSheet
import com.imnotndesh.truehub.ui.homepage.details.ShareType
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    manager: TrueNASApiManager,
    onNavigateToSettings: () -> Unit = {},
    onPoolClick: (System.Pool) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.HomeViewModelFactory(manager, LocalContext.current.applicationContext)
    )

    val uiState by viewModel.uiState.collectAsState()
    val loadAveragesState by viewModel.loadAverages.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    var showShutdownDialog by remember { mutableStateOf(false) }
    val isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing ?: false

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Dashboard",
            subtitle = "Welcome back",
            isLoading = uiState is HomeUiState.Loading,
            isRefreshing = isRefreshing,
            error = null,
            onRefresh = { viewModel.refresh() },
            onDismissError = { viewModel.refresh() },
            manager = manager,
            onNavigateToSettings = onNavigateToSettings,
            onShutdownInvoke = { showShutdownDialog = true }
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        )
        {
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
                    loadAveragesState = loadAveragesState,
                    onPoolClick = onPoolClick
                )
            }
        }
    }
    if (showShutdownDialog) {
        ShutdownDialog(
            onConfirm = { reason ->
                viewModel.shutdownSystem(reason)
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
            shape = RoundedCornerShape(28.dp)
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
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
    isConnectedStatus: Boolean,
    state: HomeUiState.Success,
    loadAveragesState: LoadAveragesState,
    onRefresh: () -> Unit,
    onPoolClick: (System.Pool) -> Unit,
    onRefreshGraph: () -> Unit,
    onShutdown: (String) -> Unit,
) {
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showPerformanceDialog by remember { mutableStateOf(false) }
    var currentMetricType by remember { mutableStateOf(MetricType.ALL) }
    var selectedShare by remember { mutableStateOf<ShareType?>(null) }

    val isAdaptiveLayout = AdaptiveLayoutHelper.isExpandedLayout()
    val columnCount = AdaptiveLayoutHelper.getColumnCount()
    val contentPadding = AdaptiveLayoutHelper.getContentPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding.dp)
    ) {
        // System Overview Card - Always full width
        SystemOverviewCard(
            isConnectedStatus = isConnectedStatus,
            systemInfo = state.systemInfo,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isAdaptiveLayout) {
            // Adaptive multi-column layout for landscape/tablet
            AdaptiveGridLayout(
                columnCount = columnCount,
                state = state,
                loadAveragesState = loadAveragesState,
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
                },
                onTempClick = {
                    currentMetricType = MetricType.TEMPERATURE
                    showPerformanceDialog = true
                },
                onDiskClick = { showMemoryDialog = true },
                onPoolClick = onPoolClick,
                onSmbShareClick = { share -> selectedShare = ShareType.Smb(share) },
                onNfsShareClick = { share -> selectedShare = ShareType.Nfs(share) }
            )
        } else {
            // Portrait Layout
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
                },
                onTempClick = {
                    currentMetricType = MetricType.TEMPERATURE
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

            if (state.poolDetails.isNotEmpty()) {
                state.poolDetails.forEach { pool ->
                    StorageCard(
                        pool = pool,
                        modifier = Modifier.padding(bottom = 16.dp),
                        onClick = { onPoolClick(pool) }
                    )
                }
            } else {
                NoStorageCard(modifier = Modifier.padding(bottom = 16.dp))
            }

            SharesCard(
                smbShares = state.smbShares,
                nfsShares = state.nfsShares,
                onSmbShareClick = { share -> selectedShare = ShareType.Smb(share) },
                onNfsShareClick = { share -> selectedShare = ShareType.Nfs(share) }
            )
        }
    }

    // Dialogs
    if (showMemoryDialog) {
        if (state.diskDetails.isNotEmpty()) {
            DiskInfoBottomSheet(
                disks = state.diskDetails,
                onDismiss = { showMemoryDialog = false },
            )
        } else {
            ToastManager.showInfo("No disk Information Found")
        }
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
private fun AdaptiveGridLayout(
    columnCount: Int,
    state: HomeUiState.Success,
    loadAveragesState: LoadAveragesState,
    onCpuClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onLoadClick: () -> Unit,
    onTempClick: () -> Unit,
    onDiskClick: () -> Unit,
    onPoolClick: (System.Pool) -> Unit,
    onSmbShareClick: (Shares.SmbShare) -> Unit,
    onNfsShareClick: (Shares.NfsShare) -> Unit
) {
    val spacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    val sections = buildList {
        add(SectionItem.LoadAverages(loadAveragesState, onCpuClick, onMemoryClick, onLoadClick, onTempClick))
        add(SectionItem.SystemStats(state.systemInfo, state.poolDetails.firstOrNull(), state.diskDetails.size, onDiskClick))
        if (state.poolDetails.isNotEmpty()) {
            state.poolDetails.forEach { pool ->
                add(SectionItem.StoragePool(pool, onPoolClick))
            }
        } else {
            add(SectionItem.NoStorage)
        }
        add(SectionItem.Shares(state.smbShares, state.nfsShares, onSmbShareClick, onNfsShareClick))
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = columnCount
    ) {
        sections.forEach { section ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (section) {
                    is SectionItem.LoadAverages -> LoadAveragesGrid(
                        loadAveragesState = section.state,
                        onCpuClick = section.onCpuClick,
                        onMemoryClick = section.onMemoryClick,
                        onLoadClick = section.onLoadClick,
                        onTempClick = section.onTempClick
                    )
                    is SectionItem.SystemStats -> SystemStatsSection(
                        systemInfo = section.systemInfo,
                        poolDetails = section.poolDetails,
                        diskCount = section.diskCount,
                        onDiskClick = section.onDiskClick
                    )
                    is SectionItem.StoragePool -> StorageCard(
                        pool = section.pool,
                        onClick = { section.onPoolClick(section.pool) }
                    )
                    is SectionItem.NoStorage -> NoStorageCard()
                    is SectionItem.Shares -> SharesCard(
                        smbShares = section.smbShares,
                        nfsShares = section.nfsShares,
                        onSmbShareClick = section.onSmbShareClick,
                        onNfsShareClick = section.onNfsShareClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemOverviewCard(
    isConnectedStatus: Boolean,
    systemInfo: System.SystemInfo,
    modifier: Modifier = Modifier
) {
    WavyGradientBackground {
        Card(
            shape = RoundedCornerShape(24.dp), // Expressive
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // More breathing room
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Server Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = systemInfo.hostname,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status indicator with Animation
                val statusColor by animateColorAsState(
                    if (isConnectedStatus) Color(0xFF2E7D32) else Color(0xFFF57C00), label = "statusColor"
                )
                val statusText = if (isConnectedStatus) "Online" else "Offline"

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(100.dp) // Pill
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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

@Composable
private fun LoadAveragesGrid(
    loadAveragesState: LoadAveragesState,
    modifier: Modifier = Modifier,
    onCpuClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onLoadClick: () -> Unit,
    onTempClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Load Averages",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
        )

        when (loadAveragesState) {
            is LoadAveragesState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingStatCard("CPU", Icons.Default.Memory, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    LoadingStatCard("Memory", Icons.Default.Storage, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoadingStatCard("Load", Icons.Default.Timeline, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    LoadingStatCard("Temperature", Icons.Default.Thermostat, Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }
            is LoadAveragesState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "CPU",
                        value = loadAveragesState.cpuAverage?.let { DecimalFormat("#.##").format(it) + "%" } ?: "N/A",
                        subtitle = "Average usage",
                        icon = Icons.Default.Memory,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onCpuClick
                    )
                    StatCard(
                        title = "Memory",
                        value = loadAveragesState.memoryAverage?.let { DecimalFormat("#.##").format(it) + "%" } ?: "N/A",
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
                        value = loadAveragesState.loadAverage?.let { DecimalFormat("#.##").format(it) } ?: "N/A",
                        subtitle = "System load",
                        icon = Icons.Default.Timeline,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = onLoadClick
                    )
                    StatCard(
                        title = "Temperature",
                        value = loadAveragesState.tempAverage?.let { DecimalFormat("#.#").format(it) + "°C" } ?: "N/A",
                        subtitle = "CPU temp",
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = onTempClick
                    )
                }
            }
            is LoadAveragesState.Error -> {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.onErrorContainer)
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = color,
                strokeWidth = 2.dp
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
        )

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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }
            }

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StorageCard(
    modifier: Modifier = Modifier,
    pool: System.Pool,
    onClick: () -> Unit
) {
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
        onClick = onClick,
        shape = RoundedCornerShape(24.dp), // Expressive
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Storage Pool",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = pool.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val statusColor = if (pool.healthy) {
                    if (pool.warning) Color(0xFFF57C00) else Color(0xFF2E7D32)
                } else MaterialTheme.colorScheme.error

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = if (pool.healthy) {
                            if (pool.warning) "Warning" else "Healthy"
                        } else "Error",
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Storage Progress
            val usedPercentage = if (pool.size > 0) (pool.allocated.toFloat() / pool.size.toFloat()) else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = usedPercentage,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "storageProgress"
            )

            val progressColor by animateColorAsState(
                targetValue = when {
                    usedPercentage > 0.8f -> MaterialTheme.colorScheme.error
                    usedPercentage > 0.6f -> Color(0xFFF57C00)
                    else -> MaterialTheme.colorScheme.primary
                },
                label = "progressColor"
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Used: ${formatBytes(pool.allocated)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Free: ${formatBytes(pool.free)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total: ${formatBytes(pool.size)} • Fragmentation: ${pool.fragmentation}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (pool.warning && !pool.status_detail.isNullOrEmpty()) {
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
private fun NoStorageCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Storage Pools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "No storage pools are currently configured.",
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            // SMB Header
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
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "${smbShares.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (smbShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyShareState("No SMB shares")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                smbShares.take(5).forEach { share ->
                    SmbShareItem(share = share, onShareClick = onSmbShareClick)
                    if (share != smbShares.take(5).last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (smbShares.size > 5) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "+ ${smbShares.size - 5} more...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Divider
            if (smbShares.isNotEmpty() || nfsShares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))
            }

            // NFS Header
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
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "${nfsShares.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (nfsShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyShareState("No NFS shares")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                nfsShares.take(5).forEach { share ->
                    NfsShareItem(share = share, onShareClick = onNfsShareClick)
                    if (share != nfsShares.take(5).last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (nfsShares.size > 5) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "+ ${nfsShares.size - 5} more...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyShareState(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderShared,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SmbShareItem(
    share: Shares.SmbShare,
    onShareClick: (Shares.SmbShare) -> Unit
) {
    Surface(
        onClick = { onShareClick(share) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (share.timemachine ?: false) Icons.Default.Backup
                            else if (share.home ?: false) Icons.Default.Home
                            else Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = share.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
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
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (share.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val contentColor = if (share.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

            Surface(
                color = statusColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (share.enabled) "Active" else "Off",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = share.path.substringAfterLast('/').ifEmpty { share.path },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
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
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (share.enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
            val contentColor = if (share.enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer

            Surface(
                color = statusColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (share.enabled) "Active" else "Off",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
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
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = shutdownReason,
                    onValueChange = { shutdownReason = it },
                    label = { Text("Shutdown reason") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(shutdownReason) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Shutdown", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}

private sealed class SectionItem {
    data class LoadAverages(
        val state: LoadAveragesState,
        val onCpuClick: () -> Unit,
        val onMemoryClick: () -> Unit,
        val onLoadClick: () -> Unit,
        val onTempClick: () -> Unit
    ) : SectionItem()

    data class SystemStats(
        val systemInfo: System.SystemInfo,
        val poolDetails: System.Pool?,
        val diskCount: Int,
        val onDiskClick: () -> Unit
    ) : SectionItem()

    data class StoragePool(
        val pool: System.Pool,
        val onPoolClick: (System.Pool) -> Unit
    ) : SectionItem()

    object NoStorage : SectionItem()

    data class Shares(
        val smbShares: List<Shares.SmbShare>,
        val nfsShares: List<Shares.NfsShare>,
        val onSmbShareClick: (Shares.SmbShare) -> Unit,
        val onNfsShareClick: (Shares.NfsShare) -> Unit
    ) : SectionItem()
}