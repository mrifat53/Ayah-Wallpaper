package com.rifat.ayahwall

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View

// Five prayers on a smooth arc. Tap a dot to mark it as prayed.
class PrayerArcView(
    c: Context,
    private val labels: List<String>,
    private val times: List<String>,
    private val active: Int,
    private val done: List<Boolean>,
    private val onTap: (Int) -> Unit
) : View(c) {

    private val d = resources.displayMetrics.density
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * d
        strokeCap = Paint.Cap.ROUND
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
        color = Color.argb(225, 255, 240, 210)
        textAlign = Paint.Align.CENTER
        textSize = 12f * d
    }

    private fun xAt(i: Int): Float = width * (0.10f + 0.20f * i)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val baseY = h * 0.50f
        val rise = h * 0.30f
        fun yAt(x: Float): Float {
            val t = (x - w / 2f) / (w * 0.4f)
            return baseY - rise * (1f - t * t)
        }

        val path = Path()
        for (k in 0..40) {
            val x = w * 0.10f + w * 0.80f * k / 40f
            val y = yAt(x)
            if (k == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, line)

        for (i in 0 until 5) {
            val x = xAt(i)
            val y = yAt(x)
            val r = if (i == active) 10f * d else 7f * d
            when {
                done.getOrElse(i) { false } -> {
                    fill.color = Theme.col("accent")
                    canvas.drawCircle(x, y, r, fill)
                }
                i == active -> {
                    fill.color = Color.parseColor("#E8C36A")
                    canvas.drawCircle(x, y, r, fill)
                }
                else -> canvas.drawCircle(x, y, r, ring)
            }
            canvas.drawText(labels.getOrElse(i) { "" }, x, y + 32f * d, name)
            canvas.drawText(times.getOrElse(i) { "" }, x, y + 50f * d, time)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP) {
            var best = -1
            var bestD = width * 0.10f
            for (i in 0 until 5) {
                val dx = Math.abs(e.x - xAt(i))
                if (dx < bestD) {
                    bestD = dx
                    best = i
                }
            }
            if (best >= 0) {
                performClick()
                onTap(best)
            }
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()
}
