package com.gk.diaguide.di

import android.content.Context
import androidx.room.Room
import com.gk.diaguide.data.imports.CgmCsvParser
import com.gk.diaguide.data.imports.CgmJsonParser
import com.gk.diaguide.data.local.AppDatabase
import com.gk.diaguide.data.mock.SyntheticCgmDataFactory
import com.gk.diaguide.data.repository.CgmRepositoryImpl
import com.gk.diaguide.data.repository.SettingsRepositoryImpl
import com.gk.diaguide.domain.engine.CgmAnalysisEngine
import com.gk.diaguide.domain.engine.RecommendationEngine
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "dia_guide.db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideCsvParser(): CgmCsvParser = CgmCsvParser()

    @Provides
    fun provideJsonParser(): CgmJsonParser = CgmJsonParser()

    @Provides
    fun provideSyntheticDataFactory(): SyntheticCgmDataFactory = SyntheticCgmDataFactory()

    @Provides
    fun provideAnalysisEngine(): CgmAnalysisEngine = CgmAnalysisEngine()

    @Provides
    fun provideRecommendationEngine(): RecommendationEngine = RecommendationEngine()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    @Binds
    abstract fun bindCgmRepository(
        impl: CgmRepositoryImpl,
    ): CgmRepository

    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository
}
