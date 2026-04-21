package com.gk.diaguide.core.util

import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.domain.model.TrendDirection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun GlucoseUnit.shortLabelRu(): String = when (this) {
    GlucoseUnit.MG_DL -> "мг/дл"
    GlucoseUnit.MMOL_L -> "ммоль/л"
}

fun Double.formatGlucose(unit: GlucoseUnit): String =
    if (unit == GlucoseUnit.MG_DL) "%.0f %s".format(this, unit.shortLabelRu())
    else "%.1f %s".format(this, unit.shortLabelRu())

fun Instant.formatDateTime(zoneId: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(atZone(zoneId))

fun TrendDirection.asArrow(): String = when (this) {
    TrendDirection.DOUBLE_UP -> "⇈"
    TrendDirection.UP -> "↑"
    TrendDirection.FLAT -> "→"
    TrendDirection.DOWN -> "↓"
    TrendDirection.DOUBLE_DOWN -> "⇊"
    TrendDirection.UNKNOWN -> "•"
}

fun RecommendationSeverity.label(): String = when (this) {
    RecommendationSeverity.INFORMATIONAL -> "Информация"
    RecommendationSeverity.WARNING -> "Внимание"
    RecommendationSeverity.URGENT -> "Срочно"
}
