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
        data class Success(val rootNode: Storage.ZfsDataset?) : UiState() // Modified to hold rootNode
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedDataset = MutableStateFlow<Storage.ZfsDataset?>(null)
    val selectedDataset: StateFlow<Storage.ZfsDataset?> = _selectedDataset.asStateFlow()

    val expandedNodeIds = mutableStateListOf<String>()

    fun loadDatasets(poolName: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = manager.storage.getAllDatasets()
                when(result){
                    is ApiResult.Loading -> {}
                    is ApiResult.Error ->{
                        _uiState.value = UiState.Error(result.message)
                    }
                    is ApiResult.Success<*> -> {
                        val flatList = result.data as List<Storage.ZfsDataset>
                        val root = buildTree(poolName, flatList)
                        if (root != null && !expandedNodeIds.contains(root.id)) {
                            expandedNodeIds.add(root.id)
                        }

                        _uiState.value = UiState.Success(root)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Failed to load datasets")
            }
        }
    }

    // Recursive helper to build the hierarchy
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