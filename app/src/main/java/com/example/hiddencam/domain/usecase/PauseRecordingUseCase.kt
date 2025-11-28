package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * Use case for pausing video recording
 */
class PauseRecordingUseCase @Inject constructor(
    private val videoRecordingRepository: VideoRecordingRepository
) {
    suspend operator fun invoke() {
        videoRecordingRepository.pauseRecording()
    }
}
