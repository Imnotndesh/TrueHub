package com.imnotndesh.truehub.data.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat
import com.imnotndesh.truehub.data.helpers.TrueHubLogger

class AppUpdateInstallReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_INSTALL_STATUS = "com.imnotndesh.truehub.ACTION_INSTALL_STATUS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_STATUS) return

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                confirmation?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                confirmation?.let { context.startActivity(it) }
            }
            PackageInstaller.STATUS_SUCCESS ->
                TrueHubLogger.e("AppUpdate", "Update installed successfully")
            else ->
                TrueHubLogger.e("AppUpdate", "Install failed: ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}")
        }
    }
}
