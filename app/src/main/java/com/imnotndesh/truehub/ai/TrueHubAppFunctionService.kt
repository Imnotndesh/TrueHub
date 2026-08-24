package com.imnotndesh.truehub.ai

import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionAppUnknownException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.AppFunctionSerializable
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.AuthService
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.models.Config
import com.imnotndesh.truehub.data.models.LoginMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TrueHub's [AppFunction]s for the Android intelligence system.
 *
 * These let an agent (e.g. Gemini or another authorized caller) query the
 * connected TrueNAS server's current version and update installed apps
 * directly from natural-language prompts, without the user navigating the UI.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "TrueHubAppFunctionService",
    appFunctionXmlFileName = "truehub_app_function_service",
)
@SuppressLint("LongLogTag")
abstract class BaseTrueHubAppFunctionService : AppFunctionService() {

    /**
     * Reports the currently connected TrueNAS server details.
     *
     * Resolves the last-used saved TrueNAS account and returns its hostname,
     * software version and product type. Callers should call this first to
     * learn which server they're talking to before performing actions.
     *
     * @return The [TruenasServerInfo] describing the connected TrueNAS server.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTruenasVersion(): TruenasServerInfo = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        try {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(ctx)
                ?: throw AppFunctionAppUnknownException(
                    "No TrueNAS server found. Open the TrueHub app and connect one first."
                )

            val server = MultiAccountPrefs.getServer(ctx, serverId)
                ?: throw AppFunctionAppUnknownException("Saved TrueNAS server no longer exists.")

            val client = connectClient(serverId, accountId) ?: run {
                throw AppFunctionAppUnknownException(
                    "Could not authenticate with the TrueNAS server. Re-login in the app."
                )
            }

            try {
                val version = client.system.getSystemVersion()
                val versionShort = client.system.getSystemVersionShort()
                val product = client.system.getProductType()

                TruenasServerInfo(
                    name = server.nickname ?: server.serverUrl,
                    serverUrl = server.serverUrl,
                    version = (version as? ApiResult.Success)?.data,
                    versionShort = (versionShort as? ApiResult.Success)?.data,
                    productType = (product as? ApiResult.Success)?.data,
                )
            } finally {
                client.disconnect()
            }
        } catch (e: AppFunctionAppUnknownException) {
            throw e
        } catch (e: Exception) {
            throw AppFunctionAppUnknownException("Failed to read TrueNAS version: ${e.message}")
        }
    }

    /**
     * Returns the update status of an installed app, if any.
     *
     * Look up whether a specific installed app on the TrueNAS server has an
     * update available and, if so, which newer version is the target.
     *
     * @param appName The name of the installed app to inspect (e.g. "plex").
     * @return The [AppUpdateStatus] describing whether an update is available.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getAppUpdateStatus(
        appName: String,
    ): AppUpdateStatus = withContext(Dispatchers.IO) {
        if (appName.isBlank()) {
            throw AppFunctionInvalidArgumentException("appName must not be blank")
        }
        val ctx = applicationContext
        try {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(ctx)
                ?: throw AppFunctionAppUnknownException("No TrueNAS server connected.")
            val client = connectClient(serverId, accountId)
                ?: throw AppFunctionAppUnknownException("Could not authenticate with the TrueNAS server.")

            try {
                val appResult = client.apps.getAppByName(appName)
                val app = (appResult as? ApiResult.Success)?.data
                    ?: throw AppFunctionAppUnknownException("App '$appName' was not found on the server.")

                AppUpdateStatus(
                    appName = app.name,
                    state = app.state,
                    installedVersion = app.humanVersion ?: app.version,
                    upgradeAvailable = app.upgrade_available,
                    targetVersion = app.latestVersion,
                )
            } finally {
                client.disconnect()
            }
        } catch (e: AppFunctionAppUnknownException) {
            throw e
        } catch (e: Exception) {
            throw AppFunctionAppUnknownException("Failed to read app update status: ${e.message}")
        }
    }

    /**
     * Updates an installed TrueNAS app to the latest version.
     *
     * This is a mutating action. Use [getAppUpdateStatus] first to confirm an
     * update is actually available. The app should be RUNNING; if it is
     * stopped, the update may still be attempted (TrueHub will try to start it).
     *
     * @param appName The name of the installed app to update (e.g. "plex").
     * @return The [AppUpgradeResult] describing the outcome.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun upgradeApp(
        appName: String,
    ): AppUpgradeResult = withContext(Dispatchers.IO) {
        if (appName.isBlank()) {
            throw AppFunctionInvalidArgumentException("appName must not be blank")
        }
        val ctx = applicationContext
        try {
            val (serverId, accountId) = MultiAccountPrefs.getLastUsedProfile(ctx)
                ?: throw AppFunctionAppUnknownException("No TrueNAS server connected.")
            val client = connectClient(serverId, accountId)
                ?: throw AppFunctionAppUnknownException("Could not authenticate with the TrueNAS server.")

            try {
                val result = client.apps.upgradeAppWithResult(appName)
                when (result) {
                    is ApiResult.Success -> {
                        AppUpgradeResult(
                            appName = appName,
                            success = true,
                            jobId = result.data,
                        )
                    }
                    is ApiResult.Error -> {
                        throw AppFunctionAppUnknownException(
                            "Failed to upgrade '$appName': ${result.message}"
                        )
                    }
                    is ApiResult.Loading -> {
                        AppUpgradeResult(
                            appName = appName,
                            success = true,
                            jobId = null,
                        )
                    }
                }
            } finally {
                client.disconnect()
            }
        } catch (e: AppFunctionAppUnknownException) {
            throw e
        } catch (e: Exception) {
            throw AppFunctionAppUnknownException("Failed to upgrade app: ${e.message}")
        }
    }

    /** Connects and authenticates the last-used TrueNAS account. Returns null on failure. */
    private suspend fun connectClient(
        serverId: String,
        accountId: String,
    ): TrueNASApiManager? {
        val ctx = applicationContext
        val server = MultiAccountPrefs.getServer(ctx, serverId) ?: return null
        val account = MultiAccountPrefs.getAccount(ctx, accountId) ?: return null

        val client = TrueNASClient(
            Config.ClientConfig(
                serverUrl = server.serverUrl,
                insecure = server.insecure,
            )
        )
        if (!client.connect()) return null

        val manager = TrueNASApiManager(client, ctx)
        val token = MultiAccountPrefs.getTokenForLastUsed(ctx)
        val authed = if (token != null) {
            manager.auth.loginWithTokenAndResult(token) is ApiResult.Success
        } else false

        if (!authed) {
            val (cred1, cred2) = MultiAccountPrefs.getAccountCredentials(
                ctx, accountId, account.loginMethod
            )
            val loginResult = when (account.loginMethod) {
                LoginMethod.API_KEY -> cred1?.let { manager.auth.loginWithApiKeyWithResult(it) }
                LoginMethod.PASSWORD, LoginMethod.TOTP ->
                    if (cred1 != null && cred2 != null) {
                        manager.auth.loginUserWithResult(AuthService.DefaultAuth(cred1, cred2))
                    } else null
            }
            if (loginResult == null || loginResult is ApiResult.Error) {
                client.disconnect()
                return null
            }
        }
        return manager
    }
}

/**
 * Information about the connected TrueNAS server.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TruenasServerInfo(
    /** The display name or URL of the TrueNAS server. */
    val name: String,
    /** The URL used to reach the server. */
    val serverUrl: String,
    /** The full TrueNAS software version string, if available. */
    val version: String?,
    /** The short TrueNAS software version string, if available. */
    val versionShort: String?,
    /** The product type (e.g. COMMUNITY_EDITION or ENTERPRISE). */
    val productType: String?,
)

/**
 * Update status of an installed TrueNAS app.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppUpdateStatus(
    /** The name of the installed app. */
    val appName: String,
    /** The current runtime state of the app (e.g. RUNNING or STOPPED). */
    val state: String?,
    /** The currently installed human-readable version. */
    val installedVersion: String?,
    /** Whether an update is available for this app. */
    val upgradeAvailable: Boolean,
    /** The newer version available for upgrade, if any. */
    val targetVersion: String?,
)

/**
 * Outcome of an app upgrade request.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppUpgradeResult(
    /** The name of the app that was updated. */
    val appName: String,
    /** Whether the upgrade was submitted successfully. */
    val success: Boolean,
    /** The TrueNAS job id that tracks the upgrade, if known. */
    val jobId: Int?,
)
