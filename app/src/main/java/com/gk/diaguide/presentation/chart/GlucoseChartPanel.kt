package com.gk.diaguide.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.Dimens
import com.gk.diaguide.core.ui.EmptyState
import com.gk.diaguide.core.util.formatDateTime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart

@Composable
fun GlucoseChartPanel(
    state: ChartUiState,
    onRangeSelected: (ChartRange) -> Unit,
    modifier: Modifier = Modifier,
    chartHeightDp: Int = 260,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        val primaryRanges = listOf(
            ChartRange.FIVE_DAYS,
            ChartRange.SEVEN_DAYS,
            ChartRange.FOURTEEN_DAYS,
        )
        val secondaryRanges = listOf(
            ChartRange.TWENTY_ONE_DAYS,
            ChartRange.THIRTY_DAYS,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.chipSpacing)) {
            primaryRanges.forEach { range ->
                FilterChip(
                    selected = state.selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = { Text(chartRangeLabel(range)) },
                )
            }
        }
        Row(
            modifier = Modifier.padding(start = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.chipSpacing),
        ) {
            secondaryRanges.forEach { range ->
                FilterChip(
                    selected = state.selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = { Text(chartRangeLabel(range)) },
                )
            }
        }
        Text(
            text = stringResource(R.string.chart_range_subtitle, chartRangeLabel(state.selectedRange)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card {
            Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)) {
                Text(stringResource(R.string.chart_glucose_over_time), style = MaterialTheme.typography.titleMedium)
                if (state.records.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.empty_state_chart_title),
                        message = stringResource(R.string.chart_no_data),
                        icon = Icons.Outlined.ShowChart,
                        contentDescription = null,
                    )
                } else {
                    GlucoseSeriesChart(
                        records = state.records,
                        settings = state.settings,
                        modifier = Modifier.height(chartHeightDp.dp),
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
            Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.chipSpacing)) {
                Text(stringResource(R.string.chart_event_markers_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.chart_event_markers_blue))
                Text(stringResource(R.string.chart_event_markers_amber))
            }
        }
    }
}

@Composable
private fun chartRangeLabel(range: ChartRange): String = when (range) {
    ChartRange.FIVE_DAYS -> stringResource(R.string.chart_range_5days)
    ChartRange.SEVEN_DAYS -> stringResource(R.string.chart_range_7days)
    ChartRange.FOURTEEN_DAYS -> stringResource(R.string.chart_range_14days)
    ChartRange.TWENTY_ONE_DAYS -> stringResource(R.string.chart_range_21days)
    ChartRange.THIRTY_DAYS -> stringResource(R.string.chart_range_30days)
}
