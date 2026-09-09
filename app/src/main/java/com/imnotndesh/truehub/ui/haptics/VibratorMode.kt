package com.imnotndesh.truehub.ui.haptics

/** Whether a mode reads as positive, negative, or neutral feedback. */
enum class Polarity { POSITIVE, NEGATIVE, NEUTRAL }

/** How strongly a mode asserts itself (used to curb energy creep over time). */
enum class Severity { SUBTLE, NORMAL, STRONG }

/** Design tag carried by every mode for audit and future per-severity settings. */
data class Profile(val polarity: Polarity, val severity: Severity)

/** One motor-ON event: vibrate at [amplitude] (0..255) for [onMs], then silence for [gapMs]. */
data class HapticPulse(val onMs: Long, val amplitude: Int, val gapMs: Long = 0L)

/** Equal-length OFF/ON-alternating arrays ready for `VibrationEffect.createWaveform`. */
data class WaveformSpec(val timings: LongArray, val amplitudes: IntArray, val repeat: Int = -1) {
    override fun equals(other: Any?): Boolean =
        other is WaveformSpec &&
            timings.contentEquals(other.timings) &&
            amplitudes.contentEquals(other.amplitudes) &&
            repeat == other.repeat

    override fun hashCode(): Int {
        var result = timings.contentHashCode()
        result = 31 * result + amplitudes.contentHashCode()
        result = 31 * result + repeat
        return result
    }
}

/**
 * Named, reusable vibration modes for TrueHub.
 *
 * Every mode is a one-shot (`repeatFrom = -1`) except [PROGRESS_HEARTBEAT], which repeats and
 * must be cancelled by the caller. All one-shots total well under ~350ms so nothing buzzes long.
 */
enum class VibratorMode(
    val pulses: List<HapticPulse>,
    val repeatFrom: Int = -1,
    val profile: Profile
) {
    // A pen drawing "✓": firm down-stroke, brief dip at the corner, crisp bright up-tick.
    SUCCESS_TICK(
        pulses = listOf(
            HapticPulse(onMs = 40, amplitude = 200, gapMs = 20),
            HapticPulse(onMs = 30, amplitude = 95, gapMs = 20),
            HapticPulse(onMs = 55, amplitude = 255, gapMs = 0)
        ),
        profile = Profile(Polarity.POSITIVE, Severity.NORMAL)
    ),

    // "No" — three quick, sharp, tightly-spaced bursts. Equal amplitudes keep it staccato, not a siren.
    ERROR_ALERT(
        pulses = listOf(
            HapticPulse(onMs = 55, amplitude = 220, gapMs = 80),
            HapticPulse(onMs = 55, amplitude = 220, gapMs = 80),
            HapticPulse(onMs = 55, amplitude = 220, gapMs = 0)
        ),
        profile = Profile(Polarity.NEGATIVE, Severity.STRONG)
    ),

    // One slow soft "heads up", gentler than ERROR and ramped so it never clacks.
    WARNING_SOFT(
        pulses = listOf(
            HapticPulse(onMs = 25, amplitude = 60, gapMs = 20),
            HapticPulse(onMs = 35, amplitude = 110, gapMs = 20),
            HapticPulse(onMs = 40, amplitude = 170, gapMs = 20),
            HapticPulse(onMs = 40, amplitude = 130, gapMs = 20),
            HapticPulse(onMs = 30, amplitude = 80, gapMs = 0)
        ),
        profile = Profile(Polarity.NEGATIVE, Severity.NORMAL)
    ),

    // "Still alive" during a long live operation; one low-amplitude poke on a slow cadence.
    PROGRESS_HEARTBEAT(
        pulses = listOf(
            HapticPulse(onMs = 28, amplitude = 70, gapMs = 1800)
        ),
        repeatFrom = 0,
        profile = Profile(Polarity.NEUTRAL, Severity.SUBTLE)
    ),

    // "Registered" — a single short bright tap for confirmations / selection.
    CONFIRM_TAP(
        pulses = listOf(
            HapticPulse(onMs = 15, amplitude = 160, gapMs = 0)
        ),
        profile = Profile(Polarity.NEUTRAL, Severity.SUBTLE)
    ),

    // Subtle "you have arrived" marker; unwired in v1, kept for later navigation adoption.
    NAVIGATION_POP(
        pulses = listOf(
            HapticPulse(onMs = 20, amplitude = 110, gapMs = 0)
        ),
        profile = Profile(Polarity.NEUTRAL, Severity.SUBTLE)
    );

    val isRepeating: Boolean get() = repeatFrom >= 0
}

/** Convenience: this mode's resolved waveform spec. */
fun VibratorMode.toWaveformSpec(): WaveformSpec = buildWaveform(pulses, repeatFrom)

/**
 * Turns pulse events into canonical [WaveformSpec]: a leading 0ms OFF (start immediately), then
 * alternating ON/OFF, amplitude 0 on every OFF slot, ending on a trailing 0ms OFF.
 */
fun buildWaveform(pulses: List<HapticPulse>, repeatFrom: Int = -1): WaveformSpec {
    require(pulses.isNotEmpty()) { "pulses must not be empty" }
    val timings = java.util.ArrayList<Long>(2 + 2 * pulses.size)
    val amplitudes = java.util.ArrayList<Int>(2 + 2 * pulses.size)
    timings.add(0L); amplitudes.add(0)
    for (pulse in pulses) {
        require(pulse.onMs > 0) { "onMs must be > 0" }
        require(pulse.amplitude in 0..255) { "amplitude must be within 0..255" }
        timings.add(pulse.onMs); amplitudes.add(pulse.amplitude)
        timings.add(pulse.gapMs); amplitudes.add(0)
    }
    return WaveformSpec(timings.toLongArray(), amplitudes.toIntArray(), repeatFrom)
}
