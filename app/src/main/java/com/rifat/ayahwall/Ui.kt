package com.rifat.ayahwall

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

// Shared look for every screen: dark rounded cards, one heading style, one body style.
object Ui {

    private fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun card(c: Context, views: List<View>): LinearLayout {
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14))
        val bg = GradientDrawable()
        bg.setColor(Color.parseColor("#161B22"))
        bg.cornerRadius = dp(c, 18).toFloat()
        box.background = bg
        for (v in views) {
            (v.parent as? ViewGroup)?.removeView(v)
            box.addView(v, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, dp(c, 14))
        box.layoutParams = lp
        return box
    }

    fun heading(c: Context, text: String): TextView {
        val tv = TextView(c)
        tv.text = text
        tv.textSize = 14f
        tv.setTextColor(Color.parseColor("#2EA043"))
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(0, dp(c, 6), 0, dp(c, 8))
        return tv
    }

    fun body(c: Context, text: String, size: Float = 14f, color: String = "#E6EDF3"): TextView {
        val tv = TextView(c)
        tv.text = text
        tv.textSize = size
        tv.setTextColor(Color.parseColor(color))
        tv.setPadding(0, dp(c, 4), 0, dp(c, 4))
        return tv
    }

    fun screenTitle(c: Context, text: String): TextView {
        val tv = TextView(c)
        tv.text = text
        tv.textSize = 20f
        tv.setTextColor(Color.WHITE)
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(dp(c, 4), dp(c, 14), dp(c, 4), dp(c, 12))
        return tv
    }

    // Two buttons side by side, used for action rows
    fun row(c: Context, a: View, b: View): LinearLayout {
        val r = LinearLayout(c)
        r.orientation = LinearLayout.HORIZONTAL
        val lpA = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        lpA.setMargins(0, 0, dp(c, 6), 0)
        val lpB = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        lpB.setMargins(dp(c, 6), 0, 0, 0)
        (a.parent as? ViewGroup)?.removeView(a)
        (b.parent as? ViewGroup)?.removeView(b)
        r.addView(a, lpA)
        r.addView(b, lpB)
        return r
    }

    // Main action: filled gold
    fun primary(c: Context, text: String, onClick: () -> Unit): Button {
        val b = Button(c)
        b.text = text
        b.setTextColor(Color.parseColor("#0D1117"))
        b.setTypeface(null, Typeface.BOLD)
        val bg = GradientDrawable()
        bg.setColor(Color.parseColor("#2EA043"))
        bg.cornerRadius = dp(c, 14).toFloat()
        b.background = bg
        b.setOnClickListener { onClick() }
        return b
    }

    // Secondary action: outlined green
    fun outline(c: Context, text: String, onClick: () -> Unit): Button {
        val b = Button(c)
        b.text = text
        b.setTextColor(Color.parseColor("#2EA043"))
        val bg = GradientDrawable()
        bg.setColor(Color.TRANSPARENT)
        bg.cornerRadius = dp(c, 14).toFloat()
        bg.setStroke(dp(c, 2), Color.parseColor("#2EA043"))
        b.background = bg
        b.setOnClickListener { onClick() }
        return b
    }

    // Icon + title, so every settings group is easy to find at a glance
    fun sectionHead(c: Context, icon: Int, text: String): LinearLayout {
        val row = LinearLayout(c)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = android.view.Gravity.CENTER_VERTICAL
        row.setPadding(0, dp(c, 4), 0, dp(c, 8))
        val iv = android.widget.ImageView(c)
        iv.setImageResource(icon)
        iv.setColorFilter(Color.parseColor("#2EA043"))
        row.addView(iv, LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)))
        val tv = TextView(c)
        tv.text = text
        tv.textSize = 15f
        tv.setTextColor(Color.parseColor("#E6EDF3"))
        tv.setTypeface(null, Typeface.BOLD)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp(c, 10), 0, 0, 0)
        row.addView(tv, lp)
        return row
    }

    // Collapsible group, like a dropdown menu: tap the header to open or close it
    fun dropGroup(c: Context, icon: Int, title: String, body: View, open: Boolean): LinearLayout {
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        val bg = GradientDrawable()
        bg.setColor(Color.parseColor("#161B22"))
        bg.cornerRadius = dp(c, 12).toFloat()
        bg.setStroke(dp(c, 1), Color.parseColor("#30363D"))
        box.background = bg
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, dp(c, 12))
        box.layoutParams = lp

        val head = LinearLayout(c)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = android.view.Gravity.CENTER_VERTICAL
        head.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14))
        val iv = android.widget.ImageView(c)
        iv.setImageResource(icon)
        iv.setColorFilter(Color.parseColor("#2EA043"))
        head.addView(iv, LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)))
        val tv = TextView(c)
        tv.text = title
        tv.textSize = 16f
        tv.setTextColor(Color.parseColor("#E6EDF3"))
        tv.setTypeface(null, Typeface.BOLD)
        val tlp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tlp.setMargins(dp(c, 14), 0, 0, 0)
        head.addView(tv, tlp)
        val chev = android.widget.ImageView(c)
        chev.setColorFilter(Color.parseColor("#8B949E"))
        head.addView(chev, LinearLayout.LayoutParams(dp(c, 24), dp(c, 24)))

        val line = View(c)
        line.setBackgroundColor(Color.parseColor("#30363D"))
        val wrap = LinearLayout(c)
        wrap.orientation = LinearLayout.VERTICAL
        wrap.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 14))
        (body.parent as? ViewGroup)?.removeView(body)
        wrap.addView(body, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        fun show(v: Boolean) {
            wrap.visibility = if (v) View.VISIBLE else View.GONE
            line.visibility = if (v) View.VISIBLE else View.GONE
            chev.setImageResource(if (v) R.drawable.ic_expand_less else R.drawable.ic_expand_more)
        }
        show(open)
        head.setOnClickListener { show(wrap.visibility != View.VISIBLE) }

        box.addView(head)
        box.addView(line, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(c, 1)))
        box.addView(wrap)
        return box
    }

    // One tappable row with an icon on the left and an optional control on the right
    fun item(c: Context, icon: Int, label: String, control: View? = null, onClick: (() -> Unit)? = null): LinearLayout {
        val row = LinearLayout(c)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = android.view.Gravity.CENTER_VERTICAL
        row.setPadding(0, dp(c, 10), 0, dp(c, 10))
        val iv = android.widget.ImageView(c)
        iv.setImageResource(icon)
        iv.setColorFilter(Color.parseColor("#8B949E"))
        row.addView(iv, LinearLayout.LayoutParams(dp(c, 22), dp(c, 22)))
        val tv = TextView(c)
        tv.text = label
        tv.textSize = 15f
        tv.setTextColor(Color.parseColor("#E6EDF3"))
        val tlp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tlp.setMargins(dp(c, 14), 0, 0, 0)
        row.addView(tv, tlp)
        if (control != null) {
            (control.parent as? ViewGroup)?.removeView(control)
            row.addView(control)
        }
        if (onClick != null) row.setOnClickListener { onClick() }
        return row
    }
}
