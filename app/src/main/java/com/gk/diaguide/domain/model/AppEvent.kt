package com.gk.diaguide.domain.model

import java.time.Instant

data class AppEvent(
    val id: String,
    val timestamp: Instant,
    val type: EventType,
    val title: String,
    val description: String,
    val relatedRecordId: String? = null,
)
