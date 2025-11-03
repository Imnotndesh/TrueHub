package com.imnotndesh.truehub.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val description: String) {
    DYNAMIC("Dynamic", "Adapts to your system colors"),
    TRUEHUB("TrueHub", "Classic blue and teal"),
    OCEAN("Ocean", "Deep blue waters"),
    FOREST("Forest", "Natural greens"),
    SUNSET("Sunset", "Warm orange and pink"),
    LAVENDER("Lavender", "Soft purple tones"),
    MONOCHROME("Monochrome", "Elegant grayscale")
}

// TrueHub Theme (Blue/Teal)
val TrueHubLightColors = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF00696D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF6FF7FB),
    onSecondaryContainer = Color(0xFF002020)
)

val TrueHubDarkColors = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF4DD9DD),
    onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF004F51),
    onSecondaryContainer = Color(0xFF6FF7FB)
)

// Ocean Theme (Deep Blue)
val OceanLightColors = lightColorScheme(
    primary = Color(0xFF006494),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF4D5F7F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E3FF),
    onSecondaryContainer = Color(0xFF091E39)
)

val OceanDarkColors = darkColorScheme(
    primary = Color(0xFF8DCDFF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004C6F),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB7C7E7),
    onSecondary = Color(0xFF21334F),
    secondaryContainer = Color(0xFF374A67),
    onSecondaryContainer = Color(0xFFD5E3FF)
)

// Forest Theme (Green)
val ForestLightColors = lightColorScheme(
    primary = Color(0xFF3D6B3A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBEF3B4),
    onPrimaryContainer = Color(0xFF002203),
    secondary = Color(0xFF53634F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E8CE),
    onSecondaryContainer = Color(0xFF111F0F)
)

val ForestDarkColors = darkColorScheme(
    primary = Color(0xFFA3D699),
    onPrimary = Color(0xFF0C390A),
    primaryContainer = Color(0xFF245223),
    onPrimaryContainer = Color(0xFFBEF3B4),
    secondary = Color(0xFFBADBB3),
    onSecondary = Color(0xFF263423),
    secondaryContainer = Color(0xFF3C4B38),
    onSecondaryContainer = Color(0xFFD6E8CE)
)

// Sunset Theme (Orange/Pink)
val SunsetLightColors = lightColorScheme(
    primary = Color(0xFFBB3F0C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCC),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Color(0xFFB91B6A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD8E8),
    onSecondaryContainer = Color(0xFF3F0020)
)

val SunsetDarkColors = darkColorScheme(
    primary = Color(0xFFFFB599),
    onPrimary = Color(0xFF5F1900),
    primaryContainer = Color(0xFF8D2A00),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFFFB0D4),
    onSecondary = Color(0xFF640039),
    secondaryContainer = Color(0xFF8F0050),
    onSecondaryContainer = Color(0xFFFFD8E8)
)

// Lavender Theme (Purple)
val LavenderLightColors = lightColorScheme(
    primary = Color(0xFF7E4FA4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF0DBFF),
    onPrimaryContainer = Color(0xFF2D0052),
    secondary = Color(0xFF665A70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDDDF7),
    onSecondaryContainer = Color(0xFF21192A)
)

val LavenderDarkColors = darkColorScheme(
    primary = Color(0xFFDDB9FF),
    onPrimary = Color(0xFF471D6E),
    primaryContainer = Color(0xFF633689),
    onPrimaryContainer = Color(0xFFF0DBFF),
    secondary = Color(0xFFD0C1DB),
    onSecondary = Color(0xFF372D40),
    secondaryContainer = Color(0xFF4E4357),
    onSecondaryContainer = Color(0xFFEDDDF7)
)

// Monochrome Theme (Gray)
val MonochromeLightColors = lightColorScheme(
    primary = Color(0xFF5C5C5C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1E1E1),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF5D5D5D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E2E2),
    onSecondaryContainer = Color(0xFF1B1B1B)
)

val MonochromeDarkColors = darkColorScheme(
    primary = Color(0xFFC4C4C4),
    onPrimary = Color(0xFF2E2E2E),
    primaryContainer = Color(0xFF444444),
    onPrimaryContainer = Color(0xFFE1E1E1),
    secondary = Color(0xFFC5C5C5),
    onSecondary = Color(0xFF2F2F2F),
    secondaryContainer = Color(0xFF454545),
    onSecondaryContainer = Color(0xFFE2E2E2)
)