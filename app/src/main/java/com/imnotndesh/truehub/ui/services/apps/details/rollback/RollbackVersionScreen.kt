package com.imnotndesh.truehub.ui.services.apps.details.rollback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.ServerRackAnimation
import com.imnotndesh.truehub.ui.haptics.VibrationFeedback
import com.imnotndesh.truehub.ui.haptics.VibratorMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RollbackVersionScreen(
    appName: String,
    versions: List<String>,
    isLoadingVersions: Boolean,
    fetchError: String?,
    onConfirmRollback: (version: String, rollbackSnapshot: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activeJobs by JobRepository.activeJobs.collectAsState()
    val liveJob = activeJobs.values.find { it.appName == appName }

    val isRollingBack = liveJob != null && liveJob.state !in listOf("SUCCESS", "FAILED", "ABORTED")
    val isDone = liveJob?.state == "SUCCESS"
    val isFailed = liveJob?.state in listOf("FAILED", "ABORTED")

    val context = LocalContext.current
    val haptics = remember(context) { VibrationFeedback(context) }
    var terminalFeedbackPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(isDone, isFailed) {
        if (!terminalFeedbackPlayed) {
            when {
                isDone -> { haptics.play(VibratorMode.SUCCESS_TICK); terminalFeedbackPlayed = true }
                isFailed -> { haptics.play(VibratorMode.ERROR_ALERT); terminalFeedbackPlayed = true }
            }
        }
    }

    var selectedVersion by remember { mutableStateOf<String?>(null) }
    var rollbackSnapshot by remember { mutableStateOf(true) }

    LaunchedEffect(isDone) {
        if (isDone) {
            kotlinx.coroutines.delay(1800.milliseconds)
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = isRollingBack || isDone || isFailed,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 4 })
            },
            label = "rollback_screen_state",
            modifier = Modifier.fillMaxSize()
        ) { showingProgress ->
            if (showingProgress) {
                RollingBackView(
                    appName = appName,
                    targetVersion = selectedVersion ?: "Unknown",
                    progress = liveJob?.progress ?: 0,
                    description = liveJob?.description,
                    isDone = isDone,
                    isFailed = isFailed,
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VersionSelectionView(
                    modifier = Modifier.fillMaxSize(),
                    appName = appName,
                    versions = versions,
                    isLoading = isLoadingVersions,
                    error = fetchError,
                    selectedVersion = selectedVersion,
                    rollbackSnapshot = rollbackSnapshot,
                    onVersionSelected = { selectedVersion = it },
                    onRollbackSnapshotChange = { rollbackSnapshot = it },
                    onConfirm = { selectedVersion?.let { onConfirmRollback(it, rollbackSnapshot) } },
                    onNavigateBack = onNavigateBack,
                )
            }
        }

        MinimalBackHeader(
            onBackPressed = onNavigateBack,
            error = fetchError,
            onDismissError = {},
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun VersionSelectionView(
    modifier: Modifier = Modifier,
    appName: String,
    versions: List<String>,
    isLoading: Boolean,
    error: String?,
    selectedVersion: String?,
    rollbackSnapshot: Boolean,
    onVersionSelected: (String) -> Unit,
    onRollbackSnapshotChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(72.dp))

        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "Rollback $appName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select Previous Version",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            error != null -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Failed to load versions:\n$error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            versions.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No previous versions available to rollback to.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(versions) { version ->
                        val isSelected = version == selectedVersion
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVersionSelected(version) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.History,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = version,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rollback options
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Rollback Snapshot",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Restore the dataset snapshot taken at the time of upgrade",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = rollbackSnapshot,
                            onCheckedChange = onRollbackSnapshotChange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
            Button(
                onClick = onConfirm,
                enabled = selectedVersion != null && !isLoading && error == null,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rollback")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RollingBackView(
    appName: String,
    targetVersion: String,
    progress: Int,
    description: String?,
    isDone: Boolean,
    isFailed: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat() / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "smoothProgress"
    )

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ServerRackAnimation(
            size = 400f,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = when {
                isDone -> "done"
                isFailed -> "failed"
                else -> "running"
            },
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(200))
            },
            label = "status_text"
        ) { state ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (state) {
                        "done" -> "Rollback Complete"
                        "failed" -> "Rollback Failed"
                        else -> "Rolling back $appName"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = when (state) {
                        "failed" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (state) {
                        "done" -> "Successfully restored to $targetVersion"
                        "failed" -> "Something went wrong during the rollback."
                        else -> description ?: "Restoring to $targetVersion..."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = !isDone && !isFailed,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = isFailed,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
private fun PixelShapesOrbit(
    primaryColor: Color,
    secondaryColor: Color,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    val speedScale by animateFloatAsState(
        targetValue = if (isDone) 0f else 1f,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "speed_scale"
    )

    val ring1Angle = remember { Animatable(0f) }
    val ring2Angle = remember { Animatable(0f) }
    val ring3Angle = remember { Animatable(0f) }

    LaunchedEffect(speedScale) {

    }

    // Use withFrameMillis for smooth per-frame updates
    val ring1AngleState = remember { mutableFloatStateOf(0f) }
    val ring2AngleState = remember { mutableFloatStateOf(0f) }
    val ring3AngleState = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val dt = (now - lastTime) / 1000f
            lastTime = now
            val s = speedScale
            ring1AngleState.floatValue += dt * 45f * s
            ring2AngleState.floatValue -= dt * 28f * s
            ring3AngleState.floatValue += dt * 18f * s
        }
    }

    val r1 = ring1AngleState.floatValue
    val r2 = ring2AngleState.floatValue
    val r3 = ring3AngleState.floatValue

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val unit = size.minDimension / 2f

        drawOrbitRing(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.42f,
            baseAngleDeg = r1,
            shapeCount = 3,
            shapeSize = unit * 0.18f,
            cornerFraction = 0.35f,
            color = primaryColor.copy(alpha = 0.85f)
        )

        drawOrbitRingPills(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.68f,
            baseAngleDeg = r2,
            shapeCount = 4,
            pillWidth = unit * 0.22f,
            pillHeight = unit * 0.09f,
            color = secondaryColor.copy(alpha = 0.55f)
        )

        drawOrbitRing(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.88f,
            baseAngleDeg = r3,
            shapeCount = 5,
            shapeSize = unit * 0.11f,
            cornerFraction = 0.4f,
            color = primaryColor.copy(alpha = 0.35f)
        )
    }
}

private fun DrawScope.drawOrbitRing(
    cx: Float, cy: Float,
    orbitRadius: Float,
    baseAngleDeg: Float,
    shapeCount: Int,
    shapeSize: Float,
    cornerFraction: Float,
    color: Color
) {
    val stepDeg = 360f / shapeCount
    repeat(shapeCount) { i ->
        val angleDeg = baseAngleDeg + stepDeg * i
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val x = cx + orbitRadius * cos(angleRad).toFloat()
        val y = cy + orbitRadius * sin(angleRad).toFloat()
        withTransform({
            translate(x - shapeSize / 2f, y - shapeSize / 2f)
            rotate(angleDeg, pivot = Offset(shapeSize / 2f, shapeSize / 2f))
        }) {
            drawRoundRect(
                color = color,
                size = Size(shapeSize, shapeSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeSize * cornerFraction)
            )
        }
    }
}

private fun DrawScope.drawOrbitRingPills(
    cx: Float, cy: Float,
    orbitRadius: Float,
    baseAngleDeg: Float,
    shapeCount: Int,
    pillWidth: Float,
    pillHeight: Float,
    color: Color
) {
    val stepDeg = 360f / shapeCount
    repeat(shapeCount) { i ->
        val angleDeg = baseAngleDeg + stepDeg * i
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val x = cx + orbitRadius * cos(angleRad).toFloat()
        val y = cy + orbitRadius * sin(angleRad).toFloat()
        withTransform({
            translate(x - pillWidth / 2f, y - pillHeight / 2f)
            rotate(angleDeg + 90f, pivot = Offset(pillWidth / 2f, pillHeight / 2f))
        }) {
            drawRoundRect(
                color = color,
                size = Size(pillWidth, pillHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillHeight / 2f)
            )
        }
    }
}