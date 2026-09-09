package com.imnotndesh.truehub.ui.widgets.quicklaunch

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.data.helpers.IconCache
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.QuickLaunchApp

private const val MAX_ICONS = 6

class QuickLaunchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QuickLaunchWidget()
}

class QuickLaunchWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val apps by WidgetDataStore.quickLaunchAppsFlow(context)
                .collectAsState(initial = emptyList())

            GlanceTheme {
                if (apps.isEmpty()) {
                    EmptyState(context)
                } else {
                    QuickLaunchRow(apps = apps.take(MAX_ICONS), context = context)
                }
            }
        }
    }
}

private fun configIntent(context: Context): Intent =
    Intent(context, QuickLaunchConfigActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

@Composable
private fun EmptyState(context: Context) {
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
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onPrimaryContainer)
                )
            }
            Spacer(GlanceModifier.height(8.dp))
            Text(text = "No apps configured", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}

@Composable
private fun QuickLaunchRow(apps: List<QuickLaunchApp>, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEachIndexed { index, app ->
            QuickLaunchIcon(app = app)
            if (index < apps.lastIndex) Spacer(GlanceModifier.width(14.dp))
        }
        Spacer(GlanceModifier.width(14.dp))
        AddMoreTile(context = context)
    }
}

@Composable
private fun QuickLaunchIcon(app: QuickLaunchApp) {
    val launchIntent = Intent(Intent.ACTION_VIEW, app.webUiUrl.toUri()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val bitmap = IconCache.loadCachedBitmap(app.cachedIconPath)

    Box(
        modifier = GlanceModifier
            .size(56.dp)
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .clickable(actionStartActivity(launchIntent)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = app.title,
                modifier = GlanceModifier.size(44.dp)
            )
        } else {
            Text(
                text = app.title.take(1).uppercase(),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

@Composable
private fun AddMoreTile(context: Context) {
    Box(
        modifier = GlanceModifier
            .size(56.dp)
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(actionStartActivity(configIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "+", style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onPrimaryContainer))
    }
}