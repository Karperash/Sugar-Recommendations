package com.gk.diaguide.domain.model

import java.time.Instant

data class DetectedPattern(
    val type: PatternType,
    val title: String,
    val explanation: String,
    val severity: RecommendationSeverity,
    val detectedAt: Instant,
    val relatedPatternLabel: String = type.name,
)
