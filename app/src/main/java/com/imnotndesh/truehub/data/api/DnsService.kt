package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class DnsService(private val manager: TrueNASApiManager) {

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Dns.DNS_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))
}
