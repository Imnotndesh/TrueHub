package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class HardwareService(private val manager: TrueNASApiManager) {

    suspend fun virtualizationVariantWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Hardware.HARDWARE_VIRTUALIZATION_VARIANT, listOf(), Any::class.java)
}
