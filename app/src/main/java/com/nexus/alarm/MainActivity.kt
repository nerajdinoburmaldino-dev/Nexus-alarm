package com.nexus.alarm

import android.Manifest
import android.app.*
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private var hour = 7
    private var minute = 0
    private var soundUri: Uri? = null
    private lateinit var timeText: TextView
    private lateinit var soundText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("nexus", MODE_PRIVATE)
        hour = prefs.getInt("hour", 7)
        minute = prefs.getInt("minute", 0)
        soundUri = prefs.getString("sound", null)?.let { Uri.parse(it) }
        buildUi()
        requestPermissionsIfNeeded()
        createChannel()
    }

    private fun buildUi() {
        val bg = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(0xFF080B18.toInt(), 0xFF17102F.toInt(), 0xFF090D1B.toInt())
        )
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(26, 30, 26, 30)
            background = bg
        }
        val title = TextView(this).apply {
            text = "N E X U S   A L A R M"
            textSize = 19f
            setTextColor(0xFFEDEBFF.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "WAKE UP IN YOUR OWN WAY"
            textSize = 10f
            letterSpacing = 0.22f
            setTextColor(0xFF9C91C9.toInt())
            gravity = Gravity.CENTER
        }
        timeText = TextView(this).apply {
            textSize = 54f
            typeface = android.graphics.Typeface.create("sans-serif-light", 0)
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 34, 0, 4)
        }
        val timeHint = TextView(this).apply {
            text = "ВРЕМЯ ПРОБУЖДЕНИЯ"
            textSize = 10f
            letterSpacing = 0.18f
            setTextColor(0xFFAAA2D0.toInt())
            gravity = Gravity.CENTER
        }
        val timeButton = makeButton("ИЗМЕНИТЬ ВРЕМЯ") { showTimePicker() }
        soundText = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFE7E3FF.toInt())
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 8)
        }
        val soundButton = makeButton("ВЫБРАТЬ МОЮ МЕЛОДИЮ") { chooseSound() }
        val setButton = makeButton("СОХРАНИТЬ БУДИЛЬНИК") { scheduleAlarm() }
        setButton.background = rounded(0xFF7658E8.toInt())
        val note = TextView(this).apply {
            text = "Будильник сработает даже при выключенном экране, если Android разрешит точный сигнал и уведомления."
            textSize = 11f
            setTextColor(0xFF9690B4.toInt())
            gravity = Gravity.CENTER
            setPadding(10, 24, 10, 0)
        }
        root.addView(title, matchWrap())
        root.addView(subtitle, matchWrap())
        root.addView(timeText, matchWrap())
        root.addView(timeHint, matchWrap())
        root.addView(timeButton, buttonParams())
        root.addView(soundText, matchWrap())
        root.addView(soundButton, buttonParams())
        root.addView(setButton, buttonParams())
        root.addView(note, matchWrap())
        setContentView(root)
        updateLabels()
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun buttonParams() = LinearLayout.LayoutParams(-1, 56).apply { topMargin = 12 }
    private fun rounded(color: Int) = android.graphics.drawable.GradientDrawable().apply {
        setColor(color); cornerRadius = 28f
    }
    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 12f
        letterSpacing = 0.08f
        setTextColor(0xFFFFFFFF.toInt())
        background = rounded(0xFF292344.toInt())
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun updateLabels() {
        timeText.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        soundText.text = if (soundUri == null) "Звук: стандартный сигнал телефона" else "Звук: выбранный файл"
    }

    private fun showTimePicker() {
        TimePickerDialog(this, { _, h, m ->
            hour = h; minute = m
            getSharedPreferences("nexus", MODE_PRIVATE).edit()
                .putInt("hour", h).putInt("minute", m).apply()
            updateLabels()
        }, hour, minute, true).show()
    }

    private val pickSound = registerForActivityResultCompat()
    private fun registerForActivityResultCompat() = Unit

    private fun chooseSound() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, 101)
    }

    @Deprecated("Legacy picker callback for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                soundUri = uri
                getSharedPreferences("nexus", MODE_PRIVATE).edit().putString("sound", uri.toString()).apply()
                updateLabels()
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 22)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } catch (_: Exception) {}
            }
        }
        if (Build.VERSION.SDK_INT >= 34 && !getSystemService(NotificationManager::class.java).canUseFullScreenIntent()) {
            try { startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))) } catch (_: Exception) {}
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("nexus_alarm", "NEXUS Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarm notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun scheduleAlarm() {
        val prefs = getSharedPreferences("nexus", MODE_PRIVATE)
        prefs.edit().putInt("hour", hour).putInt("minute", minute)
            .putString("sound", soundUri?.toString()).putBoolean("enabled", true).apply()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi), pi)
            } else am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            Toast.makeText(this, "Будильник установлен на ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Разреши точные будильники в настройках Android", Toast.LENGTH_LONG).show()
            requestPermissionsIfNeeded()
        }
    }
}
