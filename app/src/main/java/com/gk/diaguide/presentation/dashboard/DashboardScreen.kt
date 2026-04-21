package com.gk.diaguide.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Timeline
import com.gk.diaguide.BuildConfig
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.CgmLineChart
import com.gk.diaguide.core.ui.Dimens
import com.gk.diaguide.core.ui.EmptyState
import com.gk.diaguide.core.ui.SeverityChip
import com.gk.diaguide.core.ui.StatusBadge
import com.gk.diaguide.core.util.asArrow
import com.gk.diaguide.core.util.formatGlucose
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.presentation.recommendations.displayExplanation
import com.gk.diaguide.presentation.recommendations.displayTitle
import com.gk.diaguide.navigation.AppDestination
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning
import kotlinx.coroutines.launch

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
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        // Временные стрелки для прокрутки в эмуляторе; при необходимости удалить вместе с IconButton ниже
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.screenPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
            ) {
            ElevatedCard {
                Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)) {
                    Text(stringResource(R.string.dashboard_current_glucose), style = MaterialTheme.typography.titleMedium)
                    val latest = state.latestEntry
                    if (latest != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)) {
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
                        EmptyState(
                            title = stringResource(R.string.empty_state_readings_title),
                            message = stringResource(R.string.dashboard_no_readings),
                            icon = Icons.Outlined.Timeline,
                            contentDescription = null,
                        )
                    }
                }
            }

            ElevatedCard {
                Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)) {
                    Text(stringResource(R.string.dashboard_today_summary), style = MaterialTheme.typography.titleMedium)
                    if (state.todaySummary.totalReadings == 0) {
                        EmptyState(
                            title = stringResource(R.string.empty_state_summary_title),
                            message = stringResource(R.string.empty_state_summary_message),
                            icon = Icons.Outlined.Assessment,
                            contentDescription = null,
                        )
                    } else {
                        Text(stringResource(R.string.dashboard_min, state.todaySummary.minimum.toString()))
                        Text(stringResource(R.string.dashboard_max, state.todaySummary.maximum.toString()))
                        Text(stringResource(R.string.dashboard_average, "%.1f".format(state.todaySummary.average)))
                        Text(stringResource(R.string.dashboard_time_in_target, "%.1f".format(state.todaySummary.inRangePercent)))
                    }
                }
            }

            ElevatedCard {
                Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing)) {
                    Text(stringResource(R.string.dashboard_active_recommendations), style = MaterialTheme.typography.titleMedium)
                    if (state.recommendations.isEmpty()) {
                        EmptyState(
                            title = stringResource(R.string.empty_state_recommendations_title),
                            message = stringResource(R.string.dashboard_no_recommendations),
                            icon = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                        )
                    } else {
                        state.recommendations.take(3).forEach { recommendation ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(recommendation.displayTitle(), style = MaterialTheme.typography.titleSmall)
                                Text(recommendation.displayExplanation())
                                SeverityChip(recommendation.severity)
                            }
                        }
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.chipSpacing), verticalArrangement = Arrangement.spacedBy(Dimens.chipSpacing)) {
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

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            scrollState.scrollTo(
                                (scrollState.value - 280).coerceAtLeast(0),
                            )
                        }
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Text("▲", style = MaterialTheme.typography.labelMedium)
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            scrollState.scrollTo(
                                (scrollState.value + 280).coerceAtMost(scrollState.maxValue),
                            )
                        }
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Text("▼", style = MaterialTheme.typography.labelMedium)
                }
            }
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
