package com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlertClassEditState(
    val level: Alerts.AlertLevels,
    val policy: Alerts.AlertPolicies,
    val proactiveSupport: Boolean
)

data class AlertClassesConfigUiState(
    val categories: List<System.AlertCategoriesResponse> = emptyList(),
    val availablePolicies: List<String> = emptyList(),
    val edits: Map<String, AlertClassEditState> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AlertClassesConfigViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertClassesConfigUiState())
    val uiState: StateFlow<AlertClassesConfigUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val categoriesDeferred = async { manager.alertsService.listAlertCategoriesWithResult() }
            val policiesDeferred = async { manager.alertsService.listAlertPoliciesWithResult() }
            val configDeferred = async { manager.alertsService.getAlertClassesConfigWithResult() }

            val categoriesResult = categoriesDeferred.await()
            val policiesResult = policiesDeferred.await()
            val configResult = configDeferred.await()

            val categories = (categoriesResult as? ApiResult.Success)?.data ?: emptyList()
            val policies = (policiesResult as? ApiResult.Success)?.data ?: emptyList()
            val existingOverrides = (configResult as? ApiResult.Success)?.data?.classes ?: emptyMap()

            if (categoriesResult is ApiResult.Error) {
                _uiState.update { it.copy(isLoading = false, error = categoriesResult.message) }
                return@launch
            }

            val edits = mutableMapOf<String, AlertClassEditState>()
            categories.forEach { category ->
                category.classes.forEach { cls ->
                    val override = existingOverrides[cls.id]
                    val defaultLevel = runCatching { Alerts.AlertLevels.valueOf(cls.level) }
                        .getOrDefault(Alerts.AlertLevels.INFO)

                    edits[cls.id] = AlertClassEditState(
                        level = override?.level ?: defaultLevel,
                        policy = override?.policy ?: Alerts.AlertPolicies.IMMEDIATELY,
                        proactiveSupport = override?.proactive_support ?: cls.proactive_support
                    )
                }
            }

            _uiState.update {
                it.copy(
                    categories = categories,
                    availablePolicies = policies,
                    edits = edits,
                    isLoading = false
                )
            }
        }
    }

    fun updateLevel(classId: String, level: Alerts.AlertLevels) {
        _uiState.update { state ->
            val current = state.edits[classId] ?: return@update state
            state.copy(edits = state.edits + (classId to current.copy(level = level)))
        }
    }

    fun updatePolicy(classId: String, policy: Alerts.AlertPolicies) {
        _uiState.update { state ->
            val current = state.edits[classId] ?: return@update state
            state.copy(edits = state.edits + (classId to current.copy(policy = policy)))
        }
    }

    fun updateProactive(classId: String, proactive: Boolean) {
        _uiState.update { state ->
            val current = state.edits[classId] ?: return@update state
            state.copy(edits = state.edits + (classId to current.copy(proactiveSupport = proactive)))
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }

            val update = Alerts.AlertClassUpdate(
                classes = _uiState.value.edits.mapValues { (_, edit) ->
                    Alerts.AlertClassConfiguration(
                        level = edit.level,
                        policy = edit.policy,
                        proactive_support = edit.proactiveSupport
                    )
                }
            )

            when (val result = manager.alertsService.updateAlertClassesWithResult(update)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}