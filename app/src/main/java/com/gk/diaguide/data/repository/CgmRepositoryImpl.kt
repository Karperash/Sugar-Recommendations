package com.gk.diaguide.data.repository

import com.gk.diaguide.data.local.AppDatabase
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.repository.CgmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CgmRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : CgmRepository {

    override fun observeLatestEntry(): Flow<CgmRecord?> =
        database.cgmDao().observeLatest().map { it?.toDomain() }

    override fun observeAllEntries(): Flow<List<CgmRecord>> =
        database.cgmDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<CgmRecord>> =
        database.cgmDao().observeBetween(startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeRecommendations(): Flow<List<Recommendation>> =
        database.recommendationDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeEvents(): Flow<List<AppEvent>> =
        database.eventDao().observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertEntries(entries: List<CgmRecord>) {
        database.cgmDao().insertAll(entries.map { it.toEntity() })
    }

    override suspend fun insertEntry(entry: CgmRecord) {
        database.cgmDao().insert(entry.toEntity())
    }

    override suspend fun replaceRecommendations(recommendations: List<Recommendation>) {
        database.recommendationDao().clearAll()
        database.recommendationDao().insertAll(recommendations.map { it.toEntity() })
    }

    override suspend fun insertEvent(event: AppEvent) {
        database.eventDao().insert(event.toEntity())
    }

    override suspend fun clearAll() {
        database.recommendationDao().clearAll()
        database.cgmDao().clearAll()
    }
}
