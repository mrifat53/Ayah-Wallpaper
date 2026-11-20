package com.rifat.ayahwall

import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object WallpaperSetter {

    // which: 0 = lock screen, 1 = home screen, 2 = both
    fun setAsync(c: Context, a: Ayah, which: Int, done: (Boolean) -> Unit) {
        val ui = Handler(Looper.getMainLooper())
        Thread {
            val dm = c.resources.displayMetrics
            val flags = when (which) {
                0 -> WallpaperManager.FLAG_LOCK
                1 -> WallpaperManager.FLAG_SYSTEM
                else -> WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
            }
            val ok = try {
                val bmp = WallpaperPainter.paint(c, a, dm.widthPixels, dm.heightPixels, Prefs.yPercent(c), false)
                WallpaperManager.getInstance(c).setBitmap(bmp, null, true, flags)
                true
            } catch (e: Exception) {
                false
            }
            ui.post { done(ok) }
        }.start()
    }

    fun setLockAsync(c: Context, a: Ayah, done: (Boolean) -> Unit) = setAsync(c, a, 0, done)

    // Small list: lock screen, home screen or both. Nothing else pops up, no ads.
    fun choose(activity: Activity, a: Ayah) {
        val lang = Prefs.uiLang(activity)
        val items = arrayOf(I18n.t("wp_lock", lang), I18n.t("wp_home", lang), I18n.t("wp_both", lang))
        AlertDialog.Builder(activity)
            .setTitle(I18n.t("wp_title", lang))
            .setItems(items) { _, which ->
                setAsync(activity, a, which) { ok ->
                    Toast.makeText(activity, I18n.t(if (ok) "wp_done" else "lock_fail", lang), Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
