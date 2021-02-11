package com.example.wallpaperchanger.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.room.getDatabase

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application.applicationContext)
    private val repository = Repository(database, application.applicationContext)

    val images = repository.imagesC
}