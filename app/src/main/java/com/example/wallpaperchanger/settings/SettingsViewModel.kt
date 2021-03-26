package com.example.wallpaperchanger.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import androidx.work.*
import com.example.wallpaperchanger.R
import com.example.wallpaperchanger.work.*
import java.util.concurrent.TimeUnit



class SettingsViewModel(application: Application): AndroidViewModel(application) {

    private val context = application.applicationContext
    private val search = context.getString(R.string.search)
    private val collection = context.getString(R.string.collection)

    private val workManager = WorkManager.getInstance(context)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    var switch = prefs.getBoolean(SWITCH, false)
    var list = prefs.getString(LIST, search)
    var freq = prefs.getInt(FREQ, 4).toLong()

    fun setupWork() {
        if (switch) {
            val inputData = workDataOf(LIST to list)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresDeviceIdle(false)
                .build()
            val repeatingRequest = PeriodicWorkRequestBuilder<WpWorker>(freq, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(1, TimeUnit.HOURS)
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