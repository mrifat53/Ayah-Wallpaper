package com.rifat.ayahwall

import android.app.WallpaperManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class WallpaperWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val c = applicationContext
        val prefs = c.getSharedPreferences("ayah_prefs", Context.MODE_PRIVATE)
        val ayah = AyahFetcher.next(c) ?: return Result.retry()
        val y = prefs.getInt("y_percent", 32)
        val bmp = WallpaperPainter.paint(c, ayah, y)
        return try {
            WallpaperManager.getInstance(c).setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val NAME = "ayah_hourly"

        fun schedule(c: Context) {
            val req = PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(c)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        fun cancel(c: Context) {
            WorkManager.getInstance(c).cancelUniqueWork(NAME)
        }

        fun runNow(c: Context) {
            WorkManager.getInstance(c).enqueue(OneTimeWorkRequestBuilder<WallpaperWorker>().build())
        }
    }
}
