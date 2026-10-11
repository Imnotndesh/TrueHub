package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class NfsService(private val manager: TrueNASApiManager) {

    suspend fun bindipChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Nfs.NFS_BINDIP_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun clientCountWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nfs.NFS_CLIENT_COUNT, listOf(), Any::class.java)

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nfs.NFS_CONFIG, listOf(), Any::class.java)

    suspend fun getNfs3ClientsWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nfs.NFS_GET_NFS3_CLIENTS, listOf(filters, options), Any::class.java)

    suspend fun getNfs4ClientsWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nfs.NFS_GET_NFS4_CLIENTS, listOf(filters, options), Any::class.java)

    suspend fun updateWithResult(nfsUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nfs.NFS_UPDATE, listOf(nfsUpdate), Any::class.java)
}
