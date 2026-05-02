package com.gk.diaguide.presentation.settings

import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.convertGlucose
import java.util.Locale

fun SettingsUiState.convertGlucoseThresholdStringsToUnit(newUnit: GlucoseUnit): SettingsUiState {
    if (unit == newUnit) return this
    fun conv(s: String): String {
        val v = s.toDoubleOrNull() ?: return s
        val c = v.convertGlucose(unit, newUnit)
        return if (newUnit == GlucoseUnit.MG_DL) String.format(Locale.US, "%.0f", c) else String.format(Locale.US, "%.1f", c)
    }
    return copy(
        unit = newUnit,
        targetLow = conv(targetLow),
        targetHigh = conv(targetHigh),
        warningLow = conv(warningLow),
        warningHigh = conv(warningHigh),
        criticalLow = conv(criticalLow),
        criticalHigh = conv(criticalHigh),
        rapidRise = conv(rapidRise),
        rapidFall = conv(rapidFall),
    )
}
