package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Cloudsync
import com.squareup.moshi.Types

class CloudsyncService(private val manager: TrueNASApiManager) {

    private fun entryListType() = Types.newParameterizedType(List::class.java, Cloudsync.Entry::class.java)
    private fun credListType() = Types.newParameterizedType(List::class.java, Cloudsync.CredentialEntry::class.java)
    private fun mapType() = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private fun strListType() = Types.newParameterizedType(List::class.java, String::class.java)

    // ── tasks ──
    suspend fun queryTasks(filters: List<Any> = emptyList()): ApiResult<List<Cloudsync.Entry>> =
        manager.callWithResult(ApiMethods.Cloudsync.QUERY, listOf(filters), entryListType())

    suspend fun getTask(id: Int): ApiResult<Cloudsync.Entry> =
        manager.callWithResult(ApiMethods.Cloudsync.GET_INSTANCE, listOf(id), Cloudsync.Entry::class.java)

    suspend fun createTask(payload: Map<String, Any?>): ApiResult<Cloudsync.Entry> =
        manager.callWithResult(ApiMethods.Cloudsync.CREATE, listOf(payload), Cloudsync.Entry::class.java)

    suspend fun updateTask(id: Int, payload: Map<String, Any?>): ApiResult<Cloudsync.Entry> =
        manager.callWithResult(ApiMethods.Cloudsync.UPDATE, listOf(id, payload), Cloudsync.Entry::class.java)

    suspend fun deleteTask(id: Int): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Cloudsync.DELETE, listOf(id), Unit::class.java)

    suspend fun abort(id: Int): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cloudsync.ABORT, listOf(id), Any::class.java)

    suspend fun sync(id: Int, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cloudsync.SYNC, listOf(id, options), Any::class.java)

    suspend fun syncOnetime(payload: Map<String, Any?>, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cloudsync.SYNC_ONETIME, listOf(payload, options), Any::class.java)

    suspend fun restore(id: Int, opts: Map<String, Any?> = emptyMap()): ApiResult<Cloudsync.Entry> =
        manager.callWithResult(ApiMethods.Cloudsync.RESTORE, listOf(id, opts), Cloudsync.Entry::class.java)

    suspend fun listDirectory(payload: Map<String, Any?>): ApiResult<List<Map<String, Any?>>> {
        val type = Types.newParameterizedType(List::class.java, mapType())
        return manager.callWithResult(ApiMethods.Cloudsync.LIST_DIRECTORY, listOf(payload), type)
    }

    suspend fun listBuckets(credentialsId: Int): ApiResult<List<String>> =
        manager.callWithResult(ApiMethods.Cloudsync.LIST_BUCKETS, listOf(credentialsId), strListType())

    suspend fun createBucket(credentialsId: Int, name: String): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Cloudsync.CREATE_BUCKET, listOf(credentialsId, name), Any::class.java)

    suspend fun onedriveListDrives(payload: Map<String, Any?> = emptyMap()): ApiResult<List<Cloudsync.OnedriveDrive>> {
        val type = Types.newParameterizedType(List::class.java, Cloudsync.OnedriveDrive::class.java)
        return manager.callWithResult(ApiMethods.Cloudsync.ONEDRIVE_LIST_DRIVES, listOf(payload), type)
    }

    suspend fun getProviders(): ApiResult<List<Cloudsync.Provider>> {
        val type = Types.newParameterizedType(List::class.java, Cloudsync.Provider::class.java)
        return manager.callWithResult(ApiMethods.Cloudsync.PROVIDERS, listOf(), type)
    }

    // ── credentials ──
    suspend fun queryCredentials(filters: List<Any> = emptyList()): ApiResult<List<Cloudsync.CredentialEntry>> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_QUERY, listOf(filters), credListType())

    suspend fun getCredential(id: Int): ApiResult<Cloudsync.CredentialEntry> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_GET_INSTANCE, listOf(id), Cloudsync.CredentialEntry::class.java)

    suspend fun createCredential(payload: Map<String, Any?>): ApiResult<Cloudsync.CredentialEntry> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_CREATE, listOf(payload), Cloudsync.CredentialEntry::class.java)

    suspend fun updateCredential(id: Int, payload: Map<String, Any?>): ApiResult<Cloudsync.CredentialEntry> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_UPDATE, listOf(id, payload), Cloudsync.CredentialEntry::class.java)

    suspend fun deleteCredential(id: Int): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_DELETE, listOf(id), Unit::class.java)

    suspend fun verifyCredential(payload: Map<String, Any?>): ApiResult<Cloudsync.CredentialsVerifyResult> =
        manager.callWithResult(ApiMethods.Cloudsync.CREDENTIALS_VERIFY, listOf(payload), Cloudsync.CredentialsVerifyResult::class.java)
}
