package com.imnotndesh.truehub.ui.homepage

// import com.imnotndesh.truehub.ui.components.HalfCircleGauge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.background.WavyGradientBackground
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.details.MetricType
import com.imnotndesh.truehub.ui.homepage.details.ShareType
import com.imnotndesh.truehub.ui.services.apps.details.appdetails.AppDataHolder
import com.imnotndesh.truehub.ui.utils.AdaptiveLayoutHelper
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    manager: TrueNASApiManager,
    onNavigateToSettings: () -> Unit = {},
    onPoolClick: (System.Pool) -> Unit,
    onDisksClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onInstanceConfigClick: () -> Unit,
    onSystemInfoClick: () -> Unit = {},
    onNavigateToPerformance: (MetricType) -> Unit,
    onNavigateToShareInfo: (ShareType) -> Unit = {},
    onSearchClick: (() -> Unit)? = null
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.HomeViewModelFactory(manager, LocalContext.current.applicationContext)
    )

    val uiState by viewModel.uiState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    var showShutdownDialog by remember { mutableStateOf(false) }
    val isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing ?: false

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Dashboard",
            subtitle = "Welcome back",
            isLoading = uiState is HomeUiState.Loading,
            isRefreshing = false,
            error = null,
            onDismissError = { viewModel.refresh() },
            manager = manager,
            onNavigateToSettings = onNavigateToSettings,
            onShutdownInvoke = { showShutdownDialog = true },
            onSearchClick = onSearchClick
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
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
                    isConnectedStatus = isConnected,
                    // loadAveragesState = loadAveragesState,
                    onPoolClick = onPoolClick,
                    onDiskClick = { onDisksClick() },
                    onNavigateToShareInfo = onNavigateToShareInfo,
                    onNavigateToPerformance = {metricType ->
                        onNavigateToPerformance(metricType)
                    },
                    onUpdateClick = {
                        onUpdateClick()
                    },
                    onInstanceConfigClick = { onInstanceConfigClick() },
                    onSystemInfoClick = { onSystemInfoClick() }
                )
            }
        }
    }
    if (showShutdownDialog) {
        ShutdownDialog(
            onShutdown = { reason ->
                viewModel.shutdownSystem(reason)
                showShutdownDialog = false
            },
            onRestart = { reason ->
                viewModel.rebootSystem(reason)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onDismiss) { Text("Dismiss") }
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
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
    // loadAveragesState: LoadAveragesState,
    onRefresh: () -> Unit,
    onPoolClick: (System.Pool) -> Unit,
    onShutdown: (String) -> Unit,
    onInstanceConfigClick: () -> Unit = {},
    onSystemInfoClick: () -> Unit = {},
    onDiskClick: () -> Unit,
    onNavigateToShareInfo: (ShareType) -> Unit,
    onNavigateToPerformance: (MetricType) -> Unit,
    onUpdateClick: () -> Unit
) {

    val isAdaptiveLayout = AdaptiveLayoutHelper.isExpandedLayout()
    val columnCount = AdaptiveLayoutHelper.getColumnCount()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        SystemOverviewCard(
            isConnectedStatus = isConnectedStatus,
            systemInfo = state.systemInfo,
            systemVersionShort = state.systemVersionShort,
            modifier = Modifier.padding(bottom = 16.dp),
            systemUpdateVersions = state.systemUpdateVersions,
            onUpdateClick = onUpdateClick,
            onPerformanceClick = {
                AppDataHolder.cpuData = state.cpuData
                AppDataHolder.memoryData = state.memoryData
                AppDataHolder.temperatureData = state.temperatureData
                AppDataHolder.initialMetricType = MetricType.ALL
                onNavigateToPerformance(MetricType.ALL)
            },
            onInstanceConfigClick = onInstanceConfigClick,
            onSystemInfoClick = onSystemInfoClick
        )

        if (isAdaptiveLayout) {
            AdaptiveGridLayout(
                columnCount = columnCount,
                state = state,
                // loadAveragesState = loadAveragesState,
                // onCpuClick = { AppDataHolder.cpuData = state.cpuData; onNavigateToPerformance(MetricType.CPU) },
                // onMemoryClick = { AppDataHolder.memoryData = state.memoryData;onNavigateToPerformance(MetricType.MEMORY) },
                // onLoadClick = { onNavigateToPerformance(MetricType.ALL)},
                // onTempClick = {AppDataHolder.temperatureData = state.temperatureData; onNavigateToPerformance(MetricType.TEMPERATURE)},
                onDiskClick = { AppDataHolder.disks = state.diskDetails; onDiskClick() },
                onPoolClick = onPoolClick,
                onSmbShareClick = { share -> onNavigateToShareInfo(ShareType.Smb(share)) },
                onNfsShareClick = { share -> onNavigateToShareInfo(ShareType.Nfs(share)) }
            )
        } else {
            // LoadAveragesGrid commented out
            // LoadAveragesGrid(
            //     loadAveragesState = loadAveragesState,
            //     modifier = Modifier.padding(bottom = 16.dp),
            //     onCpuClick = { AppDataHolder.cpuData = state.cpuData;onNavigateToPerformance(MetricType.CPU) },
            //     onMemoryClick = {AppDataHolder.memoryData = state.memoryData; onNavigateToPerformance(MetricType.MEMORY) },
            //     onTempClick = { AppDataHolder.temperatureData = state.temperatureData;onNavigateToPerformance(MetricType.TEMPERATURE) }
            // )

            if (state.poolDetails.isNotEmpty()) {
                state.poolDetails.forEach { pool ->
                    StorageCard(pool = pool, modifier = Modifier.padding(bottom = 16.dp), onClick = { onPoolClick(pool) })
                }
            } else {
                NoStorageCard(modifier = Modifier.padding(bottom = 16.dp))
            }

            SharesCard(
                smbShares = state.smbShares,
                nfsShares = state.nfsShares,
                onSmbShareClick = { share -> onNavigateToShareInfo(ShareType.Smb(share)) },
                onNfsShareClick = { share -> onNavigateToShareInfo(ShareType.Nfs(share)) }
            )
        }
    }
}

