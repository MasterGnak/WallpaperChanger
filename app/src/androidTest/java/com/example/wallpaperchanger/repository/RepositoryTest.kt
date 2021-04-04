package com.example.wallpaperchanger.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.wallpaperchanger.MainCoroutineRule
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.CollectionWallpaper
import com.example.wallpaperchanger.network.EntityWallpaper
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RepositoryTest {

    private lateinit var database: ImageDatabase
    private lateinit var repository: RepositoryInterface
    private val dir = File(dirPath)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        dir.mkdir()
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ImageDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = Repository(database, getApplicationContext())
    }

    @After
    fun close() {
        database.close()
        dir.delete()
    }

    @Test
    fun clear_databaseIsEmpty() {
        val wp1 = EntityWallpaper("1", "test.com")
        File(dir, "1").createNewFile()
        mainCoroutineRule.launch {
            database.imageDao.insertAll(listOf(wp1))

            repository.clear()
            val wallpapers = database.imageDao.getAllNotLive()

            assertThat(wallpapers.isEmpty(), `is`(true))
            assertThat(File(dirPath, "1").exists(), `is`(false))
        }
    }

    @Test
    fun clear_wallpapersInCollectionPersist() {
        val wp1 = EntityWallpaper("1", "test.com")
        val wp2 = CollectionWallpaper("1", "test.com")
        val wp11 = EntityWallpaper("11", "test.com")
        File(dir, "1").createNewFile()
        File(dir, "11").createNewFile()

        mainCoroutineRule.launch {
            database.imageDao.insertAll(listOf(wp1, wp11))
            database.imageDao.insertAllC(listOf(wp2))

            repository.clear()
            val wallpapers = database.imageDao.getAllNotLive()
            val wallpapersC = database.imageDao.getAllCNotLive()

            assertThat(wallpapers.isEmpty(), `is`(true))
            assertThat(wallpapersC.isEmpty(), `is`(false))
            assertThat(File(dirPath, "1").exists(), `is`(true))
            assertThat(File(dirPath, "11").exists(), `is`(false))
        }
    }

    @Test
    fun clearC_databaseIsEmpty() {
        val wp1 = CollectionWallpaper("1", "test.com")
        File(dir, "1").createNewFile()
        mainCoroutineRule.launch {
            database.imageDao.insertAllC(listOf(wp1))

            repository.clear()
            val wallpapers = database.imageDao.getAllCNotLive()

            assertThat(wallpapers.isEmpty(), `is`(true))
            assertThat(File(dirPath, "1").exists(), `is`(false))
        }
    }

    @Test
    fun clearC_wallpapersInSelectionPersist() {
        val wp1 = EntityWallpaper("1", "test.com")
        val wp2 = CollectionWallpaper("1", "test.com")
        val wp22 = CollectionWallpaper("22", "test.com")
        File(dir, "1").createNewFile()
        File(dir, "22").createNewFile()

        mainCoroutineRule.launch {
            database.imageDao.insertAll(listOf(wp1))
            database.imageDao.insertAllC(listOf(wp2, wp22))

            repository.clear()
            val wallpapers = database.imageDao.getAllNotLive()
            val wallpapersC = database.imageDao.getAllCNotLive()

            assertThat(wallpapers.isEmpty(), `is`(false))
            assertThat(wallpapersC.isEmpty(), `is`(true))
            assertThat(File(dirPath, "1").exists(), `is`(true))
            assertThat(File(dirPath, "22").exists(), `is`(false))
        }
    }

}