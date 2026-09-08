package com.imnotndesh.truehub.ui.homepage.update

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.GlobalJobTracker
import com.imnotndesh.truehub.data.helpers.JobRepository
import com.imnotndesh.truehub.data.models.System
import com.imnotndesh.truehub.ui.components.ToastManager
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import kotlinx.coroutines.launch

/**
 * Screen listing all available system update versions (per train) that the
 * user can update to. Mirrors the visual language used in AppsScreen /
 * RollbackVersionScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemUpdateScreen(
    manager: TrueNASApiManager,
    versions: List<System.UpdateAvailableVersionsResponse>,
    currentVersion: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activeJobs by JobRepository.activeJobs.collectAsState()
    var selectedVersionForNotes by remember { mutableStateOf<System.UpdateAvailableVersionsResponse?>(null) }

    val activeUpdateJob = remember(activeJobs) {
        activeJobs.values.find { it.type == "SYSTEM_UPDATE" && it.state !in listOf("SUCCESS", "FAILED", "ABORTED") }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        UnifiedScreenHeader(
            title = "System Updates",
            subtitle = if (versions.isEmpty()) "No updates available" else "${versions.size} available",
            isLoading = false,
            isRefreshing = false,
            error = null,
            onRefresh = {},
            onDismissError = {},
            manager = manager,
            onBackPressed = onNavigateBack
        )

        if (versions.isEmpty()) {
            EmptyUpdatesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    UpdateHeaderCard(currentVersion = currentVersion)
                }

                items(items = versions, key = { it.train + it.version.version }) { update ->
                    val matchingJob = activeUpdateJob?.takeIf { it.appName.endsWith(update.version.version) }
                    UpdateVersionCard(
                        update = update,
                        isThisJobActive = matchingJob != null,
                        activeJob = matchingJob,
                        isAnotherJobActive = activeUpdateJob != null && matchingJob == null,
                        onInfoClick = { selectedVersionForNotes = update },
                        onDownloadRestart = {
                            startSystemUpdateJob(context, manager, update, reboot = true)
                        },
                        onDownloadLater = {
                            startSystemUpdateJob(context, manager, update, reboot = false)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    selectedVersionForNotes?.let { update ->
        val matchingJob = activeUpdateJob?.takeIf { it.appName.endsWith(update.version.version) }
        ReleaseNotesSheet(
            update = update,
            isJobActive = matchingJob != null,
            activeJob = matchingJob,
            onDismiss = { selectedVersionForNotes = null },
            onDownloadRestart = {
                startSystemUpdateJob(context, manager, update, reboot = true)
                selectedVersionForNotes = null
            },
            onDownloadLater = {
                startSystemUpdateJob(context, manager, update, reboot = false)
                selectedVersionForNotes = null
            }
        )
    }
}

private fun startSystemUpdateJob(
    context: Context,
    manager: TrueNASApiManager,
    update: System.UpdateAvailableVersionsResponse,
    reboot: Boolean
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            ToastManager.showInfo("Starting update to ${update.version.version}...")
            val result = manager.system.runSystemUpdate(
                System.UpdateRunDefaults(
                    train = update.train,
                    version = update.version.version,
                    reboot = reboot
                )
            )
            when (result) {
                is ApiResult.Success -> {
                    GlobalJobTracker.startTracking(
                        context = context,
                        manager = manager,
                        type = "SYSTEM_UPDATE",
                        jobId = result.data,
                        appName = "System Update ${update.version.version}",
                        showNotif = true
                    )
                }
                is ApiResult.Error -> {
                    ToastManager.showWarning(result.message ?: "Failed to start update")
                }
                ApiResult.Loading -> {}
            }
        } catch (_: Exception) {
            ToastManager.showWarning("Failed to start update")
        }
    }
}

@Composable
private fun UpdateHeaderCard(currentVersion: String?) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Updates Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentVersion != null) {
                        Text(
                            "Currently running $currentVersion",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Review release notes before updating",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateVersionCard(
    update: System.UpdateAvailableVersionsResponse,
    isThisJobActive: Boolean,
    activeJob: com.imnotndesh.truehub.data.helpers.TrackedJob?,
    isAnotherJobActive: Boolean,
    onInfoClick: () -> Unit,
    onDownloadRestart: () -> Unit,
    onDownloadLater: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        update.version.version,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Train: ${update.train}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View release notes for ${update.version.version}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isThisJobActive && activeJob != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeJob?.let { job ->
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = job.description ?: "Updating...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${job.progress}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { job.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }

            if (!isThisJobActive) {
                Spacer(modifier = Modifier.height(16.dp))
                if (isAnotherJobActive) {
                    Button(
                        onClick = onDownloadLater,
                        enabled = false,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Another update is running")
                    }
                } else {
                    // Restart now (primary)
                    Button(
                        onClick = onDownloadRestart,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download & Restart Now", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restart later (secondary)
                    OutlinedButton(
                        onClick = onDownloadLater,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download & Restart Later", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyUpdatesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "System is up to date",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "No new versions available right now",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReleaseNotesSheet(
    update: System.UpdateAvailableVersionsResponse,
    isJobActive: Boolean,
    activeJob: com.imnotndesh.truehub.data.helpers.TrackedJob?,
    onDismiss: () -> Unit,
    onDownloadRestart: () -> Unit,
    onDownloadLater: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Release Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${update.version.version} • ${update.train}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val notes = update.version.release_notes
                if (!notes.isNullOrBlank()) {
                    MarkdownContent(markdown = notes)
                } else {
                    Text(
                        text = "No release notes were provided for this version.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (update.version.release_notes_url.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { uriHandler.openUri(update.version.release_notes_url) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Full Release Notes", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isJobActive && activeJob != null) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeJob.description ?: "Updating...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeJob.progress}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { activeJob.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            } else {
                // Restart now (primary)
                Button(
                    onClick = onDownloadRestart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Restart Now", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Restart later (secondary)
                OutlinedButton(
                    onClick = onDownloadLater,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download & Restart Later", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Lightweight markdown renderer (headings, lists, bold, links)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MarkdownContent(markdown: String) {
    val uriHandler = LocalUriHandler.current

    markdown
        .replace("\r\n", "\n")
        .split("\n")
        .forEach { rawLine ->
            val trimmed = rawLine.trim()

            when {
                // Headings: # , ## , ###
                trimmed.startsWith("### ") -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = inlineStyled(trimmed.removePrefix("### "), uriHandler),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                }
                trimmed.startsWith("## ") -> {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = inlineStyled(trimmed.removePrefix("## "), uriHandler),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                }
                trimmed.startsWith("# ") -> {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = inlineStyled(trimmed.removePrefix("# "), uriHandler),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                }

                // Unordered list item: - * + 
                trimmed.startsWith("- ")
                        || trimmed.startsWith("* ")
                        || trimmed.startsWith("+ ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp)
                        )
                        Text(
                            text = inlineStyled(trimmed.substringAfter(" "), uriHandler),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ordered list item: 1. 2. etc.
                trimmed.matches(Regex("^\\d+\\.\\s")) -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = trimmed.substringBefore(".") + ".",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp)
                        )
                        Text(
                            text = inlineStyled(trimmed.substringAfter(". "), uriHandler),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Horizontal rule
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                // Blank line → vertical rhythm
                trimmed.isEmpty() -> {
                    Spacer(Modifier.height(4.dp))
                }

                // Plain paragraph
                else -> {
                    Text(
                        text = inlineStyled(trimmed, uriHandler),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
}

/** Builds an [AnnotatedString] handling `**bold**` and `[text](url)` inline markdown. */
@Composable
private fun inlineStyled(text: String, uriHandler: androidx.compose.ui.platform.UriHandler): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val n = text.length
        while (i < n) {
            val nextBold = text.indexOf("**", i)
            val nextLink = text.indexOf('[', i)
            val next = when {
                nextBold != -1 && nextLink != -1 -> minOf(nextBold, nextLink)
                nextBold != -1 -> nextBold
                nextLink != -1 -> nextLink
                else -> -1
            }

            if (next == -1) {
                append(text.substring(i, n))
                i = n
                continue
            }

            append(text.substring(i, next))

            if (text.startsWith("**", next)) {
                val endBold = text.indexOf("**", next + 2)
                if (endBold != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(next + 2, endBold))
                    }
                    i = endBold + 2
                } else {
                    append(text.substring(next))
                    i = next + 2
                }
            } else if (text.startsWith("[", next)) {
                val closeBracket = text.indexOf(']', next + 1)
                val urlStart = if (closeBracket != -1 && closeBracket + 1 < n && text[closeBracket + 1] == '(') {
                    closeBracket + 2
                } else -1
                val urlEnd = if (urlStart != -1) text.indexOf(')', urlStart) else -1

                if (urlStart != -1 && urlEnd != -1) {
                    val label = text.substring(next + 1, closeBracket)
                    val url = text.substring(urlStart, urlEnd)
                    val linkId = pushLink(
                        LinkAnnotation.Url(
                            url = url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        )
                    )
                    append(label)
                    pop(linkId)
                    i = urlEnd + 1
                } else {
                    append(text.substring(next))
                    i = next + 1
                }
            } else {
                append(text.substring(next))
                i = next + 1
            }
        }
        uriHandler
    }
}