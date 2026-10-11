package com.imnotndesh.truehub.data.models

data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val stage: Int = STAGE_STABLE,
    val prerelease: String? = null,
    val prereleaseNumber: Int = 0
) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        if (major != other.major) return major - other.major
        if (minor != other.minor) return minor - other.minor
        if (patch != other.patch) return patch - other.patch
        if (stage != other.stage) return stage - other.stage
        return prereleaseNumber - other.prereleaseNumber
    }

    fun toVersionCode(): Int =
        (major * 100 + minor) * 10_000 + patch * 100 + stage * 10 + prereleaseNumber

    companion object {
        const val STAGE_ALPHA = 0
        const val STAGE_BETA = 1
        const val STAGE_RC = 2
        const val STAGE_STABLE = 3

        private val PATTERN =
            Regex("""[vV]?(\d+)\.(\d+)\.(\d+)(?:[-_ ]?([A-Za-z]+)\.?(\d+)?)?""")

        // Only explicit rc/beta tags rank below a release; "Alpha" is used as a
        // released channel label in this project, so it sorts with stable builds.
        private val STAGE_RANKS = mapOf(
            "beta" to STAGE_BETA,
            "rc" to STAGE_RC
        )

        fun parse(raw: String?): AppVersion? {
            val match = PATTERN.find(raw?.trim().orEmpty()) ?: return null
            val label = match.groupValues[4].lowercase().ifBlank { null }
            return AppVersion(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].toInt(),
                stage = label?.let { STAGE_RANKS[it] } ?: STAGE_STABLE,
                prerelease = label,
                prereleaseNumber = match.groupValues[5].toIntOrNull() ?: 0
            )
        }
    }
}
