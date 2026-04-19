package com.gk.diaguide

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.gk.diaguide.data.notification.ReminderWorker
import com.gk.diaguide.di.ApplicationEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class DiaGuideApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(this, ApplicationEntryPoint::class.java)
        runBlocking {
            val settings = entryPoint.settingsRepository().observeSettings().first()
            entryPoint.appLocaleManager().applyPreferredLanguage(settings.appLanguageTag)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            "Measurement reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Periodic reminders to check glucose"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
