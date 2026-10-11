package com.imnotndesh.truehub.ui.services.containers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.graphics.shapes.RoundedPolygon
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.expressive.StatusTone
import java.util.Locale

internal const val BYTES_PER_MB = 1024L * 1024L
internal const val MIN_CONTAINER_MEMORY_MB = 32
internal const val MIN_ROOT_DISK_GB = 5
internal const val MIN_VOLUME_SIZE_MB = 512

internal fun formatMemory(bytes: Long?): String {
    if (bytes == null) return "Default"
    val gigabytes = bytes.toDouble() / (1024 * 1024 * 1024)
    return if (gigabytes >= 1.0) {
        String.format(Locale.US, "%.1f GB", gigabytes)
    } else {
        String.format(Locale.US, "%.0f MB", bytes.toDouble() / BYTES_PER_MB)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun containerShape(type: Virt.Type): RoundedPolygon = when (type) {
    Virt.Type.CONTAINER -> MaterialShapes.Cookie4Sided
    Virt.Type.VM -> MaterialShapes.ClamShell
}

internal fun containerTypeIcon(type: Virt.Type): ImageVector = when (type) {
    Virt.Type.CONTAINER -> Icons.Default.ViewInAr
    Virt.Type.VM -> Icons.Default.DesktopWindows
}

@Composable
internal fun containerTypeContainerColor(type: Virt.Type): Color = when (type) {
    Virt.Type.CONTAINER -> MaterialTheme.colorScheme.primaryContainer
    Virt.Type.VM -> MaterialTheme.colorScheme.tertiaryContainer
}

@Composable
internal fun containerTypeContentColor(type: Virt.Type): Color = when (type) {
    Virt.Type.CONTAINER -> MaterialTheme.colorScheme.onPrimaryContainer
    Virt.Type.VM -> MaterialTheme.colorScheme.onTertiaryContainer
}

internal fun containerStatusTone(status: Virt.Status): StatusTone = when (status) {
    Virt.Status.RUNNING -> StatusTone.POSITIVE
    Virt.Status.STOPPED -> StatusTone.NEUTRAL
    Virt.Status.ERROR -> StatusTone.NEGATIVE
    Virt.Status.FROZEN -> StatusTone.INFO
    Virt.Status.STARTING, Virt.Status.STOPPING,
    Virt.Status.FREEZING, Virt.Status.THAWED,
    Virt.Status.ABORTING -> StatusTone.WARNING
    Virt.Status.UNKNOWN -> StatusTone.OUTLINE
}

internal fun containerStatusIcon(status: Virt.Status): ImageVector = when (status) {
    Virt.Status.RUNNING -> Icons.Default.PlayArrow
    Virt.Status.STOPPED -> Icons.Default.Stop
    Virt.Status.ERROR -> Icons.Default.Error
    Virt.Status.FROZEN -> Icons.Default.Pause
    else -> Icons.Default.Refresh
}

internal fun containerStatusLabel(status: Virt.Status): String =
    status.name.lowercase().replaceFirstChar { it.uppercase() }
