package com.example.wallpaperchanger.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.*
import com.example.wallpaperchanger.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        val switch = SwitchPreferenceCompat(context).apply {
            key = "switch"
            setDefaultValue(false)
            title = getString(R.string.title_switch_preference)
            isIconSpaceReserved = false
            isSingleLineTitle = false
            setOnPreferenceChangeListener { preference, newValue ->
                viewModel.switch = newValue as Boolean
                viewModel.setupWork()
                true
            }
        }
        val list = ListPreference(context).apply {
            key = "list"
            title = getString(R.string.title_list_preference)
            entries = arrayOf(getString(R.string.search), getString(R.string.collection))
            entryValues = arrayOf(getString(R.string.search), getString(R.string.collection))
            summary = getString(R.string.summary_list_preference)
            setDefaultValue(getString(R.string.search))
            isIconSpaceReserved = false
            isSingleLineTitle = false
            setOnPreferenceChangeListener { preference, newValue ->
                viewModel.list = newValue as String
                viewModel.setupWork()
                true
            }
        }
        val seekBar = SeekBarPreference(context).apply {
            key = "frequency"
            title = getString(R.string.title_frequency_list_preference)
            max = 24
            min = 1
            setDefaultValue(4)
            isIconSpaceReserved = false
            isSingleLineTitle = false
            showSeekBarValue = true
            setOnPreferenceChangeListener { preference, newValue ->
                viewModel.freq = (newValue as Int).toLong()
                viewModel.setupWork()
                true
            }
        }
        screen.addPreference(switch)
        screen.addPreference(list)
        screen.addPreference(seekBar)
        preferenceScreen = screen
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        root?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
        return root
    }


}