package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Storage
import com.squareup.moshi.Types

class StorageService(val manager: TrueNASApiManager) {

    suspend fun getScrubTasks(): ApiResult<List<Storage.PoolScrubQueryResponse>>{
        val type = Types.newParameterizedType(List::class.java,Storage.PoolScrubQueryResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_QUERY,
            params = listOf(),
            resultType = type
        )
    }

}