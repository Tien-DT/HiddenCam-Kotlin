package com.example.hiddencam.data.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.hiddencam.HiddenCamApplication
import com.example.hiddencam.R
import com.example.hiddencam.data.repository.VideoRecordingRepositoryImpl
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.FocusMode
import com.example.hiddencam.domain.model.IsoMode
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.ShutterSpeedMode
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.presentation.MainActivity
import com.example.hiddencam.presentation.widget.RecordingWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class VideoRecordingService : LifecycleService() {
    
    companion object {
        private const val TAG = "VideoRecordingService"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_RECORDING = "action_start_recording"
        const val ACTION_PAUSE_RECORDING = "action_pause_recording"
        const val ACTION_RESUME_RECORDING = "action_resume_recording"
        const val ACTION_STOP_RECORDING = "action_stop_recording"
        
        fun getIntent(context: Context, action: String): Intent {
            return Intent(context, VideoRecordingService::class.java).apply {
                this.action = action
            }
        }
    }
    
    @Inject
    lateinit var videoRecordingRepository: VideoRecordingRepositoryImpl
    
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var cameraExecutor: ExecutorService? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentSettings: VideoSettings? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): VideoRecordingService = this@VideoRecordingService
    }
    
    override fun onCreate() {
        super.onCreate()
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupRepositoryCallbacks()
        acquireWakeLock()
    }
    
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                // Start recording from widget - load settings directly
                lifecycleScope.launch {
                    val settings = videoRecordingRepository.getSettings()
                    currentSettings = settings
                    startForegroundService()
                    startRecording(settings)
                }
            }
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
            ACTION_STOP_RECORDING -> stopRecording()
        }
        
        return START_STICKY
    }
    
    private fun setupRepositoryCallbacks() {
        videoRecordingRepository.onStartRecording = { settings ->
            currentSettings = settings
            startForegroundService()
            startRecording(settings)
        }
        
        videoRecordingRepository.onPauseRecording = {
            pauseRecording()
        }
        
        videoRecordingRepository.onResumeRecording = {
            resumeRecording()
        }
        
        videoRecordingRepository.onStopRecording = {
            stopRecording()
        }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HiddenCam::VideoRecordingWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L) // 10 hours max
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    private fun startForegroundService() {
        val notification = createNotification("Preparing to record...")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
    
    private fun createNotification(contentText: String, duration: String = ""): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = getIntent(this, ACTION_STOP_RECORDING)
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val pauseIntent = getIntent(this, ACTION_PAUSE_RECORDING)
        val pausePendingIntent = PendingIntent.getService(
            this,
            2,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val cameraInfo = currentSettings?.let { 
            "${it.cameraFacing.name} • ${it.resolution.displayName}" 
        } ?: ""
        
        val builder = NotificationCompat.Builder(this, HiddenCamApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("🔴 Recording Video")
            .setContentText(if (duration.isNotEmpty()) "Duration: $duration" else contentText)
            .setSubText(cameraInfo)
            .setSmallIcon(R.drawable.ic_videocam)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_stop_recording,
                "⏹ Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setUsesChronometer(true)
        
        return builder.build()
    }
    
    private fun updateNotification(contentText: String, duration: String = "") {
        val notification = createNotification(contentText, duration)
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Update widget - recording is active
        RecordingWidgetReceiver.updateWidget(this, true)
    }
    
    private fun startRecording(settings: VideoSettings) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(this@VideoRecordingService)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    bindCameraAndStartRecording(settings)
                }, ContextCompat.getMainExecutor(this@VideoRecordingService))
            } catch (e: Exception) {
                Log.e(TAG, "Error starting recording", e)
                videoRecordingRepository.updateRecordingState(
                    RecordingState.Error("Failed to start recording: ${e.message}")
                )
            }
        }
    }
    
    private fun bindCameraAndStartRecording(settings: VideoSettings) {
        try {
            cameraProvider?.unbindAll()
            
            val cameraSelector = when (settings.cameraFacing) {
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            }
            
            // Get supported qualities for the selected camera
            val cameraInfo = cameraProvider!!.bindToLifecycle(this, cameraSelector).cameraInfo
            val supportedQualities = QualitySelector.getSupportedQualities(cameraInfo)
            cameraProvider?.unbindAll()
            
            Log.d(TAG, "Supported qualities: $supportedQualities")
            
            val preferredQuality = when (settings.resolution) {
                VideoResolution.SD_480P -> Quality.SD
                VideoResolution.HD_720P -> Quality.HD
                VideoResolution.FHD_1080P -> Quality.FHD
                VideoResolution.UHD_4K -> Quality.UHD
            }
            
            // Use fallback strategy: preferred quality -> lower quality -> highest available
            val qualitySelector = if (supportedQualities.contains(preferredQuality)) {
                QualitySelector.from(preferredQuality)
            } else {
                // Fallback to best available quality
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD, Quality.UHD),
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            }
            
            Log.d(TAG, "Using quality selector with preferred: $preferredQuality")
            
            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()
            
            videoCapture = VideoCapture.withOutput(recorder)
            
            // Set target rotation based on orientation setting
            val targetRotation = when (settings.orientation) {
                VideoOrientation.PORTRAIT -> Surface.ROTATION_0
                VideoOrientation.LANDSCAPE -> Surface.ROTATION_90
            }
            videoCapture?.targetRotation = targetRotation
            
            camera = cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                videoCapture
            )
            
            // Enable flash/torch if requested and available (back camera only)
            if (settings.flashEnabled && settings.cameraFacing == CameraFacing.BACK) {
                camera?.cameraControl?.enableTorch(true)
                Log.d(TAG, "Flash enabled")
            }
            
            // Apply advanced camera settings using Camera2 interop
            applyAdvancedCameraSettings(settings)
            
            startVideoCapture(settings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera", e)
            videoRecordingRepository.updateRecordingState(
                RecordingState.Error("Failed to bind camera: ${e.message}")
            )
        }
    }
    
    @OptIn(ExperimentalCamera2Interop::class)
    private fun applyAdvancedCameraSettings(settings: VideoSettings) {
        val camera = camera ?: return
        
        try {
            val camera2CameraControl = Camera2CameraControl.from(camera.cameraControl)
            val camera2CameraInfo = Camera2CameraInfo.from(camera.cameraInfo)
            
            val captureRequestOptionsBuilder = CaptureRequestOptions.Builder()
            
            // Apply ISO setting
            if (settings.isoMode != IsoMode.AUTO) {
                val isoValue = settings.isoMode.isoValue
                if (isoValue != null) {
                    // Get supported ISO range
                    val isoRange = camera2CameraInfo.getCameraCharacteristic(
                        CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE
                    )
                    if (isoRange != null) {
                        val clampedIso = isoValue.coerceIn(isoRange.lower, isoRange.upper)
                        captureRequestOptionsBuilder.setCaptureRequestOption(
                            CaptureRequest.SENSOR_SENSITIVITY,
                            clampedIso
                        )
                        // Disable auto exposure to allow manual ISO
                        captureRequestOptionsBuilder.setCaptureRequestOption(
                            CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_OFF
                        )
                        Log.d(TAG, "Applied ISO: $clampedIso (range: ${isoRange.lower}-${isoRange.upper})")
                    }
                }
            }
            
            // Apply exposure compensation
            if (settings.exposureCompensation != 0) {
                val evRange = camera2CameraInfo.getCameraCharacteristic(
                    CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE
                )
                if (evRange != null) {
                    val clampedEv = settings.exposureCompensation.coerceIn(evRange.lower, evRange.upper)
                    captureRequestOptionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                        clampedEv
                    )
                    Log.d(TAG, "Applied exposure compensation: $clampedEv EV")
                }
            }
            
            // Apply shutter speed (exposure time)
            if (settings.shutterSpeedMode == ShutterSpeedMode.CUSTOM && settings.customShutterSpeed > 0) {
                val exposureTimeRange = camera2CameraInfo.getCameraCharacteristic(
                    CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE
                )
                if (exposureTimeRange != null) {
                    val clampedExposureTime = settings.customShutterSpeed.coerceIn(
                        exposureTimeRange.lower,
                        exposureTimeRange.upper
                    )
                    captureRequestOptionsBuilder.setCaptureRequestOption(
                        CaptureRequest.SENSOR_EXPOSURE_TIME,
                        clampedExposureTime
                    )
                    // Disable auto exposure to allow manual shutter speed
                    captureRequestOptionsBuilder.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_OFF
                    )
                    Log.d(TAG, "Applied shutter speed: ${clampedExposureTime}ns (1/${1_000_000_000L / clampedExposureTime}s)")
                }
            }
            
            // Apply focus mode
            val afMode = when (settings.focusMode) {
                FocusMode.CONTINUOUS_VIDEO -> CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                FocusMode.CONTINUOUS_PICTURE -> CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                FocusMode.AUTO -> CaptureRequest.CONTROL_AF_MODE_AUTO
                FocusMode.MACRO -> CaptureRequest.CONTROL_AF_MODE_MACRO
                FocusMode.INFINITY -> CaptureRequest.CONTROL_AF_MODE_OFF // Infinity focus uses OFF mode
                FocusMode.FACE_DETECTION -> CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO // Face detection uses continuous mode
            }
            
            // Check if the AF mode is supported
            val availableAfModes = camera2CameraInfo.getCameraCharacteristic(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            )
            if (availableAfModes != null && availableAfModes.contains(afMode)) {
                captureRequestOptionsBuilder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    afMode
                )
                Log.d(TAG, "Applied focus mode: $afMode")
                
                // For infinity focus, set focus distance to infinity
                if (settings.focusMode == FocusMode.INFINITY) {
                    captureRequestOptionsBuilder.setCaptureRequestOption(
                        CaptureRequest.LENS_FOCUS_DISTANCE,
                        0.0f // 0 = infinity
                    )
                }
            } else {
                Log.w(TAG, "Focus mode $afMode not supported, using default")
            }
            
            // Enable face detection if requested
            if (settings.focusMode == FocusMode.FACE_DETECTION) {
                val availableFaceDetectModes = camera2CameraInfo.getCameraCharacteristic(
                    CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES
                )
                if (availableFaceDetectModes != null && availableFaceDetectModes.isNotEmpty()) {
                    // Use the highest available face detection mode
                    val maxFaceDetectMode = availableFaceDetectModes.maxOrNull() ?: 0
                    if (maxFaceDetectMode > 0) {
                        captureRequestOptionsBuilder.setCaptureRequestOption(
                            CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                            maxFaceDetectMode
                        )
                        Log.d(TAG, "Enabled face detection mode: $maxFaceDetectMode")
                    }
                }
            }
            
            // Apply all capture request options
            camera2CameraControl.setCaptureRequestOptions(captureRequestOptionsBuilder.build())
            Log.d(TAG, "Advanced camera settings applied successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error applying advanced camera settings", e)
        }
    }
    
    private fun startVideoCapture(settings: VideoSettings) {
        val videoCapture = videoCapture ?: return
        
        val outputFile = createOutputFile()
        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        
        val pendingRecording = videoCapture.output
            .prepareRecording(this, outputOptions)
        
        // Add audio if enabled
        if (settings.audioSource != AudioSource.NONE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED) {
                pendingRecording.withAudioEnabled()
            }
        }
        
        activeRecording = pendingRecording.start(
            ContextCompat.getMainExecutor(this)
        ) { recordEvent ->
            handleRecordEvent(recordEvent)
        }
        
        updateNotification("Recording in progress...")
    }
    
    private fun handleRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                Log.d(TAG, "Recording started")
                videoRecordingRepository.updateRecordingState(RecordingState.Recording())
                RecordingWidgetReceiver.updateWidget(this, true)
            }
            is VideoRecordEvent.Status -> {
                val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                videoRecordingRepository.updateRecordingState(RecordingState.Recording(durationMs))
                
                val durationStr = formatDuration(durationMs)
                updateNotification("Recording in progress", durationStr)
            }
            is VideoRecordEvent.Pause -> {
                val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                videoRecordingRepository.updateRecordingState(RecordingState.Paused(durationMs))
                updateNotification("Recording paused", formatDuration(durationMs))
            }
            is VideoRecordEvent.Resume -> {
                videoRecordingRepository.updateRecordingState(RecordingState.Recording())
                updateNotification("Recording resumed")
            }
            is VideoRecordEvent.Finalize -> {
                // Turn off flash
                camera?.cameraControl?.enableTorch(false)
                
                // Update widget
                RecordingWidgetReceiver.updateWidget(this, false)
                
                if (event.hasError()) {
                    Log.e(TAG, "Recording error: ${event.error}")
                    videoRecordingRepository.updateRecordingState(
                        RecordingState.Error("Recording failed with error: ${event.error}")
                    )
                } else {
                    Log.d(TAG, "Recording saved: ${event.outputResults.outputUri}")
                    // Add to media store
                    addVideoToMediaStore(event.outputResults.outputUri.path ?: "")
                }
                videoRecordingRepository.updateRecordingState(RecordingState.Idle)
                stopSelf()
            }
        }
    }
    
    private fun pauseRecording() {
        activeRecording?.pause()
    }
    
    private fun resumeRecording() {
        activeRecording?.resume()
    }
    
    private fun stopRecording() {
        // Turn off flash
        camera?.cameraControl?.enableTorch(false)
        activeRecording?.stop()
        activeRecording = null
        RecordingWidgetReceiver.updateWidget(this, false)
    }
    
    private fun createOutputFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "VID_$timestamp.mp4"
        
        val outputDir = File(getExternalFilesDir(null), "Videos")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        return File(outputDir, fileName)
    }
    
    private fun addVideoToMediaStore(filePath: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val file = File(filePath)
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/HiddenCam")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            
            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
    }
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        activeRecording?.stop()
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        releaseWakeLock()
    }
}
