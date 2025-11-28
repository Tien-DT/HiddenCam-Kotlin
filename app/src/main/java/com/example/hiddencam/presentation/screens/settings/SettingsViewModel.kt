package com.example.hiddencam.presentation.screens.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiddencam.domain.model.AppIcon
import com.example.hiddencam.domain.model.AppName
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.usecase.GetSettingsUseCase
import com.example.hiddencam.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    @ApplicationContext private val context: Context
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
    
    fun setOrientation(orientation: VideoOrientation) {
        viewModelScope.launch {
            updateSettingsUseCase.setOrientation(orientation)
        }
    }
    
    fun setFlashEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setFlashEnabled(enabled)
        }
    }
    
    fun setAppIcon(appIcon: AppIcon) {
        viewModelScope.launch {
            // Disable all aliases first
            AppIcon.entries.forEach { icon ->
                val componentName = ComponentName(
                    context.packageName,
                    "com.example.hiddencam${icon.aliasName}"
                )
                context.packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
            
            // Enable selected alias
            val selectedComponent = ComponentName(
                context.packageName,
                "com.example.hiddencam${appIcon.aliasName}"
            )
            context.packageManager.setComponentEnabledSetting(
                selectedComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            
            updateSettingsUseCase.setAppIcon(appIcon)
        }
    }
    
    fun setAppName(appName: AppName) {
        viewModelScope.launch {
            updateSettingsUseCase.setAppName(appName)
        }
    }
}
