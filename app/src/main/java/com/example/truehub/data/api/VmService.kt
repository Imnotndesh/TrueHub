package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Vm
import com.squareup.moshi.Types

class VmService(client: TrueNASClient): BaseApiService(client) {

    suspend fun queryAllVms(): List<Vm.VmQueryResponse> {
        val type = Types.newParameterizedType(List::class.java, Vm.VmQueryResponse::class.java)
        return client.call(
            method = ApiMethods.Vm.GET_ALL_VM_INSTANCES,
            params = listOf(),
            resultType = type
        )
    }
    suspend fun queryAllVmsWithResult(): ApiResult<List<Vm.VmQueryResponse>>{
        return try {
            val result = queryAllVms()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }
    // Get a specific instance
    suspend fun queryVmInstance(id: String): Vm.VmQueryResponse {
        return client.call(
            method = ApiMethods.Vm.GET_INSTANCE,
            params = listOf(id),
            resultType = Vm.VmQueryResponse::class.java
        )
    }

    suspend fun queryVmInstanceWithResult(id: String): ApiResult<Vm.VmQueryResponse> {
        return try {
            val result = queryVmInstance(id)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }

    // Start Vm
    suspend fun startVmInstance(id : String){
        return client.call(
            method = ApiMethods.Vm.START_VM_INSTANCE,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun startVmInstanceWithResult(id : String): ApiResult<Any>{
        return try {
            val result = startVmInstance(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to start Vm Instance: ${e.message}",e)
        }
    }

    // Restart VM
    suspend fun restartVmInstance(id : String){
        return client.call(
            method = ApiMethods.Vm.RESTART_INSTANCE,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun restartVmInstanceWithResult(id : String): ApiResult<Any>{
        return try {
            val result = restartVmInstance(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to restart Vm Instance: ${e.message}",e)
        }
    }

    // Stop Vm
    suspend fun stopVmInstance(id : String, force: Boolean? = false, forceAfterTimeout : Boolean? = false) {
        return client.call(
            method = ApiMethods.Vm.STOP_INSTANCE,
            params = listOf(id, force, forceAfterTimeout),
            resultType = Any::class.java
        )
    }
    suspend fun stopVmInstanceWithResult(id : String, force: Boolean? = false, forceAfterTimeout : Boolean? = false): ApiResult<Any>{
        return try {
            val result = stopVmInstance(id,force,forceAfterTimeout)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to stop Vm Instance: ${e.message}",e)
        }
    }

    // Suspend Vm
    suspend fun suspendVmInstance(id:String){
        return client.call(
            method = ApiMethods.Vm.SUSPEND_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun suspendVmInstanceWithResult(id:String): ApiResult<Any>{
        return try {
            val result = suspendVmInstance(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to suspend Vm Instance: ${e.message}",e)
        }
    }

    // Power-off VM
    suspend fun powerOffVmInstance(id:String){
        return client.call(
            method = ApiMethods.Vm.POWER_OFF_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun powerOffVmInstanceWithResult(id:String): ApiResult<Any>{
        return try {
            val result = powerOffVmInstance(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to power off Vm Instance: ${e.message}",e)
        }
    }
    // Resume VM
    suspend fun resumeVmInstance(id:String){
        return client.call(
            method = ApiMethods.Vm.RESUME_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun resumeVmInstanceWithResult(id:String): ApiResult<Any>{
        return try {
            val result = resumeVmInstance(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to resume Vm Instance: ${e.message}",e)
        }
    }

    // Clone VM
    suspend fun cloneVmInstance(id:String, cloneName :String? = null): Boolean{
        return client.call(
            method = ApiMethods.Vm.CLONE_VM,
            params = listOf(id,cloneName),
            resultType = Boolean::class.java
        )
    }
    suspend fun cloneVmInstanceWithResult(id:String, cloneName :String? = null): ApiResult<Boolean> {
        return try {
            val result = cloneVmInstance(id, cloneName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to clone Vm Instance: ${e.message}", e)
        }
    }
    // Get Memory usage
    suspend fun getVmMemoryUsage(id:String): Int{
        return client.call(
            method = ApiMethods.Vm.GET_VM_MEMORY_USAGE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }
    suspend fun getVmMemoryUsageWithResult(id:String): ApiResult<Int>{
        return try {
            val result = getVmMemoryUsage(id)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Failed to get Vm Memory Usage: ${e.message}",e)
        }
    }

    // Delete VM
    suspend fun deleteVmInstance(id:String){
        return client.call(
            method = ApiMethods.Vm.DELETE_INSTANCE,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun deleteVmInstanceWithResult(id:String): ApiResult<Any>{
        return try {
            val result = deleteVmInstance(id)
            ApiResult.Success(result)
        }
        catch (e: Exception){
            ApiResult.Error("Failed to delete Vm Instance: ${e.message}",e)
        }
    }

    /**
     * Get vm Display
     * responds with:
     * @see Vm.VmDisplayUriQueryResponse
     */
    suspend fun getVmDisplay(id:String):Vm.VmDisplayUriQueryResponse{
        return client.call(
            method = ApiMethods.Vm.GET_DISPLAY_URL,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    suspend fun getVmDisplayWithResult(id:String): ApiResult<Vm.VmDisplayUriQueryResponse>{
        return try {
            val result = getVmDisplay(id)
            if (result.error != null){
                return ApiResult.Error(result.error)
            }
            ApiResult.Success(result)
        }
        catch (e: Exception){
            ApiResult.Error("Failed to get Vm Display: ${e.message}",e)
        }
    }

    /**
     * [Get VM Status]
     * Responds with
     * @see Vm.VmStatus
     */
    suspend fun getVmInstanceStatus(id:String): Vm.VmStatus{
        return client.call(
            method = ApiMethods.Vm.GET_VM_STATUS,
            params = listOf(id),
            resultType = Vm.VmStatus::class.java
        )
    }
    suspend fun getVmInstanceStatusWithResult(id:String): ApiResult<Vm.VmStatus>{
        return try {
            val result = getVmInstanceStatus(id)
            ApiResult.Success(result)
        }
        catch (e: Exception){
            ApiResult.Error("Failed to get Vm Status: ${e.message}",e)
        }
    }

}