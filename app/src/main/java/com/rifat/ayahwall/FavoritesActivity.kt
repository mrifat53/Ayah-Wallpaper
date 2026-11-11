package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class FavoritesActivity : Activity() {

    private var lang = "en"
    private lateinit var list: LinearLayout

    private fun t(k: String): String = I18n.t(k, lang)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.setBackgroundColor(Color.parseColor("#0B0B0B"))
        outer.fitsSystemWindows = true
        outer.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        val title = Ui.screenTitle(this, t("favs_title"))
        outer.addView(title)
        outer.addView(Ads.banner(this))

        val scroll = ScrollView(this)
        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(dp(16), 0, dp(16), dp(16))
        scroll.addView(list)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        outer.addView(BottomNav.build(this, "profile"))
        setContentView(outer)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        list.removeAllViews()
        val favs = AyahSource.favorites(this)
        if (favs.isEmpty()) {
            val tv = TextView(this)
            tv.text = t("fav_empty")
            tv.setTextColor(Color.parseColor("#888888"))
            tv.setPadding(0, dp(12), 0, dp(12))
            list.addView(tv)
            return
        }
        val langs = Prefs.langs(this)
        for (a in favs) {
            val first = when {
                "ar" in langs -> a.arabic
                "bn" in langs -> a.bangla
                else -> a.english
            }
            val snippet = if (first.length > 90) first.substring(0, 90) + "…" else first
            val tv = TextView(this)
            tv.text = I18n.ref(lang, a.surah, a.ayah) + "\n" + snippet
            tv.textSize = 14f
            tv.setTextColor(Color.parseColor("#DDDDDD"))
            tv.setPadding(0, dp(10), 0, dp(6))
            list.addView(tv)

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            val show = Button(this)
            show.text = t("favs_show")
            show.setOnClickListener {
                AyahSource.setCurrentFirst(this, a)
                startActivity(Intent(this, TodayActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            }
            row.addView(show, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            val lock = Button(this)
            lock.text = t("lock_set")
            lock.setOnClickListener {
                WallpaperSetter.setLockAsync(this, a) { ok ->
                    Toast.makeText(this, t(if (ok) "lock_done" else "lock_fail"), Toast.LENGTH_SHORT).show()
                }
            }
            row.addView(lock, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            val rm = Button(this)
            rm.text = t("fav_remove")
            rm.setOnClickListener {
                AyahSource.toggleFav(this, a)
                render()
            }
            row.addView(rm, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            list.addView(row)
        }
    }
}
