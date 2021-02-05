package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(private val database: ImageDatabase, private val context: Context) {

    val images = Transformations.map(database.imageDao.getAll()) {
        it.asWallpaper()
    }

    var listSize = MutableLiveData(-1)

    var count = MutableLiveData(0)



    fun increment() {
        MainScope().launch {
            count.value = count.value?.plus(1)
        }
    }

    suspend fun NetworkWallpaper.validate(context: Context) {
        withContext(Dispatchers.IO) {
            val imgUri = this@validate.contentUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(context).load(imgUri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    increment()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    database.imageDao.insertOne(this@validate.asEntityWallpaper())
                    increment()
                    return false
                }
            }).submit()
        }
    }

    suspend fun downloadWallpapers(query: String) {
        val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall")
        listSize.value = wallpapers.size
        for (wp in wallpapers) {
            wp.validate(context)
        }
    }
}