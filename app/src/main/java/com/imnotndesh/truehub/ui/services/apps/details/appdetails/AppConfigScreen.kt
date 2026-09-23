package com.imnotndesh.truehub.ui.services.apps.details.appdetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel
import com.imnotndesh.truehub.ui.services.apps.details.marketplace.SchemaGroupSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

data class AppConfigPageValues (
    val appName: String = "",
    val appTrain : String = ""
)
object AppConfigEditCache {
    private val cache = mutableMapOf<String, Map<String, Any?>>()
    fun save(appName: String, state: Map<String, Any?>) {
        cache[appName] = state.toMap()
    }
    fun load(appName: String): Map<String, Any?>? = cache[appName]

    fun clear(appName: String) { cache.remove(appName) }
}
@Composable
fun AppConfigScreen(
    manager: TrueNASApiManager,
    appValues: AppConfigPageValues,
    onNavigateBack: () -> Unit
) {
    val viewModel: AppsScreenViewModel = hiltViewModel()
    val configState by viewModel.appConfigState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val formState = remember { mutableStateMapOf<String, Any?>() }
    var formReady by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activeJobsMap by JobRepository.activeJobs.collectAsState()
    val trackedJob = configState.saveJobId?.let { gid -> activeJobsMap[gid] }

    LaunchedEffect(appValues.appName) {
        formReady = false
        viewModel.clearAppConfigError()
        viewModel.loadAppConfig(appValues.appName)
        viewModel.loadCatalogAppDetails(appValues.appName, appValues.appTrain)
    }
    LaunchedEffect(formReady) {
        if (!formReady) return@LaunchedEffect
        snapshotFlow { formState.toMap() }
            .debounce(500)
            .collect { snapshot ->
                AppConfigEditCache.save(appValues.appName, snapshot)
            }
    }
    LaunchedEffect(trackedJob) {
        if (trackedJob != null) {
            when (trackedJob.state.uppercase()) {
                "SUCCESS" -> {
                    AppConfigEditCache.clear(appValues.appName)
                    viewModel.clearAppConfigError()
                    onNavigateBack()
                }
                "FAILED", "ABORTED" -> {

                }
            }
        }
    }

    LaunchedEffect(uiState.catalogAppDetails, configState.config) {
        val details = uiState.catalogAppDetails ?: return@LaunchedEffect
        val liveConfig = configState.config ?: return@LaunchedEffect
        val versionKey = details.versions?.keys?.firstOrNull() ?: return@LaunchedEffect
        val versionData = details.versions[versionKey]

        formState.clear()
        flattenDefaults(versionData?.values ?: emptyMap(), formState)
        flattenDefaults(liveConfig, formState)
        AppConfigEditCache.load(appValues.appName)?.forEach { (k, v) ->
            formState[k] = v
        }
        formReady = true
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCatalogAppDetails() }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (formReady) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                AppConfigEditCache.clear(appValues.appName)
                                val details = uiState.catalogAppDetails
                                val versionKey = details?.versions?.keys?.firstOrNull()
                                val versionData = details?.versions?.get(versionKey)
                                formState.clear()
                                flattenDefaults(versionData?.values ?: emptyMap(), formState)
                                flattenDefaults(configState.config ?: emptyMap(), formState)
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !configState.isSaving
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discard", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.updateAppConfig(context, appValues.appName, unflattenMap(formState))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !configState.isSaving,
                            colors = when {
                                configState.saveSuccess -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )

                                configState.saveFailed -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                else -> ButtonDefaults.buttonColors()
                            }
                        ) {
                            when {
                                configState.isSaving -> CircularProgressIndicator(
                                    modifier = Modifier.size(
                                        20.dp
                                    ),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                configState.saveSuccess -> {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Saved!", fontWeight = FontWeight.Bold)
                                }

                                configState.saveFailed -> {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Failed — Retry", fontWeight = FontWeight.Bold)
                                }

                                else -> {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Changes", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val listState = rememberLazyListState()
        val density = LocalDensity.current
        val collapseThreshold = remember(density) { with(density) { 32.dp.roundToPx() } }
        val collapsed by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > collapseThreshold
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(targetState = collapsed, label = "configHeader") { isCollapsed ->
                if (isCollapsed) {
                    MinimalBackHeader(
                        onBackPressed = onNavigateBack,
                        error = configState.error,
                        onDismissError = { viewModel.clearAppConfigError() },
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                } else {
                    UnifiedScreenHeader(
                        title = "App Configuration",
                        subtitle = appValues.appName,
                        isLoading = configState.isLoading,
                        isRefreshing = false,
                        error = configState.error,
                        onDismissError = { viewModel.clearAppConfigError() },
                        manager = manager,
                        onBackPressed = onNavigateBack
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
            when {
                configState.isLoading || uiState.isLoadingCatalogDetails || !formReady -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                configState.config == null && configState.error == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No configuration available")
                    }
                }

                else -> {
                    val schema = uiState.catalogAppDetails?.versions
                        ?.get(uiState.catalogAppDetails?.versions?.keys?.firstOrNull())?.schema

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (schema?.groups != null && schema.questions != null) {
                            schema.groups.forEach { group ->
                                val groupQuestions =
                                    schema.questions.filter { it.group == group.name }
                                if (groupQuestions.isNotEmpty()) {
                                    item(key = group.name) {
                                        SchemaGroupSection(
                                            group = group,
                                            questions = groupQuestions,
                                            formState = formState,
                                            modifier = Modifier.padding(horizontal = 0.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            }
            if (configState.isSaving || configState.saveFailed || configState.saveJobId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (configState.saveFailed) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Update Failed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = configState.saveError ?: "Unknown error occurred.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.clearAppConfigError() },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Dismiss")
                            }
                        } else {
                            val progressValue = (trackedJob?.progress ?: 0) / 100f
                            val progressPercent = trackedJob?.progress ?: 0
                            val stepDescription =
                                trackedJob?.description ?: "Applying configuration changes..."

                            CircularProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier.size(84.dp),
                                strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Updating Application",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stepDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
/**
 * Recursive composable that displays an editable field for any JSON-like value.
 */
@Composable
private fun ConfigEditorRow(
    key: String,
    value: Any?,
    onValueChange: (Any?) -> Unit,
    modifier: Modifier = Modifier,
    depth: Int = 0
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = if (depth == 0) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (depth > 0) {
                    // For nested fields, show a small indicator
                    Text(
                        text = "nested",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Value editor based on type
            when (value) {
                is Map<*, *> -> {
                    // Recursively edit each entry in the map
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        value.forEach { (subKey, subValue) ->
                            ConfigEditorRow(
                                key = subKey.toString(),
                                value = subValue,
                                onValueChange = { newSubValue ->
                                    val updatedMap = (value as Map<String, Any?>).toMutableMap()
                                    updatedMap[subKey.toString()] = newSubValue
                                    onValueChange(updatedMap)
                                },
                                depth = depth + 1
                            )
                        }
                    }
                }
                is List<*> -> {
                    // For simplicity, treat lists as editable text (JSON representation)
                    var listText by remember(value) { mutableStateOf(value.joinToString(separator = ", ")) }
                    OutlinedTextField(
                        value = listText,
                        onValueChange = { newText ->
                            listText = newText
                            onValueChange(newText.split(",").map { it.trim() })
                        },
                        label = { Text("List (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
                is Boolean -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = value,
                            onCheckedChange = onValueChange
                        )
                    }
                }
                is Number -> {
                    OutlinedTextField(
                        value = value.toString(),
                        onValueChange = { newText ->
                            newText.toDoubleOrNull()?.let { num ->
                                // Preserve original type (Int/Long/Double)
                                when (value) {
                                    is Int -> onValueChange(num.toInt())
                                    is Long -> onValueChange(num.toLong())
                                    else -> onValueChange(num)
                                }
                            }
                        },
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                is String -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                null -> {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { newText -> onValueChange(newText.ifBlank { null }) },
                        label = { Text("$key (null)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                else -> {
                    // Fallback: show as string
                    var text by remember(value) { mutableStateOf(value.toString()) }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { newText ->
                            text = newText
                            onValueChange(newText)
                        },
                        label = { Text(key) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun flattenDefaults(source: Map<String, Any?>, target: MutableMap<String, Any?>, prefix: String = "") {
    source.forEach { (key, value) ->
        val path = if (prefix.isBlank()) key else "$prefix.$key"
        @Suppress("UNCHECKED_CAST")
        if (value is Map<*, *>) flattenDefaults(value as Map<String, Any?>, target, path)
        else target[path] = value
    }
}

private fun unflattenMap(flatMap: Map<String, Any?>): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    for ((key, value) in flatMap) {
        if (value == null) continue
        val parts = key.split(".")
        var currentMap = result
        for (i in 0 until parts.lastIndex) {
            val part = parts[i]
            @Suppress("UNCHECKED_CAST")
            val nextMap = currentMap[part] as? MutableMap<String, Any?>
                ?: mutableMapOf<String, Any?>().also { currentMap[part] = it }
            currentMap = nextMap
        }
        currentMap[parts.last()] = value
    }
    return result
}