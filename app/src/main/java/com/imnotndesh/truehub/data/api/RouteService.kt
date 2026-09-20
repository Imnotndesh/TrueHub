package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class RouteService(private val manager: TrueNASApiManager) {

    suspend fun ipv4gwReachableWithResult(ipv4Gateway: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Route.ROUTE_IPV4GW_REACHABLE, listOf(ipv4Gateway), Any::class.java)

    suspend fun systemRoutesWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Route.ROUTE_SYSTEM_ROUTES, listOf(filters, options), Any::class.java)
}
