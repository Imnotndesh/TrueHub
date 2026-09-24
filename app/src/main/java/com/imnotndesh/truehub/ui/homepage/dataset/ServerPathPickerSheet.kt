package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Filesystem

data class ServerPathPickerConfig(
    val title: String = "Select path",
    val confirmLabel: String = "Select",
    val startPath: String = "/mnt",
    val showFiles: Boolean = false,
    val fileExtensions: List<String> = emptyList()
)

private fun isDirectory(entry: Filesystem.FileEntry): Boolean =
    entry.type?.equals("DIRECTORY", ignoreCase = true) == true

private fun Filesystem.FileEntry.matchesExtensions(extensions: List<String>): Boolean {
    if (extensions.isEmpty()) return true
    val fileName = name ?: return false
    return extensions.any { fileName.endsWith(".$it", ignoreCase = true) }
}

private fun parentPath(path: String): String? {
    val trimmed = path.trimEnd('/')
    if (trimmed.isEmpty() || trimmed == "/mnt" || trimmed == "/") return null
    val parent = trimmed.substringBeforeLast('/', "")
    return parent.ifBlank { "/" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerPathPickerSheet(
    manager: TrueNASApiManager,
    config: ServerPathPickerConfig = ServerPathPickerConfig(),
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentPath by remember { mutableStateOf(config.startPath) }
    var showFiles by remember { mutableStateOf(config.showFiles) }
    var entries by remember { mutableStateOf<List<Filesystem.FileEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentPath) {
        isLoading = true
        selectedFilePath = null
        when (val result = manager.filesystem.listdirEntriesWithResult(currentPath)) {
            is ApiResult.Success -> entries = result.data
            else -> entries = emptyList()
        }
        isLoading = false
    }

    val visibleEntries = remember(entries, showFiles) {
        entries
            .filter { entry ->
                isDirectory(entry) || (showFiles && entry.matchesExtensions(config.fileExtensions))
            }
            .sortedWith(compareByDescending<Filesystem.FileEntry> { isDirectory(it) }.thenBy { it.name?.lowercase() })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Show files", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        if (config.fileExtensions.isEmpty()) {
                            "Browse individual files in this folder"
                        } else {
                            "Show ${config.fileExtensions.joinToString(", ") { it.uppercase() }} files"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = showFiles, onCheckedChange = { showFiles = it })
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    visibleEntries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Empty folder", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            parentPath(currentPath)?.let { parent ->
                                item(key = "..") {
                                    PathEntryRow(
                                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                                        label = "..",
                                        subtitle = parent,
                                        selected = false,
                                        onClick = { currentPath = parent }
                                    )
                                }
                            }
                            items(visibleEntries, key = { it.path ?: it.name ?: it.hashCode().toString() }) { entry ->
                                val directory = isDirectory(entry)
                                PathEntryRow(
                                    icon = {
                                        Icon(
                                            imageVector = if (directory) {
                                                Icons.Default.Folder
                                            } else {
                                                Icons.AutoMirrored.Filled.InsertDriveFile
                                            },
                                            contentDescription = null
                                        )
                                    },
                                    label = entry.name ?: "(unnamed)",
                                    subtitle = if (directory) {
                                        "Folder"
                                    } else {
                                        entry.size?.let { formatEntrySize(it) } ?: "File"
                                    },
                                    selected = !directory && selectedFilePath == entry.path,
                                    onClick = {
                                        if (directory) {
                                            entry.path?.let { currentPath = it }
                                        } else {
                                            selectedFilePath = entry.path
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(
                    onClick = { onConfirm(selectedFilePath ?: currentPath) },
                    enabled = !showFiles || selectedFilePath != null,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) { Text(config.confirmLabel) }
            }
        }
    }
}

@Composable
private fun PathEntryRow(
    icon: @Composable () -> Unit,
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatEntrySize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.size - 1) {
        value /= 1024
        unit++
    }
    return String.format(java.util.Locale.US, "%.1f %s", value, units[unit])
}
