package com.imnotndesh.truehub.ui.services.containers.create

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

data class ContainerCreateUiState(
    val imageChoices: List<Virt.ImageChoice> = emptyList(),
    val poolChoices: Map<String, String> = emptyMap(),
    val isLoadingChoices: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val createdJobId: Int? = null
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

    fun loadChoices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChoices = true, error = null)
            val imagesResult = manager.virtService.getVirtImageChoicesWithResult()
            val poolsResult = manager.virtService.getGlobalPoolChoicesWithResult()
            val images = (imagesResult as? ApiResult.Success)?.data.orEmpty()
                .filter { choice -> choice.instance_types.any { it == "CONTAINER" } }
            val pools = (poolsResult as? ApiResult.Success)?.data.orEmpty()
            val error = listOfNotNull(
                (imagesResult as? ApiResult.Error)?.message,
                (poolsResult as? ApiResult.Error)?.message
            ).firstOrNull()
            _uiState.value = ContainerCreateUiState(
                imageChoices = images,
                poolChoices = pools,
                isLoadingChoices = false,
                error = error
            )
        }
    }

    fun create(
        name: String,
        image: Virt.ImageChoice?,
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
            image == null -> "Select an image"
            pool.isBlank() -> "Select a storage pool"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(error = validationError)
            return
        }
        val selectedImage = requireNotNull(image)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            val payload = buildCreatePayload(
                name = normalizedName,
                image = selectedImage,
                pool = pool,
                cpu = cpu,
                memory = memory,
                rootDiskSize = rootDiskSize,
                rootDiskIoBus = rootDiskIoBus,
                autostart = autostart,
                privileged = privileged
            )
            when (val result = manager.virtService.createInstanceWithResult(payload)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    createdJobId = result.data
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.message
                )
                ApiResult.Loading -> _uiState.value = _uiState.value.copy(isSaving = true)
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

internal fun buildCreatePayload(
    name: String,
    image: Virt.ImageChoice,
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
        put("image", image.label)
        put("pool", pool)
        put("remote", "LINUX_CONTAINERS")
        put("instance_type", "CONTAINER")
        put("autostart", autostart)
        put("privileged", privileged)
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
