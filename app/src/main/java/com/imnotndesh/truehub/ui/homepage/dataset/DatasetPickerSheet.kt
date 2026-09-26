package com.imnotndesh.truehub.ui.homepage.dataset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.models.Storage
/**
 * Configuration for the DatasetPickerSheet.
 *
 * @param title Title shown in the sheet header
 * @param confirmLabel Label for the confirm button
 * @param allowMultiSelect Whether the user can select multiple datasets
 * @param filterPredicate Optional filter to restrict which datasets appear (e.g. only FILESYSTEM type)
 * @param initialPoolName If provided, auto-expands this pool's root on open
 */
data class DatasetPickerConfig(
    val title: String = "Select Dataset",
    val confirmLabel: String = "Select",
    val allowMultiSelect: Boolean = false,
    val filterPredicate: ((Storage.ZfsDataset) -> Boolean)? = null,
    val initialPoolName: String? = null
)

internal fun fullDatasetMountPath(name: String, mountpoint: String?): String {
    return mountpoint?.trim()?.takeIf { it.isNotBlank() }
        ?: "/mnt/${name.trimStart('/')}"
}

fun Storage.ZfsDataset.toFullMountPath(): String = fullDatasetMountPath(name, mountpoint)

/**
 * A reusable dataset picker bottom sheet, similar to the system file picker.
 * Can be launched from any screen to let the user pick one or more datasets.
 *
 * Usage:
 * ```
 * var showPicker by remember { mutableStateOf(false) }
 * if (showPicker) {
 *     DatasetPickerSheet(
 *         manager = manager,
 *         datasets = flatDatasetList,           // pass your already-loaded list, or null to load internally
 *         config = DatasetPickerConfig(title = "Choose a target"),
 *         onConfirm = { selected -> /* use selected datasets */ showPicker = false },
 *         onDismiss = { showPicker = false }
 *     )
 * }
 * ```
 *
 * @param manager TrueNAS API manager (used if [datasets] is null)
 * @param datasets Pre-loaded flat dataset list. If null, the sheet will load datasets itself.
 * @param config Picker behavior configuration
 * @param onConfirm Called with the selected dataset(s) when the user confirms
 * @param onDismiss Called when the user dismisses without confirming
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetPickerSheet(
    manager: TrueNASApiManager,
    datasets: List<Storage.ZfsDataset>? = null,
    config: DatasetPickerConfig = DatasetPickerConfig(),
    onConfirm: (List<Storage.ZfsDataset>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var flatList by remember { mutableStateOf(datasets ?: emptyList()) }
    var isLoading by remember { mutableStateOf(datasets == null) }
    var searchQuery by remember { mutableStateOf("") }
    val expandedIds = remember { mutableStateListOf<String>() }
    val selectedIds = remember { mutableStateListOf<String>() }

    // Load datasets if not provided
    LaunchedEffect(Unit) {
        if (datasets == null) {
            try {
                val result = manager.storage.getAllDatasets()
                if (result is com.imnotndesh.truehub.data.ApiResult.Success) {
                    flatList = result.data
                    // Auto-expand root of initialPoolName
                    config.initialPoolName?.let { pool ->
                        flatList.find { it.name == pool }?.let { root ->
                            expandedIds.add(root.id)
                        }
                    }
                }
            } finally {
                isLoading = false
            }
        } else {
            config.initialPoolName?.let { pool ->
                flatList.find { it.name == pool }?.let { root ->
                    expandedIds.add(root.id)
                }
            }
        }
    }

    val roots = remember(flatList) {
        flatList.filter { ds ->
            flatList.none { other ->
                ds.name.startsWith("${other.name}/") &&
                        ds.name.removePrefix("${other.name}/").isNotEmpty() &&
                        !ds.name.removePrefix("${other.name}/").contains("/")
            } && !ds.name.contains("/")
        }
    }
    val searchResults = remember(flatList, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else flatList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val isSearching = searchQuery.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            PickerHeader(
                title = config.title,
                selectedCount = selectedIds.size,
                onClose = onDismiss
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ── Search bar ────────────────────────────────────────────────────
            PickerSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Content ───────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    isSearching -> {
                        PickerSearchResults(
                            results = searchResults,
                            selectedIds = selectedIds,
                            allowMultiSelect = config.allowMultiSelect,
                            filterPredicate = config.filterPredicate,
                            onToggle = { ds ->
                                if (config.allowMultiSelect) {
                                    if (selectedIds.contains(ds.id)) selectedIds.remove(ds.id)
                                    else selectedIds.add(ds.id)
                                } else {
                                    selectedIds.clear()
                                    selectedIds.add(ds.id)
                                }
                            }
                        )
                    }
                    else -> {
                        PickerTreeView(
                            roots = roots,
                            flatList = flatList,
                            expandedIds = expandedIds,
                            selectedIds = selectedIds,
                            allowMultiSelect = config.allowMultiSelect,
                            filterPredicate = config.filterPredicate,
                            onToggleExpand = { id ->
                                if (expandedIds.contains(id)) expandedIds.remove(id)
                                else expandedIds.add(id)
                            },
                            onToggleSelect = { ds ->
                                if (config.allowMultiSelect) {
                                    if (selectedIds.contains(ds.id)) selectedIds.remove(ds.id)
                                    else selectedIds.add(ds.id)
                                } else {
                                    selectedIds.clear()
                                    selectedIds.add(ds.id)
                                }
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            PickerFooter(
                confirmLabel = config.confirmLabel,
                selectedCount = selectedIds.size,
                onCancel = onDismiss,
                onConfirm = {
                    val selected = flatList.filter { selectedIds.contains(it.id) }
                    onConfirm(selected)
                }
            )
        }
    }
}

@Composable
private fun PickerHeader(title: String, selectedCount: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selectedCount > 0) {
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

@Composable
private fun PickerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search datasets...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PickerSearchResults(
    results: List<Storage.ZfsDataset>,
    selectedIds: List<String>,
    allowMultiSelect: Boolean,
    filterPredicate: ((Storage.ZfsDataset) -> Boolean)?,
    onToggle: (Storage.ZfsDataset) -> Unit
) {
    val filtered = if (filterPredicate != null) results.filter(filterPredicate) else results
    if (filtered.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No matching datasets", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(filtered, key = { it.id }) { ds ->
            val isSelected = selectedIds.contains(ds.id)
            PickerListItem(
                dataset = ds,
                isSelected = isSelected,
                showFullPath = true,
                onClick = { onToggle(ds) }
            )
        }
    }
}

@Composable
private fun PickerTreeView(
    roots: List<Storage.ZfsDataset>,
    flatList: List<Storage.ZfsDataset>,
    expandedIds: List<String>,
    selectedIds: List<String>,
    allowMultiSelect: Boolean,
    filterPredicate: ((Storage.ZfsDataset) -> Boolean)?,
    onToggleExpand: (String) -> Unit,
    onToggleSelect: (Storage.ZfsDataset) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        fun buildVisibleItems(
            ds: Storage.ZfsDataset,
            level: Int,
            acc: MutableList<Pair<Storage.ZfsDataset, Int>>
        ) {
            val passesFilter = filterPredicate?.invoke(ds) ?: true
            val hasChildren = ds.children.isNotEmpty()
            if (!passesFilter && !hasChildren) return

            acc.add(ds to level)
            if (expandedIds.contains(ds.id)) {
                ds.children.forEach { child -> buildVisibleItems(child, level + 1, acc) }
            }
        }

        val visibleItems = mutableListOf<Pair<Storage.ZfsDataset, Int>>()
        roots.forEach { buildVisibleItems(it, 0, visibleItems) }

        items(visibleItems, key = { it.first.id }) { (ds, level) ->
            val isExpanded = expandedIds.contains(ds.id)
            val isSelected = selectedIds.contains(ds.id)
            val hasChildren = ds.children.isNotEmpty()
            val passesFilter = filterPredicate?.invoke(ds) ?: true

            PickerTreeItem(
                dataset = ds,
                level = level,
                isExpanded = isExpanded,
                isSelected = isSelected,
                hasChildren = hasChildren,
                isSelectable = passesFilter,
                onToggleExpand = { onToggleExpand(ds.id) },
                onSelect = { onToggleSelect(ds) }
            )
        }
    }
}

@Composable
private fun PickerTreeItem(
    dataset: Storage.ZfsDataset,
    level: Int,
    isExpanded: Boolean,
    isSelected: Boolean,
    hasChildren: Boolean,
    isSelectable: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "chevron"
    )

    Surface(
        onClick = {
            if (isSelectable) onSelect()
            if (hasChildren) onToggleExpand()
        },
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 20).dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand chevron
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                if (hasChildren) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation)
                            .clickable { onToggleExpand() }
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            // Folder icon
            Icon(
                imageVector = if (level == 0) Icons.Default.Storage
                else if (isExpanded) Icons.Default.FolderOpen
                else Icons.Default.Folder,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else if (level == 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Name
            Text(
                text = if (level == 0) dataset.name
                else dataset.name.substringAfterLast("/"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || level == 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else if (!isSelectable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            // Selected checkmark
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerListItem(
    dataset: Storage.ZfsDataset,
    isSelected: Boolean,
    showFullPath: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dataset.name.substringAfterLast("/").ifEmpty { dataset.name },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showFullPath) {
                    Text(
                        text = dataset.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerFooter(
    confirmLabel: String,
    selectedCount: Int,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = onConfirm,
            enabled = selectedCount > 0,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = if (selectedCount > 1) "$confirmLabel ($selectedCount)" else confirmLabel,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}