@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.imnotndesh.truehub.ui.services.containers.create
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.imePadding
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveToggleRow
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerCreateScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit
) {
    val viewModel: ContainerCreateViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Pair<String, Virt.ImageChoice>?>(null) }
    var selectedPool by rememberSaveable { mutableStateOf("") }
    var cpu by rememberSaveable { mutableStateOf("") }
    var memory by rememberSaveable { mutableStateOf("") }
    var rootDiskSize by rememberSaveable { mutableStateOf("10") }
    var rootDiskIoBus by remember { mutableStateOf<Virt.RootDiskIOBus?>(Virt.RootDiskIOBus.VIRTIO_BLK) }
    var autostart by rememberSaveable { mutableStateOf(true) }
    var privileged by rememberSaveable { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showPoolPicker by remember { mutableStateOf(false) }
    var showIoBusPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isDirty = name.isNotBlank() || selectedImage != null || selectedPool.isNotBlank() ||
        cpu.isNotBlank() || memory.isNotBlank() || rootDiskSize != "10" ||
        !autostart || privileged

    fun requestExit() {
        if (isDirty) showDiscardDialog = true else onNavigateBack()
    }

    BackHandler(enabled = isDirty) { showDiscardDialog = true }

    LaunchedEffect(state.creationCompleted) {
        if (state.creationCompleted) onCreated()
    }

    fun submit() {
        if (!state.isSaving) {
            viewModel.create(
                name = name,
                imageKey = selectedImage?.first,
                pool = selectedPool,
                cpu = cpu,
                memory = memory,
                rootDiskSize = rootDiskSize,
                rootDiskIoBus = rootDiskIoBus,
                autostart = autostart,
                privileged = privileged
            )
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Create container",
                subtitle = "New virtualized container",
                isLoading = state.isLoadingChoices || state.isSaving,
                isRefreshing = state.isRefreshing,
                error = state.error,
                onDismissError = viewModel::dismissError,
                manager = manager,
                onBackPressed = { requestExit() }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { requestExit() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { submit() },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Create")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            when {
                state.isSaving -> {
                    val job = state.creationJob
                    val failed = job?.state == "FAILED" || job?.state == "ABORTED"
                    JobProgressView(
                        title = if (failed) "Container Creation Failed" else "Creating ${name.ifBlank { "container" }}",
                        description = if (failed) {
                            "Something went wrong while creating the container"
                        } else {
                            job?.progress?.description?.takeIf { it.isNotBlank() }
                        },
                        progress = job?.progress?.percent ?: 0,
                        isFailed = failed,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.isLoadingChoices && state.imageChoices.isEmpty() && state.poolChoices.isEmpty() -> {
                    LoadingScreen("Loading container options")
                }
                else -> {
                    PullToRefreshContent(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::refreshChoices,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            ExpressiveSection(title = "Identity", icon = Icons.Default.Info) {
                                ExpressiveInfoCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        ExpressiveTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = "Container name"
                                        )
                                        ExpressiveChoiceField(
                                            label = "Image",
                                            value = selectedImage?.second?.label.orEmpty(),
                                            placeholder = "Select from server choices",
                                            onClick = { showImagePicker = true }
                                        )
                                    }
                                }
                            }

                            ExpressiveSection(title = "Resources", icon = Icons.Default.Memory) {
                                ExpressiveInfoCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        ExpressiveChoiceField(
                                            label = "Storage pool",
                                            value = state.poolChoices[selectedPool] ?: selectedPool,
                                            placeholder = "Select a pool",
                                            onClick = { showPoolPicker = true }
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            ExpressiveTextField(
                                                value = cpu,
                                                onValueChange = { cpu = it },
                                                label = "CPU",
                                                placeholder = "Optional",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExpressiveTextField(
                                                value = memory,
                                                onValueChange = { memory = it },
                                                label = "Memory MB",
                                                placeholder = "Optional",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            ExpressiveTextField(
                                                value = rootDiskSize,
                                                onValueChange = { rootDiskSize = it },
                                                label = "Root disk GB",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExpressiveChoiceField(
                                                label = "I/O bus",
                                                value = rootDiskIoBus?.name.orEmpty(),
                                                placeholder = "Select",
                                                onClick = { showIoBusPicker = true },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            ExpressiveSection(title = "Behaviour", icon = Icons.Default.Settings) {
                                ExpressiveInfoCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                        ExpressiveToggleRow(
                                            title = "Autostart",
                                            description = "Start the container when TrueNAS starts",
                                            checked = autostart,
                                            onCheckedChange = { autostart = it }
                                        )
                                        ExpressiveToggleRow(
                                            title = "Privileged",
                                            description = "Allow elevated host access for this container",
                                            checked = privileged,
                                            onCheckedChange = { privileged = it }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (showImagePicker) {
        ExpressiveChoiceSheet(
            title = "Choose image",
            choices = state.imageChoices.map { (key, choice) ->
                ExpressiveChoice(
                    key = key,
                    label = choice.label.orEmpty().ifBlank { "Unnamed image" },
                    supporting = listOfNotNull(choice.os, choice.release, choice.variant).joinToString(" · ")
                )
            },
            selectedKey = selectedImage?.first,
            onDismiss = { showImagePicker = false },
            onSelect = { key ->
                state.imageChoices[key]?.let { selectedImage = key to it }
                showImagePicker = false
            }
        )
    }

    if (showPoolPicker) {
        ExpressiveChoiceSheet(
            title = "Choose storage pool",
            choices = state.poolChoices.map { (key, value) ->
                ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
            },
            selectedKey = selectedPool.ifBlank { null },
            onDismiss = { showPoolPicker = false },
            onSelect = {
                selectedPool = it
                showPoolPicker = false
            }
        )
    }

    if (showIoBusPicker) {
        ExpressiveChoiceSheet(
            title = "Choose root disk I/O bus",
            choices = Virt.RootDiskIOBus.entries.map { ExpressiveChoice(key = it.name, label = it.name) },
            selectedKey = rootDiskIoBus?.name,
            searchable = false,
            height = 280,
            onDismiss = { showIoBusPicker = false },
            onSelect = {
                rootDiskIoBus = Virt.RootDiskIOBus.entries.firstOrNull { bus -> bus.name == it }
                showIoBusPicker = false
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard this container?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
        )
    }
}
