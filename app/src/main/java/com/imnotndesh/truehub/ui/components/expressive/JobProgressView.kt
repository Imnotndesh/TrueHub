package com.imnotndesh.truehub.ui.components.expressive

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.ui.components.ServerRackAnimation

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JobProgressView(
    title: String,
    description: String?,
    progress: Int,
    modifier: Modifier = Modifier,
    isFailed: Boolean = false,
    indeterminate: Boolean = false,
    rackSize: Float = 320f,
    actions: (@Composable () -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0, 100).toFloat() / 100f,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>(),
        label = "jobProgress"
    )

    Box(
        modifier = modifier
            .padding(16.dp)
            .semantics {
                liveRegion = if (isFailed) LiveRegionMode.Assertive else LiveRegionMode.Polite
                paneTitle = title
            }
    ) {
        ServerRackAnimation(
            size = rackSize,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                label = "jobProgressTitle"
            ) { currentTitle ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (isFailed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.semantics {
                            liveRegion = if (isFailed) LiveRegionMode.Assertive else LiveRegionMode.Polite
                        }
                    )
                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (indeterminate) {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            } else {
                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics {
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = animatedProgress,
                                range = 0f..1f
                            )
                        },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.coerceIn(0, 100)}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            actions?.let {
                Spacer(modifier = Modifier.height(40.dp))
                it()
            }
        }
    }
}
