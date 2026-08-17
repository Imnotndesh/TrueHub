package com.imnotndesh.truehub.data.helpers

import android.content.Context

/**
 * Public logging facade retained for compatibility with all existing call sites.
 *
 * Under the hood it delegates to [InternalLogger], which records into a bounded
 * in-memory ring buffer and (optionally) a rotating local file. The enable flag is now
 * a real, persisted toggle (see [LoggingPrefs] and the "App Logging" settings entry)
 * instead of the previous ad-hoc `enableDebugLogging` constant.
 */
object TrueHubLogger {

    /** Legacy mutable flag kept for source compatibility; read is overridden by [InternalLogger]. */
    var isLoggingEnabled: Boolean
        get() = InternalLogger.isLoggingEnabled()
        set(value) { /* no-op writes; use LoggingPrefs/InternalLogger */ }

    /** Initialise the backing logger with an app Context (call early, e.g. Application.onCreate). */
    fun initialize(context: Context) = InternalLogger.initialize(context)

    /** Persist + apply the logging enable/disable toggle. */
    fun setLoggingEnabled(context: Context, enabled: Boolean) =
        InternalLogger.setLoggingEnabled(context, enabled)

    fun d(tag: String = APP_TAG, message: String) = InternalLogger.d(tag, message)
    fun i(tag: String = APP_TAG, message: String) = InternalLogger.i(tag, message)
    fun w(tag: String = APP_TAG, message: String) = InternalLogger.w(tag, message)
    fun e(tag: String = APP_TAG, message: String, throwable: Throwable? = null) =
        InternalLogger.e(tag, message, throwable)

    private const val APP_TAG = "TrueHub"
}
