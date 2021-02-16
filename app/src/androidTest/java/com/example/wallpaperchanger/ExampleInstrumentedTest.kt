package com.example.wallpaperchanger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.example.wallpaperchanger.work.LIST
import com.example.wallpaperchanger.work.TestWorker
import com.example.wallpaperchanger.work.WpWorker

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testPeriodicWork() {

        val repeatingRequest = PeriodicWorkRequestBuilder<TestWorker>(
            1, TimeUnit.HOURS
        ).setInputData(workDataOf(LIST to "Поиск")).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)


        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        testDriver?.setPeriodDelayMet(repeatingRequest.id)
    }
}

