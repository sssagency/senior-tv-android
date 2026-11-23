package org.seniortv.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Best-effort boot launch; Android TV firmware may block background launches. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        try {
            val launch = Intent(context, MainActivity::class.java)
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(launch)
        } catch (error: Exception) {
            Log.w("SeniorEducationTV", "Firmware blocked boot auto-launch", error)
        }
    }
}
