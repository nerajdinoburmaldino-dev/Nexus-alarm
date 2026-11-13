package com.nexus.alarm

import android.app.*
import android.content.*
import android.os.Build

object AlarmScheduler {
    fun schedule(context: Context, time: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= 23) am.setAlarmClock(AlarmManager.AlarmClockInfo(time, pi), pi)
            else am.setExact(AlarmManager.RTC_WAKEUP, time, pi)
        } catch (_: SecurityException) {}
    }
}
