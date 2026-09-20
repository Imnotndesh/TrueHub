package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class ZfsService(private val manager: TrueNASApiManager) {

    suspend fun resourceQueryWithResult(data: Any?): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Zfs.ZFS_RESOURCE_QUERY, listOf(data), Types.newParameterizedType(List::class.java, Any::class.java))
}
