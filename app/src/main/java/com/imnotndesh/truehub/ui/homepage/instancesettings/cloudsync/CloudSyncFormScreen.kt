package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

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
    var direction by remember { mutableStateOf("PUSH") }
    var transferMode by remember { mutableStateOf("COPY") }
    var enabled by remember { mutableStateOf(true) }

    LaunchedEffect(taskId) {
        if (!isEdit) return@LaunchedEffect
        when (val result = manager.cloudsync.getTask(taskId)) {
            is ApiResult.Success -> result.data.let { task ->
                description = task.description ?: ""
                path = task.path ?: ""
                direction = task.direction ?: "PUSH"
                transferMode = task.transferMode ?: "COPY"
                enabled = task.enabled ?: true
            }
            else -> Unit
        }
    }

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UnifiedScreenHeader(
                title = if (isEdit) "Edit Task" else "New Task",
                subtitle = "Cloud Sync",
                isLoading = false,
                isRefreshing = false,
                error = state.error,
                onDismissError = { viewModel.dismissError() },
                manager = manager,
                onBackPressed = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path") },
                placeholder = { Text("/mnt/…") },
                modifier = Modifier.fillMaxWidth()
            )

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

            FieldLabel("Transfer Mode")
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FieldLabel("Enabled")
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Button(
                onClick = {
                    val payload = mapOf(
                        "description" to description,
                        "path" to path,
                        "direction" to direction,
                        "transfer_mode" to transferMode,
                        "enabled" to enabled
                    )
                    if (isEdit) viewModel.updateTask(taskId, payload) else viewModel.createTask(payload)
                },
                enabled = !state.isSaving && path.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Create Task")
            }
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
