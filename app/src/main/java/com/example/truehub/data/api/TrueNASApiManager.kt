package com.example.truehub.data.api

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.example.truehub.data.ApiResult
import com.example.truehub.data.ConnectionState
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.helpers.EncryptedPrefs
import com.example.truehub.data.helpers.NetworkConnectivityObserver
import java.lang.reflect.Type

// The ApiManager now holds all the dependencies.
class TrueNASApiManager(
    private val client: TrueNASClient,
    private val context: Context // Use ApplicationContext here
) {
    // These helpers are private to the manager.
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private val encryptedPrefs = EncryptedPrefs

    // --- SERVICE INITIALIZATION (Simple constructors) ---
    val auth: AuthService by lazy { AuthService(this) }
    val system: SystemService by lazy { SystemService(this) }
    val vmService: VmService by lazy { VmService(client) }
    val apps: AppsService by lazy { AppsService(this) }
    val virtService: VirtService by lazy { VirtService(client) }
    val sharing: SharingService by lazy { SharingService(client) }
    val connection : ConnectionService by lazy { ConnectionService(this) }
    val user : UserService by lazy { UserService(client) }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun <T> callWithResult(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        if (checkAndRecoverConnection()) {
            return ApiResult.Error("No internet or server connection.")
        }
        return client.callWithResult(method, params, resultType)
    }

    /**
     * Checks connection, and attempts to recover by reconnecting and re-authenticating.
     * @return `true` if connection is ultimately lost, `false` if it's OK.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private suspend fun checkAndRecoverConnection(): Boolean {
        if (!connectivityObserver.isNetworkAvailable()) {
            return true
        }

        if (client.getCurrentConnectionState() is ConnectionState.Disconnected) {
            if (client.connect()) {
                return false
            }
            return !reauthenticate()
        }
        return false
    }

    /**
     * Attempts to re-authenticate using stored credentials.
     * @return True if successful, false otherwise.
     */
    private suspend fun reauthenticate(): Boolean {
        if (encryptedPrefs.getUseAutoLogin(context) != true) return false

        val loginMethod = encryptedPrefs.getLoginMethod(context)
        val loginSuccessful = when (loginMethod) {
            "api_key" -> {
                val apiKey = encryptedPrefs.getApiKey(context) ?: return false
                auth.loginWithApiKeyWithResult(apiKey) is ApiResult.Success
            }
            "password" -> {
                val username = encryptedPrefs.getUsername(context) ?: return false
                val password = encryptedPrefs.getUserPass(context) ?: return false
                auth.loginUserWithResult(AuthService.DefaultAuth(username, password)) is ApiResult.Success
            }
            else -> false
        }

        if (loginSuccessful) {
            val tokenResult = auth.generateTokenWithResult()
            if (tokenResult is ApiResult.Success) {
                encryptedPrefs.saveAuthToken(context, tokenResult.data)
                encryptedPrefs.saveIsLoggedIn(context)
                return client.connect()
            }
        }

        encryptedPrefs.clearIsLoggedIn(context)
        encryptedPrefs.clearAuthToken(context)
        return false
    }
    suspend fun connect(): Boolean = client.connect()
    suspend fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.getCurrentConnectionState() == ConnectionState.Connected
}
