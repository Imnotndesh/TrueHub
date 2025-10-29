package com.example.truehub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TrueHubAppTheme(
    theme: AppTheme = AppTheme.TRUEHUB,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.DYNAMIC -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        AppTheme.TRUEHUB -> if (darkTheme) TrueHubDarkColors else TrueHubLightColors
        AppTheme.OCEAN -> if (darkTheme) OceanDarkColors else OceanLightColors
        AppTheme.FOREST -> if (darkTheme) ForestDarkColors else ForestLightColors
        AppTheme.SUNSET -> if (darkTheme) SunsetDarkColors else SunsetLightColors
        AppTheme.LAVENDER -> if (darkTheme) LavenderDarkColors else LavenderLightColors
        AppTheme.MONOCHROME -> if (darkTheme) MonochromeDarkColors else MonochromeLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}