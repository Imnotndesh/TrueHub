package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class SystemdatasetService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Systemdataset.SYSTEMDATASET_CONFIG, listOf(), Any::class.java)

    suspend fun poolChoicesWithResult(includeCurrentPool: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Systemdataset.SYSTEMDATASET_POOL_CHOICES, listOf(includeCurrentPool), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Systemdataset.SYSTEMDATASET_UPDATE, listOf(data), Any::class.java)
}
