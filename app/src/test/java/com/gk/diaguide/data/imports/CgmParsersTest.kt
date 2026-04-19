package com.gk.diaguide.data.imports

import com.gk.diaguide.domain.model.GlucoseUnit
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import org.junit.Test

class CgmParsersTest {

    private val csvParser = CgmCsvParser()
    private val jsonParser = CgmJsonParser()

    @Test
    fun parsesCsvRecords() {
        val csv = """
timestamp,glucoseValue,unit,trendDirection,meal
2026-03-25T08:00:00Z,112,MG_DL,FLAT,true
2026-03-25T08:15:00Z,126,MG_DL,UP,false
""".trimIndent()

        val result = csvParser.parse(ByteArrayInputStream(csv.toByteArray()), GlucoseUnit.MG_DL)

        assertThat(result).hasSize(2)
        assertThat(result.first().meal).isTrue()
        assertThat(result.last().glucoseValue).isEqualTo(126.0)
    }

    @Test
    fun parsesJsonRecords() {
        val json = """
[
  {
    "timestamp": "2026-03-25T08:00:00Z",
    "glucoseValue": 112,
    "unit": "MG_DL",
    "trendDirection": "FLAT",
    "activity": true
  }
]
""".trimIndent()

        val result = jsonParser.parse(ByteArrayInputStream(json.toByteArray()), GlucoseUnit.MG_DL)

        assertThat(result).hasSize(1)
        assertThat(result.first().activity).isTrue()
    }
}
