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
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast

// Settings. Each topic is its own card, in the same order every time.
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)
        val sp = Prefs.sp(this)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(8), dp(16), dp(16))
        root.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        root.addView(Ui.screenTitle(this, t("title")))

        // App language
        val langRow = LinearLayout(this)
        langRow.orientation = LinearLayout.HORIZONTAL
        for ((code, name) in listOf("bn" to "বাংলা", "en" to "English", "ar" to "العربية")) {
            val b = Ui.outline(this, name) {
                sp.edit().putString("ui_lang", code).apply()
                recreate()
            }
            if (code == lang) b.alpha = 1f else b.alpha = 0.55f
            langRow.addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(Ui.card(this, listOf(Ui.sectionHead(this, R.drawable.ic_translate, t("ui_lang")), langRow)))

        // Ayah on/off
        val on = Switch(this)
        on.text = t("enable")
        on.setTextColor(Color.WHITE)
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
        root.addView(Ui.card(this, listOf(on)))

        // Which languages appear on the lock screen
        val langBox = LinearLayout(this)
        langBox.orientation = LinearLayout.VERTICAL
        for ((code, name) in listOf("ar" to t("arabic"), "bn" to t("bangla"), "en" to t("english"))) {
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
            langBox.addView(cb)
        }
        root.addView(Ui.card(this, listOf(Ui.sectionHead(this, R.drawable.ic_book, t("content_lang")), langBox)))

        // Which ayahs: category and its choice
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
        val catSpin = spinner(catNames, cats.indexOf(Prefs.category(this)).coerceAtLeast(0)) { pos ->
            val c = cats[pos]
            if (c != Prefs.category(this)) {
                sp.edit().putString("cat", c).putInt("cat_param", if (c == "theme") 0 else 1).apply()
                buildParam()
            }
        }
        buildParam()
        root.addView(Ui.card(this, listOf(Ui.sectionHead(this, R.drawable.ic_grid, t("category")), catSpin, paramHolder)))

        // Look: background style, text position, apply now
        val styleNames = listOf(
            t("style_random"), t("style_night"), t("style_pattern"),
            t("style_kaaba"), t("style_dome"), t("style_mihrab"), t("style_aurora")
        )
        val styleSpin = spinner(styleNames, Prefs.style(this).coerceIn(0, 6)) { pos ->
            sp.edit().putInt("style", pos).apply()
        }
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
        val apply = Ui.primary(this, t("apply")) {
            WallpaperWorker.runNow(this)
            Toast.makeText(this, t("working"), Toast.LENGTH_SHORT).show()
        }
        root.addView(Ui.card(this, listOf(
            Ui.sectionHead(this, R.drawable.ic_palette, t("style")), styleSpin,
            Ui.sectionHead(this, R.drawable.ic_text, t("position")), bar,
            apply
        )))

        // Prayer
        val remind = Switch(this)
        remind.text = t("pt_remind")
        remind.setTextColor(Color.WHITE)
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
        val prayerItems = mutableListOf<View>(Ui.sectionHead(this, R.drawable.ic_moon, t("pt_title")), remind)
        if (!PrayerTimes.hasPermission(this)) {
            prayerItems.add(Ui.outline(this, t("pt_give")) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 77)
            })
        }
        root.addView(Ui.card(this, prayerItems))

        // More pages
        val favBtn = Ui.outline(this, t("favs_open")) { startActivity(Intent(this, FavoritesActivity::class.java)) }
        val khBtn = Ui.outline(this, t("khatam")) { startActivity(Intent(this, KhatamActivity::class.java)) }
        val batBtn = Ui.outline(this, t("battery_btn")) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        root.addView(Ui.card(this, listOf(
            Ui.sectionHead(this, R.drawable.ic_info, t("about")),
            favBtn, khBtn, batBtn,
            Ui.body(this, t("battery_note"), 13f, "#9FB3A9")
        )))

        root.addView(Ui.card(this, listOf(Ui.body(this, t("credits"), 12f, "#7F9A8F"))))

        // Ad at the top, away from the bottom bar
        root.addView(Ads.banner(this), 1)

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.fitsSystemWindows = true
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#0E1512"))
        scroll.addView(root)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        outer.addView(BottomNav.build(this, "profile"))
        setContentView(outer)
    }
}
