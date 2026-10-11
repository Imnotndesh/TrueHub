package com.imnotndesh.truehub.ui.services.containers.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.services.containers.BYTES_PER_MB
import com.imnotndesh.truehub.ui.services.containers.MIN_CONTAINER_MEMORY_MB
import com.imnotndesh.truehub.ui.services.containers.MIN_ROOT_DISK_GB
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContainerEditUiState(
    val container: Virt.ContainerResponse? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val saveJob: System.Job? = null,
    val error: String? = null
)

@HiltViewModel
class ContainerEditViewModel @Inject constructor(
    private val manager: TrueNASApiManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val containerId: String = savedStateHandle["containerId"] ?: ""
    private val _uiState = MutableStateFlow(ContainerEditUiState())
    val uiState: StateFlow<ContainerEditUiState> = _uiState.asStateFlow()

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
        if (containerId.isBlank()) {
            _uiState.value = ContainerEditUiState(isLoading = false, error = "Container ID is missing")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            when (val result = manager.virtService.getInstanceWithResult(containerId)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    container = result.data,
                    isLoading = false,
                    isRefreshing = false
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.message
                )
                ApiResult.Loading -> Unit
            }
        }
    }

    fun save(
        autostart: Boolean,
        cpu: String,
        memory: String,
        vncEnabled: Boolean,
        vncPort: String,
        vncPassword: String,
        secureBoot: Boolean,
        rootDiskSize: String,
        rootDiskIoBus: Virt.RootDiskIOBus?
    ) {
        val current = _uiState.value.container ?: return
        val memoryMb = memory.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        val rootDiskGb = rootDiskSize.trim().takeIf { it.isNotBlank() }?.toIntOrNull()
        val validationError = when {
            memory.trim().isNotBlank() && memoryMb == null -> "Memory must be a number of MB"
            memoryMb != null && memoryMb < MIN_CONTAINER_MEMORY_MB ->
                "Memory must be at least $MIN_CONTAINER_MEMORY_MB MB"
            rootDiskSize.trim().isNotBlank() && rootDiskGb == null ->
                "Root disk size must be a number of GB"
            rootDiskGb != null && rootDiskGb < MIN_ROOT_DISK_GB ->
                "Root disk size must be at least $MIN_ROOT_DISK_GB GB"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val update = Virt.ContainerUpdate(
                environment = current.environment,
                autostart = autostart,
                cpu = cpu.trim().takeIf { it.isNotBlank() },
                memory = memoryMb?.toLong()?.times(BYTES_PER_MB),
                vnc_port = vncPort.trim().toIntOrNull(),
                vnc_enabled = vncEnabled,
                vnc_password = vncPassword.takeIf { it.isNotBlank() },
                secure_boot = secureBoot,
                root_disk_size = rootDiskGb,
                root_disk_io_bus = rootDiskIoBus
            )
            when (val result = manager.virtService.updateVirtInstanceWithResult(containerId, update)) {
                is ApiResult.Success -> trackSaveJob(result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.message
                )
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun trackSaveJob(jobId: Int) {
        viewModelScope.launch {
            repeat(150) { attempt ->
                when (val result = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _uiState.value = _uiState.value.copy(saveJob = job)
                        when (job.state) {
                            "SUCCESS" -> {
                                _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
                                return@launch
                            }
                            "FAILED", "ABORTED" -> {
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    error = "Failed to save container: ${job.exception ?: job.state}"
                                )
                                return@launch
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "Unable to track save job: ${result.message}"
                        )
                        return@launch
                    }
                    ApiResult.Loading -> Unit
                }
                if (attempt < 149) delay(2000)
            }
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                error = "Timed out waiting for save to finish"
            )
        }
    }
}
