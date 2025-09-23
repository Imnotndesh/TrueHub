package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient

class ConnectionService(client: TrueNASClient): BaseApiService(client) {
    suspend fun pingConnection(): Boolean{
        val response = client.call<String>(
            method = ApiMethods.Connection.CONNECTION_KEEP_ALIVE,
            params = listOf(),
            resultType = String::class.java
        )
        return response == "pong"
    }
    suspend fun pingConnectionWithResult(): ApiResult<Boolean> {
        return try {
            val result = pingConnection()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to ping connection: ${e.message}", e)
        }
    }
}