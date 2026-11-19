package com.rifat.ayahwall

import android.view.ViewGroup
import android.graphics.Shader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.io.File
import android.Manifest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.io.FileOutputStream

class TodayActivity : Activity() {

    private var lang = "en"
    private lateinit var content: LinearLayout
    private lateinit var preview: ImageView
    private var previewKey = ""
    private var pickS = 1
    private var pickA = 1
    private val ui = Handler(Looper.getMainLooper())

    private fun t(key: String): String = I18n.t(key, lang)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.refresh(this)
        lang = Prefs.uiLang(this)
        MobileAds.initialize(this) {}

        val sp0 = Prefs.sp(this)
        if (!sp0.contains("enabled")) sp0.edit().putBoolean("enabled", true).apply()
        if (Prefs.enabled(this)) {
            WallpaperWorker.ensure(this)
            if (!sp0.getBoolean("first_run_done", false)) {
                sp0.edit().putBoolean("first_run_done", true).apply()
                WallpaperWorker.runNow(this)
            }
        }
        Thread { AyahSource.counts(this) }.start()
        AyahSource.current(this)?.let {
            pickS = it.surah
            pickA = it.ayah
        }

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.setBackgroundColor(Theme.col("bg"))
        outer.fitsSystemWindows = true
        outer.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        // Top bar with the three-dot menu
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER_VERTICAL
        bar.setPadding(dp(16), dp(6), dp(6), dp(6))
        val title = TextView(this)
        title.text = t("title")
        title.textSize = 18f
        title.setTextColor(Theme.col("text"))
        title.setTypeface(null, Typeface.BOLD)
        bar.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        outer.addView(bar)


