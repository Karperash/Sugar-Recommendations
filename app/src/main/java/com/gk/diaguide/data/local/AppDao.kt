package com.gk.diaguide.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CgmDao {
    @Query("SELECT * FROM cgm_records ORDER BY timestampMillis DESC LIMIT 1")
    fun observeLatest(): Flow<CgmRecordEntity?>

    @Query("SELECT * FROM cgm_records ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<CgmRecordEntity>>

    @Query("SELECT * FROM cgm_records WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis ASC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<CgmRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<CgmRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CgmRecordEntity)

    @Query("DELETE FROM cgm_records")
    suspend fun clearAll()
}

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<RecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<RecommendationEntity>)

    @Query("DELETE FROM recommendations")
    suspend fun clearAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM app_events ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<AppEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AppEventEntity)
}
