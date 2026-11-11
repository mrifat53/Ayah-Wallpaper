package com.rifat.ayahwall

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class KhatamActivity : Activity() {

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

        val title = TextView(this)
        title.text = t("khatam_title")
        title.textSize = 18f
        title.setTextColor(Color.WHITE)
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(dp(16), dp(14), dp(16), dp(14))
        outer.addView(title)

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

    private fun tv(text: String, size: Float, color: String, bold: Boolean): TextView {
        val v = TextView(this)
        v.text = text
        v.textSize = size
        v.setTextColor(Color.parseColor(color))
        v.setPadding(0, dp(8), 0, dp(8))
        if (bold) v.setTypeface(null, Typeface.BOLD)
        return v
    }

    private fun btn(text: String, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.setOnClickListener { onClick() }
        return b
    }

    private fun render() {
        list.removeAllViews()
        val sp = Prefs.sp(this)
        val start = sp.getString("khatam_start", null)

        if (start == null) {
            list.addView(tv(t("khatam_none"), 15f, "#BBBBBB", false))
            list.addView(btn(t("khatam_start")) {
                sp.edit()
                    .putString("khatam_start", LocalDate.now().toString())
                    .remove("khatam_done")
                    .apply()
                render()
            })
            return
        }

        val dayNo = (ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.now()) + 1).coerceIn(1L, 30L).toInt()
        val done: Set<String> = sp.getStringSet("khatam_done", emptySet()) ?: emptySet()

        list.addView(tv("${t("khatam_day")} ${I18n.digits(dayNo.toString(), lang)} / 30 · ${t("khatam_done_n")} ${I18n.digits(done.size.toString(), lang)}/30", 15f, "#FFFFFF", true))
        if (done.size >= 30) list.addView(tv(t("khatam_finished"), 15f, "#8FD3B0", true))

        val juzText = "${t("khatam_today")}: ${t("juz_word")} ${I18n.digits(dayNo.toString(), lang)}"
        list.addView(tv(juzText, 16f, "#D9B65B", true))

        val isDone = done.contains(dayNo.toString())
        list.addView(btn(if (isDone) "✓ " + t("khatam_mark") else t("khatam_mark")) {
            val nd = HashSet(done)
            if (isDone) nd.remove(dayNo.toString()) else nd.add(dayNo.toString())
            sp.edit().putStringSet("khatam_done", nd).apply()
            render()
        })

        // 30 juz in a grid, ticked when done
        for (row in 0 until 6) {
            val line = LinearLayout(this)
            line.orientation = LinearLayout.HORIZONTAL
            for (col in 0 until 5) {
                val n = row * 5 + col + 1
                val cell = TextView(this)
                val mark = if (done.contains(n.toString())) "✓ " else ""
                cell.text = mark + I18n.digits(n.toString(), lang)
                cell.gravity = Gravity.CENTER
                cell.textSize = 14f
                cell.setPadding(0, dp(10), 0, dp(10))
                cell.setTextColor(
                    when {
                        n == dayNo -> Color.parseColor("#0B0B0B")
                        done.contains(n.toString()) -> Color.parseColor("#8FD3B0")
                        else -> Color.parseColor("#AAAAAA")
                    }
                )
                if (n == dayNo) cell.setBackgroundColor(Color.parseColor("#D9B65B"))
                line.addView(cell, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }
            list.addView(line)
        }

        list.addView(btn(t("khatam_restart")) {
            sp.edit().remove("khatam_start").remove("khatam_done").apply()
            render()
        })
    }
}
