package com.rifat.ayahwall

import android.graphics.BlurMaskFilter
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

// Original backgrounds drawn in code. No photos, so there are no licence problems.
// style: 0 = random, 1 = night sky, 2 = pattern, 3 = Kaaba, 4 = green dome, 5 = mihrab, 6 = glowing sky
object BackgroundGenerator {

    fun draw(canvas: Canvas, w: Int, h: Int, style: Int, seed: Long = 0L) {
        val rnd = Random(seed)
        val st = if (style in 1..16) style else 15
        when (st) {
            1 -> nightSky(canvas, w, h, rnd)
            2 -> pattern(canvas, w, h, rnd)
            3 -> kaaba(canvas, w, h, rnd)
            4 -> greenDome(canvas, w, h, rnd)
            5 -> mihrab(canvas, w, h, rnd)
            6 -> aurora(canvas, w, h, rnd)
            7 -> calligraphy(canvas, w, h, rnd)
            8 -> rug(canvas, w, h, rnd)
            15 -> endless(canvas, w, h, rnd)
            16 -> arches(canvas, w, h, rnd)
            10 -> floral(canvas, w, h, rnd)
            11 -> goldRibbon(canvas, w, h, rnd)
            12 -> marble(canvas, w, h, rnd)
            13 -> hills(canvas, w, h, rnd)
            14 -> goldStars(canvas, w, h, rnd)
            else -> softTone(canvas, w, h, rnd)
        }
        vignette(canvas, w, h)
    }

    // ---------- helpers ----------

