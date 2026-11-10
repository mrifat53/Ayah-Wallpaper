package com.rifat.ayahwall

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

data class Ayah(
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val bangla: String,
    val english: String
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("s", surah)
        o.put("a", ayah)
        o.put("ar", arabic)
        o.put("bn", bangla)
        o.put("en", english)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): Ayah =
            Ayah(o.getInt("s"), o.getInt("a"), o.getString("ar"), o.optString("bn"), o.optString("en"))
    }
}

object AyahSource {

    // Footnote markers like [1] or [১] are part of the translation text.
    // We never edit the text; we just prefer ayahs without them.
    private val marker = Regex("\\[[0-9\u09E6-\u09EF]+\\]")

    private fun http(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) {
                conn.disconnect()
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            body
        } catch (e: Exception) {
            null
        }
    }

    private fun surahCounts(c: Context): IntArray? {
        val sp = Prefs.sp(c)
        val cached = sp.getString("counts", null)
        if (cached != null) {
            val parts = cached.split(",")
            if (parts.size == 114) return IntArray(114) { parts[it].toInt() }
        }
        val body = http("https://api.alquran.cloud/v1/surah") ?: return null
        return try {
            val data = JSONObject(body).getJSONArray("data")
            if (data.length() != 114) return null
            val counts = IntArray(114) { data.getJSONObject(it).getInt("numberOfAyahs") }
            sp.edit().putString("counts", counts.joinToString(",")).apply()
            counts
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRefs(s: String): List<Pair<Int, Int>> {
        return s.split(",").mapNotNull { p ->
            val x = p.split(":")
            if (x.size == 2) Pair(x[0].toInt(), x[1].toInt()) else null
        }
    }

    private fun juzRefs(c: Context, juz: Int): List<Pair<Int, Int>>? {
        val sp = Prefs.sp(c)
        val key = "juz_$juz"
        val cached = sp.getString(key, null)
        if (cached != null) return parseRefs(cached)
        val body = http("https://api.alquran.cloud/v1/juz/$juz/quran-uthmani") ?: return null
        return try {
            val ayahs = JSONObject(body).getJSONObject("data").getJSONArray("ayahs")
            val sb = StringBuilder()
            for (i in 0 until ayahs.length()) {
                val o = ayahs.getJSONObject(i)
                if (i > 0) sb.append(',')
                sb.append(o.getJSONObject("surah").getInt("number"))
                sb.append(':')
                sb.append(o.getInt("numberInSurah"))
            }
            sp.edit().putString(key, sb.toString()).apply()
            parseRefs(sb.toString())
        } catch (e: Exception) {
            null
        }
    }

    // The first ayah of a surah can come with the Bismillah attached, so we skip those.
    private fun okFirst(s: Int, a: Int): Boolean = a > 1 || s == 1 || s == 9

    private fun pickRef(c: Context, counts: IntArray): Pair<Int, Int>? {
        val cat = Prefs.category(c)
        val param = Prefs.catParam(c)
        return when (cat) {
            "surah" -> {
                val s = param.coerceIn(1, 114)
                val n = counts[s - 1]
                val lo = if (s == 1 || s == 9) 1 else 2
                Pair(s, Random.nextInt(lo, n + 1))
            }
            "juz" -> {
                val refs = juzRefs(c, param.coerceIn(1, 30)) ?: return null
                val good = refs.filter { okFirst(it.first, it.second) }
                val pool = if (good.isEmpty()) refs else good
                pool[Random.nextInt(pool.size)]
            }
            "theme" -> {
                val key = I18n.themeKeys[param.coerceIn(0, I18n.themeKeys.size - 1)]
                val refs = I18n.themeRefs[key] ?: return null
                refs[Random.nextInt(refs.size)]
            }
            else -> {
                val total = counts.sum()
                var result = Pair(1, 2)
                for (i in 0 until 6) {
                    var r = Random.nextInt(total)
                    var s = 0
                    while (r >= counts[s]) {
                        r -= counts[s]
                        s++
                    }
                    if (okFirst(s + 1, r + 1)) {
                        result = Pair(s + 1, r + 1)
                        break
                    }
                }
                result
            }
        }
    }

    private fun fetchArabic(s: Int, a: Int): String? {
        val body = http("https://api.alquran.cloud/v1/ayah/$s:$a/quran-uthmani") ?: return null
        return try {
            JSONObject(body).getJSONObject("data").getString("text")
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchTrans(key: String, s: Int, a: Int): String {
        val body = http("https://quranenc.com/api/v1/translation/aya/$key/$s/$a") ?: return ""
        return try {
            val root = JSONObject(body)
            val r = root.opt("result")
            val o: JSONObject? = when {
                r is JSONObject -> r
                r is JSONArray && r.length() > 0 -> r.optJSONObject(0)
                root.has("translation") -> root
                else -> null
            }
            o?.optString("translation", "")?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun fetchOne(c: Context): Ayah? {
        val counts = surahCounts(c) ?: return null
        val langs = Prefs.langs(c)
        val limit = 260 * langs.size
        for (attempt in 0 until 8) {
            val ref = pickRef(c, counts) ?: return null
            val ar = fetchArabic(ref.first, ref.second) ?: continue
            val bn = fetchTrans("bengali_zakaria", ref.first, ref.second)
            val en = fetchTrans("english_saheeh", ref.first, ref.second)
            if (bn.isBlank() && en.isBlank()) continue
            if (attempt < 7) {
                if (marker.containsMatchIn(bn) || marker.containsMatchIn(en)) continue
                var len = 0
                if ("ar" in langs) len += ar.length
                if ("bn" in langs) len += bn.length
                if ("en" in langs) len += en.length
                if (len > limit) continue
            }
            return Ayah(ref.first, ref.second, ar, bn, en)
        }
        return null
    }

    // Gets a new ayah, or an old saved one if there is no internet.
    fun next(c: Context): Ayah? {
        val a = fetchOne(c)
        if (a != null) {
            addHistory(c, a)
            return a
        }
        val h = history(c)
        return if (h.isEmpty()) null else h[Random.nextInt(h.size)]
    }

    private fun readList(c: Context, key: String): List<Ayah> {
        val s = Prefs.sp(c).getString(key, "[]") ?: "[]"
        return try {
            val arr = JSONArray(s)
            (0 until arr.length()).map { Ayah.fromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeList(c: Context, key: String, list: List<Ayah>) {
        val arr = JSONArray()
        for (a in list) arr.put(a.toJson())
        Prefs.sp(c).edit().putString(key, arr.toString()).apply()
    }

    fun history(c: Context): List<Ayah> = readList(c, "history2")
    fun favorites(c: Context): List<Ayah> = readList(c, "favs2")
    fun current(c: Context): Ayah? = history(c).firstOrNull()

    fun addHistory(c: Context, a: Ayah) {
        val l = ArrayList<Ayah>()
        l.add(a)
        for (x in history(c)) {
            if (!(x.surah == a.surah && x.ayah == a.ayah)) l.add(x)
        }
        writeList(c, "history2", l.take(40))
    }

    fun setCurrentFirst(c: Context, a: Ayah) = addHistory(c, a)

    fun isFav(c: Context, a: Ayah): Boolean =
        favorites(c).any { it.surah == a.surah && it.ayah == a.ayah }

    fun toggleFav(c: Context, a: Ayah): Boolean {
        val f = ArrayList(favorites(c))
        val idx = f.indexOfFirst { it.surah == a.surah && it.ayah == a.ayah }
        return if (idx >= 0) {
            f.removeAt(idx)
            writeList(c, "favs2", f)
            false
        } else {
            f.add(0, a)
            writeList(c, "favs2", f)
            true
        }
    }
}
