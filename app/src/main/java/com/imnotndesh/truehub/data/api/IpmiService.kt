package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Ipmi
import com.squareup.moshi.Types

class IpmiService(private val manager: TrueNASApiManager) {

    suspend fun chassisWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_CHASSIS, listOf(), Any::class.java)

    suspend fun chassisIdentifyWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_CHASSIS_IDENTIFY, listOf(data), Any::class.java)

    suspend fun chassisInfoWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_CHASSIS_INFO, listOf(data), Any::class.java)

    suspend fun isLoadedWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_IS_LOADED, listOf(), Any::class.java)

    suspend fun lanWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_LAN, listOf(), Any::class.java)

    suspend fun lanChannelsWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_LAN_CHANNELS, listOf(), Any::class.java)

    suspend fun lanQueryWithResult(data: Any?): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_LAN_QUERY, listOf(data), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun lanUpdateWithResult(channel: Any?, data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_LAN_UPDATE, listOf(channel, data), Any::class.java)

    suspend fun selWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_SEL, listOf(), Any::class.java)

    suspend fun selClearWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_SEL_CLEAR, listOf(), Any::class.java)

    suspend fun selElistWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_SEL_ELIST, listOf(filters, options), Any::class.java)

    suspend fun selInfoWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Ipmi.IPMI_SEL_INFO, listOf(), Any::class.java)
}
