package com.example.wallpaperchanger

import android.app.Application
import com.example.wallpaperchanger.repository.RepositoryInterface

lateinit var dirPath: String

class WpApplication: Application() {

    val repository: RepositoryInterface
        get() = ServiceLocator.provideRepository(this)

    override fun onCreate() {
        super.onCreate()
        dirPath = ServiceLocator.getPath(this)
    }
}