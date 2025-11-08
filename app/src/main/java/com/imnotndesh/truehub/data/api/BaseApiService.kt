package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import java.lang.reflect.Type

abstract class BaseApiService(protected open val manager: TrueNASApiManager) {
    protected suspend fun <T> apiCallWithResult(
        method: String,
        params: List<Any?> = listOf(),
        resultType: Type
    ): ApiResult<T> = manager.callWithResult(method, params, resultType)
}
