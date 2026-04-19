package com.gk.diaguide.domain.engine

import com.gk.diaguide.domain.model.DetectedPattern
import com.gk.diaguide.domain.model.PatternType
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.domain.model.UserSettings
import java.util.UUID
import javax.inject.Inject

class RecommendationEngine @Inject constructor() {

    fun generate(
        patterns: List<DetectedPattern>,
        settings: UserSettings,
    ): List<Recommendation> {
        return patterns.mapNotNull { pattern ->
            when (pattern.type) {
                PatternType.CRITICAL_LOW -> recommendation(
                    title = "Urgent: value below critical low threshold",
                    explanation = "Re-check your glucose promptly and seek medical advice or emergency help if you feel unwell.",
                    severity = RecommendationSeverity.URGENT,
                    pattern = pattern,
                    action = "Open chart",
                )

                PatternType.CRITICAL_HIGH -> recommendation(
                    title = "Urgent: value above critical high threshold",
                    explanation = "Review recent notes and symptoms now. If symptoms are severe, contact a clinician or emergency services.",
                    severity = RecommendationSeverity.URGENT,
                    pattern = pattern,
                    action = "Open chart",
                )

                PatternType.LOW_VALUE -> recommendation(
                    title = "Low glucose reading detected",
                    explanation = "Repeat the check soon and record symptoms if present. Compare with recent meal or activity notes.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "Log symptoms",
                )

                PatternType.HIGH_VALUE -> recommendation(
                    title = "High glucose reading detected",
                    explanation = "Review recent meals, activity, stress, and note whether this episode repeats.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "View history",
                )

                PatternType.RAPID_RISE -> recommendation(
                    title = "Rapid glucose rise detected",
                    explanation = "Check recent food intake and watch the next readings to confirm whether the trend persists.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "Open chart",
                )

                PatternType.RAPID_FALL -> recommendation(
                    title = "Rapid glucose fall detected",
                    explanation = "Consider re-checking your glucose after a short interval and record any symptoms.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "Open chart",
                )

                PatternType.PROLONGED_OUT_OF_RANGE -> recommendation(
                    title = "Prolonged deviation from target range",
                    explanation = "A sustained period outside target was detected. Keep the episode documented and consider discussing repeated episodes with your clinician.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "View history",
                )

                PatternType.REPEATED_DEVIATIONS -> recommendation(
                    title = "Repeated deviations pattern",
                    explanation = "Several separate deviations were found in the configured window. This may justify a closer review with your healthcare team if it continues.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "View recommendations",
                )

                PatternType.MORNING_HIGHS -> recommendation(
                    title = "Morning high pattern",
                    explanation = "Frequent morning elevations were detected. Review routine factors and note whether the pattern repeats on subsequent days.",
                    severity = RecommendationSeverity.INFORMATIONAL,
                    pattern = pattern,
                    action = "Open chart",
                )

                PatternType.POST_MEAL_SPIKE -> recommendation(
                    title = "Possible post-meal spike",
                    explanation = "Meal-tagged entries were followed by elevated values. Keep logging meals to make this pattern easier to evaluate.",
                    severity = RecommendationSeverity.INFORMATIONAL,
                    pattern = pattern,
                    action = "Add note",
                )

                PatternType.NIGHT_LOW -> recommendation(
                    title = "Night low pattern",
                    explanation = "Repeated low values were detected at night. Track recurrence and consider discussing it with a clinician if it continues.",
                    severity = RecommendationSeverity.WARNING,
                    pattern = pattern,
                    action = "View history",
                )
            }
        }
    }

    private fun recommendation(
        title: String,
        explanation: String,
        severity: RecommendationSeverity,
        pattern: DetectedPattern,
        action: String?,
    ): Recommendation = Recommendation(
        id = UUID.randomUUID().toString(),
        title = title,
        shortExplanation = explanation,
        severity = severity,
        timestamp = pattern.detectedAt,
        relatedDetectedPattern = pattern.relatedPatternLabel,
        actionButtonLabel = action,
    )
}
