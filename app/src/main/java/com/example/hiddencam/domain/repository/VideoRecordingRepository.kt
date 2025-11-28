package com.example.hiddencam.domain.repository

import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for video recording operations
 */
interface VideoRecordingRepository {
    
    /**
     * Get current recording state as a Flow
     */
    fun getRecordingStateFlow(): Flow<RecordingState>
    
    /**
     * Get current recording state
     */
    fun getCurrentState(): RecordingState
    
    /**
     * Start video recording with given settings
     */
    suspend fun startRecording(settings: VideoSettings)
    
    /**
     * Pause current recording
     */
    suspend fun pauseRecording()
    
    /**
     * Resume paused recording
     */
    suspend fun resumeRecording()
    
    /**
     * Stop recording and save video file
     */
    suspend fun stopRecording()
    
    /**
     * Check if recording is currently active
     */
    fun isRecording(): Boolean
}
