package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class StaticrouteService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Staticroute.STATICROUTE_CREATE, listOf(data), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Staticroute.STATICROUTE_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Staticroute.STATICROUTE_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Staticroute.STATICROUTE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Staticroute.STATICROUTE_UPDATE, listOf(id, data), Any::class.java)
}