@Composable
private fun AdaptiveGridLayout(
    columnCount: Int,
    state: HomeUiState.Success,
    // loadAveragesState: LoadAveragesState,
    // onCpuClick: () -> Unit,
    // onMemoryClick: () -> Unit,
    // onLoadClick: () -> Unit,
    // onTempClick: () -> Unit,
    onDiskClick: () -> Unit,
    onPoolClick: (System.Pool) -> Unit,
    onSmbShareClick: (Shares.SmbShare) -> Unit,
    onNfsShareClick: (Shares.NfsShare) -> Unit
) {
    val spacing = AdaptiveLayoutHelper.getHorizontalSpacing()

    val sections = buildList {
        // add(SectionItem.LoadAverages(loadAveragesState, onCpuClick, onMemoryClick, onLoadClick, onTempClick))
        if (state.poolDetails.isNotEmpty()) {
            state.poolDetails.forEach { pool -> add(SectionItem.StoragePool(pool, onPoolClick)) }
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
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (section) {
                    // is SectionItem.LoadAverages -> LoadAveragesGrid(
                    //     loadAveragesState = section.state,
                    //     onCpuClick = section.onCpuClick,
                    //     onMemoryClick = section.onMemoryClick,
                    //     onTempClick = section.onTempClick
                    // )
                    is SectionItem.StoragePool -> StorageCard(pool = section.pool, onClick = { section.onPoolClick(section.pool) })
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
    modifier: Modifier = Modifier,
    systemVersionShort: String? = null,
    systemUpdateVersions: List<System.UpdateAvailableVersionsResponse> = emptyList(),
    onUpdateClick: () -> Unit = {},
    onPerformanceClick: () -> Unit = {},
    onInstanceConfigClick: () -> Unit = {},
    onSystemInfoClick: () -> Unit = {}
) {
    val statusColor = if (isConnectedStatus) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFF57C00)
    }

    val glassContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
    val glassBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.3f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    val themeAdaptiveBorderBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
        )
    )

    WavyGradientBackground {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .border(
                    width = 1.dp,
                    brush = themeAdaptiveBorderBrush,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = glassContainerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                // Making the icon box also slightly transparent for a cohesive look
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Computer, null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                systemInfo.hostname,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                systemVersionShort ?: systemInfo.version,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Uptime: ${systemInfo.uptime.toShortUptime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }


                    Surface(
                        color = statusColor,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pills: Update (conditional) → Performance → Instance Settings → System Information
                // Scrollable to support future additions
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    // Update pill — only when updates are available, shown first
                    if (systemUpdateVersions.isNotEmpty()) {
                        item {
                            PillChip(
                                onClick = onUpdateClick,
                                icon = Icons.Filled.SystemUpdateAlt,
                                label = "Update (${systemUpdateVersions.size})",
                                tint = Color(0xFFF57C00)
                            )
                        }
                    }

                    // Performance Overview
                    item {
                        PillChip(
                            onClick = onPerformanceClick,
                            icon = Icons.Filled.Memory,
                            label = "Performance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Instance Settings
                    item {
                        PillChip(
                            onClick = onInstanceConfigClick,
                            icon = Icons.Filled.Tune,
                            label = "Instance Settings"
                        )
                    }

                    // System Information
                    item {
                        PillChip(
                            onClick = onSystemInfoClick,
                            icon = Icons.Filled.Info,
                            label = "System Information"
                        )
                    }
                }
            }
        }
    }
}

