package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class UpsService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ups.UPS_CONFIG, listOf(), Any::class.java)

    suspend fun driverChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Ups.UPS_DRIVER_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun portChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Ups.UPS_PORT_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(upsUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ups.UPS_UPDATE, listOf(upsUpdate), Any::class.java)
}
