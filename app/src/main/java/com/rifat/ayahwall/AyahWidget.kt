package com.rifat.ayahwall

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

// Home-screen widget with the latest ayah. Text only, tap opens the app.
class AyahWidget : AppWidgetProvider() {

    override fun onUpdate(c: Context, mgr: AppWidgetManager, ids: IntArray) {
        val a = AyahSource.current(c)
        val lang = Prefs.uiLang(c)
        val pi = PendingIntent.getActivity(c, 0, Intent(c, TodayActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        for (id in ids) {
            val v = RemoteViews(c.packageName, R.layout.widget_ayah)
            if (a != null) {
                v.setTextViewText(R.id.wAr, a.arabic)
                val bn = if (a.bangla.isNotBlank()) a.bangla else a.english
                v.setTextViewText(R.id.wBn, if (bn.length > 120) bn.substring(0, 120) + "…" else bn)
                v.setTextViewText(R.id.wRef, I18n.ref(lang, a.surah, a.ayah))
            }
            v.setOnClickPendingIntent(R.id.wRoot, pi)
            mgr.updateAppWidget(id, v)
        }
    }

    companion object {
        fun refreshAll(c: Context) {
            val mgr = AppWidgetManager.getInstance(c)
            val ids = mgr.getAppWidgetIds(ComponentName(c, AyahWidget::class.java))
            if (ids.isEmpty()) return
            val i = Intent(c, AyahWidget::class.java)
            i.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            c.sendBroadcast(i)
        }
    }
}
