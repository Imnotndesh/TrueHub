package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class DeviceService(private val manager: TrueNASApiManager) {

    suspend fun getInfoWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Device.DEVICE_GET_INFO, listOf(data), Any::class.java)
}
