package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class EnclosureService(private val manager: TrueNASApiManager) {

    suspend fun labelSetWithResult(id: Any?, label: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Enclosure.ENCLOSURE_LABEL_SET, listOf(id, label), Any::class.java)
}
