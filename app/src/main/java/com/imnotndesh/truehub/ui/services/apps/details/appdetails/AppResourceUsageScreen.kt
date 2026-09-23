package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import android.graphics.drawable.GradientDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.AppStats
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.haptics.VibrationFeedback
import com.imnotndesh.truehub.ui.haptics.VibratorMode
import java.util.Locale

private val INTERVAL_OPTIONS = listOf(2, 5, 10)

@Composable
fun AppResourceUsageScreen(
    manager: TrueNASApiManager,
    appId: String,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AppResourceUsageViewModel = hiltViewModel(key = appId)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = remember(context) { VibrationFeedback(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val collapseThreshold = remember(density) { with(density) { 32.dp.roundToPx() } }
    val collapsed by remember { derivedStateOf { scrollState.value > collapseThreshold } }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.setActive(true)
                Lifecycle.Event.ON_STOP -> viewModel.setActive(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setActive(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 96.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading && state.current == null ->
                    LoadingUsage(Modifier.fillMaxWidth().padding(top = 120.dp))
                state.current == null ->
                    EmptyUsage(
                        appId = appId,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth().padding(top = 120.dp)
                    )
                else -> UsageContent(
                    state = state,
                    onIntervalChange = {
                        haptics.play(VibratorMode.CONFIRM_TAP)
                        viewModel.setInterval(it)
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = !collapsed,
            enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { -it / 2 },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
        ) {
            UnifiedScreenHeader(
                title = "App Resource Usage",
                subtitle = appId,
                isLoading = false,
                isRefreshing = false,
                error = state.error,
                onRefresh = { viewModel.retry() },
                onDismissError = { viewModel.dismissError() },
                onBackPressed = onNavigateBack
            )
        }

        AnimatedVisibility(
            visible = collapsed,
            enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { -it },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            MinimalBackHeader(
                onBackPressed = onNavigateBack,
                error = state.error,
                onDismissError = { viewModel.dismissError() }
            )
        }
    }
}

@Composable
private fun UsageContent(
    state: AppResourceUsageUiState,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val current = state.current ?: return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val wide = maxWidth >= 600.dp
        val cpuValues = state.history.map { it.cpuUsage.toFloat() }
        val memoryValues = state.history.map { it.memory / (1024f * 1024f) }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OverviewCard(current)

            IntervalSelector(
                selected = state.intervalSeconds,
                onSelect = onIntervalChange
            )

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    UsageChartCard(
                        title = "CPU",
                        currentLabel = String.format(Locale.US, "%.1f%%", current.cpuUsage),
                        values = cpuValues,
                        axisFormatter = { value -> "${value.toInt()}%" },
                        modifier = Modifier.weight(1f)
                    )
                    UsageChartCard(
                        title = "Memory",
                        currentLabel = formatBytes(current.memory),
                        values = memoryValues,
                        axisFormatter = { value -> formatAxisBytes(value * 1024f * 1024f) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                UsageChartCard(
                    title = "CPU",
                    currentLabel = String.format(Locale.US, "%.1f%%", current.cpuUsage),
                    values = cpuValues,
                    axisFormatter = { value -> "${value.toInt()}%" },
                    modifier = Modifier.fillMaxWidth()
                )
                UsageChartCard(
                    title = "Memory",
                    currentLabel = formatBytes(current.memory),
                    values = memoryValues,
                    axisFormatter = { value -> formatAxisBytes(value * 1024f * 1024f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(stats: AppStats) {
    val colors = MaterialTheme.colorScheme
    val rx = stats.networks.sumOf { it.rxBytes }
    val tx = stats.networks.sumOf { it.txBytes }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(
                    icon = Icons.Default.Speed,
                    label = "CPU",
                    value = String.format(Locale.US, "%.1f%%", stats.cpuUsage),
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    icon = Icons.Default.Memory,
                    label = "Memory",
                    value = formatBytes(stats.memory),
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = colors.outlineVariant.copy(alpha = 0.5f)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(
                    icon = Icons.Default.NetworkCheck,
                    label = "Network /s",
                    value = "↓ ${formatBytes(rx)}  ↑ ${formatBytes(tx)}",
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    icon = Icons.Default.Storage,
                    label = "Disk I/O",
                    value = "R ${formatBytes(stats.blkio.read)}  W ${formatBytes(stats.blkio.write)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalSelector(selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Update interval",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            INTERVAL_OPTIONS.forEachIndexed { index, seconds ->
                SegmentedButton(
                    selected = selected == seconds,
                    onClick = { onSelect(seconds) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = INTERVAL_OPTIONS.size
                    )
                ) {
                    Text("${seconds}s")
                }
            }
        }
    }
}

@Composable
private fun UsageChartCard(
    title: String,
    currentLabel: String,
    values: List<Float>,
    axisFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (values.size >= 2) {
                StatsLineChart(
                    values = values,
                    color = colors.primary,
                    axisFormatter = axisFormatter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = "Collecting samples…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 60.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsLineChart(
    values: List<Float>,
    color: Color,
    axisFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).toArgb()
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f).toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setTouchEnabled(true)
                isDragEnabled = false
                setScaleEnabled(false)
                setExtraOffsets(6f, 6f, 6f, 6f)
                setNoDataText("")
                xAxis.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.apply {
                    setDrawAxisLine(false)
                    setDrawGridLines(true)
                    this.gridColor = gridColor
                    gridLineWidth = 0.5f
                    textColor = axisColor
                    axisMinimum = 0f
                    setLabelCount(4, false)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String = axisFormatter(value)
                    }
                }
            }
        },
        update = { chart ->
            val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            if (entries.size >= 2) {
                val dataSet = LineDataSet(entries, "").apply {
                    this.color = color.toArgb()
                    lineWidth = 3f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    setFillDrawable(
                        GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(color.copy(alpha = 0.30f).toArgb(), Color.Transparent.toArgb())
                        )
                    )
                    setDrawHorizontalHighlightIndicator(false)
                    setHighLightColor(color.toArgb())
                }
                chart.data = LineData(dataSet)
                chart.notifyDataSetChanged()
                chart.invalidate()
            } else {
                chart.clear()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun LoadingUsage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = "Loading live usage…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyUsage(
    appId: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MonitorHeart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No live stats for $appId right now.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0) {
        "${bytes} ${units[unit]}"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unit])
    }
}

private fun formatAxisBytes(bytes: Float): String {
    if (bytes <= 0f) return "0"
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1024f) {
        String.format(Locale.US, "%.1f GB", mb / 1024f)
    } else {
        String.format(Locale.US, "%.0f MB", mb)
    }
}
