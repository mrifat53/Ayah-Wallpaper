package com.rifat.ayahwall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Prayer times from the free Aladhan API. Location is used only for the calculation.
object PrayerTimes {

    val order = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    fun hasPermission(c: Context): Boolean =
        c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun lastLocation(c: Context): Location? {
        if (!hasPermission(c)) return null
        val lm = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var best: Location? = null
        for (p in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)) {
            val l = try { lm.getLastKnownLocation(p) } catch (e: Exception) { null }
            val cur = best
            if (l != null && (cur == null || l.time > cur.time)) best = l
        }
        return best
    }

    fun fetch(lat: Double, lng: Double): List<Pair<String, String>>? {
        val date = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val body = http("https://api.aladhan.com/v1/timings/$date?latitude=$lat&longitude=$lng") ?: return null
        return try {
            val t = JSONObject(body).getJSONObject("data").getJSONObject("timings")
            order.map { Pair(it, t.getString(it).substringBefore(" ")) }
        } catch (e: Exception) {
            null
        }
    }

    private fun http(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
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

    private fun minutes(hhmm: String): Int {
        val p = hhmm.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    // Index of the next prayer. After Isha, the next one is Fajr tomorrow.
    fun nextIndex(times: List<Pair<String, String>>, now: Calendar): Int {
        val cur = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        for (i in times.indices) {
            if (minutes(times[i].second) > cur) return i
        }
        return 0
    }
}
