package com.rifat.ayahwall

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
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
        outer.setBackgroundColor(Color.parseColor("#0E1512"))
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
        title.setTextColor(Color.WHITE)
        title.setTypeface(null, Typeface.BOLD)
        bar.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        outer.addView(bar)

        // Test banner. Replace with the real AdMob unit id before publishing.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.loadAd(AdRequest.Builder().build())
        outer.addView(
            adView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )

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
        setContentView(outer)
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

    private var showMore = false

    private fun render() {
        content.removeAllViews()
        val a = AyahSource.current(this)

        if (a != null) {
            val key = "${a.surah}:${a.ayah}|${Prefs.style(this)}|${Prefs.yPercent(this)}|${Prefs.langs(this).sorted().joinToString(",")}"
            if (key != previewKey) {
                previewKey = key
                renderPreview(key)
            }
            content.addView(Ui.card(this, listOf(preview)))

            val fav = AyahSource.isFav(this, a)
            val newBtn = Ui.primary(this, t("new_ayah")) { fetchNew() }
            val favBtn = Ui.outline(this, if (fav) t("fav_remove") else t("fav_add")) {
                AyahSource.toggleFav(this, a)
                render()
            }
            val shareBtn = Ui.outline(this, t("share")) { share(a) }
            val lockBtn = Ui.outline(this, t("lock_set")) {
                WallpaperSetter.setLockAsync(this, a) { ok ->
                    Toast.makeText(this, t(if (ok) "lock_done" else "lock_fail"), Toast.LENGTH_SHORT).show()
                }
            }
            val actions = LinearLayout(this)
            actions.orientation = LinearLayout.VERTICAL
            actions.addView(Ui.row(this, newBtn, favBtn))
            actions.addView(Ui.row(this, shareBtn, lockBtn))
            content.addView(Ui.card(this, listOf(actions)))
        } else {
            content.addView(Ui.card(this, listOf(
                tv(t("loading"), 16f, "#9FB3A9", false),
                Ui.primary(this, t("new_ayah")) { fetchNew() }
            )))
        }

        content.addView(Ui.card(this, listOf(prayerCard())))

        content.addView(Ui.outline(this, if (showMore) t("more_hide") else t("more_show")) {
            showMore = !showMore
            render()
        })
        if (showMore) {
            content.addView(Ui.card(this, listOf(picker())))
            content.addView(Ui.card(this, listOf(sectionBox(t("favorites"), AyahSource.favorites(this), "fav_empty"))))
            content.addView(Ui.card(this, listOf(sectionBox(t("recent"), AyahSource.history(this).take(10), "empty"))))
        }
    }

    // A titled list of ayahs, tap one to show it on the home screen
    private fun sectionBox(title: String, list: List<Ayah>, emptyKey: String): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.addView(Ui.heading(this, title))
        if (list.isEmpty()) {
            box.addView(Ui.body(this, t(emptyKey), 14f, "#7F9A8F"))
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
            val item = Ui.body(this, I18n.ref(lang, a.surah, a.ayah) + "\n" + snippet, 14f, "#F2F5F3")
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
            if (!PrayerTimes.hasPermission(this)) {
                box.addView(Ui.body(this, t("pt_need_loc"), 14f, "#9FB3A9"))
                box.addView(btn(t("pt_give")) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 77)
                })
            } else {
                box.addView(Ui.body(this, t("loading"), 14f, "#9FB3A9"))
                refreshPrayer(today)
            }
            return box
        }

        PrayerAlarm.scheduleNext(this)
        val sp = Prefs.sp(this)
        val idx = PrayerTimes.nextIndex(times, Calendar.getInstance())
        box.addView(Ui.heading(this, t("pt_title")))
        box.addView(tv(t("pt_" + times[idx].first) + " · " + I18n.digits(times[idx].second, lang), 18f, "#5FB894", true))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        for (i in times.indices) {
            val key = times[i].first
            val flag = "done_" + today + "_" + key
            val done = sp.getBoolean(flag, false)
            val c = TextView(this)
            c.text = (if (done) "✓ " else "") + t("pt_" + key) + "\n" + I18n.digits(times[i].second, lang)
            c.gravity = Gravity.CENTER
            c.textSize = 12f
            c.setPadding(0, dp(8), 0, dp(8))
            c.setTextColor(
                when {
                    done -> Color.parseColor("#5FB894")
                    i == idx -> Color.WHITE
                    else -> Color.parseColor("#9FB3A9")
                }
            )
            if (i == idx) c.setTypeface(null, Typeface.BOLD)
            c.setOnClickListener {
                Prefs.sp(this).edit().putBoolean(flag, !done).apply()
                render()
            }
            row.addView(c, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        box.addView(row)
        box.addView(Ui.body(this, t("pt_tap_hint"), 11f, "#7F9A8F"))
        box.addView(Ui.body(this, t("pt_note"), 11f, "#7F9A8F"))
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
