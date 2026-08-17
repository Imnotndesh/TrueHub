package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Apps
import com.squareup.moshi.Types
import kotlin.time.Duration.Companion.milliseconds

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

    // Start an app
    suspend fun startAppWithResult(appName: String): ApiResult<Any> {
        return manager.callWithResult(
            method = ApiMethods.Apps.START_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }

    // Stop App
    suspend fun stopAppWithResult(appName: String): ApiResult<Any> {
        return manager.callWithResult(
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
     *
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

        val result = manager.callWithResult<Int>(
            method = ApiMethods.Apps.UPGRADE_APP,
            params = listOf(appName, options),
            resultType = Int::class.java
        )

        if (result is ApiResult.Error && isStoppedStateError(result)) {
            val startResult = startAppWithResult(appName)
            if (startResult is ApiResult.Error) {
                return startResult.let { ApiResult.Error(it.message, it.throwable) }
            }

            val reachedRunning = waitUntilAppRunning(appName)
            if (!reachedRunning) {
                return ApiResult.Error("App did not reach RUNNING state after start; upgrade not retried.")
            }

            return manager.callWithResult(
                method = ApiMethods.Apps.UPGRADE_APP,
                params = listOf(appName, options),
                resultType = Int::class.java
            )
        }

        return result
    }

    private fun isStoppedStateError(error: ApiResult.Error): Boolean {
        return error.message?.contains("must not be in stopped state", ignoreCase = true) == true
    }

    /**
     * Polls @see app.query for [appName] until its state is RUNNING.
     * Returns false on timeout or query failure rather than throwing,
     * so callers can decide how to surface that.
     */
    private suspend fun waitUntilAppRunning(
        appName: String,
        pollIntervalMillis: Long = 1500L,
        maxAttempts: Int = 20
    ): Boolean {
        repeat(maxAttempts) {
            val result = getAppByName(appName)
            if (result is ApiResult.Success && result.data?.state == "RUNNING") {
                return true
            }
            kotlinx.coroutines.delay(pollIntervalMillis.milliseconds)
        }
        return false
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
     * @param version The version of the app to roll back to.
     * @param rollbackSnapshot Whether to roll back the app's snapshot.
     * @return The ID of the rollback job.
     */
    suspend fun rollbackAppWithResult(appName: String, version: String = "latest", rollbackSnapshot:Boolean = true): ApiResult<Int> {
        return manager.callWithResult(
            method = ApiMethods.Apps.ROLLBACK_APP,
            params = listOf(appName, Apps.RollbackOptions(version, rollbackSnapshot)),
            resultType = Int::class.java
        )
    }

    /**
     * Query apps with optional filters and options.
     * @param filters List of filter objects (see API docs). Default empty.
     * @param options Query options (pagination, extra flags, etc.)
     * @return List of apps matching the query.
     */
    suspend fun queryApps(
        filters: List<Any> = emptyList(),
        options: Apps.AppQueryOptions = Apps.AppQueryOptions()
    ): ApiResult<List<Apps.AppQueryResponse>> {
        val type = Types.newParameterizedType(List::class.java, Apps.AppQueryResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.QUERY_APPS,
            params = listOf(filters, options),
            resultType = type
        )
    }

    /**
     * Get the available disk space (in bytes) in the configured apps pool that apps can consume.
     *
     * @return Available space in bytes, or an [ApiResult.Error] on failure.
     */
    suspend fun getAvailableSpaceWithResult(): ApiResult<Long> {
        return manager.callWithResult(
            method = ApiMethods.Apps.APP_AVAILABLE_SPACE,
            params = listOf(),
            resultType = Long::class.java
        )
    }

    suspend fun queryMarketplaceAvailableItems(): ApiResult<List<Apps.AppAvailableItem>>{
        val type = Types.newParameterizedType(List::class.java,Apps.AppAvailableItem::class.java)
        return  manager.callWithResult(
            method = ApiMethods.Apps.QUERY_MARKETPLACE_APPS,
            params = listOf(),
            resultType = type
        )
    }

    suspend fun getAppByName(name: String): ApiResult<Apps.AppQueryResponse?> {
        val filters = listOf(listOf("name", "=", name))
        val options = Apps.AppQueryOptions(get = true)
        return queryApps(filters, options).let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.firstOrNull())
                is ApiResult.Error -> ApiResult.Error(result.message, result.throwable)
                is ApiResult.Loading -> ApiResult.Loading
            }
        }
    }
    suspend fun getCatalogAppDetails(
        appName: String,
        train: String = ApiMethods.Apps.STABLE_APPS_TRAIN
    ): ApiResult<Apps.CatalogAppDetails> {
        return manager.callWithResult(
            method = ApiMethods.Apps.GET_CATALOG_APP_DETAILS,
            params = listOf(appName, mapOf("train" to train)),
            resultType = Apps.CatalogAppDetails::class.java
        )
    }

    /**
     * Retrieve applications similar to the given app name.
     * @param appName Name of the application to find similar apps for.
     * @param train The catalog train to search within (default "latest").
     * @return List of similar applications.
     */
    suspend fun getSimilarApps(
        appName: String,
        train: String = ApiMethods.Apps.STABLE_APPS_TRAIN
    ): ApiResult<List<Apps.AppSimilarResponse>> {
        val type = Types.newParameterizedType(List::class.java, Apps.AppSimilarResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.SIMILAR_APPS,
            params = listOf(appName, train),
            resultType = type
        )
    }
    suspend fun removeAppWithResult(
        appName: String,
        options: Apps.DeleteAppOptions = Apps.DeleteAppOptions()
    ): ApiResult<Int>{
      return manager.callWithResult(
          method = ApiMethods.Apps.DELETE_APP,
          params = listOf(appName, options),
          resultType = Int::class.java
      )
    }
    suspend fun createAppWithResult(
        appName: String,
        catalogApp: String,
        train: String,
        version: String,
        values: Map<String, Any?>
    ): ApiResult<Int> {
        val payload = mapOf(
            "app_name" to appName,
            "catalog_app" to catalogApp,
            "train" to train,
            "version" to version,
            "values" to values
        )
        return manager.callWithResult(
            method = ApiMethods.Apps.APP_CREATE,
            params = listOf(payload),
            resultType = Int::class.java
        )
    }
    /**
     * Retrieve certificate choices that applications can use during setup
     * @return List of id and name of certificate choices
     */
    suspend fun getCertificateChoices(): ApiResult<Apps.CertificateChoiceResponse>{
        val result = Types.newParameterizedType(List::class.java,Apps.CertificateChoiceResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.CERTIFICATE_CHOICES,
            params = listOf(),
            resultType = result
        )
    }
    /**
     * Retrieve ports used by applications
     * @return List of ports used by the applications installed
     */
    suspend fun getAppPorts(): ApiResult<List<Int>>{
        val result = Types.newParameterizedType(List::class.java, Int::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.USED_APP_PORTS,
            params = listOf(),
            resultType = result
        )
    }
    suspend fun getAppConfig(appName: String): ApiResult<Map<String,Any>>{
        val result = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return manager.callWithResult(
            method = ApiMethods.Apps.GET_APP_CONFIG,
            params = listOf(appName),
            resultType = result
        )
    }
    suspend fun updateAppConfig(appName: String, configOptions: Apps.UpdateAppConfigOptions): ApiResult<Int> {
        return manager.callWithResult(
            method = ApiMethods.Apps.UPDATE_APP_CONFIG,
            params = listOf(appName, configOptions),
            resultType = Int::class.java
        )
    }

}
