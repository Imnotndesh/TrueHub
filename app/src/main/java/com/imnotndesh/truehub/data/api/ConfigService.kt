package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class ConfigService(private val manager: TrueNASApiManager) {

    suspend fun resetWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Config.CONFIG_RESET, listOf(options), Any::class.java)

    suspend fun saveWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Config.CONFIG_SAVE, listOf(options), Any::class.java)

    suspend fun uploadWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Config.CONFIG_UPLOAD, listOf(), Any::class.java)
}
