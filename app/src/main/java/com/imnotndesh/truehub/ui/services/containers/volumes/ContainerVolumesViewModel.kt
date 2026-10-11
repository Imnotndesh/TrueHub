package com.imnotndesh.truehub.ui.services.containers.volumes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.services.containers.MIN_VOLUME_SIZE_MB
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
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
    val editingVolume: Virt.VolumeEntry? = null,
    val importJob: System.Job? = null,
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

    fun create(name: String, storagePool: String, sizeMb: Int) {
        if (name.isBlank() || storagePool.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name and pool are required")
            return
        }
        if (sizeMb < MIN_VOLUME_SIZE_MB) {
            _uiState.value = _uiState.value.copy(error = "Volume size must be at least $MIN_VOLUME_SIZE_MB MB")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val payload = Virt.VolumeCreate(
                name = name.trim(),
                storagePool = storagePool,
                size = sizeMb
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

    fun beginEdit(volume: Virt.VolumeEntry) {
        val id = volume.id ?: return
        viewModelScope.launch {
            when (val result = manager.virtService.getVolumeWithResult(id)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(editingVolume = result.data, error = null)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(editingVolume = volume, error = result.message)
                ApiResult.Loading -> _uiState.value = _uiState.value.copy(editingVolume = volume)
            }
        }
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(editingVolume = null)
    }

    fun resize(volumeId: String, sizeMb: Int) {
        if (sizeMb < MIN_VOLUME_SIZE_MB) {
            _uiState.value = _uiState.value.copy(error = "Volume size must be at least $MIN_VOLUME_SIZE_MB MB")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            when (val result = manager.virtService.updateVolumeWithResult(volumeId, Virt.VolumeUpdate(size = sizeMb))) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        volumes = _uiState.value.volumes.map { if (it.id == volumeId) result.data else it },
                        isSaving = false,
                        editingVolume = null,
                        saved = true
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    fun importIso(name: String, isoLocation: String, storagePool: String) {
        if (name.isBlank() || isoLocation.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name and ISO path are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, importJob = null, error = null)
            val payload = buildMap<String, Any?> {
                put("name", name.trim())
                put("iso_location", isoLocation.trim())
                put("upload_iso", false)
                storagePool.trim().takeIf { it.isNotBlank() }?.let { put("storage_pool", it) }
            }
            when (val result = manager.virtService.importIsoWithResult(payload)) {
                is ApiResult.Success -> trackImportJob(result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun trackImportJob(jobId: Int) {
        viewModelScope.launch {
            repeat(150) { attempt ->
                when (val result = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _uiState.value = _uiState.value.copy(importJob = job)
                        when (job.state) {
                            "SUCCESS" -> {
                                _uiState.value = _uiState.value.copy(isSaving = false, importJob = null, saved = true)
                                loadInternal(isRefresh = true)
                                return@launch
                            }
                            "FAILED", "ABORTED" -> {
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    importJob = null,
                                    error = job.exception?.takeIf { it.isNotBlank() }
                                        ?: "Failed to import ISO"
                                )
                                return@launch
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(isSaving = false, importJob = null, error = result.message)
                        return@launch
                    }
                    ApiResult.Loading -> Unit
                }
                if (attempt < 149) delay(2000)
            }
            _uiState.value = _uiState.value.copy(isSaving = false, importJob = null, error = "Timed out waiting for the ISO import")
        }
    }

    fun delete(volume: Virt.VolumeEntry) {
        if (volume.usedBy.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Volume is in use by one or more instances")
            return
        }
        val id = volume.id ?: return
        viewModelScope.launch {
            when (val result = manager.virtService.deleteVolumeWithResult(id)) {
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
