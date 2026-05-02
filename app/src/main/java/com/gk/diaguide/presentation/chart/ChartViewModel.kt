package com.gk.diaguide.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.model.toUnit
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class ChartRange(val days: Long) {
    FIVE_DAYS(5),
    SEVEN_DAYS(7),
    FOURTEEN_DAYS(14),
    TWENTY_ONE_DAYS(21),
    THIRTY_DAYS(30),
}

data class ChartUiState(
    val settings: UserSettings = UserSettings(),
    val selectedRange: ChartRange = ChartRange.FIVE_DAYS,
    val records: List<CgmRecord> = emptyList(),
)

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val cgmRepository: CgmRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val selectedRange = MutableStateFlow(ChartRange.FIVE_DAYS)
    private val zoneId = ZoneId.systemDefault()

    val state = combine(
        settingsRepository.observeSettings(),
        selectedRange,
    ) { settings, range -> settings to range }
        .flatMapLatest { (settings, range) ->
            val today = LocalDate.now()
            val start = today.minusDays(range.days - 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            combine(
                cgmRepository.observeEntriesBetween(start, end),
                MutableStateFlow(settings),
                MutableStateFlow(range),
            ) { records, localSettings, localRange ->
                val normalizedRecords = records.map { it.toUnit(localSettings.glucoseUnit) }
                ChartUiState(
                    settings = localSettings,
                    selectedRange = localRange,
                    records = normalizedRecords,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChartUiState())

    fun setRange(range: ChartRange) {
        selectedRange.value = range
    }
}
