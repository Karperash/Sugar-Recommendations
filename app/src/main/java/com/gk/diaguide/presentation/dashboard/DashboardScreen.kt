package com.gk.diaguide.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.BuildConfig
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.CgmLineChart
import com.gk.diaguide.core.ui.SeverityChip
import com.gk.diaguide.core.ui.StatusBadge
import com.gk.diaguide.core.util.asArrow
import com.gk.diaguide.core.util.formatGlucose
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.navigation.AppDestination
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigate: (AppDestination) -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    Text(
                        text = stringResource(R.string.dashboard_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                    )
                    Button(onClick = onRefresh) { Text(stringResource(R.string.dashboard_refresh)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.padding(0.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dashboard_current_glucose), style = MaterialTheme.typography.titleMedium)
                    val latest = state.latestEntry
                    if (latest != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                latest.glucoseValue.formatGlucose(state.settings.glucoseUnit),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(latest.trendDirection.asArrow(), style = MaterialTheme.typography.headlineMedium)
                            StatusBadge(
                                text = currentStatusText(latest.glucoseValue, state.settings),
                                color = currentStatusColor(latest.glucoseValue, state.settings),
                            )
                        }
                        CgmLineChart(
                            records = state.todayEntries.ifEmpty { listOf(latest) }.takeLast(12),
                            settings = state.settings,
                            modifier = Modifier.height(120.dp),
                        )
                    } else {
                        Text(stringResource(R.string.dashboard_no_readings))
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dashboard_today_summary), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.dashboard_min, state.todaySummary.minimum.toString()))
                    Text(stringResource(R.string.dashboard_max, state.todaySummary.maximum.toString()))
                    Text(stringResource(R.string.dashboard_average, "%.1f".format(state.todaySummary.average)))
                    Text(stringResource(R.string.dashboard_time_in_target, "%.1f".format(state.todaySummary.inRangePercent)))
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dashboard_active_recommendations), style = MaterialTheme.typography.titleMedium)
                    if (state.recommendations.isEmpty()) {
                        Text(stringResource(R.string.dashboard_no_recommendations))
                    } else {
                        state.recommendations.take(3).forEach { recommendation ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(recommendation.title, style = MaterialTheme.typography.titleSmall)
                                Text(recommendation.shortExplanation)
                                SeverityChip(recommendation.severity)
                            }
                        }
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onNavigate(AppDestination.ManualEntry) }) { Text(stringResource(R.string.dashboard_add_entry)) }
                Button(onClick = { onNavigate(AppDestination.Import) }) { Text(stringResource(R.string.dashboard_import_file)) }
                Button(onClick = { onNavigate(AppDestination.Chart) }) { Text(stringResource(R.string.dashboard_open_chart)) }
                Button(onClick = { onNavigate(AppDestination.Recommendations) }) { Text(stringResource(R.string.dashboard_recommendations)) }
                Button(onClick = { onNavigate(AppDestination.EventLog) }) { Text(stringResource(R.string.dashboard_event_log)) }
            }

            Text(
                stringResource(R.string.dashboard_disclaimer),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun currentStatusText(value: Double, settings: UserSettings): String = when {
    value <= settings.criticalLow || value >= settings.criticalHigh -> stringResource(R.string.status_critical)
    value < settings.targetLow || value > settings.targetHigh -> stringResource(R.string.status_out_of_range)
    else -> stringResource(R.string.status_in_target)
}

private fun currentStatusColor(value: Double, settings: UserSettings) = when {
    value <= settings.criticalLow || value >= settings.criticalHigh -> Critical
    value < settings.targetLow || value > settings.targetHigh -> Warning
    else -> Success
}
