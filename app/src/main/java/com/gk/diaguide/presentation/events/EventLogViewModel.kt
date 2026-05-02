package com.gk.diaguide.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val UserOwnedEntrySources: Set<EntrySource> = setOf(
    EntrySource.MANUAL,
    EntrySource.CSV_IMPORT,
    EntrySource.JSON_IMPORT,
    EntrySource.MOCK,
)

data class EventLogUiState(
    val records: List<CgmRecord> = emptyList(),
    val settings: UserSettings = UserSettings(),
)

@HiltViewModel
class EventLogViewModel @Inject constructor(
    private val repository: CgmRepository,
    settingsRepository: SettingsRepository,
    private val refreshInsightsUseCase: RefreshInsightsUseCase,
) : ViewModel() {

    val uiState = combine(
        repository.observeAllEntries(),
        settingsRepository.observeSettings(),
    ) { entries, settings ->
        EventLogUiState(
            records = entries
                .filter { it.source in UserOwnedEntrySources }
                .sortedByDescending { it.timestamp },
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventLogUiState())

    fun updateEntry(record: CgmRecord) {
        viewModelScope.launch {
            repository.updateEntry(record)
            refreshInsightsUseCase()
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
            refreshInsightsUseCase()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            refreshInsightsUseCase()
        }
    }
}
