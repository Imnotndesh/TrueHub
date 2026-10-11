package com.imnotndesh.truehub.ui.setup
import androidx.compose.material3.toShape

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.background.ExpressiveScreenBackground

/**
 * Adaptive container for the first-run onboarding flow (Welcome / Server setup / Login).
 *
 * - Compact width (phones portrait): a single centered, scrollable column — [brand] sits at the
 *   top, then [content], with an optional [compactLeading] (e.g. a back button) above and
 *   [actions] pinned to the bottom.
 * - Medium width and up (tablets/foldables/landscape): the window is split edge-to-edge into a
 *   coloured brand pane on the start side and the task content on the end side, so the extra
 *   space is used deliberately instead of leaving a narrow column marooned in whitespace.
 *
 * Content and state are shared between both forms; only the arrangement changes, so nothing is
 * lost when the window is resized, folded, or rotated.
 */
@Composable
fun OnboardingScaffold(
    brand: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    brandLeading: (@Composable () -> Unit)? = null,
    compactLeading: (@Composable () -> Unit)? = null,
    compactAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val window = LocalTrueHubWindow.current
    ExpressiveScreenBackground {
        if (window.isAtLeastMedium) {
            Row(modifier.fillMaxSize()) {
                // Brand pane — full-height, edge-to-edge expressive surface.
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .systemBarsPadding()
                        .imePadding()
                        .padding(horizontal = 48.dp, vertical = 40.dp)
                ) {
                    brandLeading?.invoke()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        brand()
                    }
                }

                // Task pane — full-height, edge-to-edge.
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .systemBarsPadding()
                        .imePadding()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 56.dp, vertical = 48.dp),
                            content = content
                        )
                    }
                    actions?.invoke()
                }
            }
        } else {
            Column(modifier = modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                compactLeading?.invoke()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = compactAlignment,
                    verticalArrangement = Arrangement.Center
                ) {
                    brand()
                    Spacer(Modifier.height(36.dp))
                    content()
                }
                actions?.invoke()
            }
        }
    }
}

/**
 * Shared brand block used inside [OnboardingScaffold]. Renders an animated Material shape holding
 * the app icon, the product name, and a one-line tagline.
 */
@Composable
fun OnboardingBrand(
    title: String,
    tagline: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Dns,
    onContainer: Boolean = false,
    shapeSize: Dp = 128.dp,
    titleStyle: TextStyle = MaterialTheme.typography.displaySmall,
) {
    val iconContainer = if (onContainer) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val iconTint = if (onContainer) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedBrandShape(
            containerColor = iconContainer,
            icon = icon,
            iconTint = iconTint,
            size = shapeSize
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = title,
            style = titleStyle,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = tagline,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * The expressive Material shape behind the brand icon. The shape slowly rotates and gently
 * "breathes" while the icon stays upright; motion is decorative and non-blocking.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnimatedBrandShape(
    containerColor: Color,
    icon: ImageVector,
    iconTint: Color,
    size: Dp,
) {
    val transition = rememberInfiniteTransition(label = "brandShape")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val shape = MaterialShapes.Cookie9Sided
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
                .clip(shape.toShape())
                .background(containerColor)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size * 0.46f),
            tint = iconTint
        )
    }
}
