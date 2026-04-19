package com.gk.diaguide.data.imports

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.TrendDirection
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CgmCsvParser @Inject constructor() {

    fun parse(stream: InputStream, defaultUnit: GlucoseUnit): List<CgmRecord> {
        val lines = BufferedReader(InputStreamReader(stream)).readLines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val header = lines.first().split(",").map { it.trim() }
        val rows = lines.drop(1)

        return rows.mapNotNull { line ->
            val cells = line.split(",").map { it.trim() }
            val values = header.zip(cells).toMap()
            val timestampText = values["timestamp"] ?: return@mapNotNull null
            val glucoseText = values["glucoseValue"] ?: values["glucose"] ?: return@mapNotNull null

            CgmRecord(
                id = values["id"].orEmpty().ifBlank { UUID.randomUUID().toString() },
                timestamp = Instant.parse(timestampText),
                glucoseValue = glucoseText.toDoubleOrNull() ?: return@mapNotNull null,
                unit = values["unit"]?.runCatching { GlucoseUnit.valueOf(this) }?.getOrNull() ?: defaultUnit,
                trendDirection = values["trendDirection"]?.runCatching { TrendDirection.valueOf(this) }?.getOrNull()
                    ?: TrendDirection.UNKNOWN,
                source = EntrySource.CSV_IMPORT,
                note = values["note"],
                meal = values["meal"].toBooleanFlexible(),
                insulin = values["insulin"].toBooleanFlexible(),
                activity = values["activity"].toBooleanFlexible(),
                sleep = values["sleep"].toBooleanFlexible(),
                stress = values["stress"].toBooleanFlexible(),
                symptom = values["symptom"].toBooleanFlexible(),
            )
        }
    }

    private fun String?.toBooleanFlexible(): Boolean {
        return when (this?.lowercase()) {
            "true", "1", "yes", "y" -> true
            else -> false
        }
    }
}
