package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class Repository(private val database: ImageDatabase, private val context: Context) {

    val images = Transformations.map(database.imageDao.getAll()) {
        it.asWallpapers()
    }

    var listSize = MutableLiveData(-1)

    var count = MutableLiveData(0)

    val dirPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/" + context.getString(R.string.app_name) + "/"
    private val dir = File(dirPath)
    private var dirCreated = false

    init {
        Log.i("loading", "path is $dirPath, dir exists: ${dir.exists()}")
    }


    fun increment(vararg id: String) {
        MainScope().launch {
            count.value = count.value?.plus(1)
            if (id.isNotEmpty()) {
                remove(id[0])
            }
        }
    }

    private suspend fun remove(id: String) {
        withContext(Dispatchers.IO) {
            database.imageDao.remove(id)
        }
    }

    private fun List<NetworkWallpaper>.validate(context: Context): List<EntityWallpaper> {

        return map {
            val fileName = it.imageId
            val imgUri = it.contentUrl.toUri().buildUpon().scheme("https").build()
            val imageFile = File(dir, fileName)
            val path = imageFile.absolutePath
            downloadImage(imgUri, imageFile, fileName)
            EntityWallpaper(
                imageId = it.imageId,
                path = path
            )
        }
    }

    suspend fun downloadWallpapers(query: String) {
            val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall", 35)
            listSize.value = wallpapers.size
            withContext(Dispatchers.IO) {
                database.imageDao.insertAll(wallpapers.validate(context))
            }
    }

    private fun downloadImage(uri: Uri, file: File, id: String) {
        Glide.with(context).load(uri).into(object: CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                saveImage(resource.toBitmap(), file)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                increment(id)
            }
        })
    }

    private fun saveImage(bitmap: Bitmap, imageFile: File) {
        dirCreated = if (!dir.exists()) {
            dir.mkdir()
        } else {
            true
        }
        if (dirCreated) {
            try {
                val fOut = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                Log.i("loading", "image saved")
                fOut.close()
                increment()
            } catch (e: Exception) {
                Log.e("loading", "Failed to save image", e)
            }
        } else {
            Log.e("loading", "Failed to create dir")
        }
    }
}