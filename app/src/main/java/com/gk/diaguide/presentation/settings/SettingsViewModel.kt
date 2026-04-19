package com.gk.diaguide.presentation.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.R
import com.gk.diaguide.core.locale.AppLocaleManager
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.SaveThresholdsUseCase
import com.gk.diaguide.domain.usecase.SaveUserProfileUseCase
import com.gk.diaguide.data.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val displayName: String = "",
    val diabetesType: String = "",
    val ageGroup: String = "",
    val unit: GlucoseUnit = GlucoseUnit.MG_DL,
    val targetLow: String = "",
    val targetHigh: String = "",
    val warningLow: String = "",
    val warningHigh: String = "",
    val criticalLow: String = "",
    val criticalHigh: String = "",
    val rapidRise: String = "",
    val rapidFall: String = "",
    val prolongedMinutes: String = "",
    val patternWindowHours: String = "",
    val reminderIntervalHours: String = "0",
    /** "" = system, "en", "ru" */
    val appLanguageTag: String = "",
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val saveThresholdsUseCase: SaveThresholdsUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val appLocaleManager: AppLocaleManager,
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            val settings = settingsRepository.observeSettings().first()
            uiState = settings.toUiState()
        }
    }

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        uiState = transform(uiState)
    }

    fun save() {
        viewModelScope.launch {
            saveUserProfileUseCase(
                displayName = uiState.displayName,
                diabetesType = uiState.diabetesType,
                ageGroup = uiState.ageGroup,
            )
            val current = settingsRepository.observeSettings().first()
            val updated = current.copy(
                displayName = uiState.displayName,
                diabetesType = uiState.diabetesType,
                ageGroup = uiState.ageGroup,
                glucoseUnit = uiState.unit,
                targetLow = uiState.targetLow.toDoubleOrNull() ?: current.targetLow,
                targetHigh = uiState.targetHigh.toDoubleOrNull() ?: current.targetHigh,
                warningLow = uiState.warningLow.toDoubleOrNull() ?: current.warningLow,
                warningHigh = uiState.warningHigh.toDoubleOrNull() ?: current.warningHigh,
                criticalLow = uiState.criticalLow.toDoubleOrNull() ?: current.criticalLow,
                criticalHigh = uiState.criticalHigh.toDoubleOrNull() ?: current.criticalHigh,
                rapidRiseThresholdPer15Min = uiState.rapidRise.toDoubleOrNull() ?: current.rapidRiseThresholdPer15Min,
                rapidFallThresholdPer15Min = uiState.rapidFall.toDoubleOrNull() ?: current.rapidFallThresholdPer15Min,
                prolongedOutOfRangeMinutes = uiState.prolongedMinutes.toLongOrNull() ?: current.prolongedOutOfRangeMinutes,
                patternWindowHours = uiState.patternWindowHours.toLongOrNull() ?: current.patternWindowHours,
                reminderIntervalHours = uiState.reminderIntervalHours.toLongOrNull() ?: current.reminderIntervalHours,
                appLanguageTag = uiState.appLanguageTag,
            )
            saveThresholdsUseCase(updated)
            reminderScheduler.schedule(updated.reminderIntervalHours)
            appLocaleManager.applyPreferredLanguage(updated.appLanguageTag)
            uiState = updated.toUiState(message = context.getString(R.string.settings_saved))
        }
    }

    private fun UserSettings.toUiState(message: String? = null): SettingsUiState = SettingsUiState(
        displayName = displayName,
        diabetesType = diabetesType,
        ageGroup = ageGroup,
        unit = glucoseUnit,
        targetLow = targetLow.toString(),
        targetHigh = targetHigh.toString(),
        warningLow = warningLow.toString(),
        warningHigh = warningHigh.toString(),
        criticalLow = criticalLow.toString(),
        criticalHigh = criticalHigh.toString(),
        rapidRise = rapidRiseThresholdPer15Min.toString(),
        rapidFall = rapidFallThresholdPer15Min.toString(),
        prolongedMinutes = prolongedOutOfRangeMinutes.toString(),
        patternWindowHours = patternWindowHours.toString(),
        reminderIntervalHours = reminderIntervalHours.toString(),
        appLanguageTag = appLanguageTag,
        message = message,
    )
}
