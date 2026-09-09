package com.imnotndesh.truehub.ui.haptics

import android.content.ContentResolver
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Single correctness wrapper around the app-authored vibrator channel.
 *
 * Gates, in order: in-app self setting → OS [Settings.System.HAPTIC_FEEDBACK_ENABLED] →
 * [Vibrator.hasVibrator]. Direct aperture `vibrate()` bypasses the OS haptics preference, so
 * that gate is mandatory here (unlike Compose one-shot `performHapticFeedback`).
 */
class VibrationFeedback(context: Context) {

    private val appContext: Context = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver

    private val vibrator: Vibrator? = runCatching {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    }.getOrNull()

    /** In-app override (a future Settings toggle), default ON. Mirrors the system pref. */
    private val selfEnabled = AtomicBoolean(true)

    /** OS device-tuned one-shot effects preferred per mode when the device declares support. */
    private val predefinedByMode: Map<VibratorMode, Int> =
        mapOf(
            VibratorMode.CONFIRM_TAP to VibrationEffect.EFFECT_CLICK,
            VibratorMode.NAVIGATION_POP to VibrationEffect.EFFECT_TICK
        )

    fun setSelfEnabled(enabled: Boolean) {
        selfEnabled.set(enabled)
    }

    val isHardwareAvailable: Boolean get() = vibrator?.hasVibrator() == true

    /** True iff the in-app setting and the OS haptics toggle both allow vibration. */
    fun isEffectivelyEnabled(): Boolean =
        selfEnabled.get() &&
            runCatching {
                Settings.System.getInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
            }.getOrDefault(true)

    /**
     * Plays [mode] once as a silent no-op when unavailable or disabled. Returns true iff a
     * vibration was actually issued (for logging/test assertions).
     */
    fun play(mode: VibratorMode): Boolean {
        val effect = resolveEffect(mode) ?: return false
        return vibrateOrNull(effect)
    }

    /** Starts a repeating mode such as [VibratorMode.PROGRESS_HEARTBEAT]; pair with [cancel]. */
    fun startRepeating(mode: VibratorMode): Boolean {
        check(mode.isRepeating) { "startRepeating requires a repeating mode, got $mode" }
        return play(mode)
    }

    /** Stops any running pattern. Call on screen dispose / completion / before a different cadence. */
    fun cancel() {
        vibrator?.cancel()
    }

    private fun resolveEffect(mode: VibratorMode): VibrationEffect? {
        if (!isEffectivelyEnabled()) return null
        val target = vibrator ?: return null
        if (!target.hasVibrator()) return null

        predefinedByMode[mode]?.let { predefined ->
            val supported = runCatching {
                target.areEffectsSupported(predefined).firstOrNull() == 1
            }.getOrDefault(false)
            if (supported) return VibrationEffect.createPredefined(predefined)
        }
        val spec = mode.toWaveformSpec()
        return VibrationEffect.createWaveform(spec.timings, spec.amplitudes, spec.repeat)
    }

    private fun vibrateOrNull(effect: VibrationEffect): Boolean =
        runCatching { vibrator?.vibrate(effect); true }.getOrDefault(false)
}

/** Compose-friendly construction; cheap, survives recomposition, needs no lifecycle. */
@Composable
fun rememberVibrationFeedback(): VibrationFeedback {
    val context = LocalContext.current
    return remember(context) { VibrationFeedback(context) }
}
