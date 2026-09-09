package com.imnotndesh.truehub.ui.services.apps.details.marketplace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.haptics.VibrationFeedback
import com.imnotndesh.truehub.ui.haptics.VibratorMode
import com.imnotndesh.truehub.ui.services.apps.AppsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceAppInstallScreen(
    appName: String,
    train: String,
    viewModel: AppsScreenViewModel,
    onBack: () -> Unit,
    onInstallSuccess: () -> Unit,
    manager: TrueNASApiManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isInstalling by uiState.isInstalling.collectAsStateWithLifecycle()
    val installError by uiState.installError.collectAsStateWithLifecycle()
    val installJobId by uiState.installJobId.collectAsStateWithLifecycle()

    val activeJobsMap by JobRepository.activeJobs.collectAsStateWithLifecycle()
    val trackedJob = installJobId?.let { gid -> activeJobsMap[gid] }

    val haptics = remember(context) {
        VibrationFeedback(context)
    }

    // Fire-once per surfaced terminal event: clearInstallState() nulls installJobId so the
    // effect below re-keys and re-enters; without a job-keyed guard it would re-fire.
    val terminalHapticsFired = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(trackedJob) {
        val job = trackedJob ?: return@LaunchedEffect
        when (job.state.uppercase()) {
            "SUCCESS" -> {
                // Fire BEFORE clearInstallState()/onInstallSuccess(): the latter pops the nav
                // stack, so the haptic must start while still foreground (trigger-then-navigate).
                if (terminalHapticsFired.put(job.jobId, "SUCCESS") == null) {
                    haptics.play(VibratorMode.SUCCESS_TICK)
                    viewModel.clearInstallState()
                    onInstallSuccess()
                }
            }
            "FAILED", "ABORTED" -> {
                if (terminalHapticsFired.put(job.jobId, "ERROR") == null) {
                    haptics.play(VibratorMode.ERROR_ALERT)
                }
            }
        }
    }

    // Fire ERROR_ALERT exactly when the "Installation Failed" overlay first appears (null ->
    // non-null transition), never on recomposition; re-arms naturally after a real retry.
    var previousInstallError by remember(trackedJob) { mutableStateOf(installError) }
    LaunchedEffect(installError, installJobId) {
        val errorNow = installError
        val firstAppearance = errorNow != null && previousInstallError == null && installJobId == null
        previousInstallError = errorNow
        if (firstAppearance) {
            haptics.play(VibratorMode.ERROR_ALERT)
        }
    }

    LaunchedEffect(appName) {
        viewModel.loadCatalogAppDetails(appName, train)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCatalogAppDetails() }
    }

    val isLoadingDetails = uiState.isLoadingCatalogDetails && uiState.catalogAppDetails == null

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                isLoadingDetails -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Loading configurations…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.catalogDetailsError != null && uiState.catalogAppDetails == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Failed to load schema", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { viewModel.loadCatalogAppDetails(appName, train) }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.catalogAppDetails != null -> {
                    val details = uiState.catalogAppDetails!!
                    var selectedVersionKey by remember(details) {
                        mutableStateOf(details.versions?.keys?.firstOrNull() ?: "")
                    }
                    val versionData = details.versions?.get(selectedVersionKey)

                    InstallOptionsContent(
                        details = details,
                        versionData = versionData,
                        versionsMap = details.versions ?: emptyMap(),
                        selectedVersionKey = selectedVersionKey,
                        onVersionSelected = { selectedVersionKey = it },
                        onBack = onBack,
                        onInstallAction = { targetedInstanceName, flatMapValues ->
                            viewModel.installMarketplaceApp(
                                context = context,
                                appName = targetedInstanceName,
                                catalogApp = details.name,
                                train = train,
                                version = selectedVersionKey,
                                flatValues = flatMapValues,
                                unflattenMapFunc = ::unflattenMap
                            )
                        }
                    )
                }
            }

            if (isInstalling || installError != null) {
                InstallProgressView(
                    appName = appName,
                    progress = trackedJob?.progress ?: 0,
                    description = trackedJob?.description,
                    isError = installError != null,
                    errorMessage = installError,
                    onDismissError = { viewModel.clearInstallState() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun InstallOptionsContent(
    details: Apps.CatalogAppDetails,
    versionData: Apps.CatalogAppVersion?,
    versionsMap: Map<String, Apps.CatalogAppVersion>,
    selectedVersionKey: String,
    onVersionSelected: (String) -> Unit,
    onBack: () -> Unit,
    onInstallAction: (appName: String, values: Map<String, Any?>) -> Unit
) {
    val schema = versionData?.schema
    val defaults = versionData?.values ?: emptyMap()

    val formState = remember(versionData) {
        mutableStateMapOf<String, Any?>().also { map ->
            flattenDefaults(defaults, map)
        }
    }

    var targetedAppNameInstance by remember(details) { mutableStateOf(details.name) }

    // Precompute questions-by-group ONCE per schema instead of filtering per group per recomposition
    val questionsByGroup = remember(schema) {
        schema?.questions?.groupBy { it.group } ?: emptyMap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            UnifiedScreenHeader(
                title = details.title ?: details.name,
                onBackPressed = {
                    onBack()
                },
                subtitle = "Deployment Wizard",
                isLoading = false,
                isRefreshing = false,
                onDismissError = {},
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Application Instance Name",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = targetedAppNameInstance,
                    onValueChange = { targetedAppNameInstance = it.lowercase() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null, modifier = Modifier.size(18.dp)) },
                    placeholder = { Text("e.g. wireguard") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val categories = details.categories
                if (!categories.isNullOrEmpty()) {
                    InstallFlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(cat.replaceFirstChar { it.uppercase() }) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (versionsMap.isNotEmpty()) {
                    Text(
                        text = "App Target Version",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    var versionDropdownExpanded by remember { mutableStateOf(false) }

                    @OptIn(ExperimentalMaterial3Api::class)
                    ExposedDropdownMenuBox(
                        expanded = versionDropdownExpanded,
                        onExpandedChange = { versionDropdownExpanded = !versionDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedVersionKey,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = versionDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            supportingText = versionData?.humanVersion?.let {
                                { Text("Build Config Version: $it") }
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = versionDropdownExpanded,
                            onDismissRequest = { versionDropdownExpanded = false }
                        ) {
                            versionsMap.keys.forEach { versionKey ->
                                val currentData = versionsMap[versionKey]
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(versionKey, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            currentData?.humanVersion?.let {
                                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    onClick = {
                                        onVersionSelected(versionKey)
                                        versionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (schema?.groups != null && schema.questions != null) {
            item {
                Text(
                    text = "Configuration Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            schema.groups.forEach { group ->
                val groupQuestions = questionsByGroup[group.name] ?: emptyList()
                if (groupQuestions.isNotEmpty()) {
                    item(key = group.name) {
                        SchemaGroupSection(
                            group = group,
                            questions = groupQuestions,
                            formState = formState,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            InstallBottomAction(
                title = details.title ?: details.name,
                onInstall = { onInstallAction(targetedAppNameInstance, formState.toMap()) }
            )
        }
    }
}

@Composable
private fun InstallHeroHeader(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Deployment Wizard",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Flattened representation of a (possibly nested) question tree.
 * Dict children are flattened into this same list instead of being
 * rendered as nested Column/background wrappers, so we never get
 * subsections-within-subsections. `depth` is capped at 1 so visual
 * indent stays flat regardless of actual schema nesting depth.
 */
private data class FlatQuestion(
    val question: Apps.SchemaQuestion,
    val path: String,
    val depth: Int,
    val isDictHeader: Boolean
)

private fun flattenQuestions(
    questions: List<Apps.SchemaQuestion>,
    pathPrefix: String = "",
    depth: Int = 0,
    out: MutableList<FlatQuestion> = mutableListOf()
): List<FlatQuestion> {
    questions.forEach { q ->
        val schema = q.schema ?: return@forEach
        if (schema.hidden == true) return@forEach
        val path = if (pathPrefix.isBlank()) q.variable else "$pathPrefix.${q.variable}"

        if (schema.type == "dict" && !schema.attrs.isNullOrEmpty()) {
            out.add(FlatQuestion(q, path, depth, isDictHeader = true))
            flattenQuestions(schema.attrs, path, (depth + 1).coerceAtMost(1), out)
        } else {
            out.add(FlatQuestion(q, path, depth, isDictHeader = false))
        }
    }
    return out
}

@Composable
internal fun SchemaGroupSection(
    group: Apps.SchemaGroup,
    questions: List<Apps.SchemaQuestion>,
    formState: MutableMap<String, Any?>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    val flatQuestions = remember(questions) { flattenQuestions(questions) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                group.description?.let {
                    val stripped = it.replace(Regex("<[^>]+>"), " ").trim()
                    if (stripped.isNotBlank()) {
                        Text(stripped, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle"
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                flatQuestions.forEachIndexed { idx, fq ->
                    val indent = (fq.depth * 12).dp

                    if (fq.isDictHeader) {
                        Text(
                            text = fq.question.label?.ifBlank { fq.question.variable } ?: fq.question.variable,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = indent, top = if (idx == 0) 0.dp else 8.dp, bottom = 2.dp)
                        )
                    } else {
                        Box(modifier = Modifier.padding(start = indent)) {
                            LeafQuestionField(
                                question = fq.question,
                                formState = formState,
                                path = fq.path
                            )
                        }
                    }

                    if (idx < flatQuestions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp, start = indent, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders a single leaf question (never recurses into dict). Dict recursion
 * is handled structurally by flattenQuestions() before this is ever called.
 */
@Composable
private fun LeafQuestionField(
    question: Apps.SchemaQuestion,
    formState: MutableMap<String, Any?>,
    path: String
) {
    val schema = question.schema ?: return

    val label = question.label?.ifBlank { question.variable } ?: question.variable
    val isRequired = schema.required == true
    val isNullable = schema.`null` == true

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            if (isRequired) {
                Text(" *", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            }
            if (isNullable) {
                Spacer(Modifier.width(4.dp))
                Text(
                    "optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        question.description?.let {
            val stripped = it.replace(Regex("<[^>]+>"), " ").replace("  ", " ").trim()
            if (stripped.isNotBlank()) {
                Text(stripped, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when (schema.type) {
            "string"   -> StringField(schema = schema, path = path, formState = formState)
            "int"      -> IntField(schema = schema, path = path, formState = formState)
            "boolean"  -> BooleanField(schema = schema, path = path, formState = formState)
            "list"     -> ListFieldInfo()
            "path",
            "hostpath" -> PathField(path = path, formState = formState)
            else       -> DefaultField(type = schema.type, path = path, formState = formState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    val isPrivate = schema.private == true

    if (!schema.enum.isNullOrEmpty()) {
        EnumPickerField(schema = schema, path = path, formState = formState)
    } else {
        var text by remember(path) { mutableStateOf((formState[path] ?: schema.default)?.toString() ?: "") }
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it; formState[path] = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter token parameter...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPrivate && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPrivate) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                }
            } else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumPickerField(
    schema: Apps.SchemaDefinition,
    path: String,
    formState: MutableMap<String, Any?>
) {
    var showSheet by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val currentVal = (formState[path] ?: schema.default)?.toString()
        ?: schema.enum?.firstOrNull()?.value?.toString() ?: ""
    val displayText = schema.enum?.find { it.value?.toString() == currentVal }?.description ?: currentVal

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showSheet = true },
        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
        shape = RoundedCornerShape(12.dp),
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    if (showSheet) {
        // derivedStateOf avoids recomputing the filtered list unless
        // `search` actually changes its resulting output, and avoids
        // recomposing the whole sheet on unrelated state churn.
        val filtered by remember {
            derivedStateOf {
                schema.enum?.filter {
                    search.isBlank() ||
                            it.description?.contains(search, ignoreCase = true) == true ||
                            it.value?.toString()?.contains(search, ignoreCase = true) == true
                } ?: emptyList()
            }
        }

        ModalBottomSheet(onDismissRequest = { showSheet = false; search = "" }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Select Option",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(filtered, key = { _, option -> option.value?.toString() ?: option.hashCode() }) { _, option ->
                        val optionValue = option.value?.toString() ?: ""
                        val optionLabel = option.description ?: optionValue
                        val isSelected = optionValue == currentVal
                        ListItem(
                            headlineContent = { Text(optionLabel) },
                            supportingContent = if (optionLabel != optionValue) {
                                { Text(optionValue, style = MaterialTheme.typography.labelSmall) }
                            } else null,
                            trailingContent = if (isSelected) {
                                { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                            } else null,
                            modifier = Modifier.clickable {
                                formState[path] = optionValue
                                showSheet = false
                                search = ""
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun IntField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    var text by remember(path) { mutableStateOf((formState[path] ?: schema.default)?.toString()?.takeIf { it != "null" } ?: "") }
    val supportText = when {
        schema.min != null && schema.max != null -> "Bounds: ${schema.min} – ${schema.max}"
        schema.min != null -> "Minimum required: ${schema.min}"
        schema.max != null -> "Maximum allocation: ${schema.max}"
        else -> null
    }
    OutlinedTextField(
        value = text,
        onValueChange = { v -> text = v; formState[path] = v.toIntOrNull() },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("Enter dynamic port or capacity integer...") },
        supportingText = supportText?.let { msg -> { Text(msg, style = MaterialTheme.typography.labelSmall) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun BooleanField(schema: Apps.SchemaDefinition, path: String, formState: MutableMap<String, Any?>) {
    var checked by remember(path) { mutableStateOf((formState[path] ?: schema.default) as? Boolean ?: false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Switch(checked = checked, onCheckedChange = { checked = it; formState[path] = it })
        Text(if (checked) "Active" else "Inactive", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PathField(path: String, formState: MutableMap<String, Any?>) {
    var text by remember(path) { mutableStateOf(formState[path]?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; formState[path] = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("/mnt/dataset/appdata") },
        leadingIcon = { Icon(Icons.Default.Folder, null, modifier = Modifier.size(18.dp)) }
    )
}

@Composable
private fun ListFieldInfo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Column {
            Text("Collection Parameters Configuration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Appends natively via backend blocks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun DefaultField(type: String?, path: String, formState: MutableMap<String, Any?>) {
    var text by remember(path) { mutableStateOf(formState[path]?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; formState[path] = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("($type) raw syntax entry...") }
    )
}

@Composable
private fun InstallBottomAction(title: String, onInstall: () -> Unit) {
    Button(
        onClick = onInstall,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(52.dp)
    ) {
        Icon(Icons.Default.Download, null)
        Spacer(Modifier.width(8.dp))
        Text("Deploy instance: $title", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

private fun flattenDefaults(source: Map<String, Any?>, target: MutableMap<String, Any?>, prefix: String = "") {
    source.forEach { (key, value) ->
        val path = if (prefix.isBlank()) key else "$prefix.$key"
        @Suppress("UNCHECKED_CAST")
        if (value is Map<*, *>) {
            flattenDefaults(value as Map<String, Any?>, target, path)
        } else {
            target[path] = value
        }
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
        currentMap[parts[parts.lastIndex]] = value
    }
    return result
}

@Composable
private fun InstallFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val rowItems = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rows = mutableListOf<List<Placeable>>()
        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0

        rowItems.forEach { item ->
            if (currentWidth + item.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(item)
            currentWidth += item.width + horizontalArrangement.spacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val rowHeights = rows.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val totalHeight = rowHeights.sum() + verticalArrangement.spacing.roundToPx() * (rows.size - 1).coerceAtLeast(0)

        layout(constraints.maxWidth, totalHeight.coerceAtMost(constraints.maxHeight)) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { item ->
                    item.placeRelative(x, y)
                    x += item.width + horizontalArrangement.spacing.roundToPx()
                }
                y += rowHeights[index] + verticalArrangement.spacing.roundToPx()
            }
        }
    }
}

@Composable
private fun InstallProgressView(
    appName: String,
    progress: Int,
    description: String?,
    isError: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "installProgress"
    )

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            PixelShapesOrbit(
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                isDone = isError,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedContent(
                targetState = if (isError) "error" else "running",
                transitionSpec = {
                    (scaleIn(tween(400)) + fadeIn(tween(400))) togetherWith
                            (scaleOut(tween(250)) + fadeOut(tween(250)))
                },
                label = "install_center_icon"
            ) { state ->
                when (state) {
                    "error" -> Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    else -> CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = if (isError) "error" else "running",
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(200))
            },
            label = "install_status_text"
        ) { state ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (state == "error") "Installation Failed" else "Deploying $appName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (state == "error")
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state == "error")
                        errorMessage ?: "An unknown error occurred."
                    else
                        description ?: "Provisioning application resources…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (state != "error") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        if (isError) {
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onDismissError,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Dismiss", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PixelShapesOrbit(
    primaryColor: Color,
    secondaryColor: Color,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    val speedScale by animateFloatAsState(
        targetValue = if (isDone) 0f else 1f,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "speed_scale"
    )

    val ring1AngleState = remember { mutableFloatStateOf(0f) }
    val ring2AngleState = remember { mutableFloatStateOf(0f) }
    val ring3AngleState = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isDone) {
        if (isDone) return@LaunchedEffect
        var lastTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val dt = (now - lastTime) / 1000f
            lastTime = now
            val s = speedScale
            ring1AngleState.floatValue += dt * 45f * s
            ring2AngleState.floatValue -= dt * 28f * s
            ring3AngleState.floatValue += dt * 18f * s
        }
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val unit = size.minDimension / 2f

        drawOrbitRing(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.42f,
            baseAngleDeg = ring1AngleState.floatValue,
            shapeCount = 3,
            shapeSize = unit * 0.18f,
            cornerFraction = 0.35f,
            color = primaryColor.copy(alpha = 0.85f)
        )
        drawOrbitRingPills(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.68f,
            baseAngleDeg = ring2AngleState.floatValue,
            shapeCount = 4,
            pillWidth = unit * 0.22f,
            pillHeight = unit * 0.09f,
            color = secondaryColor.copy(alpha = 0.55f)
        )
        drawOrbitRing(
            cx = cx, cy = cy,
            orbitRadius = unit * 0.88f,
            baseAngleDeg = ring3AngleState.floatValue,
            shapeCount = 5,
            shapeSize = unit * 0.11f,
            cornerFraction = 0.4f,
            color = primaryColor.copy(alpha = 0.35f)
        )
    }
}

private fun DrawScope.drawOrbitRing(
    cx: Float, cy: Float,
    orbitRadius: Float,
    baseAngleDeg: Float,
    shapeCount: Int,
    shapeSize: Float,
    cornerFraction: Float,
    color: Color
) {
    val stepDeg = 360f / shapeCount
    repeat(shapeCount) { i ->
        val angleDeg = baseAngleDeg + stepDeg * i
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val x = cx + orbitRadius * cos(angleRad).toFloat()
        val y = cy + orbitRadius * sin(angleRad).toFloat()
        withTransform({
            translate(x - shapeSize / 2f, y - shapeSize / 2f)
            rotate(angleDeg, pivot = Offset(shapeSize / 2f, shapeSize / 2f))
        }) {
            drawRoundRect(
                color = color,
                size = Size(shapeSize, shapeSize),
                cornerRadius = CornerRadius(shapeSize * cornerFraction)
            )
        }
    }
}

private fun DrawScope.drawOrbitRingPills(
    cx: Float, cy: Float,
    orbitRadius: Float,
    baseAngleDeg: Float,
    shapeCount: Int,
    pillWidth: Float,
    pillHeight: Float,
    color: Color
) {
    val stepDeg = 360f / shapeCount
    repeat(shapeCount) { i ->
        val angleDeg = baseAngleDeg + stepDeg * i
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val x = cx + orbitRadius * cos(angleRad).toFloat()
        val y = cy + orbitRadius * sin(angleRad).toFloat()
        withTransform({
            translate(x - pillWidth / 2f, y - pillHeight / 2f)
            rotate(angleDeg + 90f, pivot = Offset(pillWidth / 2f, pillHeight / 2f))
        }) {
            drawRoundRect(
                color = color,
                size = Size(pillWidth, pillHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillHeight / 2f)
            )
        }
    }
}