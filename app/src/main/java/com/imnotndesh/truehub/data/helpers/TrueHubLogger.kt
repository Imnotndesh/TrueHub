package com.imnotndesh.truehub.data.helpers

import android.util.Log

object TrueHubLogger {
    var isLoggingEnabled: Boolean = false

    private const val APP_TAG = "TrueHub"

    fun d(tag: String = APP_TAG, message: String) {
        if (isLoggingEnabled) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String = APP_TAG, message: String, throwable: Throwable? = null) {
        if (isLoggingEnabled) {
            Log.e(tag, message, throwable)
        }
    }

    fun i(tag: String = APP_TAG, message: String) {
        if (isLoggingEnabled) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String = APP_TAG, message: String) {
        if (isLoggingEnabled) {
            Log.w(tag, message)
        }
    }
}