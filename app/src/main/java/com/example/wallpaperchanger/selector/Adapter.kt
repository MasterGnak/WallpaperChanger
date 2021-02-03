package com.example.wallpaperchanger.selector

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpaperchanger.databinding.GridItemBinding
import com.example.wallpaperchanger.network.Wallpaper

class Adapter(): ListAdapter<Wallpaper, Adapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
    }

    class ViewHolder private constructor(private val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(GridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        fun bind(image: Wallpaper) {
            binding.image = image
            binding.executePendingBindings()
        }


    }

    companion object DiffCallback: DiffUtil.ItemCallback<Wallpaper>() {
        override fun areItemsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
            return oldItem.imageId == newItem.imageId //&& oldItem.loaded == newItem.loaded
        }

    }
}