    private fun col(hex: String): Int = Color.parseColor(hex)

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun vgrad(canvas: Canvas, w: Int, h: Int, colors: IntArray, pos: FloatArray?) {
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), colors, pos, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    private fun glow(canvas: Canvas, x: Float, y: Float, r: Float, color: Int, alpha: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val c0 = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        val c1 = Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))
        p.shader = RadialGradient(x, y, r, c0, c1, Shader.TileMode.CLAMP)
        canvas.drawCircle(x, y, r, p)
    }

    private fun stars(canvas: Canvas, w: Int, h: Int, rnd: Random, count: Int, maxY: Float) {
        val u = w / 1080f
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until count) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h * maxY
            val r = (0.6f + rnd.nextFloat() * 1.6f) * u
            p.color = Color.argb(70 + rnd.nextInt(160), 255, 255, 240)
            canvas.drawCircle(x, y, r, p)
        }
        for (i in 0 until 7) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h * maxY
            glow(canvas, x, y, 16f * u, Color.WHITE, 130)
            p.color = Color.WHITE
            canvas.drawCircle(x, y, 2.2f * u, p)
        }
    }

    private fun vignette(canvas: Canvas, w: Int, h: Int) {
        val p = Paint()
        p.shader = RadialGradient(
            w / 2f, h / 2f, h * 0.78f,
            Color.TRANSPARENT, Color.argb(120, 0, 0, 0), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    private fun bil(a: FloatArray, b: FloatArray, c: FloatArray, d: FloatArray, u: Float, v: Float): FloatArray {
        val tx = lerp(a[0], b[0], u)
        val ty = lerp(a[1], b[1], u)
        val bx = lerp(d[0], c[0], u)
        val by = lerp(d[1], c[1], u)
        return floatArrayOf(lerp(tx, bx, v), lerp(ty, by, v))
    }

    private fun poly(canvas: Canvas, p: Paint, pts: List<FloatArray>) {
        val path = Path()
        path.moveTo(pts[0][0], pts[0][1])
        for (i in 1 until pts.size) path.lineTo(pts[i][0], pts[i][1])
        path.close()
        canvas.drawPath(path, p)
    }

    private fun domeShape(canvas: Canvas, p: Paint, cx: Float, y0: Float, rx: Float, ry: Float) {
        val path = Path()
        path.moveTo(cx - rx, y0)
        path.arcTo(RectF(cx - rx, y0 - ry, cx + rx, y0 + ry), 180f, 180f)
        path.close()
        canvas.drawPath(path, p)
    }

    private fun minaret(canvas: Canvas, p: Paint, gold: Paint, x: Float, base: Float, mw: Float, mh: Float) {
        val top = base - mh
        canvas.drawRect(x - mw / 2f, top, x + mw / 2f, base, p)
        canvas.drawRect(x - mw * 1.3f, top + mh * 0.12f, x + mw * 1.3f, top + mh * 0.16f, p)
        canvas.drawRect(x - mw * 1.3f, top + mh * 0.50f, x + mw * 1.3f, top + mh * 0.54f, p)
        val cone = Path()
        cone.moveTo(x - mw * 0.85f, top)
        cone.lineTo(x, top - mw * 3.2f)
        cone.lineTo(x + mw * 0.85f, top)
        cone.close()
        canvas.drawPath(cone, p)
        canvas.drawCircle(x, top - mw * 3.5f, mw * 0.28f, gold)
        for (i in 0 until 4) {
            canvas.drawCircle(x, top + mh * (0.25f + 0.18f * i), mw * 0.10f, gold)
        }
    }

    // ---------- 1. Starry night with moon and mosque ----------

    private val skyPalettes = listOf(
        intArrayOf(Color.parseColor("#02030A"), Color.parseColor("#0B1B3A")),
        intArrayOf(Color.parseColor("#04020C"), Color.parseColor("#251440")),
        intArrayOf(Color.parseColor("#010708"), Color.parseColor("#0A3036")),
        intArrayOf(Color.parseColor("#050505"), Color.parseColor("#1A2230"))
    )

    private fun nightSky(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val pal = skyPalettes[rnd.nextInt(skyPalettes.size)]
        vgrad(canvas, w, h, pal, null)
        stars(canvas, w, h, rnd, 220 + rnd.nextInt(120), 0.85f)

        val cx = w * (0.4f + rnd.nextFloat() * 0.2f)
        val moonR = w * 0.075f
        val my = h * 0.79f
        glow(canvas, cx, my, moonR * 4.5f, col("#FFF0C8"), 85)

        val outer = Path()
        outer.addCircle(cx, my, moonR, Path.Direction.CW)
        val cut = Path()
        cut.addCircle(cx + moonR * 0.45f, my - moonR * 0.2f, moonR * 0.88f, Path.Direction.CW)
        outer.op(cut, Path.Op.DIFFERENCE)
        val moon = Paint(Paint.ANTI_ALIAS_FLAG)
        moon.color = col("#F4E7B8")
        canvas.drawPath(outer, moon)

        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = col("#020406")
        val gold = Paint(Paint.ANTI_ALIAS_FLAG)
        gold.color = col("#D9B65B")
        val base = h * 0.93f
        canvas.drawRect(0f, base, w.toFloat(), h.toFloat(), p)
        val bw = w * 0.34f
        val bodyTop = base - h * 0.025f
        canvas.drawRect(cx - bw / 2f, bodyTop, cx + bw / 2f, base, p)
        val dr = bw * 0.30f
        domeShape(canvas, p, cx, bodyTop, dr, dr)
        canvas.drawRect(cx - w * 0.002f, bodyTop - dr - h * 0.015f, cx + w * 0.002f, bodyTop - dr, p)
        domeShape(canvas, p, cx - bw * 0.38f, bodyTop, dr * 0.5f, dr * 0.5f)
        domeShape(canvas, p, cx + bw * 0.38f, bodyTop, dr * 0.5f, dr * 0.5f)
        val mw = w * 0.018f
        val mh = h * 0.14f
        minaret(canvas, p, gold, cx - bw * 0.75f, base, mw, mh)
        minaret(canvas, p, gold, cx + bw * 0.75f, base, mw, mh)
    }

    // ---------- 2. Islamic geometric pattern ----------

    private val patternPalettes = listOf(
        arrayOf("#031A14", "#08382B", "#D9B65B"),
        arrayOf("#050B1A", "#0E2347", "#C9D6F5"),
        arrayOf("#0E0A05", "#2B1D0C", "#D9B65B"),
        arrayOf("#04141A", "#0B3A45", "#9FE0D2")
    )

    private fun pattern(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val pal = patternPalettes[rnd.nextInt(patternPalettes.size)]
        vgrad(canvas, w, h, intArrayOf(col(pal[0]), col(pal[1])), null)
        glow(canvas, w * 0.5f, h * 0.42f, w * 1.0f, col(pal[2]), 55)

        val line = Paint(Paint.ANTI_ALIAS_FLAG)
        line.style = Paint.Style.STROKE
        line.strokeWidth = w * 0.0028f
        line.color = col(pal[2])
        line.alpha = 85
        val thin = Paint(line)
        thin.strokeWidth = w * 0.0016f
        thin.alpha = 45

        val s = w / 4f
        val rows = (h / s).toInt() + 3
        for (r in -1 until rows) {
            for (c in -1 until 6) {
                val x = c * s
                val y = r * s
                starShape(canvas, line, x, y, s * 0.42f)
                starShape(canvas, thin, x, y, s * 0.30f)
                starShape(canvas, line, x + s / 2f, y + s / 2f, s * 0.22f)
            }
        }
    }

    private fun starShape(canvas: Canvas, p: Paint, cx: Float, cy: Float, radius: Float) {
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

    // ---------- 3. Kaaba at night ----------

    private fun kaaba(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#01030A"), col("#0A1330"), col("#2A1F14")), floatArrayOf(0f, 0.7f, 1f))
        stars(canvas, w, h, rnd, 200, 0.6f)

        val ground = h * 0.88f
        glow(canvas, w * 0.5f, ground, w * 0.85f, col("#E8C36A"), 80)
        val floor = Paint()
        floor.shader = LinearGradient(
            0f, ground, 0f, h.toFloat(), col("#1A1812"), col("#07070A"), Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, ground, w.toFloat(), h.toFloat(), floor)

        val cx = w * 0.5f
        val fw = w * 0.30f
        val kh = fw * 1.02f
        val t0 = ground - kh
        val d = kh * 0.045f
        val lw = fw * 0.62f
        val rw = fw * 0.46f

        val a1 = floatArrayOf(cx - lw, t0 + d)
        val b1 = floatArrayOf(cx, t0)
        val c1 = floatArrayOf(cx, ground)
        val d1 = floatArrayOf(cx - lw, ground - d)
        val a2 = b1
        val b2 = floatArrayOf(cx + rw, t0 + d)
        val c2 = floatArrayOf(cx + rw, ground - d)
        val d2 = c1

        val left = Paint(Paint.ANTI_ALIAS_FLAG)
        left.color = col("#101015")
        val right = Paint(Paint.ANTI_ALIAS_FLAG)
        right.color = col("#050507")
        poly(canvas, left, listOf(a1, b1, c1, d1))
        poly(canvas, right, listOf(a2, b2, c2, d2))

        val goldFill = Paint(Paint.ANTI_ALIAS_FLAG)
        goldFill.color = col("#C9A24B")
        val goldDark = Paint(Paint.ANTI_ALIAS_FLAG)
        goldDark.color = col("#7A5C1E")
        goldDark.style = Paint.Style.STROKE
        goldDark.strokeWidth = 3f * u

        // Gold belt on both faces
        val faces = listOf(listOf(a1, b1, c1, d1), listOf(a2, b2, c2, d2))
        for (f in faces) {
            val p1 = bil(f[0], f[1], f[2], f[3], 0f, 0.15f)
            val p2 = bil(f[0], f[1], f[2], f[3], 1f, 0.15f)
            val p3 = bil(f[0], f[1], f[2], f[3], 1f, 0.25f)
            val p4 = bil(f[0], f[1], f[2], f[3], 0f, 0.25f)
            poly(canvas, goldFill, listOf(p1, p2, p3, p4))
            for (i in 1 until 10) {
                val uu = i / 10f
                val q1 = bil(f[0], f[1], f[2], f[3], uu, 0.17f)
                val q2 = bil(f[0], f[1], f[2], f[3], uu, 0.23f)
                canvas.drawLine(q1[0], q1[1], q2[0], q2[1], goldDark)
            }
        }

        // Door
        val dr = Paint(Paint.ANTI_ALIAS_FLAG)
        dr.color = col("#D6B25A")
        val f0 = faces[0]
        poly(
            canvas, dr,
            listOf(
                bil(f0[0], f0[1], f0[2], f0[3], 0.52f, 0.42f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.80f, 0.42f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.80f, 0.86f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.52f, 0.86f)
            )
        )
        val doorIn = Paint(Paint.ANTI_ALIAS_FLAG)
        doorIn.color = col("#3A2A0C")
        poly(
            canvas, doorIn,
            listOf(
                bil(f0[0], f0[1], f0[2], f0[3], 0.56f, 0.46f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.76f, 0.46f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.76f, 0.82f),
                bil(f0[0], f0[1], f0[2], f0[3], 0.56f, 0.82f)
            )
        )

        // Top edge highlight
        val edge = Paint(Paint.ANTI_ALIAS_FLAG)
        edge.color = col("#2C2C36")
        edge.style = Paint.Style.STROKE
        edge.strokeWidth = 3f * u
        canvas.drawLine(a1[0], a1[1], b1[0], b1[1], edge)
        canvas.drawLine(b1[0], b1[1], b2[0], b2[1], edge)
        canvas.drawLine(b1[0], b1[1], c1[0], c1[1], edge)

        // Minarets far away
        val mp = Paint(Paint.ANTI_ALIAS_FLAG)
        mp.color = col("#07080D")
        val gold = Paint(Paint.ANTI_ALIAS_FLAG)
        gold.color = col("#E8C36A")
        minaret(canvas, mp, gold, w * 0.10f, ground, w * 0.020f, h * 0.30f)
        minaret(canvas, mp, gold, w * 0.90f, ground, w * 0.020f, h * 0.30f)

        // Worshippers as tiny warm lights on the floor
        val dot = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 170) {
            val ang = rnd.nextFloat() * 6.2832f
            val rad = fw * (0.9f + rnd.nextFloat() * 1.9f)
            val x = cx + cos(ang) * rad
            val y = ground + h * 0.035f + sin(ang) * rad * 0.16f
            if (y < ground) continue
            dot.color = Color.argb(70 + rnd.nextInt(110), 255, 236, 190)
            canvas.drawCircle(x, y, (1.8f + rnd.nextFloat() * 2.2f) * u, dot)
        }
    }

    // ---------- 4. Green dome at dusk ----------

    private fun greenDome(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        vgrad(
            canvas, w, h,
            intArrayOf(col("#040616"), col("#1B2A5A"), col("#8E5A44"), col("#C98B4E")),
            floatArrayOf(0f, 0.55f, 0.85f, 1f)
        )
        stars(canvas, w, h, rnd, 160, 0.5f)
        glow(canvas, w * 0.5f, h * 0.90f, w * 0.95f, col("#FFB35A"), 95)

        val u = w / 1080f
        val base = h * 0.93f
        val ground = Paint(Paint.ANTI_ALIAS_FLAG)
        ground.color = col("#04060B")
        val gold = Paint(Paint.ANTI_ALIAS_FLAG)
        gold.color = col("#E8C36A")
        val dark = Paint(Paint.ANTI_ALIAS_FLAG)
        dark.color = col("#0B0F1A")

        val cx = w * 0.5f
        val r = w * 0.12f
        val y0 = base - r * 0.1f
        val baseTop = y0 - r * 0.55f

        // side buildings and small domes
        val side = Paint(Paint.ANTI_ALIAS_FLAG)
        side.color = col("#0A1018")
        canvas.drawRect(cx - r * 2.3f, y0 - r * 0.32f, cx + r * 2.3f, base, side)
        val teal = Paint(Paint.ANTI_ALIAS_FLAG)
        teal.color = col("#0D2A26")
        domeShape(canvas, teal, cx - r * 1.7f, y0 - r * 0.32f, r * 0.42f, r * 0.42f)
        domeShape(canvas, teal, cx + r * 1.7f, y0 - r * 0.32f, r * 0.42f, r * 0.42f)

        // central block
        canvas.drawRect(cx - r * 1.2f, baseTop, cx + r * 1.2f, base, dark)
        for (i in 0 until 5) {
            canvas.drawCircle(cx - r * 0.8f + i * r * 0.4f, baseTop + r * 0.25f, 3f * u, gold)
        }

        // green dome with a soft highlight
        val domePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        domePaint.shader = RadialGradient(
            cx - r * 0.3f, baseTop - r * 0.7f, r * 1.7f,
            col("#47D493"), col("#0B5A3A"), Shader.TileMode.CLAMP
        )
        domeShape(canvas, domePaint, cx, baseTop, r, r * 1.05f)
        val spire = Paint(Paint.ANTI_ALIAS_FLAG)
        spire.color = col("#E8C36A")
        spire.strokeWidth = 4f * u
        canvas.drawLine(cx, baseTop - r * 1.05f, cx, baseTop - r * 1.4f, spire)
        canvas.drawCircle(cx, baseTop - r * 1.2f, 7f * u, gold)

        // minarets
        minaret(canvas, dark, gold, cx - r * 2.7f, base, w * 0.022f, h * 0.15f)
        minaret(canvas, dark, gold, cx + r * 2.7f, base, w * 0.022f, h * 0.15f)
        minaret(canvas, dark, gold, cx - r * 3.8f, base, w * 0.020f, h * 0.12f)
        minaret(canvas, dark, gold, cx + r * 3.8f, base, w * 0.020f, h * 0.12f)

        canvas.drawRect(0f, base, w.toFloat(), h.toFloat(), ground)
    }

    // ---------- 5. Mihrab arch ----------

    private fun mihrab(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#02130E"), col("#04251C"), col("#02100C")), floatArrayOf(0f, 0.5f, 1f))

        val l = w * 0.06f
        val r = w * 0.94f
        val cx = w * 0.5f
        val b = h * 0.94f
        val sp = h * 0.42f
        val top = h * 0.14f

        fun arch(inset: Float): Path {
            val p = Path()
            p.moveTo(l + inset, b)
            p.lineTo(l + inset, sp)
            p.quadTo(l + inset, top + (sp - top) * 0.18f + inset, cx, top + inset)
            p.quadTo(r - inset, top + (sp - top) * 0.18f + inset, r - inset, sp)
            p.lineTo(r - inset, b)
            p.close()
            return p
        }

        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.shader = LinearGradient(0f, top, 0f, b, col("#0C5240"), col("#031A13"), Shader.TileMode.CLAMP)
        canvas.drawPath(arch(0f), fill)
        glow(canvas, cx, h * 0.38f, w * 0.75f, col("#E8C36A"), 75)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
        stroke.style = Paint.Style.STROKE
        stroke.color = col("#D9B65B")
        stroke.strokeWidth = 7f * u
        canvas.drawPath(arch(0f), stroke)
        stroke.strokeWidth = 2.5f * u
        stroke.alpha = 170
        canvas.drawPath(arch(26f * u), stroke)
        stroke.alpha = 90
        canvas.drawPath(arch(48f * u), stroke)

        // small stars inside the arch
        val star = Paint(Paint.ANTI_ALIAS_FLAG)
        star.style = Paint.Style.STROKE
        star.color = col("#D9B65B")
        star.alpha = 60
        star.strokeWidth = 2f * u
        for (i in 0 until 5) {
            starShape(canvas, star, w * (0.2f + 0.15f * i), h * 0.90f, w * 0.045f)
        }

        // hanging lantern
        val ropeX = cx
        val lanternY = top + (sp - top) * 0.55f
        val rope = Paint(Paint.ANTI_ALIAS_FLAG)
        rope.color = col("#D9B65B")
        rope.strokeWidth = 3f * u
        canvas.drawLine(ropeX, top + 8f * u, ropeX, lanternY, rope)
        glow(canvas, ropeX, lanternY + 40f * u, 170f * u, col("#FFD27A"), 150)
        val lantern = Paint(Paint.ANTI_ALIAS_FLAG)
        lantern.color = col("#E8C36A")
        val lp = Path()
        lp.moveTo(ropeX - 14f * u, lanternY)
        lp.lineTo(ropeX + 14f * u, lanternY)
        lp.lineTo(ropeX + 30f * u, lanternY + 70f * u)
        lp.lineTo(ropeX - 30f * u, lanternY + 70f * u)
        lp.close()
        canvas.drawPath(lp, lantern)
        stars(canvas, w, h, rnd, 40, 0.1f)
    }

    // ---------- 6. Glowing sky ----------

    private val auroraColors = listOf("#18E0B0", "#7A4DFF", "#2D7BFF", "#FFC857", "#FF6FB5", "#3DDC84")

    private fun aurora(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#02040A"), col("#06101C"), col("#020509")), floatArrayOf(0f, 0.6f, 1f))

        val picks = auroraColors.shuffled(kotlin.random.Random(rnd.nextLong())).take(3)
        for (c in picks) {
            val x = rnd.nextFloat() * w
            val y = h * (0.15f + rnd.nextFloat() * 0.7f)
            glow(canvas, x, y, w * (0.6f + rnd.nextFloat() * 0.5f), col(c), 85)
        }

        val ribbon = Paint(Paint.ANTI_ALIAS_FLAG)
        ribbon.style = Paint.Style.STROKE
        ribbon.strokeCap = Paint.Cap.ROUND
        ribbon.maskFilter = BlurMaskFilter(40f * u, BlurMaskFilter.Blur.NORMAL)
        for (i in 0 until 3) {
            val c = col(picks[i % picks.size])
            ribbon.color = Color.argb(80, Color.red(c), Color.green(c), Color.blue(c))
            ribbon.strokeWidth = (70f + rnd.nextFloat() * 70f) * u
            val y1 = h * (0.2f + rnd.nextFloat() * 0.6f)
            val y2 = h * (0.2f + rnd.nextFloat() * 0.6f)
            val path = Path()
            path.moveTo(-50f, y1)
            path.cubicTo(w * 0.3f, y1 - h * 0.18f, w * 0.7f, y2 + h * 0.18f, w + 50f, y2)
            canvas.drawPath(path, ribbon)
        }
        stars(canvas, w, h, rnd, 180, 0.9f)

        // small crescent near the bottom
        val mx = w * (0.25f + rnd.nextFloat() * 0.5f)
        val my = h * 0.86f
        val mr = w * 0.05f
        glow(canvas, mx, my, mr * 4f, col("#FFF0C8"), 70)
        val outer = Path()
        outer.addCircle(mx, my, mr, Path.Direction.CW)
        val cut = Path()
        cut.addCircle(mx + mr * 0.45f, my - mr * 0.2f, mr * 0.88f, Path.Direction.CW)
        outer.op(cut, Path.Op.DIFFERENCE)
        val moon = Paint(Paint.ANTI_ALIAS_FLAG)
        moon.color = col("#F4E7B8")
        canvas.drawPath(outer, moon)
    }

    // ---------- 7. Dark calligraphy-style strokes (drawn in code) ----------

    private fun calligraphy(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        vgrad(canvas, w, h, intArrayOf(col("#050608"), col("#0A0D10"), col("#050608")), null)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 46) {
            var cx = rnd.nextFloat() * w
            var cy = rnd.nextFloat() * h
            val path = Path()
            path.moveTo(cx, cy)
            for (k in 0 until 3) {
                val c1x = cx + (rnd.nextFloat() - 0.5f) * w * 0.5f
                val c1y = cy + (rnd.nextFloat() - 0.5f) * h * 0.2f
                val nx = cx + (rnd.nextFloat() - 0.5f) * w * 0.4f
                val ny = cy + (rnd.nextFloat() - 0.5f) * h * 0.25f
                path.cubicTo(c1x, c1y, c1x * 0.5f + nx * 0.5f, ny, nx, ny)
                cx = nx
                cy = ny
            }
            p.strokeWidth = (2f + rnd.nextFloat() * 7f) * w / 1080f
            val g = 90 + rnd.nextInt(90)
            p.color = Color.argb(40 + rnd.nextInt(70), g, g, g)
            canvas.drawPath(path, p)
        }
    }

    // ---------- 8. Patterned rug border (drawn in code, original design) ----------

    private fun rug(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#3A0F14"), col("#1A0508")), null)
        val frame = Paint(Paint.ANTI_ALIAS_FLAG)
        frame.style = Paint.Style.STROKE
        frame.color = col("#C9A24B")
        frame.strokeWidth = 6f * u
        val inset = w * 0.07f
        canvas.drawRect(inset, inset * 1.4f, w - inset, h - inset * 1.4f, frame)
        frame.strokeWidth = 2f * u
        frame.color = col("#E8D7B0")
        val in2 = inset + 18f * u
        canvas.drawRect(in2, in2 * 1.4f, w - in2, h - in2 * 1.4f, frame)

        // repeated diamonds inside the frame
        val motif = Paint(Paint.ANTI_ALIAS_FLAG)
        motif.style = Paint.Style.STROKE
        motif.strokeWidth = 2f * u
        motif.color = col("#E8D7B0")
        motif.alpha = 110
        val step = w * 0.11f
        var y = in2 * 1.4f + step / 2f
        var row = 0
        while (y < h - in2 * 1.4f) {
            var x = in2 + step / 2f + if (row % 2 == 0) 0f else step / 2f
            while (x < w - in2) {
                val path = Path()
                path.moveTo(x, y - step * 0.36f)
                path.lineTo(x + step * 0.28f, y)
                path.lineTo(x, y + step * 0.36f)
                path.lineTo(x - step * 0.28f, y)
                path.close()
                canvas.drawPath(path, motif)
                x += step
            }
            y += step * 0.72f
            row++
        }
    }

    // ---------- 9. Soft tones with a thin frame (drawn in code) ----------

    private fun softTone(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#E9DCD2"), col("#C9B7A6")), null)
        glow(canvas, w * 0.5f, h * 0.35f, w * 0.9f, col("#FFF8EE"), 150)
        val frame = Paint(Paint.ANTI_ALIAS_FLAG)
        frame.style = Paint.Style.STROKE
        frame.color = col("#8C6F5A")
        frame.strokeWidth = 2.5f * u
        val inset = w * 0.06f
        canvas.drawRect(inset, inset, w - inset, h - inset, frame)
        frame.strokeWidth = 1f * u
        canvas.drawRect(inset + 12f * u, inset + 12f * u, w - inset - 12f * u, h - inset - 12f * u, frame)
    }

    // ---------- 10. Soft flowers (drawn in code) ----------

    private fun floral(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#2B0D17"), col("#120609")), null)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = arrayOf("#F2A7B8", "#E58AA0", "#F7D08A", "#FFE3EC")
        for (i in 0 until 22) {
            val cx = rnd.nextFloat() * w
            val cy = rnd.nextFloat() * h
            val r = (30f + rnd.nextFloat() * 60f) * u
            val c = col(colors[rnd.nextInt(colors.size)])
            p.color = Color.argb(60 + rnd.nextInt(70), Color.red(c), Color.green(c), Color.blue(c))
            for (k in 0 until 6) {
                canvas.save()
                canvas.rotate(k * 60f, cx, cy)
                canvas.drawOval(cx - r * 0.35f, cy - r, cx + r * 0.35f, cy - r * 0.1f, p)
                canvas.restore()
            }
            p.color = col("#F7D08A")
            p.alpha = 150
            canvas.drawCircle(cx, cy, r * 0.18f, p)
            p.alpha = 255
        }
    }

    // ---------- 11. Gold ribbons (drawn in code) ----------

    private fun goldRibbon(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#0B1220"), col("#05080F")), null)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 7) {
            val path = Path()
            val y0 = h * (0.1f + rnd.nextFloat() * 0.8f)
            path.moveTo(-50f, y0)
            path.cubicTo(w * 0.3f, y0 - h * 0.2f, w * 0.7f, y0 + h * 0.2f, w + 50f, y0 + (rnd.nextFloat() - 0.5f) * h * 0.2f)
            p.strokeWidth = (2f + rnd.nextFloat() * 5f) * u
            p.color = Color.argb(90 + rnd.nextInt(110), 201, 162, 75)
            canvas.drawPath(path, p)
        }
    }

    // ---------- 12. Marble veins (drawn in code) ----------

    private fun marble(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#F4F1EC"), col("#DCD6CC")), null)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 26) {
            var cx = rnd.nextFloat() * w
            var cy = rnd.nextFloat() * h
            val path = Path()
            path.moveTo(cx, cy)
            for (k in 0 until 4) {
                val nx = cx + (rnd.nextFloat() - 0.5f) * w * 0.5f
                val ny = cy + (rnd.nextFloat() - 0.5f) * h * 0.25f
                path.quadTo(cx + (rnd.nextFloat() - 0.5f) * w * 0.4f, cy + (rnd.nextFloat() - 0.5f) * h * 0.2f, nx, ny)
                cx = nx
                cy = ny
            }
            p.strokeWidth = (1f + rnd.nextFloat() * 4f) * u
            val g = 90 + rnd.nextInt(60)
            p.color = Color.argb(40 + rnd.nextInt(60), g, g - 5, g - 10)
            canvas.drawPath(path, p)
        }
    }

    // ---------- 13. Green hills under a soft sky (drawn in code) ----------

    private fun hills(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        vgrad(canvas, w, h, intArrayOf(col("#7FB6D9"), col("#F3E7C9")), null)
        val greens = arrayOf("#B9D7A8", "#8DBB7A", "#5E9A5C", "#3B6E45")
        for (i in 0 until 4) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = col(greens[i])
            val baseY = h * (0.55f + i * 0.13f)
            val path = Path()
            path.moveTo(0f, h.toFloat())
            path.lineTo(0f, baseY)
            path.cubicTo(w * 0.25f, baseY - h * (0.06f + rnd.nextFloat() * 0.04f), w * 0.5f, baseY + h * 0.03f, w * 0.75f, baseY - h * 0.02f)
            path.cubicTo(w * 0.85f, baseY - h * 0.05f, w * 0.95f, baseY, w.toFloat(), baseY - h * 0.01f)
            path.lineTo(w.toFloat(), h.toFloat())
            path.close()
            canvas.drawPath(path, p)
        }
    }

    // ---------- 14. Gold stars on navy (drawn in code) ----------

    private fun goldStars(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#0A1A33"), col("#05101F")), null)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2f * u
        p.color = col("#C9A24B")
        p.alpha = 110
        val s = w / 3.5f
        var y = 0f
        var row = 0
        while (y < h + s) {
            var x = if (row % 2 == 0) 0f else s / 2f
            while (x < w + s) {
                starShape(canvas, p, x, y, s * 0.42f)
                x += s
            }
            y += s * 0.5f
            row++
        }
    }

    // ---------- 15. Endless: a new colour family and layout for every ayah ----------
    // Hue, saturation, brightness and the drawing style all come from the random seed,
    // so there is no fixed list of backgrounds; every ayah can get a different one.

    private fun hsv(h: Float, s: Float, v: Float, a: Int = 255): Int {
        val hh = ((h % 360f) + 360f) % 360f
        val c = Color.HSVToColor(floatArrayOf(hh, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f)))
        return Color.argb(a, Color.red(c), Color.green(c), Color.blue(c))
    }

    private fun endless(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        val hue = rnd.nextFloat() * 360f
        val dark = 0.10f + rnd.nextFloat() * 0.10f
        vgrad(canvas, w, h,
            intArrayOf(hsv(hue, 0.55f, dark), hsv(hue + 25f, 0.65f, dark * 0.45f)), null)

        // soft light spots in the same family
        for (i in 0 until 3) {
            glow(canvas, rnd.nextFloat() * w, rnd.nextFloat() * h, w * (0.4f + rnd.nextFloat() * 0.6f),
                hsv(hue + rnd.nextFloat() * 60f - 30f, 0.5f, 1f), 60 + rnd.nextInt(60))
        }

        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val accent = hsv(hue + 40f + rnd.nextFloat() * 40f, 0.45f + rnd.nextFloat() * 0.3f, 0.85f + rnd.nextFloat() * 0.15f)

        when (rnd.nextInt(5)) {
            0 -> { // flowing strokes
                p.style = Paint.Style.STROKE
                p.strokeCap = Paint.Cap.ROUND
                for (i in 0 until 30) {
                    var cx = rnd.nextFloat() * w
                    var cy = rnd.nextFloat() * h
                    val path = Path()
                    path.moveTo(cx, cy)
                    for (k in 0 until 3) {
                        val nx = cx + (rnd.nextFloat() - 0.5f) * w * 0.5f
                        val ny = cy + (rnd.nextFloat() - 0.5f) * h * 0.25f
                        path.quadTo(cx + (rnd.nextFloat() - 0.5f) * w * 0.4f, cy + (rnd.nextFloat() - 0.5f) * h * 0.2f, nx, ny)
                        cx = nx
                        cy = ny
                    }
                    p.strokeWidth = (1f + rnd.nextFloat() * 5f) * u
                    p.color = hsv(hue + rnd.nextFloat() * 50f, 0.4f, 0.9f, 50 + rnd.nextInt(90))
                    canvas.drawPath(path, p)
                }
            }
            1 -> { // petals
                p.style = Paint.Style.FILL
                val n = 6 + rnd.nextInt(6)
                for (i in 0 until 18) {
                    val cx = rnd.nextFloat() * w
                    val cy = rnd.nextFloat() * h
                    val r = (30f + rnd.nextFloat() * 70f) * u
                    p.color = hsv(hue + rnd.nextFloat() * 60f, 0.45f, 0.95f, 60 + rnd.nextInt(80))
                    for (k in 0 until n) {
                        canvas.save()
                        canvas.rotate(k * 360f / n, cx, cy)
                        canvas.drawOval(cx - r * 0.3f, cy - r, cx + r * 0.3f, cy - r * 0.15f, p)
                        canvas.restore()
                    }
                }
            }
            2 -> { // ribbons
                p.style = Paint.Style.STROKE
                p.strokeCap = Paint.Cap.ROUND
                for (i in 0 until 8) {
                    val y0 = h * (0.05f + rnd.nextFloat() * 0.9f)
                    val path = Path()
                    path.moveTo(-50f, y0)
                    path.cubicTo(w * 0.3f, y0 - h * 0.2f, w * 0.7f, y0 + h * 0.2f, w + 50f, y0 + (rnd.nextFloat() - 0.5f) * h * 0.2f)
                    p.strokeWidth = (2f + rnd.nextFloat() * 6f) * u
                    p.color = hsv(hue + 30f + rnd.nextFloat() * 40f, 0.5f, 0.95f, 80 + rnd.nextInt(120))
                    canvas.drawPath(path, p)
                }
            }
            3 -> { // hills
                for (i in 0 until 4) {
                    p.style = Paint.Style.FILL
                    p.color = hsv(hue + i * 12f, 0.5f, 0.35f + i * 0.15f)
                    val baseY = h * (0.5f + i * 0.14f)
                    val path = Path()
                    path.moveTo(0f, h.toFloat())
                    path.lineTo(0f, baseY)
                    path.cubicTo(w * 0.25f, baseY - h * (0.05f + rnd.nextFloat() * 0.05f), w * 0.5f, baseY + h * 0.03f, w * 0.75f, baseY - h * 0.02f)
                    path.cubicTo(w * 0.85f, baseY - h * 0.05f, w * 0.95f, baseY, w.toFloat(), baseY - h * 0.01f)
                    path.lineTo(w.toFloat(), h.toFloat())
                    path.close()
                    canvas.drawPath(path, p)
                }
            }
            else -> { // geometric stars
                p.style = Paint.Style.STROKE
                p.strokeWidth = 2f * u
                p.color = accent
                p.alpha = 110
                val sz = w / (2.5f + rnd.nextFloat() * 3f)
                var y = 0f
                var row = 0
                while (y < h + sz) {
                    var x = if (row % 2 == 0) 0f else sz / 2f
                    while (x < w + sz) {
                        starShape(canvas, p, x, y, sz * 0.42f)
                        x += sz
                    }
                    y += sz * 0.5f
                    row++
                }
                p.alpha = 255
            }
        }
    }

    // ---------- 16. Arch colonnade in warm light (drawn in code, original) ----------

    private fun arches(canvas: Canvas, w: Int, h: Int, rnd: Random) {
        val u = w / 1080f
        vgrad(canvas, w, h, intArrayOf(col("#3A2C1A"), col("#8A6E47"), col("#1A130B")), floatArrayOf(0f, 0.45f, 1f))
        val vy = h * 0.40f
        glow(canvas, w * 0.5f, vy, w * 0.7f, col("#FFE3A8"), 150)

        // side pillars
        val pillar = Paint(Paint.ANTI_ALIAS_FLAG)
        pillar.color = col("#C8B38E")
        pillar.alpha = 70
        for (side in intArrayOf(-1, 1)) {
            val x = w * 0.5f + side * w * 0.36f
            canvas.drawRect(x - w * 0.05f, 0f, x + w * 0.05f, h.toFloat(), pillar)
        }

        // nested arches, getting smaller towards the vanishing point
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
        stroke.style = Paint.Style.STROKE
        stroke.color = col("#E8D7B0")
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 7) {
            val sc = Math.pow(0.8, i.toDouble()).toFloat()
            val halfW = w * 0.42f * sc
            val baseY = vy + (h * 0.98f - vy) * sc
            val topH = h * 0.55f * sc
            val path = Path()
            path.moveTo(w * 0.5f - halfW, baseY)
            path.lineTo(w * 0.5f - halfW, baseY - topH)
            path.arcTo(RectF(w * 0.5f - halfW, baseY - topH - halfW, w * 0.5f + halfW, baseY - topH + halfW), 180f, 180f)
            path.lineTo(w * 0.5f + halfW, baseY)
            path.close()
            fill.color = col("#1E150B")
            fill.alpha = 90 + i * 12
            canvas.drawPath(path, fill)
            stroke.strokeWidth = (5f - i * 0.5f).coerceAtLeast(1f) * u
            stroke.alpha = 200 - i * 20
            canvas.drawPath(path, stroke)
        }
    }
}
