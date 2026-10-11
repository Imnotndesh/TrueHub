package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class CertificateService(private val manager: TrueNASApiManager) {

    suspend fun acmeServerChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_ACME_SERVER_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun countryChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_COUNTRY_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun createWithResult(certificateCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_CREATE, listOf(certificateCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?, force: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_DELETE, listOf(id, force), Unit::class.java)

    suspend fun ecCurveChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_EC_CURVE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun extendedKeyUsageChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_EXTENDED_KEY_USAGE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun updateWithResult(id: Any?, certificateUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Certificate.CERTIFICATE_UPDATE, listOf(id, certificateUpdate), Any::class.java)
}
