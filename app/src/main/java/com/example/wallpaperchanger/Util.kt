package com.example.wallpaperchanger


import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.wallpaperchanger.network.NetworkWallpaper
import com.example.wallpaperchanger.network.Wallpaper
import com.example.wallpaperchanger.selector.Adapter
import com.example.wallpaperchanger.selector.SelectionViewModel


@BindingAdapter("image")
fun bindImage(imgView: ImageView, image: Wallpaper?) {
    image?.let {
        if (it.loaded != false) {
            val imgUri = image.contentUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("loading", "load failed", e)
                        image.loaded = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                })
                .apply(RequestOptions())
                .into(imgView)
        }
    }
}

@BindingAdapter("imageList")
fun listImages(recyclerView: RecyclerView, list: List<Wallpaper>?) {
    val adapter = recyclerView.adapter as Adapter
    adapter.submitList(list)
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
fun visibleAnim(imageView: ImageView, hidden: Boolean) {
    if (!hidden) {
        imageView.visibility = View.GONE
    } else {
        imageView.visibility = View.VISIBLE
    }
}

