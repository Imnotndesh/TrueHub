package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class CronjobService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_CREATE, listOf(data), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun runWithResult(id: Any?, skipDisabled: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_RUN, listOf(id, skipDisabled), Any::class.java)

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cronjob.CRONJOB_UPDATE, listOf(id, data), Any::class.java)
}
