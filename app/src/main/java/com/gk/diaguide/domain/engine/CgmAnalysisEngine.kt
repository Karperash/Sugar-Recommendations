package com.gk.diaguide.domain.engine

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.DailySummary
import com.gk.diaguide.domain.model.DetectedPattern
import com.gk.diaguide.domain.model.PatternType
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.domain.model.UserSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

data class AnalysisResult(
    val summary: DailySummary,
    val patterns: List<DetectedPattern>,
)

class CgmAnalysisEngine @Inject constructor() {

    fun analyze(
        records: List<CgmRecord>,
        settings: UserSettings,
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): AnalysisResult {
        val dayRecords = records
            .filter { it.timestamp.atZone(zoneId).toLocalDate() == date }
            .sortedBy { it.timestamp }

        val summary = buildSummary(dayRecords, settings, date)
        if (dayRecords.isEmpty()) {
            return AnalysisResult(summary = summary, patterns = emptyList())
        }

        val patterns = mutableListOf<DetectedPattern>()
        val now = dayRecords.last().timestamp

        if (dayRecords.any { it.glucoseValue <= settings.criticalLow }) {
            patterns += pattern(
                PatternType.CRITICAL_LOW,
                "Detected critical low value",
                "At least one value reached the user-defined critical low threshold.",
                RecommendationSeverity.URGENT,
                now,
            )
        }

        if (dayRecords.any { it.glucoseValue >= settings.criticalHigh }) {
            patterns += pattern(
                PatternType.CRITICAL_HIGH,
                "Detected critical high value",
                "At least one value reached the user-defined critical high threshold.",
                RecommendationSeverity.URGENT,
                now,
            )
        }

        if (dayRecords.any { it.glucoseValue < settings.targetLow }) {
            patterns += pattern(
                PatternType.LOW_VALUE,
                "Values below target range",
                "At least one reading fell below the configured target range.",
                RecommendationSeverity.WARNING,
                now,
            )
        }

        if (dayRecords.any { it.glucoseValue > settings.targetHigh }) {
            patterns += pattern(
                PatternType.HIGH_VALUE,
                "Values above target range",
                "At least one reading exceeded the configured target range.",
                RecommendationSeverity.WARNING,
                now,
            )
        }

        patterns += detectRapidChanges(dayRecords, settings)
        patterns += detectProlongedOutOfRange(dayRecords, settings)
        patterns += detectRepeatedDeviations(dayRecords, settings)
        patterns += detectMorningHighs(dayRecords, settings)
        patterns += detectNightLows(dayRecords, settings)
        patterns += detectPostMealSpikes(dayRecords, settings)

        return AnalysisResult(summary = summary, patterns = patterns.distinctBy { it.type })
    }

    private fun buildSummary(
        records: List<CgmRecord>,
        settings: UserSettings,
        date: LocalDate,
    ): DailySummary {
        if (records.isEmpty()) {
            return DailySummary(
                date = date,
                minimum = 0.0,
                maximum = 0.0,
                average = 0.0,
                inRangePercent = 0.0,
                totalReadings = 0,
                lowReadings = 0,
                highReadings = 0,
            )
        }

        val min = records.minOf { it.glucoseValue }
        val max = records.maxOf { it.glucoseValue }
        val avg = records.map { it.glucoseValue }.average()
        val inRange = records.count { it.glucoseValue in settings.targetLow..settings.targetHigh }
        val low = records.count { it.glucoseValue < settings.targetLow }
        val high = records.count { it.glucoseValue > settings.targetHigh }

        return DailySummary(
            date = date,
            minimum = min,
            maximum = max,
            average = avg,
            inRangePercent = (inRange.toDouble() / records.size.toDouble()) * 100.0,
            totalReadings = records.size,
            lowReadings = low,
            highReadings = high,
        )
    }

    private fun detectRapidChanges(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        val rapidRise = records.zipWithNext().firstOrNull { (prev, next) ->
            val minutes = ((next.timestamp.toEpochMilli() - prev.timestamp.toEpochMilli()) / 60000.0).coerceAtLeast(1.0)
            ((next.glucoseValue - prev.glucoseValue) / minutes) * 15.0 >= settings.rapidRiseThresholdPer15Min
        }

        val rapidFall = records.zipWithNext().firstOrNull { (prev, next) ->
            val minutes = ((next.timestamp.toEpochMilli() - prev.timestamp.toEpochMilli()) / 60000.0).coerceAtLeast(1.0)
            ((prev.glucoseValue - next.glucoseValue) / minutes) * 15.0 >= settings.rapidFallThresholdPer15Min
        }

        val result = mutableListOf<DetectedPattern>()
        rapidRise?.let { (_, next) ->
            result += pattern(
                PatternType.RAPID_RISE,
                "Rapid glucose increase",
                "A reading-to-reading change exceeded the configured rise threshold per 15 minutes.",
                RecommendationSeverity.WARNING,
                next.timestamp,
            )
        }
        rapidFall?.let { (_, next) ->
            result += pattern(
                PatternType.RAPID_FALL,
                "Rapid glucose decrease",
                "A reading-to-reading change exceeded the configured fall threshold per 15 minutes.",
                RecommendationSeverity.WARNING,
                next.timestamp,
            )
        }
        return result
    }

