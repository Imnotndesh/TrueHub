package com.imnotndesh.truehub.data.helpers

import com.imnotndesh.truehub.data.ConnectionState
import com.imnotndesh.truehub.data.api.ApiMethods
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.AppStats
import com.imnotndesh.truehub.data.models.AppStatsEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold

class AppStatsRepository(private val manager: TrueNASApiManager) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val eventAdapter = moshi.adapter(AppStatsEvent::class.java)

    fun observe(appId: String, intervalSeconds: Int = DEFAULT_INTERVAL): Flow<AppStats> = flow {
        manager.ensureConnected()
        val connected = manager.connectionState
            .map { it is ConnectionState.Connected }
            .distinctUntilChanged()
        emitAll(
            connected.flatMapLatest { isConnected ->
                if (isConnected) ticks(appId, intervalSeconds) else emptyFlow()
            }
        )
    }

    fun observeHistory(
        appId: String,
        intervalSeconds: Int = DEFAULT_INTERVAL,
        capacity: Int = DEFAULT_CAPACITY
    ): Flow<List<AppStats>> =
        observe(appId, intervalSeconds)
            .runningFold(emptyList<AppStats>()) { history, stat ->
                (history + stat).takeLast(capacity)
            }

    private fun ticks(appId: String, intervalSeconds: Int): Flow<AppStats> = flow {
        val subscriptionId = manager.subscribe(subscriptionName(intervalSeconds))
        try {
            manager.events
                .filter { it.method == ApiMethods.Events.COLLECTION_UPDATE }
                .mapNotNull { event -> statFor(event.params, appId) }
                .collect { emit(it) }
        } finally {
            runCatching { manager.unsubscribe(subscriptionId) }
        }
    }

    private fun statFor(params: Any?, appId: String): AppStats? {
        if (params == null) return null
        val event = runCatching { eventAdapter.fromJsonValue(params) }.getOrNull() ?: return null
        if (event.collection?.startsWith(ApiMethods.Events.APP_STATS) != true) return null
        return event.fields.firstOrNull { it.appName == appId }
    }

    private fun subscriptionName(intervalSeconds: Int): String {
        val interval = intervalSeconds.coerceAtLeast(MIN_INTERVAL)
        return "${ApiMethods.Events.APP_STATS}:{\"interval\":$interval}"
    }

    companion object {
        const val DEFAULT_INTERVAL = 2
        const val MIN_INTERVAL = 2
        const val DEFAULT_CAPACITY = 90
    }
}
