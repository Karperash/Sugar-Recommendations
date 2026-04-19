package com.gk.diaguide.domain.model

import java.time.Instant

data class CgmRecord(
    val id: String,
    val timestamp: Instant,
    val glucoseValue: Double,
    val unit: GlucoseUnit,
    val trendDirection: TrendDirection = TrendDirection.UNKNOWN,
    val source: EntrySource = EntrySource.MANUAL,
    val note: String? = null,
    val meal: Boolean = false,
    val insulin: Boolean = false,
    val activity: Boolean = false,
    val sleep: Boolean = false,
    val stress: Boolean = false,
    val symptom: Boolean = false,
)
