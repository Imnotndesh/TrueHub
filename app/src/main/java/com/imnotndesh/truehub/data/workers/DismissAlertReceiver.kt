package com.imnotndesh.truehub.data.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class DismissAlertReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS_ALERT = "com.imnotndesh.truehub.ACTION_DISMISS_ALERT"
        const val EXTRA_ALERT_UUID = "extra_alert_uuid"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_SERVER_ID = "extra_server_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISS_ALERT) return

        val alertUuid = intent.getStringExtra(EXTRA_ALERT_UUID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        DismissAlertWorker.enqueue(
            context,
            alertUuid,
            intent.getStringExtra(EXTRA_SERVER_ID),
            intent.getStringExtra(EXTRA_ACCOUNT_ID)
        )
    }
}