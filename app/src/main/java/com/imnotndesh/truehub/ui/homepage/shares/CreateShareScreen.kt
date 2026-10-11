package com.imnotndesh.truehub.ui.homepage.shares

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerConfig
import com.imnotndesh.truehub.ui.homepage.dataset.DatasetPickerSheet
import com.imnotndesh.truehub.ui.homepage.dataset.toFullMountPath

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateShareScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit
) {
    val viewModel: CreateShareViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var type by remember { mutableStateOf(CreateShareType.Smb) }
    var path by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var readOnly by rememberSaveable { mutableStateOf(false) }
    var guestAccess by rememberSaveable { mutableStateOf(false) }
    var showDatasetPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.created) {
        if (state.created) {
            ToastManager.showSuccess("Share created")
            onCreated()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "Create share",
            subtitle = "Expose a dataset over SMB or NFS",
            isLoading = state.isSaving,
            isRefreshing = false,
            error = state.error,
            onDismissError = viewModel::dismissError,
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == CreateShareType.Smb,
                    onClick = { type = CreateShareType.Smb },
                    label = { Text("SMB") }
                )
                FilterChip(
                    selected = type == CreateShareType.Nfs,
                    onClick = { type = CreateShareType.Nfs },
                    label = { Text("NFS") }
                )
            }

            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dataset path") },
                placeholder = { Text("/pool/dataset") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showDatasetPicker = true }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Choose dataset")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            if (type == CreateShareType.Smb) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Share name") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comment") },
                minLines = 2,
                maxLines = 3
            )

            CreateShareSetting(
                title = "Enabled",
                description = "Allow clients to use this share",
                checked = enabled,
                onCheckedChange = { enabled = it }
            )
            CreateShareSetting(
                title = "Read-only",
                description = "Prevent clients from changing share data",
                checked = readOnly,
                onCheckedChange = { readOnly = it }
            )
            if (type == CreateShareType.Smb) {
                CreateShareSetting(
                    title = "Guest access",
                    description = "Allow access without SMB credentials",
                    checked = guestAccess,
                    onCheckedChange = { guestAccess = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.create(
                        type = type,
                        path = path,
                        name = name,
                        comment = comment,
                        enabled = enabled,
                        readOnly = readOnly,
                        guestAccess = guestAccess
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Creating..." else "Create share")
            }
        }
    }

    if (showDatasetPicker) {
        DatasetPickerSheet(
            manager = manager,
            config = DatasetPickerConfig(
                title = "Choose share dataset",
                confirmLabel = "Use dataset",
                filterPredicate = { it.mountpoint != null }
            ),
            onConfirm = { selected ->
                selected.firstOrNull()?.toFullMountPath()?.let { selectedPath ->
                    path = selectedPath
                }
                showDatasetPicker = false
            },
            onDismiss = { showDatasetPicker = false }
        )
    }
}

@Composable
private fun CreateShareSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
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
        Switch(checked = checked, onCheckedChange = null)
    }
}
