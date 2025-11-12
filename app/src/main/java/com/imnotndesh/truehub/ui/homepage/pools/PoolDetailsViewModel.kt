package com.imnotndesh.truehub.ui.homepage.pools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobState
import com.imnotndesh.truehub.data.helpers.JobTracker
import com.imnotndesh.truehub.data.models.Storage
import com.imnotndesh.truehub.data.models.System.Pool
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PoolDetailsUiState {
    object Loading : PoolDetailsUiState()
    data class Success(
        val pool: Pool,
        val scrubTasks: List<Storage.PoolScrubQueryResponse> = emptyList(),
        val isRefreshing: Boolean = false,
        val jobStates : Map<String, JobState> = emptyMap()
    ) : PoolDetailsUiState()
    data class Error(val message: String) : PoolDetailsUiState()
}

class PoolDetailsViewModel(
    private val apiManager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PoolDetailsUiState>(PoolDetailsUiState.Loading)
    val uiState: StateFlow<PoolDetailsUiState> = _uiState.asStateFlow()
    private val _jobStates = MutableStateFlow<Map<String, JobState>>(emptyMap())
    private var currentPoolId: Int? = null

    init {
        val pool = PoolDataHolder.currentPool
        if (pool != null) {
            _uiState.value = PoolDetailsUiState.Success(pool)
            currentPoolId = pool.id
            PoolDataHolder.currentPool = null
        } else {
            _uiState.value = PoolDetailsUiState.Error("Failed to load pool data. Please go back and try again.")
        }
        viewModelScope.launch {
            _jobStates.collect { jobs ->
                _uiState.update {
                    if (it is PoolDetailsUiState.Success) {
                        it.copy(jobStates = jobs)
                    } else {
                        it
                    }
                }
            }
        }

        getScrubTasks()
    }
    fun refresh(){
        getPoolDetails()
        getScrubTasks()
    }
    fun getPoolDetails() {
        val id = currentPoolId ?: run {
            _uiState.value = PoolDetailsUiState.Error("Pool ID not found. Cannot refresh.")
            return
        }

        val currentScrubTasks = (_uiState.value as? PoolDetailsUiState.Success)?.scrubTasks ?: emptyList()

        viewModelScope.launch {
            _uiState.update {
                if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }

            try {
                val result = apiManager.system.getPoolsWithResult()
                when (result) {
                    is ApiResult.Success -> {
                        val newPool = result.data.find { it.id == id }
                        if (newPool != null) {
                            _uiState.value = PoolDetailsUiState.Success(
                                pool = newPool,
                                scrubTasks = currentScrubTasks,
                                isRefreshing = false
                            )
                            currentPoolId = newPool.id
                        } else {
                            _uiState.value = PoolDetailsUiState.Error("Failed to refresh pool data.")
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(result.message)
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
    fun getScrubTasks(){
        viewModelScope.launch {
            _uiState.update {
                if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val res = apiManager.storage.getScrubTasks()
                when (res){
                    is ApiResult.Loading -> {}
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(res.message)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            if (it is PoolDetailsUiState.Success) {
                                it.copy(scrubTasks = res.data, isRefreshing = false)
                            } else {
                                it
                            }
                        }
                    }
                }
            }catch (e: Exception){
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
    fun createScrubTask(args: Storage.UpdatePoolScrubDetails) {
        viewModelScope.launch {
            _uiState.update {
                if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val result = apiManager.storage.createScrubTask(args)
                when (result) {
                    is ApiResult.Success -> {
                        getScrubTasks()
                    }
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(result.message)
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
    fun updateScrubTask(id: Int, data: Storage.UpdatePoolScrubDetails) {
        val args = Storage.UpdatePoolScrubArgs(id, data)
        viewModelScope.launch {
            _uiState.update {if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val result = apiManager.storage.updateScrubTask(args)
                when (result) {
                    is ApiResult.Success -> {
                        getScrubTasks()
                    }
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(result.message)
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
    fun runScrubTask(args :Storage.RunPoolScrubArgs){
        viewModelScope.launch {
            _uiState.update {
                if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val result = apiManager.storage.runScrubTask(args)
                when (result) {
                    is ApiResult.Success -> {
                        ToastManager.showToast("Scrub task started successfully.")
                        getScrubTasks()
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = false) else it
                        }
                        ToastManager.showToast("Scrub task started successfully.")
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
    fun switchScrubTaskState(poolName: String, scrubId: Long, action: Storage.PoolScrubAction) {
        val args = Storage.TakeActionOnPoolScrubArgs(name = poolName, action = action)
        viewModelScope.launch {
            try {
                val result = apiManager.storage.setScrubState(args)
                when (result) {
                    is ApiResult.Success -> {
                        val jobId = result.data
                        ToastManager.showToast("Action initiated for scrub task.")
                        JobTracker.pollJobStatus(
                            jobId = jobId,
                            manager = apiManager,
                            jobsStateFlow = _jobStates,
                            trackingKey = "scrub_$scrubId",
                            onComplete = { finalState ->
                                if (finalState == "SUCCESS") {
                                    getScrubTasks()
                                }else if (finalState == "FAILED"){
                                    _jobStates.update {
                                        it - "scrub_$scrubId"
                                    }
                                    ToastManager.showToast("Switching scrub tasks failed")
                                }
                            }
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(result.message)
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }


    fun deleteScrubTask(id: Int) {
        viewModelScope.launch {
            _uiState.update {
                if (it is PoolDetailsUiState.Success) it.copy(isRefreshing = true) else it
            }
            try {
                val result = apiManager.storage.deleteScrubTask(id)
                when (result) {
                    is ApiResult.Success -> {
                        if (result.data){
                            getScrubTasks()
                        }else{
                            ToastManager.showToast("Could not delete scrub task")
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = PoolDetailsUiState.Error(result.message)
                    }
                    ApiResult.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = PoolDetailsUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }



    class PoolDetailsViewModelFactory(
        private val apiManager: TrueNASApiManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PoolDetailsViewModel::class.java)) {
                return PoolDetailsViewModel(apiManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}