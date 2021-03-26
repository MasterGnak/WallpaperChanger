package com.example.wallpaperchanger.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.wallpaperchanger.network.Wallpaper

interface RepositoryInterface {
    val images: LiveData<List<Wallpaper>>
    val imagesC: LiveData<List<Wallpaper>>

    suspend fun downloadWallpapers(query: String)

    suspend fun addToCollection(selection: List<Wallpaper>)

    suspend fun clear()

    suspend fun clearC(collection: List<Wallpaper>)
}