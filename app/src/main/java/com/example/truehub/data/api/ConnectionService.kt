package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient

class ConnectionService(val manager: TrueNASApiManager){
    suspend fun pingConnectionWithResult(): ApiResult<String> {
        return manager.callWithResult(
            method = ApiMethods.Connection.CONNECTION_KEEP_ALIVE,
            params = listOf(),
            resultType = String::class.java
        )
    }
}