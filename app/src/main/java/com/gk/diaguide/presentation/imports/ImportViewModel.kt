package com.gk.diaguide.presentation.imports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.data.mock.ScenarioType
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.EventType
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.ImportCgmCsvUseCase
import com.gk.diaguide.domain.usecase.ImportCgmJsonUseCase
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import com.gk.diaguide.domain.usecase.SeedSyntheticDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ImportUiState(
    val message: String? = null,
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importCgmCsvUseCase: ImportCgmCsvUseCase,
    private val importCgmJsonUseCase: ImportCgmJsonUseCase,
    private val seedSyntheticDataUseCase: SeedSyntheticDataUseCase,
    private val refreshInsightsUseCase: RefreshInsightsUseCase,
    private val settingsRepository: SettingsRepository,
    private val cgmRepository: CgmRepository,
) : ViewModel() {

    var uiState by mutableStateOf(ImportUiState())
        private set

    fun importCsv(stream: InputStream) {
        viewModelScope.launch {
            val unit = settingsRepository.observeSettings().first().glucoseUnit
            val count = importCgmCsvUseCase(stream, unit)
            cgmRepository.insertEvent(
                AppEvent(
                    id = UUID.randomUUID().toString(),
                    timestamp = Instant.now(),
                    type = EventType.IMPORT,
                    title = "CSV imported",
                    description = "Imported $count CGM records from CSV.",
                ),
            )
            refreshInsightsUseCase()
            uiState = ImportUiState(message = "CSV import completed: $count records.")
        }
    }

    fun importJson(stream: InputStream) {
        viewModelScope.launch {
            val unit = settingsRepository.observeSettings().first().glucoseUnit
            val count = importCgmJsonUseCase(stream, unit)
            cgmRepository.insertEvent(
                AppEvent(
                    id = UUID.randomUUID().toString(),
                    timestamp = Instant.now(),
                    type = EventType.IMPORT,
                    title = "JSON imported",
                    description = "Imported $count CGM records from JSON.",
                ),
            )
            refreshInsightsUseCase()
            uiState = ImportUiState(message = "JSON import completed: $count records.")
        }
    }

    fun loadScenario(scenarioType: ScenarioType) {
        viewModelScope.launch {
            val unit = settingsRepository.observeSettings().first().glucoseUnit
            seedSyntheticDataUseCase(scenarioType, unit)
            refreshInsightsUseCase()
            uiState = ImportUiState(message = "Synthetic scenario loaded: $scenarioType")
        }
    }
}
