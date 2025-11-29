package com.example.hiddencam.domain.repository

import com.example.hiddencam.domain.model.AppIcon
import com.example.hiddencam.domain.model.AppName
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.FocusMode
import com.example.hiddencam.domain.model.IsoMode
import com.example.hiddencam.domain.model.RecordingMode
import com.example.hiddencam.domain.model.ShutterSpeedMode
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
     * Enable/disable vibration feedback for quick controls
     */
    suspend fun setVibrationFeedbackEnabled(enabled: Boolean)
    
    /**
     * Update video orientation (portrait/landscape)
     */
    suspend fun setOrientation(orientation: VideoOrientation)
    
    /**
     * Enable/disable flash light
     */
    suspend fun setFlashEnabled(enabled: Boolean)
    
    /**
     * Update app icon
     */
    suspend fun setAppIcon(appIcon: AppIcon)
    
    /**
     * Update app name
     */
    suspend fun setAppName(appName: AppName)
    
    // Advanced camera settings
    
    /**
     * Update ISO mode (Auto or specific ISO value)
     */
    suspend fun setIsoMode(isoMode: IsoMode)
    
    /**
     * Update exposure compensation (EV value)
     */
    suspend fun setExposureCompensation(ev: Int)
    
    /**
     * Update shutter speed mode (Auto or Custom)
     */
    suspend fun setShutterSpeedMode(mode: ShutterSpeedMode)
    
    /**
     * Update custom shutter speed in nanoseconds
     */
    suspend fun setCustomShutterSpeed(speedNs: Long)
    
    /**
     * Update focus mode
     */
    suspend fun setFocusMode(focusMode: FocusMode)

    /**
     * Update recording mode (Manual, Until Full, Loop)
     */
    suspend fun setRecordingMode(recordingMode: RecordingMode)

    /**
     * Update minimum free GB for loop recording
     */
    suspend fun setLoopRecordingMinFreeGB(minFreeGB: Int)

    // Web Server settings

    /**
     * Enable/disable web server
     */
    suspend fun setWebServerEnabled(enabled: Boolean)

    /**
     * Update web server port
     */
    suspend fun setWebServerPort(port: Int)

    /**
     * Update web server password
     */
    suspend fun setWebServerPassword(password: String)
}
