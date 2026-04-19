package com.gk.diaguide.data.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun schedule(intervalHours: Long) {
        if (intervalHours <= 0) {
            cancel()
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalHours,
            TimeUnit.HOURS,
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "dia_guide_reminder"
    }
}
