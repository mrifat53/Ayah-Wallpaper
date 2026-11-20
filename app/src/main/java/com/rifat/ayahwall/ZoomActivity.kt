package com.rifat.ayahwall

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

// Full-screen card viewer with pinch and double-tap zoom, save to gallery, share and wallpaper.
class ZoomActivity : Activity() {

    private var lang = "en"
    private var bitmap: Bitmap? = null
    private lateinit var ayah: Ayah

    private fun t(k: String): String = I18n.t(k, lang)
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Theme.refresh(this)
        lang = Prefs.uiLang(this)
        val raw = intent.getStringExtra("ayah")
        if (raw == null) {
            finish()
            return
        }
        ayah = Ayah.fromJson(JSONObject(raw))

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)
        root.fitsSystemWindows = true

        val zoom = ZoomImageView(this)
        root.addView(zoom, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val close = TextView(this)
        close.text = "✕"
        close.textSize = 22f
        close.setTextColor(Color.WHITE)
        close.setPadding(dp(18), dp(12), dp(18), dp(12))
        close.setOnClickListener { finish() }
        root.addView(close, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END))

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.setBackgroundColor(Color.argb(170, 0, 0, 0))
        bar.setPadding(dp(8), dp(10), dp(8), dp(14))
        fun cell(b: android.widget.Button) {
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp.setMargins(dp(4), 0, dp(4), 0)
            bar.addView(b, lp)
        }
        cell(Ui.outline(this, t("zoom_save")) { saveToGallery() })
        cell(Ui.outline(this, t("b_share")) { share() })
        cell(Ui.primary(this, t("b_wall")) { WallpaperSetter.choose(this, ayah) })
        root.addView(bar, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM))

        Thread {
            val bmp = WallpaperPainter.paint(this, ayah, 1080, 1620, 0, true)
            runOnUiThread {
                bitmap = bmp
                zoom.setImageBitmap(bmp)
            }
        }.start()

        Ui.fadeIn(root)
        setContentView(root)
    }

    private fun saveToGallery() {
        val bmp = bitmap ?: return
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val v = ContentValues()
                v.put(MediaStore.Images.Media.DISPLAY_NAME, "ayah_" + ayah.surah + "_" + ayah.ayah + ".png")
                v.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                v.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AyahWallpaper")
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)
                    ?: throw Exception("no uri")
                contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                Toast.makeText(this, t("zoom_saved"), Toast.LENGTH_SHORT).show()
            } else {
                share()
            }
        } catch (e: Exception) {
            Toast.makeText(this, t("zoom_fail"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun share() {
        val bmp = bitmap ?: return
        try {
            val dir = File(cacheDir, "share")
            dir.mkdirs()
            val f = File(dir, "ayah_zoom.png")
            FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "image/png"
            i.putExtra(Intent.EXTRA_STREAM, uri)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(i, t("b_share")))
        } catch (e: Exception) {
            Toast.makeText(this, t("zoom_fail"), Toast.LENGTH_SHORT).show()
        }
    }
}
