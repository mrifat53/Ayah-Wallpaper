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
        val ayah = AyahSource.next(c) ?: return Result.retry()
        val dm = c.resources.displayMetrics
        val bmp = WallpaperPainter.paint(c, ayah, dm.widthPixels, dm.heightPixels, Prefs.yPercent(c), false)
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
            val req = PeriodicWorkRequestBuilder<WallpaperWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(c)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        // Keeps the existing schedule if one is already running
        fun ensure(c: Context) {
            val req = PeriodicWorkRequestBuilder<WallpaperWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(c).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun cancel(c: Context) {
            WorkManager.getInstance(c).cancelUniqueWork(NAME)
        }

        fun runNow(c: Context) {
            WorkManager.getInstance(c).enqueue(OneTimeWorkRequestBuilder<WallpaperWorker>().build())
        }
    }
}
