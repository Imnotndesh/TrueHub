package com.imnotndesh.truehub.ui.widgets.appcontrol

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object AppControlWidgetUpdater {
    suspend fun update(context: Context) {
        GlanceAppWidgetManager(context)
            .getGlanceIds(AppControlWidget::class.java)
            .forEach { AppControlWidget().update(context, it) }
    }
}
