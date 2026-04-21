package com.gk.diaguide.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gk.diaguide.R
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.EventType
import com.gk.diaguide.domain.model.TrendDirection

@Composable
fun entrySourceLabel(source: EntrySource): String = stringResource(
    when (source) {
        EntrySource.MANUAL -> R.string.entry_source_manual
        EntrySource.CSV_IMPORT -> R.string.entry_source_csv_import
        EntrySource.JSON_IMPORT -> R.string.entry_source_json_import
        EntrySource.MOCK -> R.string.entry_source_mock
        EntrySource.API_PLACEHOLDER -> R.string.entry_source_api_placeholder
        EntrySource.BLUETOOTH_PLACEHOLDER -> R.string.entry_source_bluetooth_placeholder
    },
)

@Composable
fun eventTypeLabel(type: EventType): String = stringResource(
    when (type) {
        EventType.NOTE -> R.string.event_type_note
        EventType.MEAL -> R.string.event_type_meal
        EventType.INSULIN -> R.string.event_type_insulin
        EventType.ACTIVITY -> R.string.event_type_activity
        EventType.SLEEP -> R.string.event_type_sleep
        EventType.STRESS -> R.string.event_type_stress
        EventType.SYMPTOM -> R.string.event_type_symptom
        EventType.IMPORT -> R.string.event_type_import
    },
)

@Composable
fun trendDirectionLabel(direction: TrendDirection): String = stringResource(
    when (direction) {
        TrendDirection.DOUBLE_UP -> R.string.trend_double_up
        TrendDirection.UP -> R.string.trend_up
        TrendDirection.FLAT -> R.string.trend_flat
        TrendDirection.DOWN -> R.string.trend_down
        TrendDirection.DOUBLE_DOWN -> R.string.trend_double_down
        TrendDirection.UNKNOWN -> R.string.trend_unknown
    },
)
