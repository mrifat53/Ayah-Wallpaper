package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

// Bottom bar shared by every screen: Home, Feed (middle, main), Profile.
// Profile holds all settings, including battery.
object BottomNav {

    fun build(activity: Activity, current: String): LinearLayout {
        val lang = Prefs.uiLang(activity)
        val dm = activity.resources.displayMetrics
        fun dp(v: Int): Int = (v * dm.density).toInt()

        val bar = LinearLayout(activity)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setBackgroundColor(Color.parseColor("#101418"))
        bar.setPadding(dp(6), dp(6), dp(6), dp(8))

        // key, icon, label, weight. Feed is the middle tab and a bit bigger.
        val items = listOf(
            listOf("home", "\uD83C\uDFE0", I18n.t("nav_home", lang), "1"),
            listOf("feed", "\uD83D\uDCD6", I18n.t("nav_feed", lang), "1.4"),
            listOf("profile", "\uD83D\uDC64", I18n.t("nav_profile", lang), "1")
        )

        for (item in items) {
            val key = item[0]
            val tv = TextView(activity)
            tv.text = item[1] + "\n" + item[2]
            tv.gravity = Gravity.CENTER
            val selected = key == current
            val isFeed = key == "feed"
            tv.textSize = if (isFeed) 15f else 13f
            tv.setPadding(dp(4), dp(if (isFeed) 12 else 10), dp(4), dp(if (isFeed) 12 else 10))
            tv.setTextColor(
                if (selected || isFeed) Color.parseColor("#8FD3B0") else Color.parseColor("#AAAAAA")
            )
            if (selected || isFeed) tv.setTypeface(null, Typeface.BOLD)
            if (selected || isFeed) {
                tv.setBackgroundColor(Color.parseColor(if (isFeed) "#1B3A2E" else "#1B2A24"))
            }
            tv.setOnClickListener { go(activity, key) }
            val weight = item[3].toFloat()
            bar.addView(tv, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight))
        }
        return bar
    }

    private fun go(activity: Activity, key: String) {
        val intent = when (key) {
            "feed" -> Intent(activity, FeedActivity::class.java)
            "home" -> Intent(activity, TodayActivity::class.java)
            else -> Intent(activity, MainActivity::class.java)
        }
        // Bring an open screen to the front instead of stacking copies of it
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        activity.startActivity(intent)
    }
}
