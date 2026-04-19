package com.gk.diaguide.domain.repository

import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.Recommendation
import kotlinx.coroutines.flow.Flow

interface CgmRepository {
    fun observeLatestEntry(): Flow<CgmRecord?>
    fun observeAllEntries(): Flow<List<CgmRecord>>
    fun observeEntriesBetween(startMillis: Long, endMillis: Long): Flow<List<CgmRecord>>
    fun observeRecommendations(): Flow<List<Recommendation>>
    fun observeEvents(): Flow<List<AppEvent>>

    suspend fun insertEntries(entries: List<CgmRecord>)
    suspend fun insertEntry(entry: CgmRecord)
    suspend fun replaceRecommendations(recommendations: List<Recommendation>)
    suspend fun insertEvent(event: AppEvent)
    suspend fun clearAll()
}
