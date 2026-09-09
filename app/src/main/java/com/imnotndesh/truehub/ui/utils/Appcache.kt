package com.imnotndesh.truehub.ui.utils

import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.data.models.Vm
import com.imnotndesh.truehub.data.helpers.PersistentCache
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppCache {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var serverId: String? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Points the cache at a server and hydrates the in-memory flows from disk. */
    fun bindServer(context: Context, serverId: String) {
        appContext = context.applicationContext
        this.serverId = serverId
        hydrate(serverId)
    }

    private fun hydrate(serverId: String) = scope.launch {
        val ctx = appContext ?: return@launch
        _cachedApps.value = PersistentCache.loadApps(ctx, serverId)
        _cachedMarketplaceApps.value = PersistentCache.loadMarketplace(ctx, serverId)
        _cachedPools.value = PersistentCache.loadPools(ctx, serverId)
        _cachedDisks.value = PersistentCache.loadDisks(ctx, serverId)
        _cachedSmbShares.value = PersistentCache.loadSmbShares(ctx, serverId)
        _cachedNfsShares.value = PersistentCache.loadNfsShares(ctx, serverId)
        _cachedContainers.value = PersistentCache.loadContainers(ctx, serverId)
        _cachedVms.value = PersistentCache.loadVms(ctx, serverId)
        _cachedServices.value = PersistentCache.loadServices(ctx, serverId)
        _cachedSystemUpdateVersions.value = PersistentCache.loadUpdateVersions(ctx, serverId)
        PersistentCache.loadSystemInfo(ctx, serverId)?.let { _cachedSystemInfo.value = it }
    }

    private fun persist(block: suspend (Context, String) -> Unit) {
        val ctx = appContext ?: return
        val id = serverId ?: return
        scope.launch { runCatching { block(ctx, id) } }
    }

    private val _cachedApps = MutableStateFlow<List<Apps.AppQueryResponse>>(emptyList())
    val cachedApps: StateFlow<List<Apps.AppQueryResponse>> = _cachedApps.asStateFlow()
    private val _cachedSystemUpdateVersions = MutableStateFlow<List<System.UpdateAvailableVersionsResponse>>(emptyList())
    val cachedUpdateVersions : StateFlow<List<System.UpdateAvailableVersionsResponse>> = _cachedSystemUpdateVersions.asStateFlow()

    private val _cachedSystemInfo = MutableStateFlow<System.SystemInfo?>(null)
    val cachedSystemInfo: StateFlow<System.SystemInfo?> = _cachedSystemInfo.asStateFlow()

    private val _cachedMarketplaceApps = MutableStateFlow<List<Apps.AppAvailableItem>>(emptyList())
    val cachedMarketplaceApps: StateFlow<List<Apps.AppAvailableItem>> = _cachedMarketplaceApps.asStateFlow()

    private val _cachedPools = MutableStateFlow<List<System.Pool>>(emptyList())
    val cachedPools: StateFlow<List<System.Pool>> = _cachedPools.asStateFlow()

    private val _cachedDisks = MutableStateFlow<List<System.DiskDetails>>(emptyList())
    val cachedDisks: StateFlow<List<System.DiskDetails>> = _cachedDisks.asStateFlow()

    private val _cachedSmbShares = MutableStateFlow<List<Shares.SmbShare>>(emptyList())
    val cachedSmbShares: StateFlow<List<Shares.SmbShare>> = _cachedSmbShares.asStateFlow()

    private val _cachedNfsShares = MutableStateFlow<List<Shares.NfsShare>>(emptyList())
    val cachedNfsShares: StateFlow<List<Shares.NfsShare>> = _cachedNfsShares.asStateFlow()

    private val _cachedContainers = MutableStateFlow<List<Virt.ContainerResponse>>(emptyList())
    val cachedContainers: StateFlow<List<Virt.ContainerResponse>> = _cachedContainers.asStateFlow()
    private val _cachedVms = MutableStateFlow<List<Vm.VmQueryResponse>>(emptyList())
    val cachedVms: StateFlow<List<Vm.VmQueryResponse>> = _cachedVms.asStateFlow()

    private val _cachedServices = MutableStateFlow<List<System.ServiceQueryResponse>>(emptyList())
    val cachedServices: StateFlow<List<System.ServiceQueryResponse>> = _cachedServices.asStateFlow()

    fun updateApps(apps: List<Apps.AppQueryResponse>) {
        _cachedApps.value = apps
        persist { ctx, id -> PersistentCache.saveApps(ctx, id, apps) }
    }
    fun updateSystemUpdateVersions(versions : List<System.UpdateAvailableVersionsResponse>){
        _cachedSystemUpdateVersions.value = versions
        persist { ctx, id -> PersistentCache.saveUpdateVersions(ctx, id, versions) }
    }

    fun updateSystemInfo(info: System.SystemInfo) {
        _cachedSystemInfo.value = info
        persist { ctx, id -> PersistentCache.saveSystemInfo(ctx, id, info) }
    }

    fun updateMarketplaceApps(apps : List<Apps.AppAvailableItem>){
        _cachedMarketplaceApps.value = apps
        persist { ctx, id -> PersistentCache.saveMarketplace(ctx, id, apps) }
    }

    fun updatePools(pools: List<System.Pool>) {
        _cachedPools.value = pools
        persist { ctx, id -> PersistentCache.savePools(ctx, id, pools) }
    }

    fun updateDisks(disks: List<System.DiskDetails>) {
        _cachedDisks.value = disks
        persist { ctx, id -> PersistentCache.saveDisks(ctx, id, disks) }
    }

    fun updateSmbShares(shares: List<Shares.SmbShare>) {
        _cachedSmbShares.value = shares
        persist { ctx, id -> PersistentCache.saveSmbShares(ctx, id, shares) }
    }

    fun updateNfsShares(shares: List<Shares.NfsShare>) {
        _cachedNfsShares.value = shares
        persist { ctx, id -> PersistentCache.saveNfsShares(ctx, id, shares) }
    }

    fun updateContainers(containers: List<Virt.ContainerResponse>) {
        _cachedContainers.value = containers
        persist { ctx, id -> PersistentCache.saveContainers(ctx, id, containers) }
    }

    fun updateVms(vms: List<Vm.VmQueryResponse>) {
        _cachedVms.value = vms
        persist { ctx, id -> PersistentCache.saveVms(ctx, id, vms) }
    }

    fun updateServices(services: List<System.ServiceQueryResponse>) {
        _cachedServices.value = services
        persist { ctx, id -> PersistentCache.saveServices(ctx, id, services) }
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
        _cachedServices.value = emptyList()
        _cachedMarketplaceApps.value = emptyList()
        _cachedSystemUpdateVersions.value = emptyList()
        val ctx = appContext
        val id = serverId
        if (ctx != null && id != null) scope.launch { runCatching { PersistentCache.clearServer(ctx, id) } }
    }
}