package com.imnotndesh.truehub.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No Activity found in Context")
}
object AdaptiveLayoutHelper {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun getWindowSizeClass(): WindowSizeClass {
        val context = LocalContext.current
        val activity = context.findActivity()
        return calculateWindowSizeClass(activity)
    }

    @Composable
    fun getColumnCount(): Int {
        val windowSizeClass = getWindowSizeClass()
        return when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 1
            WindowWidthSizeClass.Medium -> 2
            WindowWidthSizeClass.Expanded -> 3
            else -> 1
        }
    }

    @Composable
    fun getColumnCount(
        compact: Int = 1,
        medium: Int = 2,
        expanded: Int = 3
    ): Int {
        val windowSizeClass = getWindowSizeClass()
        return when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> compact
            WindowWidthSizeClass.Medium -> medium
            WindowWidthSizeClass.Expanded -> expanded
            else -> compact
        }
    }

    @Composable
    fun isCompact(): Boolean {
        val windowSizeClass = getWindowSizeClass()
        return windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    }

    @Composable
    fun isExpandedLayout(): Boolean {
        val windowSizeClass = getWindowSizeClass()
        return windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    }
    @Composable
    fun getWidthSizeClass(): WindowWidthSizeClass {
        val windowSizeClass = getWindowSizeClass()
        return windowSizeClass.widthSizeClass
    }

    @Composable
    fun getContentPadding(): Int {
        return when (getWidthSizeClass()) {
            WindowWidthSizeClass.Compact -> 16
            WindowWidthSizeClass.Medium -> 24
            WindowWidthSizeClass.Expanded -> 32
            else -> 16
        }
    }

    @Composable
    fun getHorizontalSpacing(): Int {
        return when (getWidthSizeClass()) {
            WindowWidthSizeClass.Compact -> 12
            WindowWidthSizeClass.Medium -> 16
            WindowWidthSizeClass.Expanded -> 20
            else -> 12
        }
    }
}
