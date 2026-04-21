package com.gk.diaguide.presentation.onboarding

import com.gk.diaguide.domain.model.UserSettings

fun UserSettings.toOnboardingUiState(): OnboardingUiState = OnboardingUiState(
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
    disclaimerAccepted = disclaimerAccepted,
    error = null,
)
