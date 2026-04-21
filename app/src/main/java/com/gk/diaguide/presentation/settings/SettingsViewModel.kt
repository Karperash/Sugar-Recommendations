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
import com.gk.diaguide.domain.model.RecommendationPresets
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.SaveThresholdsUseCase
import com.gk.diaguide.domain.usecase.SaveUserProfileUseCase
import com.gk.diaguide.data.notification.ReminderScheduler
import com.gk.diaguide.presentation.onboarding.OnboardingUiState
import com.gk.diaguide.presentation.onboarding.toOnboardingUiState
import kotlinx.coroutines.flow.collect
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

    var introUiState by mutableStateOf(OnboardingUiState())
        private set

    var settingsIntroCompleted by mutableStateOf(false)
        private set

    private var introHydrated = false

    init {
        viewModelScope.launch {
            settingsRepository.observeSettings().collect { settings ->
                settingsIntroCompleted = settings.settingsIntroCompleted
                uiState = settings.toUiState()
                if (!settings.settingsIntroCompleted && !introHydrated) {
                    introUiState = settings.toOnboardingUiState()
                    introHydrated = true
                }
            }
        }
    }

    fun update(transform: (SettingsUiState) -> SettingsUiState) {
        uiState = transform(uiState)
    }

    fun updateIntro(transform: (OnboardingUiState) -> OnboardingUiState) {
        introUiState = transform(introUiState)
    }

    /** Первичный блок как на старом онбординге: пороги + отказ; сохраняет профиль и помечает первый проход. */
    fun completeFirstTimeIntro() {
        val ui = introUiState
        val low = ui.targetLow.toDoubleOrNull()
        val high = ui.targetHigh.toDoubleOrNull()
        val warningLow = ui.warningLow.toDoubleOrNull()
        val warningHigh = ui.warningHigh.toDoubleOrNull()
        val criticalLow = ui.criticalLow.toDoubleOrNull()
        val criticalHigh = ui.criticalHigh.toDoubleOrNull()

        if (!ui.disclaimerAccepted ||
            listOf(low, high, warningLow, warningHigh, criticalLow, criticalHigh).any { it == null }
        ) {
            introUiState = ui.copy(error = context.getString(R.string.onboarding_error))
            return
        }

        viewModelScope.launch {
            val current = settingsRepository.observeSettings().first()
            val updated = current.copy(
                displayName = ui.displayName,
                diabetesType = ui.diabetesType,
                ageGroup = ui.ageGroup,
                glucoseUnit = ui.unit,
                targetLow = low!!,
                targetHigh = high!!,
                warningLow = warningLow!!,
                warningHigh = warningHigh!!,
                criticalLow = criticalLow!!,
                criticalHigh = criticalHigh!!,
                settingsIntroCompleted = true,
                onboardingCompleted = true,
                disclaimerAccepted = ui.disclaimerAccepted,
                appLanguageTag = RUSSIAN_LOCALE_TAG,
            )
            saveUserProfileUseCase(
                displayName = updated.displayName,
                diabetesType = updated.diabetesType,
                ageGroup = updated.ageGroup,
            )
            saveThresholdsUseCase(updated)
            settingsRepository.completeOnboarding(completed = true, disclaimerAccepted = ui.disclaimerAccepted)
            reminderScheduler.schedule(updated.reminderIntervalHours)
            appLocaleManager.applyPreferredLanguage(RUSSIAN_LOCALE_TAG)
            introUiState = introUiState.copy(error = null)
            uiState = updated.toUiState(message = context.getString(R.string.settings_intro_completed))
        }
    }

    /** Пороги под датасет T1D-UOM с Zenodo (ммоль/л). Сохранение — кнопкой «Сохранить». */
    fun applyZenodoT1dUomPreset() {
        val p = RecommendationPresets.zenodoT1dUomMmolL()
        uiState = uiState.copy(
            unit = p.glucoseUnit,
            targetLow = p.targetLow.toString(),
            targetHigh = p.targetHigh.toString(),
            warningLow = p.warningLow.toString(),
            warningHigh = p.warningHigh.toString(),
            criticalLow = p.criticalLow.toString(),
            criticalHigh = p.criticalHigh.toString(),
            rapidRise = p.rapidRiseThresholdPer15Min.toString(),
            rapidFall = p.rapidFallThresholdPer15Min.toString(),
            prolongedMinutes = p.prolongedOutOfRangeMinutes.toString(),
            patternWindowHours = p.patternWindowHours.toString(),
            message = context.getString(R.string.settings_preset_zenodo_applied),
        )
    }

    /** Заполняет пороги под исследование на рядах OhioT1DM (мг/дл); сохранение — отдельно кнопкой «Сохранить». */
    fun applyOhioResearchPreset() {
        val p = RecommendationPresets.ohioT1DmResearchMgDl()
        uiState = uiState.copy(
            unit = p.glucoseUnit,
            targetLow = p.targetLow.toString(),
            targetHigh = p.targetHigh.toString(),
            warningLow = p.warningLow.toString(),
            warningHigh = p.warningHigh.toString(),
            criticalLow = p.criticalLow.toString(),
            criticalHigh = p.criticalHigh.toString(),
            rapidRise = p.rapidRiseThresholdPer15Min.toString(),
            rapidFall = p.rapidFallThresholdPer15Min.toString(),
            prolongedMinutes = p.prolongedOutOfRangeMinutes.toString(),
            patternWindowHours = p.patternWindowHours.toString(),
            message = context.getString(R.string.settings_preset_ohio_applied),
        )
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
                appLanguageTag = RUSSIAN_LOCALE_TAG,
                settingsIntroCompleted = current.settingsIntroCompleted,
            )
            saveThresholdsUseCase(updated)
            reminderScheduler.schedule(updated.reminderIntervalHours)
            appLocaleManager.applyPreferredLanguage(RUSSIAN_LOCALE_TAG)
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
        message = message,
    )

    private companion object {
        const val RUSSIAN_LOCALE_TAG = "ru"
    }
}
