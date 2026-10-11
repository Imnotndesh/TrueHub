package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class DirectoryservicesService(private val manager: TrueNASApiManager) {

    suspend fun cacheRefreshWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_CACHE_REFRESH, listOf(), Any::class.java)

    suspend fun certificateChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_CERTIFICATE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_CONFIG, listOf(), Any::class.java)

    suspend fun leaveWithResult(credential: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_LEAVE, listOf(credential), Any::class.java)

    suspend fun statusWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_STATUS, listOf(), Any::class.java)

    suspend fun syncKeytabWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_SYNC_KEYTAB, listOf(), Any::class.java)

    suspend fun updateWithResult(directoryservicesUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Directoryservices.DIRECTORYSERVICES_UPDATE, listOf(directoryservicesUpdate), Any::class.java)
}
