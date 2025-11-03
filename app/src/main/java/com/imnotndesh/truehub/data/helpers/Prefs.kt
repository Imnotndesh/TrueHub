package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.core.content.edit
import com.imnotndesh.truehub.ui.theme.AppTheme

object Prefs {
    private const val PREFS_NAME = "truehub_prefs"
    private const val SERVER_URL = "server_url"
    private const val SERVER_INSECURE = "insecure"
    private const val APP_THEME = "app_theme"

    fun save(context: Context, serverUrl: String, insecure: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(SERVER_URL, serverUrl)
                putBoolean(SERVER_INSECURE, insecure)
            }
    }

    fun load(context: Context): Pair<String?, Boolean> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SERVER_URL, null) to prefs.getBoolean(SERVER_INSECURE, false)
    }

    fun saveTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(APP_THEME, theme.name)
            }
    }

    fun loadTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeName = prefs.getString(APP_THEME, AppTheme.TRUEHUB.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.TRUEHUB.name)
        } catch (e: IllegalArgumentException) {
            AppTheme.TRUEHUB
        }
    }
}