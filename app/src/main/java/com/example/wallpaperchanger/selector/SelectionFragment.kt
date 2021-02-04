package com.example.wallpaperchanger.selector

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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
        binding.selectionList.adapter = Adapter()

        viewModel.count.observe(viewLifecycleOwner) {
            if (it == viewModel.listSize.value) {
                viewModel.toggleVisibility()
            }
        }

        return binding.root
    }

}