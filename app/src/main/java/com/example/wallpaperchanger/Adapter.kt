package com.example.wallpaperchanger

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wallpaperchanger.databinding.GridItemBinding
import com.example.wallpaperchanger.network.Wallpaper

class Adapter(): ListAdapter<Wallpaper, Adapter.ViewHolder>(DiffCallback) {

    val keyMap = HashMap<String, Int>()

    private lateinit var tracker: SelectionTracker<String>

    fun setTracker(selector: SelectionTracker<String>) {
        tracker = selector
    }

    fun updateMap() {
        keyMap.clear()
        for (i in currentList.indices) {
            keyMap[currentList[i].imageId] = i
        }
    }

    fun getItemKey(position: Int): String {
        return getItem(position).imageId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image, tracker.isSelected(getItemKey(position)))
    }

    class ViewHolder private constructor(private val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private var details: Details = Details()

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(GridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        fun bind(image: Wallpaper, isSelected: Boolean) {
            binding.itemFrame.isActivated = isSelected
            binding.image = image
            details.key = image.imageId
            binding.executePendingBindings()
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            details.pos = adapterPosition
            return details
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

    class Details: ItemDetailsLookup.ItemDetails<String>() {
        var pos: Int = -1
        var key: String = "1"

        override fun getPosition(): Int {
            return pos
        }

        override fun getSelectionKey(): String {
            return key
        }

        override fun inSelectionHotspot(e: MotionEvent): Boolean {
            return true
        }
    }

    class KeyProvider(private val adapter: Adapter):
        ItemKeyProvider<String>(SCOPE_CACHED) {

        override fun getKey(position: Int): String {
            return adapter.getItemKey(position)
        }

        override fun getPosition(key: String): Int {
            return adapter.keyMap[key]!!
        }
    }


    class DetailsLookup(private val recyclerView: RecyclerView): ItemDetailsLookup<String>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val viewHolder = recyclerView.getChildViewHolder(view) as ViewHolder
                return viewHolder.getItemDetails()
            }
            return null
        }
    }

    fun getSingleSelection(): Wallpaper {
        lateinit var selectedWp: Wallpaper
        tracker.selection.forEach {
            selectedWp = getItem(keyMap[it]!!)
        }
        return selectedWp
    }

    fun getMultipleSelection(): List<Wallpaper> {
        val list = mutableListOf<Wallpaper>()
        tracker.selection.forEach {
            list.add(getItem(keyMap[it]!!))
        }
        return list
    }

}