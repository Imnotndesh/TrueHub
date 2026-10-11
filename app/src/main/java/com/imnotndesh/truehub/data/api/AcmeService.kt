package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class AcmeService(private val manager: TrueNASApiManager) {

    suspend fun dnsAuthenticatorAuthenticatorSchemasWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_AUTHENTICATOR_SCHEMAS, listOf(), Any::class.java)

    suspend fun dnsAuthenticatorCreateWithResult(dnsAuthenticatorCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_CREATE, listOf(dnsAuthenticatorCreate), Any::class.java)

    suspend fun dnsAuthenticatorDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_DELETE, listOf(id), Unit::class.java)

    suspend fun dnsAuthenticatorGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun dnsAuthenticatorQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun dnsAuthenticatorUpdateWithResult(id: Any?, dnsAuthenticatorUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Acme.ACME_DNS_AUTHENTICATOR_UPDATE, listOf(id, dnsAuthenticatorUpdate), Any::class.java)
}
