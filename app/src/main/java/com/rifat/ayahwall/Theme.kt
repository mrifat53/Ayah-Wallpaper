package com.rifat.ayahwall

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

// Colours follow the phone: dark mode gives the dark palette, light mode gives the light one.
// Every activity calls refresh() first, so a change in system theme is picked up on the next screen.
object Theme {

    var night = true

    private val dark = mapOf(
        "bg" to "#0D1117", "card" to "#161B22", "border" to "#30363D",
        "text" to "#E6EDF3", "muted" to "#8B949E", "accent" to "#2EA043"
    )
    private val light = mapOf(
        "bg" to "#F6F8FA", "card" to "#FFFFFF", "border" to "#D0D7DE",
        "text" to "#1F2328", "muted" to "#656D76", "accent" to "#1A7F37"
    )

    fun refresh(c: Context) {
        val mode = c.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        night = mode != Configuration.UI_MODE_NIGHT_NO
    }

    fun hex(name: String): String = (if (night) dark else light)[name] ?: "#000000"

    fun col(name: String): Int = Color.parseColor(hex(name))
}
