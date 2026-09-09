package com.imnotndesh.truehub.data.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.TrueNASClient
import com.imnotndesh.truehub.data.api.TrueNASApiManager
import com.imnotndesh.truehub.data.helpers.MultiAccountPrefs
import com.imnotndesh.truehub.data.helpers.WorkerSession
import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.helpers.dataStore
import com.imnotndesh.truehub.data.models.System
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AlertsWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "TrueNAS_Alerts_Sync"

        // Channels we expose to users for independent toggling.
        const val CHANNEL_SYSTEM = "truehub_system_channel"
        const val CHANNEL_INFORMATIONAL = "truehub_informational_channel"
        const val CHANNEL_WARNING = "truehub_warning_channel"

        private val SEEN_ALERTS_KEY = stringSetPreferencesKey("seen_alerts_ids")

        /**
         * Routes a TrueNAS alert to the correct channel based on its level and text.
         *
         * - Warning/critical levels → dedicated high-priority warning channel.
         * - System updates: text mentions "truenas version" or "system update".
         * - Application updates: text mentions "applications" or "updates are available".
         * - Everything else falls back to informational.
         */
        fun channelForAlert(alert: System.AlertResponse): String {
            val level = alert.level.lowercase()
            if (level == "warning" || level == "critical") {
                return CHANNEL_WARNING
            }

            val haystack = buildString {
                alert.formatted?.let { append(it.lowercase()) }
                append(' ')
                append(alert.text.lowercase())
            }

            return when {
                haystack.contains("truenas version") || haystack.contains("system update") -> CHANNEL_SYSTEM
                haystack.contains("applications") || haystack.contains("updates are available") -> CHANNEL_INFORMATIONAL
                else -> CHANNEL_INFORMATIONAL
            }
        }

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AlertsWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var client: TrueNASClient? = null
        try {
            val manager: TrueNASApiManager
            when (val session = WorkerSession.open(context)) {
                is WorkerSession.Result.Ready -> {
                    manager = session.manager
                    client = session.client
                }
                is WorkerSession.Result.Unauthenticated -> return@withContext Result.success()
                is WorkerSession.Result.Retryable ->
                    return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
            }

            val alertsResult = manager.system.listAlertsWithResult()

            when (alertsResult) {
                is ApiResult.Success -> {
                    val unDismissedAlerts = alertsResult.data.filter { !it.dismissed }
                    val currentActiveUuids = unDismissedAlerts.map { it.uuid }.toSet()

                    val prefs = context.dataStore.data.first()
                    val seenAlertUuids = prefs[SEEN_ALERTS_KEY] ?: emptySet()

                    val newAlerts = unDismissedAlerts.filter { !seenAlertUuids.contains(it.uuid) }

                    if (newAlerts.isNotEmpty()) {
                        notifyAlerts(newAlerts)
                    }

                    context.dataStore.edit { it[SEEN_ALERTS_KEY] = currentActiveUuids }
                }
                is ApiResult.Error -> {
                    Log.e("AlertsWorker", "Failed to list alerts: ${alertsResult.message}")
                }
                else -> {}
            }

            client.disconnect()
            Result.success()

        } catch (e: Exception) {
            Log.e("AlertsWorker", "Error fetching system alerts in background", e)
            client?.disconnect()
            Result.retry()
        }
    }

    private fun notifyAlerts(alerts: List<System.AlertResponse>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("AlertsWorker", "Skipping alert notification: POST_NOTIFICATIONS not granted")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Independently-toggleable channels, matching the user-facing grouping we want.
        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM, "System", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "System-level update and maintenance notifications" }
        val informationalChannel = NotificationChannel(
            CHANNEL_INFORMATIONAL, "Informational", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Informational and application update notifications" }
        val warningChannel = NotificationChannel(
            CHANNEL_WARNING, "Warnings", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "High-priority warnings and critical alerts" }

        notificationManager.createNotificationChannels(
            listOf(systemChannel, informationalChannel, warningChannel)
        )

        alerts.forEach { alert ->
            val notificationId = alert.uuid.hashCode()
            val channelId = channelForAlert(alert)

            val dismissIntent = Intent(context, DismissAlertReceiver::class.java).apply {
                action = DismissAlertReceiver.ACTION_DISMISS_ALERT
                putExtra(DismissAlertReceiver.EXTRA_ALERT_UUID, alert.uuid)
                putExtra(DismissAlertReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.imnotndesh.truehub.R.drawable.ic_stat_notification)
                .setContentTitle("TrueNAS Alert: ${alert.level}")
                .setContentText(alert.formatted ?: "A new system alert has been triggered.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(alert.formatted))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)
        }
    }
}