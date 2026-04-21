package com.gk.diaguide.presentation.imports

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.R
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ImportUiState(
    val message: String? = null,
    val previewSnippet: String? = null,
    val isError: Boolean = false,
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
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
            val bytes = runCatching { stream.readBytes() }.getOrElse {
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_error_read),
                    isError = true,
                )
                return@launch
            }
            if (bytes.isEmpty()) {
                uiState = ImportUiState(message = appContext.getString(R.string.import_error_empty_file), isError = true)
                return@launch
            }
            val preview = buildPreviewSnippet(bytes)
            val count = runCatching {
                importCgmCsvUseCase(ByteArrayInputStream(bytes), unit)
            }.getOrElse { e ->
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_error_parse, e.message ?: ""),
                    previewSnippet = preview,
                    isError = true,
                )
                return@launch
            }
            if (count == 0) {
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_zero_rows),
                    previewSnippet = preview,
                    isError = true,
                )
                return@launch
            }
            cgmRepository.insertEvent(
                AppEvent(
                    id = UUID.randomUUID().toString(),
                    timestamp = Instant.now(),
                    type = EventType.IMPORT,
                    title = appContext.getString(R.string.import_event_csv_title),
                    description = appContext.getString(R.string.import_event_csv_desc, count),
                ),
            )
            refreshInsightsUseCase()
            uiState = ImportUiState(
                message = appContext.getString(R.string.import_completed_csv, count),
                previewSnippet = preview,
            )
        }
    }

    fun importJson(stream: InputStream) {
        viewModelScope.launch {
            val unit = settingsRepository.observeSettings().first().glucoseUnit
            val bytes = runCatching { stream.readBytes() }.getOrElse {
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_error_read),
                    isError = true,
                )
                return@launch
            }
            if (bytes.isEmpty()) {
                uiState = ImportUiState(message = appContext.getString(R.string.import_error_empty_file), isError = true)
                return@launch
            }
            val preview = buildPreviewSnippet(bytes)
            val count = runCatching {
                importCgmJsonUseCase(ByteArrayInputStream(bytes), unit)
            }.getOrElse { e ->
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_error_parse, e.message ?: ""),
                    previewSnippet = preview,
                    isError = true,
                )
                return@launch
            }
            if (count == 0) {
                uiState = ImportUiState(
                    message = appContext.getString(R.string.import_zero_rows),
                    previewSnippet = preview,
                    isError = true,
                )
                return@launch
            }
            cgmRepository.insertEvent(
                AppEvent(
                    id = UUID.randomUUID().toString(),
                    timestamp = Instant.now(),
                    type = EventType.IMPORT,
                    title = appContext.getString(R.string.import_event_json_title),
                    description = appContext.getString(R.string.import_event_json_desc, count),
                ),
            )
            refreshInsightsUseCase()
            uiState = ImportUiState(
                message = appContext.getString(R.string.import_completed_json, count),
                previewSnippet = preview,
            )
        }
    }

    fun loadScenario(scenarioType: ScenarioType) {
        viewModelScope.launch {
            val unit = settingsRepository.observeSettings().first().glucoseUnit
            seedSyntheticDataUseCase(scenarioType, unit)
            refreshInsightsUseCase()
            uiState = ImportUiState(
                message = appContext.getString(
                    R.string.import_completed_scenario,
                    appContext.getString(scenarioTypeTitleRes(scenarioType)),
                ),
                previewSnippet = null,
            )
        }
    }

    private fun buildPreviewSnippet(bytes: ByteArray, maxChars: Int = 280): String {
        val len = minOf(bytes.size, 4096)
        val raw = String(bytes, 0, len, StandardCharsets.UTF_8)
        return raw.lineSequence().take(4).joinToString("\n").take(maxChars)
    }
}
