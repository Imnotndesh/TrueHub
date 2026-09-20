package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class JbofService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_CREATE, listOf(data), Any::class.java)

    suspend fun deleteWithResult(id: Any?, force: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_DELETE, listOf(id, force), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun licensedWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_LICENSED, listOf(), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun reapplyConfigWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_REAPPLY_CONFIG, listOf(), Any::class.java)

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Jbof.JBOF_UPDATE, listOf(id, data), Any::class.java)
}
