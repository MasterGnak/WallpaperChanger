package com.example.wallpaperchanger

import android.content.Context
import android.os.Environment
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.wallpaperchanger.repository.Repository
import com.example.wallpaperchanger.repository.RepositoryInterface
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.runBlocking

object ServiceLocator {
    private var database: ImageDatabase? = null

    @Volatile
    var repository: RepositoryInterface? = null

    fun provideRepository(context: Context): RepositoryInterface {
        synchronized(this) {
            return repository?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): RepositoryInterface {
        val newRepo = Repository(provideDatabase(context), context)
        repository = newRepo
        return newRepo
    }

    fun getPath(context: Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/" + context.getString(R.string.app_name) + "/"
    }

    private fun provideDatabase(context: Context): ImageDatabase {
        return this.database ?: createDatabase(context)
    }

    private fun createDatabase(context: Context): ImageDatabase {
        val result = Room.databaseBuilder(context.applicationContext,
            ImageDatabase::class.java,
            "images").build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        val lock = Any()
        synchronized(lock) {
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            repository = null
        }
    }
}