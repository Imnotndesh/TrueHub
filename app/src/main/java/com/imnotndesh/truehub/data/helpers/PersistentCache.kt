package com.imnotndesh.truehub.data.helpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.data.models.Shares
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.data.models.Vm
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first

private const val CACHE_DATASTORE = "persistent_cache"
val Context.cacheDataStore: DataStore<Preferences> by preferencesDataStore(name = CACHE_DATASTORE)

/**
 * Disk-backed, per-server cache for slow-changing server data.
 *
 * The app reads [AppCache] StateFlows first (which hydrate from here), then refreshes over the
 * network; this store makes that cache survive process death and new launches. Writes are
 * namespaced by server id so switching instances never mixes data.
 */
object PersistentCache {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private fun <T> listAdapter(cls: Class<T>) =
        moshi.adapter<List<T>>(Types.newParameterizedType(List::class.java, cls))

    private val appsAdapter = listAdapter(Apps.AppQueryResponse::class.java)
    private val marketplaceAdapter = listAdapter(Apps.AppAvailableItem::class.java)
    private val poolsAdapter = listAdapter(System.Pool::class.java)
    private val disksAdapter = listAdapter(System.DiskDetails::class.java)
    private val smbAdapter = listAdapter(Shares.SmbShare::class.java)
    private val nfsAdapter = listAdapter(Shares.NfsShare::class.java)
    private val containersAdapter = listAdapter(Virt.ContainerResponse::class.java)
    private val vmsAdapter = listAdapter(Vm.VmQueryResponse::class.java)
    private val servicesAdapter = listAdapter(System.ServiceQueryResponse::class.java)
    private val updateVersionsAdapter = listAdapter(System.UpdateAvailableVersionsResponse::class.java)
    private val systemInfoAdapter = moshi.adapter(System.SystemInfo::class.java)

    private fun key(serverId: String, name: String) = stringPreferencesKey("${serverId}_$name")

    private suspend fun write(context: Context, serverId: String, name: String, json: String) {
        context.cacheDataStore.edit { it[key(serverId, name)] = json }
    }

    private suspend fun read(context: Context, serverId: String, name: String): String? =
        context.cacheDataStore.data.first()[key(serverId, name)]

    suspend fun saveApps(context: Context, serverId: String, value: List<Apps.AppQueryResponse>) =
        write(context, serverId, "apps", appsAdapter.toJson(value))

    suspend fun loadApps(context: Context, serverId: String): List<Apps.AppQueryResponse> =
        decode(read(context, serverId, "apps"), appsAdapter)

    suspend fun saveMarketplace(context: Context, serverId: String, value: List<Apps.AppAvailableItem>) =
        write(context, serverId, "marketplace", marketplaceAdapter.toJson(value))

    suspend fun loadMarketplace(context: Context, serverId: String): List<Apps.AppAvailableItem> =
        decode(read(context, serverId, "marketplace"), marketplaceAdapter)

    suspend fun savePools(context: Context, serverId: String, value: List<System.Pool>) =
        write(context, serverId, "pools", poolsAdapter.toJson(value))

    suspend fun loadPools(context: Context, serverId: String): List<System.Pool> =
        decode(read(context, serverId, "pools"), poolsAdapter)

    suspend fun saveDisks(context: Context, serverId: String, value: List<System.DiskDetails>) =
        write(context, serverId, "disks", disksAdapter.toJson(value))

    suspend fun loadDisks(context: Context, serverId: String): List<System.DiskDetails> =
        decode(read(context, serverId, "disks"), disksAdapter)

    suspend fun saveSmbShares(context: Context, serverId: String, value: List<Shares.SmbShare>) =
        write(context, serverId, "smb", smbAdapter.toJson(value))

    suspend fun loadSmbShares(context: Context, serverId: String): List<Shares.SmbShare> =
        decode(read(context, serverId, "smb"), smbAdapter)

    suspend fun saveNfsShares(context: Context, serverId: String, value: List<Shares.NfsShare>) =
        write(context, serverId, "nfs", nfsAdapter.toJson(value))

    suspend fun loadNfsShares(context: Context, serverId: String): List<Shares.NfsShare> =
        decode(read(context, serverId, "nfs"), nfsAdapter)

    suspend fun saveContainers(context: Context, serverId: String, value: List<Virt.ContainerResponse>) =
        write(context, serverId, "containers", containersAdapter.toJson(value))

    suspend fun loadContainers(context: Context, serverId: String): List<Virt.ContainerResponse> =
        decode(read(context, serverId, "containers"), containersAdapter)

    suspend fun saveVms(context: Context, serverId: String, value: List<Vm.VmQueryResponse>) =
        write(context, serverId, "vms", vmsAdapter.toJson(value))

    suspend fun loadVms(context: Context, serverId: String): List<Vm.VmQueryResponse> =
        decode(read(context, serverId, "vms"), vmsAdapter)

    suspend fun saveServices(context: Context, serverId: String, value: List<System.ServiceQueryResponse>) =
        write(context, serverId, "services", servicesAdapter.toJson(value))

    suspend fun loadServices(context: Context, serverId: String): List<System.ServiceQueryResponse> =
        decode(read(context, serverId, "services"), servicesAdapter)

    suspend fun saveUpdateVersions(context: Context, serverId: String, value: List<System.UpdateAvailableVersionsResponse>) =
        write(context, serverId, "update_versions", updateVersionsAdapter.toJson(value))

    suspend fun loadUpdateVersions(context: Context, serverId: String): List<System.UpdateAvailableVersionsResponse> =
        decode(read(context, serverId, "update_versions"), updateVersionsAdapter)

    suspend fun saveSystemInfo(context: Context, serverId: String, value: System.SystemInfo) =
        write(context, serverId, "system_info", systemInfoAdapter.toJson(value))

    suspend fun loadSystemInfo(context: Context, serverId: String): System.SystemInfo? {
        val json = read(context, serverId, "system_info") ?: return null
        return runCatching { systemInfoAdapter.fromJson(json) }.getOrNull()
    }

    suspend fun clearServer(context: Context, serverId: String) {
        context.cacheDataStore.edit { prefs ->
            prefs.asMap().keys
                .filter { it.name.startsWith("${serverId}_") }
                .forEach { prefs.remove(it) }
        }
    }

    private fun <T> decode(json: String?, adapter: com.squareup.moshi.JsonAdapter<List<T>>): List<T> =
        if (json == null) emptyList() else runCatching { adapter.fromJson(json) }.getOrNull() ?: emptyList()
}
