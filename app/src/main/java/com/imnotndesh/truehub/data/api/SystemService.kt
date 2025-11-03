package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.ApiMethods.System.GET_GRAPH_DATA
import com.imnotndesh.truehub.data.api.ApiMethods.System.GET_POOL_DETAILS
import com.imnotndesh.truehub.data.models.System
import com.squareup.moshi.Types

class SystemService(val manager: TrueNASApiManager){
    suspend fun getSystemInfoWithResult(): ApiResult<System.SystemInfo> {
        return manager.callWithResult(
            method = ApiMethods.System.SYSTEM_INFO,
            params = listOf(),
            resultType = System.SystemInfo::class.java
        )
    }
    // Shutdown Call
    suspend fun shutdownSystemWithResult(reason: String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.SHUTDOWN,
            params = listOf(reason),
            resultType = Any::class.java
        )
    }
    // Getting Disk Details
    suspend fun getPoolsWithResult(): ApiResult<List<System.Pool>> {
        return manager.callWithResult(
            method = GET_POOL_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.Pool::class.java)
        )
    }

    // Get Disk Info
    suspend fun getDisksWithResult(): ApiResult<List<System.DiskDetails>> {
        return manager.callWithResult(
            method = ApiMethods.System.GET_DISK_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.DiskDetails::class.java)
        )
    }

    // Get all possible graph types
    suspend fun getPossibleGraphsWithResult(): ApiResult<System.GraphResult>{
        return manager.callWithResult(
            method = ApiMethods.System.GET_GRAPHS,
            params = listOf(),
            resultType = System.GraphResult::class.java
        )
    }

    // Get actual graph reporting data
    suspend fun getReportingDataWithResult(
        graphs: List<System.ReportingGraphRequest>,
        query: System.ReportingGraphQuery? = null): ApiResult<List<System.ReportingGraphResponse>> {
        return manager.callWithResult(
            method = GET_GRAPH_DATA,
            params = listOf(graphs, query),
            resultType = Types.newParameterizedType(
                List::class.java,
                System.ReportingGraphResponse::class.java
            )
        )
    }

    // Get latest job info
    suspend fun getJobInfoJobWithResult(jobId: Int): ApiResult<System.Job> {
        val filters = listOf(listOf("id", "=", jobId))
        val arrayResult = manager.callWithResult<Array<System.Job>>(
            method = ApiMethods.System.GET_JOB_STATUS,
            params = listOf(filters),
            resultType = Array<System.Job>::class.java
        )
        return when (arrayResult) {
            is ApiResult.Success -> {
                val job = arrayResult.data.firstOrNull()
                if (job != null) {
                    ApiResult.Success(job)
                } else {
                    ApiResult.Error("Job with ID $jobId not found.")
                }
            }
            is ApiResult.Error -> {
                arrayResult
            }
            is ApiResult.Loading -> {
                arrayResult
            }
        }
    }



    // Alerts Info
    suspend fun dismissAlertWithResult(uuid: String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.DISMISS_ALERT,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }

    /**
     * Fetch all alerts from system
     * @param none
     * @see ApiMethods.System.LIST_ALERTS
     */
    suspend fun listAlertsWithResult(): ApiResult<List<System.AlertResponse>>{
        val type = Types.newParameterizedType(List::class.java, System.AlertResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_ALERTS,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Fetch alert categories from server
     * @param none
     * @see ApiMethods.System.LIST_CATEGORIES
     */

    suspend fun listCategoriesWithResult(): ApiResult<List<System.AlertCategoriesResponse>>{
        val type = Types.newParameterizedType(List::class.java, System.AlertCategoriesResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_CATEGORIES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * List all alert policies from server
     * @return ArrayOf(String)
     * @param none
     */
    suspend fun listAlertPoliciesWithResult(): ApiResult<List<String>>{
        val type  = Types.newParameterizedType(List::class.java,String::class.java)
        return manager.callWithResult(
            method = ApiMethods.System.LIST_POLICIES,
            params = listOf(),
            resultType = type
        )
    }
    /**
     * Restore alerts based on their `uuid`
     * @param uuid
     * @see ApiMethods.System.RESTORE_ALERTS
     * @return null
     */
    suspend fun restoreAlertWithResult(uuid:String): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.System.RESTORE_ALERTS,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }
}