package com.imnotndesh.truehub.data.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.imnotndesh.truehub.data.workers.AppUpdateInstallReceiver
import java.io.File

object ApkInstaller {

    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun unknownSourcesIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())

    fun install(context: Context, apkFile: File): Result<Unit> =
        runCatching { commitSession(context, apkFile) }
            .recoverCatching { openSystemInstaller(context, apkFile) }

    private fun commitSession(context: Context, apkFile: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setSize(apkFile.length())
        }
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)
        session.use { activeSession ->
            activeSession.openWrite("base.apk", 0, apkFile.length()).use { output ->
                apkFile.inputStream().use { input -> input.copyTo(output) }
            }
            val statusIntent = Intent(context, AppUpdateInstallReceiver::class.java).apply {
                action = AppUpdateInstallReceiver.ACTION_INSTALL_STATUS
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                sessionId,
                statusIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
            )
            activeSession.commit(pendingIntent.intentSender)
        }
    }

    private fun openSystemInstaller(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
