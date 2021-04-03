package com.example.wallpaperchanger


import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.wallpaperchanger.network.Wallpaper
import java.io.File


@BindingAdapter("image")
fun bindImage(imgView: ImageView, image: Wallpaper) {
    val file = File(dirPath + image.imageId)
    val uri = if (file.exists()) {
        Uri.fromFile(file)
    } else {
        image.url.toUri().buildUpon().scheme("https").build()
    }
    Glide.with(imgView.context).load(uri).apply (
        RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_broken_image)
    ).into(imgView)
}

@BindingAdapter("imageList")
fun listImages(recyclerView: RecyclerView, list: List<Wallpaper>?) {
    val adapter = recyclerView.adapter as Adapter
    adapter.submitList(list)
    Log.i("loading", "list is empty or null: ${list.isNullOrEmpty()} ${list?.size}")
}




