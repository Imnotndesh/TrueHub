package com.imnotndesh.truehub.ui.adaptive

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Standard confirmation (default AlertDialog width is already within the 560dp cap). */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Compact: bottom sheet (Material default max width 640dp). Medium and up: centered dialog capped at
 * [AdaptiveTokens.DialogMaxWidth], scrollable, Esc/outside click dismisses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveSheetOrDialog(
    onDismiss: () -> Unit,
    title: String,
    window: TrueHubWindow = LocalTrueHubWindow.current,
    maxHeightFraction: Float = 0.9f,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (window.isCompactWidth) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                content()
            }
        }
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .widthIn(max = AdaptiveTokens.DialogMaxWidth)
                        .heightIn(max = maxHeight * maxHeightFraction)
                        .pointerInput(Unit) { detectTapGestures { } },
                ) {
                    Column(
                        Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                    ) {
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        content()
                    }
                }
            }
        }
    }
}
