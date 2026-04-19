package com.gk.diaguide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cgm_records")
data class CgmRecordEntity(
    @PrimaryKey val id: String,
    val timestampMillis: Long,
    val glucoseValue: Double,
    val unit: String,
    val trendDirection: String,
    val source: String,
    val note: String?,
    val meal: Boolean,
    val insulin: Boolean,
    val activity: Boolean,
    val sleep: Boolean,
    val stress: Boolean,
    val symptom: Boolean,
)

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val shortExplanation: String,
    val severity: String,
    val timestampMillis: Long,
    val relatedDetectedPattern: String,
    val actionButtonLabel: String?,
)

@Entity(tableName = "app_events")
data class AppEventEntity(
    @PrimaryKey val id: String,
    val timestampMillis: Long,
    val type: String,
    val title: String,
    val description: String,
    val relatedRecordId: String?,
)
