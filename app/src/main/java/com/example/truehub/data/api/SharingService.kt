package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Shares
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
}