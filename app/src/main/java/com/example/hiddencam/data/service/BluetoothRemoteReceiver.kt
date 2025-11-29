package com.example.hiddencam.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives media button events from Bluetooth remotes.
 * This receiver handles play/pause/stop commands from connected Bluetooth devices.
 * 
 * Note: This receiver works best when the app is in the foreground.
 * When the screen is locked, the system may route media buttons to other apps.
 */
class BluetoothRemoteReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON != intent.action) {
            return
        }
        
        val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            ?: return
        
        // Only handle key down events
        if (keyEvent.action != KeyEvent.ACTION_DOWN) {
            return
        }
        
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                // Toggle recording
                handleToggleRecording(context)
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                // Stop recording
                handleStopRecording(context)
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                // Pause recording
                handlePauseRecording(context)
            }
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                // Single press on headset/remote button - toggle recording
                handleToggleRecording(context)
            }
        }
    }
    
    private fun handleToggleRecording(context: Context) {
        val intent = VideoRecordingService.getIntent(context, VideoRecordingService.ACTION_TOGGLE_RECORDING)
        ContextCompat.startForegroundService(context, intent)
    }
    
    private fun handleStopRecording(context: Context) {
        val intent = VideoRecordingService.getIntent(context, VideoRecordingService.ACTION_STOP_RECORDING)
        context.startService(intent)
    }
    
    private fun handlePauseRecording(context: Context) {
        val intent = VideoRecordingService.getIntent(context, VideoRecordingService.ACTION_PAUSE_RECORDING)
        context.startService(intent)
    }
}
