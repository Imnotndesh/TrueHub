package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class PrivilegeService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(privilegeCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_CREATE, listOf(privilegeCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun rolesWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_ROLES, listOf(filters, options), Any::class.java)

    suspend fun updateWithResult(id: Any?, privilegeUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Privilege.PRIVILEGE_UPDATE, listOf(id, privilegeUpdate), Any::class.java)
}
