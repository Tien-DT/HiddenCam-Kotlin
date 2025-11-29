package com.example.hiddencam.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Utility class for vibration feedback.
 * Provides haptic feedback for quick controls when screen is off.
 */
object VibrationUtil {
    
    private const val SHORT_VIBRATION_MS = 100L
    private const val PAUSE_BETWEEN_VIBRATIONS_MS = 100L
    
    /**
     * Get vibrator service compatible with all API levels
     */
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Single short vibration - indicates action received
     */
    fun vibrateOnce(context: Context) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(SHORT_VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(SHORT_VIBRATION_MS)
        }
    }
    
    /**
     * Double vibration pattern - indicates recording started
     * Pattern: vibrate - pause - vibrate
     */
    fun vibrateRecordingStarted(context: Context) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        // Pattern: delay, vibrate, delay, vibrate
        // [0] = delay before start
        // [1] = first vibration duration
        // [2] = pause duration  
        // [3] = second vibration duration
        val pattern = longArrayOf(0, SHORT_VIBRATION_MS, PAUSE_BETWEEN_VIBRATIONS_MS, SHORT_VIBRATION_MS)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1) // -1 = don't repeat
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Triple vibration pattern - indicates recording stopped
     * Pattern: vibrate - pause - vibrate - pause - vibrate
     */
    fun vibrateRecordingStopped(context: Context) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        val pattern = longArrayOf(
            0, SHORT_VIBRATION_MS, 
            PAUSE_BETWEEN_VIBRATIONS_MS, SHORT_VIBRATION_MS,
            PAUSE_BETWEEN_VIBRATIONS_MS, SHORT_VIBRATION_MS
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    /**
     * Long vibration - indicates error or failure
     */
    fun vibrateError(context: Context) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        val longVibrationMs = 500L
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(longVibrationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longVibrationMs)
        }
    }
}
