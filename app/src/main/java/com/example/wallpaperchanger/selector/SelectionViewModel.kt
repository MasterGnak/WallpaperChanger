package com.example.wallpaperchanger.selector

import android.app.Application
import androidx.lifecycle.*
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application.applicationContext)
    private val repository = Repository(database, application.applicationContext)

    private val _hidden = MutableLiveData(false)
    val hidden: LiveData<Boolean>
        get() = _hidden

    val images = repository.images
    val listSize = repository.listSize
    val count = repository.count

    fun toggleVisibility() {
        _hidden.value = !_hidden.value!!
    }

    private val _menuOpened = MutableLiveData(false)
    val menuOpened: LiveData<Boolean>
        get() = _menuOpened

    fun showMenu() {
        _menuOpened.value = true
    }

    fun hideMenu() {
        _menuOpened.value = false
    }

    fun downloadWp(query: String) {
        viewModelScope.launch {
            toggleVisibility()
            clear()
            repository.downloadWallpapers(query)
        }
    }

    init {
//        if (images.value.isNullOrEmpty()) {
//            viewModelScope.launch {
//                clear()
//                repository.downloadWallpapers()
//            }
//        } else {
//            toggleVisibility()
//        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.imageDao.clear()
        }
    }
}
