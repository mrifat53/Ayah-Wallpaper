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
        bg.setColor(Color.parseColor("#141C18"))
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
        tv.setTextColor(Color.parseColor("#8FD3B0"))
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(0, dp(c, 6), 0, dp(c, 8))
        return tv
    }

    fun body(c: Context, text: String, size: Float = 14f, color: String = "#DDDDDD"): TextView {
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
        b.setTextColor(Color.parseColor("#0B0B0B"))
        b.setTypeface(null, Typeface.BOLD)
        val bg = GradientDrawable()
        bg.setColor(Color.parseColor("#D9B65B"))
        bg.cornerRadius = dp(c, 14).toFloat()
        b.background = bg
        b.setOnClickListener { onClick() }
        return b
    }

    // Secondary action: outlined green
    fun outline(c: Context, text: String, onClick: () -> Unit): Button {
        val b = Button(c)
        b.text = text
        b.setTextColor(Color.parseColor("#8FD3B0"))
        val bg = GradientDrawable()
        bg.setColor(Color.TRANSPARENT)
        bg.cornerRadius = dp(c, 14).toFloat()
        bg.setStroke(dp(c, 2), Color.parseColor("#8FD3B0"))
        b.background = bg
        b.setOnClickListener { onClick() }
        return b
    }
}
