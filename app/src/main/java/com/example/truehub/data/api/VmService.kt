package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.Vm
import com.squareup.moshi.Types

class VmService(var manager: TrueNASApiManager) {
    suspend fun queryAllVmsWithResult(): ApiResult<List<Vm.VmQueryResponse>>{
        val type = Types.newParameterizedType(List::class.java, Vm.VmQueryResponse::class.java)
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_ALL_VM_INSTANCES,
            params = listOf(),
            resultType = type
        )
    }
    // Get a specific instance
    suspend fun queryVmInstanceWithResult(id: Int): ApiResult<Vm.VmQueryResponse> {
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_INSTANCE,
            params = listOf(id),
            resultType = Vm.VmQueryResponse::class.java
        )
    }

    // Start Vm
    suspend fun startVmInstanceWithResult(id : Int,overcommit: Boolean = false): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.START_VM_INSTANCE,
            params = listOf(id, Vm.StartOptions(overcommit)),
            resultType = Int::class.java
        )
    }

    // Restart VM
    suspend fun restartVmInstanceWithResult(id : Int): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.RESTART_INSTANCE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Stop Vm

    suspend fun stopVmInstanceWithResult(id : Int, force: Boolean? = false, forceAfterTimeout : Boolean? = false): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.STOP_INSTANCE,
            params = listOf(id, Vm.StopOptions(force, forceAfterTimeout)),
            resultType = Int::class.java
        )
    }

    // Suspend Vm
    suspend fun suspendVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.SUSPEND_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    // Power-off VM
    suspend fun powerOffVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.POWER_OFF_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }
    // Resume VM
    suspend fun resumeVmInstanceWithResult(id: Int): ApiResult<Any>{
        return manager.callWithResult(
            method = ApiMethods.Vm.RESUME_VM,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    // Clone VM
    suspend fun cloneVmInstanceWithResult(id: Int, cloneName :String? = null): ApiResult<Boolean> {
        return manager.callWithResult(
            method = ApiMethods.Vm.CLONE_VM,
            params = listOf(id,cloneName),
            resultType = Boolean::class.java
        )
    }
    // Get Memory usage
    suspend fun getVmMemoryUsageWithResult(id: Int): ApiResult<Int>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_VM_MEMORY_USAGE,
            params = listOf(id),
            resultType = Int::class.java
        )
    }

    // Delete VM
    suspend fun deleteVmInstanceWithResult(id: Int,deleteZvols: Boolean? = false, forceDelete: Boolean? = false): ApiResult<Boolean>{
        return manager.callWithResult(
            method = ApiMethods.Vm.DELETE_INSTANCE,
            params = listOf(id, Vm.DeleteOptions(deleteZvols,forceDelete)),
            resultType = Boolean::class.java
        )
    }

    /**
     * Get vm Display
     * responds with:
     * @see Vm.VmDisplayUriQueryResponse
     */

    suspend fun getVmDisplayWithResult(id: Int): ApiResult<Vm.VmDisplayUriQueryResponse>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_DISPLAY_URL,
            params = listOf(id),
            resultType = Any::class.java
        )
    }

    /**
     * [Get VM Status]
     * Responds with
     * @see Vm.VmStatus
     */
    suspend fun getVmInstanceStatusWithResult(id: Int): ApiResult<Vm.VmStatus>{
        return manager.callWithResult(
            method = ApiMethods.Vm.GET_VM_STATUS,
            params = listOf(id),
            resultType = Vm.VmStatus::class.java
        )
    }

}