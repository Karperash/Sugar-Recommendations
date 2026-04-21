package com.gk.diaguide.presentation.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.SaveThresholdsUseCase
import com.gk.diaguide.domain.usecase.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val displayName: String = "",
    val diabetesType: String = "",
    val ageGroup: String = "",
    val biologicalSex: String = "",
    val unit: GlucoseUnit = GlucoseUnit.MG_DL,
    val targetLow: String = "80",
    val targetHigh: String = "140",
    val warningLow: String = "70",
    val warningHigh: String = "180",
    val criticalLow: String = "55",
    val criticalHigh: String = "250",
    val disclaimerAccepted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val saveThresholdsUseCase: SaveThresholdsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState())
        private set

    private val events = Channel<Boolean>(Channel.BUFFERED)
    val completed = events.receiveAsFlow()

    fun update(transform: (OnboardingUiState) -> OnboardingUiState) {
        uiState = transform(uiState)
    }

    fun save() {
        val low = uiState.targetLow.toDoubleOrNull()
        val high = uiState.targetHigh.toDoubleOrNull()
        val warningLow = uiState.warningLow.toDoubleOrNull()
        val warningHigh = uiState.warningHigh.toDoubleOrNull()
        val criticalLow = uiState.criticalLow.toDoubleOrNull()
        val criticalHigh = uiState.criticalHigh.toDoubleOrNull()

        if (!uiState.disclaimerAccepted || listOf(low, high, warningLow, warningHigh, criticalLow, criticalHigh).any { it == null }) {
            uiState = uiState.copy(error = "Fill numeric thresholds and accept the disclaimer to continue.")
            return
        }

        viewModelScope.launch {
            saveUserProfileUseCase(
                displayName = uiState.displayName,
                diabetesType = uiState.diabetesType,
                ageGroup = uiState.ageGroup,
                biologicalSex = uiState.biologicalSex,
            )
            saveThresholdsUseCase(
                UserSettings(
                    displayName = uiState.displayName,
                    diabetesType = uiState.diabetesType,
                    ageGroup = uiState.ageGroup,
                    biologicalSex = uiState.biologicalSex,
                    glucoseUnit = uiState.unit,
                    targetLow = low ?: 80.0,
                    targetHigh = high ?: 140.0,
                    warningLow = warningLow ?: 70.0,
                    warningHigh = warningHigh ?: 180.0,
                    criticalLow = criticalLow ?: 55.0,
                    criticalHigh = criticalHigh ?: 250.0,
                    onboardingCompleted = true,
                    disclaimerAccepted = uiState.disclaimerAccepted,
                    settingsIntroCompleted = true,
                ),
            )
            settingsRepository.completeOnboarding(completed = true, disclaimerAccepted = uiState.disclaimerAccepted)
            events.send(true)
        }
    }
}
