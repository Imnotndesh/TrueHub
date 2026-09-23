package com.imnotndesh.truehub.ui.homepage.instancesettings.users

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: UserSettingsViewModel = hiltViewModel(key = userId.toString())
    val uiState by viewModel.detailState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    var editUsername by remember { mutableStateOf("") }
    var editFullName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editHome by remember { mutableStateOf("") }
    var editShell by remember { mutableStateOf("") }
    var editLocked by remember { mutableStateOf(false) }
    var editPasswordDisabled by remember { mutableStateOf(false) }
    var showTwoFactorSheet by remember { mutableStateOf(false) }
    var showPasswordSheet by remember { mutableStateOf(false) }
    var changePasswordValue by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()


    fun enterEditMode(user: System.UserCreateUpdateResult) {
        editUsername = user.username
        editFullName = user.full_name
        editEmail = user.email ?: ""
        editHome = user.home
        editShell = user.shell
        editLocked = user.locked
        editPasswordDisabled = user.password_disabled
        isEditing = true
    }

    LaunchedEffect(Unit) { viewModel.loadUserDetail(userId) }
    LaunchedEffect(uiState.deleteResult) {
        if (uiState.deleteResult == true) onNavigateBack()
    }

    val current = uiState.user
    var showDeleteDialog by remember { mutableStateOf(false) }

    val statusColor by animateColorAsState(
        targetValue = when {
            current?.locked == true -> Color(0xFFC62828)
            current?.builtin == true -> Color(0xFF1565C0)
            current?.roles?.contains("FULL_ADMIN") == true -> Color(0xFF6A1B9A)
            else -> Color(0xFF2E7D32)
        },
        label = "statusColor"
    )

    val statusLabel = when {
        current?.locked == true -> "Locked"
        current?.builtin == true -> "System"
        current?.roles?.contains("FULL_ADMIN") == true -> "Admin"
        else -> "Active"
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
            title = current?.username ?: "User",
            subtitle = current?.full_name ?: "User details",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = uiState.error,
            onDismissError = { viewModel.clearDetailError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> LoadingScreen("Loading user details")
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person, null,
                                    tint = statusColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    current.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    current.full_name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (current.email != null) {
                                    Text(
                                        current.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Surface(
                                color = statusColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isEditing) {
                        EditUserForm(
                            username = editUsername, onUsernameChange = { editUsername = it },
                            fullName = editFullName, onFullNameChange = { editFullName = it },
                            email = editEmail, onEmailChange = { editEmail = it },
                            home = editHome, onHomeChange = { editHome = it },
                            shell = editShell, onShellChange = { editShell = it },
                            locked = editLocked, onLockedChange = { editLocked = it },
                            passwordDisabled = editPasswordDisabled,
                            onPasswordDisabledChange = { editPasswordDisabled = it }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                            Button(
                                onClick = {
                                    viewModel.updateUser(
                                        userId,
                                        System.UserUpdate(
                                            username = editUsername.takeIf { it != current.username },
                                            full_name = editFullName.takeIf { it != current.full_name },
                                            email = editEmail.takeIf { it != (current.email ?: "") },
                                            home = editHome.takeIf { it != current.home },
                                            shell = editShell.takeIf { it != current.shell },
                                            locked = editLocked.takeIf { it != current.locked },
                                            password_disabled = editPasswordDisabled
                                                .takeIf { it != current.password_disabled }
                                        )
                                    )
                                },
                                enabled = !uiState.isUpdating && editUsername.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isUpdating) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Save")
                            }
                        }
                    } else {
                        // Account Info section
                        SectionCard(
                            title = "Account Info",
                            icon = Icons.Default.Person
                        ) {
                            DetailRow("UID", current.uid.toString())
                            DetailRow("Username", current.username)
                            DetailRow("Full Name", current.full_name)
                            DetailRow("Email", current.email ?: "—")
                            DetailRow("Home", current.home)
                            DetailRow("Shell", current.shell)
                        }

                        // Status & Permissions section
                        SectionCard(
                            title = "Status & Permissions",
                            icon = Icons.Default.Security
                        ) {
                            DetailRow("Built-in", if (current.builtin) "Yes" else "No")
                            DetailRow("Local", if (current.local) "Yes" else "No")
                            DetailRow("Locked", if (current.locked) "Yes" else "No")
                            DetailRow("SMB Access", if (current.smb) "Enabled" else "Disabled")
                            DetailRow("Password", if (current.password_disabled) "Disabled" else "Enabled")
                            DetailRow("SSH Password", if (current.ssh_password_enabled) "Enabled" else "Disabled")
                            DetailRow("2FA", if (current.twofactor_auth_configured) "Configured" else "Not configured")
                            DetailRow("Change Required", if (current.password_change_required) "Yes" else "No")
                        }

                        // Roles section
                        SectionCard(
                            title = "Roles & Groups",
                            icon = Icons.Default.AdminPanelSettings
                        ) {
                            DetailRow("Roles", current.roles.joinToString(", ").ifEmpty { "—" })
                            DetailRow(
                                "Primary Group",
                                current.group?.bsdgrp_group ?: "—"
                            )
                            DetailRow(
                                "GID",
                                current.group?.bsdgrp_gid?.toString() ?: "—"
                            )
                            DetailRow("Groups", current.groups.joinToString(", ").ifEmpty { "—" })
                        }

                        // Password section
                        SectionCard(
                            title = "Password",
                            icon = Icons.Default.Lock
                        ) {
                            DetailRow("Age (days)", current.password_age?.toString() ?: "—")
                            DetailRow("SMB Hash", if (current.smbhash != null && current.smbhash != "") "Present" else "—")
                            DetailRow("SID", current.sid ?: "—")
                        }
                        if (!current.builtin && !current.password_disabled) {
                            OutlinedButton(
                                onClick = { showPasswordSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Key, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Change Password")
                            }
                        }
                        if (current.twofactor_auth_configured) {
                            SectionCard(
                                title = "Two-Factor Authentication",
                                icon = Icons.Default.Security
                            ) {
                                Text(
                                    "2FA is currently configured for this account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showTwoFactorSheet = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Manage 2FA")
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { enterEditMode(current) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit")
                            }
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                enabled = !uiState.isDeleting && !current.builtin,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isDeleting) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
        if (showTwoFactorSheet && current != null) {
            ModalBottomSheet(
                onDismissRequest = { showTwoFactorSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Manage Two-Factor Authentication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "User: ${current.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    Button(
                        onClick = {
                            viewModel.renew2faSecret(current.username)
                        },
                        enabled = !uiState.isRenewing2fa,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isRenewing2fa) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Renew 2FA Secret")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.unset2faSecret(current.username)
                        },
                        enabled = !uiState.isUnsetting2fa,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isUnsetting2fa) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.RemoveModerator, null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Unset 2FA Secret")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showPasswordSheet && current != null) {
            ModalBottomSheet(
                onDismissRequest = { showPasswordSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Change Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Set a new password for ${current.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    OutlinedTextField(
                        value = changePasswordValue,
                        onValueChange = { changePasswordValue = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.changeUserPassword(current.username, changePasswordValue)
                        },
                        enabled = !uiState.isChangingPassword && changePasswordValue.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isChangingPassword) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Key, null, Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Set Password")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Handle results
        LaunchedEffect(uiState.passwordChangeResult) {
            if (uiState.passwordChangeResult == true) {
                showPasswordSheet = false
                changePasswordValue = ""
                viewModel.clearPasswordChangeResult()
            }
        }
        LaunchedEffect(uiState.unset2faResult) {
            if (uiState.unset2faResult == true) {
                showTwoFactorSheet = false
                viewModel.clearUnset2faResult()
                viewModel.loadUserDetail(userId)
            }
        }
        LaunchedEffect(uiState.renew2faResult) {
            if (uiState.renew2faResult != null) {
                showTwoFactorSheet = false
                viewModel.clearRenew2faResult()
            }
        }

    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = {
                Text("Are you sure you want to delete \"${current?.username}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteUser(userId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
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
private fun EditUserForm(
    username: String, onUsernameChange: (String) -> Unit,
    fullName: String, onFullNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    home: String, onHomeChange: (String) -> Unit,
    shell: String, onShellChange: (String) -> Unit,
    locked: Boolean, onLockedChange: (Boolean) -> Unit,
    passwordDisabled: Boolean, onPasswordDisabledChange: (Boolean) -> Unit
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
                    Icons.Default.Edit, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Edit User",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            OutlinedTextField(
                value = username, onValueChange = onUsernameChange,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = fullName, onValueChange = onFullNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = home, onValueChange = onHomeChange,
                label = { Text("Home Directory") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = shell, onValueChange = onShellChange,
                label = { Text("Shell") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Locked", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Switch(checked = locked, onCheckedChange = onLockedChange)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Password Disabled", style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Switch(
                    checked = passwordDisabled,
                    onCheckedChange = onPasswordDisabledChange
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
