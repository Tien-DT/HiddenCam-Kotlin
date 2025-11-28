package com.example.hiddencam.di

import com.example.hiddencam.data.repository.SettingsRepositoryImpl
import com.example.hiddencam.data.repository.VideoRecordingRepositoryImpl
import com.example.hiddencam.domain.repository.SettingsRepository
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
    
    @Binds
    @Singleton
    abstract fun bindVideoRecordingRepository(
        videoRecordingRepositoryImpl: VideoRecordingRepositoryImpl
    ): VideoRecordingRepository
}
