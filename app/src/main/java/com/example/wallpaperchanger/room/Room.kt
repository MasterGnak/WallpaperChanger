package com.example.wallpaperchanger.room

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.wallpaperchanger.network.EntityWallpaper

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(images: List<EntityWallpaper>)

    @Insert
    fun insertOne(image: EntityWallpaper)

    @Query("DELETE FROM wallpaper_table WHERE imageId = :imageId")
    fun remove(imageId: String)

    @Query("DELETE FROM wallpaper_table")
    fun clear()

    @Query("SELECT * FROM wallpaper_table")
    fun getAll(): LiveData<List<EntityWallpaper>>

    @Query("SELECT count(*) FROM wallpaper_table")
    fun getCount(): Int
}

@Database(entities = [EntityWallpaper::class], version = 1)
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