fun String.toShortUptime(): String {
    val lastColonIndex = this.lastIndexOf(':')
    if (lastColonIndex == -1) return this
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

/*
@Composable
private fun LoadAveragesGrid(
    loadAveragesState: LoadAveragesState,
    modifier: Modifier = Modifier,
    onCpuClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onTempClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "System Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            when (loadAveragesState) {
                is LoadAveragesState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
                is LoadAveragesState.Success -> {
                    val cpuVal = loadAveragesState.cpuAverage
                    val memVal = loadAveragesState.memoryAverage
                    val tempVal = loadAveragesState.tempAverage

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HalfCircleGauge(
                            progress = cpuVal?.let { (it / 100.0).toFloat() } ?: 0f,
                            valueText = cpuVal?.let { "${DecimalFormat("#.#").format(it)}%" } ?: "N/A",
                            label = "CPU",
                            icon = Icons.Default.Memory,
                            gaugeColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = onCpuClick
                        )

                        HalfCircleGauge(
                            progress = memVal?.let { (it / 100.0).toFloat() } ?: 0f,
                            valueText = memVal?.let { "${DecimalFormat("#.#").format(it)}%" } ?: "N/A",
                            label = "Memory",
                            icon = Icons.Default.Memory,
                            gaugeColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = onMemoryClick
                        )

                        HalfCircleGauge(
                            progress = tempVal?.let { (it / 100.0).toFloat() } ?: 0f,
                            valueText = tempVal?.let { "${DecimalFormat("#.#").format(it)}°C" } ?: "N/A",
                            label = "Temperature",
                            icon = Icons.Default.Thermostat,
                            gaugeColor = if ((tempVal ?: 0.0) > 75.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = onTempClick
                        )
                    }
                }
                is LoadAveragesState.Error -> {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Failed to load metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
*/

@Composable
private fun StorageCard(modifier: Modifier = Modifier, pool: System.Pool, onClick: () -> Unit) {
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) { size /= 1024.0; unitIndex++ }
        return "${DecimalFormat("#.#").format(size)} ${units[unitIndex]}"
    }

    Card(onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Storage Pool", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(pool.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                val isHealthy = pool.healthy
                val hasWarning = pool.warning
                val statusColor = when {
                    !isHealthy -> MaterialTheme.colorScheme.error
                    hasWarning -> Color(0xFFF57C00)
                    else -> Color(0xFF2E7D32)
                }
                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(100.dp)) {
                    val statusText = when {
                        !isHealthy -> "Error"
                        hasWarning -> "Warning"
                        else -> "Healthy"
                    }
                    Text(statusText, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val size = pool.size
            val allocated = pool.allocated
            val free = pool.free
            val fragmentation = pool.fragmentation

            if (size != null && allocated != null && size > 0) {
                val usedPercentage = (allocated.toFloat() / size.toFloat()).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(targetValue = usedPercentage, animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing), label = "storageProgress")
                val progressColor by animateColorAsState(
                    targetValue = when {
                        usedPercentage > 0.8f -> MaterialTheme.colorScheme.error
                        usedPercentage > 0.6f -> Color(0xFFF57C00)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    label = "progressColor"
                )

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Used: ${formatBytes(allocated)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        Text("Free: ${free?.let { formatBytes(it) } ?: "Unavailable"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)), color = progressColor, trackColor = MaterialTheme.colorScheme.surfaceVariant, strokeCap = ProgressIndicatorDefaults.LinearStrokeCap)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Total: ${formatBytes(size)} • Fragmentation: ${fragmentation?.let { "${it}%" } ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    "Storage details missing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if ((pool.warning == true) && !pool.status_detail.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(pool.status_detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFFF57C00), modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
private fun NoStorageCard(modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Storage, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Storage Pools", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("No storage pools are currently configured.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("SMB Shares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(100.dp)) {
                    Text("${smbShares.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            if (smbShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyShareState("No SMB shares")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                smbShares.take(5).forEach { share ->
                    SmbShareItem(share = share, onShareClick = onSmbShareClick)
                    if (share != smbShares.take(5).last()) Spacer(modifier = Modifier.height(8.dp))
                }
                if (smbShares.size > 5) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("+ ${smbShares.size - 5} more...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (smbShares.isNotEmpty() || nfsShares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("NFS Shares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(100.dp)) {
                    Text("${nfsShares.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
            if (nfsShares.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EmptyShareState("No NFS shares")
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                nfsShares.take(5).forEach { share ->
                    NfsShareItem(share = share, onShareClick = onNfsShareClick)
                    if (share != nfsShares.take(5).last()) Spacer(modifier = Modifier.height(8.dp))
                }
                if (nfsShares.size > 5) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("+ ${nfsShares.size - 5} more...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyShareState(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.FolderShared, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun SmbShareItem(share: Shares.SmbShare, onShareClick: (Shares.SmbShare) -> Unit) {
    Surface(onClick = { onShareClick(share) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = if (share.timemachine ?: false) Icons.Default.Backup else if (share.home ?: false) Icons.Default.Home else Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(share.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(share.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (share.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val contentColor = if (share.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
            Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                Text(if (share.enabled) "Active" else "Off", style = MaterialTheme.typography.labelSmall, color = contentColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NfsShareItem(share: Shares.NfsShare, onShareClick: (Shares.NfsShare) -> Unit) {
    Surface(onClick = { onShareClick(share) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(share.path.substringAfterLast('/').ifEmpty { share.path }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(share.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val statusColor = if (share.enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
            val contentColor = if (share.enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
            Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                Text(if (share.enabled) "Active" else "Off", style = MaterialTheme.typography.labelSmall, color = contentColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShutdownDialog(onShutdown: (String) -> Unit, onRestart: (String) -> Unit, onDismiss: () -> Unit) {
    var shutdownReason by remember { mutableStateOf("User requested shutdown") }
    var selectedAction by remember { mutableStateOf(PowerAction.RESTART) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthDp = configuration.screenWidthDp
    val isCompactHeight = screenHeightDp < 600
    val isCompactWidth = screenWidthDp < 360

    val outerHorizontalPadding = if (isCompactWidth) 12.dp else 20.dp
    val contentPadding = if (isCompactHeight) 18.dp else 24.dp
    val maxDialogHeightFraction = if (isCompactHeight) 0.92f else 0.85f

    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val actionAccent = if (selectedAction == PowerAction.RESTART) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val animatedAccent by animateColorAsState(
        targetValue = actionAccent,
        animationSpec = tween(250),
        label = "powerActionAccent"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = outerHorizontalPadding)
                .heightIn(max = (screenHeightDp * maxDialogHeightFraction).dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            color = containerColor,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
            ) {
                // Header: centered icon badge + title/subtitle
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactHeight) 52.dp else 60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        animatedAccent.copy(alpha = 0.22f),
                                        animatedAccent.copy(alpha = 0.10f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PowerSettingsNew, null,
                            modifier = Modifier.size(if (isCompactHeight) 26.dp else 30.dp),
                            tint = animatedAccent
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Power Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Choose how you'd like to power this system",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 24.dp))

                // Segmented action selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PowerActionSegment(
                        modifier = Modifier.weight(1f),
                        label = "Restart",
                        icon = Icons.Default.RestartAlt,
                        selected = selectedAction == PowerAction.RESTART,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = { selectedAction = PowerAction.RESTART }
                    )
                    PowerActionSegment(
                        modifier = Modifier.weight(1f),
                        label = "Shutdown",
                        icon = Icons.Default.PowerSettingsNew,
                        selected = selectedAction == PowerAction.SHUTDOWN,
                        accent = MaterialTheme.colorScheme.error,
                        onClick = { selectedAction = PowerAction.SHUTDOWN }
                    )
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 20.dp))

                // Reason field - only relevant/shown for shutdown
                if (selectedAction == PowerAction.SHUTDOWN) {
                    OutlinedTextField(
                        value = shutdownReason,
                        onValueChange = { shutdownReason = it },
                        label = { Text("Shutdown reason") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 2,
                        singleLine = false,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = animatedAccent,
                            focusedLabelColor = animatedAccent,
                            cursorColor = animatedAccent
                        )
                    )
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 20.dp))
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Services will briefly go offline while the system restarts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 20.dp))
                }

                // Primary confirm button - matches selected action
                Button(
                    onClick = {
                        if (selectedAction == PowerAction.RESTART) onRestart("")
                        else onShutdown(shutdownReason)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 48.dp else 54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedAccent,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (selectedAction == PowerAction.RESTART) Icons.Default.RestartAlt else Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (selectedAction == PowerAction.RESTART) "Restart System" else "Shutdown System",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel - centered text button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 40.dp else 44.dp)
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private enum class PowerAction { RESTART, SHUTDOWN }

@Composable
private fun PowerActionSegment(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(200),
        label = "segmentBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "segmentContent"
    )

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PillChip(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        onClick = onClick,
        color = tint.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private sealed class SectionItem {
    // data class LoadAverages(val state: LoadAveragesState, val onCpuClick: () -> Unit, val onMemoryClick: () -> Unit, val onLoadClick: () -> Unit, val onTempClick: () -> Unit) : SectionItem()
    data class StoragePool(val pool: System.Pool, val onPoolClick: (System.Pool) -> Unit) : SectionItem()
    object NoStorage : SectionItem()
    data class Shares(val smbShares: List<Shares.SmbShare>, val nfsShares: List<Shares.NfsShare>, val onSmbShareClick: (Shares.SmbShare) -> Unit, val onNfsShareClick: (Shares.NfsShare) -> Unit) : SectionItem()
}