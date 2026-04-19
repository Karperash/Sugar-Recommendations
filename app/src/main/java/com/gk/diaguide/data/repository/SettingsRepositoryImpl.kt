package com.gk.diaguide.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dia_guide_settings")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override fun observeSettings(): Flow<UserSettings> =
        context.dataStore.data.map { prefs ->
            UserSettings(
                displayName = prefs[Keys.DISPLAY_NAME].orEmpty(),
                diabetesType = prefs[Keys.DIABETES_TYPE].orEmpty(),
                ageGroup = prefs[Keys.AGE_GROUP].orEmpty(),
                glucoseUnit = prefs[Keys.UNIT]?.let { GlucoseUnit.valueOf(it) } ?: GlucoseUnit.MG_DL,
                targetLow = prefs[Keys.TARGET_LOW] ?: 80.0,
                targetHigh = prefs[Keys.TARGET_HIGH] ?: 140.0,
                warningLow = prefs[Keys.WARNING_LOW] ?: 70.0,
                warningHigh = prefs[Keys.WARNING_HIGH] ?: 180.0,
                criticalLow = prefs[Keys.CRITICAL_LOW] ?: 55.0,
                criticalHigh = prefs[Keys.CRITICAL_HIGH] ?: 250.0,
                rapidRiseThresholdPer15Min = prefs[Keys.RAPID_RISE] ?: 20.0,
                rapidFallThresholdPer15Min = prefs[Keys.RAPID_FALL] ?: 20.0,
                prolongedOutOfRangeMinutes = prefs[Keys.PROLONGED] ?: 45L,
                patternWindowHours = prefs[Keys.PATTERN_WINDOW] ?: 24L,
                reminderIntervalHours = prefs[Keys.REMINDER_INTERVAL] ?: 0L,
                appLanguageTag = prefs[Keys.APP_LANGUAGE].orEmpty(),
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                disclaimerAccepted = prefs[Keys.DISCLAIMER_ACCEPTED] ?: false,
            )
        }

    override suspend fun saveProfile(
        displayName: String,
        diabetesType: String,
        ageGroup: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName
            prefs[Keys.DIABETES_TYPE] = diabetesType
            prefs[Keys.AGE_GROUP] = ageGroup
        }
    }

    override suspend fun saveThresholds(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNIT] = settings.glucoseUnit.name
            prefs[Keys.TARGET_LOW] = settings.targetLow
            prefs[Keys.TARGET_HIGH] = settings.targetHigh
            prefs[Keys.WARNING_LOW] = settings.warningLow
            prefs[Keys.WARNING_HIGH] = settings.warningHigh
            prefs[Keys.CRITICAL_LOW] = settings.criticalLow
            prefs[Keys.CRITICAL_HIGH] = settings.criticalHigh
            prefs[Keys.RAPID_RISE] = settings.rapidRiseThresholdPer15Min
            prefs[Keys.RAPID_FALL] = settings.rapidFallThresholdPer15Min
            prefs[Keys.PROLONGED] = settings.prolongedOutOfRangeMinutes
            prefs[Keys.PATTERN_WINDOW] = settings.patternWindowHours
            prefs[Keys.REMINDER_INTERVAL] = settings.reminderIntervalHours
            prefs[Keys.APP_LANGUAGE] = settings.appLanguageTag
        }
    }

    override suspend fun completeOnboarding(completed: Boolean, disclaimerAccepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
            prefs[Keys.DISCLAIMER_ACCEPTED] = disclaimerAccepted
        }
    }

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val DIABETES_TYPE = stringPreferencesKey("diabetes_type")
        val AGE_GROUP = stringPreferencesKey("age_group")
        val UNIT = stringPreferencesKey("unit")
        val TARGET_LOW = doublePreferencesKey("target_low")
        val TARGET_HIGH = doublePreferencesKey("target_high")
        val WARNING_LOW = doublePreferencesKey("warning_low")
        val WARNING_HIGH = doublePreferencesKey("warning_high")
        val CRITICAL_LOW = doublePreferencesKey("critical_low")
        val CRITICAL_HIGH = doublePreferencesKey("critical_high")
        val RAPID_RISE = doublePreferencesKey("rapid_rise")
        val RAPID_FALL = doublePreferencesKey("rapid_fall")
        val PROLONGED = longPreferencesKey("prolonged")
        val PATTERN_WINDOW = longPreferencesKey("pattern_window")
        val REMINDER_INTERVAL = longPreferencesKey("reminder_interval")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
    }
}
