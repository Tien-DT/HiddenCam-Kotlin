package com.example.hiddencam.data.repository

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.hiddencam.data.local.SettingsDataStore
import com.example.hiddencam.data.service.VideoRecordingService
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val appContext: Context
) : VideoRecordingRepository {
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    
    override fun getRecordingStateFlow(): Flow<RecordingState> {
        return _recordingState.asStateFlow()
    }
    
    override fun getCurrentState(): RecordingState {
        return _recordingState.value
    }
    
    override suspend fun startRecording(settings: VideoSettings) {
        _recordingState.value = RecordingState.Starting
        val intent = VideoRecordingService.getIntent(
            appContext,
            VideoRecordingService.ACTION_START_RECORDING
        )
        ContextCompat.startForegroundService(appContext, intent)
    }
    
    override suspend fun pauseRecording() {
        val intent = VideoRecordingService.getIntent(
            appContext,
            VideoRecordingService.ACTION_PAUSE_RECORDING
        )
        appContext.startService(intent)
    }
    
    override suspend fun resumeRecording() {
        val intent = VideoRecordingService.getIntent(
            appContext,
            VideoRecordingService.ACTION_RESUME_RECORDING
        )
        appContext.startService(intent)
    }
    
    override suspend fun stopRecording() {
        _recordingState.value = RecordingState.Stopping
        val intent = VideoRecordingService.getIntent(
            appContext,
            VideoRecordingService.ACTION_STOP_RECORDING
        )
        appContext.startService(intent)
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
