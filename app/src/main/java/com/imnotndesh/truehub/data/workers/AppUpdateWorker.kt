package com.imnotndesh.truehub.data.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.imnotndesh.truehub.BuildConfig
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.R
import com.imnotndesh.truehub.data.helpers.UpdatePrefs
import com.imnotndesh.truehub.data.helpers.UpdateRepository
import com.imnotndesh.truehub.data.models.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "TrueHub_App_Update_Check"
        const val CHANNEL_ID = "truehub_app_updates"
        const val ACTION_OPEN_APP_UPDATE = "com.imnotndesh.truehub.OPEN_APP_UPDATE"
        private const val NOTIFICATION_ID = 0x7548

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<AppUpdateWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (BuildConfig.IS_PLAYSTORE_BUILD) return@withContext Result.success()
        if (!UpdatePrefs.isAutoCheckEnabled(context)) return@withContext Result.success()

        UpdateRepository.check(context).fold(
            onSuccess = { info ->
                if (info != null) notifyIfNeeded(info)
                Result.success()
            },
            onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() }
        )
    }

    private suspend fun notifyIfNeeded(info: UpdateInfo) {
        val dismissed = UpdatePrefs.getDismissedVersion(context)
        val notified = UpdatePrefs.getLastNotifiedVersion(context)
        if (dismissed == info.versionName || notified == info.versionName) return

        postNotification(info)
        UpdatePrefs.setLastNotifiedVersion(context, info.versionName)
    }

    private fun postNotification(info: UpdateInfo) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "App Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "New TrueHub releases"
            }
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_APP_UPDATE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summary = info.notes?.lineSequence()?.firstOrNull { it.isNotBlank() }?.trim()
            ?: "Tap to see what's new."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("TrueHub ${info.versionName} is available")
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(info.notes ?: summary))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
