package com.gk.diaguide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CgmRecordEntity::class,
        RecommendationEntity::class,
        AppEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cgmDao(): CgmDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun eventDao(): EventDao
}
