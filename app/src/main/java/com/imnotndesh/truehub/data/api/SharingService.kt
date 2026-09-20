package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Shares
import com.squareup.moshi.Types

class SharingService(var manager : TrueNASApiManager) {
    suspend fun getSmbSharesWithResult(): ApiResult<List<Shares.SmbShare>>{
        val type = Types.newParameterizedType(List::class.java, Shares.SmbShare::class.java)
        return manager.callWithResult(
            method = ApiMethods.Shares.GET_SMB_SHARES,
            params = listOf(),
            resultType = type
        )
    }
    suspend fun getNfsSharesWithResult(): ApiResult<List<Shares.NfsShare>>{
        val type = Types.newParameterizedType(List::class.java, Shares.NfsShare::class.java)
        return manager.callWithResult(
            method = ApiMethods.Shares.GET_NFS_SHARES,
            params = emptyList(),
            resultType = type,
        )
    }

    // ── NFS shares ──

    suspend fun createNfsShareWithResult(data: Map<String, Any?>): ApiResult<Shares.NfsShare> =
        manager.callWithResult(ApiMethods.Shares.CREATE_NFS_SHARE, listOf(data), Shares.NfsShare::class.java)

    suspend fun deleteNfsShareWithResult(id: Int): ApiResult<Boolean> =
        manager.callWithResult(ApiMethods.Shares.DELETE_NFS_SHARE, listOf(id), Boolean::class.java)

    suspend fun getNfsShareWithResult(id: Int): ApiResult<Shares.NfsShare> =
        manager.callWithResult(ApiMethods.Shares.GET_NFS_SHARE_INSTANCE, listOf(id), Shares.NfsShare::class.java)

    suspend fun updateNfsShareWithResult(id: Int, data: Map<String, Any?>): ApiResult<Shares.NfsShare> =
        manager.callWithResult(ApiMethods.Shares.UPDATE_NFS_SHARE, listOf(id, data), Shares.NfsShare::class.java)

    // ── SMB shares ──

    suspend fun createSmbShareWithResult(data: Map<String, Any?>): ApiResult<Shares.SmbShare> =
        manager.callWithResult(ApiMethods.Shares.CREATE_SMB_SHARE, listOf(data), Shares.SmbShare::class.java)

    suspend fun deleteSmbShareWithResult(id: Int): ApiResult<Boolean> =
        manager.callWithResult(ApiMethods.Shares.DELETE_SMB_SHARE, listOf(id), Boolean::class.java)

    suspend fun getSmbShareWithResult(id: Int): ApiResult<Shares.SmbShare> =
        manager.callWithResult(ApiMethods.Shares.GET_SMB_SHARE_INSTANCE, listOf(id), Shares.SmbShare::class.java)

    suspend fun updateSmbShareWithResult(id: Int, data: Map<String, Any?>): ApiResult<Shares.SmbShare> =
        manager.callWithResult(ApiMethods.Shares.UPDATE_SMB_SHARE, listOf(id, data), Shares.SmbShare::class.java)

    suspend fun getSmbShareAclWithResult(shareName: String): ApiResult<Shares.SmbAcl> =
        manager.callWithResult(
            ApiMethods.Shares.GET_SMB_SHARE_ACL,
            listOf(mapOf("share_name" to shareName)),
            Shares.SmbAcl::class.java
        )

    suspend fun setSmbShareAclWithResult(acl: Shares.SmbAcl): ApiResult<Shares.SmbAcl> =
        manager.callWithResult(ApiMethods.Shares.SET_SMB_SHARE_ACL, listOf(acl), Shares.SmbAcl::class.java)

    suspend fun getSmbSharePresetsWithResult(): ApiResult<Map<String, Any?>> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return manager.callWithResult(ApiMethods.Shares.SMB_SHARE_PRESETS, listOf(), type)
    }

    suspend fun precheckSmbShareWithResult(data: Map<String, Any?>): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Shares.SMB_SHARE_PRECHECK, listOf(data), Unit::class.java)
}