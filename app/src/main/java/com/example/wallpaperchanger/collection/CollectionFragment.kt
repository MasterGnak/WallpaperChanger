package com.example.wallpaperchanger.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.example.wallpaperchanger.Adapter
import com.example.wallpaperchanger.databinding.CollectionFragmentBinding

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


        return binding.root
    }

}