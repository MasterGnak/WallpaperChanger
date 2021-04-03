package com.example.wallpaperchanger.selector

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

import androidx.lifecycle.*
import androidx.recyclerview.selection.Selection
import com.bumptech.glide.Glide
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.EntityWallpaper
import com.example.wallpaperchanger.network.Wallpaper
import com.example.wallpaperchanger.network.asWallpapers
import com.example.wallpaperchanger.network.asWallpapersC
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.repository.RepositoryInterface
import com.example.wallpaperchanger.room.getDatabase
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File

class SelectionViewModel(private val repository: RepositoryInterface) : ViewModel() {

    val images = repository.images

    var menuOpened = false
    var severalSelected = false

    fun downloadWp(query: String) {
        viewModelScope.launch {
            repository.downloadWallpapers(query)
        }
    }

    fun addToCollection(selection: List<Wallpaper>) {
        viewModelScope.launch {
            repository.addToCollection(selection)
        }
    }

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

    @Suppress("UNCHECKED_CAST")
    class SelectionViewModelFactory (private val repository: RepositoryInterface): ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return (SelectionViewModel(repository) as T)
        }
    }
}
