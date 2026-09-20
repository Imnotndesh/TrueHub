package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class SupportService(private val manager: TrueNASApiManager) {

    suspend fun attachTicketWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_ATTACH_TICKET, listOf(data), Any::class.java)

    suspend fun attachTicketMaxSizeWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_ATTACH_TICKET_MAX_SIZE, listOf(), Any::class.java)

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_CONFIG, listOf(), Any::class.java)

    suspend fun fieldsWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_FIELDS, listOf(), Any::class.java)

    suspend fun isAvailableWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_IS_AVAILABLE, listOf(), Any::class.java)

    suspend fun isAvailableAndEnabledWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_IS_AVAILABLE_AND_ENABLED, listOf(), Any::class.java)

    suspend fun newTicketWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_NEW_TICKET, listOf(data), Any::class.java)

    suspend fun similarIssuesWithResult(query: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_SIMILAR_ISSUES, listOf(query), Any::class.java)

    suspend fun updateWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Support.SUPPORT_UPDATE, listOf(data), Any::class.java)
}
