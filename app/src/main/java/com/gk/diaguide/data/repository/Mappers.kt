package com.gk.diaguide.data.repository

import com.gk.diaguide.data.local.AppEventEntity
import com.gk.diaguide.data.local.CgmRecordEntity
import com.gk.diaguide.data.local.RecommendationEntity
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.EventType
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.domain.model.TrendDirection
import java.time.Instant

fun CgmRecordEntity.toDomain(): CgmRecord = CgmRecord(
    id = id,
    timestamp = Instant.ofEpochMilli(timestampMillis),
    glucoseValue = glucoseValue,
    unit = GlucoseUnit.valueOf(unit),
    trendDirection = TrendDirection.valueOf(trendDirection),
    source = EntrySource.valueOf(source),
    note = note,
    meal = meal,
    insulin = insulin,
    activity = activity,
    sleep = sleep,
    stress = stress,
    symptom = symptom,
)

fun CgmRecord.toEntity(): CgmRecordEntity = CgmRecordEntity(
    id = id,
    timestampMillis = timestamp.toEpochMilli(),
    glucoseValue = glucoseValue,
    unit = unit.name,
    trendDirection = trendDirection.name,
    source = source.name,
    note = note,
    meal = meal,
    insulin = insulin,
    activity = activity,
    sleep = sleep,
    stress = stress,
    symptom = symptom,
)

fun RecommendationEntity.toDomain(): Recommendation = Recommendation(
    id = id,
    title = title,
    shortExplanation = shortExplanation,
    severity = RecommendationSeverity.valueOf(severity),
    timestamp = Instant.ofEpochMilli(timestampMillis),
    relatedDetectedPattern = relatedDetectedPattern,
    actionButtonLabel = actionButtonLabel,
)

fun Recommendation.toEntity(): RecommendationEntity = RecommendationEntity(
    id = id,
    title = title,
    shortExplanation = shortExplanation,
    severity = severity.name,
    timestampMillis = timestamp.toEpochMilli(),
    relatedDetectedPattern = relatedDetectedPattern,
    actionButtonLabel = actionButtonLabel,
)

fun AppEventEntity.toDomain(): AppEvent = AppEvent(
    id = id,
    timestamp = Instant.ofEpochMilli(timestampMillis),
    type = EventType.valueOf(type),
    title = title,
    description = description,
    relatedRecordId = relatedRecordId,
)

fun AppEvent.toEntity(): AppEventEntity = AppEventEntity(
    id = id,
    timestampMillis = timestamp.toEpochMilli(),
    type = type.name,
    title = title,
    description = description,
    relatedRecordId = relatedRecordId,
)
