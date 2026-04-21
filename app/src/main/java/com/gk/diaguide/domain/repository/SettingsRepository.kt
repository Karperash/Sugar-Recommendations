package com.gk.diaguide.domain.repository

import com.gk.diaguide.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>
    suspend fun saveProfile(
        displayName: String,
        diabetesType: String,
        ageGroup: String,
        biologicalSex: String,
    )
    suspend fun saveThresholds(settings: UserSettings)
    suspend fun completeOnboarding(completed: Boolean, disclaimerAccepted: Boolean)
}
