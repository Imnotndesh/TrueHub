package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class FcService(private val manager: TrueNASApiManager) {

    suspend fun fcHostCreateWithResult(fcHostCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fc.FC_FC_HOST_CREATE, listOf(fcHostCreate), Any::class.java)

    suspend fun fcHostDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Fc.FC_FC_HOST_DELETE, listOf(id), Unit::class.java)

    suspend fun fcHostGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fc.FC_FC_HOST_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun fcHostQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Fc.FC_FC_HOST_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun fcHostUpdateWithResult(id: Any?, fcHostUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Fc.FC_FC_HOST_UPDATE, listOf(id, fcHostUpdate), Any::class.java)
}
