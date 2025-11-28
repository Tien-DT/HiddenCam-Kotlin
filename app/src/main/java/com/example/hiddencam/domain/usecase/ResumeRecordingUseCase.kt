package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.repository.VideoRecordingRepository
import javax.inject.Inject

/**
 * Use case for resuming paused video recording
 */
class ResumeRecordingUseCase @Inject constructor(
    private val videoRecordingRepository: VideoRecordingRepository
) {
    suspend operator fun invoke() {
        videoRecordingRepository.resumeRecording()
    }
}
