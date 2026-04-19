package com.gk.diaguide.presentation.manual

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.EventType
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.TrendDirection
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.AddManualGlucoseEntryUseCase
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class ManualEntryUiState(
    val glucoseValue: String = "",
    val note: String = "",
    val unit: GlucoseUnit = GlucoseUnit.MG_DL,
    val trendDirection: TrendDirection = TrendDirection.FLAT,
    val meal: Boolean = false,
    val insulin: Boolean = false,
    val activity: Boolean = false,
    val sleep: Boolean = false,
    val stress: Boolean = false,
    val symptom: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val addManualGlucoseEntryUseCase: AddManualGlucoseEntryUseCase,
    private val cgmRepository: CgmRepository,
    private val settingsRepository: SettingsRepository,
    private val refreshInsightsUseCase: RefreshInsightsUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(ManualEntryUiState())
        private set

    private val events = Channel<Boolean>(Channel.BUFFERED)
    val saved = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.observeSettings().first()
            uiState = uiState.copy(unit = settings.glucoseUnit)
        }
    }

    fun update(transform: (ManualEntryUiState) -> ManualEntryUiState) {
        uiState = transform(uiState)
    }

    fun save() {
        val glucoseValue = uiState.glucoseValue.toDoubleOrNull()
        if (glucoseValue == null) {
            uiState = uiState.copy(error = "Enter a valid glucose value.")
            return
        }

        viewModelScope.launch {
            val recordId = UUID.randomUUID().toString()
            addManualGlucoseEntryUseCase(
                CgmRecord(
                    id = recordId,
                    timestamp = Instant.now(),
                    glucoseValue = glucoseValue,
                    unit = uiState.unit,
                    trendDirection = uiState.trendDirection,
                    source = EntrySource.MANUAL,
                    note = uiState.note.takeIf { it.isNotBlank() },
                    meal = uiState.meal,
                    insulin = uiState.insulin,
                    activity = uiState.activity,
                    sleep = uiState.sleep,
                    stress = uiState.stress,
                    symptom = uiState.symptom,
                ),
            )

            if (uiState.note.isNotBlank() || uiState.meal || uiState.insulin || uiState.activity || uiState.sleep || uiState.stress || uiState.symptom) {
                cgmRepository.insertEvent(
                    AppEvent(
                        id = UUID.randomUUID().toString(),
                        timestamp = Instant.now(),
                        type = EventType.NOTE,
                        title = "Manual entry saved",
                        description = uiState.note.ifBlank { "Tagged manual glucose entry." },
                        relatedRecordId = recordId,
                    ),
                )
            }

            refreshInsightsUseCase()
            events.send(true)
        }
    }
}
