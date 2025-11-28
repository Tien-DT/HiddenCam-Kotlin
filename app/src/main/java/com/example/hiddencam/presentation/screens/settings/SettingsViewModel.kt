package com.example.hiddencam.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.usecase.GetSettingsUseCase
import com.example.hiddencam.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {
    
    val settings: StateFlow<VideoSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VideoSettings()
        )
    
    fun setCameraFacing(facing: CameraFacing) {
        viewModelScope.launch {
            updateSettingsUseCase.setCameraFacing(facing)
        }
    }
    
    fun setResolution(resolution: VideoResolution) {
        viewModelScope.launch {
            updateSettingsUseCase.setResolution(resolution)
        }
    }
    
    fun setFrameRate(fps: Int) {
        viewModelScope.launch {
            updateSettingsUseCase.setFrameRate(fps)
        }
    }
    
    fun setBitrate(bitrate: VideoBitrate) {
        viewModelScope.launch {
            updateSettingsUseCase.setBitrate(bitrate)
        }
    }
    
    fun setAudioSource(source: AudioSource) {
        viewModelScope.launch {
            updateSettingsUseCase.setAudioSource(source)
        }
    }
    
    fun setVolumeButtonEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setVolumeButtonEnabled(enabled)
        }
    }
    
    fun setPowerButtonEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setPowerButtonEnabled(enabled)
        }
    }
}
