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
import com.example.wallpaperchanger.repository.RepositoryInterface
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class SelectionViewModel(private val repository: RepositoryInterface) : ViewModel() {

    private val _hidden = MutableLiveData(false)
    val hidden: LiveData<Boolean>
        get() = _hidden

    val images = repository.images
    //val listSize = repository.listSize
    //val count = repository.count

    var menuOpened = false
    var severalSelected = false

    fun toggleVisibility() {
        _hidden.value = !_hidden.value!!
    }

//    fun resetCount() {
//        count.value = 0
//    }

    fun downloadWp(query: String) {
        viewModelScope.launch {
            //toggleVisibility()
            repository.downloadWallpapers(query)
        }
    }

    fun addToCollection(selection: List<Wallpaper>) {
        viewModelScope.launch {
            repository.addToCollection(selection)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class SelectionViewModelFactory (private val repository: RepositoryInterface): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return (SelectionViewModel(repository) as T)
        }
    }
}
