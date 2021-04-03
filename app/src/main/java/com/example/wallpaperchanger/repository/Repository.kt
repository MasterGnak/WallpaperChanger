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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import com.example.wallpaperchanger.work.FileWorker
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class Repository(private val database: ImageDatabase, private val context: Context) : RepositoryInterface {

    override val images: LiveData<List<Wallpaper>> = Transformations.map(database.imageDao.getAll()) {
        it.asWallpapers()
    }

    override val imagesC = Transformations.map(database.imageDao.getAllC()) {
        it.asWallpapersC()
    }

    override suspend fun downloadWallpapers(query: String) {
        clear()
        val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall", 36)
        val entityWallpapers = wallpapers.map { EntityWallpaper(it.imageId, it.contentUrl) }
        database.imageDao.insertAll(entityWallpapers)
        val inputData = Data.Builder().putAll(entityWallpapers.associateBy({ it.imageId }, { it.url })).build()
        val request = OneTimeWorkRequestBuilder<FileWorker>().setInputData(inputData).build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            FileWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun addToCollection(selection: List<Wallpaper>) {
        database.imageDao.insertAllC(
            selection.map {
                CollectionWallpaper(it.imageId, it.url)
            }
        )

    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            val collection = database.imageDao.getAllC().value
            list?.minus(collection)?.forEach {
                File(dirPath, (it as EntityWallpaper).imageId).delete()
            }
            database.imageDao.clear()
        }
    }

    override suspend fun clearC(collection: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            collection.minus(list).forEach {
                File(dirPath, (it as Wallpaper).imageId).delete()
            }
            database.imageDao.clearC(collection.map {
                CollectionWallpaper(it.imageId, it.url)
            })
        }
    }

}