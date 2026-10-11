package com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import kotlinx.coroutines.launch

private val SERVICE_TYPES = listOf(
    "AWSSNS", "InfluxDB", "Mail", "Mattermost", "OpsGenie",
    "PagerDuty", "Slack", "SNMPTrap", "Telegram", "VictorOps"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertServiceCreateScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Slack") }
    var level by remember { mutableStateOf(Alerts.AlertLevels.WARNING) }
    var enabled by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }

    var slackUrl by remember { mutableStateOf("") }
    var mailEmail by remember { mutableStateOf("") }
    var mattermostUrl by remember { mutableStateOf("") }
    var mattermostUsername by remember { mutableStateOf("") }
    var mattermostChannel by remember { mutableStateOf("") }
    var pagerdutyServiceKey by remember { mutableStateOf("") }
    var pagerdutyClientName by remember { mutableStateOf("") }
    var telegramBotToken by remember { mutableStateOf("") }
    var telegramChatIds by remember { mutableStateOf("") }
    var opsgenieApiKey by remember { mutableStateOf("") }
    var opsgenieApiUrl by remember { mutableStateOf("") }
    var victoropsApiKey by remember { mutableStateOf("") }
    var victoropsRoutingKey by remember { mutableStateOf("") }
    var awsRegion by remember { mutableStateOf("") }
    var awsTopicArn by remember { mutableStateOf("") }
    var awsAccessKey by remember { mutableStateOf("") }
    var awsSecretKey by remember { mutableStateOf("") }
    var influxHost by remember { mutableStateOf("") }
    var influxUsername by remember { mutableStateOf("") }
    var influxPassword by remember { mutableStateOf("") }
    var influxDatabase by remember { mutableStateOf("") }
    var influxSeriesName by remember { mutableStateOf("") }
    // SNMPTrap
    var snmpHost by remember { mutableStateOf("") }
    var snmpPort by remember { mutableStateOf("162") }
    var snmpV3 by remember { mutableStateOf(false) }
    var snmpCommunity by remember { mutableStateOf("") }

    fun buildAttributes(): Alerts.AlertServiceAttributes = when (selectedType) {
        "AWSSNS" -> Alerts.AlertServiceAttributes(
            type = "AWSSNS", region = awsRegion, topic_arn = awsTopicArn,
            aws_access_key_id = awsAccessKey, aws_secret_access_key = awsSecretKey
        )
        "InfluxDB" -> Alerts.AlertServiceAttributes(
            type = "InfluxDB", host = influxHost, username = influxUsername,
            password = influxPassword, database = influxDatabase, series_name = influxSeriesName
        )
        "Mail" -> Alerts.AlertServiceAttributes(
            type = "Mail", email = mailEmail
        )
        "Mattermost" -> Alerts.AlertServiceAttributes(
            type = "Mattermost", url = mattermostUrl, username = mattermostUsername,
            channel = mattermostChannel
        )
        "OpsGenie" -> Alerts.AlertServiceAttributes(
            type = "OpsGenie", api_key = opsgenieApiKey, api_url = opsgenieApiUrl
        )
        "PagerDuty" -> Alerts.AlertServiceAttributes(
            type = "PagerDuty", service_key = pagerdutyServiceKey, client_name = pagerdutyClientName
        )
        "Slack" -> Alerts.AlertServiceAttributes(
            type = "Slack", url = slackUrl
        )
        "SNMPTrap" -> Alerts.AlertServiceAttributes(
            type = "SNMPTrap", host = snmpHost, port = snmpPort.toIntOrNull(),
            v3 = snmpV3, community = snmpCommunity
        )
        "Telegram" -> Alerts.AlertServiceAttributes(
            type = "Telegram", bot_token = telegramBotToken,
            chat_ids = telegramChatIds.split(",").mapNotNull { it.trim().toIntOrNull() }
        )
        "VictorOps" -> Alerts.AlertServiceAttributes(
            type = "VictorOps", api_key = victoropsApiKey, routing_key = victoropsRoutingKey
        )
        else -> Alerts.AlertServiceAttributes(type = selectedType)
    }

    fun save() {
        scope.launch {
            isSaving = true
            val create = Alerts.AlertServiceCreate(
                name = name, attributes = buildAttributes(), level = level, enabled = enabled
            )
            when (val result = manager.alertsService.createAlertServiceWithResult(create)) {
                is ApiResult.Success -> {
                    ToastManager.showSuccess("Alert service created")
                    onNavigateBack()
                }
                is ApiResult.Error -> {
                    snackbarHostState.showSnackbar("Error: ${result.message}")
                }
                else -> {}
            }
            isSaving = false
        }
    }

    fun test() {
        scope.launch {
            isTesting = true
            val create = Alerts.AlertServiceCreate(
                name = name, attributes = buildAttributes(), level = level, enabled = enabled
            )
            when (val result = manager.alertsService.testAlertServiceWithResult(create)) {
                is ApiResult.Success -> {
                    snackbarHostState.showSnackbar(
                        if (result.data) "Test successful!" else "Test failed."
                    )
                }
                is ApiResult.Error -> {
                    snackbarHostState.showSnackbar("Test error: ${result.message}")
                }
                else -> {}
            }
            isTesting = false
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "New Alert Service",
                subtitle = "Configure a notification service",
                isLoading = false,
                isRefreshing = false,
                error = null,
                onDismissError = {},
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Service Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    SERVICE_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = levelExpanded,
                onExpandedChange = { levelExpanded = it }
            ) {
                OutlinedTextField(
                    value = level.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Minimum Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = levelExpanded,
                    onDismissRequest = { levelExpanded = false }
                ) {
                    Alerts.AlertLevels.entries.forEach { lvl ->
                        DropdownMenuItem(
                            text = { Text(lvl.name) },
                            onClick = {
                                level = lvl
                                levelExpanded = false
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$selectedType Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    when (selectedType) {
                        "Slack" -> OutlinedTextField(
                            value = slackUrl, onValueChange = { slackUrl = it },
                            label = { Text("Webhook URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        "Mail" -> OutlinedTextField(
                            value = mailEmail, onValueChange = { mailEmail = it },
                            label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        "Mattermost" -> {
                            OutlinedTextField(value = mattermostUrl, onValueChange = { mattermostUrl = it }, label = { Text("Webhook URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = mattermostUsername, onValueChange = { mattermostUsername = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = mattermostChannel, onValueChange = { mattermostChannel = it }, label = { Text("Channel (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "PagerDuty" -> {
                            OutlinedTextField(value = pagerdutyServiceKey, onValueChange = { pagerdutyServiceKey = it }, label = { Text("Service Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = pagerdutyClientName, onValueChange = { pagerdutyClientName = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "Telegram" -> {
                            OutlinedTextField(value = telegramBotToken, onValueChange = { telegramBotToken = it }, label = { Text("Bot Token") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = telegramChatIds, onValueChange = { telegramChatIds = it }, label = { Text("Chat IDs (comma-separated)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "OpsGenie" -> {
                            OutlinedTextField(value = opsgenieApiKey, onValueChange = { opsgenieApiKey = it }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = opsgenieApiUrl, onValueChange = { opsgenieApiUrl = it }, label = { Text("API URL (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "VictorOps" -> {
                            OutlinedTextField(value = victoropsApiKey, onValueChange = { victoropsApiKey = it }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = victoropsRoutingKey, onValueChange = { victoropsRoutingKey = it }, label = { Text("Routing Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "AWSSNS" -> {
                            OutlinedTextField(value = awsRegion, onValueChange = { awsRegion = it }, label = { Text("Region") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = awsTopicArn, onValueChange = { awsTopicArn = it }, label = { Text("Topic ARN") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = awsAccessKey, onValueChange = { awsAccessKey = it }, label = { Text("Access Key ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = awsSecretKey, onValueChange = { awsSecretKey = it }, label = { Text("Secret Access Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "InfluxDB" -> {
                            OutlinedTextField(value = influxHost, onValueChange = { influxHost = it }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = influxUsername, onValueChange = { influxUsername = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = influxPassword, onValueChange = { influxPassword = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = influxDatabase, onValueChange = { influxDatabase = it }, label = { Text("Database") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = influxSeriesName, onValueChange = { influxSeriesName = it }, label = { Text("Series Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                        "SNMPTrap" -> {
                            OutlinedTextField(value = snmpHost, onValueChange = { snmpHost = it }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = snmpPort, onValueChange = { snmpPort = it }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(value = snmpCommunity, onValueChange = { snmpCommunity = it }, label = { Text("Community") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = snmpV3,
                                        role = Role.Switch,
                                        onValueChange = { snmpV3 = it }
                                    )
                            ) {
                                Text("SNMP v3", modifier = Modifier.weight(1f))
                                Switch(checked = snmpV3, onCheckedChange = null)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .toggleable(
                            value = enabled,
                            role = Role.Switch,
                            onValueChange = { enabled = it }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enabled",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = enabled, onCheckedChange = null)
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { test() },
                    enabled = !isTesting && !isSaving && name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTesting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Test")
                }
                Button(
                    onClick = { save() },
                    enabled = !isSaving && !isTesting && name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Create")
                }
            }
        }
    }
}
