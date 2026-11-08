package com.rifat.ayahwall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

object WallpaperPainter {

    fun paint(ctx: Context, ayah: Ayah, yPercent: Int): Bitmap {
        val dm = ctx.resources.displayMetrics
        val w = dm.widthPixels
        val h = dm.heightPixels
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val isPhoto = drawBackground(ctx, canvas, w, h)
        canvas.drawColor(Color.argb(if (isPhoto) 150 else 70, 0, 0, 0))
        drawText(canvas, ayah, w, h, yPercent)
        return bmp
    }

    private fun drawBackground(ctx: Context, canvas: Canvas, w: Int, h: Int): Boolean {
        val names: List<String> = try {
            (ctx.assets.list("backgrounds") ?: emptyArray()).filter { n ->
                val l = n.lowercase()
                l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") || l.endsWith(".webp")
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (names.isEmpty()) {
            BackgroundGenerator.draw(canvas, w, h)
            return false
        }

        val src = decode(ctx, "backgrounds/" + names.random(), w, h)
        if (src == null) {
            BackgroundGenerator.draw(canvas, w, h)
            return false
        }

        val scale = maxOf(w.toFloat() / src.width, h.toFloat() / src.height)
        val dw = src.width * scale
        val dh = src.height * scale
        val left = (w - dw) / 2f
        val top = (h - dh) / 2f
        canvas.drawBitmap(src, null, RectF(left, top, left + dw, top + dh), Paint(Paint.FILTER_BITMAP_FLAG))
        return true
    }

    private fun decode(ctx: Context, path: String, w: Int, h: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= w && bounds.outHeight / (sample * 2) >= h) sample *= 2
            val opts = BitmapFactory.Options()
            opts.inSampleSize = sample
            ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
        } catch (e: Exception) {
            null
        }
    }

    private fun drawGradient(canvas: Canvas, w: Int, h: Int) {
        val p = Paint()
        p.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            Color.parseColor("#050505"), Color.parseColor("#0B2A20"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    private fun build(ayah: Ayah, w: Int, blockW: Int, scale: Float): List<StaticLayout> {
        fun mk(text: String, size: Float, color: Int): StaticLayout {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG)
            p.color = color
            p.textSize = size * scale
            return StaticLayout.Builder.obtain(text, 0, text.length, p, blockW)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.25f)
                .build()
        }
        return listOf(
            mk(ayah.arabic, w * 0.065f, Color.WHITE),
            mk(ayah.bangla, w * 0.042f, Color.parseColor("#EDEDED")),
            mk(ayah.english, w * 0.036f, Color.parseColor("#CFCFCF")),
            mk("${ayah.surahName} ${ayah.surahNumber}:${ayah.ayahNumber}", w * 0.032f, Color.parseColor("#D9B65B"))
        )
    }

    private fun drawText(canvas: Canvas, ayah: Ayah, w: Int, h: Int, yPercent: Int) {
        val blockW = (w * 0.84f).toInt()
        val x = w * 0.08f
        val top = h * yPercent / 100f
        val gap = w * 0.035f

        var scale = 1f
        var layouts = build(ayah, w, blockW, scale)
        for (attempt in 0 until 8) {
            val total = layouts.sumOf { it.height } + gap * (layouts.size - 1)
            if (top + total <= h * 0.93f) break
            scale *= 0.9f
            layouts = build(ayah, w, blockW, scale)
        }

        var y = top
        for (layout in layouts) {
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + gap
        }
    }
}
