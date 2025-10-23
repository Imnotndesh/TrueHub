package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Virt
import com.squareup.moshi.Types

class VirtService(val manager: TrueNASApiManager) {
    // Get all instance
    suspend fun getAllInstancesWithResult(): ApiResult<List<Virt.ContainerResponse>>{
        val type = Types.newParameterizedType(List::class.java, Virt.ContainerResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(),
            resultType = type
        )
    }

    // Get an instance
    suspend fun genAnInstanceWithResult(id: String): ApiResult<Virt.ContainerResponse>{
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    // Start Instance
    suspend fun startVirtInstanceWithResult(id : String): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.START_INSTANCE,
            params = listOf(id),
            resultType = Double::class.java
        )
    }
    // Stop Instance
    suspend fun stopVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.STOP_INSTANCE,
            params = listOf(id, Virt.stopArgs(timeout, force)),
            resultType = Double::class.java
        )
    }
    // Restart Instance
    suspend fun restartVirtInstanceWithResult(id : String,timeout : Int? = -1, force : Boolean? = false): ApiResult<Double>{
        return manager.callWithResult(
            method = ApiMethods.Virt.RESTART_INSTANCE,
            params = listOf(id, Virt.stopArgs(timeout, force)),
            resultType = Int::class.java
        )
    }
    // Delete virt instance
    suspend fun deleteVirtInstanceWithResult(id : String): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Virt.DELETE_INSTANCE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Update Instance
    suspend fun updateVirtInstanceWithResult(id : String,newInstanceInfo: Virt.ContainerUpdate): ApiResult<Virt.ContainerResponse> {
        return manager.callWithResult(
            method = ApiMethods.Virt.UPDATE_INSTANCE,
            params = listOf(id,newInstanceInfo),
            resultType = Virt.ContainerResponse::class.java
        )
    }
    // Get all instance devices
    suspend fun getVirtInstanceDeviceListWithResult(id:String): ApiResult<List<Virt.Device>>{
        val type = Types.newParameterizedType(List::class.java, Virt.Device::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_ALL_INSTANCES,
            params = listOf(id),
            resultType = type::class.java
        )
    }
    // Delete virt instance device
    suspend fun deleteVirtInstanceDeviceWithResult(id: String,deviceName: String): ApiResult<Boolean>{
        return manager.callWithResult(
            method = ApiMethods.Virt.DELETE_INSTANCE_DEVICE,
            params = listOf(id,deviceName),
            resultType = Boolean::class.java
        )
    }

    // Get Virt image choices
    suspend fun getVirtImageChoicesWithResult(): ApiResult<List<Virt.ImageChoice>>{
        val type = Types.newParameterizedType(List::class.java, Virt.ImageChoice::class.java)
        return manager.callWithResult(
            method = ApiMethods.Virt.GET_IMAGE_CHOICES,
            params = listOf(),
            resultType = type
        )
    }
}