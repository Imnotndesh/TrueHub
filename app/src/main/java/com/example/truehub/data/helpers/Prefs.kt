package com.example.truehub.data.helpers

import android.content.Context
import androidx.core.content.edit

object Prefs {
    private const val PREFS_NAME = "truehub_prefs"
    private const val SERVER_URL = "server_url"
    private const val SERVER_INSECURE = "insecure"

    fun save(context: Context, serverUrl: String, insecure: Boolean){
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
}