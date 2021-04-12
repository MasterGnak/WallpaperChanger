package com.example.wallpaperchanger.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.work.FREQ
import com.example.wallpaperchanger.work.LIST
import com.example.wallpaperchanger.work.SWITCH
import com.example.wallpaperchanger.work.WpWorker
import java.util.concurrent.TimeUnit


class SettingsViewModel(application: Application): AndroidViewModel(application) {

    private val search = application.getString(R.string.search)
    private val collection = application.getString(R.string.collection)

    private val workManager = WorkManager.getInstance(application)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    var switch = prefs.getBoolean(SWITCH, false)
    var list = prefs.getString(LIST, search)
    var freq = prefs.getInt(FREQ, 4).toLong()

    fun setupWork(context: Context) {
        if (switch) {
            val inputData = workDataOf(LIST to list)
            val repeatingRequest = PeriodicWorkRequestBuilder<WpWorker>(freq, TimeUnit.HOURS)
                .setInputData(inputData)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WpWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                repeatingRequest)
        } else {
            workManager.cancelUniqueWork(WpWorker.WORK_NAME)
        }
    }
}