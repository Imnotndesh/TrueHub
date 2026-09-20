package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class KmipService(private val manager: TrueNASApiManager) {

    suspend fun clearSyncPendingKeysWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kmip.KMIP_CLEAR_SYNC_PENDING_KEYS, listOf(), Any::class.java)

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kmip.KMIP_CONFIG, listOf(), Any::class.java)

    suspend fun kmipSyncPendingWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kmip.KMIP_KMIP_SYNC_PENDING, listOf(), Any::class.java)

    suspend fun syncKeysWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kmip.KMIP_SYNC_KEYS, listOf(), Any::class.java)

    suspend fun updateWithResult(kmipUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Kmip.KMIP_UPDATE, listOf(kmipUpdate), Any::class.java)
}
