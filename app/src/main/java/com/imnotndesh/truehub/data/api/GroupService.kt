package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class GroupService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(groupCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_CREATE, listOf(groupCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Group.GROUP_DELETE, listOf(id, options), Unit::class.java)

    suspend fun getGroupObjWithResult(getGroupObj: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_GET_GROUP_OBJ, listOf(getGroupObj), Any::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun getNextGidWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_GET_NEXT_GID, listOf(), Any::class.java)

    suspend fun hasPasswordEnabledUserWithResult(gids: Any?, excludeUserIds: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_HAS_PASSWORD_ENABLED_USER, listOf(gids, excludeUserIds), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Group.GROUP_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun updateWithResult(id: Any?, groupUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Group.GROUP_UPDATE, listOf(id, groupUpdate), Any::class.java)
}
