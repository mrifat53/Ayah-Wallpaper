package com.rifat.ayahwall

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

data class Ayah(
    val arabic: String,
    val bangla: String,
    val english: String,
    val surahName: String,
    val surahNumber: Int,
    val ayahNumber: Int
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("ar", arabic)
        o.put("bn", bangla)
        o.put("en", english)
        o.put("sn", surahName)
        o.put("s", surahNumber)
        o.put("a", ayahNumber)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): Ayah {
            return Ayah(
                o.getString("ar"),
                o.getString("bn"),
                o.getString("en"),
                o.getString("sn"),
                o.getInt("s"),
                o.getInt("a")
            )
        }
    }
}

object AyahFetcher {
    private const val PREFS = "ayah_prefs"
    private const val CACHE_KEY = "cache"
    private const val MAX_CACHE = 20
    private const val MAX_CHARS = 380

    // Returns a new ayah from the internet, or a saved one if offline.
    fun next(ctx: Context): Ayah? {
        for (i in 1..6) {
            val ayah = fetchOnline() ?: break
            val total = ayah.arabic.length + ayah.bangla.length + ayah.english.length
            if (total <= MAX_CHARS || i == 6) {
                save(ctx, ayah)
                return ayah
            }
        }
        return fromCache(ctx)
    }

    private fun fetchOnline(): Ayah? {
        return try {
            val n = Random.nextInt(1, 6237)
            val url = URL("https://api.alquran.cloud/v1/ayah/$n/editions/quran-uthmani,bn.bengali,en.sahih")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val data = JSONObject(body).getJSONArray("data")
            var ar = ""
            var bn = ""
            var en = ""
            var sn = ""
            var s = 0
            var a = 0
            for (i in 0 until data.length()) {
                val o = data.getJSONObject(i)
                val id = o.getJSONObject("edition").getString("identifier")
                val text = o.getString("text")
                if (id == "quran-uthmani") ar = text
                if (id == "bn.bengali") bn = text
                if (id == "en.sahih") en = text
                val surah = o.getJSONObject("surah")
                sn = surah.getString("englishName")
                s = surah.getInt("number")
                a = o.getInt("numberInSurah")
            }
            if (ar.isBlank() || bn.isBlank() || en.isBlank()) null else Ayah(ar, bn, en, sn, s, a)
        } catch (e: Exception) {
            null
        }
    }

    private fun save(ctx: Context, ayah: Ayah) {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(p.getString(CACHE_KEY, "[]"))
        arr.put(ayah.toJson())
        val trimmed = JSONArray()
        val start = maxOf(0, arr.length() - MAX_CACHE)
        for (i in start until arr.length()) trimmed.put(arr.get(i))
        p.edit().putString(CACHE_KEY, trimmed.toString()).apply()
    }

    private fun fromCache(ctx: Context): Ayah? {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray(p.getString(CACHE_KEY, "[]"))
        if (arr.length() == 0) return null
        return Ayah.fromJson(arr.getJSONObject(Random.nextInt(arr.length())))
    }
}
