package com.example.wallpaperchanger.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.CollectionWallpaper
import com.example.wallpaperchanger.network.EntityWallpaper
import com.example.wallpaperchanger.network.Wallpaper
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application.applicationContext)
    private val repository = Repository(database, application.applicationContext)

    val images = repository.imagesC

    var menuOpened = false
    var severalSelected = false

//    suspend fun clear(collection: List<Wallpaper>) {
//        withContext(Dispatchers.IO) {
//            val list = database.imageDao.getAll().value
//            collection.minus(list).forEach {
//                File(dirPath, (it as Wallpaper).imageId).delete()
//            }
//            database.imageDao.clearC()
//        }
//    }

    fun clearC(collection: List<Wallpaper>) {
        viewModelScope.launch {
            repository.clearC(collection)
        }
    }
}