package com.example.truehub.data.api

import android.util.Log
import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Apps
import com.example.truehub.data.models.System
import com.squareup.moshi.Types

class AppsService(val manager: TrueNASApiManager) {
    /**
     * Get all installed apps from system
     * @param none
     * @return Apps.AppQueryResponse
     */
    suspend fun getInstalledAppsWithResult(): ApiResult<List<Apps.AppQueryResponse>> {
        val type = Types.newParameterizedType(List::class.java, Apps.AppQueryResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.QUERY_APPS,
            params = listOf(),
            resultType = type
        )
    }

    suspend fun startApp(appName: String) {

    }

    // Start an app
    suspend fun startAppWithResult(appName: String): ApiResult<Any> {
        return manager.callWithResult<Any>(
            method = ApiMethods.Apps.START_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }

    // Stop App
    suspend fun stopAppWithResult(appName: String): ApiResult<Any> {
        return manager.callWithResult<Any>(
            method = ApiMethods.Apps.STOP_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }

    /**
     * Upgrade an app to a new version
     * @param appName
     * @param version (optional)
     * @param backup (optional)
     */
    suspend fun upgradeAppWithResult(
        appName: String,
        version: String? = null,
        backup: Boolean? = null
    ): ApiResult<Int> {
        val options = Apps.UpgradeOptions(
            app_version = version ?: "latest",
            snapshot_hostpaths = backup ?: false
        )
        return manager.callWithResult(
            method = ApiMethods.Apps.UPGRADE_APP,
            params = listOf(appName, options),
            resultType = Int::class.java
        )
    }

    /**
     * Fetch app upgrade summary
     * @param appName
     * @param appVersion (Optional: default="latest")
     * @return Apps.AppUpgradeSummaryResult
     */
    suspend fun getUpgradeSummaryWithResult(
        appName: String,
        appVersion: String? = "latest"
    ): ApiResult<Apps.AppUpgradeSummaryResult> {
        return manager.callWithResult(
            method = ApiMethods.Apps.GET_UPGRADE_SUMMARY,
            params = listOf(appName, Apps.AppUpgradeRequest(appVersion)),
            resultType = Apps.AppUpgradeSummaryResult::class.java
        )
    }

    /**
     * Get rollback versions for an app.
     *
     * @param appName The name of the app.
     * @return A list of rollback versions.
     */
    suspend fun getRollbackVersionsWithResult(appName: String): ApiResult<List<String>> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.APP_ROLLBACK_VERSIONS,
            params = listOf(appName),
            resultType = type
        )
    }

    /**
     * Rollback an app.
     *
     * @param appName The name of the app.
     * @param version The version of the app to rollback to.
     * @param rollbackSnapshot Whether to rollback the app's snapshot.
     * @return The ID of the rollback job.
     */
    suspend fun rollbackAppWithResult(appName: String, version: String = "latest", rollbackSnapshot:Boolean = true): ApiResult<Int> {
        return manager.callWithResult(
            method = ApiMethods.Apps.ROLLBACK_APP,
            params = listOf(appName, Apps.RollbackOptions(version, rollbackSnapshot)),
            resultType = Int::class.java
        )
    }

}
