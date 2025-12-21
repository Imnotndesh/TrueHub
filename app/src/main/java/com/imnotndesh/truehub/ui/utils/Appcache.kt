package com.imnotndesh.truehub.data.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppCache {
    private val _cachedApps = MutableStateFlow<List<com.imnotndesh.truehub.data.models.Apps.AppQueryResponse>>(emptyList())
    val cachedApps: StateFlow<List<com.imnotndesh.truehub.data.models.Apps.AppQueryResponse>> = _cachedApps.asStateFlow()

    private val _cachedSystemInfo = MutableStateFlow<com.imnotndesh.truehub.data.models.System.SystemInfo?>(null)
    val cachedSystemInfo: StateFlow<com.imnotndesh.truehub.data.models.System.SystemInfo?> = _cachedSystemInfo.asStateFlow()

    private val _cachedPools = MutableStateFlow<List<com.imnotndesh.truehub.data.models.System.Pool>>(emptyList())
    val cachedPools: StateFlow<List<com.imnotndesh.truehub.data.models.System.Pool>> = _cachedPools.asStateFlow()

    private val _cachedDisks = MutableStateFlow<List<com.imnotndesh.truehub.data.models.System.DiskDetails>>(emptyList())
    val cachedDisks: StateFlow<List<com.imnotndesh.truehub.data.models.System.DiskDetails>> = _cachedDisks.asStateFlow()

    private val _cachedSmbShares = MutableStateFlow<List<com.imnotndesh.truehub.data.models.Shares.SmbShare>>(emptyList())
    val cachedSmbShares: StateFlow<List<com.imnotndesh.truehub.data.models.Shares.SmbShare>> = _cachedSmbShares.asStateFlow()

    private val _cachedNfsShares = MutableStateFlow<List<com.imnotndesh.truehub.data.models.Shares.NfsShare>>(emptyList())
    val cachedNfsShares: StateFlow<List<com.imnotndesh.truehub.data.models.Shares.NfsShare>> = _cachedNfsShares.asStateFlow()

    private val _cachedContainers = MutableStateFlow<List<com.imnotndesh.truehub.data.models.Virt.ContainerResponse>>(emptyList())
    val cachedContainers: StateFlow<List<com.imnotndesh.truehub.data.models.Virt.ContainerResponse>> = _cachedContainers.asStateFlow()

    private val _cachedVms = MutableStateFlow<List<com.imnotndesh.truehub.data.models.Vm.VmQueryResponse>>(emptyList())
    val cachedVms: StateFlow<List<com.imnotndesh.truehub.data.models.Vm.VmQueryResponse>> = _cachedVms.asStateFlow()

    fun updateApps(apps: List<com.imnotndesh.truehub.data.models.Apps.AppQueryResponse>) {
        _cachedApps.value = apps
    }

    fun updateSystemInfo(info: com.imnotndesh.truehub.data.models.System.SystemInfo) {
        _cachedSystemInfo.value = info
    }

    fun updatePools(pools: List<com.imnotndesh.truehub.data.models.System.Pool>) {
        _cachedPools.value = pools
    }

    fun updateDisks(disks: List<com.imnotndesh.truehub.data.models.System.DiskDetails>) {
        _cachedDisks.value = disks
    }

    fun updateSmbShares(shares: List<com.imnotndesh.truehub.data.models.Shares.SmbShare>) {
        _cachedSmbShares.value = shares
    }

    fun updateNfsShares(shares: List<com.imnotndesh.truehub.data.models.Shares.NfsShare>) {
        _cachedNfsShares.value = shares
    }

    fun updateContainers(containers: List<com.imnotndesh.truehub.data.models.Virt.ContainerResponse>) {
        _cachedContainers.value = containers
    }

    fun updateVms(vms: List<com.imnotndesh.truehub.data.models.Vm.VmQueryResponse>) {
        _cachedVms.value = vms
    }

    fun clearAllCache() {
        _cachedApps.value = emptyList()
        _cachedSystemInfo.value = null
        _cachedPools.value = emptyList()
        _cachedDisks.value = emptyList()
        _cachedSmbShares.value = emptyList()
        _cachedNfsShares.value = emptyList()
        _cachedContainers.value = emptyList()
        _cachedVms.value = emptyList()
    }
}