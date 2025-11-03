package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import java.lang.reflect.Type

abstract class BaseApiService(protected open val client: TrueNASClient) {

    // Convenient wrapper methods
    protected suspend fun <T> apiCall(
        method: String,
        params: List<Any?> = listOf(),
        resultType: Type
    ): T = client.call(method, params, resultType)
    protected suspend fun <T> apiCallWithResult(
        method: String,
        params: List<Any?> = listOf(),
        resultType: Type
    ): ApiResult<T> = client.callWithResult(method, params, resultType)
}
