package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System.formatDate
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun ApiKeyDetailScreen(
    keyId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val vm: ApiKeyViewModel = hiltViewModel(key = keyId.toString())
    val uiState by vm.detailState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadDetail(keyId) }
    LaunchedEffect(uiState.deleteResult) { if (uiState.deleteResult == true) onNavigateBack() }

    val current = uiState.key
    val sc = animateColorAsState(
        if (current?.revoked == true) Color(0xFFC62828) else Color(0xFF2E7D32), label = ""
    )

    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer))
    )) {
        UnifiedScreenHeader(
            title = current?.name ?: "API Key",
            subtitle = current?.username ?: "Key details",
            isLoading = uiState.isLoading, isRefreshing = false,
            error = uiState.error, onDismissError = { vm.clearDetailError() },
            manager = manager, onBackPressed = onNavigateBack
        )
        Box(Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingScreen("Loading key details")
                current != null -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Status card
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).clip(CircleShape).background(sc.value.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Key, null, tint = sc.value, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(current.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Text(current.username ?: "System key", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(color = sc.value.copy(alpha = 0.12f), shape = RoundedCornerShape(100.dp)) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(sc.value))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (current.revoked) "Revoked" else "Active",
                                        style = MaterialTheme.typography.labelMedium, color = sc.value, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Key Info section
                    SectionCard(title = "Key Info", icon = Icons.Default.VpnKey) {
                        DetailRow("Name", current.name)
                        DetailRow("Username", current.username ?: "—")
                        DetailRow("Key Hash", current.keyhash.take(16) + "...")
                        DetailRow("Created", current.created_at.formatDate())
                        DetailRow("Expires", current.expires_at?.formatDate() ?: "Never")


                    }
                    // Status section
                    SectionCard(title = "Status", icon = Icons.Default.Security) {
                        DetailRow("Local", if (current.local) "Yes" else "No")
                        DetailRow("Revoked", if (current.revoked) "Yes" else "No")
                        if (current.revoked_reason != null) DetailRow("Reason", current.revoked_reason)
                    }

                    // Actions
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showResetDialog = true },
                            enabled = !uiState.isResetting && !current.revoked,
                            modifier = Modifier.weight(1f)) {
                            if (uiState.isResetting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text("Reset")
                        }
                        OutlinedButton(onClick = { showDeleteDialog = true },
                            enabled = !uiState.isDeleting,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)) {
                            if (uiState.isDeleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp)); Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text("Delete API Key") },
        text = { Text("Are you sure you want to delete \"${current?.name}\"? This cannot be undone.") },
        confirmButton = { TextButton(onClick = { showDeleteDialog = false; vm.deleteKey(keyId) },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
    )

    if (showResetDialog) AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset API Key") },
        text = { Text("Reset \"${current?.name}\"? The old key will stop working immediately.") },
        confirmButton = { TextButton(onClick = { showResetDialog = false; vm.resetKey(keyId) }) { Text("Reset") } },
        dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
    )
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

