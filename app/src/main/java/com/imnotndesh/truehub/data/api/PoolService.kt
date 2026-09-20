package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Pool
import com.squareup.moshi.Types

class PoolService(private val manager: TrueNASApiManager) {

    suspend fun attachWithResult(oid: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_ATTACH, listOf(oid, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun attachmentsWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_ATTACHMENTS, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun createWithResult(data: Any?): ApiResult<Pool.Entry> =
        manager.callWithResult(ApiMethods.Pool.POOL_CREATE, listOf(data), Pool.Entry::class.java)

    suspend fun datasetWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET, listOf(), Any::class.java)

    suspend fun datasetAttachmentsWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_ATTACHMENTS, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetChangeKeyWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_CHANGE_KEY, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetChecksumChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_CHECKSUM_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetCompressionChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_COMPRESSION_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetEncryptionAlgorithmChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_ENCRYPTION_ALGORITHM_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetEncryptionSummaryWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_ENCRYPTION_SUMMARY, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetExportKeyWithResult(id: Any?, download: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_EXPORT_KEY, listOf(id, download), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetExportKeysWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_EXPORT_KEYS, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetExportKeysForReplicationWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_EXPORT_KEYS_FOR_REPLICATION, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Pool.DatasetEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_GET_INSTANCE, listOf(id, options), Pool.DatasetEntry::class.java)

    suspend fun datasetGetQuotaWithResult(dataset: Any?, quotaType: Any?, filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_GET_QUOTA, listOf(dataset, quotaType, filters, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetInheritParentEncryptionPropertiesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_INHERIT_PARENT_ENCRYPTION_PROPERTIES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetLockWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_LOCK, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetProcessesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_PROCESSES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetPromoteWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_PROMOTE, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetRecommendedZvolBlocksizeWithResult(pool: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_RECOMMENDED_ZVOL_BLOCKSIZE, listOf(pool), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetRecordsizeChoicesWithResult(poolName: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_RECORDSIZE_CHOICES, listOf(poolName), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetRenameWithResult(id: Any?, data: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_RENAME, listOf(id, data), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetSetQuotaWithResult(dataset: Any?, quotas: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_SET_QUOTA, listOf(dataset, quotas), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetSnapshotCountWithResult(dataset: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_SNAPSHOT_COUNT, listOf(dataset), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun datasetUnlockWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_UNLOCK, listOf(id, options), Any::class.java)

    suspend fun datasetUpdateWithResult(id: Any?, data: Any?): ApiResult<Pool.DatasetEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_DATASET_UPDATE, listOf(id, data), Pool.DatasetEntry::class.java)

    suspend fun ddtPrefetchWithResult(poolName: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DDT_PREFETCH, listOf(poolName), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun ddtPruneWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DDT_PRUNE, listOf(options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun detachWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_DETACH, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun expandWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_EXPAND, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun exportWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_EXPORT, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun filesystemChoicesWithResult(types: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_FILESYSTEM_CHOICES, listOf(types), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getDisksWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_GET_DISKS, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Pool.Entry> =
        manager.callWithResult(ApiMethods.Pool.POOL_GET_INSTANCE, listOf(id, options), Pool.Entry::class.java)

    suspend fun importFindWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_IMPORT_FIND, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun importPoolWithResult(poolImport: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_IMPORT_POOL, listOf(poolImport), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun isUpgradedWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_IS_UPGRADED, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun offlineWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_OFFLINE, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun onlineWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_ONLINE, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun processesWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_PROCESSES, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun removeWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_REMOVE, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun replaceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_REPLACE, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun resilverWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_RESILVER, listOf(), Any::class.java)

    suspend fun resilverConfigWithResult(): ApiResult<Pool.ResilverEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_RESILVER_CONFIG, listOf(), Pool.ResilverEntry::class.java)

    suspend fun resilverUpdateWithResult(data: Any?): ApiResult<Pool.ResilverEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_RESILVER_UPDATE, listOf(data), Pool.ResilverEntry::class.java)

    suspend fun scrubWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_SCRUB, listOf(), Any::class.java)

    suspend fun snapshotWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT, listOf(), Any::class.java)

    suspend fun snapshotCloneWithResult(data: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_CLONE, listOf(data), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshotCreateWithResult(data: Any?): ApiResult<Pool.SnapshotEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_CREATE, listOf(data), Pool.SnapshotEntry::class.java)

    suspend fun snapshotDeleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_DELETE, listOf(id, options), Unit::class.java)

    suspend fun snapshotGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Pool.SnapshotEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_GET_INSTANCE, listOf(id, options), Pool.SnapshotEntry::class.java)

    suspend fun snapshotHoldWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_HOLD, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshotQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Pool.SnapshotEntry>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Pool.SnapshotEntry::class.java))

    suspend fun snapshotReleaseWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_RELEASE, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshotRenameWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_RENAME, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshotRollbackWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_ROLLBACK, listOf(id, options), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshotUpdateWithResult(id: Any?, data: Any?): ApiResult<Pool.SnapshotEntry> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOT_UPDATE, listOf(id, data), Pool.SnapshotEntry::class.java)

    suspend fun snapshottaskWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOTTASK, listOf(), Any::class.java)

    suspend fun snapshottaskMaxCountWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOTTASK_MAX_COUNT, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun snapshottaskMaxTotalCountWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_SNAPSHOTTASK_MAX_TOTAL_COUNT, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Pool.Entry> =
        manager.callWithResult(ApiMethods.Pool.POOL_UPDATE, listOf(id, data), Pool.Entry::class.java)

    suspend fun upgradeWithResult(id: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_UPGRADE, listOf(id), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun validateNameWithResult(poolName: Any?): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.Pool.POOL_VALIDATE_NAME, listOf(poolName), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
}
