package com.imnotndesh.truehub.ui.services.containers.devices

import android.provider.OpenableColumns
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.imnotndesh.truehub.ui.components.ExpressiveActionFab
import com.imnotndesh.truehub.ui.components.LoadingScreen
import com.imnotndesh.truehub.ui.components.PullToRefreshContent
import com.imnotndesh.truehub.ui.components.UnifiedScreenHeader
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveEntryCard
import com.imnotndesh.truehub.ui.components.expressive.ExpressiveTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDevicesScreen(
    containerId: String,
    manager: TrueNASApiManager,
    onNavigateBack: () -> Unit,
    onAddDevice: () -> Unit,
    onEditDevice: (String) -> Unit,
    viewModel: ContainerDevicesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showUploadSheet by remember { mutableStateOf(false) }
    var uploadInFlight by remember { mutableStateOf(false) }

    LaunchedEffect(state.isUploading) {
        if (state.isUploading) {
            uploadInFlight = true
        } else if (uploadInFlight) {
            uploadInFlight = false
            showUploadSheet = false
        }
    }

    Scaffold(
        topBar = {
            UnifiedScreenHeader(
                title = "Container devices",
                subtitle = "Attached hardware and devices",
                isLoading = state.isLoading,
                isRefreshing = state.isRefreshing,
                error = state.error,
                onDismissError = viewModel::load,
                manager = manager,
                onBackPressed = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshContent(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    state.isLoading && state.devices.isEmpty() -> LoadingScreen("Loading devices")
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item(key = "import") {
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Download,
                                    title = "Import disk image",
                                    subtitle = "Convert a disk image file into a ZFS volume",
                                    onClick = { showImportSheet = true }
                                )
                            }
                            item(key = "upload") {
                                ExpressiveEntryCard(
                                    icon = Icons.Default.Upload,
                                    title = "Upload ISO from device",
                                    subtitle = "Send an ISO from this device to the server",
                                    onClick = { showUploadSheet = true }
                                )
                            }
                            if (state.devices.isEmpty()) {
                                item(key = "empty") {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Devices,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "No devices attached",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(top = 12.dp)
                                        )
                                        Text(
                                            "Add a disk, NIC, GPU, or passthrough device.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            } else {
                                items(state.devices, key = { deviceIdentifier(it) ?: it.hashCode().toString() }) { device ->
                                    val identifier = deviceIdentifier(device)
                                    DeviceRow(
                                        device = device,
                                        isDeleting = state.deletingDeviceName == device["name"]?.toString(),
                                        onEdit = { identifier?.let(onEditDevice) },
                                        canEdit = identifier != null,
                                        onDelete = { pendingDelete = device["name"]?.toString() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ExpressiveActionFab(
                icon = Icons.Default.Add,
                label = "Add device",
                onClick = onAddDevice,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }
    }

    pendingDelete?.let { deviceName ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove device?") },
            text = { Text("This removes $deviceName from the container configuration.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(deviceName)
                        pendingDelete = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showImportSheet) {
        ImportImageSheet(
            onDismiss = { showImportSheet = false },
            onImport = { diskimg, zvol ->
                showImportSheet = false
                viewModel.importDiskImage(diskimg, zvol)
            }
        )
    }

    if (showUploadSheet) {
        UploadIsoSheet(
            manager = manager,
            isUploading = state.isUploading,
            uploadBytes = state.uploadBytes,
            uploadTotal = state.uploadTotal,
            onDismiss = { if (!state.isUploading) showUploadSheet = false },
            onUpload = { dir, fileName, length, openStream, cleanup ->
                viewModel.uploadIso(dir, fileName, length, openStream, cleanup)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportImageSheet(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var diskimg by rememberSaveable { mutableStateOf("") }
    var zvol by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Import disk image",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = diskimg,
                onValueChange = { diskimg = it },
                label = "Disk image path",
                supportingText = { Text("Path to the image on the server (QCOW2, RAW, VDI, VPC, VMDK)") }
            )
            ExpressiveTextField(
                value = zvol,
                onValueChange = { zvol = it },
                label = "Target ZFS volume",
                supportingText = { Text("ZFS volume (zvol) the image will be imported into") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = { onImport(diskimg, zvol) },
                    enabled = diskimg.isNotBlank() && zvol.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Import") }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Map<String, Any?>,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    canEdit: Boolean,
    onDelete: () -> Unit
) {
    val type = device["dev_type"]?.toString() ?: "DEVICE"
    val source = device["source"]?.toString()
    ExpressiveEntryCard(
        icon = Icons.Default.Devices,
        title = device["name"]?.toString()?.ifBlank { "Unnamed device" } ?: "Unnamed device",
        subtitle = listOfNotNull(type, source).joinToString(" · "),
        onClick = onEdit,
        enabled = canEdit && !isDeleting,
        trailing = {
            IconButton(onClick = onDelete, enabled = !isDeleting) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove device",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadIsoSheet(
    manager: TrueNASApiManager,
    isUploading: Boolean,
    uploadBytes: Long,
    uploadTotal: Long,
    onDismiss: () -> Unit,
    onUpload: (String, String, Long, () -> java.io.InputStream, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var targetDir by rememberSaveable { mutableStateOf("") }
    var fileName by rememberSaveable { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var fileSize by remember { mutableStateOf(-1L) }
    var isPreparing by remember { mutableStateOf(false) }
    var showDirPicker by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0 && fileName.isBlank()) {
                        fileName = cursor.getString(nameIdx).orEmpty()
                    }
                    if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isUploading) onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Upload ISO from device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            ExpressiveTextField(
                value = targetDir,
                onValueChange = { targetDir = it },
                label = "Target directory",
                enabled = !isUploading,
                supportingText = { Text("Server directory to write the ISO into") },
                trailingIcon = {
                    IconButton(onClick = { showDirPicker = true }, enabled = !isUploading) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Choose directory")
                    }
                }
            )
            ExpressiveTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = "File name",
                enabled = !isUploading
            )
            OutlinedButton(
                onClick = { launcher.launch(arrayOf("application/octet-stream", "application/x-iso9660-image", "*/*")) },
                enabled = !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedUri == null) "Choose ISO file" else "Change file")
            }
            if (selectedUri != null) {
                Text(
                    text = "Selected: ${fileName.ifBlank { "file" }}${
                        if (fileSize > 0) " (${Formatter.formatFileSize(context, fileSize)})" else ""
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isUploading) {
                val fraction = if (uploadTotal > 0) {
                    (uploadBytes.toFloat() / uploadTotal).coerceIn(0f, 1f)
                } else {
                    0f
                }
                LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                Text(
                    text = if (uploadTotal > 0) {
                        "${Formatter.formatFileSize(context, uploadBytes)} / ${Formatter.formatFileSize(context, uploadTotal)}"
                    } else {
                        Formatter.formatFileSize(context, uploadBytes)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isUploading,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        val uri = selectedUri
                        if (uri != null) {
                            if (fileSize > 0) {
                                onUpload(targetDir, fileName, fileSize, {
                                    context.contentResolver.openInputStream(uri)
                                        ?: throw IllegalStateException("Cannot open selected file")
                                }, {})
                            } else {
                                isPreparing = true
                                scope.launch {
                                    val prepared = withContext(Dispatchers.IO) {
                                        runCatching {
                                            val temp = File(
                                                context.cacheDir,
                                                "truehub_upload_${System.currentTimeMillis()}"
                                            )
                                            context.contentResolver.openInputStream(uri)!!.use { input ->
                                                temp.outputStream().use { output -> input.copyTo(output) }
                                            }
                                            temp
                                        }.getOrNull()
                                    }
                                    isPreparing = false
                                    if (prepared != null) {
                                        onUpload(
                                            targetDir,
                                            fileName,
                                            prepared.length(),
                                            { FileInputStream(prepared) },
                                            { prepared.delete() }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUploading && !isPreparing && targetDir.isNotBlank() &&
                        fileName.isNotBlank() && selectedUri != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) { Text(if (isPreparing) "Preparing…" else "Upload") }
            }
        }
    }

    if (showDirPicker) {
        com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerSheet(
            manager = manager,
            config = com.imnotndesh.truehub.ui.homepage.dataset.ServerPathPickerConfig(
                title = "Choose target directory",
                confirmLabel = "Use directory",
                startPath = "/mnt",
                showFiles = false
            ),
            onConfirm = {
                targetDir = it
                showDirPicker = false
            },
            onDismiss = { showDirPicker = false }
        )
    }
}
