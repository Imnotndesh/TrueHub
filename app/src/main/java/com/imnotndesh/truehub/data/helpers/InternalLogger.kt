package com.imnotndesh.truehub.data.helpers

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.Volatile

/**
 * Efficient internal logging engine backing [TrueHubLogger].
 *
 * Design goals:
 *  - **Bounded memory**: an [ArrayDeque] ring buffer capped at [BUFFER_CAPACITY] entries so
 *    performance-monitoring logs can't balloon memory.
 *  - **Non-blocking writes**: all file IO runs on a dedicated single-thread executor, so logging
 *    never janks the UI / worker threads.
 *  - **Rotating local file sink**: appends to capped rolling files under `filesDir/logs/`.
 *  - **Persistent + live state**: enable/disable and format are read from [LoggingPrefs] each call,
 *    so toggling in the UI takes effect immediately without a restart.
 *
 * Values are snapshot cheaply: [enabled] is a volatile flag updated when the pref changes.
 */
object InternalLogger {

    private const val APP_TAG = "TrueHub"
    private const val BUFFER_CAPACITY = 2000
    private const val FILE_CAPACITY_LINES = 2000
    private const val MAX_FILES = 10

    /** Format of a single ring-buffer entry. */
    data class Entry(
        val timeMillis: Long,
        val level: Char,   // V/D/I/W/E
        val tag: String,
        val message: String,
        val throwable: String? = null
    ) {
        fun toCsvLine(): String =
            "\"${formatTime(timeMillis)}\",\"$level\",\"${escape(tag)}\",\"${escape(message)}\",\"${escape(throwable ?: "")}\""

        fun toJson(): JSONObject = JSONObject().apply {
            put("time", formatTime(timeMillis))
            put("level", level.toString())
            put("tag", tag)
            put("message", message)
            if (throwable != null) put("throwable", throwable)
        }
    }

