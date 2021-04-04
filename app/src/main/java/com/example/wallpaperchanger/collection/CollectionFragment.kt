package com.example.wallpaperchanger.collection

import android.animation.ObjectAnimator
import android.app.WallpaperManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bumptech.glide.Glide
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.databinding.CollectionFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionFragment : Fragment() {

    private lateinit var viewModel: CollectionViewModel
    private lateinit var binding: CollectionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CollectionFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(CollectionViewModel::class.java)
        binding.viewModel = viewModel
        val menu = binding.bottomMenu.root

        val fullPx =
            (3 * resources.getDimensionPixelSize(R.dimen.text_size) + 6 * resources.getDimensionPixelSize(R.dimen.small)).toFloat()
        val partPx =
            (1 * resources.getDimensionPixelSize(R.dimen.text_size) + 2 * resources.getDimensionPixelSize(R.dimen.small)).toFloat()
        val preHideM = ObjectAnimator.ofFloat(menu, "translationY", fullPx).apply { duration = 1 }
        val showM = ObjectAnimator.ofFloat(menu, "translationY", fullPx, 0f).apply { duration = 500 }
        val hideM = ObjectAnimator.ofFloat(menu, "translationY", fullPx).apply { duration = 500 }
        val showOneM = ObjectAnimator.ofFloat(menu, "translationY", partPx, 0f).apply { duration = 400 }
        val hideOneM = ObjectAnimator.ofFloat(menu, "translationY", partPx).apply { duration = 400 }
        val hidePartM = ObjectAnimator.ofFloat(menu, "translationY", partPx, fullPx).apply { duration = 500 }
        preHideM.start()

        val wpManager = WallpaperManager.getInstance(context)

        val adapter = Adapter()
        binding.collectionList.adapter = adapter

        val tracker = SelectionTracker.Builder(
            "tracker",
            binding.collectionList,
            Adapter.KeyProvider(adapter),
            Adapter.DetailsLookup(binding.collectionList),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        adapter.setTracker(tracker)

        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {

            override fun onSelectionChanged() {
                super.onSelectionChanged()
                if (!viewModel.menuOpened) {
                    showM.start()
                    viewModel.menuOpened = true
                } else if (!tracker.hasSelection()) {
                    if (viewModel.severalSelected) {
                        hidePartM.start()
                    } else {
                        hideM.start()
                    }
                    viewModel.severalSelected = false
                    viewModel.menuOpened = false
                } else if (tracker.selection.size() == 1) {
                    showOneM.start()
                    viewModel.severalSelected = false
                } else if (tracker.selection.size() > 1 && !viewModel.severalSelected) {
                    hideOneM.start()
                    viewModel.severalSelected = true
                }
            }

            override fun onSelectionCleared() {}
        })

        binding.bottomMenu.setAsWp.setOnClickListener {
            viewModel.updateWp(adapter.getSingleSelection(), requireContext())
            Toast.makeText(context, "Обои обновляются, не выключайте приложение...", Toast.LENGTH_SHORT).show()
            tracker.clearSelection()
        }

        binding.bottomMenu.clearSelection.setOnClickListener {
            tracker.clearSelection()
        }

        binding.bottomMenu.removeFromCollection.setOnClickListener {
            viewModel.clearC(adapter.getMultipleSelection())
            tracker.clearSelection()
        }



        return binding.root
    }

}