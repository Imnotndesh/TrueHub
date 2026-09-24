package com.imnotndesh.truehub.ui.services.containers.volumes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContainerVolumesUiState(
    val volumes: List<Virt.VolumeEntry> = emptyList(),
    val poolChoices: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ContainerVolumesViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContainerVolumesUiState())
    val uiState: StateFlow<ContainerVolumesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        loadInternal(isRefresh = false)
    }

    fun refresh() {
        loadInternal(isRefresh = true)
    }

    private fun loadInternal(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            val volumesResult = manager.virtService.queryVolumesWithResult()
            val server = manager.serverBaseHttpUrl
            val cachedPools = PoolChoiceCache.get(server)
            val poolsResult = if (!isRefresh && cachedPools != null) {
                ApiResult.Success(cachedPools)
            } else {
                manager.virtService.getGlobalPoolChoicesWithResult()
            }
            val pools = (poolsResult as? ApiResult.Success)?.data
            if (pools != null) PoolChoiceCache.put(server, pools)
            _uiState.value = ContainerVolumesUiState(
                volumes = (volumesResult as? ApiResult.Success)?.data.orEmpty(),
                poolChoices = pools.orEmpty(),
                isLoading = false,
                isRefreshing = false,
                error = listOfNotNull(
                    (volumesResult as? ApiResult.Error)?.message,
                    (poolsResult as? ApiResult.Error)?.message
                ).firstOrNull()
            )
        }
    }

    fun create(name: String, storagePool: String, contentType: String, type: String) {
        if (name.isBlank() || storagePool.isBlank() || contentType.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name, pool, and content type are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val payload = Virt.VolumeCreate(
                name = name.trim(),
                storagePool = storagePool,
                contentType = contentType,
                type = type.trim().ifBlank { null }
            )
            when (val result = manager.virtService.createVolumeWithResult(payload)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        volumes = (_uiState.value.volumes + result.data).distinctBy { it.id },
                        isSaving = false,
                        saved = true
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    fun delete(volume: Virt.VolumeEntry) {
        if (volume.usedBy.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Volume is in use by one or more instances")
            return
        }
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVolumeWithResult(volume.id)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    volumes = _uiState.value.volumes.filterNot { it.id == volume.id }
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
                ApiResult.Loading -> Unit
            }
        }
    }
}

private object PoolChoiceCache {
    private val values = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()

    fun get(server: String): Map<String, String>? = values[server]

    fun put(server: String, choices: Map<String, String>) {
        values[server] = choices
    }
}
