package com.example.wallpaperchanger.work

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.ServiceLocator
import com.example.wallpaperchanger.dirPath
import java.io.File
import kotlin.random.Random

const val SWITCH = "switch"
const val LIST = "list"
const val FREQ = "frequency"

class WpWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val search = appContext.getString(R.string.search)
    private val collection = appContext.getString(R.string.collection)
    private val path = dirPath

    private val wpManager = WallpaperManager.getInstance(appContext)
    private val repository = ServiceLocator.provideRepository(appContext)

    companion object {
        const val WORK_NAME = "WpWorker"
    }

    override fun doWork(): Result {
        try {
            val list = inputData.getString(LIST)
            if (list == search) {
                val images = repository.getWallpapers()
                val image = images[Random.nextInt(0, images.size)]
                val file = File(path + image.imageId)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                wpManager.setBitmap(bitmap)
            } else if (list == collection) {
                val col = repository.getWallpapersC()
                val image = col[Random.nextInt(0, col.size)]
                val file = File(path + image.imageId)
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                wpManager.setBitmap(bitmap)
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}