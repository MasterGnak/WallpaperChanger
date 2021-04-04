package com.example.wallpaperchanger.network

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wallpaperchanger.dirPath
import com.squareup.moshi.*
import java.io.File
import java.lang.UnsupportedOperationException

data class NetworkWallpaper(
    val imageId: String,
    val contentUrl: String
) {

}

data class Wallpaper(
    val imageId: String,
    val url: String
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
    val imageId: String,
    val url: String
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
    val imageId: String,
    val url: String
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
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
            url = it.url
        )
    }
}

fun List<CollectionWallpaper>.asWallpapersC(): List<Wallpaper> {
    return map {
        Wallpaper(
            imageId = it.imageId,
            url = it.url
        )

    }
}


class WallpaperJsonAdapter {

    private val arrayKey = JsonReader.Options.of("value")
    private val itemKeys = JsonReader.Options.of("imageId", "contentUrl")

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