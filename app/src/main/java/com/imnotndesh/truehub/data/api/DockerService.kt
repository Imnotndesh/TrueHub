package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Docker
import com.squareup.moshi.Types

class DockerService(private val manager: TrueNASApiManager) {

    suspend fun getConfig(): ApiResult<Docker.DockerEntry> {
        return manager.callWithResult(ApiMethods.Docker.CONFIG, listOf(), Docker.DockerEntry::class.java)
    }

    suspend fun getStatus(): ApiResult<Docker.Status> {
        return manager.callWithResult(ApiMethods.Docker.STATUS, listOf(), Docker.Status::class.java)
    }

    suspend fun updateConfig(update: Docker.Update): ApiResult<Docker.DockerEntry> {
        return manager.callWithResult(ApiMethods.Docker.UPDATE, listOf(update), Docker.DockerEntry::class.java)
    }

    suspend fun queryNetworks(filters: List<Any> = emptyList()): ApiResult<List<Docker.Network>> {
        val type = Types.newParameterizedType(List::class.java, Docker.Network::class.java)
        return manager.callWithResult(ApiMethods.Docker.NETWORK_QUERY, listOf(filters), type)
    }

    suspend fun getNetwork(id: String): ApiResult<Docker.Network> {
        return manager.callWithResult(ApiMethods.Docker.NETWORK_GET_INSTANCE, listOf(id), Docker.Network::class.java)
    }

    suspend fun isNvidiaPresent(): ApiResult<Boolean> {
        return manager.callWithResult(ApiMethods.Docker.NVIDIA_PRESENT, listOf(), Boolean::class.java)
    }

    suspend fun backup(backupName: String): ApiResult<String> {
        return manager.callWithResult(ApiMethods.Docker.BACKUP, listOf(backupName), String::class.java)
    }

    suspend fun backupToPool(targetPool: String): ApiResult<Unit> {
        return manager.callWithResult(ApiMethods.Docker.BACKUP_TO_POOL, listOf(targetPool), Unit::class.java)
    }

    suspend fun listBackups(): ApiResult<Map<String, Docker.Backup>> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Docker.Backup::class.java)
        return manager.callWithResult(ApiMethods.Docker.LIST_BACKUPS, listOf(), type)
    }

    suspend fun deleteBackup(backupName: String): ApiResult<Unit> {
        return manager.callWithResult(ApiMethods.Docker.DELETE_BACKUP, listOf(backupName), Unit::class.java)
    }

    suspend fun restoreBackup(backupName: String): ApiResult<Unit> {
        return manager.callWithResult(ApiMethods.Docker.RESTORE_BACKUP, listOf(backupName), Unit::class.java)
    }
}
