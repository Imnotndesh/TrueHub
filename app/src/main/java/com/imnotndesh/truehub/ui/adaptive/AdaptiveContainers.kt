package com.imnotndesh.truehub.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveShapeBox

/** Caps and centers [content]. Put the cap on the container, never on each child. */
@Composable
fun CenteredContent(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/**
 * Feed/grid container (recipe R-1): adaptive columns, capped and centered.
 * Use `item(span = { GridItemSpan(maxLineSpan) })` for heroes/headers.
 */
@Composable
fun AdaptiveGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    minCellWidth: Dp = AdaptiveTokens.MinCardWidth,
    maxContentWidth: Dp = AdaptiveTokens.DashboardMaxWidth,
    content: LazyGridScope.() -> Unit,
) {
    val t = adaptiveTokens()
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(minCellWidth),
            modifier = Modifier.widthIn(max = maxContentWidth).fillMaxHeight(),
            contentPadding = PaddingValues(
                start = t.screenMargin,
                end = t.screenMargin,
                top = t.gutter,
                bottom = t.gutter + navContentBottomPadding(),
            ),
            horizontalArrangement = Arrangement.spacedBy(t.gutter),
            verticalArrangement = Arrangement.spacedBy(t.gutter),
            content = content,
        )
    }
}

/** Grid of info cards for sections inside a pane/page (min 360dp per column). */
@Composable
fun InfoCardGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    maxContentWidth: Dp = 1240.dp,
    minCellWidth: Dp = 360.dp,
    content: LazyGridScope.() -> Unit,
) {
    AdaptiveGrid(
        modifier = modifier,
        state = state,
        minCellWidth = minCellWidth,
        maxContentWidth = maxContentWidth,
        content = content,
    )
}

/**
 * Form/settings page container (recipes R-4/R-7): [header] on top spanning the window, a form column capped at
 * [AdaptiveTokens.FormMaxWidth] and centered, an optional [supporting] column (Expanded and up) and an optional
 * [bottomBar] that stays full-bleed (cap its inner content yourself with [CenteredContent]).
 */
@Composable
fun AdaptiveFormScreen(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val window = LocalTrueHubWindow.current
    val t = adaptiveTokens(window)
    Column(modifier.fillMaxSize()) {
        header()
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth().imePadding(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = AdaptiveTokens.FormMaxWidth)
                    .weight(1f, fill = false)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = t.screenMargin, vertical = t.gutter),
                verticalArrangement = Arrangement.spacedBy(t.gutter),
                content = content,
            )
            if (supporting != null && window.isAtLeastExpanded) {
                Column(
                    modifier = Modifier
                        .width(AdaptiveTokens.SupportingPaneWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = t.screenMargin, top = t.gutter, bottom = t.gutter),
                    verticalArrangement = Arrangement.spacedBy(t.gutter),
                    content = supporting,
                )
            }
        }
        bottomBar?.invoke()
    }
}

/** Empty detail pane placeholder (charm C-9: a Material shape + one sentence). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyDetailPlaceholder(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ExpressiveShapeBox(
            shape = MaterialShapes.Cookie6Sided,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.width(96.dp).height(96.dp),
        ) {}
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
