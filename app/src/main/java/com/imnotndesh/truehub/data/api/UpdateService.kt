package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class UpdateService(private val manager: TrueNASApiManager) {

    suspend fun fileWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Update.UPDATE_FILE, listOf(options), Any::class.java)

    suspend fun manualWithResult(path: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Update.UPDATE_MANUAL, listOf(path, options), Any::class.java)
}
