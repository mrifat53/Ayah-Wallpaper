package com.rifat.ayahwall

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
}
