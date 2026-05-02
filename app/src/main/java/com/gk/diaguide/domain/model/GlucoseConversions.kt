package com.gk.diaguide.domain.model

private const val MG_DL_PER_MMOL_L = 18.0

fun Double.convertGlucose(from: GlucoseUnit, to: GlucoseUnit): Double {
    if (from == to) return this
    return when (from) {
        GlucoseUnit.MG_DL -> this / MG_DL_PER_MMOL_L
        GlucoseUnit.MMOL_L -> this * MG_DL_PER_MMOL_L
    }
}

fun CgmRecord.toUnit(targetUnit: GlucoseUnit): CgmRecord {
    if (unit == targetUnit) return this
    return copy(
        glucoseValue = glucoseValue.convertGlucose(unit, targetUnit),
        unit = targetUnit,
    )
}

/**
 * Переводит пороги и скорости изменения в другую единицу
 * (значения в [UserSettings] хранятся в [UserSettings.glucoseUnit]).
 */
fun UserSettings.withGlucoseUnit(unit: GlucoseUnit): UserSettings {
    if (glucoseUnit == unit) return this
    return copy(
        glucoseUnit = unit,
        targetLow = targetLow.convertGlucose(glucoseUnit, unit),
        targetHigh = targetHigh.convertGlucose(glucoseUnit, unit),
        warningLow = warningLow.convertGlucose(glucoseUnit, unit),
        warningHigh = warningHigh.convertGlucose(glucoseUnit, unit),
        criticalLow = criticalLow.convertGlucose(glucoseUnit, unit),
        criticalHigh = criticalHigh.convertGlucose(glucoseUnit, unit),
        rapidRiseThresholdPer15Min = rapidRiseThresholdPer15Min.convertGlucose(glucoseUnit, unit),
        rapidFallThresholdPer15Min = rapidFallThresholdPer15Min.convertGlucose(glucoseUnit, unit),
    )
}

