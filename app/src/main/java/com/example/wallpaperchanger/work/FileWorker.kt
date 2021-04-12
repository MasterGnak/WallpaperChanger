package com.example.wallpaperchanger.work

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.example.wallpaperchanger.ServiceLocator
import com.example.wallpaperchanger.dirPath
import java.io.File
import java.io.FileOutputStream

class FileWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    private val dir = File(dirPath)
    private val repository = ServiceLocator.provideRepository(appContext)

    companion object {
        const val WORK_NAME = "FileWorker"
    }

    override fun doWork(): Result {
        val wallpapers = repository.getWallpapers()
        wallpapers.forEach {
            downloadImage(it.imageId, it.url)
        }
        return Result.success()
    }

    private fun downloadImage(id: String, url: String) {
        val uri = url.toUri().buildUpon().scheme("https").build()
        val bitmap = Glide.with(applicationContext).asBitmap().load(uri).submit()
        saveImage(bitmap.get(), id)
    }

    private fun saveImage(bitmap: Bitmap, id: String) {
        val dirCreated = if (!dir.exists()) {
            dir.mkdir()
        } else {
            true
        }
        if (dirCreated) {
            try {
                val fOut = FileOutputStream(File(dir, id))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                Log.i("loading", "image saved")
                fOut.close()
            } catch (e: Exception) {
                Log.e("loading", "Failed to save image", e)
            }
        } else {
            Log.e("loading", "Failed to create dir")
        }
    }
}