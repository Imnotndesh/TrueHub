package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

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
import kotlinx.coroutines.launch

data class ApiKeyListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val keys: List<System.ApiKeyEntry> = emptyList(),
    val searchQuery: String = "",
    val showMyKeys: Boolean = false,
    val showKey: Boolean = false,
    val filterRevoked: Boolean? = null,
    val filterLocal: Boolean? = null,
    val error: String? = null
) {
    val filteredKeys: List<System.ApiKeyEntry>
        get() {
            var result = keys
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                result = result.filter {
                    it.name.lowercase().contains(q) ||
                            (it.username?.lowercase()?.contains(q) == true)
                }
            }
            if (filterRevoked != null) result = result.filter { it.revoked == filterRevoked }
            if (filterLocal != null) result = result.filter { it.local == filterLocal }
            return result
        }
}

data class ApiKeyDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isResetting: Boolean = false,
    val key: System.ApiKeyEntry? = null,
    val resetResult: System.ApiKeyEntryWithKey? = null,
    val deleteResult: Boolean? = null,
    val error: String? = null
)

data class ApiKeyCreateUiState(
    val isCreating: Boolean = false,
    val createResult: System.ApiKeyEntryWithKey? = null,
    val error: String? = null
)

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {

    private val _listState = MutableStateFlow(ApiKeyListUiState())
    val listState: StateFlow<ApiKeyListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(ApiKeyDetailUiState())
    val detailState: StateFlow<ApiKeyDetailUiState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow(ApiKeyCreateUiState())
    val createState: StateFlow<ApiKeyCreateUiState> = _createState.asStateFlow()

    fun loadKeys() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, error = null)
            when (val r = manager.system.queryApiKeysWithResult()) {
                is ApiResult.Success -> _listState.value = _listState.value.copy(
                    isLoading = false, keys = r.data)
                is ApiResult.Error -> _listState.value = _listState.value.copy(
                    isLoading = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refreshKeys() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isRefreshing = true, error = null)
            when (val r = manager.system.queryApiKeysWithResult()) {
                is ApiResult.Success -> _listState.value = _listState.value.copy(
                    isRefreshing = false, keys = r.data)
                is ApiResult.Error -> _listState.value = _listState.value.copy(
                    isRefreshing = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }
    fun toggleMyKeys() {
        val newVal = !_listState.value.showMyKeys
        _listState.value = _listState.value.copy(showMyKeys = newVal)
        if (newVal) loadMyKeys() else loadKeys()
    }

    private fun loadMyKeys() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, error = null)
            when (val r = manager.system.getMyApiKeysWithResult()) {
                is ApiResult.Success -> _listState.value = _listState.value.copy(
                    isLoading = false, keys = r.data)
                is ApiResult.Error -> _listState.value = _listState.value.copy(
                    isLoading = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }




    fun setSearchQuery(q: String) { _listState.value = _listState.value.copy(searchQuery = q) }
    fun setFilterRevoked(v: Boolean?) { _listState.value = _listState.value.copy(filterRevoked = v) }
    fun setFilterLocal(v: Boolean?) { _listState.value = _listState.value.copy(filterLocal = v) }
    fun clearListError() { _listState.value = _listState.value.copy(error = null) }

    fun loadDetail(keyId: Int) {
        viewModelScope.launch {
            _detailState.value = ApiKeyDetailUiState(isLoading = true)
            when (val r = manager.system.getApiKeyInstanceWithResult(keyId)) {
                is ApiResult.Success -> _detailState.value = ApiKeyDetailUiState(key = r.data)
                is ApiResult.Error -> _detailState.value = ApiKeyDetailUiState(error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteKey(keyId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isDeleting = true, error = null)
            when (val r = manager.system.deleteApiKeyWithResult(keyId)) {
                is ApiResult.Success -> _detailState.value = _detailState.value.copy(
                    isDeleting = false, deleteResult = true)
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isDeleting = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun resetKey(keyId: Int) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isResetting = true, error = null)
            val update = System.ApiKeyUpdate(reset = true)
            when (val r = manager.system.updateApiKeyWithResult(keyId, update)) {
                is ApiResult.Success -> {
                    // Re-fetch to get updated key
                    loadDetail(keyId)
                    _detailState.value = _detailState.value.copy(isResetting = false)
                }
                is ApiResult.Error -> _detailState.value = _detailState.value.copy(
                    isResetting = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun createKey(create: System.ApiKeyCreate) {
        viewModelScope.launch {
            _createState.value = _createState.value.copy(isCreating = true, error = null)
            when (val r = manager.system.createApiKeyWithResult(create)) {
                is ApiResult.Success -> _createState.value = _createState.value.copy(
                    isCreating = false, createResult = r.data)
                is ApiResult.Error -> _createState.value = _createState.value.copy(
                    isCreating = false, error = r.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearDetailError() { _detailState.value = _detailState.value.copy(error = null) }
    fun clearCreateError() { _createState.value = _createState.value.copy(error = null) }
    fun clearCreateResult() { _createState.value = _createState.value.copy(createResult = null) }
}
