package com.rifat.ayahwall

import android.graphics.Typeface

// Picks the background and the text font from the ayah itself.
// An ayah that belongs to a theme (patience, gratitude, hope, dua, trust) gets a matching look.
// Other ayahs get an endless design from their own number, so they still differ from each other.
object Mood {

    fun theme(a: Ayah): String? =
        I18n.themeKeys.firstOrNull { k -> I18n.themeRefs[k]?.contains(Pair(a.surah, a.ayah)) == true }

    // userStyle 0 means "by ayah". Any other choice from settings wins.
    fun styleFor(a: Ayah, userStyle: Int): Int {
        if (userStyle != 0) return userStyle
        // The detailed looks (night sky, pattern, green dome, mihrab) are used by default.
        // They look clearer than the simple abstract shapes.
        return when (theme(a)) {
            "patience", "tawakkul" -> 1    // night sky with mosque and moon
            "gratitude" -> 2               // geometric pattern
            "hope" -> 4                    // green dome at dusk
            "dua" -> 5                     // mihrab arch with lantern
            else -> intArrayOf(1, 2, 3, 4)[(a.surah * 31 + a.ayah) % 4]  // Kaaba and the rest, varied per ayah
        }
    }

    fun typeface(a: Ayah): Typeface = when (theme(a)) {
        "dua" -> Typeface.create("serif", Typeface.NORMAL)
        "gratitude" -> Typeface.create("serif", Typeface.BOLD)
        "patience", "tawakkul" -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        "hope" -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        else -> Typeface.DEFAULT
    }
}
