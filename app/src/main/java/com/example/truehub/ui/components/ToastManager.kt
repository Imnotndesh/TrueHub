package com.example.truehub.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

data class ToastData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000L,
    val actionText: String? = null,
    val onActionClick: (() -> Unit)? = null
)

object ToastManager {
    var currentToast by mutableStateOf<ToastData?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    fun showToast(
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Long = 3000L,
        actionText: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        val toast = ToastData(
            message = message,
            type = type,
            duration = duration,
            actionText = actionText,
            onActionClick = onActionClick
        )

        currentToast = toast

        scope.launch {
            delay(duration)
            if (currentToast?.id == toast.id) {
                currentToast = null
            }
        }
    }

    fun dismissToast() {
        currentToast = null
    }

    // Convenience methods
    fun showSuccess(message: String, duration: Long = 3000L) {
        showToast(message, ToastType.SUCCESS, duration)
    }

    fun showError(message: String, duration: Long = 4000L) {
        showToast(message, ToastType.ERROR, duration)
    }

    fun showWarning(message: String, duration: Long = 3500L) {
        showToast(message, ToastType.WARNING, duration)
    }

    fun showInfo(message: String, duration: Long = 3000L) {
        showToast(message, ToastType.INFO, duration)
    }
}