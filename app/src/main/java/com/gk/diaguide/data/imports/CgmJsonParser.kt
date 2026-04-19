package com.gk.diaguide.data.imports

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.TrendDirection
import java.io.InputStream
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

class CgmJsonParser @Inject constructor() {

    fun parse(stream: InputStream, defaultUnit: GlucoseUnit): List<CgmRecord> {
        val text = stream.bufferedReader().use { it.readText() }
        if (text.isBlank()) return emptyList()

        val array = if (text.trim().startsWith("[")) {
            JSONArray(text)
        } else {
            JSONObject(text).optJSONArray("records") ?: JSONArray()
        }

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val timestamp = item.optString("timestamp")
                val value = item.optDouble("glucoseValue", Double.NaN).takeIf { !it.isNaN() }
                    ?: item.optDouble("glucose", Double.NaN).takeIf { !it.isNaN() }
                    ?: continue

                add(
                    CgmRecord(
                        id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                        timestamp = Instant.parse(timestamp),
                        glucoseValue = value,
                        unit = item.optString("unit").takeIf { it.isNotBlank() }?.runCatching {
                            GlucoseUnit.valueOf(this)
                        }?.getOrNull() ?: defaultUnit,
                        trendDirection = item.optString("trendDirection").takeIf { it.isNotBlank() }?.runCatching {
                            TrendDirection.valueOf(this)
                        }?.getOrNull() ?: TrendDirection.UNKNOWN,
                        source = EntrySource.JSON_IMPORT,
                        note = item.optString("note").takeIf { it.isNotBlank() },
                        meal = item.optBoolean("meal", false),
                        insulin = item.optBoolean("insulin", false),
                        activity = item.optBoolean("activity", false),
                        sleep = item.optBoolean("sleep", false),
                        stress = item.optBoolean("stress", false),
                        symptom = item.optBoolean("symptom", false),
                    ),
                )
            }
        }
    }
}
