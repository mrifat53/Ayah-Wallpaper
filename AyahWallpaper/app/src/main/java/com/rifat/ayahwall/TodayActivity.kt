package com.rifat.ayahwall

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.io.File
import java.io.FileOutputStream

class TodayActivity : Activity() {

    private var lang = "en"
    private lateinit var content: LinearLayout

    private fun t(key: String): String = I18n.t(key, lang)

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)
        MobileAds.initialize(this) {}

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.setBackgroundColor(Color.parseColor("#0B0B0B"))
        outer.fitsSystemWindows = true
        outer.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        val scroll = ScrollView(this)
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(20), dp(20), dp(20), dp(20))
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // Test banner. Replace with your real AdMob unit id before publishing.
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.loadAd(AdRequest.Builder().build())
        outer.addView(
            adView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )

        setContentView(outer)
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

    private fun showAyah(a: Ayah) {
        val langs = Prefs.langs(this)
        var shown = false
        fun part(code: String, text: String, size: Float, color: String) {
            if (code !in langs || text.isBlank()) return
            val v = tv(text, size, color, false)
            v.gravity = Gravity.CENTER
            if (code == "ar") v.textDirection = View.TEXT_DIRECTION_RTL
            content.addView(v)
            val r = tv(I18n.ref(code, a.surah, a.ayah), 14f, "#D9B65B", false)
            r.gravity = Gravity.CENTER
            content.addView(r)
            shown = true
        }
        part("ar", a.arabic, 28f, "#FFFFFF")
        part("bn", a.bangla, 18f, "#EDEDED")
        part("en", a.english, 16f, "#D4D4D4")
        if (!shown) {
            val v = tv(a.arabic, 28f, "#FFFFFF", false)
            v.gravity = Gravity.CENTER
            v.textDirection = View.TEXT_DIRECTION_RTL
            content.addView(v)
        }
    }

    private fun section(title: String, list: List<Ayah>) {
        content.addView(tv(title, 18f, "#FFFFFF", true))
        if (list.isEmpty()) {
            content.addView(tv(t("empty"), 14f, "#888888", false))
            return
        }
        val langs = Prefs.langs(this)
        for (a in list) {
            val first = when {
                "ar" in langs -> a.arabic
                "bn" in langs -> a.bangla
                else -> a.english
            }
            val snippet = if (first.length > 90) first.substring(0, 90) + "…" else first
            val item = tv(I18n.ref(lang, a.surah, a.ayah) + "\n" + snippet, 14f, "#DDDDDD", false)
            item.setOnClickListener {
                AyahSource.setCurrentFirst(this, a)
                render()
            }
            content.addView(item)
        }
    }

    private fun render() {
        content.removeAllViews()
        content.addView(tv(t("today"), 24f, "#FFFFFF", true))
        val a = AyahSource.current(this)
        if (a == null) {
            content.addView(tv(t("empty"), 16f, "#BBBBBB", false))
        } else {
            showAyah(a)
        }

        content.addView(btn(t("new_ayah")) { fetchNew() })
        if (a != null) {
            val fav = AyahSource.isFav(this, a)
            content.addView(btn(if (fav) t("fav_remove") else t("fav_add")) {
                AyahSource.toggleFav(this, a)
                render()
            })
            content.addView(btn(t("share")) { share(a) })
        }
        content.addView(btn(t("about")) { showAbout() })

        section(t("favorites"), AyahSource.favorites(this))
        section(t("recent"), AyahSource.history(this).take(10))
    }

    private fun fetchNew() {
        Toast.makeText(this, t("loading"), Toast.LENGTH_SHORT).show()
        Thread {
            val a = AyahSource.next(this)
            runOnUiThread {
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
