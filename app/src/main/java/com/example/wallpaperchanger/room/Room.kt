package com.example.wallpaperchanger.room

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wallpaperchanger.network.CollectionWallpaper
import com.example.wallpaperchanger.network.EntityWallpaper

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(images: List<EntityWallpaper>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllC(images: List<CollectionWallpaper>)

    @Query("DELETE FROM wallpaper_table WHERE imageId = :imageId")
    suspend fun remove(imageId: String)

    @Query("DELETE FROM collection_table WHERE imageId = :imageId")
    suspend fun removeC(imageId: String)

    @Query("DELETE FROM wallpaper_table")
    fun clear()

    @Delete
    fun removeDefective(images: List<EntityWallpaper>)

    @Delete
    fun clearC(images: List<CollectionWallpaper>)

    @Query("SELECT * FROM wallpaper_table")
    fun getAll(): LiveData<List<EntityWallpaper>>

    @Query("SELECT * FROM collection_table")
    fun getAllC(): LiveData<List<CollectionWallpaper>>

    @Query("SELECT * FROM wallpaper_table")
    fun getAllNotLive(): List<EntityWallpaper>

    @Query("SELECT * FROM collection_table")
    fun getAllCNotLive(): List<CollectionWallpaper>

    @Query("SELECT count(*) FROM wallpaper_table")
    fun getCount(): Int


}

@Database(entities = [EntityWallpaper::class, CollectionWallpaper::class], version = 101)
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