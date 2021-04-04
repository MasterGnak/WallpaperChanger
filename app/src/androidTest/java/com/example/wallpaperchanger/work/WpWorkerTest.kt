package com.example.wallpaperchanger.work

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.work.*
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@MediumTest
class WpWorkerTest {

    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(getApplicationContext())
    }

    @Test
    fun testPeriodicWork() {
        val request = PeriodicWorkRequestBuilder<WpWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(LIST to "Поиск"))
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        val workManager = WorkManager.getInstance(getApplicationContext())
        workManager.enqueueUniquePeriodicWork(
            WpWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        val testDriver = WorkManagerTestInitHelper.getTestDriver(getApplicationContext())
        testDriver?.setInitialDelayMet(request.id)
        testDriver?.setPeriodDelayMet(request.id)

        val workInfo = workManager.getWorkInfoById(request.id).get()

        assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }

}