package com.example.wallpaperchanger.room

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wallpaperchanger.network.CollectionWallpaper
import com.example.wallpaperchanger.network.EntityWallpaper

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<EntityWallpaper>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllC(images: List<CollectionWallpaper>)

    @Query("DELETE FROM wallpaper_table WHERE imageId = :imageId")
    suspend fun remove(imageId: String)

    @Query("DELETE FROM collection_table WHERE imageId = :imageId")
    suspend fun removeC(imageId: String)

    @Query("DELETE FROM wallpaper_table")
    suspend fun clear()

    @Delete
    suspend fun clearC(images: List<CollectionWallpaper>)

    @Query("SELECT * FROM wallpaper_table")
    fun getAll(): LiveData<List<EntityWallpaper>>

    @Query("SELECT * FROM collection_table")
    fun getAllC(): LiveData<List<CollectionWallpaper>>

    @Query("SELECT * FROM wallpaper_table")
    fun getAllNotLive(): List<EntityWallpaper>

    @Query("SELECT * FROM collection_table")
    fun getAllCNotLive(): List<CollectionWallpaper>

}

@Database(entities = [EntityWallpaper::class, CollectionWallpaper::class], version = 10000, exportSchema = false)
abstract class ImageDatabase: RoomDatabase() {
    abstract val imageDao: ImageDao
}

private lateinit var INSTANCE: ImageDatabase

fun getDatabase(context: Context): ImageDatabase {
    synchronized(ImageDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                ImageDatabase::class.java,
            "images").fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}