package com.example.wallpaperchanger.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
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
import kotlin.random.Random

const val SWITCH = "switch"
const val LIST = "list"
const val FREQ = "frequency"
const val LISTPOS = "listPos"

class WpWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val search = appContext.getString(R.string.search)
    private val collection = appContext.getString(R.string.collection)
    @VisibleForTesting
    private val list = inputData.getStringArray(LIST)

    private val wpManager = WallpaperManager.getInstance(appContext)
    private val database = getDatabase(appContext)
    //private val repo = Repository(database, appContext)

    companion object {
        const val WORK_NAME = "WpWorker"
    }

    override fun doWork(): Result {
        try {
            val list = inputData.getString(LIST)
            if (list == search) {
                val images = database.imageDao.getAllNotLive().asWallpapers()
                val listPos = Random.nextInt(0, images.size)
                if (!images.isNullOrEmpty()) {
                    val uri = images[listPos].url.toUri().buildUpon().scheme("https").build()
                    val bitmap = Glide.with(applicationContext).asBitmap().load(uri).submit()
                    wpManager.setBitmap(bitmap.get())
                }
            } else if (list == collection) {
                val col = database.imageDao.getAllCNotLive().asWallpapersC()
                val colPos = Random.nextInt(0, col.size)
                if (!col.isNullOrEmpty()) {
                    val uri = col[colPos].url.toUri().buildUpon().scheme("https").build()
                    val bitmap = Glide.with(applicationContext).asBitmap().load(uri).submit()
                    wpManager.setBitmap(bitmap.get())
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}