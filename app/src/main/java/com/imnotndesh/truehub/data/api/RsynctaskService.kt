package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class RsynctaskService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(rsyncTaskCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_CREATE, listOf(rsyncTaskCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun runWithResult(id: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_RUN, listOf(id), Any::class.java)

    suspend fun updateWithResult(id: Any?, rsyncTaskUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Rsynctask.RSYNCTASK_UPDATE, listOf(id, rsyncTaskUpdate), Any::class.java)
}
