package com.example.truehub.ui.homepage.details

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.truehub.data.models.System
import com.example.truehub.ui.background.WavyGradientBackground
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

enum class MetricType {
    CPU,
    MEMORY,
    TEMPERATURE,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceBottomSheet(
    cpuData: List<System.ReportingGraphResponse>?,
    memoryData: List<System.ReportingGraphResponse>?,
    temperatureData: List<System.ReportingGraphResponse>?,
    metricType: MetricType = MetricType.ALL,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val availableMetrics = mutableListOf<MetricType>()
    if (cpuData?.isNotEmpty() == true) availableMetrics.add(MetricType.CPU)
    if (memoryData?.isNotEmpty() == true) availableMetrics.add(MetricType.MEMORY)
    if (temperatureData?.isNotEmpty() == true) availableMetrics.add(MetricType.TEMPERATURE)

    var selectedTab by remember { mutableIntStateOf(
        when (metricType) {
            MetricType.CPU -> availableMetrics.indexOf(MetricType.CPU).takeIf { it >= 0 } ?: 0
            MetricType.MEMORY -> availableMetrics.indexOf(MetricType.MEMORY).takeIf { it >= 0 } ?: 0
            MetricType.TEMPERATURE -> availableMetrics.indexOf(MetricType.TEMPERATURE).takeIf { it >= 0 } ?: 0
            MetricType.ALL -> 0
        }
    )}

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section with current metric info
            val currentMetric = if (metricType == MetricType.ALL && availableMetrics.isNotEmpty()) {
                availableMetrics.getOrNull(selectedTab) ?: MetricType.CPU
            } else metricType

            PerformanceHeader(
                metricType = currentMetric,
                cpuData = cpuData?.firstOrNull(),
                memoryData = memoryData?.firstOrNull(),
                temperatureData = temperatureData?.firstOrNull(),
                onDismiss = onDismiss
            )

            // Tabs for metrics (only show if ALL type and more than one metric available)
            if (metricType == MetricType.ALL && availableMetrics.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    availableMetrics.forEachIndexed { index, metric ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = when (metric) {
                                        MetricType.CPU -> "CPU Usage"
                                        MetricType.MEMORY -> "Memory"
                                        MetricType.TEMPERATURE -> "Temperature"
                                        MetricType.ALL -> "All"
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Show content based on selected metric
            val displayMetric = if (metricType == MetricType.ALL) {
                availableMetrics.getOrNull(selectedTab) ?: MetricType.CPU
            } else metricType

            PerformanceDetailsContent(
                metricType = displayMetric,
                cpuData = cpuData?.firstOrNull(),
                memoryData = memoryData?.firstOrNull(),
                temperatureData = temperatureData?.firstOrNull(),
                isLoading = isLoading,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun PerformanceDetailsContent(
    metricType: MetricType,
    cpuData: System.ReportingGraphResponse?,
    memoryData: System.ReportingGraphResponse?,
    temperatureData: System.ReportingGraphResponse?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onRefresh,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Data")
            }
        }

        when (metricType) {
            MetricType.CPU -> {
                cpuData?.let { cpu ->
                    val processedCpuData = cpu.copy(
                        data = cpu.data.map { point ->
                            val timestamp = point[0]
                            val cpuValue = if (point.size > 1) {
                                val cpuValues = point.drop(1)
                                cpuValues.average()
                            } else 0.0
                            listOf(timestamp, cpuValue)
                        }
                    )

                    // Current Status Section
                    MetricStatusSection(
                        title = "CPU Usage",
                        icon = Icons.Default.Memory,
                        currentValue = formatDataValue(
                            processedCpuData.data.lastOrNull()?.getOrNull(1) ?: 0.0,
                            "%",
                            false
                        ),
                        data = processedCpuData,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Chart Section
                    MetricChartSection(
                        title = "CPU Usage Over Time",
                        data = processedCpuData,
                        color = MaterialTheme.colorScheme.primary,
                        unit = "%"
                    )

                    // Statistics Section
                    MetricStatsSection(
                        title = "CPU Statistics",
                        data = processedCpuData,
                        unit = "%"
                    )
                }
            }

            MetricType.MEMORY -> {
                memoryData?.let { memory ->
                    // Current Status Section
                    MetricStatusSection(
                        title = "Memory Usage",
                        icon = Icons.Default.Storage,
                        currentValue = formatDataValue(
                            memory.data.lastOrNull()?.getOrNull(1) ?: 0.0,
                            "GB",
                            true
                        ),
                        data = memory,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Chart Section
                    MetricChartSection(
                        title = "Memory Usage Over Time",
                        data = memory,
                        color = MaterialTheme.colorScheme.secondary,
                        unit = "GB",
                        isMemory = true
                    )

                    // Statistics Section
                    MetricStatsSection(
                        title = "Memory Statistics",
                        data = memory,
                        unit = "GB",
                        isMemory = true
                    )
                }
            }

            MetricType.TEMPERATURE -> {
                temperatureData?.let { temp ->
                    val processedTempData = temp.copy(
                        data = temp.data.map { point ->
                            val timestamp = point[0]
                            val temperature = if (point.size > 1) point[1] else 0.0
                            listOf(timestamp, temperature)
                        }
                    )

                    // Current Status Section
                    MetricStatusSection(
                        title = "CPU Temperature",
                        icon = Icons.Default.DeviceThermostat,
                        currentValue = formatDataValue(
                            processedTempData.data.lastOrNull()?.getOrNull(1) ?: 0.0,
                            "°C",
                            false
                        ),
                        data = processedTempData,
                        color = MaterialTheme.colorScheme.error
                    )

                    // Chart Section
                    MetricChartSection(
                        title = "Temperature Over Time",
                        data = processedTempData,
                        color = MaterialTheme.colorScheme.error,
                        unit = "°C"
                    )

                    // Statistics Section
                    MetricStatsSection(
                        title = "Temperature Statistics",
                        data = processedTempData,
                        unit = "°C"
                    )
                }
            }

            MetricType.ALL -> {
                // This case shouldn't happen with the current logic
                Text("All metrics view - select a specific metric above")
            }
        }
    }
}

@Composable
private fun PerformanceHeader(
    metricType: MetricType,
    cpuData: System.ReportingGraphResponse?,
    memoryData: System.ReportingGraphResponse?,
    temperatureData: System.ReportingGraphResponse?,
    onDismiss: () -> Unit
) {
    // Create a data class to hold the metric information
    data class MetricInfo(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val currentValue: String
    )

    val metricInfo = when (metricType) {
        MetricType.CPU -> {
            val processedValue = cpuData?.data?.lastOrNull()?.let { point ->
                if (point.size > 1) {
                    val cpuValues = point.drop(1)
                    cpuValues.average()
                } else 0.0
            } ?: 0.0

            MetricInfo(
                title = "CPU Usage",
                subtitle = "Processor usage metrics",
                icon = Icons.Default.Memory,
                currentValue = "${DecimalFormat("#.#").format(processedValue)}%"
            )
        }
        MetricType.MEMORY -> {
            val memValue = memoryData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0

            MetricInfo(
                title = "Memory Usage",
                subtitle = "System memory consumption",
                icon = Icons.Default.Storage,
                currentValue = "${DecimalFormat("#.#").format(memValue / (1024.0 * 1024.0 * 1024.0))} GB"
            )
        }
        MetricType.TEMPERATURE -> {
            val tempValue = temperatureData?.data?.lastOrNull()?.getOrNull(1) ?: 0.0

            MetricInfo(
                title = "CPU Temperature",
                subtitle = "Processor thermal status",
                icon = Icons.Default.DeviceThermostat,
                currentValue = "${DecimalFormat("#.#").format(tempValue)}°C"
            )
        }
        MetricType.ALL -> {
            MetricInfo(
                title = "System Performance",
                subtitle = "All system metrics",
                icon = Icons.Default.Assessment,
                currentValue = "Multiple"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        WavyGradientBackground {
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
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Metric info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric icon
                Icon(
                    imageVector = metricInfo.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )

                // Title
                Text(
                    text = metricInfo.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle and current value
                Text(
                    text = "${metricInfo.subtitle} • Current: ${metricInfo.currentValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MetricStatusSection(
    title: String,
    icon: ImageVector,
    currentValue: String,
    data: System.ReportingGraphResponse,
    color: Color
) {
    MetricInfoSection(
        title = "Current Status",
        icon = Icons.Default.Info
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
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
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current Value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun MetricChartSection(
    title: String,
    data: System.ReportingGraphResponse,
    color: Color,
    unit: String,
    isMemory: Boolean = false
) {
    MetricInfoSection(
        title = title,
        icon = Icons.Default.Timeline
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${data.data.size} data points • ${data.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LineChartView(
                    data = data,
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
}

@Composable
private fun MetricStatsSection(
    title: String,
    data: System.ReportingGraphResponse,
    unit: String,
    isMemory: Boolean = false
) {
    MetricInfoSection(
        title = title,
        icon = Icons.Default.Analytics
    ) {
        val values = data.data.mapNotNull { it.getOrNull(1) }

        if (values.isNotEmpty()) {
            val average = values.average()
            val min = values.minOrNull() ?: 0.0
            val max = values.maxOrNull() ?: 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = "Average",
                    value = formatDataValue(average, unit, isMemory),
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = "Minimum",
                    value = formatDataValue(min, unit, isMemory),
                    color = MaterialTheme.colorScheme.secondary
                )
                StatCard(
                    label = "Maximum",
                    value = formatDataValue(max, unit, isMemory),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MetricInfoSection(
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
private fun StatCard(
    label: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LineChartView(
    data: System.ReportingGraphResponse,
    color: Color,
    unit: String,
    isMemory: Boolean = false,
    modifier: Modifier = Modifier
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
                            } catch (e: Exception) {
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
                    } catch (e: Exception) {
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
                }
                chart.invalidate()
            } catch (e: Exception) {
                // Handle chart update errors gracefully
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