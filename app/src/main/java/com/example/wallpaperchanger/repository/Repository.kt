package com.example.wallpaperchanger.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import com.example.wallpaperchanger.work.FileWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class Repository(private val database: ImageDatabase, private val context: Context) : RepositoryInterface {

    override val images: LiveData<List<Wallpaper>> = Transformations.map(database.imageDao.getAll()) {
        it.asWallpapers()
    }

    override val imagesC = Transformations.map(database.imageDao.getAllC()) {
        it.asWallpapersC()
    }

    override fun getWallpapers() = database.imageDao.getAllNotLive().asWallpapers()

    override fun getWallpapersC() = database.imageDao.getAllCNotLive().asWallpapersC()

    override suspend fun downloadWallpapers(query: String) {
        clear()
        Log.i("loading", "cleared")
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
        withContext(Dispatchers.Default) {
            val list = database.imageDao.getAllNotLive()
            val collection = database.imageDao.getAllCNotLive()
            val deletion = list.minus(collection)
            Log.i("loading", "size to delete ${deletion.size}")
            deletion.forEach {
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