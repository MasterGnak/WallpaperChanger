package com.example.wallpaperchanger.saved

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.databinding.CollectionFragmentBinding

class CollectionFragment : Fragment() {

    companion object {
        fun newInstance() = CollectionFragment()
    }

    private lateinit var viewModel: CollectionViewModel
    private lateinit var binding: CollectionFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CollectionFragmentBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(CollectionViewModel::class.java)
        binding.viewModel = viewModel

        binding.collectionList.adapter = Adapter(Adapter.ClickListener{})


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

}