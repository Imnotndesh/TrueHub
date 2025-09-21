package com.example.truehub.data.api_methods

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.PI
import kotlin.math.sin



@Composable
fun WavyGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Extract theme colors in Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer

    Box(modifier = modifier.fillMaxSize()) {
        // Base gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            surfaceColor,
                            surfaceContainerColor
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Wavy overlay using Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Create multiple wavy layers for depth
            val waveLayers = listOf(
                WaveLayer(
                    color = primaryColor.copy(alpha = 0.08f),
                    amplitude = height * 0.12f,
                    frequency = 0.8f,
                    verticalOffset = height * 0.15f
                ),
                WaveLayer(
                    color = secondaryColor.copy(alpha = 0.06f),
                    amplitude = height * 0.08f,
                    frequency = 1.2f,
                    verticalOffset = height * 0.25f
                ),
                WaveLayer(
                    color = tertiaryColor.copy(alpha = 0.05f),
                    amplitude = height * 0.15f,
                    frequency = 0.6f,
                    verticalOffset = height * 0.4f
                )
            )

            waveLayers.forEach { layer ->
                drawWaveLayer(
                    width = width,
                    height = height,
                    layer = layer
                )
            }
        }

        content()
    }
}

data class WaveLayer(
    val color: Color,
    val amplitude: Float,
    val frequency: Float,
    val verticalOffset: Float
)

fun DrawScope.drawWaveLayer(
    width: Float,
    height: Float,
    layer: WaveLayer
) {
    val path = Path()

    // Start from top-left, slightly offset
    path.moveTo(0f, layer.verticalOffset)

    // Create the main wave
    val steps = (width / 8).toInt()
    for (i in 0..steps) {
        val x = (i * width) / steps
        val waveY = layer.verticalOffset +
                sin((x / width) * 2 * PI * layer.frequency).toFloat() * layer.amplitude

        if (i == 0) {
            path.lineTo(x, waveY)
        } else {
            // Use quadratic curves for smoother waves
            val prevX = ((i - 1) * width) / steps
            val controlX = (prevX + x) / 2
            val controlY = layer.verticalOffset +
                    sin((controlX / width) * 2 * PI * layer.frequency).toFloat() * layer.amplitude
            path.quadraticBezierTo(controlX, controlY, x, waveY)
        }
    }

    // Create "dripping" effect with random drops
    val dropCount = 3
    for (i in 0 until dropCount) {
        val dropX = width * (0.2f + i * 0.3f)
        val dropStartY = layer.verticalOffset +
                sin((dropX / width) * 2 * PI * layer.frequency).toFloat() * layer.amplitude
        val dropHeight = layer.amplitude * (0.8f + i * 0.4f)

        // Create teardrop shape
        val dropPath = Path()
        dropPath.moveTo(dropX, dropStartY)
        dropPath.quadraticBezierTo(
            dropX - 15f, dropStartY + dropHeight * 0.3f,
            dropX, dropStartY + dropHeight * 0.7f
        )
        dropPath.quadraticBezierTo(
            dropX + 15f, dropStartY + dropHeight * 0.3f,
            dropX, dropStartY
        )

        drawPath(dropPath, layer.color)
    }

    // Complete the wave shape to fill the area below
    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()

    // Draw the wave with gradient
    val gradient = Brush.verticalGradient(
        colors = listOf(
            layer.color,
            layer.color.copy(alpha = layer.color.alpha * 0.3f),
            Color.Transparent
        ),
        startY = layer.verticalOffset,
        endY = height
    )

    drawPath(path, gradient)
}

// Usage example - replace your current Box background
@Composable
fun LoginScreenWithWavyBackground(auth: Auth, navController: NavController) {
    WavyGradientBackground {
        // Your existing login content here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Your login screen content...
        }
    }
}

// For animated waves (optional)
@Composable
fun AnimatedWavyGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Extract theme colors in Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer

    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.12f),
                            surfaceColor,
                            surfaceContainerColor
                        )
                    )
                )
        )

        // Animated wavy overlay
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Create animated wave
            val path = Path()
            path.moveTo(0f, height * 0.2f)

            val steps = 50
            for (i in 0..steps) {
                val x = (i * width) / steps
                val waveY = height * 0.2f +
                        sin((x / width) * 4 * PI + animatedOffset).toFloat() * height * 0.08f

                if (i == 0) {
                    path.lineTo(x, waveY)
                } else {
                    path.quadraticBezierTo(
                        x - width / steps / 2,
                        waveY - height * 0.02f,
                        x,
                        waveY
                    )
                }
            }

            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

            drawPath(
                path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.1f),
                        primaryColor.copy(alpha = 0.03f),
                        Color.Transparent
                    )
                )
            )
        }

        content()
    }
}