package com.imnotndesh.truehub.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.alerts.AlertsBellButton
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun UnifiedScreenHeader(
    title: String,
    subtitle: String,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String?,
    onRefresh: (() -> Unit)? = null,
    onDismissError: () -> Unit,
    manager: TrueNASApiManager,
    onBackPressed: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onShutdownInvoke: (() -> Unit)? = null,
    trailingActions: @Composable RowScope.() -> Unit = {},
    onSearchClick: (() -> Unit)? = null
) {
    var isSubtitleVisible by remember { mutableStateOf(true) }

    LaunchedEffect(subtitle) {
        isSubtitleVisible = true
        if (subtitle.contains("Welcome back", ignoreCase = true)) {
            delay(4000.milliseconds)
            isSubtitleVisible = false
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBackPressed != null) {
                        ExpressiveIconButton(
                            onClick = onBackPressed,
                            icon = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            enabled = !isLoading && !isRefreshing,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {

                        AnimatedContent(
                            targetState = title,
                            transitionSpec = {
                                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut())
                            },
                            label = "TitleAnimation"
                        ) { targetTitle ->
                            Text(
                                text = targetTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }


                        AnimatedVisibility(
                            visible = isSubtitleVisible && subtitle.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trailingActions()

                    if (onSearchClick != null) {
                        ExpressiveIconButton(
                            onClick = onSearchClick,
                            icon = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AlertsBellButton(manager = manager)

                    onRefresh?.let{ onRefresh ->
                        ExpressiveIconButton(
                            onClick = onRefresh,
                            icon = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            enabled = !isLoading && !isRefreshing,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    onNavigateToSettings?.let { settingsCallback ->
                        ExpressiveIconButton(
                            onClick = settingsCallback,
                            icon = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    onShutdownInvoke?.let { shutdownCallback ->
                        ExpressiveIconButton(
                            onClick = shutdownCallback,
                            icon = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = isRefreshing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(2.dp)
                            ),
                        color = MaterialTheme.colorScheme.primary, trackColor = Color.Transparent
                    )
                }
            }

            AnimatedVisibility(
                visible = error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = onDismissError
                            ) {
                                Text(
                                    "Dismiss",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Minimal header variant: renders only the back button, using the exact
 * same [ExpressiveIconButton] styling as [UnifiedScreenHeader]'s back
 * action (same icon, same press-scale animation, same default container
 * color).
 *
 * Unlike [UnifiedScreenHeader], this does NOT wrap itself in a full-width
 * [Surface]/[Column] -- there's no title, subtitle, trailing actions,
 * refresh bar, or error card. It's just the button, sized to its content,
 * so it doesn't claim a header-height band across the top of the screen.
 * It's meant to be layered as an overlay (e.g. inside a [Box]) on top of
 * your own content, so that content can occupy the space a full header
 * would otherwise have taken -- including scrolling/being visible behind
 * and around the button itself.
 *
 * Example:
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     YourContent(modifier = Modifier.fillMaxSize())
 *     MinimalBackHeader(
 *         onBackPressed = onNavigateBack,
 *         modifier = Modifier
 *             .align(Alignment.TopStart)
 *             .padding(16.dp)
 *     )
 * }
 * ```
 */
@Composable
fun MinimalBackHeader(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    error: String? = null,
    onDismissError: () -> Unit = {}
) {
    Column(modifier = modifier) {
        ExpressiveIconButton(
            onClick = onBackPressed,
            icon = Icons.Default.ArrowBackIosNew,
            contentDescription = "Back",
            enabled = enabled,
            containerColor = containerColor
        )

        AnimatedVisibility(
            visible = error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            if (error != null) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.widthIn(max = 240.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = onDismissError) {
                                Text(
                                    "Dismiss",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = Color.Transparent
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "ButtonScale"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = tint,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = tint.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .scale(scale)
            .background(
                color = if (containerColor != Color.Transparent) containerColor else Color.Transparent,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}