package com.example.wallpaperchanger.selector

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.ImageDatabase
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _loadedCount = MutableLiveData<Int>(0)
    val loadedCount: LiveData<Int>
        get() = _loadedCount

    var listSize = -1


    private val database = getDatabase(application.applicationContext)
    private val repository = Repository(database, application.applicationContext)

    private val _hidden = MutableLiveData<Boolean>(true)
    val hidden: LiveData<Boolean>
        get() = _hidden

    val images = Transformations.map(database.imageDao.getAll()) {
        it.asWallpaper()
    }

    fun toggleVisibility() {
        _hidden.value = !_hidden.value!!
    }

    init {
        viewModelScope.launch {
            refreshWallpapers()
        }
    }

    fun insertOne(nw: NetworkWallpaper) {
        viewModelScope.launch {
            insert(nw.asEntityWallpaper())
            _loadedCount.value = _loadedCount.value?.plus(1)
        }
    }

    private suspend fun insert(wp: EntityWallpaper) {
        withContext(Dispatchers.IO) {
            database.imageDao.insertOne(wp)
        }
    }

    private fun loadFailed() {
        viewModelScope.launch {
            _loadedCount.value = _loadedCount.value?.plus(1)
        }
    }

    suspend fun NetworkWallpaper.validate(context: Context) {
        withContext(Dispatchers.IO) {
            val imgUri = this@validate.contentUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(context).load(imgUri).listener(object :RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadFailed()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    insertOne(this@validate)
                    return false
                }
            }).submit()
        }
    }

    private suspend fun refreshWallpapers() {
        viewModelScope.launch {
            clear()
            val wallpapers = Api.retrofitService.getImages("Phone wallpaper", "Wallpaper", "Tall")
            listSize = wallpapers.size
            for (wp in wallpapers) {
                wp.validate(context)
            }
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.imageDao.clear()
        }
    }
}