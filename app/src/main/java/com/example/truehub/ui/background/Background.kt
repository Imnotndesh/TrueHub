package com.example.truehub.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin

/**
 * A more performant wavy gradient background.
 *
 * This version simplifies the drawing logic by using a single path per wave and removing
 * the computationally expensive "dripping" effect. This results in fewer calculations
 * and a smoother experience.
 */
@Composable
fun WavyGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Extract theme colors once.
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.fillMaxSize()) {
        // A simple, non-blocking gradient background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.05f),
                            surfaceColor
                        )
                    )
                )
        )

        // The optimized Canvas drawing.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Define wave layers. Reduced alpha and complexity for better performance.
            val waveLayers = listOf(
                WaveLayer(
                    color = primaryColor.copy(alpha = 0.06f),
                    amplitude = height * 0.1f,
                    frequency = 0.7f,
                    verticalOffset = height * 0.1f
                ),
                WaveLayer(
                    color = secondaryColor.copy(alpha = 0.04f),
                    amplitude = height * 0.07f,
                    frequency = 1.0f,
                    verticalOffset = height * 0.2f
                ),
                WaveLayer(
                    color = tertiaryColor.copy(alpha = 0.03f),
                    amplitude = height * 0.12f,
                    frequency = 0.5f,
                    verticalOffset = height * 0.3f
                )
            )

            waveLayers.forEach { layer ->
                drawOptimizedWave(
                    width = width,
                    height = height,
                    layer = layer
                )
            }
        }

        // The actual UI content is placed on top.
        content()
    }
}
data class WaveLayer(
    val color: Color,
    val amplitude: Float,
    val frequency: Float,
    val verticalOffset: Float
)

/**
 * Optimized wave drawing function for DrawScope.
 *
 * It uses a single continuous path for the wave and fills the area below it,
 * which is much more efficient than the previous implementation.
 */
private fun DrawScope.drawOptimizedWave(
    width: Float,
    height: Float,
    layer: WaveLayer
) {
    val path = Path()
    path.moveTo(0f, height)
    val startY = layer.verticalOffset + (sin(0.0) * layer.amplitude).toFloat()
    path.lineTo(0f, startY)
    val steps = (width / 20).toInt().coerceAtLeast(20)
    for (i in 0..steps) {
        val x = (i * width) / steps
        val y = layer.verticalOffset + (sin((x / width) * 2 * PI * layer.frequency) * layer.amplitude).toFloat()
        path.lineTo(x, y)
    }
    path.lineTo(width, height)
    path.close()
    drawPath(path, color = layer.color)
}


/**
 * An optional animated version of the background.
 * Note: Animation always has a performance cost. Use judiciously.
 */
@Composable
fun AnimatedWavyGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing)
        ),
        label = "wave_phase_shift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.12f),
                            surfaceColor
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val path = Path()

            path.moveTo(0f, height)
            path.lineTo(0f, height * 0.2f)

            val steps = (width / 20).toInt().coerceAtLeast(20)
            for (i in 0..steps) {
                val x = (i * width) / steps
                val y = height * 0.2f + (sin((x / width) * 2 * PI + phaseShift) * height * 0.05f).toFloat()
                path.lineTo(x, y)
            }

            path.lineTo(width, height)
            path.close()

            drawPath(path, color = primaryColor.copy(alpha = 0.1f))
        }

        content()
    }
}