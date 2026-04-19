package com.gk.diaguide.domain.engine

import com.gk.diaguide.domain.model.DetectedPattern
import com.gk.diaguide.domain.model.PatternType
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.domain.model.UserSettings
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class RecommendationEngineTest {

    private val engine = RecommendationEngine()

    @Test
    fun mapsCriticalLowToUrgentRecommendation() {
        val patterns = listOf(
            DetectedPattern(
                type = PatternType.CRITICAL_LOW,
                title = "Critical low",
                explanation = "test",
                severity = RecommendationSeverity.URGENT,
                detectedAt = Instant.parse("2026-03-25T08:00:00Z"),
            ),
        )

        val recommendations = engine.generate(patterns, UserSettings())

        assertThat(recommendations).hasSize(1)
        assertThat(recommendations.first().severity).isEqualTo(RecommendationSeverity.URGENT)
    }
}
