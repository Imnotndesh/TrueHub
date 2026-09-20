package com.imnotndesh.truehub.data.api

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.ConnectionState
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.TrueNASRpcException
import com.imnotndesh.truehub.data.helpers.SessionProvider
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.NetworkConnectivityObserver
import com.imnotndesh.truehub.data.models.Auth
import com.imnotndesh.truehub.data.models.JsonRpcEvent
import com.imnotndesh.truehub.data.models.LoginMethod
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.OutputStream
import java.lang.reflect.Type

class TrueNASApiManager(
    private val client: TrueNASClient,
    private val applicationContext: Context
) {
    private val connectivityObserver = NetworkConnectivityObserver(applicationContext)
    private val recoveryMutex = Mutex()

    val auth: AuthService by lazy { AuthService(this) }
    val system: SystemService by lazy { SystemService(this) }
    val vmService: VmService by lazy { VmService(this) }
    val apps: AppsService by lazy { AppsService(this) }
    val docker: DockerService by lazy { DockerService(this) }
    val core: CoreService by lazy { CoreService(this) }
    val cloudsync: CloudsyncService by lazy { CloudsyncService(this) }
    val pool: PoolService by lazy { PoolService(this) }
    val interfaceService: InterfaceService by lazy { InterfaceService(this) }
    val replication: ReplicationService by lazy { ReplicationService(this) }
    val cloudBackup: CloudBackupService by lazy { CloudBackupService(this) }
    val nvmet: NvmetService by lazy { NvmetService(this) }
    val certificate: CertificateService by lazy { CertificateService(this) }
    val group: GroupService by lazy { GroupService(this) }
    val keychaincredential: KeychaincredentialService by lazy { KeychaincredentialService(this) }
    val support: SupportService by lazy { SupportService(this) }
    val vmware: VmwareService by lazy { VmwareService(this) }
    val acme: AcmeService by lazy { AcmeService(this) }
    val config: ConfigService by lazy { ConfigService(this) }
    val cronjob: CronjobService by lazy { CronjobService(this) }
    val device: DeviceService by lazy { DeviceService(this) }
    val directoryservices: DirectoryservicesService by lazy { DirectoryservicesService(this) }
    val dns: DnsService by lazy { DnsService(this) }
    val enclosure: EnclosureService by lazy { EnclosureService(this) }
    val failover: FailoverService by lazy { FailoverService(this) }
    val fc: FcService by lazy { FcService(this) }
    val fcport: FcportService by lazy { FcportService(this) }
    val ftp: FtpService by lazy { FtpService(this) }
    val hardware: HardwareService by lazy { HardwareService(this) }
    val idmap: IdmapService by lazy { IdmapService(this) }
    val initshutdownscript: InitshutdownscriptService by lazy { InitshutdownscriptService(this) }
    val jbof: JbofService by lazy { JbofService(this) }
    val kmip: KmipService by lazy { KmipService(this) }
    val mail: MailService by lazy { MailService(this) }
    val nfs: NfsService by lazy { NfsService(this) }
    val privilege: PrivilegeService by lazy { PrivilegeService(this) }
    val route: RouteService by lazy { RouteService(this) }
    val rsynctask: RsynctaskService by lazy { RsynctaskService(this) }
    val smb: SmbService by lazy { SmbService(this) }
    val snmp: SnmpService by lazy { SnmpService(this) }
    val ssh: SshService by lazy { SshService(this) }
    val staticroute: StaticrouteService by lazy { StaticrouteService(this) }
    val systemdataset: SystemdatasetService by lazy { SystemdatasetService(this) }
    val tunable: TunableService by lazy { TunableService(this) }
    val update: UpdateService by lazy { UpdateService(this) }
    val ups: UpsService by lazy { UpsService(this) }
    val zfs: ZfsService by lazy { ZfsService(this) }
    val disk: DiskService by lazy { DiskService(this) }
    val reporting: ReportingService by lazy { ReportingService(this) }
    val filesystem: FilesystemService by lazy { FilesystemService(this) }
    val ipmi: IpmiService by lazy { IpmiService(this) }
    val iscsi: IscsiService by lazy { IscsiService(this) }
    val kerberos: KerberosService by lazy { KerberosService(this) }
    val virtService: VirtService by lazy { VirtService(this) }
    val sharing: SharingService by lazy { SharingService(this) }
    val connection : ConnectionService by lazy { ConnectionService(this) }
    val user : UserService by lazy { UserService(this) }
    val storage : StorageService by lazy { StorageService(this) }
    val alertsService : AlertsService by lazy { AlertsService(this) }

    val events: SharedFlow<JsonRpcEvent> get() = client.events

    val connectionState: StateFlow<ConnectionState> get() = client.connectionState

    suspend fun ensureConnected(): Boolean = client.connect()

    suspend fun subscribe(event: String): String = client.subscribe(event)

    suspend fun unsubscribe(subscriptionId: String) = client.unsubscribe(subscriptionId)

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun <T> callWithResult(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        ensureFreshSession()
        var result = client.callWithResult<T>(method, params, resultType)
        if (isAuthError(result) && recoverSession()) {
            result = client.callWithResult<T>(method, params, resultType)
        }
        return result
    }

    /** Proactively refreshes a near-expiry token so most calls never hit a 401. */
    private suspend fun ensureFreshSession() {
        val snapshot = runCatching { MultiAccountPrefs.getSessionSnapshot(applicationContext) }.getOrNull()
            ?: return
        if (!snapshot.isExpiringSoon()) return
        recoveryMutex.withLock {
            val current = MultiAccountPrefs.getSessionSnapshot(applicationContext) ?: return
            if (current.isExpiringSoon()) recoverSessionLocked(current)
        }
    }

    /**
     * Single recovery owner. Concurrent callers serialize on [recoveryMutex]; any caller that
     * arrives after a successful refresh simply reuses the newly stored token instead of
     * re-authenticating (the dedupe fix).
     */
    private suspend fun recoverSession(): Boolean {
        recoveryMutex.withLock {
            val snapshot = MultiAccountPrefs.getSessionSnapshot(applicationContext) ?: return false
            // Another caller already refreshed a still-valid token while we waited for the lock.
            if (!snapshot.isExpiringSoon(safetyFraction = 1f)) return true
            return recoverSessionLocked(snapshot)
        }
    }

    private suspend fun recoverSessionLocked(snapshot: MultiAccountPrefs.SessionSnapshot): Boolean =
        SessionProvider.recover(applicationContext, this, snapshot.serverId, snapshot.accountId)

    private fun isAuthError(result: ApiResult<*>): Boolean {
        if (result !is ApiResult.Error) return false
        if (result.throwable is TrueNASRpcException) {
            val code = (result.throwable as TrueNASRpcException).code
            if (code == 207 || code == -32001) return true
        }
        val msg = result.message.lowercase()
        return msg.contains("enotauthenticated") || msg.contains("invalid session")
    }
    suspend fun downloadFile(urlPath: String, outputStream: OutputStream): Boolean {
        return client.downloadFile(urlPath, outputStream)
    }

    suspend fun connect(): Boolean = client.connect()
    suspend fun disconnect() = client.disconnect()
    fun isConnected(): Boolean = client.getCurrentConnectionState() == ConnectionState.Connected

    /** HTTP(S) base of the server this manager is connected to, e.g. "http://192.168.1.100:80". */
    val serverBaseHttpUrl: String get() = client.baseHttpUrl
}