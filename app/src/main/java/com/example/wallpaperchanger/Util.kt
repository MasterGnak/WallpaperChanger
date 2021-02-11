package com.example.wallpaperchanger


import android.graphics.drawable.Drawable
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


@BindingAdapter("image")
fun bindImage(imgView: ImageView, image: Wallpaper?) {
    image?.let {
        if (it.bitmap != null) {
            imgView.setImageBitmap(it.bitmap)
        } else {
            Log.i("binding", "bitmap is null")
        }
    }
}

@BindingAdapter("imageList")
fun listImages(recyclerView: RecyclerView, list: List<Wallpaper>?) {
    val adapter = recyclerView.adapter as Adapter
    adapter.submitList(list)
    adapter.updateMap()
    Log.i("loading", "list is empty or null: ${list.isNullOrEmpty()} ${list?.size}")
}

@BindingAdapter("visible")
fun visible(recyclerView: RecyclerView, hidden: Boolean) {
    if (hidden) {
        recyclerView.visibility = View.GONE
    } else {
        recyclerView.visibility = View.VISIBLE
    }
}

@BindingAdapter("visible")
fun visibleAnim(progressBar: ProgressBar, hidden: Boolean) {
    if (!hidden) {
        progressBar.visibility = View.GONE
    } else {
        progressBar.visibility = View.VISIBLE
    }
}

lateinit var dirPath: String
lateinit var dirPathC: String

