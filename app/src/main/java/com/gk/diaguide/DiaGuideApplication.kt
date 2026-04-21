package com.gk.diaguide

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.gk.diaguide.data.notification.ReminderWorker
import com.gk.diaguide.di.ApplicationEntryPoint
import com.gk.diaguide.R
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DiaGuideApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(this, ApplicationEntryPoint::class.java)
        entryPoint.appLocaleManager().applyPreferredLanguage("ru")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
