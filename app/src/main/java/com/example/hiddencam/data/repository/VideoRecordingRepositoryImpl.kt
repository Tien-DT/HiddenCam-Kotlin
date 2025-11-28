package com.example.hiddencam.data.repository

import com.example.hiddencam.data.local.SettingsDataStore
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation that acts as a bridge between UI and the VideoRecordingService.
 * The actual recording logic is handled by the service.
 */
@Singleton
class VideoRecordingRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : VideoRecordingRepository {
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    
    // Callbacks to communicate with the service
    var onStartRecording: ((VideoSettings) -> Unit)? = null
    var onPauseRecording: (() -> Unit)? = null
    var onResumeRecording: (() -> Unit)? = null
    var onStopRecording: (() -> Unit)? = null
    
    override fun getRecordingStateFlow(): Flow<RecordingState> {
        return _recordingState.asStateFlow()
    }
    
    override fun getCurrentState(): RecordingState {
        return _recordingState.value
    }
    
    override suspend fun startRecording(settings: VideoSettings) {
        _recordingState.value = RecordingState.Starting
        onStartRecording?.invoke(settings)
    }
    
    override suspend fun pauseRecording() {
        onPauseRecording?.invoke()
    }
    
    override suspend fun resumeRecording() {
        onResumeRecording?.invoke()
    }
    
    override suspend fun stopRecording() {
        _recordingState.value = RecordingState.Stopping
        onStopRecording?.invoke()
    }
    
    override fun isRecording(): Boolean {
        return _recordingState.value is RecordingState.Recording ||
               _recordingState.value is RecordingState.Paused
    }
    
    /**
     * Called by the service to update recording state
     */
    fun updateRecordingState(state: RecordingState) {
        _recordingState.value = state
    }
    
    /**
     * Get current settings for widget-initiated recording
     */
    suspend fun getSettings(): VideoSettings {
        return settingsDataStore.getSettings()
    }
}
