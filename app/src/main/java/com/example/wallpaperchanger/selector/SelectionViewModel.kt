package com.example.wallpaperchanger.selector

import android.app.Application

import androidx.lifecycle.*
import androidx.recyclerview.selection.Selection
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.EntityWallpaper
import com.example.wallpaperchanger.network.Wallpaper
import com.example.wallpaperchanger.network.asWallpapers
import com.example.wallpaperchanger.network.asWallpapersC
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class SelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application.applicationContext)
    private val repository = Repository(database, application.applicationContext)

    private val _hidden = MutableLiveData(false)
    val hidden: LiveData<Boolean>
        get() = _hidden

    val images = repository.images
    val listSize = repository.listSize
    val count = repository.count

    var menuOpened = false
    var severalSelected = false

    fun toggleVisibility() {
        _hidden.value = !_hidden.value!!
    }

    fun resetCount() {
        count.value = 0
    }

    fun downloadWp(query: String) {
        viewModelScope.launch {
            toggleVisibility()
            clear()
            repository.downloadWallpapers(query)
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            val collection = database.imageDao.getAllC().value
            list?.minus(collection)?.forEach {
                File(dirPath, (it as EntityWallpaper).imageId).delete()
            }
            database.imageDao.clear()
        }
    }

    fun addToCollection(selection: List<Wallpaper>) {
        viewModelScope.launch {
            repository.addToCollection(selection)
        }
    }

}
