package com.gk.diaguide.domain.model

import java.time.LocalDate

data class DailySummary(
    val date: LocalDate,
    val minimum: Double,
    val maximum: Double,
    val average: Double,
    val inRangePercent: Double,
    val totalReadings: Int,
    val lowReadings: Int,
    val highReadings: Int,
)
