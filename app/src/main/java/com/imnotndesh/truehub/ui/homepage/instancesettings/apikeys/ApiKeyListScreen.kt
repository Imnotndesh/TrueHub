package com.imnotndesh.truehub.ui.homepage.instancesettings.apikeys

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApiKeyListScreen(
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateToCreate: () -> Unit = {}
) {
    val vm: ApiKeyViewModel = hiltViewModel()
    val uiState by vm.listState.collectAsState()

    LaunchedEffect(Unit) { vm.loadKeys() }

    Column(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer))
    )) {
        UnifiedScreenHeader(
            title = "API Keys",
            subtitle = "${uiState.filteredKeys.size} / ${uiState.keys.size} key(s)",
            isLoading = uiState.isLoading, isRefreshing = uiState.isRefreshing,
            error = uiState.error, onDismissError = { vm.clearListError() },
            manager = manager, onBackPressed = onNavigateBack
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                var expanded by rememberSaveable { mutableStateOf(false) }
                val cs = MaterialTheme.colorScheme
                BackHandler(expanded) { expanded = false }
                FloatingActionButtonMenu(
                    expanded = expanded,
                    horizontalAlignment = Alignment.End,
                    button = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                if (expanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above),
                            tooltip = { PlainTooltip { Text(if (expanded) "Close" else "Add key") } },
                            state = rememberTooltipState()
                        ) {
                            ToggleFloatingActionButton(
                                modifier = Modifier.animateFloatingActionButton(true, Alignment.BottomEnd),
                                checked = expanded, onCheckedChange = { expanded = it },
                                containerColor = { androidx.compose.ui.graphics.lerp(cs.primaryContainer, cs.primaryContainer, it) }
                            ) {
                                val v by remember { derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add } }
                                Icon(rememberVectorPainter(v), null, tint = cs.primary, modifier = Modifier.animateIcon({ checkedProgress }))
                            }
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = { expanded = false; onNavigateToCreate() },
                        icon = { Icon(Icons.Default.Add, null) }, text = { Text("New API Key") })
                    FloatingActionButtonMenuItem(
                        onClick = { expanded = false; vm.toggleMyKeys() },
                        icon = { Icon(Icons.Default.Key, null) },
                        text = { Text(if (uiState.showMyKeys) "All Keys" else "My Keys") })
                }
            }
        ) { innerPadding ->
            PullToRefreshBox(isRefreshing = uiState.isRefreshing, onRefresh = { vm.refreshKeys() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(value = uiState.searchQuery, onValueChange = { vm.setSearchQuery(it) },
                        placeholder = { Text("Search keys…") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = { if (uiState.searchQuery.isNotEmpty()) IconButton(onClick = { vm.setSearchQuery("") }) { Icon(Icons.Default.Clear, null) } },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), unfocusedContainerColor = MaterialTheme.colorScheme.surface))

                    LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val allSel = uiState.filterRevoked == null && uiState.filterLocal == null && !uiState.showMyKeys
                        item { FilterChip(selected = allSel, onClick = {
                            if (uiState.showMyKeys) vm.toggleMyKeys()
                            vm.setFilterRevoked(null); vm.setFilterLocal(null)
                        }, label = { Text("All") }) }
                        item { FilterChip(selected = uiState.showMyKeys, onClick = { vm.toggleMyKeys() }, label = { Text("My Keys") }) }
                        item { FilterChip(selected = uiState.filterRevoked == false && !uiState.showMyKeys,
                            onClick = {
                                if (uiState.showMyKeys) vm.toggleMyKeys()
                                vm.setFilterRevoked(if (uiState.filterRevoked == false) null else false)
                            }, label = { Text("Active") }, enabled = !uiState.showMyKeys) }
                        item { FilterChip(selected = uiState.filterRevoked == true && !uiState.showMyKeys,
                            onClick = {
                                if (uiState.showMyKeys) vm.toggleMyKeys()
                                vm.setFilterRevoked(if (uiState.filterRevoked == true) null else true)
                            }, label = { Text("Revoked") }, enabled = !uiState.showMyKeys) }
                        item { FilterChip(selected = uiState.filterLocal == true && !uiState.showMyKeys,
                            onClick = {
                                if (uiState.showMyKeys) vm.toggleMyKeys()
                                vm.setFilterLocal(if (uiState.filterLocal == true) null else true)
                            }, label = { Text("Local") }, enabled = !uiState.showMyKeys) }
                    }



                    when {
                        uiState.isLoading -> LoadingScreen("Loading API keys...")
                        uiState.filteredKeys.isEmpty() -> Unit // Empty state
                        else -> LazyColumn(Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.filteredKeys, key = { it.id }) { key ->
                                KeyCard(key = key, onClick = { onNavigateToDetail(key.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyCard(key: System.ApiKeyEntry, onClick: () -> Unit) {
    val sc = animateColorAsState(if (key.revoked) Color(0xFFC62828) else Color(0xFF2E7D32), label = "")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(sc.value.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Key, null, tint = sc.value, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(key.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(key.username ?: "System", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Surface(color = sc.value.copy(alpha = 0.12f), shape = RoundedCornerShape(100.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(sc.value))
                    Spacer(Modifier.width(6.dp))
                    Text(if (key.revoked) "Revoked" else "Active",
                        style = MaterialTheme.typography.labelSmall, color = sc.value, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}
