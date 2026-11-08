package com.rifat.ayahwall

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("ayah_prefs", Context.MODE_PRIVATE)
        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad, pad, pad)
        root.setBackgroundColor(Color.parseColor("#0B0B0B"))
        root.fitsSystemWindows = true

        fun label(msg: String, size: Float, color: String): TextView {
            val t = TextView(this)
            t.text = msg
            t.textSize = size
            t.setTextColor(Color.parseColor(color))
            t.setPadding(0, pad / 2, 0, pad / 2)
            return t
        }

        root.addView(label("আয়াত ওয়ালপেপার", 26f, "#FFFFFF"))
        root.addView(label("প্রতি ঘণ্টায় লক স্ক্রিনে নতুন আয়াত: আরবি, বাংলা ও ইংরেজি।", 15f, "#BBBBBB"))

        val sw = Switch(this)
        sw.text = "প্রতি ঘণ্টায় নতুন আয়াত"
        sw.setTextColor(Color.WHITE)
        sw.isChecked = prefs.getBoolean("enabled", false)
        sw.setOnCheckedChangeListener { _, on ->
            prefs.edit().putBoolean("enabled", on).apply()
            if (on) {
                WallpaperWorker.schedule(this)
                WallpaperWorker.runNow(this)
                Toast.makeText(this, "চালু হয়েছে। লক স্ক্রিন দেখুন।", Toast.LENGTH_SHORT).show()
            } else {
                WallpaperWorker.cancel(this)
            }
        }
        root.addView(sw)

        root.addView(label("লেখার অবস্থান (ঘড়ির নিচে ঠিক করুন)", 15f, "#FFFFFF"))
        val bar = SeekBar(this)
        bar.max = 55
        bar.progress = prefs.getInt("y_percent", 32) - 15
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {
                val v = (s?.progress ?: 17) + 15
                prefs.edit().putInt("y_percent", v).apply()
            }
        })
        root.addView(bar)

        val now = Button(this)
        now.text = "এখনই নতুন আয়াত বসান"
        now.setOnClickListener {
            WallpaperWorker.runNow(this)
            Toast.makeText(this, "ওয়ালপেপার বদলানো হচ্ছে...", Toast.LENGTH_SHORT).show()
        }
        root.addView(now)

        root.addView(label("কিছু ফোনে ব্যাটারি সেভার অ্যাপ বন্ধ করে দেয়। নিয়মিত বদলাতে এই অ্যাপের ব্যাটারি সীমা বন্ধ রাখুন।", 13f, "#BBBBBB"))
        val battery = Button(this)
        battery.text = "ব্যাটারি সেটিংস খুলুন"
        battery.setOnClickListener {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
        root.addView(battery)

        root.addView(label("আরবি: Uthmani। বাংলা: মুহিউদ্দীন খান। ইংরেজি: Saheeh International। ডেটা: alquran.cloud", 12f, "#888888"))

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.parseColor("#0B0B0B"))
        scroll.addView(root)
        setContentView(scroll)
    }
}
