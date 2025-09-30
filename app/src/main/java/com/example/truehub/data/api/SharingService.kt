package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Shares
import com.squareup.moshi.Types

class SharingService(client: TrueNASClient): BaseApiService(client) {
    suspend fun getSmbShares(): List<Shares.SmbShare>{
        val type = Types.newParameterizedType(List::class.java, Shares.SmbShare::class.java)
        return client.call(
            method = ApiMethods.Shares.GET_SMB_SHARES,
            params = listOf(),
            resultType = type
        )
    }
    suspend fun getSmbSharesWithResult(): ApiResult<List<Shares.SmbShare>>{
        return try{
            val result = getSmbShares()
            ApiResult.Success(result)
        }
        catch (e: Exception){
            ApiResult.Error("Cannot fetch SMB Shares from Server: ${e.message}",e)
        }
    }

}