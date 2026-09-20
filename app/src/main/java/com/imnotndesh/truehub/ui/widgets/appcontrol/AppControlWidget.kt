package com.imnotndesh.truehub.ui.widgets.appcontrol

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.data.helpers.IconCache
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.AppControlEntry

class AppControlWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AppControlWidget()
}

class AppControlWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val entry by WidgetDataStore.appControlEntryFlow(context)
                .collectAsState(initial = null)

            GlanceTheme {
                if (entry == null) {
                    AppControlEmptyState(context)
                } else {
                    AppControlContent(entry = entry!!, context = context)
                }
            }
        }
    }
}

private fun configIntent(context: Context): Intent =
    Intent(context, AppControlConfigActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

@Composable
private fun AppControlEmptyState(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(configIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
            }
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "No app configured",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Tap to select",
                style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun AppControlContent(entry: AppControlEntry, context: Context) {
    val isRunning = entry.state.equals("running", ignoreCase = true)
    val bitmap = IconCache.loadCachedBitmap(entry.cachedIconPath)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .padding(14.dp),
    ) {
        // Header: icon + name + config tap
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(configIntent(context))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .cornerRadius(12.dp)
                    .background(GlanceTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = entry.title,
                        modifier = GlanceModifier.size(34.dp)
                    )
                } else {
                    Text(
                        text = entry.title.take(1).uppercase(),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
            Spacer(GlanceModifier.width(10.dp))
            // App name
            Text(
                text = entry.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1
            )
        }

        Spacer(GlanceModifier.height(10.dp))

        // State badge
        Box(
            modifier = GlanceModifier
                .wrapContentWidth()
                .background(
                    if (isRunning) Color(0x1F2E7D32) else Color(0x1F757575),
                    if (isRunning) Color(0x1F4CAF50) else Color(0x1F9E9E9E)
                )
                .cornerRadius(100.dp)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRunning) "\u25CF Running" else "\u25CF Stopped",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            )
        }

        Spacer(GlanceModifier.defaultWeight())

        // Action button: Start or Stop
        val actionLabel = if (isRunning) "\u25A0  Stop" else "\u25BA  Start"
        val actionValue = if (isRunning) "stop" else "start"
        val actionBgDay = if (isRunning) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        val actionBgNight = if (isRunning) Color(0xFF4E1010) else Color(0xFF0D3318)
        val actionTextColor = if (isRunning) Color(0xFFD32F2F) else Color(0xFF2E7D32)

        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(actionBgDay, actionBgNight)
                .cornerRadius(14.dp)
                .padding(vertical = 10.dp)
                .clickable(
                    actionRunCallback<AppControlActionCallback>(
                        actionParametersOf(
                            AppControlActionCallback.actionKey to actionValue,
                            AppControlActionCallback.appNameKey to entry.appName
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }
    }
}
