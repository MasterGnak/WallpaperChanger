package com.example.wallpaperchanger.selector

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.databinding.SelectionFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectionFragment : Fragment() {

    private lateinit var viewModel: SelectionViewModel
    private lateinit var binding: SelectionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(SelectionViewModel::class.java)
        binding = SelectionFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val wpManager = WallpaperManager.getInstance(context)
        val showM = ObjectAnimator.ofFloat(binding.bottomMenu.root, "translationY", -100f).apply { duration = 500 }
        val hideM = ObjectAnimator.ofFloat(binding.bottomMenu.root, "translationY", 100f).apply { duration = 500 }
        val imm = requireNotNull(activity).getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val sharedPrefs = requireContext().getSharedPreferences("queryPrefs", MODE_PRIVATE)
        binding.editQuery.setText(sharedPrefs.getString("queryText", null))

        val adapter = Adapter()

//        val selector = SelectionTracker.Builder(
//            "selector",
//            binding.selectionList,
//            Adapter.KeyProvider(adapter),
//            Adapter.DetailsLookup(binding.selectionList),
//            StorageStrategy.createLongStorage()
//        ).build()
//
//        selector.addObserver(object: SelectionTracker.SelectionObserver<Long>() {
//
//        })
//
//        adapter.setClickListener(Adapter.ClickListener {
//            binding.editQuery.clearFocus()
//            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
//            if (viewModel.menuOpened.value == false) {
//                binding.bottomMenu.bitmap = it
//                showM.start()
//                viewModel.showMenu()
//            } else if (viewModel.menuOpened.value == true) {
//                hideM.start()
//                viewModel.hideMenu()
//                binding.bottomMenu.bitmap = null
//            }
//        })

        binding.selectionList.adapter = adapter


        binding.bottomMenu.root.setOnClickListener{
            MainScope().launch {
                hideM.start()
                viewModel.hideMenu()
                Toast.makeText(context, "Обои обновлены", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.Default) {
                    wpManager.setBitmap(binding.bottomMenu.bitmap)
                }
            }
        }

        viewModel.count.observe(viewLifecycleOwner) {
            binding.progressHorizontal.progress = it
            if (it == viewModel.listSize.value) {
                viewModel.toggleVisibility()
                viewModel.resetCount()
            }
        }

        binding.editQuery.setOnFocusChangeListener{editText: View, focused: Boolean ->
            if (focused) {
                if (viewModel.menuOpened.value == true) {
                    hideM.start()
                    viewModel.hideMenu()
                    binding.bottomMenu.bitmap = null
                }
                binding.confirmQuery.visibility = View.VISIBLE
            } else {
                binding.confirmQuery.visibility = View.GONE
            }
        }

        binding.confirmQuery.setOnClickListener{
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

