package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.squareup.moshi.Types

class KeychaincredentialService(private val manager: TrueNASApiManager) {

    suspend fun createWithResult(keychainCredentialCreate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_CREATE, listOf(keychainCredentialCreate), Any::class.java)

    suspend fun deleteWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_DELETE, listOf(id, options), Unit::class.java)

    suspend fun generateSshKeyPairWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_GENERATE_SSH_KEY_PAIR, listOf(), Any::class.java)

    suspend fun getInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_GET_INSTANCE, listOf(id, options), Any::class.java)

    suspend fun queryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Any?>> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Any::class.java))

    suspend fun remoteSshHostKeyScanWithResult(keychainRemoteSshHostKeyScan: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_REMOTE_SSH_HOST_KEY_SCAN, listOf(keychainRemoteSshHostKeyScan), Any::class.java)

    suspend fun remoteSshSemiautomaticSetupWithResult(data: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_REMOTE_SSH_SEMIAUTOMATIC_SETUP, listOf(data), Any::class.java)

    suspend fun setupSshConnectionWithResult(options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_SETUP_SSH_CONNECTION, listOf(options), Any::class.java)

    suspend fun updateWithResult(id: Any?, keychainCredentialUpdate: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_UPDATE, listOf(id, keychainCredentialUpdate), Any::class.java)

    suspend fun usedByWithResult(id: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Keychaincredential.KEYCHAINCREDENTIAL_USED_BY, listOf(id), Any::class.java)
}
