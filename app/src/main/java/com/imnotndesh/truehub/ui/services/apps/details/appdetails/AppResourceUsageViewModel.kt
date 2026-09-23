package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.AppStatsRepository
import com.imnotndesh.truehub.data.models.AppStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val BOOTSTRAP_INTERVAL = 2
private const val DEFAULT_INTERVAL = 10
private const val SETTLE_DELAY_MS = 5000L

data class AppResourceUsageUiState(
    val isLoading: Boolean = true,
    val isLive: Boolean = false,
    val current: AppStats? = null,
    val history: List<AppStats> = emptyList(),
    val intervalSeconds: Int = DEFAULT_INTERVAL,
    val error: String? = null
)

@HiltViewModel
class AppResourceUsageViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appId: String = savedStateHandle.get<String>("appId").orEmpty()
    private val repository = AppStatsRepository(manager)
    private val interval = MutableStateFlow(BOOTSTRAP_INTERVAL)
    private val restart = MutableStateFlow(0)
    private val history = mutableListOf<AppStats>()
    private var intervalChosen = false
    private var streamJob: Job? = null
    private var settleJob: Job? = null

    private val _uiState = MutableStateFlow(AppResourceUsageUiState())
    val uiState: StateFlow<AppResourceUsageUiState> = _uiState.asStateFlow()

    fun setActive(active: Boolean) {
        if (active) {
            if (streamJob?.isActive == true) return
            streamJob = viewModelScope.launch {
                combine(interval, restart) { seconds, _ -> seconds }
                    .flatMapLatest { seconds -> stream(seconds) }
                    .collect { append(it) }
            }
        } else {
            streamJob?.cancel()
            streamJob = null
            settleJob?.cancel()
            settleJob = null
            _uiState.update { it.copy(isLive = false) }
        }
    }

    private fun stream(seconds: Int) =
        repository.observe(appId, seconds)
            .onStart {
                _uiState.update { it.copy(error = null) }
                if (_uiState.value.history.isEmpty()) {
                    _uiState.update { it.copy(isLoading = true) }
                    scheduleSettled()
                }
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLive = false,
                        error = throwable.message ?: "Failed to load app usage"
                    )
                }
            }

    private fun append(stat: AppStats) {
        val updated = (history + stat).takeLast(AppStatsRepository.DEFAULT_CAPACITY)
        history.clear()
        history.addAll(updated)
        settleJob?.cancel()
        settleJob = null
        _uiState.update {
            it.copy(isLoading = false, isLive = true, current = stat, history = updated)
        }
        if (!intervalChosen && interval.value != DEFAULT_INTERVAL) {
            interval.value = DEFAULT_INTERVAL
        }
    }

    private fun scheduleSettled() {
        if (settleJob?.isActive == true) return
        settleJob = viewModelScope.launch {
            delay(SETTLE_DELAY_MS)
            if (_uiState.value.history.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isLive = false) }
            }
            settleJob = null
        }
    }

    fun setInterval(seconds: Int) {
        intervalChosen = true
        interval.value = seconds
        _uiState.update { it.copy(intervalSeconds = seconds) }
    }

    fun retry() {
        restart.update { it + 1 }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

}
