package com.example.hiddencam.domain.usecase

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
import com.example.hiddencam.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating settings
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setCameraFacing(facing: CameraFacing) {
        settingsRepository.setCameraFacing(facing)
    }
    
    suspend fun setResolution(resolution: VideoResolution) {
        settingsRepository.setResolution(resolution)
    }
    
    suspend fun setFrameRate(fps: Int) {
        settingsRepository.setFrameRate(fps)
    }
    
    suspend fun setBitrate(bitrate: VideoBitrate) {
        settingsRepository.setBitrate(bitrate)
    }
    
    suspend fun setAudioSource(source: AudioSource) {
        settingsRepository.setAudioSource(source)
    }
    
    suspend fun setVolumeButtonEnabled(enabled: Boolean) {
        settingsRepository.setVolumeButtonEnabled(enabled)
    }
    
    suspend fun setPowerButtonEnabled(enabled: Boolean) {
        settingsRepository.setPowerButtonEnabled(enabled)
    }
    
    suspend fun setVibrationFeedbackEnabled(enabled: Boolean) {
        settingsRepository.setVibrationFeedbackEnabled(enabled)
    }
    
    suspend fun setOrientation(orientation: VideoOrientation) {
        settingsRepository.setOrientation(orientation)
    }
    
    suspend fun setFlashEnabled(enabled: Boolean) {
        settingsRepository.setFlashEnabled(enabled)
    }
    
    suspend fun setAppIcon(appIcon: AppIcon) {
        settingsRepository.setAppIcon(appIcon)
    }
    
    suspend fun setAppName(appName: AppName) {
        settingsRepository.setAppName(appName)
    }
    
    // Advanced camera settings
    
    suspend fun setIsoMode(isoMode: IsoMode) {
        settingsRepository.setIsoMode(isoMode)
    }
    
    suspend fun setExposureCompensation(ev: Int) {
        settingsRepository.setExposureCompensation(ev)
    }
    
    suspend fun setShutterSpeedMode(mode: ShutterSpeedMode) {
        settingsRepository.setShutterSpeedMode(mode)
    }
    
    suspend fun setCustomShutterSpeed(speedNs: Long) {
        settingsRepository.setCustomShutterSpeed(speedNs)
    }
    
    suspend fun setFocusMode(focusMode: FocusMode) {
        settingsRepository.setFocusMode(focusMode)
    }

    suspend fun setRecordingMode(recordingMode: RecordingMode) {
        settingsRepository.setRecordingMode(recordingMode)
    }

    suspend fun setLoopRecordingMinFreeGB(minFreeGB: Int) {
        settingsRepository.setLoopRecordingMinFreeGB(minFreeGB)
    }
}
