package com.example.hiddencam.data.repository

import com.example.hiddencam.data.local.SettingsDataStore
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
import com.example.hiddencam.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    
    override fun getSettingsFlow(): Flow<VideoSettings> {
        return settingsDataStore.settingsFlow
    }
    
    override suspend fun getSettings(): VideoSettings {
        return settingsDataStore.getSettings()
    }
    
    override suspend fun setCameraFacing(facing: CameraFacing) {
        settingsDataStore.setCameraFacing(facing)
    }
    
    override suspend fun setResolution(resolution: VideoResolution) {
        settingsDataStore.setResolution(resolution)
    }
    
    override suspend fun setFrameRate(fps: Int) {
        settingsDataStore.setFrameRate(fps)
    }
    
    override suspend fun setBitrate(bitrate: VideoBitrate) {
        settingsDataStore.setBitrate(bitrate)
    }
    
    override suspend fun setAudioSource(source: AudioSource) {
        settingsDataStore.setAudioSource(source)
    }
    
    override suspend fun setVolumeButtonEnabled(enabled: Boolean) {
        settingsDataStore.setVolumeButtonEnabled(enabled)
    }
    
    override suspend fun setPowerButtonEnabled(enabled: Boolean) {
        settingsDataStore.setPowerButtonEnabled(enabled)
    }
    
    override suspend fun setVibrationFeedbackEnabled(enabled: Boolean) {
        settingsDataStore.setVibrationFeedbackEnabled(enabled)
    }
    
    override suspend fun setOrientation(orientation: VideoOrientation) {
        settingsDataStore.setOrientation(orientation)
    }
    
    override suspend fun setFlashEnabled(enabled: Boolean) {
        settingsDataStore.setFlashEnabled(enabled)
    }
    
    override suspend fun setAppIcon(appIcon: AppIcon) {
        settingsDataStore.setAppIcon(appIcon)
    }
    
    override suspend fun setAppName(appName: AppName) {
        settingsDataStore.setAppName(appName)
    }
    
    // Advanced camera settings
    
    override suspend fun setIsoMode(isoMode: IsoMode) {
        settingsDataStore.setIsoMode(isoMode)
    }
    
    override suspend fun setExposureCompensation(ev: Int) {
        settingsDataStore.setExposureCompensation(ev)
    }
    
    override suspend fun setShutterSpeedMode(mode: ShutterSpeedMode) {
        settingsDataStore.setShutterSpeedMode(mode)
    }
    
    override suspend fun setCustomShutterSpeed(speedNs: Long) {
        settingsDataStore.setCustomShutterSpeed(speedNs)
    }
    
    override suspend fun setFocusMode(focusMode: FocusMode) {
        settingsDataStore.setFocusMode(focusMode)
    }

    override suspend fun setRecordingMode(recordingMode: RecordingMode) {
        settingsDataStore.setRecordingMode(recordingMode)
    }

    override suspend fun setLoopRecordingMinFreeGB(minFreeGB: Int) {
        settingsDataStore.setLoopRecordingMinFreeGB(minFreeGB)
    }
}
