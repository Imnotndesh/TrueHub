package com.imnotndesh.truehub.ui.services.apps.details.upgrade

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.components.MinimalBackHeader
import com.imnotndesh.truehub.ui.components.ServerRackAnimation
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpgradeSummaryScreen(
    appName: String,
    currentHumanVersion: String? = null,
    currentVersion: String,
    summary: Apps.AppUpgradeSummaryResult,
    manager: TrueNASApiManager,
    onConfirmUpgrade: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    canUpgrade: Boolean = true,
    appState: String = ""
) {

    val activeJobs by JobRepository.activeJobs.collectAsState()
    val liveJob = activeJobs.values.find { it.appName == appName }

    val isUpgrading = liveJob != null && liveJob.state !in listOf("SUCCESS", "FAILED", "ABORTED")
    val isDone = liveJob?.state == "SUCCESS"
    val isFailed = liveJob?.state in listOf("FAILED", "ABORTED")

    LaunchedEffect(isDone) {
        if (isDone) {
            kotlinx.coroutines.delay(1800.milliseconds)
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = isUpgrading || isDone || isFailed,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 4 })
            },
            label = "upgrade_screen_state",
            modifier = Modifier.fillMaxSize()
        ) { showingProgress ->
            if (showingProgress) {
                UpgradingView(
                    appName = appName,
                    progress = liveJob?.progress ?: 0,
                    description = liveJob?.description,
                    isDone = isDone,
                    isFailed = isFailed,
                    jobId = liveJob?.jobId,
                    manager = manager,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ReviewView(
                    modifier = Modifier.fillMaxSize(),
                    appName = appName,
                    summary = summary,
                    currentVersion = currentVersion,
                    currentHumanVersion = currentHumanVersion,
                    canUpgrade = canUpgrade,
                    appState = appState,
                    onConfirmUpgrade = {selectedVersion, backup -> onConfirmUpgrade(selectedVersion, backup)},
                    onNavigateBack = onNavigateBack,
                )
            }
        }

        MinimalBackHeader(
            onBackPressed = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpgradingView(
    appName: String,
    progress: Int,
    description: String?,
    isDone: Boolean,
    isFailed: Boolean,
    jobId: Int?,
    manager: TrueNASApiManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isCancelling by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat() / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "smoothProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        ServerRackAnimation(
            size = 400f,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            AnimatedContent(
                targetState = when {
                    isDone -> "done"
                    isFailed -> "failed"
                    else -> "running"
                },
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "status_text"
            ) { state ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (state) {
                            "done" -> "Upgrade Complete"
                            "failed" -> "Upgrade Failed"
                            else -> "Upgrading $appName"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = when (state) {
                            "failed" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (state) {
                            "done" -> "All done! Taking you back..."
                            "failed" -> "Something went wrong during the upgrade"
                            else -> description ?: "Please wait, this may take a moment"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = !isDone && !isFailed,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(300))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = !isDone && !isFailed,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(40.dp))
                    OutlinedButton(
                        onClick = {
                            val id = jobId ?: return@OutlinedButton
                            isCancelling = true
                            scope.launch {
                                manager.system.cancelJob(id)
                            }
                        },
                        enabled = !isCancelling,
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isCancelling) "Cancelling..." else "Cancel Upgrade")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ReviewView(
    modifier: Modifier = Modifier,
    appName: String,
    summary: Apps.AppUpgradeSummaryResult,
    onConfirmUpgrade: (String, Boolean) -> Unit,
    currentVersion: String,
    currentHumanVersion: String? = null,
    onNavigateBack: () -> Unit,
    canUpgrade: Boolean = true,
    appState: String = "",
) {
    val availableVersions = summary.available_versions_for_upgrade
    val showVersionPicker = availableVersions.size > 1

    val defaultVersion = summary.upgrade_version
    var selectedVersion by remember { mutableStateOf(defaultVersion) }
    val displayCurrentVersion = currentHumanVersion ?: currentVersion

    var takeBackup by remember { mutableStateOf(true) }


    val selectedHumanVersion = remember(selectedVersion) {
        availableVersions.find { it.version == selectedVersion }?.version
            ?: summary.latest_version
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "Upgrade $appName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Review Changes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Current",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = displayCurrentVersion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = selectedHumanVersion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (showVersionPicker) {
                        Spacer(modifier = Modifier.height(16.dp))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = selectedHumanVersion,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableVersions.forEach { versionItem ->
                                    DropdownMenuItem(
                                        text = { Text(versionItem.human_version) },
                                        onClick = {
                                            selectedVersion = versionItem.version
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Backup checkbox
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = takeBackup,
                        onCheckedChange = { takeBackup = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Take snapshot before upgrade",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Creates a ZFS snapshot that can be used for rollback",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Changelog section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Changelog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (!summary.changelog.isNullOrBlank()) {
                        val cachedMarkdown = remember(summary.changelog) { summary.changelog }
                        MarkdownText(
                            markdown = cachedMarkdown,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "No changelog available for this update.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!canUpgrade) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (appState.isBlank()) {
                            "This app must be running before it can be updated."
                        } else {
                            "${appState.uppercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} apps cannot be updated. Start the app first, then you can upgrade it."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel")
            }
            Button(
                onClick = { if (canUpgrade) onConfirmUpgrade(selectedVersion, takeBackup) },
                enabled = canUpgrade,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade")
            }
        }
    }
}