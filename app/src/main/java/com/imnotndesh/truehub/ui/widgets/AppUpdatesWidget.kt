package com.imnotndesh.truehub.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.models.Apps

private val SMALL_WIDTH  = 80.dp
private val MEDIUM_WIDTH = 180.dp

private val SMALL_HEIGHT  = 80.dp
private val MEDIUM_HEIGHT = 180.dp

class AppsUpdateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AppsUpdateWidget()
}

class AppsUpdateWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val apps by WidgetDataStore.upgradableAppsFlow(context)
                .collectAsState(initial = emptyList())

            GlanceTheme {
                val size = LocalSize.current
                val isSmall  = size.width  < MEDIUM_WIDTH  || size.height < MEDIUM_HEIGHT
                val isLarge  = size.width  > MEDIUM_WIDTH  && size.height > MEDIUM_HEIGHT + 60.dp

                when {
                    isSmall  -> SmallWidget(upgradableApps = apps, context = context)
                    isLarge  -> LargeWidget(upgradableApps = apps, context = context)
                    else     -> MediumWidget(upgradableApps = apps, context = context)
                }
            }
        }
    }
}

private fun openAppsIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = "com.imnotndesh.truehub.OPEN_APPS"
        flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private fun upgradeIntent(context: Context, appName: String): Intent =
    Intent(context, UpgradeBottomSheetActivity::class.java).apply {
        putExtra("EXTRA_APP_NAME", appName)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }


@Composable
private fun SmallWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openAppsIntent(context))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment   = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(6.dp)
        ) {
            Text(
                text  = if (upgradableApps.isEmpty()) "✓" else upgradableApps.size.toString(),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 26.sp,
                    color      = if (upgradableApps.isEmpty())
                        GlanceTheme.colors.onSurfaceVariant
                    else
                        GlanceTheme.colors.primary
                )
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text  = if (upgradableApps.isEmpty()) "No updates" else "Updates",
                style = TextStyle(
                    fontSize = 10.sp,
                    color    = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}


@Composable
private fun MediumWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
    ) {
        // Header
        Row(
            modifier          = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "App Updates",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            if (upgradableApps.isNotEmpty()) {
                BadgePill(count = upgradableApps.size)
            }
        }

        // Content
        if (upgradableApps.isEmpty()) {
            Box(
                modifier          = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment  = Alignment.Center
            ) {
                Text(
                    text  = "No updates available",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth()
            ) {
                itemsIndexed(upgradableApps) { index, app ->
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        CompactAppRow(app = app, context = context)
                        if (index < upgradableApps.lastIndex) {
                            Spacer(GlanceModifier.height(4.dp)) // Spacing between rows
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun LargeWidget(upgradableApps: List<Apps.AppQueryResponse>, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xD9FFFFFF), Color(0xD91C1B1F))
            .cornerRadius(20.dp)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier          = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text  = "TrueNAS Updates",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = GlanceTheme.colors.primary
                    )
                )
                Text(
                    text  = if (upgradableApps.isEmpty()) "No updates available"
                    else "${upgradableApps.size} app(s) need attention",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            if (upgradableApps.isNotEmpty()) {
                BadgePill(count = upgradableApps.size)
            }
        }

        Spacer(GlanceModifier.height(4.dp))

        if (upgradableApps.isEmpty()) {
            Box(
                modifier         = GlanceModifier.defaultWeight().fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No updates available",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth()
            ) {
                itemsIndexed(upgradableApps) { index, app ->
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        LargeAppRow(app = app, context = context)
                        if (index < upgradableApps.lastIndex) {
                            Spacer(GlanceModifier.height(6.dp)) // Spacing between rows
                        }
                    }
                }
            }
        }
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

@Composable
private fun BadgePill(count: Int) {
    Box(
        modifier         = GlanceModifier
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(100.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = count.toString(),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize   = 11.sp,
                color      = GlanceTheme.colors.onPrimaryContainer
            )
        )
    }
}

/** Compact single-line row. Added horizontal padding, vertical item padding. */
@Composable
private fun CompactAppRow(app: Apps.AppQueryResponse, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp) // Enhanced row component internal bounds
            .clickable(actionStartActivity(upgradeIntent(context, app.name))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = app.metadata?.title ?: app.name,
            style    = TextStyle(
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = GlanceTheme.colors.onSurface
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Spacer(GlanceModifier.width(6.dp))
        Box(
            modifier         = GlanceModifier
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(6.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp), // Increased interior pill padding
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = app.latestVersion ?: "↑",
                style = TextStyle(
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onPrimaryContainer
                ),
                maxLines = 1
            )
        }
    }
}

/** Larger card row. Augmented internal vertical and horizontal cell boundaries. */
@Composable
private fun LargeAppRow(app: Apps.AppQueryResponse, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(10.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(actionStartActivity(upgradeIntent(context, app.name))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text     = app.metadata?.title ?: app.name,
                style    = TextStyle(
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            if (app.version != null) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text  = "v${app.version} → ${app.latestVersion ?: "latest"}",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color    = GlanceTheme.colors.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
        }
        Spacer(GlanceModifier.width(8.dp))
        Box(
            modifier         = GlanceModifier
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(6.dp)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "Update",
                style = TextStyle(
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}