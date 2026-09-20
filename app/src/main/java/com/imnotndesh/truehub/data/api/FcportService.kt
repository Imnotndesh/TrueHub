package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class FcportService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(fcPortCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_CREATE, listOf(fcPortCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun portChoicesWithResult(includeUsed: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_PORT_CHOICES, listOf(includeUsed), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun statusWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_STATUS, listOf(filters, options), Any::class.java)

    suspend fun updateWithResult(id: Any?, fcPortUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fcport.FCPORT_UPDATE, listOf(id, fcPortUpdate), Any::class.java)
}
