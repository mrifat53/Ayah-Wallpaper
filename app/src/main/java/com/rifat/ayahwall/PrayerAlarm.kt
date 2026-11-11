package com.rifat.ayahwall

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Schedules the next prayer reminder for today. Android may delay it by a few minutes.
object PrayerAlarm {

    const val CHANNEL = "prayer"
    private const val REQ = 901

    fun ensureChannel(c: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = c.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Prayer", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    private fun pending(c: Context, name: String): PendingIntent {
        val i = Intent(c, PrayerReceiver::class.java).putExtra("name", name)
        return PendingIntent.getBroadcast(c, REQ, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun cancel(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(c, ""))
    }

    fun scheduleNext(c: Context) {
        val sp = Prefs.sp(c)
        if (!sp.getBoolean("pt_remind", false)) return
        ensureChannel(c)
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (sp.getString("pt_day", "") != day) return
        val s = sp.getString("pt_json", null) ?: return

        val now = System.currentTimeMillis()
        var best: Pair<String, Long>? = null
        for (part in s.split(";")) {
            val kv = part.split("=")
            if (kv.size != 2) continue
            val hm = kv[1].split(":")
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hm[0].toInt())
            cal.set(Calendar.MINUTE, hm[1].toInt())
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val ms = cal.timeInMillis
            if (ms > now + 60000) {
                val b = best
                if (b == null || ms < b.second) best = Pair(kv[0], ms)
            }
        }
        val b = best ?: return
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, b.second, pending(c, b.first))
    }
}
