package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.dirPath
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

    init {
        dirPath =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/" + context.getString(R.string.app_name) + "/"

    }


    val images = Transformations.map(database.imageDao.getAll()) {
        it.asWallpapers()
    }

    val imagesC = Transformations.map(database.imageDao.getAllC()) {
        it.asWallpapersC()
    }

    var listSize = MutableLiveData(-1)
    var count = MutableLiveData(0)

    private val dir = File(dirPath)

    init {
        Log.i("loading", "path is $dirPath, dir exists: ${dir.exists()}")
    }

    suspend fun downloadWallpapers(query: String) {
        val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall", 35)
        listSize.value = wallpapers.size
        withContext(Dispatchers.IO) {
            database.imageDao.insertAll(wallpapers.validate(context))
        }
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
            downloadImage(imgUri, fileName)
            EntityWallpaper(
                imageId = it.imageId
            )
        }
    }

    private fun downloadImage(uri: Uri, id: String) {
        Glide.with(context).load(uri).into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                saveImage(resource.toBitmap(), dir, id)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                increment(id)
            }
        })
    }

    private fun saveImage(bitmap: Bitmap, dir: File, id: String) {
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
                increment()
            } catch (e: Exception) {
                Log.e("loading", "Failed to save image", e)
            }
        } else {
            Log.e("loading", "Failed to create dir")
        }
    }

    suspend fun addToCollection(selection: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            database.imageDao.insertAllC(
                selection.map {
                    CollectionWallpaper(it.imageId)
                }
            )
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            val collection = database.imageDao.getAllC().value
            list?.minus(collection)?.forEach {
                File(dirPath, (it as EntityWallpaper).imageId).delete()
            }
            database.imageDao.clear()
        }
    }

    suspend fun clearC(collection: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            collection.minus(list).forEach {
                File(dirPath, (it as Wallpaper).imageId).delete()
            }
            database.imageDao.clearC()
        }
    }

}