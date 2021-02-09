package com.example.wallpaperchanger

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.ItemKeyProvider.SCOPE_CACHED
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpaperchanger.databinding.GridItemBinding
import com.example.wallpaperchanger.network.Wallpaper
import kotlinx.android.synthetic.main.grid_item.view.*

class Adapter(): ListAdapter<Wallpaper, Adapter.ViewHolder>(DiffCallback) {

    private lateinit var clickListener: ClickListener

    fun setClickListener(listener: ClickListener) {
        clickListener = listener
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).imageId.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
        holder.itemView.setOnClickListener{
            if (holder.itemView.item_image.drawable != null) {
                clickListener.onClick(holder.itemView.item_image.drawable.toBitmap())
            }
        }
    }

    class ViewHolder private constructor(private val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private var details: Details = Details()

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(GridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        fun bind(image: Wallpaper) {
            binding.image = image
            details.key = image.imageId
            binding.executePendingBindings()
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            details.pos = layoutPosition
            return details
        }

    }

    class ClickListener(val onClickListener: (bm: Bitmap) -> Unit) {
        //fun onClick(wp: Wallpaper) = onClickListener(wp)
        fun onClick(bitmap: Bitmap) = onClickListener(bitmap)
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Wallpaper>() {
        override fun areItemsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
            return oldItem.imageId == newItem.imageId //&& oldItem.loaded == newItem.loaded
        }

    }

    class Details: ItemDetailsLookup.ItemDetails<Long>() {
        var pos: Int = -1
        var key: String = "1"

        override fun getPosition(): Int {
            return pos
        }

        override fun getSelectionKey(): Long {
            return key.toLong()
        }

        override fun inSelectionHotspot(e: MotionEvent): Boolean {
            return true
        }
    }

    class KeyProvider(private val adapter: Adapter):
        ItemKeyProvider<Long>(SCOPE_CACHED) {

        override fun getKey(position: Int): Long {
            return adapter.getItemId(position)
        }

        override fun getPosition(key: Long): Int {
            var i = 0
            while(true) {
                if (adapter.getItemId(i) == key) {
                    return i
                }
                i++
            }
        }
    }


    class DetailsLookup(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val viewHolder = recyclerView.getChildViewHolder(view) as ViewHolder
                return viewHolder.getItemDetails()
            }
            return null
        }
    }

}