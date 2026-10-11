package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync
import androidx.compose.foundation.layout.ColumnScope

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Cloudsync
import com.imnotndesh.truehub.ui.background.ExpressiveScreenBackground
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerSheet
import com.imnotndesh.truehub.ui.homepage.dataset.toFullMountPath
import kotlinx.coroutines.launch

private val DIRECTIONS = listOf("PUSH", "PULL")
private val TRANSFER_MODES = listOf("COPY", "MOVE", "SYNC")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncFormScreen(
    manager: TrueNASApiManager,
    taskId: Int,
    onNavigateBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val viewModel: CloudSyncViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    val isEdit = taskId >= 0

    var description by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }
    var bucket by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("PUSH") }
    var transferMode by remember { mutableStateOf("COPY") }
    var enabled by remember { mutableStateOf(true) }
    var showDatasetPicker by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var credentials by remember { mutableStateOf<List<Cloudsync.CredentialEntry>>(emptyList()) }
    var providers by remember { mutableStateOf<List<Cloudsync.Provider>>(emptyList()) }
    var selectedCredential by remember { mutableStateOf<Cloudsync.CredentialEntry?>(null) }
    var showCredentialPicker by remember { mutableStateOf(false) }
    var showCredentialWizard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reloadCredentials() {
        scope.launch {
            when (val result = manager.cloudsync.queryCredentials()) {
                is ApiResult.Success -> credentials = result.data
                is ApiResult.Error -> loadError = result.message
                is ApiResult.Loading -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadCredentials()
        when (val result = manager.cloudsync.getProviders()) {
            is ApiResult.Success -> providers = result.data
            else -> Unit
        }
    }

    LaunchedEffect(taskId, reloadKey) {
        if (!isEdit) return@LaunchedEffect
        loadError = null
        when (val result = manager.cloudsync.getTask(taskId)) {
            is ApiResult.Success -> result.data.let { task ->
                description = task.description ?: ""
                path = task.path ?: ""
                direction = task.direction ?: "PUSH"
                transferMode = task.transferMode ?: "COPY"
                enabled = task.enabled ?: true
                selectedCredential = task.credentials
            }
            is ApiResult.Error -> loadError = result.message
            is ApiResult.Loading -> {}
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            ToastManager.showSuccess(if (isEdit) "Task updated" else "Task created")
            onSaved()
        }
    }

    val credentialType = selectedCredential?.provider?.get("type") as? String
    val supportsBuckets = providers.find { it.name == credentialType }?.buckets == true

    fun save() {
        val payload = mapOf(
            "description" to description,
            "path" to path,
            "credentials" to selectedCredential?.id,
            "direction" to direction,
            "transfer_mode" to transferMode,
            "enabled" to enabled,
            "attributes" to buildMap<String, Any?> {
                if (bucket.isNotBlank()) put("bucket", bucket.trim())
                if (folder.isNotBlank()) put("folder", folder.trim())
            }
        )
        if (isEdit) viewModel.updateTask(taskId, payload) else viewModel.createTask(payload)
    }

    val canSave = !state.isSaving && path.isNotBlank() && selectedCredential != null &&
        (!supportsBuckets || bucket.isNotBlank()) && loadError == null

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            UnifiedScreenHeader(
                title = if (isEdit) "Edit Task" else "New Task",
                subtitle = "Cloud Sync",
                isLoading = false,
                isRefreshing = false,
                error = loadError ?: state.error,
                onDismissError = { loadError = null; viewModel.dismissError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        ExpressiveScreenBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormCard(title = "Credential", icon = Icons.Default.Cloud) {
                        CredentialRow(
                            credential = selectedCredential,
                            onClick = { showCredentialPicker = true }
                        )
                    }

                    FormCard(title = "Destination", icon = Icons.Default.FolderOpen) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = path,
                            onValueChange = { path = it },
                            label = { Text("Local path") },
                            placeholder = { Text("/mnt/…") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatasetPicker = true }) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = "Choose dataset")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (supportsBuckets) {
                            OutlinedTextField(
                                value = bucket,
                                onValueChange = { bucket = it },
                                label = { Text("Bucket") },
                                singleLine = true,
                                supportingText = { Text("Required for this provider") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        OutlinedTextField(
                            value = folder,
                            onValueChange = { folder = it },
                            label = { Text("Remote folder (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FormCard(title = "Behaviour", icon = Icons.Default.Cloud) {
                        FieldLabel("Direction")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            DIRECTIONS.forEachIndexed { index, value ->
                                SegmentedButton(
                                    selected = direction == value,
                                    onClick = { direction = value },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DIRECTIONS.size)
                                ) { Text(value) }
                            }
                        }
                        FieldLabel("Transfer mode")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            TRANSFER_MODES.forEachIndexed { index, value ->
                                SegmentedButton(
                                    selected = transferMode == value,
                                    onClick = { transferMode = value },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TRANSFER_MODES.size)
                                ) { Text(value) }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = enabled,
                                    role = Role.Switch,
                                    onValueChange = { enabled = it }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FieldLabel("Enabled")
                            Switch(checked = enabled, onCheckedChange = null)
                        }
                    }

                    Button(
                        onClick = { save() },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (isEdit) "Save Changes" else "Create Task",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDatasetPicker) {
        DatasetPickerSheet(
            manager = manager,
            config = DatasetPickerConfig(
                title = "Choose cloud-sync dataset",
                confirmLabel = "Use dataset",
                filterPredicate = null
            ),
            onConfirm = { selected ->
                selected.firstOrNull()?.toFullMountPath()?.let { selectedPath -> path = selectedPath }
                showDatasetPicker = false
            },
            onDismiss = { showDatasetPicker = false }
        )
    }

    if (showCredentialPicker) {
        CloudSyncCredentialPicker(
            credentials = credentials,
            selected = selectedCredential,
            onSelect = {
                selectedCredential = it
                showCredentialPicker = false
            },
            onCreateNew = {
                showCredentialPicker = false
                showCredentialWizard = true
            },
            onDismiss = { showCredentialPicker = false }
        )
    }

    if (showCredentialWizard) {
        CloudSyncCredentialWizard(
            manager = manager,
            onDismiss = { showCredentialWizard = false },
            onCreated = { created ->
                selectedCredential = created
                showCredentialWizard = false
                reloadCredentials()
            }
        )
    }
}

@Composable
private fun FormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            content()
        }
    }
}

@Composable
private fun CredentialRow(
    credential: Cloudsync.CredentialEntry?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = credential?.name ?: "Select a credential",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = credential?.provider?.get("type")?.toString()
                        ?: "Choose an existing one or create a new one",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}
