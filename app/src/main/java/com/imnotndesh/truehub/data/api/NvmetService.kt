package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class NvmetService(private val manager: TrueNASApiManager) {

    suspend fun globalConfigWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_GLOBAL_CONFIG, listOf(), Any::class.java)

    suspend fun globalSessionsWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_GLOBAL_SESSIONS, listOf(filters, options), Any::class.java)

    suspend fun globalUpdateWithResult(nvmetUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_GLOBAL_UPDATE, listOf(nvmetUpdate), Any::class.java)

    suspend fun hostCreateWithResult(nvmetHostCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_CREATE, listOf(nvmetHostCreate), Any::class.java)

    suspend fun hostDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_DELETE, listOf(id, options), Unit::class.java)

    suspend fun hostDhchapDhgroupChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_DHCHAP_DHGROUP_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun hostDhchapHashChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_DHCHAP_HASH_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun hostGenerateKeyWithResult(dhchapHash: Any?, nqn: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_GENERATE_KEY, listOf(dhchapHash, nqn), Any::class.java)

    suspend fun hostGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun hostQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun hostUpdateWithResult(id: Any?, nvmetHostUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_UPDATE, listOf(id, nvmetHostUpdate), Any::class.java)

    suspend fun hostSubsysCreateWithResult(nvmetHostSubsysCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_SUBSYS_CREATE, listOf(nvmetHostSubsysCreate), Any::class.java)

    suspend fun hostSubsysDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_SUBSYS_DELETE, listOf(id), Unit::class.java)

    suspend fun hostSubsysGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_SUBSYS_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun hostSubsysQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_SUBSYS_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun hostSubsysUpdateWithResult(id: Any?, nvmetHostSubsysUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_HOST_SUBSYS_UPDATE, listOf(id, nvmetHostSubsysUpdate), Any::class.java)

    suspend fun namespaceCreateWithResult(nvmetNamespaceCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_NAMESPACE_CREATE, listOf(nvmetNamespaceCreate), Any::class.java)

    suspend fun namespaceDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_NAMESPACE_DELETE, listOf(id, options), Unit::class.java)

    suspend fun namespaceGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_NAMESPACE_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun namespaceQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_NAMESPACE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun namespaceUpdateWithResult(id: Any?, nvmetNamespaceUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_NAMESPACE_UPDATE, listOf(id, nvmetNamespaceUpdate), Any::class.java)

    suspend fun portCreateWithResult(nvmetPortCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_CREATE, listOf(nvmetPortCreate), Any::class.java)

    suspend fun portDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_DELETE, listOf(id, options), Unit::class.java)

    suspend fun portGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun portQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun portTransportAddressChoicesWithResult(addrTrtype: Any?, forceAna: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_TRANSPORT_ADDRESS_CHOICES, listOf(addrTrtype, forceAna), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun portUpdateWithResult(id: Any?, nvmetPortUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_UPDATE, listOf(id, nvmetPortUpdate), Any::class.java)

    suspend fun portSubsysCreateWithResult(nvmetPortSubsysCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_SUBSYS_CREATE, listOf(nvmetPortSubsysCreate), Any::class.java)

    suspend fun portSubsysDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_SUBSYS_DELETE, listOf(id), Unit::class.java)

    suspend fun portSubsysGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_SUBSYS_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun portSubsysQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_SUBSYS_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun portSubsysUpdateWithResult(id: Any?, nvmetPortSubsysUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_PORT_SUBSYS_UPDATE, listOf(id, nvmetPortSubsysUpdate), Any::class.java)

    suspend fun subsysCreateWithResult(nvmetSubsysCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_SUBSYS_CREATE, listOf(nvmetSubsysCreate), Any::class.java)

    suspend fun subsysDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_SUBSYS_DELETE, listOf(id, options), Unit::class.java)

    suspend fun subsysGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_SUBSYS_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun subsysQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_SUBSYS_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun subsysUpdateWithResult(id: Any?, nvmetSubsysUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Nvmet.NVMET_SUBSYS_UPDATE, listOf(id, nvmetSubsysUpdate), Any::class.java)
}
