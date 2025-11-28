package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.repository.SettingsRepository
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * Use case for starting video recording
 */
class StartRecordingUseCase @Inject constructor(
    private val videoRecordingRepository: VideoRecordingRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val settings = settingsRepository.getSettings()
        videoRecordingRepository.startRecording(settings)
    }
}
