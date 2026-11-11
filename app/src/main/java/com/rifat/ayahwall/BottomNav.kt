package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

// Bottom bar shared by every screen: Home, Feed (middle, main), Profile.
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

        val items = listOf(
            Triple("home", R.drawable.ic_home, I18n.t("nav_home", lang)),
            Triple("feed", R.drawable.ic_book, I18n.t("nav_feed", lang)),
            Triple("profile", R.drawable.ic_person, I18n.t("nav_profile", lang))
        )

        for ((key, icon, label) in items) {
            val isFeed = key == "feed"
            val selected = key == current || isFeed
            val col = LinearLayout(activity)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER
            col.setPadding(dp(4), dp(if (isFeed) 10 else 8), dp(4), dp(if (isFeed) 10 else 8))
            if (selected) {
                val bg = GradientDrawable()
                bg.setColor(Color.parseColor(if (isFeed) "#1B3A2E" else "#1B2A24"))
                bg.cornerRadius = dp(14).toFloat()
                col.background = bg
            }
            val color = if (selected) Color.parseColor("#8FD3B0") else Color.parseColor("#9AA5A0")

            val iv = ImageView(activity)
            iv.setImageResource(icon)
            iv.setColorFilter(color)
            val size = dp(if (isFeed) 28 else 24)
            col.addView(iv, LinearLayout.LayoutParams(size, size))

            val tv = TextView(activity)
            tv.text = label
            tv.textSize = if (isFeed) 13f else 12f
            tv.gravity = Gravity.CENTER
            tv.setTextColor(color)
            if (selected) tv.setTypeface(null, Typeface.BOLD)
            col.addView(tv)

            col.setOnClickListener { go(activity, key) }
            val weight = if (isFeed) 1.3f else 1f
            bar.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight))
        }
        return bar
    }

    private fun go(activity: Activity, key: String) {
        val intent = when (key) {
            "feed" -> Intent(activity, FeedActivity::class.java)
            "home" -> Intent(activity, TodayActivity::class.java)
            else -> Intent(activity, MainActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        activity.startActivity(intent)
    }
}
