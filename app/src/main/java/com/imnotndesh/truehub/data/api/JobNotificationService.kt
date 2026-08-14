package com.imnotndesh.truehub.data.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.data.workers.CancelJobReceiver

class JobNotificationService : Service() {

    private companion object {
        // Reuse the same channel identities as AlertsWorker so users get one consistent
        // two-channel split (System vs Informational) across the whole app.
        const val CHANNEL_SYSTEM = "truehub_system_channel"
        const val CHANNEL_INFORMATIONAL = "truehub_informational_channel"

        fun channelForType(type: String?): String = when {
            type.equals("SYSTEM_UPDATE", ignoreCase = true) -> CHANNEL_SYSTEM
            else -> CHANNEL_INFORMATIONAL
        }
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val activeJobsTracker = mutableSetOf<Int>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val jobId = intent.getIntExtra("id", -1)
        val type = intent.getStringExtra("type")
        val appName = intent.getStringExtra("name") ?: "System Task"
        val progress = intent.getIntExtra("progress", 0)
        val isDone = intent.getBooleanExtra("done", false)
        val statusText = intent.getStringExtra("status_text") ?: "Provisioning resource nodes..."

        if (jobId != -1) {
            handler.removeCallbacksAndMessages(jobId)

            if (isDone) {
                // Job finished: build a distinct, non-ongoing completion notification and
                // demote it out of the foreground/live state so it visibly shows "Done"
                // instead of vanishing abruptly.
                val doneNotification = buildJobNotification(jobId, type, appName, progress, statusText, isDone)
                activeJobsTracker.remove(jobId)

                if (activeJobsTracker.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
                notificationManager.notify(jobId, doneNotification)

                val token = jobId
                handler.postAtTime({
                    notificationManager.cancel(jobId)
                    checkAndShutdownService()
                }, token, android.os.SystemClock.uptimeMillis() + 6000)
            } else {
                val notification = buildJobNotification(jobId, type, appName, progress, statusText, isDone)
                if (activeJobsTracker.isEmpty()) {
                    activeJobsTracker.add(jobId)
                    startForeground(jobId, notification)
                } else {
                    activeJobsTracker.add(jobId)
                    notificationManager.notify(jobId, notification)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun checkAndShutdownService() {
        if (activeJobsTracker.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildJobNotification(
        jobId: Int,
        type: String?,
        appName: String,
        progress: Int,
        statusText: String,
        isDone: Boolean
    ): Notification {
        val titleText = if (isDone) "✓ Completed: $appName" else appName
        val explicitStatus = if (isDone) "Task finished successfully." else statusText
        val channelId = channelForType(type)

        val builder = NotificationCompat.Builder(this, channelId)
            .setAutoCancel(isDone)
            .setContentTitle(titleText)
            .setContentText(explicitStatus)
            .setSmallIcon(com.imnotndesh.truehub.R.drawable.ic_stat_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setOngoing(!isDone)
            .setStyle(
                NotificationCompat.ProgressStyle()
                    .addProgressSegment(NotificationCompat.ProgressStyle.Segment(100))
                    .setProgress(if (isDone) 100 else progress)
            )

        // For system updates, make the live-update card tappable to open the update
        // screen in-app (SPECIFICALLY for SYSTEM_UPDATE jobs).
        if (type.equals("SYSTEM_UPDATE", ignoreCase = true)) {
            val openUpdateIntent = Intent(this, MainActivity::class.java).apply {
                action = "com.imnotndesh.truehub.OPEN_SYSTEM_UPDATE"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val contentPendingIntent = PendingIntent.getActivity(
                this,
                jobId,
                openUpdateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(contentPendingIntent)
        }

        // Request OS promotion as a Live Update (Android 15+ / API 35+). This is a no-op
        // on older platforms and only applies while the job is still in progress.
        if (!isDone) {
            builder.setRequestPromotedOngoing(true)
            builder.setShortCriticalText("$progress%")
        } else {
            // For the completed (non-promoted) notification, show the full-size launcher
            // icon so the app branding reads clearly in the expanded notification.
            builder.setLargeIcon(
                android.graphics.BitmapFactory.decodeResource(resources, com.imnotndesh.truehub.R.mipmap.ic_launcher)
            )
        }

        if (!isDone) {
            val cancelIntent = Intent(this, CancelJobReceiver::class.java).apply {
                action = CancelJobReceiver.ACTION_CANCEL_JOB
                putExtra(CancelJobReceiver.EXTRA_JOB_ID, jobId)
            }
            val cancelPendingIntent = PendingIntent.getBroadcast(
                this,
                jobId,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                cancelPendingIntent
            )
        }

        return builder.build()
    }
    private fun createNotificationChannel() {
        val systemChannel = NotificationChannel(
            CHANNEL_SYSTEM,
            "System",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "System update and maintenance progress"
            setShowBadge(false)
        }
        val informationalChannel = NotificationChannel(
            CHANNEL_INFORMATIONAL,
            "Informational",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Application install, update, and deployment progress"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannels(
            listOf(systemChannel, informationalChannel)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}