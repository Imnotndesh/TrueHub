package com.imnotndesh.truehub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

/**
 * A Composable that observes the current toast state from [ToastManager] and displays it.
 *
 * This component is responsible for showing and hiding the toast with a slide-in/slide-out animation.
 * It acts as a host for the [ModernToast] Composable, which renders the actual toast content.
 * It should be placed at a high level in the UI hierarchy, typically within your app's main
 * Scaffold, to ensure toasts can be displayed over any screen.
 *
 * The `AnimatedVisibility` handles the appearance and disappearance of the toast.
 * - The `enter` animation is a `slideInVertically` with a bouncy spring effect.
 * - The `exit` animation is a simple `slideOutVertically`.
 *
 * When a toast is shown, this Composable passes the toast data and a dismiss callback to
 * the `ModernToast` component.
 *
 * @see ToastManager
 * @see ModernToast
 */
@Composable
fun ModernToastHost() {
    val currentToast = ToastManager.currentToast

    AnimatedVisibility(
        visible = currentToast != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        )
    ) {
        currentToast?.let { toast ->
            ModernToast(
                toast = toast,
                onDismiss = { ToastManager.dismissToast() }
            )
        }
    }
}

@Composable
private fun ModernToast(
    toast: ToastData,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(toast) {
        isVisible = true
    }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f)
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = getToastIcon(toast.type),
                    contentDescription = null,
                    tint = getToastColor(toast.type),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getToastTitle(toast.type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = toast.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Action button if provided
                    toast.actionText?.let { actionText ->
                        TextButton(
                            onClick = {
                                toast.onActionClick?.invoke()
                                onDismiss()
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = actionText,
                                style = MaterialTheme.typography.labelMedium,
                                color = getToastColor(toast.type),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
}

@Composable
private fun getToastColor(type: ToastType): Color {
    return when (type) {
        ToastType.SUCCESS -> Color(0xFF2E7D32)
        ToastType.ERROR -> Color(0xFFD32F2F)
        ToastType.WARNING -> Color(0xFFF57C00)
        ToastType.INFO -> Color(0xFF1976D2)
    }
}

private fun getToastIcon(type: ToastType): ImageVector {
    return when (type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR -> Icons.Default.Error
        ToastType.WARNING -> Icons.Default.Warning
        ToastType.INFO -> Icons.Default.Info
    }
}

private fun getToastTitle(type: ToastType): String {
    return when (type) {
        ToastType.SUCCESS -> "Success"
        ToastType.ERROR -> "Error"
        ToastType.WARNING -> "Warning"
        ToastType.INFO -> "Information"
    }
}

fun ViewModel.showToast(
    message: String,
    type: ToastType = ToastType.INFO,
    duration: Long = 3000L,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    viewModelScope.launch {
        ToastManager.showToast(message, type, duration, actionText, onActionClick)
    }
}

fun ViewModel.showSuccess(message: String, duration: Long = 3000L) {
    viewModelScope.launch {
        ToastManager.showSuccess(message, duration)
    }
}

fun ViewModel.showError(message: String, duration: Long = 4000L) {
    viewModelScope.launch {
        ToastManager.showError(message, duration)
    }
}

fun ViewModel.showWarning(message: String, duration: Long = 3500L) {
    viewModelScope.launch {
        ToastManager.showWarning(message, duration)
    }
}

fun ViewModel.showInfo(message: String, duration: Long = 3000L) {
    viewModelScope.launch {
        ToastManager.showInfo(message, duration)
    }
}