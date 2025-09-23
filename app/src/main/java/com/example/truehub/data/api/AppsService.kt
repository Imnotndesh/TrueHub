package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Apps
import com.squareup.moshi.Types

class AppsService(client: TrueNASClient) : BaseApiService(client) {
    suspend fun getInstalledApps(): List<Apps.AppQueryResponse>{
        val type = Types.newParameterizedType(List::class.java, Apps.AppQueryResponse::class.java)
        return client.call(
            method = ApiMethods.Apps.QUERY_APPS,
            params = listOf(),
            resultType = type
        )
    }
    suspend fun getInstalledAppsWithResult(): ApiResult<List<Apps.AppQueryResponse>>{
        return try {
            val result = getInstalledApps()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }
}