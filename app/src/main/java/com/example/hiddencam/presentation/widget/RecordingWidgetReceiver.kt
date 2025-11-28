package com.example.hiddencam.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.hiddencam.R
import com.example.hiddencam.data.service.VideoRecordingService

class RecordingWidgetReceiver : AppWidgetProvider() {
    
    companion object {
        const val ACTION_TOGGLE_RECORDING = "com.example.hiddencam.TOGGLE_RECORDING"
        const val ACTION_UPDATE_WIDGET = "com.example.hiddencam.UPDATE_WIDGET"
        const val EXTRA_IS_RECORDING = "extra_is_recording"
        const val EXTRA_DURATION = "extra_duration"
        
        private var isRecording = false
        
        fun updateWidget(context: Context, isRecording: Boolean, duration: String = "") {
            this.isRecording = isRecording
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, RecordingWidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            
            widgetIds.forEach { widgetId ->
                updateAppWidget(context, appWidgetManager, widgetId, isRecording, duration)
            }
        }
        
        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isRecording: Boolean,
            duration: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_recording)
            
            // Set click intent
            val toggleIntent = Intent(context, RecordingWidgetReceiver::class.java).apply {
                action = ACTION_TOGGLE_RECORDING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_status, pendingIntent)
            
            // Update UI based on recording state
            if (isRecording) {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_stop_recording)
                views.setTextViewText(R.id.widget_status, context.getString(R.string.widget_tap_to_stop))
                views.setInt(R.id.widget_icon, "setBackgroundResource", R.drawable.widget_background_recording)
                
                if (duration.isNotEmpty()) {
                    views.setTextViewText(R.id.widget_timer, duration)
                    views.setViewVisibility(R.id.widget_timer, android.view.View.VISIBLE)
                }
            } else {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_videocam)
                views.setTextViewText(R.id.widget_status, context.getString(R.string.widget_tap_to_record))
                views.setViewVisibility(R.id.widget_timer, android.view.View.GONE)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId, isRecording, "")
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_TOGGLE_RECORDING -> {
                if (isRecording) {
                    // Stop recording
                    val stopIntent = VideoRecordingService.getIntent(
                        context, 
                        VideoRecordingService.ACTION_STOP_RECORDING
                    )
                    context.startService(stopIntent)
                    isRecording = false
                } else {
                    // Start recording
                    val startIntent = VideoRecordingService.getIntent(
                        context,
                        VideoRecordingService.ACTION_START_RECORDING
                    )
                    context.startForegroundService(startIntent)
                    isRecording = true
                }
                updateWidget(context, isRecording)
            }
            ACTION_UPDATE_WIDGET -> {
                val recording = intent.getBooleanExtra(EXTRA_IS_RECORDING, false)
                val duration = intent.getStringExtra(EXTRA_DURATION) ?: ""
                isRecording = recording
                updateWidget(context, recording, duration)
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        // Called when first widget is created
    }
    
    override fun onDisabled(context: Context) {
        // Called when last widget is removed
    }
}
