package com.imnotndesh.truehub.data.api

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.ConnectionState
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.TrueNASRpcException
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.NetworkConnectivityObserver
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.LoginMethod
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.OutputStream
import java.lang.reflect.Type

class TrueNASApiManager(
    private val client: TrueNASClient,
    private val applicationContext: Context
) {
    private val connectivityObserver = NetworkConnectivityObserver(applicationContext)
    private val recoveryMutex = Mutex()

    val auth: AuthService by lazy { AuthService(this) }
    val system: SystemService by lazy { SystemService(this) }
    val vmService: VmService by lazy { VmService(this) }
    val apps: AppsService by lazy { AppsService(this) }
    val virtService: VirtService by lazy { VirtService(this) }
    val sharing: SharingService by lazy { SharingService(this) }
    val connection : ConnectionService by lazy { ConnectionService(this) }
    val user : UserService by lazy { UserService(this) }
    val storage : StorageService by lazy { StorageService(this) }
    val alertsService : AlertsService by lazy { AlertsService(this) }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun <T> callWithResult(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        ensureFreshSession()
        var result = client.callWithResult<T>(method, params, resultType)
        if (isAuthError(result) && recoverSession()) {
            result = client.callWithResult<T>(method, params, resultType)
        }
        return result
    }

    /** Proactively refreshes a near-expiry token so most calls never hit a 401. */
    private suspend fun ensureFreshSession() {
        val snapshot = runCatching { MultiAccountPrefs.getSessionSnapshot(applicationContext) }.getOrNull()
            ?: return
        if (!snapshot.isExpiringSoon()) return
        recoveryMutex.withLock {
            val current = MultiAccountPrefs.getSessionSnapshot(applicationContext) ?: return
            if (current.isExpiringSoon()) recoverSessionLocked(current)
        }
    }

    /**
     * Single recovery owner. Concurrent callers serialize on [recoveryMutex]; any caller that
     * arrives after a successful refresh simply reuses the newly stored token instead of
     * re-authenticating (the dedupe fix).
     */
    private suspend fun recoverSession(): Boolean {
        recoveryMutex.withLock {
            val snapshot = MultiAccountPrefs.getSessionSnapshot(applicationContext) ?: return false
            // Another caller already refreshed while we waited for the lock.
            if (!snapshot.isExpiringSoon(safetyFraction = 0f)) return true
            return recoverSessionLocked(snapshot)
        }
    }

    private suspend fun recoverSessionLocked(snapshot: MultiAccountPrefs.SessionSnapshot): Boolean {
        val account = MultiAccountPrefs.getAccount(applicationContext, snapshot.accountId) ?: return false
        val (credentialPrimary, credentialSecondary) = MultiAccountPrefs.getAccountCredentials(
            applicationContext,
            snapshot.accountId,
            account.loginMethod
        )
        val loginSuccess = when (account.loginMethod) {
            LoginMethod.API_KEY -> {
                if (credentialPrimary.isNullOrBlank()) return false
                val result = auth.loginWithApiKeyWithResult(credentialPrimary)
                result is ApiResult.Success && result.data == true
            }
            LoginMethod.PASSWORD, LoginMethod.TOTP -> {
                if (credentialPrimary.isNullOrBlank() || credentialSecondary.isNullOrBlank()) return false
                val result = auth.loginUserWithResult(
                    AuthService.DefaultAuth(credentialPrimary, credentialSecondary)
                )
                result is ApiResult.Success && result.data == true
            }
        }
        if (!loginSuccess) return false
        return generateAndStoreTokenLocked(snapshot.serverId, snapshot.accountId)
    }

    /** Generates a token at the requested TTL and persists it together with freshness metadata. */
    private suspend fun generateAndStoreTokenLocked(
        serverId: String,
        accountId: String,
        ttlSeconds: Int = MultiAccountPrefs.DEFAULT_TOKEN_TTL_SECONDS
    ): Boolean {
        val tokenResult = auth.generateTokenWithResult(Auth.TokenRequest(ttl = ttlSeconds))
        if (tokenResult is ApiResult.Success) {
            MultiAccountPrefs.saveCurrentSession(
                applicationContext,
                serverId,
                accountId,
                tokenResult.data,
                ttlSeconds
            )
            return true
        }
        return false
    }

    private fun isAuthError(result: ApiResult<*>): Boolean {
        if (result !is ApiResult.Error) return false
        if (result.throwable is TrueNASRpcException) {
            val code = (result.throwable as TrueNASRpcException).code
            if (code == 207 || code == -32001) return true
        }
        val msg = result.message.lowercase()
        return msg.contains("enotauthenticated") || msg.contains("invalid session")
    }
    suspend fun downloadFile(urlPath: String, outputStream: OutputStream): Boolean {
        return client.downloadFile(urlPath, outputStream)
    }

    suspend fun connect(): Boolean = client.connect()
    suspend fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.getCurrentConnectionState() == ConnectionState.Connected

    /** HTTP(S) base of the server this manager is connected to, e.g. "http://192.168.1.100:80". */
    val serverBaseHttpUrl: String get() = client.baseHttpUrl
}