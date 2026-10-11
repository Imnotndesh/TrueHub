package com.imnotndesh.truehub.ui.components.expressive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.adaptive.AdaptiveTokens
import com.imnotndesh.truehub.ui.adaptive.LocalTrueHubWindow
import com.imnotndesh.truehub.ui.adaptive.adaptiveTokens

/**
 * The single hero card used by every detail page: leading tile, title, subtitle, optional badge,
 * optional trailing actions and an optional full-width action row.
 */
@Composable
fun DetailHeroCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(28.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    val body: @Composable () -> Unit = {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading?.invoke()
                if (leading != null) Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    subtitle?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    badge?.let {
                        Spacer(Modifier.height(6.dp))
                        it()
                    }
                }
                trailing?.let {
                    Spacer(Modifier.width(8.dp))
                    it()
                }
            }
            actions?.let {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    it()
                }
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, shape = shape, colors = colors, border = border, modifier = modifier.fillMaxWidth()) { body() }
    } else {
        Card(shape = shape, colors = colors, border = border, modifier = modifier.fillMaxWidth()) { body() }
    }
}

/** Rounded-square icon tile used as the [DetailHeroCard] leading slot. */
@Composable
fun DetailHeroIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 52.dp
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

/**
 * Detail page container: a full-bleed header, a centered column capped at [maxContentWidth], and an
 * optional supporting pane shown on Expanded windows and up (tablet split detail).
 */
@Composable
fun AdaptiveDetailScreen(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    maxContentWidth: Dp = AdaptiveTokens.ReadingMaxWidth,
    content: @Composable ColumnScope.() -> Unit
) {
    val window = LocalTrueHubWindow.current
    val t = adaptiveTokens(window)
    Column(modifier.fillMaxSize()) {
        header()
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().imePadding(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .weight(1f, fill = false)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = t.screenMargin, vertical = t.gutter),
                verticalArrangement = Arrangement.spacedBy(t.sectionSpacing),
                content = content
            )
            if (supporting != null && window.isAtLeastExpanded) {
                Column(
                    modifier = Modifier
                        .width(AdaptiveTokens.SupportingPaneWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = t.screenMargin, top = t.gutter, bottom = t.gutter),
                    verticalArrangement = Arrangement.spacedBy(t.sectionSpacing),
                    content = supporting
                )
            }
        }
        bottomBar?.invoke()
    }
}
