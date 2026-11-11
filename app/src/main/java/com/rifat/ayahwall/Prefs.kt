package com.rifat.ayahwall

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object Prefs {
    fun sp(c: Context): SharedPreferences =
        c.getSharedPreferences("ayah_prefs", Context.MODE_PRIVATE)

    fun uiLang(c: Context): String {
        val saved = sp(c).getString("ui_lang", null)
        if (saved != null) return saved
        return when (Locale.getDefault().language) {
            "ar" -> "ar"
            "bn" -> "bn"
            else -> "en"
        }
    }

    fun langs(c: Context): Set<String> {
        val s = sp(c).getString("langs", "ar,bn,en") ?: "ar,bn,en"
        val set = s.split(",").filter { it.isNotBlank() }.toSet()
        return if (set.isEmpty()) setOf("ar") else set
    }

    fun setLangs(c: Context, set: Set<String>) {
        sp(c).edit().putString("langs", set.joinToString(",")).apply()
    }

    fun category(c: Context): String = sp(c).getString("cat", "all") ?: "all"
    fun catParam(c: Context): Int = sp(c).getInt("cat_param", 1)
    fun style(c: Context): Int = sp(c).getInt("style", 0)
    fun yPercent(c: Context): Int = sp(c).getInt("y_percent", 32)
    fun enabled(c: Context): Boolean = sp(c).getBoolean("enabled", true)
}
