package com.example.hiddencam.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import com.example.hiddencam.R
import com.example.hiddencam.data.service.VideoRecordingService

class RecordingWidgetReceiver : AppWidgetProvider() {
    
    companion object {
        private const val TAG = "RecordingWidget"
        const val ACTION_TOGGLE_RECORDING = "com.example.hiddencam.TOGGLE_RECORDING"
        const val ACTION_UPDATE_WIDGET = "com.example.hiddencam.UPDATE_WIDGET"
        const val EXTRA_IS_RECORDING = "extra_is_recording"
        
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_IS_RECORDING = "is_recording"
        
        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        
        fun isRecording(context: Context): Boolean {
            return getPrefs(context).getBoolean(KEY_IS_RECORDING, false)
        }
        
        fun setRecording(context: Context, recording: Boolean) {
            getPrefs(context).edit().putBoolean(KEY_IS_RECORDING, recording).apply()
        }
        
        fun updateWidget(context: Context, isRecording: Boolean) {
            Log.d(TAG, "updateWidget called: isRecording=$isRecording")
            setRecording(context, isRecording)
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, RecordingWidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            
            Log.d(TAG, "Widget IDs: ${widgetIds.toList()}")
            
            widgetIds.forEach { widgetId ->
                updateAppWidget(context, appWidgetManager, widgetId, isRecording)
            }
        }
        
        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isRecording: Boolean
        ) {
            Log.d(TAG, "updateAppWidget: widgetId=$appWidgetId, isRecording=$isRecording")
            
            val views = RemoteViews(context.packageName, R.layout.widget_recording)
            
            // Set click intent for the entire widget
            val toggleIntent = Intent(context, RecordingWidgetReceiver::class.java).apply {
                action = ACTION_TOGGLE_RECORDING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
            
            // Update UI based on recording state
            if (isRecording) {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_stop_recording)
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background_recording)
            } else {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_videocam)
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called")
        val recording = isRecording(context)
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId, recording)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        Log.d(TAG, "onReceive: action=${intent.action}")
        
        when (intent.action) {
            ACTION_TOGGLE_RECORDING -> {
                val currentlyRecording = isRecording(context)
                Log.d(TAG, "Toggle recording: currentlyRecording=$currentlyRecording")
                
                if (currentlyRecording) {
                    // Stop recording
                    Log.d(TAG, "Stopping recording...")
                    val stopIntent = Intent(context, VideoRecordingService::class.java).apply {
                        action = VideoRecordingService.ACTION_STOP_RECORDING
                    }
                    context.startService(stopIntent)
                    setRecording(context, false)
                    updateWidget(context, false)
                } else {
                    // Start recording
                    Log.d(TAG, "Starting recording...")
                    val startIntent = Intent(context, VideoRecordingService::class.java).apply {
                        action = VideoRecordingService.ACTION_START_RECORDING
                    }
                    context.startForegroundService(startIntent)
                    setRecording(context, true)
                    updateWidget(context, true)
                }
            }
            ACTION_UPDATE_WIDGET -> {
                val recording = intent.getBooleanExtra(EXTRA_IS_RECORDING, false)
                Log.d(TAG, "Update widget from service: recording=$recording")
                updateWidget(context, recording)
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: First widget created")
    }
    
    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: Last widget removed")
    }
}
