package com.imnotndesh.truehub.data.helpers

import android.content.Context
import com.imnotndesh.truehub.data.models.Apps
import com.imnotndesh.truehub.ui.widgets.appcontrol.AppControlWidgetUpdater

object AppControlSync {
    suspend fun refresh(context: Context, apps: List<Apps.AppQueryResponse>) {
        val current = WidgetDataStore.getAppControlEntry(context) ?: return

        val live = apps.find { it.name == current.appName } ?: return

        val newState = live.state.lowercase()
        if (newState == current.state.lowercase()) return  // no change, skip update

        val updated = current.copy(state = live.state)
        WidgetDataStore.saveAppControlEntry(context, updated)
        AppControlWidgetUpdater.update(context)
    }
}
