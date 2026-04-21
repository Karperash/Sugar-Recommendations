package com.gk.diaguide.presentation.dashboard

import app.cash.turbine.test
import com.gk.diaguide.domain.engine.CgmAnalysisEngine
import com.gk.diaguide.domain.engine.RecommendationEngine
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.EntrySource
import com.gk.diaguide.domain.model.GlucoseUnit
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.model.TrendDirection
import com.gk.diaguide.domain.model.UserSettings
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun exposesLatestEntryAndSummary() = runTest {
        val settingsRepo = FakeSettingsRepository()
        val cgmRepo = FakeCgmRepository()
        val refresh = RefreshInsightsUseCase(cgmRepo, settingsRepo, CgmAnalysisEngine(), RecommendationEngine())

        val now = Instant.now()
        cgmRepo.insertEntry(
            CgmRecord(
                id = "1",
                timestamp = now,
                glucoseValue = 120.0,
                unit = GlucoseUnit.MG_DL,
                trendDirection = TrendDirection.FLAT,
                source = EntrySource.MANUAL,
            ),
        )

        val viewModel = DashboardViewModel(cgmRepo, settingsRepo, refresh)

        viewModel.state.test {
            awaitItem() // initial state
            val loaded = awaitItem()
            assertThat(loaded.latestEntry?.glucoseValue).isEqualTo(120.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        private val flow = MutableStateFlow(
            UserSettings(
                onboardingCompleted = true,
                disclaimerAccepted = true,
                settingsIntroCompleted = true,
            ),
        )
        override fun observeSettings(): Flow<UserSettings> = flow
        override suspend fun saveProfile(displayName: String, diabetesType: String, ageGroup: String) = Unit
        override suspend fun saveThresholds(settings: UserSettings) {
            flow.value = settings
        }
        override suspend fun completeOnboarding(completed: Boolean, disclaimerAccepted: Boolean) = Unit
    }

    private class FakeCgmRepository : CgmRepository {
        private val entries = MutableStateFlow<List<CgmRecord>>(emptyList())
        private val recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
        private val events = MutableStateFlow<List<AppEvent>>(emptyList())

        override fun observeLatestEntry(): Flow<CgmRecord?> = MutableStateFlow(entries.value.maxByOrNull { it.timestamp.toEpochMilli() })
        override fun observeAllEntries(): Flow<List<CgmRecord>> = entries
        override fun observeEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<CgmRecord>> = MutableStateFlow(
            entries.value.filter { it.timestamp.toEpochMilli() in startMillis..endMillis }.sortedBy { it.timestamp }
        )
        override fun observeRecommendations(): Flow<List<Recommendation>> = recommendations
        override fun observeEvents(): Flow<List<AppEvent>> = events
        override suspend fun insertEntries(entries: List<CgmRecord>) {
            this.entries.value = this.entries.value + entries
        }
        override suspend fun insertEntry(entry: CgmRecord) {
            this.entries.value = this.entries.value + entry
        }
        override suspend fun replaceRecommendations(recommendations: List<Recommendation>) {
            this.recommendations.value = recommendations
        }
        override suspend fun insertEvent(event: AppEvent) {
            this.events.value = this.events.value + event
        }
        override suspend fun clearAll() = Unit
    }
}
