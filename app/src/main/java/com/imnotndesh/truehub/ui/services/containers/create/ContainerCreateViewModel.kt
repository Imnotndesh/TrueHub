package com.imnotndesh.truehub.ui.services.containers.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.data.models.Virt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContainerCreateUiState(
    val imageChoices: Map<String, Virt.ImageChoice> = emptyMap(),
    val poolChoices: Map<String, String> = emptyMap(),
    val isLoadingChoices: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val creationJob: System.Job? = null,
    val creationCompleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContainerCreateViewModel @Inject constructor(
    private val manager: TrueNASApiManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContainerCreateUiState())
    val uiState: StateFlow<ContainerCreateUiState> = _uiState.asStateFlow()

    init {
        loadChoices()
    }

    fun loadChoices(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val server = manager.serverBaseHttpUrl
            val cached = ContainerCreateChoiceCache.get(server)
            if (!forceRefresh && cached != null) {
                _uiState.value = _uiState.value.copy(
                    imageChoices = cached.images,
                    poolChoices = cached.pools,
                    isLoadingChoices = false,
                    isRefreshing = false
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                isLoadingChoices = !forceRefresh,
                isRefreshing = forceRefresh,
                error = null
            )
            val imagesResult = manager.virtService.getVirtImageChoicesWithResult()
            val poolsResult = manager.virtService.getGlobalPoolChoicesWithResult()
            val images = (imagesResult as? ApiResult.Success)?.data.orEmpty()
                .filterValues { choice ->
                    choice.instance_types.any { it.equals("CONTAINER", ignoreCase = true) } &&
                        !choice.label.isNullOrBlank()
                }
            val pools = (poolsResult as? ApiResult.Success)?.data.orEmpty()
            ContainerCreateChoiceCache.put(server, ContainerCreateChoices(images, pools))
            val error = listOfNotNull(
                (imagesResult as? ApiResult.Error)?.message,
                (poolsResult as? ApiResult.Error)?.message
            ).firstOrNull()
            _uiState.value = _uiState.value.copy(
                imageChoices = images,
                poolChoices = pools,
                isLoadingChoices = false,
                isRefreshing = false,
                error = error
            )
        }
    }

    fun refreshChoices() {
        loadChoices(forceRefresh = true)
    }

    fun create(
        name: String,
        imageKey: String?,
        pool: String,
        cpu: String,
        memory: String,
        rootDiskSize: String,
        rootDiskIoBus: Virt.RootDiskIOBus?,
        autostart: Boolean,
        privileged: Boolean
    ) {
        val normalizedName = name.trim()
        val validationError = when {
            normalizedName.isBlank() -> "Enter a container name"
            imageKey.isNullOrBlank() -> "Select an image"
            pool.isBlank() -> "Select a storage pool"
            memory.trim().isNotBlank() && memory.trim().toIntOrNull() == null ->
                "Memory must be a number of MB"
            rootDiskSize.trim().isNotBlank() && rootDiskSize.trim().toIntOrNull() == null ->
                "Root disk size must be a number of GB"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            return
        }
        val selectedImageKey = requireNotNull(imageKey)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                creationCompleted = false,
                creationJob = null,
                error = null
            )
            val payload = buildCreatePayload(
                name = normalizedName,
                imageKey = selectedImageKey,
                pool = pool,
                cpu = cpu,
                memory = memory,
                rootDiskSize = rootDiskSize,
                rootDiskIoBus = rootDiskIoBus,
                autostart = autostart,
                privileged = privileged
            )
            when (val result = manager.virtService.createInstanceWithResult(payload)) {
                is ApiResult.Success -> trackCreationJob(result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to submit container creation: ${result.message}"
                )
                ApiResult.Loading -> _uiState.value = _uiState.value.copy(isSaving = true)
            }
        }
    }

    private fun trackCreationJob(jobId: Int) {
        viewModelScope.launch {
            repeat(150) { attempt ->
                when (val result = manager.system.getJobInfoJobWithResult(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _uiState.value = _uiState.value.copy(creationJob = job)
                        when (job.state) {
                            "SUCCESS" -> {
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    creationCompleted = true,
                                    error = null
                                )
                                return@launch
                            }
                            "FAILED", "ABORTED" -> {
                                _uiState.value = _uiState.value.copy(
                                    isSaving = false,
                                    error = creationError(job)
                                )
                                return@launch
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "Unable to track container creation job: ${result.message}"
                        )
                        return@launch
                    }
                    ApiResult.Loading -> Unit
                }
                if (attempt < 149) delay(2000)
            }
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                error = "Timed out waiting for container creation to finish"
            )
        }
    }

    private fun creationError(job: System.Job): String {
        val detail = job.exception?.takeIf { it.isNotBlank() }
            ?: job.error?.toString()?.takeIf { it.isNotBlank() }
            ?: job.exc_info?.repr?.takeIf { it.isNotBlank() }
            ?: "Unknown error"
        return "Container creation ${job.state.lowercase()}: $detail"
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

private data class ContainerCreateChoices(
    val images: Map<String, Virt.ImageChoice>,
    val pools: Map<String, String>
)

private object ContainerCreateChoiceCache {
    private val values = ConcurrentHashMap<String, ContainerCreateChoices>()

    fun get(server: String): ContainerCreateChoices? = values[server]

    fun put(server: String, choices: ContainerCreateChoices) {
        values[server] = choices
    }
}

internal fun buildCreatePayload(
    name: String,
    imageKey: String,
    pool: String,
    cpu: String,
    memory: String,
    rootDiskSize: String,
    rootDiskIoBus: Virt.RootDiskIOBus?,
    autostart: Boolean,
    privileged: Boolean
): Map<String, Any?> {
    return buildMap {
        put("source_type", "IMAGE")
        put("name", name)
        put("image", imageKey)
        put("storage_pool", pool)
        put("remote", "LINUX_CONTAINERS")
        put("instance_type", "CONTAINER")
        put("autostart", autostart)
        put("privileged_mode", privileged)
        cpu.trim().takeIf { it.isNotBlank() }?.let { put("cpu", it) }
        memory.trim().toIntOrNull()?.let { put("memory", it) }
        rootDiskSize.trim().toIntOrNull()?.let { put("root_disk_size", it) }
        rootDiskIoBus?.let { put("root_disk_io_bus", it.wireValue()) }
    }
}

private fun Virt.RootDiskIOBus.wireValue(): String = when (this) {
    Virt.RootDiskIOBus.NVME -> "NVME"
    Virt.RootDiskIOBus.VIRTIO_BLK -> "VIRTIO-BLK"
    Virt.RootDiskIOBus.VIRTIO_SCSI -> "VIRTIO-SCSI"
}
