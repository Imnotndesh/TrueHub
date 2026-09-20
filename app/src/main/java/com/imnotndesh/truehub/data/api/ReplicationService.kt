package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class ReplicationService(private val manager: TrueNASApiManager) {

    suspend fun configConfigWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_CONFIG_CONFIG, listOf(), Any::class.java)

    suspend fun configUpdateWithResult(replicationConfigUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_CONFIG_UPDATE, listOf(replicationConfigUpdate), Any::class.java)

    suspend fun countEligibleManualSnapshotsWithResult(countEligibleManualSnapshots: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_COUNT_ELIGIBLE_MANUAL_SNAPSHOTS, listOf(countEligibleManualSnapshots), Any::class.java)

    suspend fun createWithResult(replicationCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_CREATE, listOf(replicationCreate), Any::class.java)

    suspend fun createDatasetWithResult(dataset: Any?, transport: Any?, sshCredentials: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_CREATE_DATASET, listOf(dataset, transport, sshCredentials), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_DELETE, listOf(id), Unit::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun listDatasetsWithResult(transport: Any?, sshCredentials: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_LIST_DATASETS, listOf(transport, sshCredentials), Any::class.java)

    suspend fun listNamingSchemasWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_LIST_NAMING_SCHEMAS, listOf(), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun restoreWithResult(id: Any?, replicationRestore: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_RESTORE, listOf(id, replicationRestore), Any::class.java)

    suspend fun runWithResult(id: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_RUN, listOf(id), Any::class.java)

    suspend fun runOnetimeWithResult(replicationRunOnetime: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_RUN_ONETIME, listOf(replicationRunOnetime), Any::class.java)

    suspend fun targetUnmatchedSnapshotsWithResult(direction: Any?, sourceDatasets: Any?, targetDataset: Any?, transport: Any?, sshCredentials: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_TARGET_UNMATCHED_SNAPSHOTS, listOf(direction, sourceDatasets, targetDataset, transport, sshCredentials), Any::class.java)

    suspend fun updateWithResult(id: Any?, replicationUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Replication.REPLICATION_UPDATE, listOf(id, replicationUpdate), Any::class.java)
}
