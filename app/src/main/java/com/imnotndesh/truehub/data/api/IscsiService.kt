package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Iscsi
import com.squareup.moshi.Types

class IscsiService(private val manager: TrueNASApiManager) {

    suspend fun authWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH, listOf(), Any::class.java)

    suspend fun authCreateWithResult(data: Any?): ApiResult<Iscsi.AuthEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH_CREATE, listOf(data), Iscsi.AuthEntry::class.java)

    suspend fun authDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH_DELETE, listOf(id), Unit::class.java)

    suspend fun authGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.AuthEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH_GET_INSTANCE, listOf(id, options), Iscsi.AuthEntry::class.java)

    suspend fun authQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.AuthEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.AuthEntry::class.java))

    suspend fun authUpdateWithResult(id: Any?, data: Any?): ApiResult<Iscsi.AuthEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_AUTH_UPDATE, listOf(id, data), Iscsi.AuthEntry::class.java)

    suspend fun extentWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT, listOf(), Any::class.java)

    suspend fun extentCreateWithResult(iscsiExtentCreate: Any?): ApiResult<Iscsi.ExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_CREATE, listOf(iscsiExtentCreate), Iscsi.ExtentEntry::class.java)

    suspend fun extentDeleteWithResult(id: Any?, remove: Any?, force: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_DELETE, listOf(id, remove, force), Unit::class.java)

    suspend fun extentDiskChoicesWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_DISK_CHOICES, listOf(), Any::class.java)

    suspend fun extentGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.ExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_GET_INSTANCE, listOf(id, options), Iscsi.ExtentEntry::class.java)

    suspend fun extentQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.ExtentEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.ExtentEntry::class.java))

    suspend fun extentUpdateWithResult(id: Any?, iscsiExtentUpdate: Any?): ApiResult<Iscsi.ExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_EXTENT_UPDATE, listOf(id, iscsiExtentUpdate), Iscsi.ExtentEntry::class.java)

    suspend fun globalWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL, listOf(), Any::class.java)

    suspend fun globalAluaEnabledWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_ALUA_ENABLED, listOf(), Any::class.java)

    suspend fun globalClientCountWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_CLIENT_COUNT, listOf(), Any::class.java)

    suspend fun globalConfigWithResult(): ApiResult<Iscsi.GlobalEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_CONFIG, listOf(), Iscsi.GlobalEntry::class.java)

    suspend fun globalIserEnabledWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_ISER_ENABLED, listOf(), Any::class.java)

    suspend fun globalSessionsWithResult(queryFilters: Any?, queryOptions: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_SESSIONS, listOf(queryFilters, queryOptions), Any::class.java)

    suspend fun globalUpdateWithResult(iscsiUpdate: Any?): ApiResult<Iscsi.GlobalEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_GLOBAL_UPDATE, listOf(iscsiUpdate), Iscsi.GlobalEntry::class.java)

    suspend fun initiatorWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR, listOf(), Any::class.java)

    suspend fun initiatorCreateWithResult(iscsiInitiatorCreate: Any?): ApiResult<Iscsi.InitiatorEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR_CREATE, listOf(iscsiInitiatorCreate), Iscsi.InitiatorEntry::class.java)

    suspend fun initiatorDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR_DELETE, listOf(id), Unit::class.java)

    suspend fun initiatorGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.InitiatorEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR_GET_INSTANCE, listOf(id, options), Iscsi.InitiatorEntry::class.java)

    suspend fun initiatorQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.InitiatorEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.InitiatorEntry::class.java))

    suspend fun initiatorUpdateWithResult(id: Any?, iscsiInitiatorUpdate: Any?): ApiResult<Iscsi.InitiatorEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_INITIATOR_UPDATE, listOf(id, iscsiInitiatorUpdate), Iscsi.InitiatorEntry::class.java)

    suspend fun portalWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL, listOf(), Any::class.java)

    suspend fun portalCreateWithResult(iscsiPortalCreate: Any?): ApiResult<Iscsi.PortalEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_CREATE, listOf(iscsiPortalCreate), Iscsi.PortalEntry::class.java)

    suspend fun portalDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_DELETE, listOf(id), Unit::class.java)

    suspend fun portalGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.PortalEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_GET_INSTANCE, listOf(id, options), Iscsi.PortalEntry::class.java)

    suspend fun portalListenIpChoicesWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_LISTEN_IP_CHOICES, listOf(), Any::class.java)

    suspend fun portalQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.PortalEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.PortalEntry::class.java))

    suspend fun portalUpdateWithResult(id: Any?, iscsiPortalUpdate: Any?): ApiResult<Iscsi.PortalEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_PORTAL_UPDATE, listOf(id, iscsiPortalUpdate), Iscsi.PortalEntry::class.java)

    suspend fun targetWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET, listOf(), Any::class.java)

    suspend fun targetCreateWithResult(iscsiTargetCreate: Any?): ApiResult<Iscsi.TargetEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_CREATE, listOf(iscsiTargetCreate), Iscsi.TargetEntry::class.java)

    suspend fun targetDeleteWithResult(id: Any?, force: Any?, deleteExtents: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_DELETE, listOf(id, force, deleteExtents), Unit::class.java)

    suspend fun targetGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.TargetEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_GET_INSTANCE, listOf(id, options), Iscsi.TargetEntry::class.java)

    suspend fun targetQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.TargetEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.TargetEntry::class.java))

    suspend fun targetUpdateWithResult(id: Any?, iscsiTargetUpdate: Any?): ApiResult<Iscsi.TargetEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_UPDATE, listOf(id, iscsiTargetUpdate), Iscsi.TargetEntry::class.java)

    suspend fun targetValidateNameWithResult(name: Any?, existingId: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGET_VALIDATE_NAME, listOf(name, existingId), Any::class.java)

    suspend fun targetextentWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT, listOf(), Any::class.java)

    suspend fun targetextentCreateWithResult(iscsiTargetToExtentCreate: Any?): ApiResult<Iscsi.TargetExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT_CREATE, listOf(iscsiTargetToExtentCreate), Iscsi.TargetExtentEntry::class.java)

    suspend fun targetextentDeleteWithResult(id: Any?, force: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT_DELETE, listOf(id, force), Unit::class.java)

    suspend fun targetextentGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Iscsi.TargetExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT_GET_INSTANCE, listOf(id, options), Iscsi.TargetExtentEntry::class.java)

    suspend fun targetextentQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Iscsi.TargetExtentEntry>> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Iscsi.TargetExtentEntry::class.java))

    suspend fun targetextentUpdateWithResult(id: Any?, iscsiTargetToExtentUpdate: Any?): ApiResult<Iscsi.TargetExtentEntry> =
        manager.callWithResult(ApiMethods.Iscsi.ISCSI_TARGETEXTENT_UPDATE, listOf(id, iscsiTargetToExtentUpdate), Iscsi.TargetExtentEntry::class.java)
}
