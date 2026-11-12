package com.rifat.ayahwall

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.io.File
import java.io.FileOutputStream

// Reads the Quran in order, surah by surah. Remembers where you reached.
class FeedActivity : Activity() {

    private companion object {
        const val BATCH = 5
    }

    private var lang = "en"
    private val ui = Handler(Looper.getMainLooper())
    private lateinit var list: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var status: TextView
    private var loading = false
    private var finished = false
    private var nextS = 1
    private var nextA = 1
    private var cards = 0

    private fun t(k: String): String = I18n.t(k, lang)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = Prefs.uiLang(this)
        val sp = Prefs.sp(this)
        nextS = sp.getInt("feed_s", 1)
        nextA = sp.getInt("feed_a", 1)

        val outer = LinearLayout(this)
        outer.orientation = LinearLayout.VERTICAL
        outer.setBackgroundColor(Color.parseColor("#121314"))
        outer.fitsSystemWindows = true
        outer.layoutDirection = if (lang == "ar") View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

        val title = Ui.screenTitle(this, t("feed"))
        outer.addView(title)

        scroll = ScrollView(this)
        list = LinearLayout(this)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(dp(16), 0, dp(16), dp(16))
        scroll.addView(list)
        outer.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        status = TextView(this)
        status.setTextColor(Color.parseColor("#B0B3B8"))
        status.gravity = Gravity.CENTER
        status.setPadding(0, dp(10), 0, dp(10))
        outer.addView(status)

        // When the reader is near the bottom, load the next few ayahs
        scroll.setOnScrollChangeListener { _, _, y, _, _ ->
            val child = scroll.getChildAt(0)
            if (child != null && child.height - (scroll.height + y) < dp(900)) loadMore()
        }

        outer.addView(BottomNav.build(this, "feed"))
        setContentView(outer)
        loadMore()
    }

    private fun loadMore() {
        if (loading || finished) return
        loading = true
        status.text = t("loading")

        Thread {
            val batch = ArrayList<Pair<Ayah, Bitmap>>()
            var s = nextS
            var a = nextA
            var ended = false
            var failed = false

            val counts = AyahSource.counts(this)
            if (counts == null) {
                failed = true
            } else {
                val dm = resources.displayMetrics
                val cardW = (dm.widthPixels * 0.8f).toInt()
                val cardH = (cardW * 1.5f).toInt()
                for (i in 0 until BATCH) {
                    val ay = AyahSource.ordered(this, s, a)
                    if (ay == null) {
                        failed = true
                        break
                    }
                    batch.add(Pair(ay, WallpaperPainter.paint(this, ay, cardW, cardH, Prefs.yPercent(this), true)))
                    if (a < counts[s - 1]) {
                        a += 1
                    } else if (s < 114) {
                        s += 1
                        a = 1
                    } else {
                        ended = true
                        break
                    }
                }
            }

            ui.post {
                for ((ay, bmp) in batch) addCard(ay, bmp)
                if (batch.isNotEmpty()) {
                    nextS = s
                    nextA = a
                    Prefs.sp(this).edit().putInt("feed_s", s).putInt("feed_a", a).apply()
                }
                if (ended) finished = true
                status.text = when {
                    ended -> t("feed_end")
                    failed -> t("no_internet")
                    else -> ""
                }
                loading = false
            }
        }.start()
    }

    private fun addCard(ay: Ayah, bmp: Bitmap) {
        cards++
        val iv = ImageView(this)
        iv.setImageBitmap(bmp)
        iv.adjustViewBounds = true
        iv.scaleType = ImageView.ScaleType.FIT_CENTER
        list.addView(iv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, dp(6), 0, dp(18))

        val fav = Button(this)
        fav.text = favText(ay)
        fav.setOnClickListener {
            AyahSource.toggleFav(this, ay)
            fav.text = favText(ay)
        }
        row.addView(fav, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val share = Button(this)
        share.text = t("share")
        share.setOnClickListener { shareBitmap(bmp) }
        row.addView(share, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val lock = Button(this)
        lock.text = t("lock_set")
        lock.setOnClickListener {
            WallpaperSetter.setLockAsync(this, ay) { ok ->
                Toast.makeText(this, t(if (ok) "lock_done" else "lock_fail"), Toast.LENGTH_SHORT).show()
            }
        }
        row.addView(lock, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        list.addView(row)

        // One small banner after every few ayahs, never inside the text itself
        if (cards % 5 == 0) addBanner()
    }

    private fun favText(ay: Ayah): String =
        if (AyahSource.isFav(this, ay)) t("fav_remove") else t("fav_add")

    private fun addBanner() {
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.loadAd(AdRequest.Builder().build())
        list.addView(adView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun shareBitmap(bmp: Bitmap) {
        try {
            val dir = File(cacheDir, "share")
            dir.mkdirs()
            val f = File(dir, "ayah_feed.png")
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
