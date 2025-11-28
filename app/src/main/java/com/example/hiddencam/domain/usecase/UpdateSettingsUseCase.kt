package com.example.hiddencam.domain.usecase

import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
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
}
