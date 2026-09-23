package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun ApiKeyCreateScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: ApiKeyViewModel = hiltViewModel()
    val uiState by vm.createState.collectAsState()
    val clipboard = LocalClipboardManager.current

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var expiresAt by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createResult) {
        if (uiState.createResult != null) {
            uiState.createResult?.key?.let {
                clipboard.setText(AnnotatedString(it))
            }
        }
    }

    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer))
    )) {
        UnifiedScreenHeader(
            title = "Create API Key",
            subtitle = "Generate a new API access key",
            isLoading = false, isRefreshing = false,
            error = uiState.error, onDismissError = { vm.clearCreateError() },
            manager = manager, onBackPressed = onNavigateBack
        )

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {


            // Show credit card dialog when key is created
            if (uiState.createResult != null) {
                CreditCardKeyDialog(
                    keyName = uiState.createResult!!.name,
                    keyValue = uiState.createResult!!.key,
                    onDismiss = { vm.clearCreateResult(); onNavigateBack() }
                )
            } else {
                // Create form
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("New API Key", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        OutlinedTextField(value = name, onValueChange = { name = it },
                            label = { Text("Key Name") }, placeholder = { Text("e.g. My App Key") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = username, onValueChange = { username = it },
                            label = { Text("Username *") }, placeholder = { Text("e.g. admin") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = expiresAt, onValueChange = { expiresAt = it },
                            label = { Text("Expires At (optional)") }, placeholder = { Text("YYYY-MM-DD or leave empty") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { vm.createKey(System.ApiKeyCreate(
                            name = name.ifBlank { "nobody" },
                            username = username,
                            expires_at = expiresAt.ifBlank { null }
                        )) },
                        enabled = !uiState.isCreating && username.isNotBlank(),
                        modifier = Modifier.weight(1f)) {
                        if (uiState.isCreating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.VpnKey, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("Create Key")
                    }
                }
            }
        }
    }
}