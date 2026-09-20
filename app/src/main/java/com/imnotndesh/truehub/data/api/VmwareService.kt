package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class VmwareService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(vmwareCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_CREATE, listOf(vmwareCreate), Any::class.java)

    suspend fun datasetHasVmsWithResult(dataset: Any?, recursive: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_DATASET_HAS_VMS, listOf(dataset, recursive), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_DELETE, listOf(id), Unit::class.java)

    suspend fun getDatastoresWithResult(vmware: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_GET_DATASTORES, listOf(vmware), Any::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun matchDatastoresWithDatasetsWithResult(vmware: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_MATCH_DATASTORES_WITH_DATASETS, listOf(vmware), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun updateWithResult(id: Any?, vmwareUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Vmware.VMWARE_UPDATE, listOf(id, vmwareUpdate), Any::class.java)
}
