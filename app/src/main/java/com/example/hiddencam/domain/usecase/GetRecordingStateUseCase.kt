package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing recording state
 */
class GetRecordingStateUseCase @Inject constructor(
    private val videoRecordingRepository: VideoRecordingRepository
) {
    operator fun invoke(): Flow<RecordingState> {
        return videoRecordingRepository.getRecordingStateFlow()
    }
}
