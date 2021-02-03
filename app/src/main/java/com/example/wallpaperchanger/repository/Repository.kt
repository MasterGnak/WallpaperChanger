package com.example.wallpaperchanger.repository

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.Transformations
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.wallpaperchanger.network.*
import com.example.wallpaperchanger.room.ImageDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class Repository(private val database: ImageDatabase, private val context: Context) {

}