package com.example.wallpaperchanger.network


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.dirPath
import com.squareup.moshi.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.UnsupportedOperationException

data class NetworkWallpaper(
    val imageId: String,
    val contentUrl: String
) {

}

data class Wallpaper(
    val imageId: String,
    val uri: Uri
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is EntityWallpaper -> imageId == other.imageId
            is CollectionWallpaper -> imageId == other.imageId
            else -> super.equals(other)
        }
    }
}

@Entity(tableName = "wallpaper_table")
data class EntityWallpaper(
    @PrimaryKey
    val imageId: String
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Wallpaper -> imageId == other.imageId
            is CollectionWallpaper -> imageId == other.imageId
            else -> super.equals(other)
        }
    }
}

@Entity(tableName = "collection_table")
data class CollectionWallpaper(
    @PrimaryKey
    val imageId: String
) {
    override fun equals(other: Any?): Boolean {
        return return when (other) {
            is Wallpaper -> imageId == other.imageId
            is EntityWallpaper -> imageId == other.imageId
            else -> super.equals(other)
        }
    }
}

fun List<EntityWallpaper>.asWallpapers(): List<Wallpaper> {
    return map {
        Wallpaper(
            imageId = it.imageId,
            uri = Uri.fromFile(File(dirPath + it.imageId))
        )
    }
}

fun List<CollectionWallpaper>.asWallpapersC(): List<Wallpaper> {
    return map {
        Wallpaper(
            imageId = it.imageId,
            uri = Uri.fromFile(File(dirPath + it.imageId))
        )

    }
}


class WallpaperJsonAdapter {

    val arrayKey = JsonReader.Options.of("value")
    val itemKeys = JsonReader.Options.of("imageId", "contentUrl")

    @FromJson
    fun fromJson(reader: JsonReader): List<NetworkWallpaper> {
        val result = mutableListOf<NetworkWallpaper>()
        var imageId: String = ""
        var contentUrl: String = ""
        with(reader) {
            beginObject()
            while (hasNext()) {
                when (selectName(arrayKey)) {
                    0 -> {
                        beginArray()
                        while (hasNext()) {
                            beginObject()
                            while (hasNext()) {
                                when (selectName(itemKeys)) {
                                    0 -> imageId = nextString()
                                    1 -> contentUrl = nextString()
                                    else -> {
                                        skipName()
                                        skipValue()
                                    }
                                }
                            }
                            endObject()
                            result.add(NetworkWallpaper(imageId, contentUrl))
                        }
                        endArray()
                    }
                    else -> {
                        skipName()
                        skipValue()
                    }
                }
            }
            endObject()
        }
        return result
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Any?) {
        throw UnsupportedOperationException("This adapter is for deserializing only")
    }

}