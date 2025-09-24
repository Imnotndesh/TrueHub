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
    suspend fun startApp(appName : String){
        client.call<Any?>(
            method = ApiMethods.Apps.START_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }
    suspend fun startAppWithResult(appName: String): ApiResult<Any>{
        return try {
            val result = startApp(appName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to start app: ${e.message}", e)
        }
    }
    suspend fun stopApp(appName : String){
        client.call<Any?>(
            method = ApiMethods.Apps.STOP_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }
    suspend fun stopAppWithResult(appName: String): ApiResult<Any>{
        return try {
            val result = stopApp(appName)
            ApiResult.Success(result)
        } catch (e: Exception){
            ApiResult.Error("Failed to stop selected app: ${e.message}",e)
        }
    }
}