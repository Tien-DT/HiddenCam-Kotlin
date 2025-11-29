package com.example.hiddencam.data.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.example.hiddencam.data.local.SettingsDataStore
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.util.VibrationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Accessibility Service for handling volume button press when screen is off.
 * 
 * This service intercepts volume key events even when the screen is turned off,
 * enabling quick recording control without unlocking the device.
 * 
 * User must enable this service in Settings > Accessibility > HiddenCam Volume Control
 */
class VolumeKeyAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "VolumeKeyAccessibility"
        private const val DOUBLE_PRESS_TIMEOUT_MS = 500L
        
        // Static flag to communicate service state
        @Volatile
        var isServiceEnabled: Boolean = false
            private set
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var settingsDataStore: SettingsDataStore? = null
    
    private var lastVolumeDownTime = 0L
    private var volumeDownPressCount = 0
    private var pendingActionJob: Job? = null
    
    // Screen state tracking
    private var isScreenOn = true
    private var screenStateReceiver: BroadcastReceiver? = null
    
    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(applicationContext)
        registerScreenStateReceiver()
        Log.d(TAG, "VolumeKeyAccessibilityService created")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceEnabled = true
        
        // Configure the accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            notificationTimeout = 100
        }
        serviceInfo = info
        
        Log.d(TAG, "VolumeKeyAccessibilityService connected and configured")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isServiceEnabled = false
        unregisterScreenStateReceiver()
        serviceScope.cancel()
        Log.d(TAG, "VolumeKeyAccessibilityService destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events, only key events
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "VolumeKeyAccessibilityService interrupted")
    }
    
    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Only handle volume down key
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }
        
        // Only handle key down events
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }
        
        Log.d(TAG, "Volume down pressed, screen on: $isScreenOn")
        
        serviceScope.launch {
            handleVolumeDownPress()
        }
        
        // Return true to consume the event when screen is off (don't let it change volume)
        // When screen is on, let the volume change happen but still detect double press
        return !isScreenOn
    }
    
    private suspend fun handleVolumeDownPress() {
        // Check if volume button control is enabled in settings
        val settings = settingsDataStore?.getSettings() ?: return
        if (!settings.volumeButtonEnabled) {
            Log.d(TAG, "Volume button control is disabled in settings")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        // Double press detection
        if (currentTime - lastVolumeDownTime < DOUBLE_PRESS_TIMEOUT_MS) {
            volumeDownPressCount++
        } else {
            volumeDownPressCount = 1
        }
        lastVolumeDownTime = currentTime
        
        // Cancel any pending action
        pendingActionJob?.cancel()
        
        // Wait for potential additional presses before taking action
        pendingActionJob = serviceScope.launch {
            kotlinx.coroutines.delay(DOUBLE_PRESS_TIMEOUT_MS)
            
            if (volumeDownPressCount >= 2) {
                // Double press = toggle recording
                Log.d(TAG, "Double press detected - toggling recording")
                toggleRecording()
            }
            // Single press is ignored (to allow volume adjustment when needed)
            
            volumeDownPressCount = 0
        }
    }
    
    private fun toggleRecording() {
        serviceScope.launch {
            try {
                // Get current recording state from shared preferences or service
                // Since we can't easily inject repository here, we'll just send toggle intent
                val intent = VideoRecordingService.getIntent(
                    applicationContext, 
                    VideoRecordingService.ACTION_TOGGLE_RECORDING_WITH_VIBRATION
                )
                ContextCompat.startForegroundService(applicationContext, intent)
                
                Log.d(TAG, "Toggle recording intent sent")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling recording", e)
                VibrationUtil.vibrateError(applicationContext)
            }
        }
    }
    
    private fun registerScreenStateReceiver() {
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOn = true
                        Log.d(TAG, "Screen ON")
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        Log.d(TAG, "Screen OFF")
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        
        // Initialize screen state
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        isScreenOn = powerManager.isInteractive
    }
    
    private fun unregisterScreenStateReceiver() {
        screenStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering screen state receiver", e)
            }
        }
        screenStateReceiver = null
    }
}
