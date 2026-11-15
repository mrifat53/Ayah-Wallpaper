package com.rifat.ayahwall

import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.graphics.drawable.GradientDrawable
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// One place for every banner. Replace the test unit id before publishing.
object Ads {
    private const val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    fun banner(c: Context): AdView {
        val v = AdView(c)
        v.setAdSize(AdSize.BANNER)
        v.adUnitId = BANNER_ID
        v.loadAd(AdRequest.Builder().build())
        return v
    }

    // A small rounded card with an "Ad" label and a centred banner
    fun slot(c: Context): LinearLayout {
        val d = c.resources.displayMetrics.density
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER_HORIZONTAL
        box.setPadding(0, (8 * d).toInt(), 0, (8 * d).toInt())
        val bg = GradientDrawable()
        bg.setColor(Theme.col("card"))
        bg.cornerRadius = 14 * d
        bg.setStroke((1 * d).toInt(), Theme.col("border"))
        box.background = bg
        val label = TextView(c)
        label.text = "Ad"
        label.textSize = 10f
        label.setTextColor(Theme.col("muted"))
        label.gravity = Gravity.CENTER
        box.addView(label)
        box.addView(banner(c))
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, (14 * d).toInt())
        box.layoutParams = lp
        return box
    }
}
