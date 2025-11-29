package com.example.hiddencam.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import androidx.datastore.preferences.core.longPreferencesKey
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
        val VIBRATION_FEEDBACK_ENABLED = booleanPreferencesKey("vibration_feedback_enabled")
        val ORIENTATION = stringPreferencesKey("orientation")
        val FLASH_ENABLED = booleanPreferencesKey("flash_enabled")
        val APP_ICON = stringPreferencesKey("app_icon")
        val APP_NAME = stringPreferencesKey("app_name")
        // Advanced camera settings
        val ISO_MODE = stringPreferencesKey("iso_mode")
        val EXPOSURE_COMPENSATION = intPreferencesKey("exposure_compensation")
        val SHUTTER_SPEED_MODE = stringPreferencesKey("shutter_speed_mode")
        val CUSTOM_SHUTTER_SPEED = longPreferencesKey("custom_shutter_speed")
        val FOCUS_MODE = stringPreferencesKey("focus_mode")
        // Recording mode settings
        val RECORDING_MODE = stringPreferencesKey("recording_mode")
        val LOOP_RECORDING_MIN_FREE_GB = intPreferencesKey("loop_recording_min_free_gb")
        // Web Server settings
        val WEB_SERVER_ENABLED = booleanPreferencesKey("web_server_enabled")
        val WEB_SERVER_PORT = intPreferencesKey("web_server_port")
        val WEB_SERVER_PASSWORD = stringPreferencesKey("web_server_password")
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
            vibrationFeedbackEnabled = preferences[PreferencesKeys.VIBRATION_FEEDBACK_ENABLED] ?: true,
            orientation = preferences[PreferencesKeys.ORIENTATION]?.let {
                VideoOrientation.valueOf(it)
            } ?: VideoOrientation.PORTRAIT,
            flashEnabled = preferences[PreferencesKeys.FLASH_ENABLED] ?: false,
            appIcon = preferences[PreferencesKeys.APP_ICON]?.let {
                try { AppIcon.valueOf(it) } catch (e: Exception) { AppIcon.DEFAULT }
            } ?: AppIcon.DEFAULT,
            appName = preferences[PreferencesKeys.APP_NAME]?.let {
                try { AppName.valueOf(it) } catch (e: Exception) { AppName.HIDDEN_CAM }
            } ?: AppName.HIDDEN_CAM,
            // Advanced camera settings
            isoMode = preferences[PreferencesKeys.ISO_MODE]?.let {
                try { IsoMode.valueOf(it) } catch (e: Exception) { IsoMode.AUTO }
            } ?: IsoMode.AUTO,
            exposureCompensation = preferences[PreferencesKeys.EXPOSURE_COMPENSATION] ?: 0,
            shutterSpeedMode = preferences[PreferencesKeys.SHUTTER_SPEED_MODE]?.let {
                try { ShutterSpeedMode.valueOf(it) } catch (e: Exception) { ShutterSpeedMode.AUTO }
            } ?: ShutterSpeedMode.AUTO,
            customShutterSpeed = preferences[PreferencesKeys.CUSTOM_SHUTTER_SPEED] ?: 0L,
            focusMode = preferences[PreferencesKeys.FOCUS_MODE]?.let {
                try { FocusMode.valueOf(it) } catch (e: Exception) { FocusMode.CONTINUOUS_VIDEO }
            } ?: FocusMode.CONTINUOUS_VIDEO,
            // Recording mode
            recordingMode = preferences[PreferencesKeys.RECORDING_MODE]?.let {
                try { RecordingMode.valueOf(it) } catch (e: Exception) { RecordingMode.MANUAL }
            } ?: RecordingMode.MANUAL,
            loopRecordingMinFreeGB = preferences[PreferencesKeys.LOOP_RECORDING_MIN_FREE_GB] ?: 2,
            // Web Server settings
            webServerEnabled = preferences[PreferencesKeys.WEB_SERVER_ENABLED] ?: false,
            webServerPort = preferences[PreferencesKeys.WEB_SERVER_PORT] ?: 8080,
            webServerPassword = preferences[PreferencesKeys.WEB_SERVER_PASSWORD] ?: "hiddencam123"
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
    
    suspend fun setVibrationFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_FEEDBACK_ENABLED] = enabled
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
    
    suspend fun setAppIcon(appIcon: AppIcon) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_ICON] = appIcon.name
        }
    }
    
    suspend fun setAppName(appName: AppName) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_NAME] = appName.name
        }
    }
    
    // Advanced camera settings
    suspend fun setIsoMode(isoMode: IsoMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ISO_MODE] = isoMode.name
        }
    }
    
    suspend fun setExposureCompensation(ev: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPOSURE_COMPENSATION] = ev
        }
    }
    
    suspend fun setShutterSpeedMode(mode: ShutterSpeedMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUTTER_SPEED_MODE] = mode.name
        }
    }
    
    suspend fun setCustomShutterSpeed(speedNs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_SHUTTER_SPEED] = speedNs
        }
    }
    
    suspend fun setFocusMode(focusMode: FocusMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_MODE] = focusMode.name
        }
    }

    // Recording mode settings
    suspend fun setRecordingMode(recordingMode: RecordingMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORDING_MODE] = recordingMode.name
        }
    }

    suspend fun setLoopRecordingMinFreeGB(minFreeGB: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOOP_RECORDING_MIN_FREE_GB] = minFreeGB
        }
    }

    // Web Server settings
    suspend fun setWebServerEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEB_SERVER_ENABLED] = enabled
        }
    }

    suspend fun setWebServerPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEB_SERVER_PORT] = port
        }
    }

    suspend fun setWebServerPassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEB_SERVER_PASSWORD] = password
        }
    }
}
