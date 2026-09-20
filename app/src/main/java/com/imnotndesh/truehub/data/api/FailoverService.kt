package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class FailoverService(private val manager: TrueNASApiManager) {

    suspend fun disabledReasonsWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Failover.FAILOVER_DISABLED_REASONS, listOf(), Any::class.java)

    suspend fun rebootInfoWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Failover.FAILOVER_REBOOT_INFO, listOf(), Any::class.java)

    suspend fun rebootOtherNodeWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Failover.FAILOVER_REBOOT_OTHER_NODE, listOf(options), Any::class.java)
}
