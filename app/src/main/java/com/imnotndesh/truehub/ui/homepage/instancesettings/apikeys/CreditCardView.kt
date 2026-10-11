package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Remembers a smoothed device-tilt reading (roughly -1f..1f per axis) driven by the
 * accelerometer, so the calling composable can use it to move a "light sheen" across
 * a card surface the way a real plastic/metal card catches light when tilted in hand.
 */
@Composable
private fun rememberDeviceTilt(): Pair<Float, Float> {
    val context = LocalContext.current
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var smoothX = 0f
        var smoothY = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rawX = (event.values.getOrElse(0) { 0f } / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                val rawY = (event.values.getOrElse(1) { 0f } / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)

                // Low-pass filter for a smooth, natural glide rather than a jittery snap
                smoothX += (rawX - smoothX) * 0.12f
                smoothY += (rawY - smoothY) * 0.12f

                tiltX = smoothX
                tiltY = smoothY
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose { sensorManager?.unregisterListener(listener) }
    }

    return tiltX to tiltY
}

@Composable
fun CreditCardKeyDialog(
    title: String = "API Key Created",
    keyName: String,
    keyValue: String,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var showKeyText by remember { mutableStateOf(false) }
    var justCopied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1400.milliseconds)
            justCopied = false
        }
    }

    // Real accelerometer-driven tilt -> used to slide a specular highlight across the card
    val (deviceTiltX, deviceTiltY) = rememberDeviceTilt()

    // --- Entrance: a genuine "card flip" — rotates in around the Y axis while
    // sliding up and scaling, instead of a flat fade/scale ---
    val entranceProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "entrance"
    )
    val slideOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 120f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "slide"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "scale"
    )
    val flipRotationY by animateFloatAsState(
        targetValue = if (visible) 0f else -70f,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "flipY"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.72f else 0f,
        animationSpec = tween(400),
        label = "scrim"
    )

    // A gentle idle wobble that settles out shortly after the card lands, on top of
    // whatever the accelerometer is doing, so the card feels alive from the first frame
    val settleWobble = (1f - entranceProgress) * 10f

    // Combine the settle wobble with live device tilt for the final 3D rotation.
    val liveRotationX = (deviceTiltY * -8f) + (settleWobble * 0.3f)
    val liveRotationY = (deviceTiltX * 10f) + flipRotationY

    val animatedRotationX by animateFloatAsState(
        targetValue = liveRotationX,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "rotX"
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = liveRotationY,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "rotY"
    )

    fun dismiss() {
        scope.launch {
            visible = false
            delay(450.milliseconds)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { dismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Store this key now — it won't be shown again",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier
                    .width(340.dp)
                    .height(210.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        rotationX = animatedRotationX
                        rotationY = animatedRotationY
                        cameraDistance = 24f * density.density
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .offset(y = slideOffset.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showKeyText = !showKeyText },
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.primary.copy(alpha = 0.92f),
                                    colorScheme.tertiary.copy(alpha = 0.9f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithCache {
                                val brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.32f),
                                        Color.White.copy(alpha = 0.06f),
                                        Color.Transparent
                                    ),
                                    center = Offset(
                                        x = 500f + (deviceTiltX * 650f),
                                        y = 200f + (deviceTiltY * 420f)
                                    ),
                                    radius = 480f
                                )
                                onDrawBehind {
                                    drawRect(brush = brush, blendMode = BlendMode.Plus)
                                }
                            }
                    )

                    // Faint diagonal texture for a brushed-card feel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.05f),
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // --- Top row: chip + brand mark ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            ChipGraphic()
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "TRUEHUB",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    "API ACCESS",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        // --- Middle: the key itself, formatted like an embossed card number ---
                        Column {
                            Text(
                                if (showKeyText) formatAsCardGroups(keyValue) else maskedCardNumber(keyValue),
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp,
                                letterSpacing = 2.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (showKeyText) "Tap card to hide" else "Tap card to reveal",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp
                            )
                        }

                        // --- Bottom row: key name + actions, like a cardholder/expiry row ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    "KEY NAME",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 8.5.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    keyName.ifBlank { "UNNAMED" }.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { showKeyText = !showKeyText },
                                    modifier = Modifier.size(34.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        if (showKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showKeyText) "Hide key" else "Reveal key",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                FilledTonalIconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(keyValue))
                                        justCopied = true
                                    },
                                    modifier = Modifier.size(34.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    CopyIconSwap(justCopied)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(visible = justCopied, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    "Copied to clipboard",
                    color = colorScheme.tertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(if (justCopied) 12.dp else 8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { dismiss() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CopyIconSwap(showCheck: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = showCheck,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(150)) + fadeOut()
        ) {
            Icon(Icons.Default.Check, contentDescription = "Copied", modifier = Modifier.size(16.dp))
        }
        AnimatedVisibility(
            visible = !showCheck,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(150)) + fadeOut()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy key", modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ChipGraphic() {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE8D48A), Color(0xFFC9A94E), Color(0xFFE8D48A))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.2.dp)
                        .background(Color(0xFF8A7226).copy(alpha = 0.5f))
                )
            }
        }
    }
}

/** Masks a raw key the way a card number is masked, preserving the last 4 characters. */
private fun maskedCardNumber(key: String): String {
    val tail = key.takeLast(4)
    return "••••  ••••  ••••  $tail"
}

/** Breaks a revealed key into readable 4-character groups, card-number style. */
private fun formatAsCardGroups(key: String): String {
    return key.chunked(4).joinToString("  ")
}