    private val buffer = ArrayDeque<Entry>(BUFFER_CAPACITY)
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "TrueHub-LogWriter").apply { isDaemon = true }
    }

    @Volatile private var enabled = false
    @Volatile private var fileSinkEnabled = false

    private val logDir: AtomicReference<File?> = AtomicReference(null)
    @Volatile private var appContext: Context? = null

    /** Must be called once with a Context early in the app lifecycle (e.g. Application/init). */
    fun initialize(context: Context) {
        val app = context.applicationContext
        appContext = app
        enabled = LoggingPrefs.isEnabled(app)
        fileSinkEnabled = LoggingPrefs.isFileSinkEnabled(app)
        logDir.set(File(app.filesDir, "logs").apply { mkdirs() })
    }

    fun isLoggingEnabled(): Boolean = enabled

    fun setLoggingEnabled(context: Context, on: Boolean) {
        enabled = on
        LoggingPrefs.setEnabled(context, on)
    }

    fun setFileSinkEnabled(context: Context, on: Boolean) {
        fileSinkEnabled = on
        LoggingPrefs.setFileSinkEnabled(context, on)
    }

    fun isFileSinkEnabled(): Boolean = fileSinkEnabled

    // ── Logging API (mirrors TrueHubLogger so call sites stay the same) ──
    fun d(tag: String, message: String) = log('D', tag, message, null)
    fun i(tag: String, message: String) = log('I', tag, message, null)
    fun w(tag: String, message: String) = log('W', tag, message, null)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log('E', tag, message, throwable)

    private fun log(level: Char, tag: String, message: String, throwable: Throwable?) {
        if (!enabled) return
        val entry = Entry(System.currentTimeMillis(), level, tag, message, throwable?.stackTraceToStringSafe())
        synchronized(buffer) {
            if (buffer.size >= BUFFER_CAPACITY) buffer.removeFirst()
            buffer.addLast(entry)
        }
        // Logcat path (cheap, direct).
        when (level) {
            'D' -> Log.d(tag, message, throwable)
            'I' -> Log.i(tag, message, throwable)
            'W' -> Log.w(tag, message, throwable)
            'E' -> Log.e(tag, message, throwable)
        }
        // Async file path.
        if (fileSinkEnabled) executor.execute { appendToFile(entry) }
    }

    // ── Ring buffer reads ──
    /** Snapshot of the in-memory log (newest last). Safe to call from any thread. */
    fun snapshot(): List<Entry> = synchronized(buffer) { buffer.toList() }

    fun clearBuffer() = synchronized(buffer) { buffer.clear() }

    // ── File persistence (rotating) ──
    private fun appendToFile(entry: Entry) {
        val dir = logDir.get() ?: return
        val current = currentFile(dir) ?: return
        current.appendText(lineFor(entry) + "\n")
        rotateIfNeeded(dir, current)
    }

    private fun lineFor(entry: Entry): String = when (currentFormat()) {
        LogFormat.CSV -> entry.toCsvLine()
        LogFormat.JSON -> entry.toJson().toString()
        LogFormat.TXT -> "${formatTime(entry.timeMillis)} ${entry.level} ${entry.tag}: ${entry.message}${entry.throwable?.let { "\n  $it" } ?: ""}"
    }

    private fun currentFormat(): LogFormat =
        LoggingPrefs.format(appContext ?: return LogFormat.CSV)

    private fun currentFile(dir: File): File? {
        val last = dir.listFiles { f -> f.extension == "log" }?.maxByOrNull { it.lastModified() }
        return last?.takeIf { it.readLines().orEmpty().size < FILE_CAPACITY_LINES }
            ?: newLogFile(dir)
    }

    private fun newLogFile(dir: File): File {
        val index = (dir.listFiles { f -> f.extension == "log" }?.size ?: 0) % MAX_FILES
        return File(dir, "app_$index.log")
    }

    private fun rotateIfNeeded(dir: File, file: File) {
        if (file.readLines().orEmpty().size > FILE_CAPACITY_LINES) {
            // Truncate and let the next append start a new file; drop oldest if too many.
            file.writeText("")
            val logs = dir.listFiles { f -> f.extension == "log" }.orEmpty()
            if (logs.size > MAX_FILES) logs.sortedByDescending { it.lastModified() }
                .drop(MAX_FILES).forEach { it.delete() }
        }
    }

    fun logFiles(dir: File? = null): List<File> =
        (dir ?: logDir.get())?.listFiles { f -> f.extension == "log" }?.sortedBy { it.lastModified() }.orEmpty()

    fun clearFiles(dir: File? = null) {
        (dir ?: logDir.get())?.listFiles { f -> f.extension == "log" }?.forEach { it.delete() }
    }

    /**
     * Export the current ring buffer (or files if present) to a shareable [File] in the
     * requested [LogFormat]. Returns the file, or null if there is nothing to export.
     */
    fun export(context: Context, format: LogFormat): File? {
        val entries = snapshot()
        if (entries.isEmpty() && logFiles().isEmpty()) return null
        val dir = logDir.get() ?: return null
        val out = File(dir, "truehub_export.${format.id}")
        val sb = StringBuilder()
        if (format == LogFormat.CSV) sb.append("\"timestamp\",\"level\",\"tag\",\"message\",\"throwable\"\n")
        // Prefer persisted files if the file sink was on; otherwise use the buffer.
        val files = logFiles().takeIf { it.isNotEmpty() }
        files?.let { fl ->
            fl.forEach { f -> f.forEachLine { sb.append(it).append('\n') } }
        } ?: entries.forEach { e ->
            sb.append(when (format) {
                LogFormat.CSV -> e.toCsvLine()
                LogFormat.JSON -> e.toJson().toString()
                LogFormat.TXT -> "${formatTime(e.timeMillis)} ${e.level} ${e.tag}: ${e.message}${e.throwable?.let { "\n  $it" } ?: ""}"
            }).append('\n')
        }
        out.writeText(sb.toString())
        return out
    }

    // ── Helpers ──
    private fun escape(s: String): String = s.replace("\"", "\"\"").replace("\n", " ")

    private fun formatTime(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(millis))

    private fun Throwable.stackTraceToStringSafe(): String =
        try { stackTraceToString() } catch (_: Throwable) { message ?: "Unknown error" }
}
