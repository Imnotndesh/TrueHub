package com.imnotndesh.truehub.ui.services.containers.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

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

    LaunchedEffect(state.creationCompleted) {
        if (state.creationCompleted) onCreated()
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Create container",
                subtitle = "New virtualized container",
                 isLoading = state.isLoadingChoices || state.isSaving,
                 isRefreshing = state.isRefreshing,
                 error = state.error,
                 onDismissError = { viewModel.loadChoices() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshContent(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refreshChoices,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                if (state.isLoadingChoices && state.imageChoices.isEmpty() && state.poolChoices.isEmpty()) {
                    LoadingScreen("Loading container options")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Container name") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = selectedImage?.second?.label.orEmpty(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Image") },
                            placeholder = { Text("Select from server choices") },
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = {
                                TextButton(onClick = { showImagePicker = true }) { Text("Choose") }
                            },
                            shape = RoundedCornerShape(14.dp)
                        )

                        ChoiceField(
                            label = "Storage pool",
                            value = state.poolChoices[selectedPool] ?: selectedPool,
                            placeholder = "Select a pool",
                            onClick = { showPoolPicker = true }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = cpu,
                                onValueChange = { cpu = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("CPU") },
                                placeholder = { Text("Optional") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = memory,
                                onValueChange = { memory = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Memory MB") },
                                placeholder = { Text("Optional") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = rootDiskSize,
                                onValueChange = { rootDiskSize = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Root disk GB") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            ChoiceField(
                                label = "I/O bus",
                                value = rootDiskIoBus?.name.orEmpty(),
                                placeholder = "Select",
                                onClick = { showIoBusPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        ToggleSetting(
                            title = "Autostart",
                            description = "Start the container when TrueNAS starts",
                            checked = autostart,
                            onCheckedChange = { autostart = it }
                        )
                        ToggleSetting(
                            title = "Privileged",
                            description = "Allow elevated host access for this container",
                            checked = privileged,
                            onCheckedChange = { privileged = it }
                        )

                        if (state.isSaving) {
                            val progress = state.creationJob?.progress?.percent ?: 0
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Creating container", style = MaterialTheme.typography.titleMedium)
                                    LinearProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        state.creationJob?.progress?.description ?: "Waiting for TrueNAS to finish the creation job",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ExpressiveActionFab(
                icon = Icons.Default.Add,
                label = "Create container",
                isBusy = state.isSaving,
                onClick = {
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
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }
    }

    if (showImagePicker) {
        ImageChoiceSheet(
            choices = state.imageChoices,
            selectedKey = selectedImage?.first,
            onDismiss = { showImagePicker = false },
            onSelect = { key, choice ->
                selectedImage = key to choice
                showImagePicker = false
            }
        )
    }

    if (showPoolPicker) {
        MapChoiceSheet(
            title = "Choose storage pool",
            choices = state.poolChoices,
            onDismiss = { showPoolPicker = false },
            onSelect = {
                selectedPool = it
                showPoolPicker = false
            }
        )
    }

    if (showIoBusPicker) {
        SimpleChoiceSheet(
            title = "Choose root disk I/O bus",
            choices = Virt.RootDiskIOBus.entries.map { it.name },
            onDismiss = { showIoBusPicker = false },
            onSelect = {
                rootDiskIoBus = Virt.RootDiskIOBus.entries.firstOrNull { bus -> bus.name == it }
                showIoBusPicker = false
            }
        )
    }
}

@Composable
private fun ChoiceField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            IconButtonCompat(onClick = onClick) {
                Icon(Icons.Default.ExpandMore, contentDescription = "Choose $label")
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun ToggleSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageChoiceSheet(
    choices: Map<String, Virt.ImageChoice>,
    selectedKey: String?,
    onDismiss: () -> Unit,
    onSelect: (String, Virt.ImageChoice) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = choices.entries.filter { (key, choice) ->
        query.isBlank() || key.contains(query, true) || listOf(choice.label, choice.os, choice.release, choice.variant)
            .any { value -> value?.contains(query, ignoreCase = true) == true }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Choose image", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search images") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.key }) { entry ->
                    val choice = entry.value
                    Surface(
                        onClick = { onSelect(entry.key, choice) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (entry.key == selectedKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(choice.label.orEmpty().ifBlank { "Unnamed image" }, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    listOfNotNull(choice.os, choice.release, choice.variant).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (entry.key == selectedKey) Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapChoiceSheet(
    title: String,
    choices: Map<String, String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = choices.entries.filter { (key, value) ->
        query.isBlank() || key.contains(query, true) || value.contains(query, true)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text("Search") },
                singleLine = true
            )
            LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                items(filtered, key = { it.key }) { entry ->
                    DropdownMenuItem(
                        text = { Text("${entry.key} · ${entry.value}") },
                        onClick = { onSelect(entry.key) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleChoiceSheet(
    title: String,
    choices: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice) },
                    onClick = { onSelect(choice) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun IconButtonCompat(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick, content = content)
}