        val scroll = ScrollView(this)
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(16), 0, dp(16), dp(16))
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))


        preview = ImageView(this)
        preview.adjustViewBounds = true
        preview.scaleType = ImageView.ScaleType.FIT_CENTER

        outer.addView(BottomNav.build(this, "home"))
        Ui.fadeIn(outer)
        setContentView(outer)
        Theme.applyBars(this)
        render()
        if (AyahSource.current(this) == null) fetchNew()
    }

    override fun onResume() {
        super.onResume()
        val newLang = Prefs.uiLang(this)
        if (newLang != lang) {
            lang = newLang
            recreate()
            return
        }
        render()
    }

    private fun showMenu(anchor: View) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add(0, 1, 0, t("menu_settings"))
        pm.menu.add(0, 2, 1, t("about"))
        pm.menu.add(0, 3, 2, t("battery_btn"))
        pm.menu.add(0, 4, 3, t("menu_feed"))
        pm.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> startActivity(Intent(this, MainActivity::class.java))
                2 -> showAbout()
                3 -> startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                4 -> startActivity(Intent(this, FeedActivity::class.java))
            }
            true
        }
        pm.show()
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

    private fun renderPreview(key: String) {
        val a = AyahSource.current(this) ?: return
        val dm = resources.displayMetrics
        val w = dm.widthPixels / 2
        val h = dm.heightPixels / 2
        val y = Prefs.yPercent(this)
        Thread {
            val bmp = WallpaperPainter.paint(this, a, w, h, y, false)
            ui.post {
                if (key == previewKey) preview.setImageBitmap(bmp)
            }
        }.start()
    }

    private var heroBg: Bitmap? = null
    private var adSlot: View? = null

    private fun heroCard(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(20), dp(18), dp(20), dp(18))
        val w = resources.displayMetrics.widthPixels - dp(32)
        val h = dp(420)
        if (heroBg == null || heroBg!!.width != w) {
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val cv = Canvas(bmp)
            BackgroundGenerator.draw(cv, w, h, 16, 42L)
            // Darken the picture so the text on top is always easy to read
            val shade = Paint()
            shade.shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.argb(190, 0, 0, 0), Color.argb(110, 0, 0, 0), Color.argb(175, 0, 0, 0)),
                floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
            )
            cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), shade)
            heroBg = bmp
        }
        box.background = BitmapDrawable(resources, heroBg)
        box.addView(prayerCard())
        Ui.round(box, 18)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, dp(14))
        box.layoutParams = lp
        return box
    }

    private fun progressBox(): LinearLayout {
        val sp = Prefs.sp(this)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val n = PrayerTimes.order.count { sp.getBoolean("done_" + today + "_" + it, false) }
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(Ui.heading(this, t("pr_today")))
        box.addView(tv(I18n.digits("$n/5", lang), 26f, Theme.hex("text"), true))
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        for (i in 0 until 5) {
            val seg = View(this)
            seg.setBackgroundColor(if (i < n) Theme.col("accent") else Theme.col("border"))
            val lp = LinearLayout.LayoutParams(0, dp(8), 1f)
            lp.setMargins(dp(2), dp(6), dp(2), 0)
            bar.addView(seg, lp)
        }
        box.addView(bar)
        return box
    }

    private fun quickBox(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(Ui.item(this, R.drawable.ic_book, t("nav_feed")) {
            startActivity(Intent(this, FeedActivity::class.java))
        })
        box.addView(Ui.item(this, R.drawable.ic_star, t("favs_open")) {
            startActivity(Intent(this, FavoritesActivity::class.java))
        })
        box.addView(Ui.item(this, R.drawable.ic_bookmark, t("khatam")) {
            startActivity(Intent(this, KhatamActivity::class.java))
        })
        return box
    }

    // Home: prayer times with a greeting, today's progress, quick links, one tidy ad
    private fun render() {
        content.removeAllViews()
        content.addView(heroCard())
        content.addView(Ui.card(this, listOf(progressBox())))
        content.addView(Ui.card(this, listOf(quickBox())))
        val slot = adSlot ?: Ads.slot(this).also { adSlot = it }
        (slot.parent as? ViewGroup)?.removeView(slot)
        content.addView(slot)
    }

    // A titled list of ayahs, tap one to show it on the home screen
    private fun sectionBox(title: String, list: List<Ayah>, emptyKey: String): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(Ui.heading(this, title))
        if (list.isEmpty()) {
            box.addView(Ui.body(this, t(emptyKey), 14f, Theme.hex("muted")))
            return box
        }
        val langs = Prefs.langs(this)
        for (a in list) {
            val first = when {
                "ar" in langs -> a.arabic
                "bn" in langs -> a.bangla
                else -> a.english
            }
            val snippet = if (first.length > 90) first.substring(0, 90) + "…" else first
            val item = Ui.body(this, I18n.ref(lang, a.surah, a.ayah) + "\n" + snippet, 14f, Theme.hex("text"))
            item.setOnClickListener {
                AyahSource.setCurrentFirst(this, a)
                render()
            }
            box.addView(item)
        }
        return box
    }

    private fun loadPrayer(day: String): List<Pair<String, String>>? {
        val sp = Prefs.sp(this)
        if (sp.getString("pt_day", "") != day) return null
        val s = sp.getString("pt_json", null) ?: return null
        return try {
            s.split(";").map {
                val p = it.split("=")
                Pair(p[0], p[1])
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun savePrayer(day: String, times: List<Pair<String, String>>) {
        Prefs.sp(this).edit()
            .putString("pt_day", day)
            .putString("pt_json", times.joinToString(";") { it.first + "=" + it.second })
            .apply()
    }

    private fun refreshPrayer(day: String) {
        Thread {
            val loc = PrayerTimes.lastLocation(this)
            if (loc != null) {
                val times = PrayerTimes.fetch(loc.latitude, loc.longitude)
                if (times != null) {
                    savePrayer(day, times)
                    PrayerAlarm.scheduleNext(this)
                    ui.post { render() }
                }
            }
        }.start()
    }

    private fun prayerCard(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val times = loadPrayer(today)

        if (times == null) {
            box.addView(tv("السلام عليكم", 18f, "#E8C36A", true))
            if (!PrayerTimes.hasPermission(this)) {
                box.addView(tv(t("pt_need_loc"), 14f, "#FFFFFF", false))
                box.addView(btn(t("pt_give")) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 77)
                })
            } else {
                box.addView(tv(t("loading"), 14f, "#FFFFFF", false))
                refreshPrayer(today)
            }
            return box
        }

        PrayerAlarm.scheduleNext(this)
        val sp = Prefs.sp(this)
        val idx = PrayerTimes.nextIndex(times, Calendar.getInstance())
        val doneFlags = times.map { sp.getBoolean("done_" + today + "_" + it.first, false) }

        box.addView(tv("السلام عليكم", 18f, "#E8C36A", true))
        val dateLine = SimpleDateFormat("EEEE, d MMM", Locale.ENGLISH).format(Date())
        box.addView(tv(dateLine, 12f, "#F3E6C8", false))
        box.addView(tv(t("pt_title"), 14f, "#F3E6C8", false))
        box.addView(tv(t("pt_" + times[idx].first) + " · " + I18n.digits(times[idx].second, lang), 32f, "#FFFFFF", true))

        val arc = PrayerArcView(
            this,
            times.map { t("pt_" + it.first) },
            times.map { I18n.digits(it.second, lang) },
            idx,
            doneFlags
        ) { i ->
            val flag = "done_" + today + "_" + times[i].first
            Prefs.sp(this).edit().putBoolean(flag, !doneFlags[i]).apply()
            render()
        }
        box.addView(arc, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(180)))
        box.addView(tv(t("pt_tap_hint"), 11f, "#D9D0BD", false))
        return box
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 77) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            refreshPrayer(today)
            render()
        }
    }

    // Pick any surah and ayah (for example for prayer) and show it here
    private fun picker(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(Ui.heading(this, t("pick_title")))

        val surahNames = (1..114).map { "${I18n.digits(it.toString(), lang)}. ${I18n.surahName(lang, it)}" }
        val surahSpin = Spinner(this)
        surahSpin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, surahNames)
        surahSpin.setSelection(pickS - 1, false)
        val ayahSpin = Spinner(this)

        fun fillAyahs() {
            val n = AyahSource.countsCached(this)?.get(pickS - 1) ?: 1
            val items = (1..n).map { I18n.digits(it.toString(), lang) }
            ayahSpin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
            pickA = pickA.coerceIn(1, n)
            ayahSpin.setSelection(pickA - 1, false)
        }
        fillAyahs()

        surahSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos + 1 != pickS) {
                    pickS = pos + 1
                    pickA = 1
                    fillAyahs()
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        ayahSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                pickA = pos + 1
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        box.addView(surahSpin)
        box.addView(ayahSpin)

        box.addView(btn(t("pick_show")) {
            Thread {
                val ay = AyahSource.ordered(this, pickS, pickA)
                ui.post {
                    if (ay == null) {
                        Toast.makeText(this, t("no_internet"), Toast.LENGTH_SHORT).show()
                    } else {
                        AyahSource.setCurrentFirst(this, ay)
                        render()
                    }
                }
            }.start()
        })
        return box
    }

    private fun fetchNew() {
        Toast.makeText(this, t("loading"), Toast.LENGTH_SHORT).show()
        Thread {
            val a = AyahSource.next(this)
            AyahWidget.refreshAll(this)
            ui.post {
                if (a == null) Toast.makeText(this, t("no_internet"), Toast.LENGTH_SHORT).show()
                render()
            }
        }.start()
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(t("about"))
            .setMessage(t("about_text") + "\n\n" + t("credits"))
            .setPositiveButton(t("close"), null)
            .show()
    }

    private fun share(a: Ayah) {
        try {
            val bmp = WallpaperPainter.paint(this, a, 1080, 1350, 0, true)
            val dir = File(cacheDir, "share")
            dir.mkdirs()
            val f = File(dir, "ayah.png")
            FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "image/png"
            i.putExtra(Intent.EXTRA_STREAM, uri)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(i, t("share")))
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "error", Toast.LENGTH_SHORT).show()
        }
    }
}
