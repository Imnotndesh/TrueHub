package com.imnotndesh.truehub.ui.services.vm

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.graphics.shapes.RoundedPolygon
import com.imnotndesh.truehub.ui.components.expressive.StatusTone

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun vmShape(): RoundedPolygon = MaterialShapes.ClamShell

internal fun vmStatusTone(state: String): StatusTone = when (state.lowercase()) {
    "running" -> StatusTone.POSITIVE
    "stopped" -> StatusTone.NEUTRAL
    "suspended" -> StatusTone.INFO
    "error" -> StatusTone.NEGATIVE
    else -> StatusTone.OUTLINE
}

internal fun vmStatusIcon(state: String): ImageVector = when (state.lowercase()) {
    "running" -> Icons.Default.PlayArrow
    "stopped" -> Icons.Default.Stop
    "suspended" -> Icons.Default.Pause
    "error" -> Icons.Default.Error
    else -> Icons.Default.PowerSettingsNew
}

internal fun vmStatusLabel(state: String): String =
    state.lowercase().replaceFirstChar { it.uppercase() }
