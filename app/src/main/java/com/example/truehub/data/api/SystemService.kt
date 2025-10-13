package com.example.truehub.data.api

import android.util.Log
import androidx.annotation.Nullable
import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.api.ApiMethods.System.GET_GRAPH_DATA
import com.example.truehub.data.api.ApiMethods.System.GET_POOL_DETAILS
import com.example.truehub.data.models.System
import com.squareup.moshi.Types

class SystemService(client: TrueNASClient): BaseApiService(client) {
    suspend fun getSystemInfo(): System.SystemInfo {
        return client.call(
            method = ApiMethods.System.SYSTEM_INFO,
            params = listOf(),
            resultType = System.SystemInfo::class.java
        )
    }
    suspend fun getSystemInfoWithResult(): ApiResult<System.SystemInfo> {
        return try {
            val result = getSystemInfo()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get system info: ${e.message}", e)
        }
    }
    // Shutdown Call
    suspend fun shutdownSystem(reason:String){
        client.call<Any?>(
            method = ApiMethods.System.SHUTDOWN,
            params = listOf(reason),
            resultType = Any::class.java
        )
    }
    suspend fun shutdownSystemWithResult(reason: String): ApiResult<Any>{
        return try {
            val result = shutdownSystem(reason)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to shutdown system: ${e.message}", e)
        }
    }
    // Getting Disk Details
    suspend fun getPools(): List<System.Pool> {
        return client.call(
            method = GET_POOL_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.Pool::class.java)
        )
    }

    suspend fun getPoolsWithResult(): ApiResult<List<System.Pool>> {
        return try {
            val result = getPools()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get pools: ${e.message}", e)
        }
    }

    suspend fun getDisks(): List<System.DiskDetails> {
        return client.call(
            method = ApiMethods.System.GET_DISK_DETAILS,
            params = listOf(),
            resultType = Types.newParameterizedType(List::class.java, System.DiskDetails::class.java)
        )
    }

    suspend fun getDisksWithResult(): ApiResult<List<System.DiskDetails>> {
        return try {
            val result = getDisks()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get disks: ${e.message}", e)
        }
    }

    // Get all possible results
    suspend fun getPossibleGraphs(): System.GraphResult{
        return client.call(
            method = ApiMethods.System.GET_GRAPHS,
            params = listOf(),
            resultType = System.GraphResult::class.java
        )
    }

    suspend fun getPossibleGraphsWithResult(): ApiResult<System.GraphResult>{
        return try {
            val result = getPossibleGraphs()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get possible graphs: ${e.message}", e)
        }
    }

    // Actual reporting data
    suspend fun getReportingData(
        graphs: List<System.ReportingGraphRequest>,
        query: System.ReportingGraphQuery? = null
    ): List<System.ReportingGraphResponse> {
        return client.call(
            method = GET_GRAPH_DATA,
            params = listOf(graphs, query),
            resultType = Types.newParameterizedType(
                List::class.java,
                System.ReportingGraphResponse::class.java
            )
        )
    }
    suspend fun getReportingDataWithResult(
        graphs: List<System.ReportingGraphRequest>,
        query: System.ReportingGraphQuery? = null): ApiResult<List<System.ReportingGraphResponse>> {
        return try {
            val result = getReportingData(graphs, query)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get reporting data: ${e.message}", e)
        }
    }
    suspend fun getJobInfo(jobId: Int): System.Job {
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

    suspend fun getJobInfoJobWithResult(jobId: Int): ApiResult<System.Job> {
        return try {
            val result = getJobInfo(jobId)
            ApiResult.Success(result)
        } catch (e: Exception) {
            Log.e("TrueNAS-API", "Cannot fetch Job Info: ${e.message}", e)
            ApiResult.Error("Cannot fetch Job Info: ${e.message}", e)
        }
    }


    // Alerts Info
    suspend fun dismissAlert(uuid: String){
        return client.call(
            method = ApiMethods.System.DISMISS_ALERT,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }

    /**
     * Modelled after method:
     * @param uuid
     * @see dismissAlert
     * @return null
     */
    suspend fun dismissAlertWithResult(uuid: String): ApiResult<Any>{
        return try {
            val res = dismissAlert(uuid)
            ApiResult.Success(res)
        }catch (e : Exception){
            ApiResult.Error("Cannot dismiss alert withe error: ${e.message}",e)
        }
    }

    suspend fun listAlerts():List<System.AlertResponse>{
        val type = Types.newParameterizedType(List::class.java, System.AlertResponse::class.java)
        return client.call(
            method = ApiMethods.System.LIST_ALERTS,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * Fetch all alerts from system
     * @param none
     * @see ApiMethods.System.LIST_ALERTS
     */
    suspend fun listAlertsWithResult(): ApiResult<List<System.AlertResponse>>{
        return try{
            val result = listAlerts()
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Cannot fetch alerts: ${e.message}",e)
        }
    }

    suspend fun listCategories():List<System.AlertCategoriesResponse>{
        val type = Types.newParameterizedType(List::class.java, System.AlertCategoriesResponse::class.java)
        return client.call(
            method = ApiMethods.System.LIST_CATEGORIES,
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
        return try {
            val result = listCategories()
            ApiResult.Success(result)
        }catch (e : Exception){
            ApiResult.Error("Cannot fetch categories : ${e.message}",e)
        }
    }

    suspend fun listAlertPolicies(): List<String>{
        val type  = Types.newParameterizedType(List::class.java,String::class.java)
        return client.call(
            method = ApiMethods.System.LIST_POLICIES,
            params = listOf(),
            resultType = type
        )
    }

    /**
     * List all alert policies from server
     * @see listAlertPolicies
     * @return ArrayOf(String)
     * @param none
     */
    suspend fun listAlertPoliciesWithResult(): ApiResult<List<String>>{
        return try{
            val result = listAlertPolicies()
            ApiResult.Success(result)
        }catch (e : Exception){
            ApiResult.Error("Cannot fetch alert policies : ${e.message}",e)
        }
    }

    suspend fun restoreAlert(uuid :String){
        return client.call(
            method = ApiMethods.System.RESTORE_ALERTS,
            params = listOf(uuid),
            resultType = Any::class.java
        )
    }

    /**
     * Restore alerts based on their `uuid`
     * @param uuid
     * @see restoreAlert
     * @see ApiMethods.System.RESTORE_ALERTS
     * @return null
     */
    suspend fun restoreAlertWithResult(uuid:String): ApiResult<Any>{
        return try {
            val response = restoreAlert(uuid)
            ApiResult.Success(response)
        }catch (e : Exception){
            ApiResult.Error("Cannot Restore Alert : ${e.message}",e)
        }
    }
}