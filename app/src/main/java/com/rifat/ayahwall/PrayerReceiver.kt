package com.rifat.ayahwall

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, intent: Intent) {
        val name = intent.getStringExtra("name") ?: return
        PrayerAlarm.ensureChannel(c)
        val lang = Prefs.uiLang(c)
        val open = PendingIntent.getActivity(c, 0, Intent(c, TodayActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(c, PrayerAlarm.CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(I18n.t("pt_" + name, lang))
            .setContentText(I18n.t("pt_now", lang))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(name.hashCode(), n)
        PrayerAlarm.scheduleNext(c)
    }
}
