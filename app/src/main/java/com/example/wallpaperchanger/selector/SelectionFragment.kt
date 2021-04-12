package com.example.wallpaperchanger.selector

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.WpApplication
import com.example.wallpaperchanger.databinding.SelectionFragmentBinding

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
            viewModel.updateWp(adapter.getSingleSelection(), requireContext())
            Toast.makeText(context, "Обои обновляются, не выключайте приложение...", Toast.LENGTH_LONG).show()
            tracker.clearSelection()
        }

        binding.bottomMenu.clearSelection.setOnClickListener {
            tracker.clearSelection()
        }

        binding.bottomMenu.addToCollection.setOnClickListener {
            viewModel.addToCollection(adapter.getMultipleSelection())
            tracker.clearSelection()
        }

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

        binding.menuButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout).openDrawer(Gravity.LEFT)
        }

        return binding.root
    }

}

