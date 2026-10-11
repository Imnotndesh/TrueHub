package com.imnotndesh.truehub.ui.adaptive

import android.content.res.Configuration
import android.view.InputDevice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window width classes (Google breakpoints, see docs/adaptive-ui/03-window-size-classes.md).
 * Declared in ascending order so `>=` comparisons are valid.
 */
enum class WidthClass { Compact, Medium, Expanded, Large, ExtraLarge }

enum class HeightClass { Compact, Medium, Expanded }

fun widthClassFor(widthDp: Float): WidthClass = when {
    widthDp >= 1600f -> WidthClass.ExtraLarge
    widthDp >= 1200f -> WidthClass.Large
    widthDp >= 840f -> WidthClass.Expanded
    widthDp >= 600f -> WidthClass.Medium
    else -> WidthClass.Compact
}

fun heightClassFor(heightDp: Float): HeightClass = when {
    heightDp >= 900f -> HeightClass.Expanded
    heightDp >= 480f -> HeightClass.Medium
    else -> HeightClass.Compact
}

/**
 * Everything a screen may know about its window. Computed once at the root by
 * [ProvideTrueHubWindow]; screens read [LocalTrueHubWindow] and pass values down as parameters.
 */
@Immutable
data class TrueHubWindow(
    val width: WidthClass,
    val height: HeightClass,
    val widthDp: Dp,
    val heightDp: Dp,
    val isTabletop: Boolean = false,
    val hasPrecisePointer: Boolean = false,
    val hasPhysicalKeyboard: Boolean = false,
) {
    val isCompactWidth: Boolean get() = width == WidthClass.Compact
    val isAtLeastMedium: Boolean get() = width >= WidthClass.Medium
    val isAtLeastExpanded: Boolean get() = width >= WidthClass.Expanded
    val isAtLeastLarge: Boolean get() = width >= WidthClass.Large
    val isCompactHeight: Boolean get() = height == HeightClass.Compact

    /** Two or more panes are allowed. */
    val allowsMultiPane: Boolean get() = isAtLeastExpanded
}

val LocalTrueHubWindow = staticCompositionLocalOf<TrueHubWindow> {
    error("TrueHubWindow not provided. Wrap content in ProvideTrueHubWindow at the root.")
}

/** Builds a [TrueHubWindow] for previews and tests. */
fun fakeTrueHubWindow(
    width: WidthClass = WidthClass.Compact,
    height: HeightClass = HeightClass.Medium,
    hasPrecisePointer: Boolean = false,
    hasPhysicalKeyboard: Boolean = false,
): TrueHubWindow {
    val w = when (width) {
        WidthClass.Compact -> 360.dp
        WidthClass.Medium -> 700.dp
        WidthClass.Expanded -> 1024.dp
        WidthClass.Large -> 1280.dp
        WidthClass.ExtraLarge -> 1600.dp
    }
    val h = when (height) {
        HeightClass.Compact -> 360.dp
        HeightClass.Medium -> 800.dp
        HeightClass.Expanded -> 1000.dp
    }
    return TrueHubWindow(width, height, w, h, false, hasPrecisePointer, hasPhysicalKeyboard)
}

/** Preview/test helper. */
@Composable
fun ProvideTrueHubWindowForPreview(
    width: WidthClass = WidthClass.Compact,
    height: HeightClass = HeightClass.Medium,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalTrueHubWindow provides fakeTrueHubWindow(width, height), content = content)
}

/**
 * Call once, at the root of the activity content, inside the theme.
 * Window size comes from the actual window container (not the physical screen), so it updates
 * live on split-screen, desktop-window resize, fold/unfold.
 */
@Composable
fun ProvideTrueHubWindow(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val size = LocalWindowInfo.current.containerSize
    val widthDp = with(density) { size.width.toDp() }
    val heightDp = with(density) { size.height.toDp() }
    val configuration = LocalConfiguration.current
    val hasKeyboard = configuration.keyboard != Configuration.KEYBOARD_NOKEYS &&
        configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    // Recomputed when the configuration (keyboard/mouse attach) or size changes.
    val hasPointer = remember(configuration, size) { detectPrecisePointer() }

    val value = remember(widthDp, heightDp, hasKeyboard, hasPointer) {
        TrueHubWindow(
            width = widthClassFor(widthDp.value),
            height = heightClassFor(heightDp.value),
            widthDp = widthDp,
            heightDp = heightDp,
            isTabletop = false, // TODO(adaptive): fill from posture when the adaptive library is added
            hasPrecisePointer = hasPointer,
            hasPhysicalKeyboard = hasKeyboard,
        )
    }
    CompositionLocalProvider(LocalTrueHubWindow provides value, content = content)
}

private fun detectPrecisePointer(): Boolean = try {
    InputDevice.getDeviceIds().any { id ->
        val device = InputDevice.getDevice(id)
        device != null && (
            device.supportsSource(InputDevice.SOURCE_MOUSE) ||
                device.supportsSource(InputDevice.SOURCE_TOUCHPAD)
            )
    }
} catch (_: Throwable) {
    false
}
