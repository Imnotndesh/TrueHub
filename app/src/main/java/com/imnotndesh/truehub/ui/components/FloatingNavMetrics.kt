package com.imnotndesh.truehub.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FloatingNavMetrics {
    val barHeight: Dp = 56.dp
    val barMargin: Dp = 16.dp
    val barGap: Dp = 10.dp
    val itemPaddingHorizontal: Dp = 12.dp
    val itemPaddingHorizontalSelected: Dp = 16.dp
    val itemPaddingVertical: Dp = 8.dp
    val searchButtonSize: Dp = 52.dp
}

@Composable
fun floatingNavBottomClearance(): Dp {
    val navigationBars = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return FloatingNavMetrics.barHeight + FloatingNavMetrics.barMargin * 2 + navigationBars
}
