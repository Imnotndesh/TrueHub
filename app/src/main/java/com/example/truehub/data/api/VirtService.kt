package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Virt
import com.squareup.moshi.Types

class VirtService(client: TrueNASClient) : BaseApiService(client) {
    // Get all instance
    suspend fun getAllInstances(): List<Virt.ContainerResponse>{
        val result = client.call<Array<Virt.ContainerResponse>>(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(),
            resultType = Array<Virt.ContainerResponse>::class.java
        )
        return result.toList()
    }
    suspend fun getAllInstancesWithResult(): ApiResult<List<Virt.ContainerResponse>>{
        return try {
            val result = getAllInstances()
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }

    // Get an instance
    suspend fun genAnInstance(id: String): Virt.ContainerResponse{
        return client.call<Virt.ContainerResponse>(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    suspend fun genAnInstanceWithResult(id: String): ApiResult<Virt.ContainerResponse>{
        return try {
            val result = genAnInstance(id)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get installed apps: ${e.message}", e)
        }
    }
    // Start Instance
    suspend fun startVirtInstance(id : String): Boolean{
        val result = client.call<Boolean>(
            method = ApiMethods.Virt.START_INSTANCE,
            params = listOf(id),
            resultType = Boolean::class.java
        )
        return result
    }
    suspend fun startVirtInstanceWithResult(id : String): ApiResult<Boolean>{
        return try {
            val result = startVirtInstance(id)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to start instance: ${e.message}", e)
        }
    }
    // Stop Instance
    suspend fun stopVirtInstance(id : String,timeout : Int? = -1, force : Boolean? = false): Boolean{
        data class stopArgs(
            val timeout: Int? = -1,
            val force: Boolean? = false
        )
        return client.call(
            method = ApiMethods.Virt.STOP_INSTANCE,
            params = listOf(id,stopArgs(timeout,force)),
            resultType = Boolean::class.java
        )
    }
    suspend fun stopVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Boolean>{
        return try {
            val result = stopVirtInstance(id,timeout,force)
            ApiResult.Success(result)
            } catch (e: Exception) {
            ApiResult.Error("Failed to stop instance: ${e.message}", e)
        }
    }
    // Restart Instance
    suspend fun restartVirtInstance(id : String,timeout : Int? = -1, force : Boolean? = false): Boolean {
        data class stopArgs(
            val timeout: Int? = -1,
            val force: Boolean? = false
        )
        return client.call(
            method = ApiMethods.Virt.RESTART_INSTANCE,
            params = listOf(id,stopArgs(timeout,force)),
            resultType = Boolean::class.java
        )
    }
    suspend fun restartVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Boolean>{
        return try {
            val result = restartVirtInstance(id,timeout,force)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to restart instance: ${e.message}", e)
        }
    }
    // Delete virt instance
    suspend fun deleteVirtInstance(id : String): Boolean {
        return client.call(
            method = ApiMethods.Virt.DELETE_INSTANCE,
            params = listOf(id),
            resultType = Boolean::class.java
        )
    }
    suspend fun deleteVirtInstanceWithResult(id : String): ApiResult<Boolean>{
        return try {
            val result = deleteVirtInstance(id)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to delete instance: ${e.message}", e)
        }
    }

    // Update Instance
    suspend fun updateVirtInstance(id : String,newInstanceInfo: Virt.ContainerUpdate): Virt.ContainerResponse {
        return client.call(
            method = ApiMethods.Virt.UPDATE_INSTANCE,
            params = listOf(id,newInstanceInfo),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    suspend fun updateVirtInstanceWithResult(id : String,newInstanceInfo: Virt.ContainerUpdate): ApiResult<Virt.ContainerResponse> {
        return try {
            val result = updateVirtInstance(id, newInstanceInfo)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to update instance: ${e.message}", e)
        }
    }
    // Get all instance devices
    suspend fun getVirtInstanceDeviceList(id: String): List<Virt.Device> {
        val type = Types.newParameterizedType(List::class.java, Virt.Device::class.java)
        return client.call(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = type::class.java
        )
    }
    suspend fun getVirtInstanceDeviceListWithResult(id:String): ApiResult<List<Virt.Device>>{
        return try {
            val result = getVirtInstanceDeviceList(id)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to get instance devices: ${e.message}", e)
        }
    }
    // Delete virt instance device
    suspend fun deleteVirtInstanceDevice(id: String,deviceName: String): Boolean {
        return client.call(
            method = ApiMethods.Virt.DELETE_INSTANCE_DEVICE,
            params = listOf(id,deviceName),
            resultType = Boolean::class.java
        )
    }
    suspend fun deleteVirtInstanceDeviceWithResult(id: String,deviceName: String): ApiResult<Boolean>{
        return try {
            val result = deleteVirtInstanceDevice(id,deviceName)
            ApiResult.Success(result)
        } catch (e: Exception) {
            ApiResult.Error("Failed to delete instance device: ${e.message}", e)
        }
    }
}