package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class SnmpService(private val manager: TrueNASApiManager) {

    suspend fun configWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Snmp.SNMP_CONFIG, listOf(), Any::class.java)

    suspend fun updateWithResult(snmpUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Snmp.SNMP_UPDATE, listOf(snmpUpdate), Any::class.java)
}
