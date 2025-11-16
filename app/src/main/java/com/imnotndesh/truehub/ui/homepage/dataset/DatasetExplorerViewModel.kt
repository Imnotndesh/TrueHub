package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DatasetExplorerViewModel(private val manager: TrueNASApiManager) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        // LoadingWithCache is a new state to show the user data is visible but being refreshed
        data class LoadingWithCache(val rootNode: Storage.ZfsDataset?) : UiState()
        data class Success(val rootNode: Storage.ZfsDataset?) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 1. Private cache to hold the last successfully loaded root dataset
    private var _cache: Storage.ZfsDataset? = null

    private val _selectedDataset = MutableStateFlow<Storage.ZfsDataset?>(null)
    val selectedDataset: StateFlow<Storage.ZfsDataset?> = _selectedDataset.asStateFlow()

    val expandedNodeIds = mutableStateListOf<String>()

    /**
     * Public function to kick off the dataset loading/refresh process.
     * Tries to show cache immediately, then refreshes.
     */
    fun loadDatasets(poolName: String) {
        // If cache exists, show it immediately and launch a background refresh
        if (_cache != null) {
            _uiState.value = UiState.LoadingWithCache(_cache)
            refreshDatasets(poolName, updateIfCached = true)
        } else {
            // Otherwise, show full loading and wait for the result
            _uiState.value = UiState.Loading
            refreshDatasets(poolName)
        }
    }

    /**
     * 2. Core function to fetch data and update the state and cache.
     * @param poolName The name of the root pool to build the tree from.
     * @param updateIfCached Set to true if the UI should update even if a cache is currently shown (for background refresh).
     */
    fun refreshDatasets(poolName: String, updateIfCached: Boolean = false) {
        viewModelScope.launch {
            // Only show Loading state if there is no cache being displayed.
            if (!updateIfCached) {
                _uiState.value = UiState.Loading
            }

            try {
                val result = manager.storage.getAllDatasets()
                when(result){
                    is ApiResult.Loading -> {} // Ignore loading state from API
                    is ApiResult.Error -> {
                        val message = result.message
                        // Show error only if there's no cache, otherwise keep showing cache
                        if (_cache == null || !updateIfCached) {
                            _uiState.value = UiState.Error(message)
                        } else {
                            // If a background refresh fails, log it and keep showing old cache
                            println("Background refresh failed: $message")
                            _uiState.value = UiState.Success(_cache)
                        }
                    }
                    is ApiResult.Success<*> -> {
                        val flatList = result.data as List<Storage.ZfsDataset>
                        val root = buildTree(poolName, flatList)

                        if (root != null) {
                            // Update cache
                            _cache = root
                            // Ensure the root node is expanded after first load/refresh
                            if (!expandedNodeIds.contains(root.id)) {
                                expandedNodeIds.add(root.id)
                            }
                        }

                        _uiState.value = UiState.Success(root)
                    }
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Failed to refresh datasets"
                if (_cache == null || !updateIfCached) {
                    _uiState.value = UiState.Error(message)
                } else {
                    println("Background refresh failed: $message")
                    _uiState.value = UiState.Success(_cache)
                }
            }
        }
    }

    fun createDataset(path: String, datasetOption: Storage.DatasetOptions, poolName: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = manager.storage.createDataset(path,datasetOption)
                when(result){
                    is ApiResult.Loading -> {}
                    is ApiResult.Error ->{
                        _uiState.value = UiState.Error(result.message)
                    }
                    is ApiResult.Success<*> -> {
                        refreshDatasets(poolName)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Failed to create dataset")
            }
        }
    }

    private fun buildTree(rootName: String, allDatasets: List<Storage.ZfsDataset>): Storage.ZfsDataset? {
        val root = allDatasets.find { it.name == rootName } ?: return null
        val prefix = "$rootName/"
        val directChildren = allDatasets.filter {
            it.name.startsWith(prefix) &&
                    !it.name.substringAfter(prefix).contains("/")
        }

        val populatedChildren = directChildren.mapNotNull { child ->
            buildTree(child.name, allDatasets)
        }
        return root.copy(children = populatedChildren)
    }

    fun toggleExpansion(nodeId: String) {
        if (expandedNodeIds.contains(nodeId)) {
            expandedNodeIds.remove(nodeId)
        } else {
            expandedNodeIds.add(nodeId)
        }
    }

    fun selectNode(dataset: Storage.ZfsDataset) {
        _selectedDataset.value = dataset
    }

    class Factory(private val manager: TrueNASApiManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DatasetExplorerViewModel(manager) as T
        }
    }
}