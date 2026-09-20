package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.QuickLaunchApp
import com.imnotndesh.truehub.data.models.System
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for widget-facing cached data.
 *
 * Sections
 * ─────────
 * Apps  – upgradable app list (pre-existing, unchanged)
 * Pools – full pool list fetched by [AppsRefreshWorker] alongside apps
 *
 * Both sections share the same DataStore file so one [edit] block can
 * atomically update everything in a single worker pass.
 */
object WidgetDataStore {

    private val UPGRADABLE_APPS_KEY  = stringPreferencesKey("widget_upgradable_apps_json")
    private val APPS_LAST_SYNC_KEY   = longPreferencesKey("widget_last_sync_epoch")

    private val POOLS_KEY            = stringPreferencesKey("widget_pools_json")
    private val POOLS_LAST_SYNC_KEY  = longPreferencesKey("widget_pools_last_sync_epoch")


    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val QUICK_LAUNCH_APPS_KEY = stringPreferencesKey("widget_quick_launch_apps_json")

    private val quickLaunchAdapter = moshi.adapter<List<QuickLaunchApp>>(
        Types.newParameterizedType(List::class.java, QuickLaunchApp::class.java)
    )

    fun quickLaunchAppsFlow(context: Context): Flow<List<QuickLaunchApp>> =
        context.dataStore.data
            .map { prefs ->
                val json = prefs[QUICK_LAUNCH_APPS_KEY] ?: return@map emptyList()
                runCatching { quickLaunchAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
            }
            .catch { emit(emptyList()) }

    suspend fun getQuickLaunchApps(context: Context): List<QuickLaunchApp> {
        val prefs = context.dataStore.data.first()
        val json = prefs[QUICK_LAUNCH_APPS_KEY] ?: return emptyList()
        return runCatching { quickLaunchAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    suspend fun saveQuickLaunchApps(context: Context, apps: List<QuickLaunchApp>) {
        context.dataStore.edit { prefs ->
            prefs[QUICK_LAUNCH_APPS_KEY] = quickLaunchAdapter.toJson(apps)
        }
    }

    private val appsAdapter = moshi.adapter<List<Apps.AppQueryResponse>>(
        Types.newParameterizedType(List::class.java, Apps.AppQueryResponse::class.java)
    )

    private val poolsAdapter = moshi.adapter<List<System.Pool>>(
        Types.newParameterizedType(List::class.java, System.Pool::class.java)
    )

    fun upgradableAppsFlow(context: Context): Flow<List<Apps.AppQueryResponse>> =
        context.dataStore.data
            .map { prefs ->
                val json = prefs[UPGRADABLE_APPS_KEY] ?: return@map emptyList()
                runCatching { appsAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
            }
            .catch { emit(emptyList()) }

    suspend fun saveUpgradableApps(
        context: Context,
        apps: List<Apps.AppQueryResponse>
    ) {
        val upgradable = apps.filter { it.upgrade_available }
        context.dataStore.edit { prefs ->
            prefs[UPGRADABLE_APPS_KEY] = appsAdapter.toJson(upgradable)
            prefs[APPS_LAST_SYNC_KEY]  = java.lang.System.currentTimeMillis()
        }
    }

    suspend fun getLastSyncEpoch(context: Context): Long {
        val prefs = context.dataStore.data.first()
        return prefs[APPS_LAST_SYNC_KEY] ?: 0L
    }


    /**
     * Reactive flow of all pools — consumed by [PoolsWidget] via [collectAsState].
     */
    fun poolsFlow(context: Context): Flow<List<System.Pool>> =
        context.dataStore.data
            .map { prefs ->
                val json = prefs[POOLS_KEY] ?: return@map emptyList()
                runCatching { poolsAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
            }
            .catch { emit(emptyList()) }

    /**
     * Persist the full pool list in one edit (called from the worker alongside apps).
     */
    suspend fun savePools(
        context: Context,
        pools: List<System.Pool>
    ) {
        context.dataStore.edit { prefs ->
            prefs[POOLS_KEY]           = poolsAdapter.toJson(pools)
            prefs[POOLS_LAST_SYNC_KEY] = java.lang.System.currentTimeMillis()
        }
    }

    /**
     * Combined atomic save – use this from the worker so both caches are
     * written in a single DataStore transaction.
     */
    suspend fun saveAppsAndPools(
        context: Context,
        apps: List<Apps.AppQueryResponse>,
        pools: List<System.Pool>
    ) {
        val upgradable = apps.filter { it.upgrade_available }
        val now        = java.lang.System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[UPGRADABLE_APPS_KEY]  = appsAdapter.toJson(upgradable)
            prefs[APPS_LAST_SYNC_KEY]   = now
            prefs[POOLS_KEY]            = poolsAdapter.toJson(pools)
            prefs[POOLS_LAST_SYNC_KEY]  = now
        }
    }

    suspend fun removeUpgradableApp(context: Context, appName: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[UPGRADABLE_APPS_KEY] ?: return@edit
            val current = runCatching { appsAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
            val updated = current.filterNot { it.name == appName }
            prefs[UPGRADABLE_APPS_KEY] = appsAdapter.toJson(updated)
        }
    }
    suspend fun getPoolsLastSyncEpoch(context: Context): Long {
        val prefs = context.dataStore.data.first()
        return prefs[POOLS_LAST_SYNC_KEY] ?: 0L
    }
}