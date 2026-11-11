package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var lang = "en"

    private fun t(key: String): String = I18n.t(key, lang)

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun label(msg: String, size: Float, color: String): TextView {
        val tv = TextView(this)
        tv.text = msg
        tv.textSize = size
        tv.setTextColor(Color.parseColor(color))
        tv.setPadding(0, dp(18), 0, dp(6))
        return tv
    }

    private fun spinner(items: List<String>, selected: Int, onSel: (Int) -> Unit): Spinner {
        val sp = Spinner(this)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        sp.setSelection(selected, false)
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onSel(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        return sp
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)
        val pad = dp(20)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad, pad, dp(60))
        root.setBackgroundColor(Color.parseColor("#0B0B0B"))
        root.fitsSystemWindows = true
        root.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        root.addView(label(t("title"), 26f, "#FFFFFF"))
        root.addView(label(t("subtitle"), 15f, "#BBBBBB"))
        root.addView(label(t("intro"), 13f, "#8FD3B0"))

        // App language
        root.addView(Ads.banner(this))
        root.addView(label(t("ui_lang"), 15f, "#FFFFFF"))
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val options = listOf("bn" to "বাংলা", "en" to "English", "ar" to "العربية")
        for ((code, name) in options) {
            val b = Button(this)
            b.text = name
            b.alpha = if (code == lang) 1f else 0.5f
            b.setOnClickListener {
                Prefs.sp(this).edit().putString("ui_lang", code).apply()
                recreate()
            }
            row.addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(row)

        // On / off
        val sw = Switch(this)
        sw.text = t("enable")
        sw.setTextColor(Color.WHITE)
        sw.setPadding(0, dp(14), 0, dp(14))
        sw.isChecked = Prefs.enabled(this)
        sw.setOnCheckedChangeListener { _, on ->
            Prefs.sp(this).edit().putBoolean("enabled", on).apply()
            if (on) {
                WallpaperWorker.schedule(this)
                WallpaperWorker.runNow(this)
                Toast.makeText(this, t("enabled_toast"), Toast.LENGTH_SHORT).show()
            } else {
                WallpaperWorker.cancel(this)
            }
        }
        root.addView(sw)

        // Ayah languages
        root.addView(label(t("content_lang"), 15f, "#FFFFFF"))
        val langBoxes = listOf("ar" to t("arabic"), "bn" to t("bangla"), "en" to t("english"))
        for ((code, name) in langBoxes) {
            val cb = CheckBox(this)
            cb.text = name
            cb.setTextColor(Color.WHITE)
            cb.isChecked = code in Prefs.langs(this)
            cb.setOnCheckedChangeListener { btn, checked ->
                val cur = Prefs.langs(this).toMutableSet()
                if (checked) cur.add(code) else cur.remove(code)
                if (cur.isEmpty()) {
                    btn.isChecked = true
                    Toast.makeText(this, t("keep_one"), Toast.LENGTH_SHORT).show()
                } else {
                    Prefs.setLangs(this, cur)
                }
            }
            root.addView(cb)
        }

        // Category
        root.addView(label(t("category"), 15f, "#FFFFFF"))
        val cats = listOf("all", "surah", "juz", "theme")
        val catNames = listOf(t("cat_all"), t("cat_surah"), t("cat_juz"), t("cat_theme"))
        val paramHolder = LinearLayout(this)
        paramHolder.orientation = LinearLayout.VERTICAL

        fun buildParam() {
            paramHolder.removeAllViews()
            val cat = Prefs.category(this)
            val items: List<String> = when (cat) {
                "surah" -> (1..114).map { "${I18n.digits(it.toString(), lang)}. ${I18n.surahName(lang, it)}" }
                "juz" -> (1..30).map { "${t("juz_word")} ${I18n.digits(it.toString(), lang)}" }
                "theme" -> I18n.themeKeys.map { t("theme_$it") }
                else -> emptyList()
            }
            if (items.isEmpty()) return
            val sel = if (cat == "theme") Prefs.catParam(this) else Prefs.catParam(this) - 1
            paramHolder.addView(
                spinner(items, sel.coerceIn(0, items.size - 1)) { pos ->
                    Prefs.sp(this).edit().putInt("cat_param", if (cat == "theme") pos else pos + 1).apply()
                }
            )
        }

        root.addView(
            spinner(catNames, cats.indexOf(Prefs.category(this)).coerceAtLeast(0)) { pos ->
                val c = cats[pos]
                if (c != Prefs.category(this)) {
                    Prefs.sp(this).edit()
                        .putString("cat", c)
                        .putInt("cat_param", if (c == "theme") 0 else 1)
                        .apply()
                    buildParam()
                }
            }
        )
        root.addView(paramHolder)
        buildParam()
        root.addView(label(t("category_hint"), 12f, "#888888"))

        // Background style
        root.addView(label(t("style"), 15f, "#FFFFFF"))
        val styleNames = listOf(
            t("style_random"), t("style_night"), t("style_pattern"),
            t("style_kaaba"), t("style_dome"), t("style_mihrab"), t("style_aurora")
        )
        root.addView(
            spinner(styleNames, Prefs.style(this).coerceIn(0, 6)) { pos ->
                Prefs.sp(this).edit().putInt("style", pos).apply()
            }
        )

        // Text position
        root.addView(label(t("position"), 15f, "#FFFFFF"))
        val bar = SeekBar(this)
        bar.max = 55
        bar.progress = (Prefs.yPercent(this) - 15).coerceIn(0, 55)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                val v = (s?.progress ?: 17) + 15
                Prefs.sp(this@MainActivity).edit().putInt("y_percent", v).apply()
            }
        })
        root.addView(bar)

        // Buttons
        val now = Button(this)
        now.text = t("apply")
        now.setOnClickListener {
            WallpaperWorker.runNow(this)
            Toast.makeText(this, t("working"), Toast.LENGTH_SHORT).show()
        }
        root.addView(now)


        val favBtn = Button(this)
        favBtn.text = t("favs_open")
        favBtn.setOnClickListener { startActivity(Intent(this, FavoritesActivity::class.java)) }
        root.addView(favBtn)

        root.addView(label(t("battery_note"), 13f, "#BBBBBB"))
        val battery = Button(this)
        battery.text = t("battery_btn")
        battery.setOnClickListener {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        root.addView(battery)

        root.addView(label(t("credits"), 12f, "#888888"))

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#0B0B0B"))
        scroll.addView(root)
        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.fitsSystemWindows = true
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        outer.addView(BottomNav.build(this, "profile"))
        setContentView(outer)
    }
}
