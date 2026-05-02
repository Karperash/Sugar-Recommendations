package com.gk.diaguide.domain.model

/**
 * Готовые наборы порогов для исследований на реальных CGM-рядах.
 *
 * Не являются медицинским назначением — только стартовые значения для калибровки правил.
 */
object RecommendationPresets {

    /**
     * Профиль под открытый датасет **T1D-UOM** (Zenodo, глюкоза в **ммоль/л**).
     * Ориентир по коридору, близкому к 3,9–10 ммоль/л (~70–180 мг/дл).
     */
    fun zenodoT1dUomMmolL(): UserSettings = UserSettings(
        glucoseUnit = GlucoseUnit.MMOL_L,
        targetLow = 3.9,
        targetHigh = 10.0,
        warningLow = 3.6,
        warningHigh = 11.1,
        criticalLow = 3.0,
        criticalHigh = 16.7,
        rapidRiseThresholdPer15Min = 1.7,
        rapidFallThresholdPer15Min = 1.7,
        prolongedOutOfRangeMinutes = 90L,
        patternWindowHours = 24L,
    )
}
