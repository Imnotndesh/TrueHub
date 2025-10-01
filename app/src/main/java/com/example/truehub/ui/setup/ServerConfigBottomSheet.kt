package com.example.truehub.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.ui.setup.SetupEvent
import com.example.truehub.ui.setup.SetupScreenViewModel
import com.example.truehub.ui.setup.SetupUiState
import com.example.truehub.ui.setup.validateUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigBottomSheet(
    onDismiss: () -> Unit,
    onConfigured: (String, Boolean) -> Unit,
    initialUrl: String? = null,
    initialInsecure: Boolean = false,
    showChangeUrlOption: Boolean = false
) {
    val viewModel = remember { SetupScreenViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize with existing values if available
    LaunchedEffect(Unit) {
        initialUrl?.let {
            viewModel.handleEvent(SetupEvent.UpdateServerUrl(it.replace("/api/current", "")))
        }
        viewModel.handleEvent(SetupEvent.UpdateInsecure(initialInsecure))
    }

    // Handle successful configuration
    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete) {
            onConfigured(viewModel.formatUrl(uiState.serverUrl), uiState.insecure)
            viewModel.handleEvent(SetupEvent.ResetSetupComplete)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!uiState.isConfiguring) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showChangeUrlOption) "Server Configuration" else "Configure TrueNAS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (showChangeUrlOption) {
                        Text(
                            text = "Change or verify your server settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!uiState.isConfiguring) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            Divider()

            // URL Input Field
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = { viewModel.handleEvent(SetupEvent.UpdateServerUrl(it)) },
                label = { Text("TrueNAS Server URL") },
                placeholder = { Text("ws://192.168.1.100") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = "Server URL"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (uiState.serverUrl.isNotEmpty() && !uiState.urlValidation.isValid) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    unfocusedBorderColor = if (uiState.serverUrl.isNotEmpty() && !uiState.urlValidation.isValid) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    }
                ),
                isError = uiState.serverUrl.isNotEmpty() && !uiState.urlValidation.isValid,
                enabled = !uiState.isConfiguring,
                singleLine = true
            )

            // Validation Messages
            if (uiState.urlValidation.errors.isNotEmpty() || uiState.urlValidation.warnings.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.urlValidation.errors.forEach { error ->
                        ValidationMessage(message = error, isError = true)
                    }
                    uiState.urlValidation.warnings.forEach { warning ->
                        ValidationMessage(message = warning, isError = false)
                    }
                }
            }

            // Connection Error
            uiState.connectionError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Security Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (uiState.insecure) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Allow insecure connections",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Enable for self-signed certificates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                        )
                    }
                    Switch(
                        checked = uiState.insecure,
                        onCheckedChange = { viewModel.handleEvent(SetupEvent.UpdateInsecure(it)) },
                        enabled = !uiState.isConfiguring
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!uiState.isConfiguring) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = { viewModel.handleEvent(SetupEvent.Configure(context)) },
                    enabled = uiState.urlValidation.isValid &&
                            uiState.serverUrl.isNotEmpty() &&
                            !uiState.isConfiguring,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isConfiguring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Testing...")
                        }
                    } else {
                        Text(if (showChangeUrlOption) "Update" else "Connect")
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationMessage(
    message: String,
    isError: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.Error else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.tertiary
            }
        )
    }
}