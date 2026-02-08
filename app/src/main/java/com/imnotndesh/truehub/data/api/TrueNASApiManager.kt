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
import com.imnotndesh.truehub.data.models.LoginMethod
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.reflect.Type

class TrueNASApiManager(
    private val client: TrueNASClient,
    private val applicationContext: Context
) {
    private val connectivityObserver = NetworkConnectivityObserver(applicationContext)
    private val recoveryMutex = Mutex()
    private val MAX_RETRY_ATTEMPTS = 3
    private var isRecovering = false

    val auth: AuthService by lazy { AuthService(this) }
    val system: SystemService by lazy { SystemService(this) }
    val vmService: VmService by lazy { VmService(this) }
    val apps: AppsService by lazy { AppsService(this) }
    val virtService: VirtService by lazy { VirtService(this) }
    val sharing: SharingService by lazy { SharingService(this) }
    val connection : ConnectionService by lazy { ConnectionService(this) }
    val user : UserService by lazy { UserService(this) }
    val storage : StorageService by lazy { StorageService(this) }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun <T> callWithResult(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        var result = client.callWithResult<T>(method, params, resultType)
        if (isAuthError(result)) {
            result = attemptRetryLogic(method, params, resultType)
        }

        return result
    }
    private suspend fun <T> attemptRetryLogic(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        recoveryMutex.withLock {
            var attempts = 0
            while (attempts < MAX_RETRY_ATTEMPTS) {
                attempts++
                if (performSessionRecovery()) {
                    val retryResult = client.callWithResult<T>(method, params, resultType)
                    if (!isAuthError(retryResult)) {
                        return retryResult
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
        return ApiResult.Error("Session expired. Please login again.")
    }
    private suspend fun performSessionRecovery(): Boolean {
        try {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(applicationContext)
                ?: run {
                    return false
                }
            val account = MultiAccountPrefs.getAccount(applicationContext, accountId)
                ?: run {
                    return false
                }
            val (credentialPrimary, credentialSecondary) = MultiAccountPrefs.getAccountCredentials(
                applicationContext,
                accountId,
                account.loginMethod
            )
            val loginSuccess = when (account.loginMethod) {
                LoginMethod.API_KEY -> {
                    if (credentialPrimary.isNullOrBlank()) {
                        return false
                    }
                    val result = auth.loginWithApiKeyWithResult(credentialPrimary)
                    result is ApiResult.Success && result.data == true
                }

                LoginMethod.PASSWORD -> {
                    if (credentialPrimary.isNullOrBlank() || credentialSecondary.isNullOrBlank()) {
                        android.util.Log.e("TrueNASApiManager", "❌ Recovery failed: Username or Password missing")
                        return false
                    }

                    val result = auth.loginUserWithResult(AuthService.DefaultAuth(credentialPrimary, credentialSecondary))
                    result is ApiResult.Success && result.data == true
                }
            }

            if (!loginSuccess) {
                return false
            }
            val tokenResult = auth.generateTokenWithResult()
            if (tokenResult is ApiResult.Success) {
                MultiAccountPrefs.saveCurrentSession(
                    applicationContext,
                    serverId,
                    accountId,
                    tokenResult.data
                )
                return true
            } else {
                return true
            }

        } catch (e: Exception) {
            return false
        }
    }

    private fun isAuthError(result: ApiResult<*>): Boolean {
        if (result !is ApiResult.Error) return false
        if (result.throwable is TrueNASRpcException) {
            val code = (result.throwable as TrueNASRpcException).code
            if (code == 207 || code == -32001) return true
        }
        val msg = result.message?.lowercase() ?: ""
        return msg.contains("enotauthenticated") || msg.contains("invalid session")
    }

    private suspend fun attemptRecovery() {
        recoveryMutex.withLock {
            if (isRecovering) return
            isRecovering = true

            try {
                android.util.Log.d("TrueNASApiManager", "🔄 Attempting session recovery...")

                val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(applicationContext)
                    ?: throw Exception("No active session found")

                val account = MultiAccountPrefs.getAccount(applicationContext, accountId)
                    ?: throw Exception("Account not found")

                if (account.loginMethod != LoginMethod.API_KEY) {
                    throw Exception("Recovery not supported for password-based login yet")
                }

                val (apiKey, _) = MultiAccountPrefs.getAccountCredentials(
                    applicationContext,
                    accountId,
                    LoginMethod.API_KEY
                )

                if (apiKey == null) {
                    throw Exception("API key not found for recovery")
                }

                android.util.Log.d("TrueNASApiManager", "🔑 Re-authenticating with API key...")

                val loginResult = auth.loginWithApiKeyWithResult(apiKey)
                if (loginResult !is ApiResult.Success || loginResult.data != true) {
                    throw Exception("Re-authentication failed")
                }

                android.util.Log.d("TrueNASApiManager", "🎫 Generating new token...")

                val tokenResult = auth.generateTokenWithResult()
                if (tokenResult is ApiResult.Success) {
                    MultiAccountPrefs.saveCurrentSession(
                        applicationContext,
                        serverId,
                        accountId,
                        tokenResult.data
                    )
                    android.util.Log.d("TrueNASApiManager", "Recovery success")
                } else {
                    throw Exception("Token generation failed")
                }

            } catch (e: Exception) {
                android.util.Log.e("TrueNASApiManager", "Recovery failed: ${e.message}")
                throw e
            } finally {
                isRecovering = false
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private suspend fun checkAndRecoverConnection(): Boolean {
        if (!connectivityObserver.isNetworkAvailable()) {
            return true
        }

        if (client.getCurrentConnectionState() is ConnectionState.Disconnected) {
            return !client.connect()
        }
        return false
    }

    suspend fun connect(): Boolean = client.connect()
    suspend fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.getCurrentConnectionState() == ConnectionState.Connected
}