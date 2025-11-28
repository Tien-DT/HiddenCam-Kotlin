package com.example.hiddencam.domain.repository

import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing app settings
 */
interface SettingsRepository {
    
    /**
     * Get settings as a Flow for observing changes
     */
    fun getSettingsFlow(): Flow<VideoSettings>
    
    /**
     * Get current settings synchronously
     */
    suspend fun getSettings(): VideoSettings
    
    /**
     * Update camera facing (front/back)
     */
    suspend fun setCameraFacing(facing: CameraFacing)
    
    /**
     * Update video resolution
     */
    suspend fun setResolution(resolution: VideoResolution)
    
    /**
     * Update frame rate
     */
    suspend fun setFrameRate(fps: Int)
    
    /**
     * Update video bitrate
     */
    suspend fun setBitrate(bitrate: VideoBitrate)
    
    /**
     * Update audio source
     */
    suspend fun setAudioSource(source: AudioSource)
    
    /**
     * Enable/disable volume button control
     */
    suspend fun setVolumeButtonEnabled(enabled: Boolean)
    
    /**
     * Enable/disable power button control
     */
    suspend fun setPowerButtonEnabled(enabled: Boolean)
    
    /**
     * Update video orientation (portrait/landscape)
     */
    suspend fun setOrientation(orientation: VideoOrientation)
    
    /**
     * Enable/disable flash light
     */
    suspend fun setFlashEnabled(enabled: Boolean)
}
