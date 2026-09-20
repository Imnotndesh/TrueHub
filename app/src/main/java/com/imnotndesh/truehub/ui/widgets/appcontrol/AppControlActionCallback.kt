package com.imnotndesh.truehub.ui.widgets.appcontrol

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.imnotndesh.truehub.data.helpers.WidgetDataStore
import com.imnotndesh.truehub.data.helpers.WorkerSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppControlActionCallback : ActionCallback {

    companion object {
        val actionKey = ActionParameters.Key<String>("action")  // "start" or "stop"
        val appNameKey = ActionParameters.Key<String>("appName")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val action = parameters[actionKey] ?: return
        val appName = parameters[appNameKey] ?: return

        withContext(Dispatchers.IO) {
            val session = WorkerSession.open(context)
            if (session !is WorkerSession.Result.Ready) return@withContext
            val manager = session.manager
            val client = session.client

            try {
                when (action) {
                    "start" -> {
                        manager.apps.startAppWithResult(appName)
                        WidgetDataStore.updateAppControlState(context, appName, "running")
                    }
                    "stop" -> {
                        manager.apps.stopAppWithResult(appName)
                        WidgetDataStore.updateAppControlState(context, appName, "stopped")
                    }
                }
            } finally {
                client.disconnect()
            }
        }

        AppControlWidgetUpdater.update(context)
    }
}
