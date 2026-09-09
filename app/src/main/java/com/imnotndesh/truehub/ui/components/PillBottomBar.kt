package com.imnotndesh.truehub.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * Controls a floating bottom bar's visibility from any nested scrollable inside the content.
 * Scrolling down hides it, scrolling up reveals it. Attach the returned connection to the
 * scrollable ancestor via Modifier.nestedScroll.
 */
class HideOnScrollState {
    internal var offset: Float by mutableFloatStateOf(0f)

    internal val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val dy = available.y
            if (source == NestedScrollSource.UserInput) {
                offset = (offset + dy).coerceIn(-1f, 0f)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val target = if (available.y < 0f) -1f else 0f
            offset = target
            return Velocity.Zero
        }
    }
}

@Composable
fun rememberHideOnScrollState(): HideOnScrollState = remember { HideOnScrollState() }

@Composable
fun PillNavBar(
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    barHeight: Dp = 56.dp,
    content: @Composable () -> Unit
) {
    val offsetY by animateDpAsState(
        targetValue = if (hidden) barHeight + 32.dp else 0.dp,
        animationSpec = spring(),
        label = "pillBarSlide"
    )
    Box(
        modifier = modifier.offset(y = offsetY).padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            content()
        }
    }
}
