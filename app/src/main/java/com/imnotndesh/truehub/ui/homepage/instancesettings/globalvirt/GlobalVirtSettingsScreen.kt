package com.imnotndesh.truehub.ui.homepage.instancesettings.globalvirt
import com.imnotndesh.truehub.ui.adaptive.InfoCardGrid

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Virt
import com.imnotndesh.truehub.ui.components.ExpressiveIconButton
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoice
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceField
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveChoiceSheet
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveDivider
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveInfoCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveSection
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField
import com.imnotndesh.truehub.ui.components.expressive.InfoRow
import com.imnotndesh.truehub.ui.components.expressive.JobProgressView
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerSheet

private const val AUTO_BRIDGE = "[AUTO]"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalVirtSettingsScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    viewModel: GlobalVirtSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Global Virt settings",
                subtitle = "Virtualization storage and networking",
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                error = state.error,
                onDismissError = viewModel::clearError,
                manager = manager,
                onBackPressed = onNavigateBack,
                trailingActions = {
                    ExpressiveIconButton(
                        onClick = { showEditor = true },
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit global Virt settings",
                        enabled = state.config != null && !state.isSaving,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val config = state.config
            when {
                state.isSaving -> {
                    JobProgressView(
                        title = "Updating global settings",
                        description = state.saveJob?.progress?.description?.takeIf { it.isNotBlank() }
                            ?: "Applying virtualization configuration",
                        progress = state.saveJob?.progress?.percent ?: 0,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.isLoading && config == null -> LoadingScreen("Loading Virt settings")
                config == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Global Virt settings are unavailable",
                            color = MaterialTheme.colorScheme.error
                        )
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
                        InfoCardGrid(modifier = Modifier.fillMaxSize()) {
                            item {
                            ExpressiveSection(title = "Storage", icon = Icons.Default.Storage) {
                                ExpressiveInfoCard {
                                    InfoRow(label = "Default pool", value = config.pool ?: "None")
                                    ExpressiveDivider()
                                    InfoRow(label = "Dataset", value = config.dataset ?: "Not configured")
                                    ExpressiveDivider()
                                    InfoRow(
                                        label = "Storage pools",
                                        value = config.storagePools?.joinToString(", ")?.ifBlank { null } ?: "None"
                                    )
                                    ExpressiveDivider()
                                    InfoRow(label = "State", value = config.state ?: "Unknown")
                                }
                            }
                            }

                            item {
                            ExpressiveSection(title = "Networking", icon = Icons.Default.Cloud) {
                                ExpressiveInfoCard {
                                    InfoRow(label = "Bridge", value = config.bridge ?: "Automatic")
                                    ExpressiveDivider()
                                    InfoRow(label = "IPv4 network", value = config.v4Network ?: "Default")
                                    ExpressiveDivider()
                                    InfoRow(label = "IPv6 network", value = config.v6Network ?: "Default")
                                    state.bridgeNetwork?.let { network ->
                                        network.ipv4Address?.let {
                                            ExpressiveDivider()
                                            InfoRow(label = "Bridge IPv4", value = it)
                                        }
                                        network.ipv4Nat?.let {
                                            ExpressiveDivider()
                                            InfoRow(label = "IPv4 NAT", value = if (it) "Enabled" else "Disabled")
                                        }
                                        network.ipv6Address?.let {
                                            ExpressiveDivider()
                                            InfoRow(label = "Bridge IPv6", value = it)
                                        }
                                        network.ipv6Nat?.let {
                                            ExpressiveDivider()
                                            InfoRow(label = "IPv6 NAT", value = if (it) "Enabled" else "Disabled")
                                        }
                                    }
                                }
                            }
                            }

                            item {
                            ExpressiveSection(title = "About", icon = Icons.Default.Info) {
                                ExpressiveInfoCard {
                                    Text(
                                        text = "These settings control where container and VM data is stored " +
                                            "and which network bridge they use. A null pool disables the " +
                                            "virtualization service; an automatic bridge is created when needed.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor && state.config != null) {
        GlobalVirtEditSheet(
            manager = manager,
            config = state.config!!,
            poolChoices = state.poolChoices,
            bridgeChoices = state.bridgeChoices,
            bridgeNetwork = state.bridgeNetwork,
            isSaving = state.isSaving,
            onBridgeSelected = viewModel::loadBridgeNetwork,
            onDismiss = { showEditor = false },
            onSave = { pool, bridge, pools, v4, v6 ->
                showEditor = false
                viewModel.update(pool, bridge, pools, v4, v6)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlobalVirtEditSheet(
    manager: TrueNASApiManager,
    config: Virt.GlobalEntry,
    poolChoices: Map<String, String>,
    bridgeChoices: Map<String, String>,
    bridgeNetwork: Virt.GlobalNetwork?,
    isSaving: Boolean,
    onBridgeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pool by rememberSaveable { mutableStateOf(config.pool.orEmpty()) }
    var bridge by rememberSaveable { mutableStateOf(config.bridge.orEmpty()) }
    var storagePools by rememberSaveable { mutableStateOf(config.storagePools?.joinToString(", ").orEmpty()) }
    var v4Network by rememberSaveable { mutableStateOf(config.v4Network.orEmpty()) }
    var v6Network by rememberSaveable { mutableStateOf(config.v6Network.orEmpty()) }
    var showPoolPicker by remember { mutableStateOf(false) }
    var showBridgePicker by remember { mutableStateOf(false) }
    var showDatasetPicker by remember { mutableStateOf(false) }

    LaunchedEffect(bridgeNetwork) {
        bridgeNetwork?.ipv4Address?.let { v4Network = it }
        bridgeNetwork?.ipv6Address?.let { v6Network = it }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Edit global Virt settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveChoiceField(
                label = "Default pool",
                value = poolChoices[pool] ?: pool,
                placeholder = "Select a pool",
                onClick = { showPoolPicker = true }
            )
            ExpressiveChoiceField(
                label = "Storage pools",
                value = storagePools.ifBlank { "None" },
                placeholder = "Select pools",
                onClick = { showDatasetPicker = true }
            )
            ExpressiveChoiceField(
                label = "Bridge",
                value = if (bridge.isBlank()) "Automatic" else bridgeChoices[bridge] ?: bridge,
                placeholder = "Select a bridge",
                onClick = { showBridgePicker = true }
            )
            ExpressiveTextField(
                value = v4Network,
                onValueChange = { v4Network = it },
                label = "IPv4 network CIDR",
                placeholder = "Optional"
            )
            ExpressiveTextField(
                value = v6Network,
                onValueChange = { v6Network = it },
                label = "IPv6 network CIDR",
                placeholder = "Optional"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        val normalizedBridge = if (bridge == AUTO_BRIDGE) "" else bridge
                        onSave(
                            pool,
                            normalizedBridge,
                            storagePools.split(","),
                            v4Network,
                            v6Network
                        )
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Save") }
            }
        }
    }

    if (showPoolPicker) {
        ExpressiveChoiceSheet(
            title = "Choose default pool",
            choices = poolChoices.map { (key, value) ->
                ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
            },
            selectedKey = pool.ifBlank { null },
            onDismiss = { showPoolPicker = false },
            onSelect = {
                pool = it
                showPoolPicker = false
            }
        )
    }

    if (showBridgePicker) {
        ExpressiveChoiceSheet(
            title = "Choose bridge",
            choices = bridgeChoices.map { (key, value) ->
                ExpressiveChoice(key = key, label = value.ifBlank { key }, supporting = key)
            },
            selectedKey = bridge.ifBlank { AUTO_BRIDGE },
            searchable = false,
            height = 320,
            onDismiss = { showBridgePicker = false },
            onSelect = {
                bridge = if (it == AUTO_BRIDGE) "" else it
                onBridgeSelected(bridge)
                showBridgePicker = false
            }
        )
    }

    if (showDatasetPicker) {
        DatasetPickerSheet(
            manager = manager,
            config = DatasetPickerConfig(
                title = "Choose storage pools",
                confirmLabel = "Use pools",
                allowMultiSelect = true
            ),
            onConfirm = { selected ->
                storagePools = selected.joinToString(", ") { it.name }
                showDatasetPicker = false
            },
            onDismiss = { showDatasetPicker = false }
        )
    }
}
