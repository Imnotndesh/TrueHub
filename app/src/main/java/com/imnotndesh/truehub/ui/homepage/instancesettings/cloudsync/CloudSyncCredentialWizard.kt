package com.imnotndesh.truehub.ui.homepage.instancesettings.cloudsync
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material.icons.filled.Add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.CloudSyncField
import com.imnotndesh.truehub.data.models.CloudSyncFieldType
import com.imnotndesh.truehub.data.models.CloudSyncProviderSpec
import com.imnotndesh.truehub.data.models.CloudSyncProviders
import com.imnotndesh.truehub.data.models.Cloudsync
import com.imnotndesh.truehub.ui.components.ToastManager
import kotlinx.coroutines.launch

/**
 * Wizard for creating a Cloud Sync credential: pick a provider, fill the provider-specific fields
 * (generated from the TrueNAS schema), name it, optionally verify, then create.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncCredentialWizard(
    manager: TrueNASApiManager,
    onDismiss: () -> Unit,
    onCreated: (Cloudsync.CredentialEntry) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var provider by remember { mutableStateOf<CloudSyncProviderSpec?>(null) }
    val values = remember { mutableStateMapOf<String, String>() }
    var name by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var verifyMessage by remember { mutableStateOf<String?>(null) }
    var verifyOk by remember { mutableStateOf<Boolean?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun buildPayload(): Map<String, Any?> {
        val spec = provider ?: return emptyMap()
        val providerMap = mutableMapOf<String, Any?>("type" to spec.type)
        spec.fields.forEach { field ->
            val raw = (values[field.key] ?: field.defaultValue.orEmpty()).trim().trim('"')
            if (raw.isEmpty()) return@forEach
            providerMap[field.key] = when (field.type) {
                CloudSyncFieldType.BOOLEAN -> raw.toBoolean()
                CloudSyncFieldType.NUMBER -> raw.toLongOrNull() ?: raw.toDoubleOrNull() ?: raw
                else -> raw
            }
        }
        return mapOf("name" to name.trim(), "provider" to providerMap)
    }

    fun missingRequired(): CloudSyncField? = provider?.fields?.firstOrNull { field ->
        field.required && (values[field.key].isNullOrBlank() && field.defaultValue.isNullOrBlank())
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                UnifiedScreenHeader(
                    title = when (step) {
                        0 -> "New credential"
                        1 -> provider?.title ?: "Provider"
                        else -> "Name & save"
                    },
                    subtitle = "Cloud Sync · step ${step + 1} of 3",
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    onDismissError = {},
                    manager = manager,
                    onBackPressed = {
                        when {
                            step > 0 -> step--
                            else -> onDismiss()
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (step) {
                        0 -> ProviderPicker(
                            onPick = { spec ->
                                provider = spec
                                values.clear()
                                spec.fields.forEach { field ->
                                    field.defaultValue?.let { values[field.key] = it.trim('"') }
                                }
                                verifyMessage = null
                                verifyOk = null
                                step = 1
                            }
                        )
                        1 -> ProviderFields(
                            spec = provider!!,
                            values = values,
                            onValueChange = { key, value -> values[key] = value }
                        )
                        else -> NameStep(
                            name = name,
                            onNameChange = { name = it },
                            verifyMessage = verifyMessage,
                            verifyOk = verifyOk
                        )
                    }
                }

                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step == 1) {
                        OutlinedButton(
                            onClick = {
                                val missing = missingRequired()
                                if (missing != null) {
                                    error = "${missing.label} is required"
                                } else {
                                    error = null
                                    step = 2
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) { Text("Continue") }
                    } else if (step == 2) {
                        OutlinedButton(
                            onClick = {
                                isVerifying = true
                                verifyMessage = null
                                verifyOk = null
                                scope.launch {
                                    when (val r = manager.cloudsync.verifyCredential(buildPayload())) {
                                        is ApiResult.Success -> {
                                            verifyOk = r.data.valid ?: true
                                            verifyMessage = if (r.data.valid == false) "Credentials look invalid" else "Credentials verified"
                                        }
                                        is ApiResult.Error -> {
                                            verifyOk = false
                                            verifyMessage = r.message
                                        }
                                        is ApiResult.Loading -> Unit
                                    }
                                    isVerifying = false
                                }
                            },
                            enabled = !isVerifying && !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Verify")
                            }
                        }
                        Button(
                            onClick = {
                                isSaving = true
                                error = null
                                scope.launch {
                                    when (val r = manager.cloudsync.createCredential(buildPayload())) {
                                        is ApiResult.Success -> {
                                            ToastManager.showSuccess("Credential created")
                                            onCreated(r.data)
                                        }
                                        is ApiResult.Error -> error = r.message
                                        is ApiResult.Loading -> Unit
                                    }
                                    isSaving = false
                                }
                            },
                            enabled = !isSaving && name.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderPicker(onPick: (CloudSyncProviderSpec) -> Unit) {
    var query by remember { mutableStateOf("") }
    val providers = remember(query) {
        if (query.isBlank()) {
            CloudSyncProviders.all
        } else {
            CloudSyncProviders.all.filter {
                it.title.contains(query, ignoreCase = true) || it.type.contains(query, ignoreCase = true)
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search providers…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(providers) { spec ->
            Card(
                onClick = { onPick(spec) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(spec.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (spec.oauth) "Requires OAuth token" else "${spec.fields.size} field(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ProviderFields(
    spec: CloudSyncProviderSpec,
    values: Map<String, String>,
    onValueChange: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (spec.oauth) {
            Text(
                text = "This provider uses OAuth. Paste the token generated from TrueNAS, or set it up on the server first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        spec.fields.forEach { field ->
            CloudSyncFieldInput(
                field = field,
                value = values[field.key].orEmpty(),
                onValueChange = { onValueChange(field.key, it) }
            )
        }
    }
}

@Composable
private fun CloudSyncFieldInput(
    field: CloudSyncField,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (field.type) {
        CloudSyncFieldType.BOOLEAN -> Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(field.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (field.required) "Required" else "Optional",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = value.toBoolean(),
                onCheckedChange = { onValueChange(it.toString()) }
            )
        }

        CloudSyncFieldType.ENUM -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(field.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val options = field.options.take(4)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = value == option,
                            onClick = { onValueChange(option) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                        ) { Text(option, maxLines = 1) }
                    }
                }
            }
        }

        else -> {
            var visible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = if (field.type == CloudSyncFieldType.NUMBER) KeyboardType.Number else KeyboardType.Text
                ),
                visualTransformation = if (field.secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = if (field.secret) {
                    {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Hide" else "Show"
                            )
                        }
                    }
                } else null,
                supportingText = if (field.required) {
                    { Text("Required") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NameStep(
    name: String,
    onNameChange: (String) -> Unit,
    verifyMessage: String?,
    verifyOk: Boolean?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Credential name") },
            placeholder = { Text("e.g. Backblaze backups") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        verifyMessage?.let { message ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (verifyOk == false) Icons.Default.ErrorOutline else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (verifyOk == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (verifyOk == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Text(
            text = "Verify is optional. Creating saves the credential to TrueNAS and selects it for this task.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Bottom sheet to pick an existing credential or start the create wizard. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncCredentialPicker(
    credentials: List<Cloudsync.CredentialEntry>,
    selected: Cloudsync.CredentialEntry?,
    onSelect: (Cloudsync.CredentialEntry) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Credential",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (credentials.isEmpty()) {
                Text(
                    text = "No credentials yet. Create one to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                credentials.forEach { cred ->
                    Card(
                        onClick = { onSelect(cred) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = cred.name ?: "Unnamed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = cred.provider?.get("type")?.toString() ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected?.id == cred.id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            Button(
                onClick = onCreateNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create new credential")
            }
        }
    }
}
