package com.rifat.ayahwall

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

// Draws original backgrounds in code: night skies with a mosque skyline,
// and Islamic geometric star patterns. No photos, so no licence problems.
object BackgroundGenerator {

    private val skyPalettes = listOf(
        intArrayOf(Color.parseColor("#02030A"), Color.parseColor("#0B1B3A")),
        intArrayOf(Color.parseColor("#04020C"), Color.parseColor("#251440")),
        intArrayOf(Color.parseColor("#010708"), Color.parseColor("#0A3036")),
        intArrayOf(Color.parseColor("#050505"), Color.parseColor("#1A2230"))
    )

    private val patternPalettes = listOf(
        arrayOf("#031A14", "#08382B", "#D9B65B"),
        arrayOf("#050B1A", "#0E2347", "#C9D6F5"),
        arrayOf("#0E0A05", "#2B1D0C", "#D9B65B"),
        arrayOf("#04141A", "#0B3A45", "#9FE0D2")
    )

    fun draw(canvas: Canvas, w: Int, h: Int) {
        val rnd = Random()
        if (rnd.nextBoolean()) {
            nightSky(canvas, w, h, rnd)
        } else {
            pattern(canvas, w, h, rnd)
        }
    }

    // ---------- Night sky with moon and mosque skyline ----------

    private fun nightSky(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val pal = skyPalettes[rnd.nextInt(skyPalettes.size)]
        val bg = Paint()
        bg.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), pal[0], pal[1], Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        val unit = w / 1080f

        val star = Paint(Paint.ANTI_ALIAS_FLAG)
        val count = 160 + rnd.nextInt(120)
        for (i in 0 until count) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h * 0.85f
            val r = (0.6f + rnd.nextFloat() * 1.6f) * unit
            star.color = Color.argb(70 + rnd.nextInt(160), 255, 255, 240)
            canvas.drawCircle(x, y, r, star)
        }

        for (i in 0 until 6) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h * 0.7f
            val glow = Paint(Paint.ANTI_ALIAS_FLAG)
            glow.shader = RadialGradient(
                x, y, 14f * unit,
                Color.argb(130, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(x, y, 14f * unit, glow)
            star.color = Color.WHITE
            canvas.drawCircle(x, y, 2.2f * unit, star)
        }

        val moonR = w * 0.075f
        val mx = if (rnd.nextBoolean()) w * 0.78f else w * 0.22f
        val my = h * (0.76f + rnd.nextFloat() * 0.04f)

        val halo = Paint(Paint.ANTI_ALIAS_FLAG)
        halo.shader = RadialGradient(
            mx, my, moonR * 4f,
            Color.argb(80, 255, 240, 200), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(mx, my, moonR * 4f, halo)

        val outer = Path()
        outer.addCircle(mx, my, moonR, Path.Direction.CW)
        val cut = Path()
        cut.addCircle(mx + moonR * 0.45f, my - moonR * 0.2f, moonR * 0.88f, Path.Direction.CW)
        outer.op(cut, Path.Op.DIFFERENCE)
        val moon = Paint(Paint.ANTI_ALIAS_FLAG)
        moon.color = Color.parseColor("#F4E7B8")
        canvas.drawPath(outer, moon)

        skyline(canvas, w, h, rnd)
    }

    private fun dome(canvas: Canvas, p: Paint, cx: Float, y0: Float, r: Float) {
        val path = Path()
        path.moveTo(cx - r, y0)
        path.arcTo(RectF(cx - r, y0 - r, cx + r, y0 + r), 180f, 180f)
        path.close()
        canvas.drawPath(path, p)
    }

    private fun skyline(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.parseColor("#020406")

        val base = h * 0.93f
        canvas.drawRect(0f, base, w.toFloat(), h.toFloat(), p)

        val cx = w * (0.4f + rnd.nextFloat() * 0.2f)
        val bw = w * 0.34f
        val bodyTop = base - h * 0.025f
        canvas.drawRect(cx - bw / 2f, bodyTop, cx + bw / 2f, base, p)

        val dr = bw * 0.30f
        dome(canvas, p, cx, bodyTop, dr)
        canvas.drawRect(cx - w * 0.002f, bodyTop - dr - h * 0.015f, cx + w * 0.002f, bodyTop - dr, p)

        dome(canvas, p, cx - bw * 0.38f, bodyTop, dr * 0.5f)
        dome(canvas, p, cx + bw * 0.38f, bodyTop, dr * 0.5f)

        val mw = w * 0.018f
        val mh = h * 0.14f
        for (side in intArrayOf(-1, 1)) {
            val mx = cx + side * bw * 0.75f
            val top = base - mh
            canvas.drawRect(mx - mw / 2f, top, mx + mw / 2f, base, p)
            canvas.drawRect(mx - mw * 1.3f, top + mh * 0.12f, mx + mw * 1.3f, top + mh * 0.17f, p)
            val cone = Path()
            cone.moveTo(mx - mw * 0.8f, top)
            cone.lineTo(mx, top - mw * 3f)
            cone.lineTo(mx + mw * 0.8f, top)
            cone.close()
            canvas.drawPath(cone, p)
        }
    }

    // ---------- Islamic geometric star pattern ----------

    private fun pattern(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val pal = patternPalettes[rnd.nextInt(patternPalettes.size)]
        val bg = Paint()
        bg.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            Color.parseColor(pal[0]), Color.parseColor(pal[1]), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        val line = Paint(Paint.ANTI_ALIAS_FLAG)
        line.style = Paint.Style.STROKE
        line.strokeWidth = w * 0.0028f
        line.color = Color.parseColor(pal[2])
        line.alpha = 85

        val s = w / 4f
        val cols = 6
        val rows = (h / s).toInt() + 3
        for (r in -1 until rows) {
            for (c in -1 until cols) {
                val x = c * s
                val y = r * s
                star(canvas, line, x, y, s * 0.42f)
                star(canvas, line, x + s / 2f, y + s / 2f, s * 0.22f)
            }
        }
    }

    private fun star(canvas: Canvas, p: Paint, cx: Float, cy: Float, radius: Float) {
        for (rot in intArrayOf(0, 45)) {
            val path = Path()
            for (k in 0 until 4) {
                val ang = Math.toRadians((rot + 90 * k).toDouble())
                val x = cx + radius * cos(ang).toFloat()
                val y = cy + radius * sin(ang).toFloat()
                if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            canvas.drawPath(path, p)
        }
        canvas.drawCircle(cx, cy, radius * 0.55f, p)
    }
}
