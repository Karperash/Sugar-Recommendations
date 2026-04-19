package com.gk.diaguide.domain.model

import java.time.Instant

data class Recommendation(
    val id: String,
    val title: String,
    val shortExplanation: String,
    val severity: RecommendationSeverity,
    val timestamp: Instant,
    val relatedDetectedPattern: String,
    val actionButtonLabel: String? = null,
)
