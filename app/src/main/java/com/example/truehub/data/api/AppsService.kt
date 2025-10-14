package com.example.truehub.data.api

import android.util.Log
import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Apps
import com.example.truehub.data.models.System
import com.squareup.moshi.Types

class AppsService(client: TrueNASClient) : BaseApiService(client) {
    suspend fun getInstalledApps(): List<Apps.AppQueryResponse> {
        val type = Types.newParameterizedType(List::class.java, Apps.AppQueryResponse::class.java)
        return client.call(
            method = ApiMethods.Apps.QUERY_APPS,
            params = listOf(),
            resultType = type
        )
    }

    suspend fun getInstalledAppsWithResult(): ApiResult<List<Apps.AppQueryResponse>> {
        return try {
            val result = getInstalledApps()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }

    suspend fun startApp(appName: String) {
        client.call<Any?>(
            method = ApiMethods.Apps.START_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }

    suspend fun startAppWithResult(appName: String): ApiResult<Any> {
        return try {
            val result = startApp(appName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to start app: ${e.message}", e)
        }
    }

    // Start App
    suspend fun stopApp(appName: String) {
        client.call<Any?>(
            method = ApiMethods.Apps.STOP_APP,
            params = listOf(appName),
            resultType = Any::class.java
        )
    }

    // Stop App
    suspend fun stopAppWithResult(appName: String): ApiResult<Any> {
        return try {
            val result = stopApp(appName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to stop selected app: ${e.message}", e)
        }
    }

    // Update App
    suspend fun upgradeApp(appName: String, version: String? = null, backup: Boolean? = null): Int {
        val options = Apps.UpgradeOptions(
            app_version = version ?: "latest",
            snapshot_hostpaths = backup ?: false
        )
        return client.call(
            method = ApiMethods.Apps.UPGRADE_APP,
            params = listOf(appName, options),
            resultType = Int::class.java
        )
    }

    suspend fun upgradeAppWithResult(
        appName: String,
        version: String? = null,
        backup: Boolean? = null
    ): ApiResult<Int> {
        return try {
            val result = upgradeApp(appName, version, backup)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to upgrade app : ${e.message}", e)
        }
    }

    suspend fun checkOnUpgradeJob(jobId: Int): System.Job {
        val filters = listOf(
            listOf("id", "=", jobId)
        )
        val jobs = client.call<Array<System.Job>>(
            method = ApiMethods.System.GET_JOB_STATUS,
            params = listOf(filters),
            resultType = Array<System.Job>::class.java
        )
        return jobs.first()
    }

    suspend fun checkOnUpgradeJobWithResult(jobId: Int): ApiResult<System.Job> {
        return try {
            val result = checkOnUpgradeJob(jobId)
            ApiResult.Success(result)
        } catch (e: Exception) {
            Log.e("TrueNAS-API", "Cannot fetch Job Info: ${e.message}", e)
            ApiResult.Error("Cannot fetch Job Info: ${e.message}", e)
        }
    }

    suspend fun getUpgradeSummary(
        appName: String,
        appVersion: String? = "latest"
    ): Apps.AppUpgradeSummaryResult {
        return client.call(
            method = ApiMethods.Apps.GET_UPGRADE_SUMMARY,
            params = listOf(appName, Apps.AppUpgradeRequest(appVersion)),
            resultType = Apps.AppUpgradeSummaryResult::class.java
        )
    }

    suspend fun getUpgradeSummaryWithResult(
        appName: String,
        appVersion: String? = "latest"
    ): ApiResult<Apps.AppUpgradeSummaryResult> {
        return try {
            val result = getUpgradeSummary(appName, appVersion)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get upgrade summary: ${e.message}", e)
        }
    }

    suspend fun getRollbackVersions(appName: String): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return client.call(
            method = ApiMethods.Apps.APP_ROLLBACK_VERSIONS,
            params = listOf(appName),
            resultType = type
        )
    }

    /**
     * Get rollback versions for an app.
     *
     * @param appName The name of the app.
     * @return A list of rollback versions.
     */
    suspend fun getRollbackVersionsWithResult(appName: String): ApiResult<List<String>> {
        return try {
            val result = getRollbackVersions(appName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get rollback versions: ${e.message}", e)
        }
    }

    suspend fun rollbackApp(appName: String, version: String = "latest", rollbackSnapshot:Boolean = true): Int {
        return client.call(
            method = ApiMethods.Apps.ROLLBACK_APP,
            params = listOf(appName, Apps.RollbackOptions(version, rollbackSnapshot)),
            resultType = Int::class.java
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
        return try {
            val result = rollbackApp(appName, version, rollbackSnapshot)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to rollback app: ${e.message}", e)
        }
    }

}
