package com.example.truehub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.truehub.TrueHubAppTheme

@Composable
fun LoadingScreen(message: String = "Loading...") {
    TrueHubAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ServerRackAnimation()
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ServerRackAnimation(
    modifier: Modifier = Modifier,
    size: Float = 120f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "server")

    // LED blink animations with different phases
    val led1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led1"
    )

    val led2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led2"
    )

    val led3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led3"
    )

    val primary = MaterialTheme.colorScheme.primary
    val primaryVariant = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(size.dp)) {
        val scale = this.size.width / 48f

        // Rack Frame
        drawRoundRect(
            color = primary.copy(alpha = 0.15f),
            topLeft = Offset(12f * scale, 8f * scale),
            size = Size(24f * scale, 32f * scale),
            cornerRadius = CornerRadius(1f * scale),
            style = Stroke(width = 1f * scale)
        )

        drawRoundRect(
            color = primary.copy(alpha = 0.08f),
            topLeft = Offset(12f * scale, 8f * scale),
            size = Size(24f * scale, 32f * scale),
            cornerRadius = CornerRadius(1f * scale)
        )

        // Server 1
        drawRoundRect(
            color = primary.copy(alpha = 0.3f),
            topLeft = Offset(14f * scale, 10f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )
        drawCircle(
            color = secondary.copy(alpha = led1),
            radius = 1f * scale,
            center = Offset(31f * scale, 13f * scale)
        )
        drawCircle(
            color = primary.copy(alpha = led1 * 0.8f),
            radius = 0.8f * scale,
            center = Offset(28f * scale, 13f * scale)
        )

        // Server 2
        drawRoundRect(
            color = primaryVariant.copy(alpha = 0.4f),
            topLeft = Offset(14f * scale, 18f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )
        drawCircle(
            color = secondary.copy(alpha = led2),
            radius = 1f * scale,
            center = Offset(31f * scale, 21f * scale)
        )
        drawCircle(
            color = primary.copy(alpha = led2 * 0.8f),
            radius = 0.8f * scale,
            center = Offset(28f * scale, 21f * scale)
        )

        // Server 3
        drawRoundRect(
            color = primary.copy(alpha = 0.25f),
            topLeft = Offset(14f * scale, 26f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )
        drawCircle(
            color = secondary.copy(alpha = led3),
            radius = 1f * scale,
            center = Offset(31f * scale, 29f * scale)
        )
        drawCircle(
            color = primary.copy(alpha = led3 * 0.8f),
            radius = 0.8f * scale,
            center = Offset(28f * scale, 29f * scale)
        )
    }
}

// Compact version for use anywhere in the app
@Composable
fun ServerRackIcon(
    modifier: Modifier = Modifier,
    size: Float = 24f,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(size.dp)) {
        val scale = this.size.width / 48f

        // Rack Frame
        drawRoundRect(
            color = tint.copy(alpha = 0.2f),
            topLeft = Offset(12f * scale, 8f * scale),
            size = Size(24f * scale, 32f * scale),
            cornerRadius = CornerRadius(1f * scale),
            style = Stroke(width = 1f * scale)
        )

        // Server 1
        drawRoundRect(
            color = tint.copy(alpha = 0.6f),
            topLeft = Offset(14f * scale, 10f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )

        // Server 2
        drawRoundRect(
            color = tint.copy(alpha = 0.8f),
            topLeft = Offset(14f * scale, 18f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )

        // Server 3
        drawRoundRect(
            color = tint,
            topLeft = Offset(14f * scale, 26f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(0.5f * scale)
        )
    }
}