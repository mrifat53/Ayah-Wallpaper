package com.rifat.ayahwall

import android.app.WallpaperManager
import android.content.Context
import android.os.Handler
import android.os.Looper

object WallpaperSetter {
    fun setLockAsync(c: Context, a: Ayah, done: (Boolean) -> Unit) {
        val ui = Handler(Looper.getMainLooper())
        Thread {
            val dm = c.resources.displayMetrics
            val ok = try {
                val bmp = WallpaperPainter.paint(c, a, dm.widthPixels, dm.heightPixels, Prefs.yPercent(c), false)
                WallpaperManager.getInstance(c).setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                true
            } catch (e: Exception) {
                false
            }
            ui.post { done(ok) }
        }.start()
    }
}
