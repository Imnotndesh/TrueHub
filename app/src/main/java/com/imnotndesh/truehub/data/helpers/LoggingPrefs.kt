package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.core.content.edit

/**
 * Persistent state for the internal logging tool.
 *
 * [isEnabled] mirrors the old ad-hoc `enableDebugLogging` flag but is now a real,
 * user-controllable toggle that survives app restarts.
 * [isVisible] is the easter-egg reveal flag: it only flips when the user taps the
 * "Performance Tracking" card in the Features (About) section 5 times, and is never
 * reset on app launch.
 */
enum class LogFormat(val id: String) {
    CSV("csv"),
    TXT("txt"),
    JSON("json")
}

object LoggingPrefs {

    private const val PREFS_NAME = "truehub_logging"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_VISIBLE = "visible"
    private const val KEY_FORMAT = "format"
    private const val KEY_FILE_SINK = "file_sink"
    private const val KEY_LOGCAT_SINK = "logcat_sink"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_ENABLED, enabled) }
    }

    /** Whether the hidden "App Logging" entry is currently revealed. Never auto-resets. */
    fun isVisible(context: Context): Boolean = prefs(context).getBoolean(KEY_VISIBLE, false)

    fun setVisible(context: Context, visible: Boolean) {
        prefs(context).edit { putBoolean(KEY_VISIBLE, visible) }
    }

    fun format(context: Context): LogFormat {
        val name = prefs(context).getString(KEY_FORMAT, LogFormat.CSV.id) ?: LogFormat.CSV.id
        return runCatching { LogFormat.valueOf(name.uppercase()) }.getOrDefault(LogFormat.CSV)
    }

    fun setFormat(context: Context, format: LogFormat) {
        prefs(context).edit { putString(KEY_FORMAT, format.id) }
    }

    /** Whether to write log lines to a local file. */
    fun isFileSinkEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_FILE_SINK, false)

    fun setFileSinkEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_FILE_SINK, enabled) }
    }

    /** Whether to print log lines to Logcat. Defaults to true (the classic developer behaviour). */
    fun isLogcatEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGCAT_SINK, true)

    fun setLogcatEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_LOGCAT_SINK, enabled) }
    }
}
