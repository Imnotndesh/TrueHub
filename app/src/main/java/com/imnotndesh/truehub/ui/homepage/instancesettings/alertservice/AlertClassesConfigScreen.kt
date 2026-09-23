package com.imnotndesh.truehub.ui.homepage.instancesettings.alertservice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Alerts
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertClassesConfigScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AlertClassesConfigViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Configuration saved!")
            viewModel.clearSaveSuccess()
        }
    }

    val policyOptions = if (uiState.availablePolicies.isNotEmpty()) {
        uiState.availablePolicies.mapNotNull { raw ->
            runCatching { Alerts.AlertPolicies.valueOf(raw) }.getOrNull()
        }
    } else {
        Alerts.AlertPolicies.entries.toList()
    }
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

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
            title = "Alert Categories",
            subtitle = "Levels, policies & proactive support",
            isLoading = uiState.isLoading,
            isRefreshing = false,
            error = null,
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (uiState.isLoading) {
                LoadingScreen("Loading categories")
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            val isExpanded = expandedCategories[category.id] ?: false
                            CategorySection(
                                category = category,
                                isExpanded = isExpanded,
                                edits = uiState.edits,
                                policyOptions = policyOptions,
                                onToggleExpand = {
                                    expandedCategories[category.id] = !isExpanded
                                },
                                onLevelChanged = { classId, level -> viewModel.updateLevel(classId, level) },
                                onPolicyChanged = { classId, policy -> viewModel.updatePolicy(classId, policy) },
                                onProactiveChanged = { classId, proactive -> viewModel.updateProactive(classId, proactive) }
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { viewModel.save() },
                            enabled = !uiState.isSaving,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(Modifier.padding(end = 8.dp).size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private fun iconForCategory(categoryId: String): ImageVector = when (categoryId) {
    "APPLICATIONS" -> Icons.Default.Apps
    "AUDIT" -> Icons.Default.FactCheck
    "CERTIFICATES" -> Icons.Default.VerifiedUser
    "DIRECTORY_SERVICE" -> Icons.Default.Dns
    "HARDWARE" -> Icons.Default.Memory
    "KMIP" -> Icons.Default.Key
    "NETWORK" -> Icons.Default.Wifi
    "REPORTING" -> Icons.Default.Insights
    "SECURITY" -> Icons.Default.Security
    "SHARING" -> Icons.Default.FolderShared
    "STORAGE" -> Icons.Default.Storage
    "SYSTEM" -> Icons.Default.Tune
    "TASKS" -> Icons.Default.PendingActions
    "TRUENAS_CONNECT" -> Icons.Default.Cloud
    "UPS" -> Icons.Default.BatteryChargingFull
    else -> Icons.Default.NotificationsActive
}

private fun colorForLevel(level: Alerts.AlertLevels): Color = when (level) {
    Alerts.AlertLevels.INFO -> Color(0xFF1976D2)
    Alerts.AlertLevels.NOTICE -> Color(0xFF00897B)
    Alerts.AlertLevels.WARNING -> Color(0xFFF57C00)
    Alerts.AlertLevels.ERROR -> Color(0xFFD84315)
    Alerts.AlertLevels.CRITICAL -> Color(0xFFC62828)
    Alerts.AlertLevels.ALERT -> Color(0xFFAD1457)
    Alerts.AlertLevels.EMERGENCY -> Color(0xFF6A1B9A)
}

@Composable
private fun CategorySection(
    category: System.AlertCategoriesResponse,
    isExpanded: Boolean,
    edits: Map<String, AlertClassEditState>,
    policyOptions: List<Alerts.AlertPolicies>,
    onToggleExpand: () -> Unit,
    onLevelChanged: (String, Alerts.AlertLevels) -> Unit,
    onPolicyChanged: (String, Alerts.AlertPolicies) -> Unit,
    onProactiveChanged: (String, Boolean) -> Unit
) {
    val rotation by animateColorAsState(Color.Transparent, label = "noop")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForCategory(category.id),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${category.classes.size} class${if (category.classes.size == 1) "" else "es"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(if (isExpanded) 180f else 0f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    category.classes.forEach { cls ->
                        val edit = edits[cls.id]
                        if (edit != null) {
                            AlertClassRow(
                                title = cls.title,
                                edit = edit,
                                policyOptions = policyOptions,
                                onLevelChanged = { onLevelChanged(cls.id, it) },
                                onPolicyChanged = { onPolicyChanged(cls.id, it) },
                                onProactiveChanged = { onProactiveChanged(cls.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertClassRow(
    title: String,
    edit: AlertClassEditState,
    policyOptions: List<Alerts.AlertPolicies>,
    onLevelChanged: (Alerts.AlertLevels) -> Unit,
    onPolicyChanged: (Alerts.AlertPolicies) -> Unit,
    onProactiveChanged: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val levelColor by animateColorAsState(colorForLevel(edit.level), label = "levelColor")

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(color = levelColor.copy(alpha = 0.14f), shape = RoundedCornerShape(100.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(levelColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = edit.level.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = levelColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LevelDropdown(
                            selected = edit.level,
                            onSelected = onLevelChanged,
                            modifier = Modifier.weight(1f)
                        )
                        PolicyDropdown(
                            selected = edit.policy,
                            options = policyOptions,
                            onSelected = onPolicyChanged,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Proactive Support",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = edit.proactiveSupport, onCheckedChange = onProactiveChanged)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelDropdown(
    selected: Alerts.AlertLevels,
    onSelected: (Alerts.AlertLevels) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = modifier, expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Level") },
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Alerts.AlertLevels.entries.forEach { lvl ->
                DropdownMenuItem(text = { Text(lvl.name) }, onClick = { onSelected(lvl); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PolicyDropdown(
    selected: Alerts.AlertPolicies,
    options: List<Alerts.AlertPolicies>,
    onSelected: (Alerts.AlertPolicies) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = modifier, expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Policy") },
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { pol ->
                DropdownMenuItem(text = { Text(pol.name) }, onClick = { onSelected(pol); expanded = false })
            }
        }
    }
}