package com.example.wallpaperchanger.selector

import android.animation.ObjectAnimator
import android.app.WallpaperManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.databinding.SelectionFragmentBinding

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
        binding.selectionList.adapter = Adapter(Adapter.ClickListener {
            if (viewModel.menuOpened.value == false) {
                binding.testView.bitmap = it
                showMenu()
            } else if (viewModel.menuOpened.value == true) {
                hideMenu()
                binding.testView.bitmap = null
            }
        })

        binding.testView.root.setOnClickListener{
            hideMenu()
            wpManager.setBitmap(binding.testView.bitmap)
            Toast.makeText(context, "Обои обновлены", Toast.LENGTH_SHORT).show()
        }

        viewModel.count.observe(viewLifecycleOwner) {
            if (it == viewModel.listSize.value) {
                viewModel.toggleVisibility()
            }
        }

        return binding.root
    }

    private fun showMenu() {
        ObjectAnimator.ofFloat(binding.testView.root, "translationY", -100f).apply {
            duration = 500
            start()
        }
        viewModel.toggleMenu()
    }

    private fun hideMenu() {
        ObjectAnimator.ofFloat(binding.testView.root, "translationY", 100f).apply {
            duration = 500
            start()
        }
        viewModel.toggleMenu()
    }

}

