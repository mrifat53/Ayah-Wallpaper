package com.rifat.ayahwall

import android.widget.ImageView
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
        Theme.refresh(this)
        lang = Prefs.uiLang(this)

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.setBackgroundColor(Theme.col("bg"))
        outer.fitsSystemWindows = true
        outer.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        val title = Ui.screenTitle(this, t("favs_title"))
        outer.addView(title)

        val scroll = ScrollView(this)
        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(dp(16), 0, dp(16), dp(16))
        scroll.addView(list)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        outer.addView(BottomNav.build(this, "profile"))
        Ui.fadeIn(outer)
        setContentView(outer)
        Theme.applyBars(this)
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
            tv.setTextColor(Theme.col("muted"))
            tv.setPadding(0, dp(12), 0, dp(12))
            list.addView(tv)
            return
        }
        val w = (resources.displayMetrics.widthPixels - dp(32)).coerceAtMost(700)
        val h = (w * 1.5f).toInt()
        val pending = ArrayList<Pair<ImageView, Ayah>>()
        for (a in favs) {
            val iv = ImageView(this)
            iv.adjustViewBounds = true
            iv.scaleType = ImageView.ScaleType.FIT_CENTER
            iv.minimumHeight = h
            iv.setBackgroundColor(Theme.col("card"))
            Ui.round(iv, 18)
            iv.setOnClickListener {
                startActivity(Intent(this, ZoomActivity::class.java).putExtra("ayah", a.toJson().toString()))
            }
            list.addView(iv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            pending.add(Pair(iv, a))

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(0, dp(6), 0, dp(18))
            val wall = Ui.primary(this, t("b_wall")) { WallpaperSetter.choose(this, a) }
            val rm = Ui.outline(this, t("fav_remove")) {
                AyahSource.toggleFav(this, a)
                render()
            }
            val lp1 = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp1.setMargins(dp(4), 0, dp(4), 0)
            val lp2 = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp2.setMargins(dp(4), 0, dp(4), 0)
            row.addView(wall, lp1)
            row.addView(rm, lp2)
            list.addView(row)
        }
        // Cards are drawn one after another in the background so the page opens fast
        Thread {
            for ((v, a) in pending) {
                val bmp = WallpaperPainter.paint(this, a, w, h, 0, true)
                runOnUiThread { v.setImageBitmap(bmp) }
            }
        }.start()
    }
}
