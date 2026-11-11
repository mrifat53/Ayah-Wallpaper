package com.rifat.ayahwall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

object WallpaperPainter {

    private class Block(val text: String, val size: Float, val color: Int, val gapAfter: Float)

    private fun layoutsFor(blocks: List<Block>, w: Int, blockW: Int, scale: Float): List<StaticLayout> {
        val out = ArrayList<StaticLayout>()
        for (b in blocks) {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG)
            p.color = b.color
            p.textSize = b.size * w * scale
            out.add(
                StaticLayout.Builder.obtain(b.text, 0, b.text.length, p, blockW)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, 1.25f)
                    .build()
            )
        }
        return out
    }

    private fun totalHeight(layouts: List<StaticLayout>, blocks: List<Block>, scale: Float): Float {
        var t = 0f
        for (i in layouts.indices) {
            t += layouts[i].height
            if (i < layouts.size - 1) t += blocks[i].gapAfter * scale
        }
        return t
    }

    fun paint(ctx: Context, ayah: Ayah, w: Int, h: Int, yPercent: Int, centered: Boolean): Bitmap {
        val langs = Prefs.langs(ctx)
        val blockW = (w * 0.84f).toInt()
        val x = w * 0.08f
        val boost = if (langs.size == 1) 1.2f else 1f
        val gold = Color.parseColor("#D9B65B")

        val blocks = ArrayList<Block>()
        fun add(lang: String, text: String, size: Float, color: Int) {
            if (lang !in langs || text.isBlank()) return
            blocks.add(Block(text, size * boost, color, w * 0.010f))
            blocks.add(Block(I18n.ref(lang, ayah.surah, ayah.ayah), 0.028f, gold, w * 0.040f))
        }
        add("ar", ayah.arabic, 0.062f, Color.WHITE)
        add("bn", ayah.bangla, 0.040f, Color.parseColor("#EDEDED"))
        add("en", ayah.english, 0.036f, Color.parseColor("#D4D4D4"))
        if (blocks.isEmpty()) {
            blocks.add(Block(ayah.arabic, 0.062f, Color.WHITE, w * 0.010f))
            blocks.add(Block(I18n.ref("ar", ayah.surah, ayah.ayah), 0.028f, gold, 0f))
        }

        var scale = 1f
        var layouts = layoutsFor(blocks, w, blockW, scale)
        var total = totalHeight(layouts, blocks, scale)
        var top = if (centered) (h - total) / 2f else h * yPercent / 100f
        for (i in 0 until 8) {
            val fits = top + total <= h * 0.92f && (!centered || total <= h * 0.8f)
            if (fits) break
            scale *= 0.9f
            layouts = layoutsFor(blocks, w, blockW, scale)
            total = totalHeight(layouts, blocks, scale)
            top = if (centered) (h - total) / 2f else h * yPercent / 100f
        }

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        BackgroundGenerator.draw(canvas, w, h, Prefs.style(ctx), ayah.surah * 1000L + ayah.ayah)

        // Soft dark band behind the text so it is always easy to read
        val bandTop = maxOf(0f, top - h * 0.05f)
        val bandBottom = minOf(h.toFloat(), top + total + h * 0.05f)
        val band = Paint()
        band.shader = LinearGradient(
            0f, bandTop, 0f, bandBottom,
            intArrayOf(Color.argb(0, 0, 0, 0), Color.argb(150, 0, 0, 0), Color.argb(150, 0, 0, 0), Color.argb(0, 0, 0, 0)),
            floatArrayOf(0f, 0.2f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, bandTop, w.toFloat(), bandBottom, band)

        var y = top
        for (i in layouts.indices) {
            canvas.save()
            canvas.translate(x, y)
            layouts[i].draw(canvas)
            canvas.restore()
            y += layouts[i].height + blocks[i].gapAfter * scale
        }
        return bmp
    }
}
