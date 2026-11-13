package com.nexus.alarm

import android.content.*
import java.util.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences("nexus", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prefs.getInt("hour", 7))
            set(Calendar.MINUTE, prefs.getInt("minute", 0))
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        AlarmScheduler.schedule(context, cal.timeInMillis)
    }
}
