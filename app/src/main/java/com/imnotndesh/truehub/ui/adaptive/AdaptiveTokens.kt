package com.imnotndesh.truehub.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.floatingNavBottomClearance

/** Fixed sizes and caps (docs/adaptive-ui/04-layout-patterns.md §2). */
object AdaptiveTokens {
    val FormMaxWidth: Dp = 640.dp
    val ReadingMaxWidth: Dp = 840.dp
    val DetailPaneMaxWidth: Dp = 840.dp
    val ListPaneWidth: Dp = 360.dp
    val SupportingPaneWidth: Dp = 360.dp
    val DialogMaxWidth: Dp = 560.dp
    val SheetMaxWidth: Dp = 640.dp
    val DashboardMaxWidth: Dp = 1440.dp
    val MinCardWidth: Dp = 300.dp
    val MinTileWidth: Dp = 180.dp
}

/** Spacing that varies by width class. */
@Immutable
data class AdaptiveSpacing(
    val screenMargin: Dp,
    val gutter: Dp,
    val paneGap: Dp,
    val sectionSpacing: Dp,
)

fun adaptiveSpacingFor(width: WidthClass): AdaptiveSpacing = when (width) {
    WidthClass.Compact -> AdaptiveSpacing(16.dp, 12.dp, 24.dp, 16.dp)
    WidthClass.Medium -> AdaptiveSpacing(24.dp, 16.dp, 24.dp, 20.dp)
    WidthClass.Expanded -> AdaptiveSpacing(24.dp, 16.dp, 24.dp, 24.dp)
    WidthClass.Large -> AdaptiveSpacing(32.dp, 24.dp, 24.dp, 24.dp)
    WidthClass.ExtraLarge -> AdaptiveSpacing(32.dp, 24.dp, 24.dp, 28.dp)
}

@Composable
fun adaptiveTokens(window: TrueHubWindow = LocalTrueHubWindow.current): AdaptiveSpacing =
    adaptiveSpacingFor(window.width)

/** Which navigation UI the shell currently shows. Set by MainScreen (shell doc 02). */
enum class ShellNav { FloatingBar, Rail }

/** Defaults to [ShellNav.FloatingBar] so behaviour is unchanged until the shell provides it. */
val LocalShellNav = compositionLocalOf { ShellNav.FloatingBar }

/** Bottom padding scrollables must reserve for the floating bar; 0 when a rail is used. */
@Composable
fun navContentBottomPadding(): Dp =
    if (LocalShellNav.current == ShellNav.FloatingBar) floatingNavBottomClearance() else 0.dp
