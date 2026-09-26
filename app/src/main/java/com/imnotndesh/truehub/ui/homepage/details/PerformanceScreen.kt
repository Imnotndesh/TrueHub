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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.FrameMetricsAggregator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MetricType {
    CPU,
    MEMORY,
    TEMPERATURE,
    ALL
}

private enum class TimeRange(val title: String, val durationSeconds: Long) {
    FIVE_MINS("5m", 5 * 60),
    TEN_MINS("10m", 10 * 60),
    THIRTY_MINS("30m", 30 * 60),
    ALL("All", Long.MAX_VALUE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    cpuData: List<System.ReportingGraphResponse>?,
    memoryData: List<System.ReportingGraphResponse>?,
    temperatureData: List<System.ReportingGraphResponse>?,
    initialMetricType: MetricType = MetricType.ALL,
    isLoading: Boolean,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            onRefresh()
        }
    }

    val availableMetrics = mutableListOf<MetricType>()
    if (cpuData?.isNotEmpty() == true) availableMetrics.add(MetricType.CPU)
    if (memoryData?.isNotEmpty() == true) availableMetrics.add(MetricType.MEMORY)
    if (temperatureData?.isNotEmpty() == true) availableMetrics.add(MetricType.TEMPERATURE)

    var selectedTab by remember {
        mutableIntStateOf(
            when (initialMetricType) {
                MetricType.CPU -> availableMetrics.indexOf(MetricType.CPU).takeIf { it >= 0 } ?: 0
                MetricType.MEMORY -> availableMetrics.indexOf(MetricType.MEMORY).takeIf { it >= 0 } ?: 0
                MetricType.TEMPERATURE -> availableMetrics.indexOf(MetricType.TEMPERATURE).takeIf { it >= 0 } ?: 0
                MetricType.ALL -> 0
            }
        )
    }

    val displayMetric = availableMetrics.getOrNull(selectedTab) ?: MetricType.CPU

    val currentCpu = cpuData?.firstOrNull()
    val currentMemory = memoryData?.firstOrNull()
    val currentTemp = temperatureData?.firstOrNull()

    val headerSubtitle = when (displayMetric) {
        MetricType.CPU -> {
            val value = currentCpu?.data?.lastOrNull()?.let { point ->
                if (point.size > 1) point.drop(1).average() else 0.0
            } ?: 0.0
            "${DecimalFormat("#.#").format(value)}% current"
        }
        MetricType.MEMORY -> {
            val value = currentMemory?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            "${DecimalFormat("#.#").format(value / (1024.0 * 1024.0 * 1024.0))} GB current"
        }
        MetricType.TEMPERATURE -> {
            val value = currentTemp?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            "${DecimalFormat("#.#").format(value)}°C current"
        }
        MetricType.ALL -> "System metrics"
    }
    val headerTitle = when (initialMetricType) {
        MetricType.CPU -> {
            "CPU Metrics"
        }

        MetricType.MEMORY ->{
            "Memory Metrics"
        }
        MetricType.TEMPERATURE -> {
            "Temperature Metrics"
        }

        else -> {
            "Metrics"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = headerTitle,
            subtitle = headerSubtitle,
            isLoading = isLoading,
            isRefreshing = isLoading,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        if (availableMetrics.size > 1) {
            MetricSelectorRow(
                metrics = availableMetrics,
                selectedIndex = selectedTab,
                onMetricSelected = { selectedTab = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MetricHeroCard(
                    metricType = displayMetric,
                    cpuData = currentCpu,
                    memoryData = currentMemory,
                    temperatureData = currentTemp
                )
            }

            item {
                MetricHealthBanner(
                    metricType = displayMetric,
                    cpuData = currentCpu,
                    memoryData = currentMemory,
                    temperatureData = currentTemp
                )
            }

            item {
                when (displayMetric) {
                    MetricType.CPU -> {
                        currentCpu?.let { cpu ->
                            val processedCpuData = cpu.copy(
                                data = cpu.data.map { point ->
                                    val timestamp = point[0]
                                    val cpuValue = if (point.size > 1) point.drop(1).average() else 0.0
                                    listOf(timestamp, cpuValue)
                                }
                            )
                            PerformanceInfoSection(title = "Current Status", icon = Icons.Default.Info) {
                                MetricCurrentValueCard(
                                    title = "CPU Usage",
                                    icon = Icons.Default.Memory,
                                    currentValue = formatDataValue(
                                        processedCpuData.data.lastOrNull()?.getOrNull(1) ?: 0.0, "%", false
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    MetricType.MEMORY -> {
                        currentMemory?.let { memory ->
                            PerformanceInfoSection(title = "Current Status", icon = Icons.Default.Info) {
                                MetricCurrentValueCard(
                                    title = "Memory Usage",
                                    icon = Icons.Default.Storage,
                                    currentValue = formatDataValue(
                                        memory.data.lastOrNull()?.getOrNull(1) ?: 0.0, "GB", true
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    MetricType.TEMPERATURE -> {
                        currentTemp?.let { temp ->
                            val processedTempData = temp.copy(
                                data = temp.data.map { point ->
                                    val timestamp = point[0]
                                    val temperature = if (point.size > 1) point[1] else 0.0
                                    listOf(timestamp, temperature)
                                }
                            )
                            PerformanceInfoSection(title = "Current Status", icon = Icons.Default.Info) {
                                MetricCurrentValueCard(
                                    title = "CPU Temperature",
                                    icon = Icons.Default.DeviceThermostat,
                                    currentValue = formatDataValue(
                                        processedTempData.data.lastOrNull()?.getOrNull(1) ?: 0.0, "°C", false
                                    ),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    MetricType.ALL -> {}
                }
            }

            item {
                when (displayMetric) {
                    MetricType.CPU -> {
                        currentCpu?.let { cpu ->
                            val processedCpuData = cpu.copy(
                                data = cpu.data.map { point ->
                                    val timestamp = point[0]
                                    val cpuValue = if (point.size > 1) point.drop(1).average() else 0.0
                                    listOf(timestamp, cpuValue)
                                }
                            )
                            PerformanceInfoSection(title = "CPU Usage Over Time", icon = Icons.Default.Timeline) {
                                MetricChartCard(
                                    data = processedCpuData,
                                    color = MaterialTheme.colorScheme.primary,
                                    unit = "%"
                                )
                            }
                        }
                    }
                    MetricType.MEMORY -> {
                        currentMemory?.let { memory ->
                            PerformanceInfoSection(title = "Memory Usage Over Time", icon = Icons.Default.Timeline) {
                                MetricChartCard(
                                    data = memory,
                                    color = MaterialTheme.colorScheme.secondary,
                                    unit = "GB",
                                    isMemory = true
                                )
                            }
                        }
                    }
                    MetricType.TEMPERATURE -> {
                        currentTemp?.let { temp ->
                            val processedTempData = temp.copy(
                                data = temp.data.map { point ->
                                    val timestamp = point[0]
                                    val temperature = if (point.size > 1) point[1] else 0.0
                                    listOf(timestamp, temperature)
                                }
                            )
                            PerformanceInfoSection(title = "Temperature Over Time", icon = Icons.Default.Timeline) {
                                MetricChartCard(
                                    data = processedTempData,
                                    color = MaterialTheme.colorScheme.error,
                                    unit = "°C"
                                )
                            }
                        }
                    }
                    MetricType.ALL -> {}
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricSelectorRow(
    metrics: List<MetricType>,
    selectedIndex: Int,
    onMetricSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(metrics.size) { index ->
            val metric = metrics[index]
            val isSelected = selectedIndex == index
            val animColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                label = "chipColor"
            )
            FilterChip(
                selected = isSelected,
                onClick = { onMetricSelected(index) },
                label = {
                    Text(
                        text = when (metric) {
                            MetricType.CPU -> "CPU"
                            MetricType.MEMORY -> "Memory"
                            MetricType.TEMPERATURE -> "Temperature"
                            MetricType.ALL -> "All"
                        },
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
private fun MetricHeroCard(
    metricType: MetricType,
    cpuData: System.ReportingGraphResponse?,
    memoryData: System.ReportingGraphResponse?,
    temperatureData: System.ReportingGraphResponse?
) {
    data class HeroInfo(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val badge1: String,
        val badge2: String
    )

    val info = when (metricType) {
        MetricType.CPU -> {
            val dataPoints = cpuData?.data?.size ?: 0
            HeroInfo(
                title = "CPU Usage",
                subtitle = cpuData?.name ?: "Processor",
                icon = Icons.Default.Memory,
                badge1 = "Processor",
                badge2 = "$dataPoints pts"
            )
        }
        MetricType.MEMORY -> {
            val dataPoints = memoryData?.data?.size ?: 0
            HeroInfo(
                title = "Memory Usage",
                subtitle = memoryData?.name ?: "System RAM",
                icon = Icons.Default.Storage,
                badge1 = "RAM",
                badge2 = "$dataPoints pts"
            )
        }
        MetricType.TEMPERATURE -> {
            val dataPoints = temperatureData?.data?.size ?: 0
            HeroInfo(
                title = "CPU Temperature",
                subtitle = temperatureData?.name ?: "Thermal",
                icon = Icons.Default.DeviceThermostat,
                badge1 = "Thermal",
                badge2 = "$dataPoints pts"
            )
        }
        MetricType.ALL -> HeroInfo(
            title = "System Performance",
            subtitle = "All metrics",
            icon = Icons.Default.Assessment,
            badge1 = "Overview",
            badge2 = "Live"
        )
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = info.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricBadge(
                        label = info.badge1,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    MetricBadge(
                        label = info.badge2,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    MetricBadge(
                        label = "Live",
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, shape = RoundedCornerShape(100.dp)) {
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
private fun MetricHealthBanner(
    metricType: MetricType,
    cpuData: System.ReportingGraphResponse?,
    memoryData: System.ReportingGraphResponse?,
    temperatureData: System.ReportingGraphResponse?
) {
    val thresholdOk = when (metricType) {
        MetricType.CPU -> {
            val value = cpuData?.data?.lastOrNull()?.let { if (it.size > 1) it.drop(1).average() else 0.0 } ?: 0.0
            value < 85.0
        }
        MetricType.MEMORY -> {
            val value = memoryData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            val gb = value / (1024.0 * 1024.0 * 1024.0)
            gb < 28.0
        }
        MetricType.TEMPERATURE -> {
            val value = temperatureData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            value < 80.0
        }
        MetricType.ALL -> true
    }

    val progressValue = when (metricType) {
        MetricType.CPU -> {
            val value = cpuData?.data?.lastOrNull()?.let { if (it.size > 1) it.drop(1).average() else 0.0 } ?: 0.0
            (value / 100.0).toFloat().coerceIn(0f, 1f)
        }
        MetricType.MEMORY -> {
            val value = memoryData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            (value / (32.0 * 1024.0 * 1024.0 * 1024.0)).toFloat().coerceIn(0f, 1f)
        }
        MetricType.TEMPERATURE -> {
            val value = temperatureData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0
            (value / 100.0).toFloat().coerceIn(0f, 1f)
        }
        MetricType.ALL -> 0.5f
    }

    val bannerColor by animateColorAsState(
        targetValue = if (thresholdOk) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "bannerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (thresholdOk) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "bannerContent"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "metricProgress"
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
                        imageVector = if (thresholdOk) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Metric Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                Text(
                    text = if (thresholdOk) "Normal" else "High Load",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
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
private fun MetricCurrentValueCard(
    title: String,
    icon: ImageVector,
    currentValue: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
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
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Current Value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Text(
                text = currentValue,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun MetricChartCard(
    data: System.ReportingGraphResponse,
    color: Color,
    unit: String,
    isMemory: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            var selectedRangeIndex by remember { mutableIntStateOf(0) }
            val timeRanges = TimeRange.entries.toTypedArray()

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                timeRanges.forEachIndexed { index, timeRange ->
                    SegmentedButton(
                        selected = index == selectedRangeIndex,
                        onClick = { selectedRangeIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = timeRanges.size,
                            baseShape = SegmentedButtonDefaults.baseShape
                        )
                    ) {
                        Text(timeRange.title)
                    }
                }
            }

            val filteredData = remember(data, selectedRangeIndex) {
                val selectedRange = timeRanges[selectedRangeIndex]
                if (selectedRange == TimeRange.ALL) {
                    data
                } else {
                    val cutoffTimestamp = (java.lang.System.currentTimeMillis() / 1000) - selectedRange.durationSeconds
                    val newPoints = data.data.filter {
                        (it.getOrNull(0)?.toLong() ?: 0L) >= cutoffTimestamp
                    }
                    data.copy(data = newPoints)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filteredData.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${filteredData.data.size} data points",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            LineChartView(
                data = filteredData,
                color = color,
                unit = unit,
                isMemory = isMemory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
private fun PerformanceInfoSection(
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(content = content)
    }
}

@Composable
private fun LineChartView(
    modifier: Modifier = Modifier,
    data: System.ReportingGraphResponse,
    color: Color,
    unit: String,
    isMemory: Boolean = false
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = Color.Gray.copy(alpha = 0.3f).toArgb()
                    textColor = Color.Gray.toArgb()
                    labelRotationAngle = -45f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return try {
                                val timestamp = value.toLong()
                                SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(Date(timestamp * 1000))
                            } catch (_: Exception) {
                                value.toInt().toString()
                            }
                        }
                    }
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.Gray.copy(alpha = 0.3f).toArgb()
                    textColor = Color.Gray.toArgb()
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatDataValue(value.toDouble(), unit, isMemory)
                        }
                    }
                }

                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            try {
                val entries = data.data.mapNotNull { point ->
                    try {
                        val timestamp = point.getOrNull(0)?.toFloat() ?: return@mapNotNull null
                        val value = point.getOrNull(1)?.toFloat() ?: return@mapNotNull null
                        Entry(timestamp, value)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, data.name).apply {
                        this.color = color.toArgb()
                        setCircleColor(color.toArgb())
                        lineWidth = 3f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        setDrawFilled(true)
                        fillColor = color.copy(alpha = 0.3f).toArgb()
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    chart.data = LineData(dataSet)
                    chart.animateX(1000)
                    chart.moveViewToX(entries.last().x)
                } else {
                    chart.clear()
                }
                chart.invalidate()
            } catch (_: Exception) {
                ToastManager.showError("Api error in fetching graph data")
            }
        },
        modifier = modifier
    )
}

private fun formatDataValue(value: Double, unit: String, isMemory: Boolean): String {
    return if (isMemory) {
        "${DecimalFormat("#.##").format(value / (1024.0 * 1024.0 * 1024.0))} $unit"
    } else {
        "${DecimalFormat("#.##").format(value)} $unit"
    }
}