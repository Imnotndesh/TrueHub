@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.imnotndesh.truehub.ui.services.containers.edit
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.imePadding

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
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
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
import com.imnotndesh.truehub.ui.services.containers.BYTES_PER_MB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerEditScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ContainerEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var autostart by rememberSaveable { mutableStateOf(false) }
    var cpu by rememberSaveable { mutableStateOf("") }
    var memory by rememberSaveable { mutableStateOf("") }
    var vncEnabled by rememberSaveable { mutableStateOf(false) }
    var vncPort by rememberSaveable { mutableStateOf("") }
    var vncPassword by rememberSaveable { mutableStateOf("") }
    var secureBoot by rememberSaveable { mutableStateOf(false) }
    var rootDiskSize by rememberSaveable { mutableStateOf("") }
    var rootDiskIoBus by remember { mutableStateOf<Virt.RootDiskIOBus?>(null) }
    var showIoBusPicker by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val vncPortValue = vncPort.trim().toIntOrNull()
    val vncPortError = if (vncEnabled && vncPort.isNotBlank() && (vncPortValue == null || vncPortValue !in 5900..65535)) {
        "Port must be between 5900 and 65535"
    } else {
        null
    }

    LaunchedEffect(state.container?.id) {
        state.container?.let { container ->
            autostart = container.autostart
            cpu = container.cpu.orEmpty()
            memory = container.memory?.let { (it / BYTES_PER_MB).toString() }.orEmpty()
            vncEnabled = container.vnc_enabled
            vncPort = container.vnc_port?.toString().orEmpty()
            secureBoot = container.secure_boot == true
            rootDiskSize = container.root_disk_size?.toString().orEmpty()
            rootDiskIoBus = container.root_disk_io_bus
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    fun submit() {
        if (state.container != null && !state.isSaving) {
            viewModel.save(
                autostart = autostart,
                cpu = cpu,
                memory = memory,
                vncEnabled = vncEnabled,
                vncPort = vncPort,
                vncPassword = vncPassword,
                secureBoot = secureBoot,
                rootDiskSize = rootDiskSize,
                rootDiskIoBus = rootDiskIoBus
            )
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = state.container?.name ?: "Edit container",
                subtitle = "Container configuration",
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                error = state.error,
                onDismissError = viewModel::load,
                manager = manager,
                onBackPressed = onNavigateBack
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
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { submit() },
                        enabled = !state.isSaving && vncPortError == null,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Save")
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
                    val job = state.saveJob
                    JobProgressView(
                        title = "Saving container",
                        description = job?.progress?.description?.takeIf { it.isNotBlank() }
                            ?: "Applying configuration changes",
                        progress = job?.progress?.percent ?: 0,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.isLoading && state.container == null -> LoadingScreen("Loading Container")
                state.container == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.error ?: "Container is unavailable", color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::load, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    PullToRefreshContent(
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            ExpressiveSection(title = "General", icon = Icons.Default.Settings) {
                                ExpressiveInfoCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        ExpressiveToggleRow(
                                            title = "Autostart",
                                            description = "Start the container when TrueNAS starts",
                                            checked = autostart,
                                            onCheckedChange = { autostart = it }
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            ExpressiveTextField(
                                                value = cpu,
                                                onValueChange = { cpu = it },
                                                label = "CPU",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExpressiveTextField(
                                                value = memory,
                                                onValueChange = { memory = it },
                                                label = "Memory MB",
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            ExpressiveSection(title = "Root Disk", icon = Icons.Default.Storage) {
                                ExpressiveInfoCard {
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

                            ExpressiveSection(title = "Remote Access", icon = Icons.Default.DesktopWindows) {
                                ExpressiveInfoCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        ExpressiveToggleRow(
                                            title = "VNC",
                                            description = "Enable remote graphical access",
                                            checked = vncEnabled,
                                            onCheckedChange = { vncEnabled = it }
                                        )
                                        if (vncEnabled) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                ExpressiveTextField(
                                                    value = vncPort,
                                                    onValueChange = { vncPort = it },
                                                    label = "VNC port",
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    supportingText = vncPortError?.let { message ->
                                                        { Text(message) }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                ExpressiveTextField(
                                                    value = vncPassword,
                                                    onValueChange = { vncPassword = it },
                                                    label = "VNC password",
                                                    visualTransformation = if (passwordVisible) {
                                                        VisualTransformation.None
                                                    } else {
                                                        PasswordVisualTransformation()
                                                    },
                                                    trailingIcon = {
                                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                            Icon(
                                                                imageVector = if (passwordVisible) {
                                                                    Icons.Default.VisibilityOff
                                                                } else {
                                                                    Icons.Default.Visibility
                                                                },
                                                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            ExpressiveSection(title = "Boot", icon = Icons.Default.Security) {
                                ExpressiveInfoCard {
                                    ExpressiveToggleRow(
                                        title = "Secure boot",
                                        description = "Enable secure boot when supported",
                                        checked = secureBoot,
                                        onCheckedChange = { secureBoot = it }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
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
}
