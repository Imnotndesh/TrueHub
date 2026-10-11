package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Interface
import com.squareup.moshi.Types

class InterfaceService(private val manager: TrueNASApiManager) {

    suspend fun bridgeMembersChoicesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_BRIDGE_MEMBERS_CHOICES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun cancelRollbackWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_CANCEL_ROLLBACK, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun checkinWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_CHECKIN, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun checkinWaitingWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_CHECKIN_WAITING, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun choicesWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_CHOICES, listOf(options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun commitWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_COMMIT, listOf(options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun createWithResult(data: Any?): ApiResult<Interface.Entry> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_CREATE, listOf(data), Interface.Entry::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Interface.Entry> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_GET_INSTANCE, listOf(id, options), Interface.Entry::class.java)

    suspend fun hasPendingChangesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_HAS_PENDING_CHANGES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun ipInUseWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_IP_IN_USE, listOf(options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun lacpduRateChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_LACPDU_RATE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun lagPortsChoicesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_LAG_PORTS_CHOICES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun networkConfigToBeRemovedWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_NETWORK_CONFIG_TO_BE_REMOVED, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Interface.Entry>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Interface.Entry::class.java))

    suspend fun rollbackWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_ROLLBACK, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun saveNetworkConfigWithResult(config: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_SAVE_NETWORK_CONFIG, listOf(config), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun servicesRestartedOnSyncWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_SERVICES_RESTARTED_ON_SYNC, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Interface.Entry> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_UPDATE, listOf(id, data), Interface.Entry::class.java)

    suspend fun vlanParentInterfaceChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_VLAN_PARENT_INTERFACE_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun websocketInterfaceWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_WEBSOCKET_INTERFACE, listOf(), Any::class.java)

    suspend fun websocketLocalIpWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_WEBSOCKET_LOCAL_IP, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun xmitHashPolicyChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Interface.INTERFACE_XMIT_HASH_POLICY_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
}
