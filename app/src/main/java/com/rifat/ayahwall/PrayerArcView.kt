package com.rifat.ayahwall

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

// Five prayers on a soft arc: the next one is gold, prayed ones are filled green.
class PrayerArcView(
    c: Context,
    private val labels: List<String>,
    private val times: List<String>,
    private val active: Int,
    private val done: List<Boolean>
) : View(c) {

    private val d = resources.displayMetrics.density
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * d
        color = Color.argb(190, 255, 255, 255)
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * d
        color = Color.WHITE
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val name = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 13f * d
    }
    private val time = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 240, 210)
        textAlign = Paint.Align.CENTER
        textSize = 12f * d
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val base = h * 0.42f
        val peak = h * 0.28f
        fun yAt(x: Float): Float {
            val t = (x - w / 2f) / (w / 2f)
            return base - peak * (1f - t * t)
        }
        val xs = FloatArray(5) { w * (0.12f + 0.19f * it) }

        val path = Path()
        path.moveTo(xs[0], yAt(xs[0]))
        for (i in 1 until 5) path.lineTo(xs[i], yAt(xs[i]))
        canvas.drawPath(path, line)

        for (i in 0 until 5) {
            val x = xs[i]
            val y = yAt(x)
            val r = if (i == active) 9f * d else 6f * d
            when {
                done.getOrElse(i) { false } -> {
                    fill.color = Color.parseColor("#5FB894")
                    canvas.drawCircle(x, y, r, fill)
                }
                i == active -> {
                    fill.color = Color.parseColor("#E8C36A")
                    canvas.drawCircle(x, y, r, fill)
                }
                else -> canvas.drawCircle(x, y, r, ring)
            }
            canvas.drawText(labels.getOrElse(i) { "" }, x, y + 24f * d, name)
            canvas.drawText(times.getOrElse(i) { "" }, x, y + 42f * d, time)
        }
    }
}
