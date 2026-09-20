package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.imnotndesh.truehub.data.models.UpdateInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val UPDATE_DATASTORE = "app_update"
val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = UPDATE_DATASTORE)

object UpdatePrefs {

    private val LAST_CHECKED = longPreferencesKey("last_checked_at")
    private val LAST_NOTIFIED = stringPreferencesKey("last_notified_version")
    private val DISMISSED = stringPreferencesKey("dismissed_version")
    private val CACHED_LATEST = stringPreferencesKey("cached_latest_json")
    private val AUTO_CHECK = booleanPreferencesKey("auto_check_enabled")
    private val WIFI_ONLY = booleanPreferencesKey("wifi_only")
    private val GITHUB_TOKEN = stringPreferencesKey("github_token")
    private val READY_VERSION = stringPreferencesKey("ready_version")
    private val READY_APK_PATH = stringPreferencesKey("ready_apk_path")

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val infoAdapter = moshi.adapter(UpdateInfo::class.java)

    fun latestFlow(context: Context): Flow<UpdateInfo?> =
        context.updateDataStore.data
            .map { decode(it[CACHED_LATEST]) }
            .catch { emit(null) }

    suspend fun getLatest(context: Context): UpdateInfo? =
        decode(context.updateDataStore.data.first()[CACHED_LATEST])

    suspend fun saveLatest(context: Context, info: UpdateInfo?) {
        context.updateDataStore.edit { prefs ->
            if (info == null) prefs.remove(CACHED_LATEST) else prefs[CACHED_LATEST] = infoAdapter.toJson(info)
        }
    }

    fun autoCheckFlow(context: Context): Flow<Boolean> =
        context.updateDataStore.data
            .map { it[AUTO_CHECK] ?: true }
            .catch { emit(true) }

    suspend fun isAutoCheckEnabled(context: Context): Boolean =
        context.updateDataStore.data.first()[AUTO_CHECK] ?: true

    suspend fun setAutoCheckEnabled(context: Context, enabled: Boolean) {
        context.updateDataStore.edit { it[AUTO_CHECK] = enabled }
    }

    fun wifiOnlyFlow(context: Context): Flow<Boolean> =
        context.updateDataStore.data
            .map { it[WIFI_ONLY] ?: false }
            .catch { emit(false) }

    suspend fun isWifiOnly(context: Context): Boolean =
        context.updateDataStore.data.first()[WIFI_ONLY] ?: false

    suspend fun setWifiOnly(context: Context, enabled: Boolean) {
        context.updateDataStore.edit { it[WIFI_ONLY] = enabled }
    }

    suspend fun getLastChecked(context: Context): Long =
        context.updateDataStore.data.first()[LAST_CHECKED] ?: 0L

    suspend fun setLastChecked(context: Context, epochMillis: Long) {
        context.updateDataStore.edit { it[LAST_CHECKED] = epochMillis }
    }

    suspend fun getLastNotifiedVersion(context: Context): String? =
        context.updateDataStore.data.first()[LAST_NOTIFIED]

    suspend fun setLastNotifiedVersion(context: Context, version: String?) {
        context.updateDataStore.edit { prefs ->
            if (version.isNullOrBlank()) prefs.remove(LAST_NOTIFIED) else prefs[LAST_NOTIFIED] = version
        }
    }

    suspend fun getDismissedVersion(context: Context): String? =
        context.updateDataStore.data.first()[DISMISSED]

    suspend fun setDismissedVersion(context: Context, version: String?) {
        context.updateDataStore.edit { prefs ->
            if (version.isNullOrBlank()) prefs.remove(DISMISSED) else prefs[DISMISSED] = version
        }
    }

    suspend fun getToken(context: Context): String? =
        context.updateDataStore.data.first()[GITHUB_TOKEN]

    suspend fun setToken(context: Context, token: String?) {
        context.updateDataStore.edit { prefs ->
            if (token.isNullOrBlank()) prefs.remove(GITHUB_TOKEN) else prefs[GITHUB_TOKEN] = token
        }
    }

    suspend fun getReadyVersion(context: Context): String? =
        context.updateDataStore.data.first()[READY_VERSION]

    suspend fun getReadyApkPath(context: Context): String? =
        context.updateDataStore.data.first()[READY_APK_PATH]

    suspend fun setReady(context: Context, version: String, apkPath: String) {
        context.updateDataStore.edit { prefs ->
            prefs[READY_VERSION] = version
            prefs[READY_APK_PATH] = apkPath
        }
    }

    suspend fun clearReady(context: Context) {
        context.updateDataStore.edit { prefs ->
            prefs.remove(READY_VERSION)
            prefs.remove(READY_APK_PATH)
        }
    }

    private fun decode(json: String?): UpdateInfo? {
        if (json.isNullOrBlank()) return null
        return runCatching { infoAdapter.fromJson(json) }.getOrNull()
    }
}
