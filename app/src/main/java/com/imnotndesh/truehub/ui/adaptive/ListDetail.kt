package com.imnotndesh.truehub.ui.adaptive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Canonical list → detail split view.
 *
 * On compact windows (or when [showDetail] is false) the list takes the full width. On expanded
 * windows the list keeps [listPaneWeight] and the detail pane animates in on the trailing side.
 * Screens own the selection state; render the detail pane with the selected id.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
fun ListDetailLayout(
    showDetail: Boolean,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    listPaneWeight: Float = 0.55f,
    detailPaneWeight: Float = 0.45f
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (showDetail) listPaneWeight else 1f)
                .fillMaxHeight()
        ) {
            list()
        }
        AnimatedVisibility(
            visible = showDetail,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ) + fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            Box(
                modifier = Modifier
                    .weight(detailPaneWeight)
                    .fillMaxHeight()
            ) {
                detail()
            }
        }
    }
}
