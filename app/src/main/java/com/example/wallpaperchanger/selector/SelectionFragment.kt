package com.example.wallpaperchanger.selector

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.bumptech.glide.Glide
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.MainActivity
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.WpApplication
import com.example.wallpaperchanger.databinding.SelectionFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectionFragment : Fragment() {

    private val viewModel by viewModels<SelectionViewModel> {
        SelectionViewModel.SelectionViewModelFactory((requireContext().applicationContext as WpApplication).repository)
    }
    private lateinit var binding: SelectionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectionFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val menu = binding.bottomMenu.root

        val fullPx = (3*resources.getDimensionPixelSize(R.dimen.text_size) + 6*resources.getDimensionPixelSize(R.dimen.small)).toFloat()
        val partPx = (1*resources.getDimensionPixelSize(R.dimen.text_size) + 2*resources.getDimensionPixelSize(R.dimen.small)).toFloat()
        val preHideM = ObjectAnimator.ofFloat(menu, "translationY", fullPx).apply { duration = 1 }
        val showM = ObjectAnimator.ofFloat(menu, "translationY", fullPx, 0f).apply { duration = 500 }
        val hideM = ObjectAnimator.ofFloat(menu, "translationY", fullPx).apply { duration = 500 }
        val showOneM = ObjectAnimator.ofFloat(menu, "translationY", partPx, 0f).apply { duration = 400 }
        val hideOneM = ObjectAnimator.ofFloat(menu, "translationY", partPx).apply { duration = 400 }
        val hidePartM = ObjectAnimator.ofFloat(menu, "translationY", partPx, fullPx).apply { duration = 500 }
        preHideM.start()

        val wpManager = WallpaperManager.getInstance(context)
        val imm = requireNotNull(activity).getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val sharedPrefs = requireContext().getSharedPreferences("queryPrefs", MODE_PRIVATE)
        binding.editQuery.setText(sharedPrefs.getString("queryText", null))

        val adapter = Adapter()
        binding.selectionList.adapter = adapter

        val tracker = SelectionTracker.Builder(
            "tracker",
            binding.selectionList,
            Adapter.KeyProvider(adapter),
            Adapter.DetailsLookup(binding.selectionList),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

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

        adapter.setTracker(tracker)

        binding.bottomMenu.setAsWp.setOnClickListener {
            MainScope().launch {
                hideM.start()
                viewModel.menuOpened = false
                Toast.makeText(context, "Обои обновлены", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.IO) {
                    val bitmap = Glide.with(requireContext()).asBitmap().load(adapter.getSingleSelection().uri).submit()
                    wpManager.setBitmap(bitmap.get())
                }
                tracker.clearSelection()
            }
        }

        binding.bottomMenu.clearSelection.setOnClickListener {
            tracker.clearSelection()
        }

        binding.bottomMenu.addToCollection.setOnClickListener {
            viewModel.addToCollection(adapter.getMultipleSelection())
            tracker.clearSelection()
        }

//        viewModel.count.observe(viewLifecycleOwner) {
//            binding.progressHorizontal.progress = it
//            if (it == viewModel.listSize.value) {
//                binding.invalidateAll()
//                viewModel.resetCount()
//                viewModel.toggleVisibility()
//            }
//        }

        binding.editQuery.setOnFocusChangeListener { editText: View, focused: Boolean ->
            if (focused) {
                tracker.clearSelection()
                binding.confirmQuery.visibility = View.VISIBLE
            } else {
                binding.confirmQuery.visibility = View.GONE
            }
        }

        binding.confirmQuery.setOnClickListener {
            binding.editQuery.clearFocus()
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            val text = binding.editQuery.text.toString()
            if (text != sharedPrefs.getString("queryText", null)) {
                sharedPrefs.edit().putString("queryText", text).apply()
                viewModel.downloadWp(text)
            }
        }

        return binding.root
    }

}

