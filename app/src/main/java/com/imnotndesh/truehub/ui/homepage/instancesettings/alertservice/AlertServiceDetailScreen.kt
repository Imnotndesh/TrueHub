package com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.homepage.instancesettings.alertsservice.AlertServiceDetailViewModel

@Composable
fun AlertServiceDetailScreen(
    serviceId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AlertServiceDetailViewModel = hiltViewModel(key = serviceId.toString())
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditing by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf("") }
    var editLevel by remember { mutableStateOf(Alerts.AlertLevels.WARNING) }
    var editEnabled by remember { mutableStateOf(true) }
    var editAttrs by remember { mutableStateOf(Alerts.AlertServiceAttributes()) }

    fun enterEditMode(service: Alerts.AlertServiceEntry) {
        editName = service.name
        editLevel = service.level
        editEnabled = service.enabled
        editAttrs = service.attributes
        isEditing = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.deleteResult) {
        if (uiState.deleteResult == true) onNavigateBack()
    }
    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { success ->
            snackbarHostState.showSnackbar(if (success) "Test alert sent successfully!" else "Test alert failed.")
            viewModel.clearTestResult()
        }
    }
    LaunchedEffect(uiState.updateResult) {
        if (uiState.updateResult == true) {
            isEditing = false
            snackbarHostState.showSnackbar("Service updated")
        }
    }

    val current = uiState.service
    var showDeleteDialog by remember { mutableStateOf(false) }

    val statusColor by animateColorAsState(
        targetValue = if (current?.enabled == true) Color(0xFF2E7D32)
        else MaterialTheme.colorScheme.outline,
        label = "statusColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer)
                )
            )
    ) {
        UnifiedScreenHeader(
            title = current?.name ?: "Alert Service",
            subtitle = current?.type__title ?: "Service details",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingScreen("Loading service details")
                current != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                                    .background(statusColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, null, tint = statusColor, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(current.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(current.type__title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(100.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (current.enabled) "Enabled" else "Disabled", style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        EditServiceForm(
                            name = editName, onNameChange = { editName = it },
                            level = editLevel, onLevelChange = { editLevel = it },
                            enabled = editEnabled, onEnabledChange = { editEnabled = it },
                            attrs = editAttrs, onAttrsChange = { editAttrs = it }
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    viewModel.updateService(
                                        Alerts.AlertServiceUpdate(
                                            name = editName, attributes = editAttrs,
                                            level = editLevel, enabled = editEnabled
                                        )
                                    )
                                },
                                enabled = !uiState.isUpdating && editName.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isUpdating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save")
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Attributes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                AlertServiceAttributesSummary(current.attributes)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Minimum Level", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(current.level.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { enterEditMode(current) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit")
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.testService(
                                        Alerts.AlertServiceCreate(current.name, current.attributes, current.level, current.enabled)
                                    )
                                },
                                enabled = !uiState.isTesting,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isTesting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Test")
                            }
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                enabled = !uiState.isDeleting,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isDeleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Alert Service") },
            text = { Text("Are you sure you want to delete \"${current?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; viewModel.deleteService() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServiceForm(
    name: String, onNameChange: (String) -> Unit,
    level: Alerts.AlertLevels, onLevelChange: (Alerts.AlertLevels) -> Unit,
    enabled: Boolean, onEnabledChange: (Boolean) -> Unit,
    attrs: Alerts.AlertServiceAttributes, onAttrsChange: (Alerts.AlertServiceAttributes) -> Unit
) {
    var levelExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            ExposedDropdownMenuBox(expanded = levelExpanded, onExpandedChange = { levelExpanded = it }) {
                OutlinedTextField(
                    value = level.name, onValueChange = {}, readOnly = true,
                    label = { Text("Minimum Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = levelExpanded, onDismissRequest = { levelExpanded = false }) {
                    Alerts.AlertLevels.entries.forEach { lvl ->
                        DropdownMenuItem(text = { Text(lvl.name) }, onClick = { onLevelChange(lvl); levelExpanded = false })
                    }
                }
            }

            // Per-type attribute fields — mirrors AlertServiceCreateScreen's pattern
            when (attrs.type) {
                "Slack" -> OutlinedTextField(value = attrs.url ?: "", onValueChange = { onAttrsChange(attrs.copy(url = it)) }, label = { Text("Webhook URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                "Mail" -> OutlinedTextField(value = attrs.email ?: "", onValueChange = { onAttrsChange(attrs.copy(email = it)) }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                "Mattermost" -> {
                    OutlinedTextField(value = attrs.url ?: "", onValueChange = { onAttrsChange(attrs.copy(url = it)) }, label = { Text("Webhook URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.username ?: "", onValueChange = { onAttrsChange(attrs.copy(username = it)) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.channel ?: "", onValueChange = { onAttrsChange(attrs.copy(channel = it)) }, label = { Text("Channel (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "PagerDuty" -> {
                    OutlinedTextField(value = attrs.service_key ?: "", onValueChange = { onAttrsChange(attrs.copy(service_key = it)) }, label = { Text("Service Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.client_name ?: "", onValueChange = { onAttrsChange(attrs.copy(client_name = it)) }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "Telegram" -> {
                    OutlinedTextField(value = attrs.bot_token ?: "", onValueChange = { onAttrsChange(attrs.copy(bot_token = it)) }, label = { Text("Bot Token") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(
                        value = attrs.chat_ids?.joinToString(", ") ?: "",
                        onValueChange = { onAttrsChange(attrs.copy(chat_ids = it.split(",").mapNotNull { id -> id.trim().toIntOrNull() })) },
                        label = { Text("Chat IDs (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
                "OpsGenie" -> {
                    OutlinedTextField(value = attrs.api_key ?: "", onValueChange = { onAttrsChange(attrs.copy(api_key = it)) }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.api_url ?: "", onValueChange = { onAttrsChange(attrs.copy(api_url = it)) }, label = { Text("API URL (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "VictorOps" -> {
                    OutlinedTextField(value = attrs.api_key ?: "", onValueChange = { onAttrsChange(attrs.copy(api_key = it)) }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.routing_key ?: "", onValueChange = { onAttrsChange(attrs.copy(routing_key = it)) }, label = { Text("Routing Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "AWSSNS" -> {
                    OutlinedTextField(value = attrs.region ?: "", onValueChange = { onAttrsChange(attrs.copy(region = it)) }, label = { Text("Region") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.topic_arn ?: "", onValueChange = { onAttrsChange(attrs.copy(topic_arn = it)) }, label = { Text("Topic ARN") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.aws_access_key_id ?: "", onValueChange = { onAttrsChange(attrs.copy(aws_access_key_id = it)) }, label = { Text("Access Key ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.aws_secret_access_key ?: "", onValueChange = { onAttrsChange(attrs.copy(aws_secret_access_key = it)) }, label = { Text("Secret Access Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "InfluxDB" -> {
                    OutlinedTextField(value = attrs.host ?: "", onValueChange = { onAttrsChange(attrs.copy(host = it)) }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.username ?: "", onValueChange = { onAttrsChange(attrs.copy(username = it)) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.password ?: "", onValueChange = { onAttrsChange(attrs.copy(password = it)) }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.database ?: "", onValueChange = { onAttrsChange(attrs.copy(database = it)) }, label = { Text("Database") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.series_name ?: "", onValueChange = { onAttrsChange(attrs.copy(series_name = it)) }, label = { Text("Series Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                "SNMPTrap" -> {
                    OutlinedTextField(value = attrs.host ?: "", onValueChange = { onAttrsChange(attrs.copy(host = it)) }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.port?.toString() ?: "", onValueChange = { onAttrsChange(attrs.copy(port = it.toIntOrNull())) }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = attrs.community ?: "", onValueChange = { onAttrsChange(attrs.copy(community = it)) }, label = { Text("Community") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                else -> Text("No editable fields for type: ${attrs.type}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
        }
    }
}

@Composable
private fun AlertServiceAttributesSummary(attrs: Alerts.AlertServiceAttributes) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (attrs.type) {
            "AWSSNS" -> { DetailRow("Region", attrs.region); DetailRow("Topic ARN", attrs.topic_arn) }
            "InfluxDB" -> { DetailRow("Host", attrs.host); DetailRow("Username", attrs.username); DetailRow("Database", attrs.database); DetailRow("Series Name", attrs.series_name) }
            "Mail" -> DetailRow("Email", attrs.email)
            "Mattermost" -> { DetailRow("URL", attrs.url); DetailRow("Username", attrs.username); DetailRow("Channel", attrs.channel) }
            "OpsGenie" -> { DetailRow("API Key", attrs.api_key?.take(8) + "..."); DetailRow("API URL", attrs.api_url) }
            "PagerDuty" -> { DetailRow("Service Key", attrs.service_key?.take(8) + "..."); DetailRow("Client Name", attrs.client_name) }
            "Slack" -> DetailRow("Webhook URL", attrs.url)
            "SNMPTrap" -> { DetailRow("Host", attrs.host); DetailRow("Port", attrs.port?.toString()); DetailRow("v3", attrs.v3?.toString()) }
            "Telegram" -> { DetailRow("Bot Token", attrs.bot_token?.take(8) + "..."); DetailRow("Chat IDs", attrs.chat_ids?.joinToString(", ")) }
            "VictorOps" -> { DetailRow("API Key", attrs.api_key?.take(8) + "..."); DetailRow("Routing Key", attrs.routing_key) }
            else -> DetailRow("Type", attrs.type ?: "Unknown")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value != null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}