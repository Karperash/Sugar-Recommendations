package com.gk.diaguide.domain.model

data class UserSettings(
    val displayName: String = "",
    val diabetesType: String = "",
    val ageGroup: String = "",
    val biologicalSex: String = "",
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val glucoseUnit: GlucoseUnit = GlucoseUnit.MG_DL,
    val targetLow: Double = 80.0,
    val targetHigh: Double = 140.0,
    val warningLow: Double = 70.0,
    val warningHigh: Double = 180.0,
    val criticalLow: Double = 55.0,
    val criticalHigh: Double = 250.0,
    val rapidRiseThresholdPer15Min: Double = 20.0,
    val rapidFallThresholdPer15Min: Double = 20.0,
    val prolongedOutOfRangeMinutes: Long = 45,
    val patternWindowHours: Long = 24,
    val reminderIntervalHours: Long = 0,
    /** Интерфейс только на русском (РФ). */
    val appLanguageTag: String = "ru",
    val onboardingCompleted: Boolean = false,
    val disclaimerAccepted: Boolean = false,
    /** Первичная анкета пройдена (раньше — полноэкранный онбординг; теперь — из «Настроек»). */
    val settingsIntroCompleted: Boolean = false,
) {
    fun bodyMassIndex(): Double? {
        val weight = weightKg ?: return null
        val heightMeters = (heightCm ?: return null) / 100.0
        if (weight <= 0.0 || heightMeters <= 0.0) return null
        return weight / (heightMeters * heightMeters)
    }
}
