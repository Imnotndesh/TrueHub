package com.imnotndesh.truehub.ui.homepage.instancesettings.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditLogsUiState(
    val logs: List<System.AuditQueryResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isExporting: Boolean = false,
    val exportPath: String? = null,
    val error: String? = null
)

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

    /** Export logs. audit.export is a job that returns a file path string. */
    suspend fun exportLogs(service: String): String? {
        _uiState.update { it.copy(isExporting = true) }

        val args = System.AuditExportArgs(
            services = listOf(service),
            exportFormat = "JSON",
            queryOptions = System.AuditQueryOptions(limit = 200)
        )

        return when (val result = manager.system.auditExport(args)) {
            is ApiResult.Success -> {
                _uiState.update { it.copy(isExporting = false, exportPath = result.data) }
                result.data
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isExporting = false, error = result.message) }
                null
            }
            is ApiResult.Loading -> null
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
