package com.imnotndesh.truehub.ui.homepage.instancesettings.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuditSaveResult {
    data object Success : AuditSaveResult()
    data class Error(val message: String) : AuditSaveResult()
}

data class AuditConfigUiState(
    val config: System.AuditEntry? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: AuditSaveResult? = null,
    val error: String? = null
)

@HiltViewModel
class AuditConfigViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditConfigUiState())
    val uiState: StateFlow<AuditConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    fun refresh() = loadConfig()

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = manager.system.auditConfig()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(config = result.data, isLoading = false)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun saveAuditConfig(
        retention: Int?,
        reservation: Int?,
        quota: Int?,
        quotaFillWarning: Int?,
        quotaFillCritical: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveResult = null) }

            val args = System.AuditUpdateArgs(
                retention = retention,
                reservation = reservation,
                quota = quota,
                quotaFillWarning = quotaFillWarning,
                quotaFillCritical = quotaFillCritical
            )

            when (val result = manager.system.auditUpdate(args)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            config = result.data,
                            isSaving = false,
                            saveResult = AuditSaveResult.Success
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveResult = AuditSaveResult.Error(result.message)
                        )
                    }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearSaveResult() {
        _uiState.update { it.copy(saveResult = null) }
    }
}
