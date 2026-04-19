package com.gk.diaguide.domain.engine

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.PatternType
import com.gk.diaguide.domain.model.TrendDirection
import com.gk.diaguide.domain.model.UserSettings
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import org.junit.Test

class CgmAnalysisEngineTest {

    private val engine = CgmAnalysisEngine()
    private val settings = UserSettings()

    @Test
    fun detectsRapidRiseAndHighValue() {
        val date = LocalDate.of(2026, 3, 25)
        val records = listOf(
            record("2026-03-25T08:00:00Z", 110.0),
            record("2026-03-25T08:15:00Z", 145.0),
            record("2026-03-25T08:30:00Z", 168.0),
        )

        val result = engine.analyze(records, settings, date)

        assertThat(result.patterns.map { it.type }).contains(PatternType.RAPID_RISE)
        assertThat(result.patterns.map { it.type }).contains(PatternType.HIGH_VALUE)
    }

    @Test
    fun detectsNightLowPattern() {
        val date = LocalDate.of(2026, 3, 25)
        val records = listOf(
            record("2026-03-25T01:00:00Z", 68.0),
            record("2026-03-25T03:00:00Z", 66.0),
            record("2026-03-25T05:00:00Z", 72.0),
        )

        val result = engine.analyze(records, settings, date)

        assertThat(result.patterns.map { it.type }).contains(PatternType.NIGHT_LOW)
    }

    private fun record(timestamp: String, value: Double) = CgmRecord(
        id = timestamp,
        timestamp = Instant.parse(timestamp),
        glucoseValue = value,
        unit = GlucoseUnit.MG_DL,
        trendDirection = TrendDirection.FLAT,
    )
}
