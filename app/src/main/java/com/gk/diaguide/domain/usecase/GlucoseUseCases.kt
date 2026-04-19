package com.gk.diaguide.domain.usecase

import com.gk.diaguide.data.imports.CgmCsvParser
import com.gk.diaguide.data.imports.CgmJsonParser
import com.gk.diaguide.data.mock.ScenarioType
import com.gk.diaguide.data.mock.SyntheticCgmDataFactory
import com.gk.diaguide.domain.engine.AnalysisResult
import com.gk.diaguide.domain.engine.CgmAnalysisEngine
import com.gk.diaguide.domain.engine.RecommendationEngine
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.DailySummary
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.EventType
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        displayName: String,
        diabetesType: String,
        ageGroup: String,
    ) {
        settingsRepository.saveProfile(displayName, diabetesType, ageGroup)
    }
}

class SaveThresholdsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(settings: UserSettings) {
        settingsRepository.saveThresholds(settings)
    }
}

class ImportCgmCsvUseCase @Inject constructor(
    private val parser: CgmCsvParser,
    private val repository: CgmRepository,
) {
    suspend operator fun invoke(stream: InputStream, defaultUnit: GlucoseUnit): Int {
        val entries = parser.parse(stream, defaultUnit).map { it.copy(source = EntrySource.CSV_IMPORT) }
        repository.insertEntries(entries)
        return entries.size
    }
}

class ImportCgmJsonUseCase @Inject constructor(
    private val parser: CgmJsonParser,
    private val repository: CgmRepository,
) {
    suspend operator fun invoke(stream: InputStream, defaultUnit: GlucoseUnit): Int {
        val entries = parser.parse(stream, defaultUnit).map { it.copy(source = EntrySource.JSON_IMPORT) }
        repository.insertEntries(entries)
        return entries.size
    }
}

class AddManualGlucoseEntryUseCase @Inject constructor(
    private val repository: CgmRepository,
) {
    suspend operator fun invoke(entry: CgmRecord) {
        repository.insertEntry(entry)
    }
}

class GetLatestGlucoseUseCase @Inject constructor(
    private val repository: CgmRepository,
) {
    operator fun invoke(): Flow<CgmRecord?> = repository.observeLatestEntry()
}

class GetDailySummaryUseCase @Inject constructor(
    private val repository: CgmRepository,
    private val settingsRepository: SettingsRepository,
    private val analysisEngine: CgmAnalysisEngine,
) {
    operator fun invoke(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Flow<DailySummary> {
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return combine(
            repository.observeEntriesBetween(start, end),
            settingsRepository.observeSettings(),
        ) { entries, settings ->
            analysisEngine.analyze(entries, settings, date, zoneId).summary
        }
    }
}

class AnalyzeGlucoseTrendsUseCase @Inject constructor(
    private val analysisEngine: CgmAnalysisEngine,
) {
    operator fun invoke(
        records: List<CgmRecord>,
        settings: UserSettings,
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): AnalysisResult = analysisEngine.analyze(records, settings, date, zoneId)
}

class GenerateRecommendationsUseCase @Inject constructor(
    private val engine: RecommendationEngine,
) {
    operator fun invoke(
        analysisResult: AnalysisResult,
        settings: UserSettings,
    ): List<Recommendation> = engine.generate(analysisResult.patterns, settings)
}

class GetRecommendationHistoryUseCase @Inject constructor(
    private val repository: CgmRepository,
) {
    operator fun invoke(): Flow<List<Recommendation>> = repository.observeRecommendations()
}

class SeedSyntheticDataUseCase @Inject constructor(
    private val repository: CgmRepository,
    private val factory: SyntheticCgmDataFactory,
) {
    suspend operator fun invoke(
        scenarioType: ScenarioType,
        unit: GlucoseUnit,
    ) {
        repository.insertEntries(factory.createScenario(scenarioType, unit))
        repository.insertEvent(
            AppEvent(
                id = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now(),
                type = EventType.IMPORT,
                title = "Synthetic data loaded",
                description = "Scenario loaded: $scenarioType",
            ),
        )
    }
}

class RefreshInsightsUseCase @Inject constructor(
    private val repository: CgmRepository,
    private val settingsRepository: SettingsRepository,
    private val analysisEngine: CgmAnalysisEngine,
    private val recommendationEngine: RecommendationEngine,
) {
    suspend operator fun invoke(
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ) {
        val settings = settingsRepository.observeSettings().firstValue()
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        val entries = repository.observeEntriesBetween(start, end).firstValue()
        val analysis = analysisEngine.analyze(entries, settings, date, zoneId)
        val recommendations = recommendationEngine.generate(analysis.patterns, settings)
        repository.replaceRecommendations(recommendations)
    }
}

private suspend fun <T> Flow<T>.firstValue(): T = first()
