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

    /**
     * Профиль под серии OhioT1DM (значения в мг/дл, интервал 5 мин).
     * Применяется вручную в настройках как отправная точка для калибровки правил.
     */
    fun ohioT1DmResearchMgDl(): UserSettings = UserSettings(
        glucoseUnit = GlucoseUnit.MG_DL,
        targetLow = 70.0,
        targetHigh = 180.0,
        warningLow = 65.0,
        warningHigh = 200.0,
        criticalLow = 54.0,
        criticalHigh = 300.0,
        rapidRiseThresholdPer15Min = 30.0,
        rapidFallThresholdPer15Min = 30.0,
        prolongedOutOfRangeMinutes = 90L,
        patternWindowHours = 24L,
    )
}
