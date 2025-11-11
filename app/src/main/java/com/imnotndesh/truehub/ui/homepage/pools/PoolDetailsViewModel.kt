package com.imnotndesh.truehub.ui.homepage.pools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Storage
import com.imnotndesh.truehub.data.models.System.Pool
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
        val isRefreshing: Boolean = false
    ) : PoolDetailsUiState()
    data class Error(val message: String) : PoolDetailsUiState()
}

class PoolDetailsViewModel(
    private val apiManager: TrueNASApiManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PoolDetailsUiState>(PoolDetailsUiState.Loading)
    val uiState: StateFlow<PoolDetailsUiState> = _uiState.asStateFlow()

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

        getScrubTasks()
    }

    fun refreshPool() {
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