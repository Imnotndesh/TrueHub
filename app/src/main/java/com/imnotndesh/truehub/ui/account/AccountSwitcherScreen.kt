package com.imnotndesh.truehub.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.PersonalizationManager
import com.imnotndesh.truehub.data.models.AccountProfile
import com.imnotndesh.truehub.data.models.LoginMethod
import com.imnotndesh.truehub.data.models.SavedAccount
import com.imnotndesh.truehub.data.models.SavedServer
import com.imnotndesh.truehub.ui.background.AnimatedWavyGradientBackground
import com.imnotndesh.truehub.ui.setup.ServerConfigBottomSheet
import kotlinx.coroutines.launch

@Composable
fun AccountSwitcherScreen(
    onAccountSelected: (SavedServer, SavedAccount) -> Unit,
    onAddNewAccount: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf<List<AccountProfile>>(emptyList()) }
    var savedServers by remember { mutableStateOf<List<SavedServer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf<AccountProfile?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showServerPicker by remember { mutableStateOf(false) }
    var showSetupSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val servers = MultiAccountPrefs.getServers(context)
        savedServers = servers
        loadProfiles(context) { loaded ->
            profiles = loaded
            isLoading = false
        }
    }

    fun reload() {
        scope.launch {
            val servers = MultiAccountPrefs.getServers(context)
            savedServers = servers
            loadProfiles(context) { loaded ->
                profiles = loaded
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedWavyGradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Use systemBars to automatically add padding below the status bar
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(horizontal = 24.dp)
            ) {
                // Header
                Text(
                    text = "Select Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = if (profiles.isEmpty()) {
                        "No saved accounts yet"
                    } else {
                        "${profiles.size} saved account${if (profiles.size != 1) "s" else ""}"
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(profiles) { profile ->
                            AccountProfileCard(
                                profile = profile,
                                onClick = { onAccountSelected(profile.server, profile.account) },
                                onDelete = { showDeleteDialog = profile }
                            )
                        }
                    }
                }

                // Split button: primary action + dropdown of additional options
                Box(modifier = Modifier.fillMaxWidth()) {
                    SplitButtonRow(
                        primaryText = "Add New Account",
                        primaryIcon = Icons.Default.Add,
                        onPrimaryClick = {
                            // If multiple saved servers exist, ask the user which server to add an account to.
                            if (savedServers.size > 1) {
                                showServerPicker = true
                            } else {
                                onAddNewAccount()
                            }
                        },
                        onMenuClick = { showAddMenu = true }
                    )

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add New Account") },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            onClick = {
                                showAddMenu = false
                                if (savedServers.size > 1) {
                                    showServerPicker = true
                                } else {
                                    onAddNewAccount()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add another server") },
                            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                            onClick = {
                                showAddMenu = false
                                showSetupSheet = true
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Delete all saved credentials", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showAddMenu = false
                                showDeleteAllDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Delete a single account ─────────────────────────────
        showDeleteDialog?.let { profile ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Account") },
                text = {
                    Text("Are you sure you want to delete '${profile.displayName}'? This will remove saved credentials.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                MultiAccountPrefs.deleteAccount(context, profile.account.id)
                                PersonalizationManager.deleteForUser(context, profile.account.id)
                                reload()
                                showDeleteDialog = null
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ── Delete all saved credentials ────────────────────────
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Delete All Credentials") },
                text = {
                    Text("Are you sure you want to delete ALL saved accounts and credentials? This cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAllDialog = false
                            scope.launch {
                                val accounts = MultiAccountPrefs.getAccounts(context)
                                accounts.forEach { account ->
                                    MultiAccountPrefs.deleteAccount(context, account.id)
                                    PersonalizationManager.deleteForUser(context, account.id)
                                }
                                reload()
                            }
                        }
                    ) {
                        Text("Delete All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ── Server picker for adding an account ────────────────
        if (showServerPicker) {
            ServerPickerDialog(
                servers = savedServers,
                onDismiss = { showServerPicker = false },
                onSelect = { server ->
                    showServerPicker = false
                    onAddNewAccount()
                }
            )
        }

        // ── Add a new server (setup screen) ────────────────────
        if (showSetupSheet) {
            ServerConfigBottomSheet(
                onDismiss = { showSetupSheet = false },
                onConfigured = { url, insecure ->
                    scope.launch {
                        val server = SavedServer(
                            serverUrl = url,
                            insecure = insecure
                        )
                        MultiAccountPrefs.saveServer(context, server)
                        reload()
                    }
                    showSetupSheet = false
                }
            )
        }
    }
}

@Composable
private fun SplitButtonRow(
    primaryText: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onPrimaryClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = primaryIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            onClick = onMenuClick,
            modifier = Modifier
                .width(56.dp)
                .height(56.dp),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
        }
    }
}

@Composable
private fun ServerPickerDialog(
    servers: List<SavedServer>,
    onDismiss: () -> Unit,
    onSelect: (SavedServer) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Server") },
        text = {
            Column {
                Text(
                    "Multiple servers are saved. Pick which server to add an account to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (servers.isEmpty()) {
                    Text("No servers saved.")
                } else {
                    servers.forEach { server ->
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(server) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = server.nickname ?: "Unnamed Server",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                server.serverUrl.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AccountProfileCard(
    profile: AccountProfile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (profile.account.loginMethod) {
                        LoginMethod.PASSWORD, LoginMethod.TOTP -> Icons.Default.Person
                        LoginMethod.API_KEY -> Icons.Default.Key
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Account Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.account.username,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = profile.server.nickname ?: profile.server.serverUrl,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (profile.account.autoLoginEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Auto-login enabled",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Icons (Delete and Chevron)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete account",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select account",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private suspend fun loadProfiles(
    context: android.content.Context,
    onLoaded: (List<AccountProfile>) -> Unit
) {
    val servers = MultiAccountPrefs.getServers(context)
    val accounts = MultiAccountPrefs.getAccounts(context)

    val profiles = accounts.mapNotNull { account ->
        val server = servers.find { it.id == account.serverId }
        if (server != null) {
            AccountProfile(server, account)
        } else null
    }.sortedByDescending { it.account.lastUsed }

    onLoaded(profiles)
}
