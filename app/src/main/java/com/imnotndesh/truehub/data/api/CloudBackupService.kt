package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class CloudBackupService(private val manager: TrueNASApiManager) {

    suspend fun abortWithResult(id: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_ABORT, listOf(id), Any::class.java)

    suspend fun createWithResult(cloudBackup: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_CREATE, listOf(cloudBackup), Any::class.java)

    suspend fun deleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_DELETE, listOf(id), Unit::class.java)

    suspend fun deleteSnapshotWithResult(id: Any?, snapshotId: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_DELETE_SNAPSHOT, listOf(id, snapshotId), Any::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun listSnapshotDirectoryWithResult(id: Any?, snapshotId: Any?, path: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_LIST_SNAPSHOT_DIRECTORY, listOf(id, snapshotId, path), Any::class.java)

    suspend fun listSnapshotsWithResult(id: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_LIST_SNAPSHOTS, listOf(id), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun restoreWithResult(id: Any?, snapshotId: Any?, subfolder: Any?, destinationPath: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_RESTORE, listOf(id, snapshotId, subfolder, destinationPath, options), Any::class.java)

    suspend fun syncWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_SYNC, listOf(id, options), Any::class.java)

    suspend fun transferSettingChoicesWithResult(): ApiResult<Map<String, String>> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_TRANSFER_SETTING_CHOICES, listOf(), Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    suspend fun updateWithResult(id: Any?, data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.CloudBackup.CLOUD_BACKUP_UPDATE, listOf(id, data), Any::class.java)
}
