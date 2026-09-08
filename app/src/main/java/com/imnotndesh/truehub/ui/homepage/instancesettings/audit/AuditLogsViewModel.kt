package com.imnotndesh.truehub.ui.homepage.instancesettings.audit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class AuditLogsUiState(
    val logs: List<System.AuditQueryResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isExporting: Boolean = false,
    val exportPath: String? = null,
    val exportJobProgress: String? = null,
    val error: String? = null,
    val reportName: String? = null,
    val downloadState: DownloadState = DownloadState.Idle
)

enum class DownloadState { Idle, Generating, Ready, Downloading, Success, Error }

class AuditLogsViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditLogsUiState())
    val uiState: StateFlow<AuditLogsUiState> = _uiState.asStateFlow()

    init {
        queryLogs("MIDDLEWARE")
    }

    fun refresh() {
        val currentService = _uiState.value.logs.firstOrNull()?.service ?: "MIDDLEWARE"
        queryLogs(currentService, forceRefresh = true)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun queryLogs(service: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (forceRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val args = System.AuditQueryArgs(
                services = listOf(service),
                queryOptions = System.AuditQueryOptions(limit = 50)
            )

            when (val result = manager.system.auditQuery(args)) {
                is ApiResult.Success -> {
                    val logList = extractLogs(result.data)
                    _uiState.update {
                        it.copy(logs = logList, isLoading = false, isRefreshing = false)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.message)
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun startAuditExport(service: String, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(downloadState = DownloadState.Generating, reportName = null, error = null) }
            val args = System.AuditExportArgs(
                services = listOf(service),
                exportFormat = "CSV",
                queryOptions = System.AuditQueryOptions(limit = 200)
            )
            when (val result = manager.system.auditExport(args)) {
                is ApiResult.Success -> {
                    val jobId = result.data
                    // Track with notifications enabled
                    GlobalJobTracker.startTracking(
                        context = context.applicationContext,
                        manager = manager,
                        jobId = jobId,
                        appName = "audit_export_$service",
                        showNotif = true,          // ← enable notification
                        type = "AUDIT_EXPORT"
                    )
                    // Listen for job completion via JobRepository
                    monitorExportJob(jobId)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(downloadState = DownloadState.Error, error = result.message) }
                }
                is ApiResult.Loading -> { }
            }
        }
    }

    private suspend fun monitorExportJob(jobId: Int) {
        JobRepository.activeJobs.collect { jobs ->
            val tracked = jobs.values.find { it.jobId == jobId }

            if (tracked == null) {
                val result = manager.system.getJobInfoJobWithResult(jobId)
                if (result is ApiResult.Success) {
                    val job = result.data
                    when (job.state) {
                        "SUCCESS" -> {
                            val path = job.result as? String
                            if (path != null) {
                                val reportName = File(path).name
                                _uiState.update { it.copy(reportName = reportName, downloadState = DownloadState.Ready) }
                            } else {
                                _uiState.update { it.copy(downloadState = DownloadState.Error, error = "Export result missing path") }
                            }
                        }
                        "FAILED", "ABORTED" -> {
                            _uiState.update { it.copy(downloadState = DownloadState.Error, error = "Export ${job.state}") }
                        }
                        else -> { /* still running – repo may have missed update */ }
                    }
                }
                return@collect
            }

            // Update progress description
            _uiState.update { it.copy(exportJobProgress = tracked.description) }

            when (tracked.state) {
                "SUCCESS" -> {
                    val result = manager.system.getJobInfoJobWithResult(jobId)
                    if (result is ApiResult.Success) {
                        val job = result.data
                        val path = job.result as? String
                        if (path != null) {
                            val reportName = File(path).name
                            _uiState.update { it.copy(reportName = reportName, downloadState = DownloadState.Ready) }
                        } else {
                            _uiState.update { it.copy(downloadState = DownloadState.Error, error = "Export result missing path") }
                        }
                    }
                    JobRepository.removeJob(jobId)
                    return@collect
                }
                "FAILED", "ABORTED" -> {
                    _uiState.update { it.copy(downloadState = DownloadState.Error, error = "Export ${tracked.state}") }
                    JobRepository.removeJob(jobId)
                    return@collect
                }
                else -> { /* still running – continue collecting */ }
            }
        }
    }

    suspend fun downloadAuditReport(reportName: String, uri: Uri, contentResolver: ContentResolver) {
        _uiState.update { it.copy(downloadState = DownloadState.Downloading) }
        val downloadArgs = System.CoreDownloadArgs(
            method = "audit.download_report",
            args = listOf(mapOf("report_name" to reportName)),
            filename = reportName
        )
        when (val result = manager.system.coreDownload(downloadArgs)) {
            is ApiResult.Success -> {
                val downloadUrl = result.data.downloadUrl
                val success = withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        manager.downloadFile(downloadUrl, outputStream)
                    } ?: false
                }
                if (success) {
                    _uiState.update { it.copy(downloadState = DownloadState.Success) }
                } else {
                    _uiState.update { it.copy(downloadState = DownloadState.Error, error = "Download failed") }
                }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(downloadState = DownloadState.Error, error = result.message) }
            }
            is ApiResult.Loading -> { }
        }
    }

    fun resetAuditExport() {
        _uiState.update { it.copy(reportName = null, downloadState = DownloadState.Idle, exportPath = null) }
        viewModelScope.launch {
            _uiState.value.reportName?.let {
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractLogs(data: Any?): List<System.AuditQueryResultItem> {
        return when (data) {
            is List<*> -> {
                // If it's a list of AuditQueryResultItem maps, convert them
                data.filterIsInstance<Map<String, Any?>>().map { map ->
                    System.AuditQueryResultItem(
                        auditId = map["audit_id"],
                        messageTimestamp = (map["message_timestamp"] as? Number)?.toLong(),
                        timestamp = map["timestamp"] as? String,
                        address = map["address"] as? String,
                        username = map["username"] as? String,
                        session = map["session"],
                        service = map["service"] as? String,
                        serviceData = map["service_data"],
                        event = map["event"] as? String,
                        eventData = map["event_data"],
                        success = map["success"] as? Boolean
                    )
                }
            }
            is Map<*, *> -> {
                // Single result (if get=true was used) — wrap it
                listOf(
                    System.AuditQueryResultItem(
                        auditId = data["audit_id"],
                        messageTimestamp = (data["message_timestamp"] as? Number)?.toLong(),
                        timestamp = data["timestamp"] as? String,
                        address = data["address"] as? String,
                        username = data["username"] as? String,
                        session = data["session"],
                        service = data["service"] as? String,
                        serviceData = data["service_data"],
                        event = data["event"] as? String,
                        eventData = data["event_data"],
                        success = data["success"] as? Boolean
                    )
                )
            }
            else -> emptyList()
        }
    }

    class AuditLogsViewModelFactory(
        private val manager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuditLogsViewModel(manager) as T
        }
    }
}
