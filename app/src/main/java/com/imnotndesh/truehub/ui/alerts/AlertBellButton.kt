package com.imnotndesh.truehub.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnotndesh.truehub.data.api.TrueNASApiManager

/**
 * Use with screens as notification icon
 */
@Composable
fun AlertsBellButton(
    manager: TrueNASApiManager,
    modifier: Modifier = Modifier
) {
    val alertsViewModel: AlertsViewModel = viewModel(
        factory = AlertsViewModel.AlertsViewModelFactory(manager)
    )
    val uiState by alertsViewModel.uiState.collectAsState()
    var showAlertsSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showAlertsSheet = true }
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alerts",
                    tint = MaterialTheme.colorScheme.primary
                )

                // Badge for unread count
                if (uiState.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.alerts.any {
                                        !it.dismissed && (it.level.equals("CRITICAL", ignoreCase = true) ||
                                                it.level.equals("ALERT", ignoreCase = true))
                                    }) Color(0xFFD32F2F)
                                else MaterialTheme.colorScheme.error
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.unreadCount > 99) "99+" else uiState.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAlertsSheet) {
        AlertsBottomSheet(
            uiState = uiState,
            onDismiss = { showAlertsSheet = false },
            onDismissAlert = { uuid -> alertsViewModel.dismissAlert(uuid) },
            onRestoreAlert = { uuid -> alertsViewModel.restoreAlert(uuid) },
            onRefresh = { alertsViewModel.refresh() },
            onClearError = { alertsViewModel.clearError() }
        )
    }
}