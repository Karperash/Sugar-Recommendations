package com.gk.diaguide.data.mock

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.TrendDirection
import com.gk.diaguide.domain.model.convertGlucose
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

enum class ScenarioType {
    STABLE_DAY,
    REPEATED_HIGH,
    REPEATED_LOW,
    SHARP_FLUCTUATIONS,
    NIGHT_EPISODES,
}

class SyntheticCgmDataFactory @Inject constructor() {

    /**
     * @param anchorDate последний календарный день окна (обычно «сегодня»). Точки располагаются
     * за **пять предыдущих суток**, заканчивающихся в этот день, чтобы не уходить в «будущее» и попадать в выборки БД.
     */
    fun createScenario(
        scenarioType: ScenarioType,
        unit: GlucoseUnit,
        anchorDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<CgmRecord> {
        val seriesStartDate = anchorDate.minusDays(4)
        val start = seriesStartDate.atStartOfDay(zoneId).toInstant()
        // Шаблоны заданы в мг/дл (как в клинической практике CGM); в запись попадают значения в выбранной единице.
        val oneDay = when (scenarioType) {
            ScenarioType.STABLE_DAY -> series(start, unit, listOf(102.0, 108.0, 111.0, 105.0, 118.0, 115.0, 109.0, 103.0))
            ScenarioType.REPEATED_HIGH -> series(start, unit, listOf(132.0, 168.0, 190.0, 172.0, 199.0, 184.0, 176.0, 160.0), mealIndexes = setOf(1, 4))
            ScenarioType.REPEATED_LOW -> series(start, unit, listOf(90.0, 78.0, 69.0, 72.0, 68.0, 80.0, 75.0, 70.0))
            ScenarioType.SHARP_FLUCTUATIONS -> series(start, unit, listOf(102.0, 145.0, 95.0, 170.0, 88.0, 162.0, 94.0, 130.0), mealIndexes = setOf(1, 3, 5))
            ScenarioType.NIGHT_EPISODES -> series(start, unit, listOf(82.0, 75.0, 68.0, 72.0, 95.0, 108.0, 140.0, 149.0), startHour = 0)
        }
        return repeatSeriesAcrossCalendarDays(oneDay, calendarDays = 5, zoneId)
    }

    private fun repeatSeriesAcrossCalendarDays(
        oneDayRecords: List<CgmRecord>,
        calendarDays: Int,
        zoneId: ZoneId,
    ): List<CgmRecord> {
        if (calendarDays <= 1) return oneDayRecords
        val out = ArrayList<CgmRecord>(oneDayRecords.size * calendarDays)
        for (dayOffset in 0 until calendarDays) {
            oneDayRecords.forEach { r ->
                val shifted = r.timestamp.atZone(zoneId).plusDays(dayOffset.toLong()).toInstant()
                out += r.copy(
                    id = UUID.randomUUID().toString(),
                    timestamp = shifted,
                )
            }
        }
        return out
    }

    private fun series(
        start: Instant,
        unit: GlucoseUnit,
        valuesMgDl: List<Double>,
        startHour: Int = 6,
        mealIndexes: Set<Int> = emptySet(),
    ): List<CgmRecord> {
        return valuesMgDl.mapIndexed { index, mgDl ->
            val timestamp = start.plusSeconds(((startHour + index * 2) * 60L * 60L))
            val valueInUnit = mgDl.convertGlucose(GlucoseUnit.MG_DL, unit)
            CgmRecord(
                id = UUID.randomUUID().toString(),
                timestamp = timestamp,
                glucoseValue = valueInUnit,
                unit = unit,
                trendDirection = when {
                    index == 0 -> TrendDirection.FLAT
                    mgDl > valuesMgDl[index - 1] + 15 -> TrendDirection.UP
                    mgDl < valuesMgDl[index - 1] - 15 -> TrendDirection.DOWN
                    else -> TrendDirection.FLAT
                },
                source = EntrySource.MOCK,
                note = "Synthetic scenario value ${index + 1}",
                meal = mealIndexes.contains(index),
            )
        }
    }
}
