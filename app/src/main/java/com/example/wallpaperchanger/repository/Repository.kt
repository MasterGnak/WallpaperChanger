package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(private val database: ImageDatabase, private val context: Context) {


    private var databaseSource = true

    val images = MediatorLiveData<List<Wallpaper>>()

    private val databaseImages = Transformations.map(database.imageDao.getAll()) {
        it.asWallpaper()
    }

    private val downloadedImages = MutableLiveData<MutableList<Wallpaper>>(mutableListOf())

    var listSize = MutableLiveData(-1)

    var count = MutableLiveData(0)

    init {
        images.addSource(databaseImages) { images.value = it }
    }


    fun increment(wp: Wallpaper?) {
        MainScope().launch {
            count.value = count.value?.plus(1)
            if (wp != null) {
                downloadedImages.value!!.add(wp)
//                withContext(Dispatchers.IO) {
//                    database.imageDao.insertOne(wp)
//                }
            }
        }
    }

    suspend fun insertWp() {
        withContext(Dispatchers.IO) {
            database.imageDao.insertAll(downloadedImages.value!!.asEntityWps())
        }
    }

    suspend fun NetworkWallpaper.validate(context: Context) {
        withContext(Dispatchers.IO) {
            val imgUri = this@validate.contentUrl.toUri().buildUpon().scheme("https").build()
            val img = Glide.with(context).load(imgUri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    increment(null)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    increment(this@validate.asWallpaper())
                    return false
                }
            }).submit()
        }
    }

    suspend fun downloadWallpapers(query: String) {
        downloadedImages.value = mutableListOf()
        val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall", 100)
        if (databaseSource) {
            images.removeSource(databaseImages)
            images.addSource(downloadedImages) {images.value = it}
            databaseSource = false
        }
        listSize.value = wallpapers.size
        for (wp in wallpapers) {
            wp.validate(context)
        }
    }
}

class WpTarget: CustomTarget<Drawable>() {

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        TODO("Not yet implemented")
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        TODO("Not yet implemented")
    }
}