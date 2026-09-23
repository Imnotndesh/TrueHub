package com.imnotndesh.truehub.ui.homepage.instancesettings.users

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader


@Composable
fun UserListScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToUserDetail: (Int) -> Unit = {},
    onNavigateToCreateUser: () -> Unit = {},
    onNavigateToSetupAdmin: () -> Unit = {},
    ) {
    val viewModel: UserSettingsViewModel = hiltViewModel()
    val uiState by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadUsers() }

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
            title = "Users",
            subtitle = "${uiState.filteredUsers.size} / ${uiState.users.size} user(s)",
            isLoading = uiState.isLoading,
            isRefreshing = uiState.isRefreshing,
            error = uiState.error,
            onDismissError = { viewModel.clearListError() },
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                UserFabMenu(
                    onCreateUserClick = onNavigateToCreateUser,
                    onSetupAdminClick = onNavigateToSetupAdmin
                )
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshUsers() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search users…") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Filter chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterLocked == null
                                        && uiState.filterSmb == null
                                        && uiState.filterBuiltin == null
                                        && uiState.filterRole == null,
                                onClick = {
                                    viewModel.setFilterLocked(null)
                                    viewModel.setFilterSmb(null)
                                    viewModel.setFilterBuiltin(null)
                                    viewModel.setFilterRole(null)
                                },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterLocked == true
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterLocked(if (sel) null else true)
                                },
                                label = { Text("Locked") },
                                leadingIcon = if (sel) null else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterLocked == false
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterLocked(if (sel) null else false)
                                },
                                label = { Text("Unlocked") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterSmb == true
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterSmb(if (sel) null else true)
                                },
                                label = { Text("SMB") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterBuiltin == true
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterBuiltin(if (sel) null else true)
                                },
                                label = { Text("System") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterBuiltin == false
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterBuiltin(if (sel) null else false)
                                },
                                label = { Text("Local") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                        item {
                            val sel = uiState.filterRole == "FULL_ADMIN"
                            FilterChip(
                                selected = sel,
                                onClick = {
                                    viewModel.setFilterRole(if (sel) null else "FULL_ADMIN")
                                },
                                label = { Text("Admin") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        }
                    }

                    // User list
                    when {
                        uiState.isLoading -> LoadingScreen("Loading users...")
                        uiState.filteredUsers.isEmpty() -> EmptyUsersState(
                            hasFilters = uiState.searchQuery.isNotBlank() ||
                                    uiState.filterLocked != null ||
                                    uiState.filterSmb != null ||
                                    uiState.filterBuiltin != null ||
                                    uiState.filterRole != null
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.filteredUsers, key = { it.id!! }) { user ->
                                UserCard(
                                    user = user,
                                    onClick = { onNavigateToUserDetail(user.id!!) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UserFabMenu(
    onCreateUserClick: () -> Unit,
    onSetupAdminClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    BackHandler(expanded) { expanded = false }

    FloatingActionButtonMenu(
        expanded = expanded,
        horizontalAlignment = Alignment.End,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (expanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip(
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Assertive
                            paneTitle = "User creation options"
                        }
                    ) {
                        Text(if (expanded) "Close" else "Add user")
                    }
                },
                state = rememberTooltipState()
            ) {
                ToggleFloatingActionButton(
                    modifier = Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = if (expanded) "Expanded" else "Collapsed"
                            contentDescription = "User creation options"
                        }
                        .animateFloatingActionButton(
                            visible = true,
                            alignment = Alignment.BottomEnd
                        ),
                    checked = expanded,
                    onCheckedChange = { expanded = it },
                    containerColor = { progress ->
                        androidx.compose.ui.graphics.lerp(
                            colorScheme.primaryContainer,
                            colorScheme.primaryContainer,
                            progress
                        )
                    }
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = { expanded = false; onCreateUserClick() },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
            text = { Text("Create User") }
        )
        FloatingActionButtonMenuItem(
            onClick = { expanded = false; onSetupAdminClick() },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
            text = { Text("Setup Local Admin") }
        )
    }
}

@Composable
private fun EmptyUsersState(hasFilters: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasFilters) "No users match your filters"
            else "No users found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasFilters) "Try adjusting your search or filter criteria"
            else "Tap the + button to add a new user",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun UserCard(
    user: System.UserCreateUpdateResult,
    onClick: () -> Unit
) {
    val isLocked = user.locked
    val statusColor by animateColorAsState(
        targetValue = when {
            isLocked -> Color(0xFFC62828)
            user.builtin -> Color(0xFF1565C0)
            user.roles.contains("FULL_ADMIN") -> Color(0xFF6A1B9A)
            else -> Color(0xFF2E7D32)
        },
        label = "statusColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (user.builtin) Icons.Default.PersonOff
                    else Icons.Default.Person,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    user.full_name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(100.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLocked) {
                        Icon(
                            Icons.Default.Lock, null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isLocked -> "Locked"
                            user.builtin -> "System"
                            user.roles.contains("FULL_ADMIN") -> "Admin"
                            else -> "Active"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
