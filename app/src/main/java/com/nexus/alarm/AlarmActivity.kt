package com.nexus.alarm

import android.app.*
import android.content.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class AlarmActivity : Activity() {
    private var ringtone: Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags( android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        val prefs = getSharedPreferences("nexus", MODE_PRIVATE)
        val uri = prefs.getString("sound", null)?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        try {
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        } catch (_: Exception) {
            ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            ringtone?.play()
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(28, 30, 28, 30)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(0xFF090B19.toInt(), 0xFF29164C.toInt(), 0xFF090B19.toInt()))
        }
        val title = TextView(this).apply {
            text = "N E X U S"; textSize = 17f; gravity = Gravity.CENTER
            setTextColor(0xFFBBAAFF.toInt())
        }
        val time = TextView(this).apply {
            text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            textSize = 64f; gravity = Gravity.CENTER; setTextColor(0xFFFFFFFF.toInt())
        }
        val hint = TextView(this).apply {
            text = "ПОРА ПРОСЫПАТЬСЯ"; textSize = 13f; gravity = Gravity.CENTER
            setTextColor(0xFFC9BFFF.toInt()); setPadding(0, 8, 0, 35)
        }
        val stop = Button(this).apply {
            text = "ВЫКЛЮЧИТЬ"; setOnClickListener { stopAlarm() }
        }
        val snooze = Button(this).apply {
            text = "ЕЩЁ 5 МИНУТ"; setOnClickListener {
                stopSound()
                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 5) }
                AlarmScheduler.schedule(this@AlarmActivity, cal.timeInMillis)
                finish()
            }
        }
        root.addView(title); root.addView(time); root.addView(hint)
        root.addView(stop, LinearLayout.LayoutParams(-1, 58))
        root.addView(snooze, LinearLayout.LayoutParams(-1, 58))
        setContentView(root)
    }
    private fun stopSound() { try { ringtone?.stop() } catch (_: Exception) {} }
    private fun stopAlarm() {
        stopSound()
        getSystemService(NotificationManager::class.java).cancel(7)
        getSharedPreferences("nexus", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
        finish()
    }
    override fun onDestroy() { stopSound(); super.onDestroy() }
}
