package com.imnotndesh.truehub.data.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.imnotndesh.truehub.BuildConfig
import com.imnotndesh.truehub.MainActivity
import com.imnotndesh.truehub.R
import com.imnotndesh.truehub.data.helpers.ApkDownloader
import com.imnotndesh.truehub.data.helpers.ApkVerifier
import com.imnotndesh.truehub.data.helpers.UpdatePrefs
import com.imnotndesh.truehub.data.models.UpdateInfo
import java.io.File

class AppUpdateDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "TrueHub_App_Update_Download"
        const val KEY_URL = "update_url"
        const val KEY_VERSION = "update_version"
        const val KEY_VERSION_CODE = "update_version_code"
        const val KEY_SHA256 = "update_sha256"
        const val KEY_SIZE = "update_size"
        const val KEY_PROGRESS = "update_progress"

        private const val DOWNLOAD_CHANNEL_ID = "truehub_app_update_download"
        private const val DOWNLOAD_NOTIFICATION_ID = 0x7549
        private const val READY_NOTIFICATION_ID = 0x754A

        suspend fun enqueue(context: Context, info: UpdateInfo) {
            val asset = info.asset ?: return
            val data = workDataOf(
                KEY_URL to asset.url,
                KEY_VERSION to info.versionName,
                KEY_VERSION_CODE to info.versionCode,
                KEY_SHA256 to (asset.sha256 ?: ""),
                KEY_SIZE to asset.size
            )
            val networkType = if (UpdatePrefs.isWifiOnly(context)) NetworkType.UNMETERED else NetworkType.CONNECTED
            val request = OneTimeWorkRequestBuilder<AppUpdateDownloadWorker>()
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).build())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        if (BuildConfig.IS_PLAYSTORE_BUILD) return Result.failure()
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val version = inputData.getString(KEY_VERSION).orEmpty()
        val versionCode = inputData.getInt(KEY_VERSION_CODE, 0)
        val expectedSha = inputData.getString(KEY_SHA256).orEmpty()
        val expectedSize = inputData.getLong(KEY_SIZE, 0L)

        val updatesDir = File(context.filesDir, "updates").apply { mkdirs() }
        val target = File(updatesDir, "truehub-$versionCode.apk")
        val partial = File(updatesDir, "truehub-$versionCode.apk.part")

        setForeground(foregroundInfo(0))

        val result = ApkDownloader.download(url, partial, expectedSize) { percent ->
            setProgress(workDataOf(KEY_PROGRESS to percent))
            if (percent % 5 == 0) setForeground(foregroundInfo(percent))
        }

        return result.fold(
            onSuccess = {
                val valid = ApkVerifier.checksumMatches(partial, expectedSha) &&
                    ApkVerifier.signatureMatches(context, partial)
                if (!valid) {
                    partial.delete()
                    return Result.failure()
                }
                target.delete()
                if (!partial.renameTo(target)) {
                    partial.delete()
                    return Result.failure()
                }
                UpdatePrefs.setReady(context, version, target.absolutePath)
                notifyReady(version)
                Result.success()
            },
            onFailure = {
                partial.delete()
                if (runAttemptCount < 2) Result.retry() else Result.failure()
            }
        )
    }

    private fun foregroundInfo(progress: Int): ForegroundInfo {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(DOWNLOAD_CHANNEL_ID, "Update downloads", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Downloading TrueHub update")
            .setContentText("$progress%")
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
        return ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun notifyReady(version: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(AppUpdateWorker.CHANNEL_ID, "App Updates", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = AppUpdateWorker.ACTION_OPEN_APP_UPDATE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            READY_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, AppUpdateWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("TrueHub $version ready to install")
            .setContentText("Tap to finish the update.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(READY_NOTIFICATION_ID, notification)
    }
}
