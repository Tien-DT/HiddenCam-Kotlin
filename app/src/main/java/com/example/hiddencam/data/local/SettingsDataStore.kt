package com.example.hiddencam.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val CAMERA_FACING = stringPreferencesKey("camera_facing")
        val RESOLUTION = stringPreferencesKey("resolution")
        val FRAME_RATE = intPreferencesKey("frame_rate")
        val BITRATE = stringPreferencesKey("bitrate")
        val AUDIO_SOURCE = stringPreferencesKey("audio_source")
        val VOLUME_BUTTON_ENABLED = booleanPreferencesKey("volume_button_enabled")
        val POWER_BUTTON_ENABLED = booleanPreferencesKey("power_button_enabled")
        val ORIENTATION = stringPreferencesKey("orientation")
        val FLASH_ENABLED = booleanPreferencesKey("flash_enabled")
    }
    
    val settingsFlow: Flow<VideoSettings> = context.dataStore.data.map { preferences ->
        VideoSettings(
            cameraFacing = preferences[PreferencesKeys.CAMERA_FACING]?.let { 
                CameraFacing.valueOf(it) 
            } ?: CameraFacing.BACK,
            resolution = preferences[PreferencesKeys.RESOLUTION]?.let { 
                VideoResolution.valueOf(it) 
            } ?: VideoResolution.HD_720P,
            frameRate = preferences[PreferencesKeys.FRAME_RATE] ?: 30,
            bitrate = preferences[PreferencesKeys.BITRATE]?.let { 
                VideoBitrate.valueOf(it) 
            } ?: VideoBitrate.MEDIUM,
            audioSource = preferences[PreferencesKeys.AUDIO_SOURCE]?.let { 
                AudioSource.valueOf(it) 
            } ?: AudioSource.MICROPHONE,
            volumeButtonEnabled = preferences[PreferencesKeys.VOLUME_BUTTON_ENABLED] ?: true,
            powerButtonEnabled = preferences[PreferencesKeys.POWER_BUTTON_ENABLED] ?: true,
            orientation = preferences[PreferencesKeys.ORIENTATION]?.let {
                VideoOrientation.valueOf(it)
            } ?: VideoOrientation.PORTRAIT,
            flashEnabled = preferences[PreferencesKeys.FLASH_ENABLED] ?: false
        )
    }
    
    suspend fun getSettings(): VideoSettings {
        return settingsFlow.first()
    }
    
    suspend fun setCameraFacing(facing: CameraFacing) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAMERA_FACING] = facing.name
        }
    }
    
    suspend fun setResolution(resolution: VideoResolution) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESOLUTION] = resolution.name
        }
    }
    
    suspend fun setFrameRate(fps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FRAME_RATE] = fps
        }
    }
    
    suspend fun setBitrate(bitrate: VideoBitrate) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BITRATE] = bitrate.name
        }
    }
    
    suspend fun setAudioSource(source: AudioSource) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_SOURCE] = source.name
        }
    }
    
    suspend fun setVolumeButtonEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOLUME_BUTTON_ENABLED] = enabled
        }
    }
    
    suspend fun setPowerButtonEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POWER_BUTTON_ENABLED] = enabled
        }
    }
    
    suspend fun setOrientation(orientation: VideoOrientation) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ORIENTATION] = orientation.name
        }
    }
    
    suspend fun setFlashEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLASH_ENABLED] = enabled
        }
    }
}
