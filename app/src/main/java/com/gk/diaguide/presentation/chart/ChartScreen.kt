package com.gk.diaguide.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.CgmLineChart
import com.gk.diaguide.core.util.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    state: ChartUiState,
    onRangeSelected: (ChartRange) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.chart_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChartRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        label = { Text(range.label) },
                    )
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.chart_glucose_over_time), style = MaterialTheme.typography.titleMedium)
                    if (state.records.isEmpty()) {
                        Text(stringResource(R.string.chart_no_data))
                    } else {
                        CgmLineChart(
                            records = state.records,
                            settings = state.settings,
                            modifier = Modifier.height(260.dp),
                        )
                        Text(
                            stringResource(
                                R.string.chart_latest_point,
                                state.records.last().timestamp.formatDateTime(),
                            ),
                        )
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.chart_event_markers_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.chart_event_markers_blue))
                    Text(stringResource(R.string.chart_event_markers_amber))
                }
            }
        }
    }
}
