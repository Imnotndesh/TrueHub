package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class CoreService(private val manager: TrueNASApiManager) {

    suspend fun ping(): ApiResult<String> =
        manager.callWithResult(ApiMethods.Core.PING, listOf(), String::class.java)

    suspend fun arp(): ApiResult<Map<String, Any?>> =
        manager.callWithResult(ApiMethods.Core.ARP, listOf(), mapType())

    suspend fun bulk(method: String, params: List<Any?> = emptyList()): ApiResult<Int> =
        manager.callWithResult(ApiMethods.Core.BULK, listOf(method, params), Int::class.java)

    suspend fun debug(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Core.DEBUG, listOf(), Any::class.java)

    suspend fun getMethods(): ApiResult<Map<String, Any?>> =
        manager.callWithResult(ApiMethods.Core.GET_METHODS, listOf(), mapType())

    suspend fun getServices(): ApiResult<List<Map<String, Any?>>> {
        val type = Types.newParameterizedType(List::class.java, mapType())
        return manager.callWithResult(ApiMethods.Core.GET_SERVICES, listOf(), type)
    }

    suspend fun jobDownloadLogs(jobId: Int): ApiResult<String> =
        manager.callWithResult(ApiMethods.Core.JOB_DOWNLOAD_LOGS, listOf(jobId), String::class.java)

    suspend fun jobWait(jobId: Int, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Core.JOB_WAIT, listOf(jobId, options), Any::class.java)

    suspend fun pingRemote(host: String): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Core.PING_REMOTE, listOf(host), Any::class.java)

    suspend fun resizeShell(id: String, cols: Int, rows: Int): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Core.RESIZE_SHELL, listOf(id, cols, rows), Any::class.java)

    suspend fun setOptions(options: Map<String, Any?>): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Core.SET_OPTIONS, listOf(options), Unit::class.java)

    suspend fun subscribe(event: String): ApiResult<String> =
        manager.callWithResult(ApiMethods.Core.SUBSCRIBE, listOf(event), String::class.java)

    suspend fun unsubscribe(subscriptionId: String): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Core.UNSUBSCRIBE, listOf(subscriptionId), Unit::class.java)

    private fun mapType() = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
}