    private fun detectProlongedOutOfRange(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        var episodeStart: CgmRecord? = null
        var last: CgmRecord? = null

        for (record in records) {
            val outOfRange = record.glucoseValue < settings.targetLow || record.glucoseValue > settings.targetHigh
            if (outOfRange && episodeStart == null) {
                episodeStart = record
            }
            if (!outOfRange && episodeStart != null && last != null) {
                val minutes = (last.timestamp.toEpochMilli() - episodeStart.timestamp.toEpochMilli()) / 60000L
                if (minutes >= settings.prolongedOutOfRangeMinutes) {
                    return listOf(
                        pattern(
                            PatternType.PROLONGED_OUT_OF_RANGE,
                            "Prolonged out-of-range period",
                            "The glucose level stayed outside the configured target range for at least ${settings.prolongedOutOfRangeMinutes} minutes.",
                            RecommendationSeverity.WARNING,
                            last.timestamp,
                        ),
                    )
                }
                episodeStart = null
            }
            last = record
        }

        if (episodeStart != null && last != null) {
            val minutes = (last.timestamp.toEpochMilli() - episodeStart.timestamp.toEpochMilli()) / 60000L
            if (minutes >= settings.prolongedOutOfRangeMinutes) {
                return listOf(
                    pattern(
                        PatternType.PROLONGED_OUT_OF_RANGE,
                        "Prolonged out-of-range period",
                        "The glucose level stayed outside the configured target range for at least ${settings.prolongedOutOfRangeMinutes} minutes.",
                        RecommendationSeverity.WARNING,
                        last.timestamp,
                    ),
                )
            }
        }
        return emptyList()
    }

    private fun detectRepeatedDeviations(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        val windowStart = records.maxOfOrNull { it.timestamp.toEpochMilli() }?.minus(settings.patternWindowHours * 60 * 60 * 1000)
            ?: return emptyList()

        val recent = records.filter { it.timestamp.toEpochMilli() >= windowStart }
        var episodes = 0
        var previousState = 0

        recent.forEach { record ->
            val state = when {
                record.glucoseValue < settings.targetLow -> -1
                record.glucoseValue > settings.targetHigh -> 1
                else -> 0
            }
            if (state != 0 && state != previousState) {
                episodes += 1
            }
            previousState = state
        }

        return if (episodes >= 3) {
            listOf(
                pattern(
                    PatternType.REPEATED_DEVIATIONS,
                    "Repeated deviations detected",
                    "Several separate deviations were detected in the last ${settings.patternWindowHours} hours.",
                    RecommendationSeverity.WARNING,
                    recent.lastOrNull()?.timestamp ?: Instant.now(),
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun detectMorningHighs(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        val highs = records.filter {
            val hour = it.timestamp.atZone(ZoneId.systemDefault()).hour
            hour in 6..10 && it.glucoseValue > settings.targetHigh
        }
        return if (highs.size >= 2) {
            listOf(
                pattern(
                    PatternType.MORNING_HIGHS,
                    "Frequent morning highs",
                    "Multiple elevated readings were detected in the morning window.",
                    RecommendationSeverity.INFORMATIONAL,
                    highs.last().timestamp,
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun detectNightLows(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        val lows = records.filter {
            val hour = it.timestamp.atZone(ZoneId.systemDefault()).hour
            hour in 0..5 && it.glucoseValue < settings.targetLow
        }
        return if (lows.size >= 2) {
            listOf(
                pattern(
                    PatternType.NIGHT_LOW,
                    "Repeated night lows",
                    "Multiple low readings were detected during the night period.",
                    RecommendationSeverity.WARNING,
                    lows.last().timestamp,
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun detectPostMealSpikes(
        records: List<CgmRecord>,
        settings: UserSettings,
    ): List<DetectedPattern> {
        records.filter { it.meal }.forEach { mealRecord ->
            val inWindow = records.filter { candidate ->
                val minutes = (candidate.timestamp.toEpochMilli() - mealRecord.timestamp.toEpochMilli()) / 60000L
                minutes in 1..120
            }
            val spike = inWindow.firstOrNull { candidate ->
                candidate.glucoseValue > settings.targetHigh ||
                    abs(candidate.glucoseValue - mealRecord.glucoseValue) >= settings.rapidRiseThresholdPer15Min
            }
            if (spike != null) {
                return listOf(
                    pattern(
                        PatternType.POST_MEAL_SPIKE,
                        "Post-meal spike pattern",
                        "A meal tag was followed by an elevated or sharply increasing glucose value within two hours.",
                        RecommendationSeverity.INFORMATIONAL,
                        spike.timestamp,
                    ),
                )
            }
        }
        return emptyList()
    }

    private fun pattern(
        type: PatternType,
        title: String,
        explanation: String,
        severity: RecommendationSeverity,
        detectedAt: Instant,
    ): DetectedPattern = DetectedPattern(
        type = type,
        title = title,
        explanation = explanation,
        severity = severity,
        detectedAt = detectedAt,
        relatedPatternLabel = type.name.lowercase(Locale.US),
    )
}
