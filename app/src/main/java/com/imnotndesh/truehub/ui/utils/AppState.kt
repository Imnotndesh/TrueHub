package com.imnotndesh.truehub.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun appStateContainerColor(state: String): Color = when (state.lowercase()) {
    "running" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
    "stopped", "exited" -> MaterialTheme.colorScheme.surfaceContainerHighest
    else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
}
