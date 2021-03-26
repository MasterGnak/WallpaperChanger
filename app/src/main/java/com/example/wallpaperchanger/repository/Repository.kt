package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.dirPath
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class Repository(private val database: ImageDatabase, private val context: Context) : RepositoryInterface {

    private val _images = MutableLiveData<MutableList<Wallpaper>>(mutableListOf())
    override val images: LiveData<List<Wallpaper>> = Transformations.map(_images){it}
//    = Transformations.map(database.imageDao.getAll()) {
//        it.asWallpapers()
//    }

    override val imagesC = Transformations.map(database.imageDao.getAllC()) {
        it.asWallpapersC()
    }

//    var listSize = MutableLiveData(-1)
//    var count = MutableLiveData(0)
    //private var defective = mutableListOf<EntityWallpaper>()
    private val valid = mutableListOf<EntityWallpaper>()
    private val dir = File(dirPath)

    init {
        Log.i("loading", "path is $dirPath, dir exists: ${dir.exists()}")
    }

    override suspend fun downloadWallpapers(query: String) {
        withContext(Dispatchers.IO) {
            clear()
            val wallpapers = Api.retrofitService.getImages(query, "Wallpaper", "Tall", 35)
            //listSize.value = wallpapers.size
            database.imageDao.insertAll(wallpapers.validate())
            //database.imageDao.removeDefective(defective)
        }
    }

//    override fun increment(vararg id: String) {
//        {
//            count.value = count.value?.plus(1)
//            if (id.isNotEmpty()) {
//                remove(id[0])
//            }
//        }
//    }

    private fun removeWp(id: String) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            remove(id)
        }
    }

    private suspend fun remove(id: String) {
        database.imageDao.remove(id)
    }


    private fun List<NetworkWallpaper>.validate(): List<EntityWallpaper> {

        val result = map {
            val fileName = it.imageId
            val imgUri = it.contentUrl.toUri().buildUpon().scheme("https").build()
            downloadImage(imgUri, fileName)
            EntityWallpaper(
                imageId = it.imageId
            )
        }
        return result
    }

    private fun downloadImage(uri: Uri, id: String) {
        Glide.with(context).load(uri).into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                //saveImage(resource.toBitmap(), dir, id)
                //valid.add(EntityWallpaper(id)
                _images.value?.add(Wallpaper(imageId = id, uri = uri))
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                //removeWp(id)
                //defective.add(EntityWallpaper(id))
            }
        })
    }

    private fun saveImage(bitmap: Bitmap, dir: File, id: String) {
        val dirCreated = if (!dir.exists()) {
            dir.mkdir()
        } else {
            true
        }
        if (dirCreated) {
            try {
                val fOut = FileOutputStream(File(dir, id))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                Log.i("loading", "image saved")
                fOut.close()
                //increment()
            } catch (e: Exception) {
                Log.e("loading", "Failed to save image", e)
            }
        } else {
            Log.e("loading", "Failed to create dir")
        }
    }

    override suspend fun addToCollection(selection: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            database.imageDao.insertAllC(
                selection.map {
                    CollectionWallpaper(it.imageId)
                }
            )
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            val collection = database.imageDao.getAllC().value
            list?.minus(collection)?.forEach {
                File(dirPath, (it as EntityWallpaper).imageId).delete()
            }
            database.imageDao.clear()
        }
    }

    override suspend fun clearC(collection: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            val list = database.imageDao.getAll().value
            collection.minus(list).forEach {
                File(dirPath, (it as Wallpaper).imageId).delete()
            }
            database.imageDao.clearC(collection.map {
                CollectionWallpaper(it.imageId)
            })
        }
    }

}