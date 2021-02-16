package com.example.wallpaperchanger.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.network.Wallpaper
import com.example.wallpaperchanger.network.asWallpapers
import com.example.wallpaperchanger.network.asWallpapersC
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val SWITCH = "switch"
const val LIST = "list"
const val FREQ = "frequency"
const val LISTPOS = "listPos"

class WpWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val search = appContext.getString(R.string.search)
    private val collection = appContext.getString(R.string.collection)
    private var colPos = 0
    private val wpManager = WallpaperManager.getInstance(appContext)
    private val database = getDatabase(applicationContext)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
    private val repo = Repository(database, appContext)

    companion object {
        const val WORK_NAME = "WpWorker"
    }

    override fun doWork(): Result {
        Log.i("work", "work attempted")
        try {
            val list = inputData.getString(LIST)
            val listPos = prefs.getInt(LISTPOS, 0)
            if (list == search) {
                val images = database.imageDao.getAllNotLive().asWallpapers()
                Log.i("work", "list is null or empty ${images.isNullOrEmpty()}")
                if (!images.isNullOrEmpty()) {
                    val bitmap = Glide.with(applicationContext).asBitmap().load(images[listPos].uri).submit()
                    wpManager.setBitmap(bitmap.get())
                    Log.i("work", "wp updated with position $listPos")
                    if (listPos == images.lastIndex) prefs.edit().putInt(LISTPOS, 0).commit()
                    else prefs.edit().putInt(LISTPOS, listPos+1).commit()
                }
            } else if (list == collection) {
                val col = database.imageDao.getAllCNotLive().asWallpapersC()
                if (!col.isNullOrEmpty()) {
                    val bitmap = Glide.with(applicationContext).asBitmap().load(col[colPos].uri).submit()
                    wpManager.setBitmap(bitmap.get())
                    if (colPos == col.lastIndex) colPos = 0
                    else colPos++
                }
            }
            showNotification()
            return Result.success()
        } catch (e: Exception) {
            Log.i("work", "work failed", e)
            return Result.failure()
        }
    }

    private fun showNotification() {
        val channel = NotificationChannel(CHANNEL_ID, "chan", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("This is a test")
            .setSmallIcon(R.drawable.loading_img)
            .setContentText("Test")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(1, builder.build())
    }
}