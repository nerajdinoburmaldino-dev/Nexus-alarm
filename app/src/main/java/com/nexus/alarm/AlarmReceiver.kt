package com.nexus.alarm

import android.app.*
import android.content.*
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val full = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullPi = PendingIntent.getActivity(context, 2, full, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, "nexus_alarm")
            else @Suppress("DEPRECATION") Notification.Builder(context)
        val notification = builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("NEXUS ALARM")
            .setContentText("Пора просыпаться")
            .setCategory(Notification.CATEGORY_ALARM)
            .setPriority(Notification.PRIORITY_MAX)
            .setFullScreenIntent(fullPi, true)
            .setContentIntent(fullPi)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(7, notification)
    }
}
