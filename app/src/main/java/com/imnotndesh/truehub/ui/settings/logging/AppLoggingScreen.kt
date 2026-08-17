package com.imnotndesh.truehub.ui.settings.logging

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.imnotndesh.truehub.data.helpers.InternalLogger
import com.imnotndesh.truehub.data.helpers.LogFormat
import com.imnotndesh.truehub.data.helpers.LoggingPrefs
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Internal logging diagnostics screen. Lets an admin/dev reveal (via the 5-tap easter egg)
 * the tool, toggle live logging, pick a format, view the ring buffer, clear it, and share
 * an export file (CSV primary, JSON/TXT also available).
 */
@Composable
fun AppLoggingScreen(
    manager: com.imnotndesh.truehub.data.api.TrueNASApiManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    var enabled by remember { mutableStateOf(LoggingPrefs.isEnabled(app)) }
    var fileSink by remember { mutableStateOf(LoggingPrefs.isFileSinkEnabled(app)) }
    var format by remember { mutableStateOf(LoggingPrefs.format(app)) }
    var entries by remember { mutableStateOf<List<InternalLogger.Entry>>(InternalLogger.snapshot()) }
    var exporting by remember { mutableStateOf(false) }

    // Poll the ring buffer for the live view (kept cheap/lightweight).
    LaunchedEffect(enabled) {
        while (true) {
            entries = InternalLogger.snapshot()
            delay(800.milliseconds)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.imnotndesh.truehub.ui.components.UnifiedScreenHeader(
                title = "App Logging",
                subtitle = if (enabled) "Capturing" else "Disabled",
                isLoading = false,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable / file sink
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToggleRow(
                        title = "Enable Logging",
                        subtitle = "Capture dispatched API calls and app logs",
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            TrueHubLogger.setLoggingEnabled(app, it)
                        }
                    )
                    ToggleRow(
                        title = "Write to Local File",
                        subtitle = "Also append to rotating files under files/logs/",
                        checked = fileSink,
                        onCheckedChange = {
                            fileSink = it
                            InternalLogger.setFileSinkEnabled(app, it)
                        }
                    )
                }
            }

            // Format picker
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export Format", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LogFormat.entries.forEach { f ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { format = f; LoggingPrefs.setFormat(app, f) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = format == f, onClick = { format = f; LoggingPrefs.setFormat(app, f) })
                                Text(f.id.uppercase(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        exporting = true
                        val file = InternalLogger.export(app, format)
                        exporting = false
                        file?.let { shareFile(context, it) }
                    },
                    enabled = !exporting,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (exporting) "Exporting…" else "Share")
                }
                OutlinedButton(
                    onClick = { InternalLogger.clearBuffer() },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Buffer")
                }
                OutlinedButton(
                    onClick = { InternalLogger.clearFiles() },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Files")
                }
            }

            // Live log view (bounded, virtualized)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Log (${entries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (!enabled) Text("logging off", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LogView(entries = entries)
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LogView(entries: List<InternalLogger.Entry>) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No log entries captured yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    // Show newest at the bottom; scroll is on the card's own state (simple reverse list).
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        // Unkeyed to avoid duplicate-key crashes with rapid same-tag/ms log lines.
        items(items = entries.takeLast(400).asReversed()) { entry ->
            LogLine(entry)
        }
    }
}

@Composable
private fun LogLine(entry: InternalLogger.Entry) {
    val levelColor = when (entry.level) {
        'E' -> MaterialTheme.colorScheme.error
        'W' -> Color(0xFFB58900)
        'I' -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "(${entry.level})",
                style = MaterialTheme.typography.labelSmall,
                color = levelColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun shareFile(context: android.content.Context, file: java.io.File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(android.content.Intent.EXTRA_TEXT, "TrueHub internal log (${file.extension})")
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share log"))
}
