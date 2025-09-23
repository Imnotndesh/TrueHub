package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.System

class SystemService(client: TrueNASClient): BaseApiService(client) {
    suspend fun getSystemInfo(): System.SystemInfo {
        return client.call(
            method = ApiMethods.System.SYSTEM_INFO,
            params = listOf(),
            resultType = Any::class.java
        )
    }
    suspend fun getSystemInfoWithResult(): ApiResult<System.SystemInfo> {
        return try {
            val result = getSystemInfo()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get system info: ${e.message}", e)
        }
    }
}