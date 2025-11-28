package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting current settings
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<VideoSettings> {
        return settingsRepository.getSettingsFlow()
    }
    
    suspend fun getOnce(): VideoSettings {
        return settingsRepository.getSettings()
    }
}
