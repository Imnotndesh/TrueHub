package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Storage
import com.squareup.moshi.Types

class StorageService(manager: TrueNASApiManager): BaseApiService(manager) {

    suspend fun getScrubTasks(): ApiResult<List<Storage.PoolScrubQueryResponse>>{
        val type = Types.newParameterizedType(List::class.java,Storage.PoolScrubQueryResponse::class.java)
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_QUERY,
            params = listOf(),
            resultType = type
        )
    }

    suspend fun getScrubTaskInstance(args : Storage.PoolScrubQuerySingleArgs):ApiResult<Storage.PoolScrubQueryResponse>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_GET_INSTANCE,
            params = listOf(args),
            resultType = Storage.PoolScrubQueryResponse::class.java
        )
    }

    // Scrub Mappings
    suspend fun createScrubTask(arg : Storage.UpdatePoolScrubDetails): ApiResult<Storage.PoolScrubQueryResponse>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_CREATE,
            params = listOf(arg),
            resultType = Storage.PoolScrubQueryResponse::class.java
        )
    }

    suspend fun deleteScrubTask(id : Int): ApiResult<Boolean>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_DELETE,
            params = listOf(id),
            resultType = Boolean::class.java
        )
    }

    suspend fun updateScrubTask(args: Storage.UpdatePoolScrubArgs): ApiResult<Storage.PoolScrubQueryResponse>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_UPDATE,
            params = listOf(args.id_,args.data),
            resultType = Storage.PoolScrubQueryResponse::class.java
        )
    }

    suspend fun setScrubState(args: Storage.TakeActionOnPoolScrubArgs): ApiResult<Int>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_ACTION,
            params = listOf(args.name,args.action),
            resultType = Int::class.java
        )
    }

    suspend fun runScrubTask(args: Storage.RunPoolScrubArgs): ApiResult<Boolean>{
        return apiCallWithResult(
            method = ApiMethods.Storage.POOL_SCRUB_RUN,
            params = listOf(args.name,args.threshold),
            resultType = Boolean::class.java
        )
    }

    // Snapshot Mappings
    suspend fun createSnapshotTask(args: Storage.SnapshotTaskCreateArgs): ApiResult<Storage.SnapshotCreationResponse> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_CREATE,
            params = listOf(args),
            resultType = Storage.SnapshotCreationResponse::class.java
        )
    }

    suspend fun deleteSnapshotTask(args: Storage.DeleteSnapshotTaskArgs): ApiResult<Int> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_DELETE,
            params = listOf(args),
            resultType = Int::class.java
        )
    }
    /**
     * Better to run this before deleting a snapshot task to notify user
     */
    suspend fun checkSnapshotTaskAffectedByDeletion(args: Storage.DeleteWillChangeRetentionForArgs): ApiResult<List<Map<String, List<String>>>> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_DELETE_WILL_CHANGE_RETENTION,
            params = listOf(args),
            resultType = Types.newParameterizedType(List::class.java, Map::class.java)
        )
    }

    suspend fun executeSnapshottask(
        args: Storage.ExecuteSnapshotTaskArgs
    ): ApiResult<Any> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_RUN,
            params = listOf(args),
            resultType = Any::class.java
        )
    }

    suspend fun queryAllSnapshotTasks(): ApiResult<List<Storage.SnapshotCreationResponse>> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_QUERY,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, Storage.SnapshotCreationResponse::class.java)
        )
    }

    suspend fun getSnapshotTaskInstance(args: Storage.GetSnapshotTaskInstanceArgs): ApiResult<Storage.SnapshotCreationResponse> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_GET_INSTANCE,
            params = listOf(args),
            resultType = Storage.SnapshotCreationResponse::class.java
        )
    }

    suspend fun updateSnapshotTask(args: Storage.UpdateSnapshotTaskArgs): ApiResult<Storage.SnapshotCreationResponse> {
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_UPDATE,
            params = listOf(args),
            resultType = Storage.SnapshotCreationResponse::class.java
        )
    }
    // Check with this function to see affected snapshot tasks
    suspend fun checkAffectedTasksIfUpdated(args: Storage.UpdateWillChangeRetentionForArgs): ApiResult<List<Map<String, List<String>>>>{
        return apiCallWithResult(
            method = ApiMethods.Storage.SNAPSHOT_TASK_UPDATE_WILL_CHANGE_RETENTION,
            params = listOf(args),
            resultType = Types.newParameterizedType(List::class.java, Map::class.java)
        )
    }

    // Dataset Stuff
    suspend fun getAllDatasets(): ApiResult<List<Storage.ZfsDataset>>{
        val type = Types.newParameterizedType(List::class.java,Storage.ZfsDataset::class.java)
        return apiCallWithResult(
            method = ApiMethods.Storage.DATASET_QUERY,
            params = listOf(),
            resultType = type
        )
    }
    suspend fun getDatasetDetails(): ApiResult<List<Storage.DatasetDetailsResponse>>{
        return apiCallWithResult(
            method = ApiMethods.Storage.DATASET_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, Storage.DatasetDetailsResponse::class.java)
        )
    }


    suspend fun destroyDatasetSnapshots(args: Storage.DestroySnapshotsArgs): ApiResult<Int>{
        return apiCallWithResult(
            method = ApiMethods.Storage.DATASET_DESTROY_SNAPSHOTS,
            params = listOf(args),
            resultType = Int::class.java
        )
    }

    suspend fun getDirStats(args: Storage.FilesystemStatfsArgs): ApiResult<Storage.FilesystemStatfsResult>{
        return apiCallWithResult(
            method = ApiMethods.Storage.FILESYSTEM_STATFS,
            params = listOf(args),
            resultType = Storage.FilesystemStatfsResult::class.java
        )
    }

    suspend fun getDirInfo(args: Storage.FilesystemStatArgs): ApiResult<Storage.FilesystemStatResult>{
        return apiCallWithResult(
            method = ApiMethods.Storage.FILESYSTEM_STAT,
            params = listOf(args),
            resultType = Storage.FilesystemStatResult::class.java
        )
    }
    suspend fun mkdir(args: Storage.FilesystemMkdirArgs): ApiResult<Storage.FilesystemMkdirResult> {
        return apiCallWithResult(
            method = ApiMethods.Storage.FILESYSTEM_MKDIR,
            params = listOf(args),
            resultType = Storage.FilesystemMkdirResult::class.java
        )
    }
}