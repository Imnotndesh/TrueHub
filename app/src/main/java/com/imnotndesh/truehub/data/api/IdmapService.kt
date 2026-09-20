package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class IdmapService(private val manager: TrueNASApiManager) {

    suspend fun clearIdmapCacheWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Idmap.IDMAP_CLEAR_IDMAP_CACHE, listOf(), Any::class.java)
}
