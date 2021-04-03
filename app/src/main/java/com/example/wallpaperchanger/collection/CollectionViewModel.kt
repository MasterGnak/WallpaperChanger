package com.example.wallpaperchanger.collection

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
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

    fun updateWp(wallpaper: Wallpaper, context: Context) {
        viewModelScope.launch {
            update(wallpaper, context)
        }
    }

    private suspend fun update(wallpaper: Wallpaper, context: Context) {
        withContext(Dispatchers.IO) {
            val file = File(dirPath + wallpaper.imageId)
            val uri = if (file.exists()) {
                Uri.fromFile(file)
            } else {
                wallpaper.url.toUri().buildUpon()?.scheme("https")?.build()
            }
            try {
                val bitmap = Glide.with(context).asBitmap().load(uri).submit()
                WallpaperManager.getInstance(context).setBitmap(bitmap.get())
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Обои обновлены", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка при обновлении обоев", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearC(collection: List<Wallpaper>) {
        viewModelScope.launch {
            repository.clearC(collection)
        }
    }
}