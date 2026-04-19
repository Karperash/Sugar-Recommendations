package com.gk.diaguide.di

import com.gk.diaguide.core.locale.AppLocaleManager
import com.gk.diaguide.domain.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun appLocaleManager(): AppLocaleManager
}
