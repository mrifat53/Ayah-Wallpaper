package com.rifat.ayahwall

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

// Photo-style viewer: pinch to zoom, double-tap to zoom in or out, drag to move.
class ZoomImageView(c: Context) : ImageView(c) {

    private val m = Matrix()
    private var minScale = 1f

    private val scaleDet = ScaleGestureDetector(c, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            zoomBy(d.scaleFactor, d.focusX, d.focusY)
            return true
        }
    })

    private val gestureDet = GestureDetector(c, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale() > minScale * 1.05f) reset() else zoomBy(3f, e.x, e.y)
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            m.postTranslate(-dx, -dy)
            fix()
            imageMatrix = m
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        reset()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        post { reset() }
    }

    private fun reset() {
        val d = drawable ?: return
        if (width == 0 || height == 0) return
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        val s = Math.min(width / dw, height / dh)
        minScale = s
        m.reset()
        m.postScale(s, s)
        m.postTranslate((width - dw * s) / 2f, (height - dh * s) / 2f)
        imageMatrix = m
    }

    private fun currentScale(): Float {
        val v = FloatArray(9)
        m.getValues(v)
        return v[Matrix.MSCALE_X]
    }

    private fun zoomBy(f: Float, fx: Float, fy: Float) {
        var factor = f
        val cur = currentScale()
        val max = minScale * 6f
        if (cur * factor > max) factor = max / cur
        if (cur * factor < minScale) factor = minScale / cur
        m.postScale(factor, factor, fx, fy)
        fix()
        imageMatrix = m
    }

    // Keeps the picture inside the screen
    private fun fix() {
        val d = drawable ?: return
        val r = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        m.mapRect(r)
        var dx = 0f
        var dy = 0f
        if (r.width() <= width) {
            dx = (width - r.width()) / 2f - r.left
        } else {
            if (r.left > 0) dx = -r.left else if (r.right < width) dx = width - r.right
        }
        if (r.height() <= height) {
            dy = (height - r.height()) / 2f - r.top
        } else {
            if (r.top > 0) dy = -r.top else if (r.bottom < height) dy = height - r.bottom
        }
        m.postTranslate(dx, dy)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleDet.onTouchEvent(e)
        if (!scaleDet.isInProgress) gestureDet.onTouchEvent(e)
        return true
    }
}
