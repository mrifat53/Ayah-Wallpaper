package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast

// Settings as dropdown groups. Tap a group header to open or close it.
class MainActivity : Activity() {

    private var lang = "en"
    private fun t(key: String): String = I18n.t(key, lang)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

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

    private fun hint(text: String): android.widget.TextView = Ui.body(this, text, 13f, "#8B949E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)
        val sp = Prefs.sp(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(8), dp(16), dp(16))
        root.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        root.addView(Ui.screenTitle(this, t("title")))

        // ---- Ayah settings (open by default)
        val ayahBody = LinearLayout(this)
        ayahBody.orientation = LinearLayout.VERTICAL
        val on = Switch(this)
        on.text = t("enable")
        on.setTextColor(Color.parseColor("#E6EDF3"))
        on.isChecked = Prefs.enabled(this)
        on.setOnCheckedChangeListener { _, checked ->
            sp.edit().putBoolean("enabled", checked).apply()
            if (checked) {
                WallpaperWorker.schedule(this)
                WallpaperWorker.runNow(this)
                Toast.makeText(this, t("enabled_toast"), Toast.LENGTH_SHORT).show()
            } else {
                WallpaperWorker.cancel(this)
            }
        }
        ayahBody.addView(on)

        ayahBody.addView(Ui.heading(this, t("content_lang")))
        for ((code, name) in listOf("ar" to t("arabic"), "bn" to t("bangla"), "en" to t("english"))) {
            val cb = CheckBox(this)
            cb.text = name
            cb.setTextColor(Color.parseColor("#E6EDF3"))
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
            ayahBody.addView(cb)
        }

        ayahBody.addView(Ui.heading(this, t("category")))
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
            paramHolder.addView(spinner(items, sel.coerceIn(0, items.size - 1)) { pos ->
                sp.edit().putInt("cat_param", if (cat == "theme") pos else pos + 1).apply()
            })
        }
        ayahBody.addView(spinner(catNames, cats.indexOf(Prefs.category(this)).coerceAtLeast(0)) { pos ->
            val c = cats[pos]
            if (c != Prefs.category(this)) {
                sp.edit().putString("cat", c).putInt("cat_param", if (c == "theme") 0 else 1).apply()
                buildParam()
            }
        })
        buildParam()
        ayahBody.addView(paramHolder)

        ayahBody.addView(Ui.heading(this, t("style")))
        val styleNames = listOf(
            t("style_random"), t("style_night"), t("style_pattern"),
            t("style_kaaba"), t("style_dome"), t("style_mihrab"), t("style_aurora")
        )
        ayahBody.addView(spinner(styleNames, Prefs.style(this).coerceIn(0, 6)) { pos ->
            sp.edit().putInt("style", pos).apply()
        })

        ayahBody.addView(Ui.heading(this, t("position")))
        val bar = SeekBar(this)
        bar.max = 55
        bar.progress = (Prefs.yPercent(this) - 15).coerceIn(0, 55)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                val v = (s?.progress ?: 17) + 15
                sp.edit().putInt("y_percent", v).apply()
            }
        })
        ayahBody.addView(bar)
        ayahBody.addView(Ui.primary(this, t("apply")) {
            WallpaperWorker.runNow(this)
            Toast.makeText(this, t("working"), Toast.LENGTH_SHORT).show()
        })
        root.addView(Ui.dropGroup(this, R.drawable.ic_play, t("g_ayah"), ayahBody, true))

        // ---- Prayer
        val prayerBody = LinearLayout(this)
        prayerBody.orientation = LinearLayout.VERTICAL
        val remind = Switch(this)
        remind.text = t("pt_remind")
        remind.setTextColor(Color.parseColor("#E6EDF3"))
        remind.isChecked = sp.getBoolean("pt_remind", false)
        remind.setOnCheckedChangeListener { _, checked ->
            sp.edit().putBoolean("pt_remind", checked).apply()
            if (checked) {
                PrayerAlarm.ensureChannel(this)
                if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 78)
                }
                PrayerAlarm.scheduleNext(this)
            } else {
                PrayerAlarm.cancel(this)
            }
        }
        prayerBody.addView(remind)
        if (!PrayerTimes.hasPermission(this)) {
            prayerBody.addView(Ui.outline(this, t("pt_give")) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 77)
            })
        }
        root.addView(Ui.dropGroup(this, R.drawable.ic_moon, t("g_prayer"), prayerBody, false))

        // ---- App language
        val langBody = LinearLayout(this)
        langBody.orientation = LinearLayout.VERTICAL
        for ((code, name) in listOf("bn" to "বাংলা", "en" to "English", "ar" to "العربية")) {
            langBody.addView(Ui.item(this, R.drawable.ic_translate,
                name + if (code == lang) "  ✓" else "") {
                sp.edit().putString("ui_lang", code).apply()
                recreate()
            })
        }
        root.addView(Ui.dropGroup(this, R.drawable.ic_translate, t("g_lang"), langBody, false))

        // ---- Saved and plan
        val savedBody = LinearLayout(this)
        savedBody.orientation = LinearLayout.VERTICAL
        savedBody.addView(Ui.item(this, R.drawable.ic_star, t("favs_open")) {
            startActivity(Intent(this, FavoritesActivity::class.java))
        })
        savedBody.addView(Ui.item(this, R.drawable.ic_bookmark, t("khatam")) {
            startActivity(Intent(this, KhatamActivity::class.java))
        })
        root.addView(Ui.dropGroup(this, R.drawable.ic_star, t("g_saved"), savedBody, false))

        // ---- Battery
        val batBody = LinearLayout(this)
        batBody.orientation = LinearLayout.VERTICAL
        batBody.addView(Ui.item(this, R.drawable.ic_battery, t("battery_btn")) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        })
        batBody.addView(hint(t("battery_note")))
        root.addView(Ui.dropGroup(this, R.drawable.ic_battery, t("g_battery"), batBody, false))

        // ---- About
        val aboutBody = LinearLayout(this)
        aboutBody.orientation = LinearLayout.VERTICAL
        aboutBody.addView(hint(t("credits")))
        root.addView(Ui.dropGroup(this, R.drawable.ic_info, t("g_about"), aboutBody, false))

        // Ad at the top, away from the bottom bar
        root.addView(Ads.banner(this), 1)

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.fitsSystemWindows = true
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#0D1117"))
        scroll.addView(root)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        outer.addView(BottomNav.build(this, "profile"))
        setContentView(outer)
    }
}
