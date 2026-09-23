package com.imnotndesh.truehub.ui.homepage.instancesettings.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@Composable
fun UserCreateScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: UserSettingsViewModel = hiltViewModel()
    val uiState by viewModel.createState.collectAsState()

    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var home by remember { mutableStateOf("/var/empty") }
    var shell by remember { mutableStateOf("/usr/bin/zsh") }
    var password by remember { mutableStateOf("") }
    var locked by remember { mutableStateOf(false) }
    var smb by remember { mutableStateOf(true) }
    var passwordDisabled by remember { mutableStateOf(false) }
    var groupCreate by remember { mutableStateOf(true) }
    var homeCreate by remember { mutableStateOf(false) }

    val isFormValid = username.isNotBlank() && fullName.isNotBlank()

    LaunchedEffect(Unit) { viewModel.loadCreateFormData() }
    LaunchedEffect(uiState.createResult) {
        if (uiState.createResult != null) onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        UnifiedScreenHeader(
            title = "Create User",
            subtitle = "Add a new local user account",
            isLoading = false,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { viewModel.clearCreateError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        when {
            uiState.isLoading -> LoadingScreen("Loading defaults...")
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Account Details section ──
                SectionCard(
                    title = "Account Details",
                    icon = Icons.Default.Person
                ) {
                    OutlinedTextField(
                        value = username, onValueChange = { username = it },
                        label = { Text("Username *") },
                        placeholder = { Text("e.g. jdoe") },
                        supportingText = {
                            if (username.isNotBlank() && !username.matches(Regex("^[a-zA-Z0-9._-]+$"))) {
                                Text("Only alphanumeric, hyphens, underscores, and periods",
                                    color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = fullName, onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. John Doe") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("user@example.com") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Leave blank to set later") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }

                // ── Home & Shell section ──
                SectionCard(
                    title = "Home & Shell",
                    icon = Icons.Default.Folder
                ) {
                    OutlinedTextField(
                        value = home, onValueChange = { home = it },
                        label = { Text("Home Directory") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = shell, onValueChange = { shell = it },
                        label = { Text("Shell") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    if (uiState.nextUid != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Next available UID: ${uiState.nextUid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Options section ──
                SectionCard(
                    title = "Options",
                    icon = Icons.Default.Lock
                ) {
                    ToggleRow(label = "SMB Access", description = "Allow SMB share access",
                        checked = smb, onCheckedChange = { smb = it })
                    ToggleRow(label = "Locked", description = "Prevent account authentication",
                        checked = locked, onCheckedChange = { locked = it })
                    ToggleRow(label = "Password Disabled", description = "Disable password login",
                        checked = passwordDisabled, onCheckedChange = { passwordDisabled = it })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ToggleRow(label = "Create Group", description = "Auto-create primary group",
                        checked = groupCreate, onCheckedChange = { groupCreate = it })
                    ToggleRow(label = "Create Home", description = "Auto-create home directory",
                        checked = homeCreate, onCheckedChange = { homeCreate = it })
                }

                // ── Action buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.createUser(
                                System.UserCreate(
                                    username = username, full_name = fullName,
                                    email = email.takeIf { it.isNotBlank() },
                                    home = home, shell = shell,
                                    password = password.takeIf { it.isNotBlank() },
                                    locked = locked, smb = smb,
                                    password_disabled = passwordDisabled,
                                    group_create = groupCreate, home_create = homeCreate
                                )
                            )
                        },
                        enabled = !uiState.isCreating && isFormValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Create User")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
