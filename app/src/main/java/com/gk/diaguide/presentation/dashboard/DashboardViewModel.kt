package com.gk.diaguide.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.DailySummary
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val settings: UserSettings = UserSettings(),
    val latestEntry: CgmRecord? = null,
    val todaySummary: DailySummary = DailySummary(LocalDate.now(), 0.0, 0.0, 0.0, 0.0, 0, 0, 0),
    val todayEntries: List<CgmRecord> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    cgmRepository: CgmRepository,
    settingsRepository: SettingsRepository,
    private val refreshInsightsUseCase: RefreshInsightsUseCase,
) : ViewModel() {

    private val today = LocalDate.now()
    private val zoneId = ZoneId.systemDefault()
    private val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    private val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

    val state: StateFlow<DashboardUiState> = combine(
        settingsRepository.observeSettings(),
        cgmRepository.observeLatestEntry(),
        cgmRepository.observeEntriesBetween(start, end),
        cgmRepository.observeRecommendations(),
    ) { settings, latest, entries, recommendations ->
        DashboardUiState(
            settings = settings,
            latestEntry = latest,
            todaySummary = buildSummary(entries, settings),
            todayEntries = entries,
            recommendations = recommendations,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshInsightsUseCase(today)
        }
    }

    private fun buildSummary(entries: List<CgmRecord>, settings: UserSettings): DailySummary {
        if (entries.isEmpty()) {
            return DailySummary(today, 0.0, 0.0, 0.0, 0.0, 0, 0, 0)
        }
        val inRange = entries.count { it.glucoseValue in settings.targetLow..settings.targetHigh }
        return DailySummary(
            date = today,
            minimum = entries.minOf { it.glucoseValue },
            maximum = entries.maxOf { it.glucoseValue },
            average = entries.map { it.glucoseValue }.average(),
            inRangePercent = inRange.toDouble() / entries.size.toDouble() * 100.0,
            totalReadings = entries.size,
            lowReadings = entries.count { it.glucoseValue < settings.targetLow },
            highReadings = entries.count { it.glucoseValue > settings.targetHigh },
        )
    }
}
