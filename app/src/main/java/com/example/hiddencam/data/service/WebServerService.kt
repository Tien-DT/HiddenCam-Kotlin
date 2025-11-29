package com.example.hiddencam.data.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.hiddencam.HiddenCamApplication
import com.example.hiddencam.R
import com.example.hiddencam.data.local.SettingsDataStore
import com.example.hiddencam.data.repository.VideoRecordingRepositoryImpl
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Web Server Service that provides:
 * - HTTP server for web-based camera control
 * - MJPEG streaming for live preview
 * - REST API for recording control and settings
 */
@AndroidEntryPoint
class WebServerService : LifecycleService() {
    
    companion object {
        private const val TAG = "WebServerService"
        private const val NOTIFICATION_ID = 2001
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOTSPOT_PASSWORD = "hiddencam123"
        
        const val ACTION_START_SERVER = "action_start_server"
        const val ACTION_STOP_SERVER = "action_stop_server"
        const val EXTRA_PORT = "extra_port"
        
        fun getIntent(context: Context, action: String, port: Int = DEFAULT_PORT): Intent {
            return Intent(context, WebServerService::class.java).apply {
                this.action = action
                putExtra(EXTRA_PORT, port)
            }
        }
        
        @Volatile
        var isServerRunning: Boolean = false
            private set
            
        @Volatile
        var serverAddress: String = ""
            private set
    }
    
    @Inject
    lateinit var videoRecordingRepository: VideoRecordingRepositoryImpl
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore
    
    private var webServer: CameraWebServer? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    
    // Current frame for MJPEG streaming
    private val currentFrame = AtomicReference<ByteArray?>(null)
    private val isStreaming = AtomicBoolean(false)
    
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): WebServerService = this@WebServerService
    }
    
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_SERVER -> {
                val port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT)
                startWebServer(port)
            }
            ACTION_STOP_SERVER -> {
                stopWebServer()
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopWebServer()
    }
    
    private fun startWebServer(port: Int) {
        if (webServer != null) {
            Log.d(TAG, "Web server already running")
            return
        }
        
        startForegroundService()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                webServer = CameraWebServer(port)
                webServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                isServerRunning = true
                serverAddress = "http://${getLocalIpAddress()}:$port"
                
                Log.d(TAG, "Web server started at $serverAddress")
                
                withContext(Dispatchers.Main) {
                    startCameraForStreaming()
                    updateNotification("Web Server running at $serverAddress")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start web server", e)
                isServerRunning = false
            }
        }
    }
    
    private fun stopWebServer() {
        webServer?.stop()
        webServer = null
        isServerRunning = false
        serverAddress = ""
        
        stopCameraStreaming()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.d(TAG, "Web server stopped")
    }
    
    private fun startForegroundService() {
        val notification = createNotification("Starting web server...")
        
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
    
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = getIntent(this, ACTION_STOP_SERVER)
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, HiddenCamApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("📡 Camera Web Server")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_videocam)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop_recording, "Stop Server", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun startCameraForStreaming() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraForStreaming()
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun bindCameraForStreaming() {
        lifecycleScope.launch {
            val settings = settingsDataStore.getSettings()
            
            val cameraSelector = when (settings.cameraFacing) {
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                CameraFacing.USB -> CameraSelector.DEFAULT_BACK_CAMERA // Fallback
            }
            
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(this@WebServerService)) { imageProxy ->
                        processImageForStreaming(imageProxy)
                    }
                }
            
            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this@WebServerService,
                    cameraSelector,
                    imageAnalysis
                )
                isStreaming.set(true)
                Log.d(TAG, "Camera bound for streaming")
            } catch (e: Exception) {
                Log.e(TAG, "Error binding camera", e)
            }
        }
    }
    
    private fun processImageForStreaming(imageProxy: ImageProxy) {
        if (!isStreaming.get()) {
            imageProxy.close()
            return
        }
        
        try {
            val jpeg = imageProxyToJpeg(imageProxy)
            currentFrame.set(jpeg)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        } finally {
            imageProxy.close()
        }
    }
    
    private fun imageProxyToJpeg(imageProxy: ImageProxy): ByteArray {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )
        
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 70, out)
        return out.toByteArray()
    }
    
    private fun stopCameraStreaming() {
        isStreaming.set(false)
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageAnalysis = null
    }
    
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return "127.0.0.1"
    }
    
    /**
     * Internal NanoHTTPD web server implementation
     */
    inner class CameraWebServer(port: Int) : NanoHTTPD(port) {
        
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            val method = session.method
            
            Log.d(TAG, "Request: $method $uri")
            
            return when {
                uri == "/" || uri == "/index.html" -> serveHtml()
                uri == "/stream.mjpg" -> serveMjpegStream()
                uri == "/snapshot.jpg" -> serveSnapshot()
                uri.startsWith("/api/") -> handleApiRequest(session)
                uri == "/style.css" -> serveCss()
                uri == "/script.js" -> serveJs()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
            }
        }
        
        private fun serveHtml(): Response {
            val html = getWebInterfaceHtml()
            return newFixedLengthResponse(Response.Status.OK, "text/html", html)
        }
        
        private fun serveCss(): Response {
            val css = getWebInterfaceCss()
            return newFixedLengthResponse(Response.Status.OK, "text/css", css)
        }
        
        private fun serveJs(): Response {
            val js = getWebInterfaceJs()
            return newFixedLengthResponse(Response.Status.OK, "application/javascript", js)
        }
        
        private fun serveSnapshot(): Response {
            val frame = currentFrame.get()
            return if (frame != null) {
                newFixedLengthResponse(
                    Response.Status.OK,
                    "image/jpeg",
                    ByteArrayInputStream(frame),
                    frame.size.toLong()
                )
            } else {
                newFixedLengthResponse(Response.Status.SERVICE_UNAVAILABLE, MIME_PLAINTEXT, "No frame available")
            }
        }
        
        private fun serveMjpegStream(): Response {
            val boundary = "mjpegboundary"
            
            return newChunkedResponse(
                Response.Status.OK,
                "multipart/x-mixed-replace; boundary=$boundary",
                MjpegInputStream(boundary)
            )
        }
        
        private fun handleApiRequest(session: IHTTPSession): Response {
            val uri = session.uri
            val method = session.method
            
            return when {
                uri == "/api/status" && method == Method.GET -> handleGetStatus()
                uri == "/api/settings" && method == Method.GET -> handleGetSettings()
                uri == "/api/settings" && method == Method.POST -> handleUpdateSettings(session)
                uri == "/api/record/start" && method == Method.POST -> handleStartRecording()
                uri == "/api/record/stop" && method == Method.POST -> handleStopRecording()
                uri == "/api/record/pause" && method == Method.POST -> handlePauseRecording()
                uri == "/api/record/resume" && method == Method.POST -> handleResumeRecording()
                uri == "/api/camera/switch" && method == Method.POST -> handleSwitchCamera()
                uri == "/api/flash/toggle" && method == Method.POST -> handleToggleFlash()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "API not found")
            }
        }
        
        private fun handleGetStatus(): Response {
            val json = JSONObject().apply {
                put("isRecording", videoRecordingRepository.getCurrentState() is RecordingState.Recording)
                put("isPaused", videoRecordingRepository.getCurrentState() is RecordingState.Paused)
                put("recordingState", videoRecordingRepository.getCurrentState().toString())
            }
            return jsonResponse(json)
        }
        
        private fun handleGetSettings(): Response {
            var settings: VideoSettings? = null
            lifecycleScope.launch {
                settings = settingsDataStore.getSettings()
            }
            
            // Wait a bit for coroutine
            Thread.sleep(100)
            
            val json = JSONObject().apply {
                settings?.let { s ->
                    put("cameraFacing", s.cameraFacing.name)
                    put("resolution", s.resolution.name)
                    put("frameRate", s.frameRate)
                    put("bitrate", s.bitrate.name)
                    put("flashEnabled", s.flashEnabled)
                    put("audioSource", s.audioSource.name)
                }
            }
            return jsonResponse(json)
        }
        
        private fun handleUpdateSettings(session: IHTTPSession): Response {
            try {
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
                val body = ByteArray(contentLength)
                session.inputStream.read(body)
                val jsonStr = String(body)
                val json = JSONObject(jsonStr)
                
                lifecycleScope.launch {
                    if (json.has("cameraFacing")) {
                        val facing = CameraFacing.valueOf(json.getString("cameraFacing"))
                        settingsDataStore.setCameraFacing(facing)
                        // Rebind camera with new setting
                        withContext(Dispatchers.Main) {
                            bindCameraForStreaming()
                        }
                    }
                    if (json.has("flashEnabled")) {
                        settingsDataStore.setFlashEnabled(json.getBoolean("flashEnabled"))
                    }
                }
                
                return jsonResponse(JSONObject().put("success", true))
            } catch (e: Exception) {
                Log.e(TAG, "Error updating settings", e)
                return jsonResponse(JSONObject().put("success", false).put("error", e.message))
            }
        }
        
        private fun handleStartRecording(): Response {
            val intent = VideoRecordingService.getIntent(
                this@WebServerService,
                VideoRecordingService.ACTION_START_RECORDING
            )
            ContextCompat.startForegroundService(this@WebServerService, intent)
            return jsonResponse(JSONObject().put("success", true).put("action", "start"))
        }
        
        private fun handleStopRecording(): Response {
            val intent = VideoRecordingService.getIntent(
                this@WebServerService,
                VideoRecordingService.ACTION_STOP_RECORDING
            )
            startService(intent)
            return jsonResponse(JSONObject().put("success", true).put("action", "stop"))
        }
        
        private fun handlePauseRecording(): Response {
            val intent = VideoRecordingService.getIntent(
                this@WebServerService,
                VideoRecordingService.ACTION_PAUSE_RECORDING
            )
            startService(intent)
            return jsonResponse(JSONObject().put("success", true).put("action", "pause"))
        }
        
        private fun handleResumeRecording(): Response {
            val intent = VideoRecordingService.getIntent(
                this@WebServerService,
                VideoRecordingService.ACTION_RESUME_RECORDING
            )
            startService(intent)
            return jsonResponse(JSONObject().put("success", true).put("action", "resume"))
        }
        
        private fun handleSwitchCamera(): Response {
            lifecycleScope.launch {
                val settings = settingsDataStore.getSettings()
                val newFacing = if (settings.cameraFacing == CameraFacing.FRONT) {
                    CameraFacing.BACK
                } else {
                    CameraFacing.FRONT
                }
                settingsDataStore.setCameraFacing(newFacing)
                
                withContext(Dispatchers.Main) {
                    bindCameraForStreaming()
                }
            }
            return jsonResponse(JSONObject().put("success", true).put("action", "switch_camera"))
        }
        
        private fun handleToggleFlash(): Response {
            lifecycleScope.launch {
                val settings = settingsDataStore.getSettings()
                settingsDataStore.setFlashEnabled(!settings.flashEnabled)
            }
            return jsonResponse(JSONObject().put("success", true).put("action", "toggle_flash"))
        }
        
        private fun jsonResponse(json: JSONObject): Response {
            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json.toString()
            ).apply {
                addHeader("Access-Control-Allow-Origin", "*")
            }
        }
    }
    
    /**
     * MJPEG Stream InputStream
     */
    inner class MjpegInputStream(private val boundary: String) : java.io.InputStream() {
        private var currentData: ByteArray? = null
        private var currentIndex = 0
        private var lastFrameTime = 0L
        private val frameInterval = 33L // ~30 fps
        
        override fun read(): Int {
            val data = getNextData()
            if (data == null || currentIndex >= data.size) {
                return -1
            }
            return data[currentIndex++].toInt() and 0xFF
        }
        
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val data = getNextData() ?: return -1
            
            if (currentIndex >= data.size) {
                // Wait for next frame
                currentData = null
                currentIndex = 0
                return 0
            }
            
            val available = data.size - currentIndex
            val toRead = minOf(len, available)
            System.arraycopy(data, currentIndex, b, off, toRead)
            currentIndex += toRead
            
            return toRead
        }
        
        private fun getNextData(): ByteArray? {
            if (currentData == null || currentIndex >= currentData!!.size) {
                // Rate limit
                val now = System.currentTimeMillis()
                val elapsed = now - lastFrameTime
                if (elapsed < frameInterval) {
                    Thread.sleep(frameInterval - elapsed)
                }
                lastFrameTime = System.currentTimeMillis()
                
                val frame = currentFrame.get() ?: return null
                
                val header = "--$boundary\r\nContent-Type: image/jpeg\r\nContent-Length: ${frame.size}\r\n\r\n"
                val footer = "\r\n"
                
                currentData = header.toByteArray() + frame + footer.toByteArray()
                currentIndex = 0
            }
            return currentData
        }
    }
    
    // Web interface HTML
    private fun getWebInterfaceHtml(): String = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HiddenCam - Web Control</title>
    <link rel="stylesheet" href="/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>📹 HiddenCam</h1>
            <span id="status" class="status">Connecting...</span>
        </header>
        
        <div class="preview-container">
            <img id="stream" src="/stream.mjpg" alt="Camera Stream">
            <div id="recording-indicator" class="recording-indicator hidden">
                <span class="dot"></span> REC
            </div>
        </div>
        
        <div class="controls">
            <button id="btn-record" class="btn btn-record" onclick="startRecording()">
                ⏺ Start Recording
            </button>
            <button id="btn-stop" class="btn btn-stop hidden" onclick="stopRecording()">
                ⏹ Stop
            </button>
            <button id="btn-pause" class="btn btn-pause hidden" onclick="pauseRecording()">
                ⏸ Pause
            </button>
            <button id="btn-resume" class="btn btn-resume hidden" onclick="resumeRecording()">
                ▶ Resume
            </button>
        </div>
        
        <div class="quick-actions">
            <button class="btn btn-secondary" onclick="switchCamera()">
                🔄 Switch Camera
            </button>
            <button id="btn-flash" class="btn btn-secondary" onclick="toggleFlash()">
                💡 Flash: OFF
            </button>
            <button class="btn btn-secondary" onclick="takeSnapshot()">
                📷 Snapshot
            </button>
        </div>
        
        <div class="settings-panel">
            <h3>⚙️ Settings</h3>
            <div class="setting-item">
                <label>Camera</label>
                <select id="camera-select" onchange="updateCamera()">
                    <option value="BACK">Back Camera</option>
                    <option value="FRONT">Front Camera</option>
                </select>
            </div>
            <div class="setting-item">
                <label>Resolution</label>
                <select id="resolution-select" onchange="updateResolution()">
                    <option value="SD_480P">480p (SD)</option>
                    <option value="HD_720P">720p (HD)</option>
                    <option value="FHD_1080P">1080p (Full HD)</option>
                    <option value="UHD_4K">4K (UHD)</option>
                </select>
            </div>
        </div>
        
        <footer>
            <p>HiddenCam Web Interface v2.0</p>
            <p id="connection-info"></p>
        </footer>
    </div>
    <script src="/script.js"></script>
</body>
</html>
""".trimIndent()

    private fun getWebInterfaceCss(): String = """
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    min-height: 100vh;
    color: #fff;
}

.container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid rgba(255,255,255,0.1);
}

header h1 {
    font-size: 1.5rem;
}

.status {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 0.8rem;
    background: rgba(255,255,255,0.1);
}

.status.connected {
    background: #2ecc71;
}

.status.recording {
    background: #e74c3c;
    animation: pulse 1s infinite;
}

@keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
}

.preview-container {
    position: relative;
    margin: 20px 0;
    border-radius: 12px;
    overflow: hidden;
    background: #000;
}

#stream {
    width: 100%;
    height: auto;
    display: block;
}

.recording-indicator {
    position: absolute;
    top: 15px;
    left: 15px;
    background: rgba(231, 76, 60, 0.9);
    padding: 8px 15px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: bold;
}

.recording-indicator .dot {
    width: 10px;
    height: 10px;
    background: #fff;
    border-radius: 50%;
    animation: blink 1s infinite;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.3; }
}

.hidden {
    display: none !important;
}

.controls {
    display: flex;
    gap: 10px;
    justify-content: center;
    margin: 20px 0;
}

.btn {
    padding: 15px 30px;
    border: none;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 8px;
}

.btn-record {
    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
    color: white;
    flex: 1;
    justify-content: center;
}

.btn-record:hover {
    transform: scale(1.02);
    box-shadow: 0 5px 20px rgba(231, 76, 60, 0.4);
}

.btn-stop {
    background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
    color: white;
}

.btn-pause {
    background: linear-gradient(135deg, #f39c12 0%, #d68910 100%);
    color: white;
}

.btn-resume {
    background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
    color: white;
}

.btn-secondary {
    background: rgba(255,255,255,0.1);
    color: white;
    border: 1px solid rgba(255,255,255,0.2);
}

.btn-secondary:hover {
    background: rgba(255,255,255,0.2);
}

.quick-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
    margin: 15px 0;
}

.quick-actions .btn {
    flex: 1;
    min-width: 100px;
    justify-content: center;
}

.settings-panel {
    background: rgba(255,255,255,0.05);
    border-radius: 12px;
    padding: 20px;
    margin-top: 20px;
}

.settings-panel h3 {
    margin-bottom: 15px;
    font-size: 1.1rem;
}

.setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid rgba(255,255,255,0.1);
}

.setting-item:last-child {
    border-bottom: none;
}

.setting-item label {
    font-weight: 500;
}

.setting-item select {
    padding: 8px 15px;
    border-radius: 8px;
    border: 1px solid rgba(255,255,255,0.2);
    background: rgba(255,255,255,0.1);
    color: white;
    font-size: 0.9rem;
}

footer {
    text-align: center;
    padding: 20px;
    margin-top: 30px;
    opacity: 0.6;
    font-size: 0.8rem;
}

@media (max-width: 600px) {
    .controls {
        flex-direction: column;
    }
    
    .quick-actions {
        flex-direction: column;
    }
    
    .quick-actions .btn {
        width: 100%;
    }
}
""".trimIndent()

    private fun getWebInterfaceJs(): String = """
let isRecording = false;
let isPaused = false;
let flashEnabled = false;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    updateStatus();
    loadSettings();
    setInterval(updateStatus, 2000);
});

async function updateStatus() {
    try {
        const response = await fetch('/api/status');
        const data = await response.json();
        
        isRecording = data.isRecording;
        isPaused = data.isPaused;
        
        updateUI();
        
        document.getElementById('status').textContent = 
            isRecording ? (isPaused ? 'Paused' : 'Recording') : 'Connected';
        document.getElementById('status').className = 
            'status ' + (isRecording ? 'recording' : 'connected');
            
    } catch (error) {
        document.getElementById('status').textContent = 'Disconnected';
        document.getElementById('status').className = 'status';
    }
}

async function loadSettings() {
    try {
        const response = await fetch('/api/settings');
        const data = await response.json();
        
        document.getElementById('camera-select').value = data.cameraFacing || 'BACK';
        document.getElementById('resolution-select').value = data.resolution || 'HD_720P';
        flashEnabled = data.flashEnabled || false;
        updateFlashButton();
    } catch (error) {
        console.error('Failed to load settings', error);
    }
}

function updateUI() {
    const btnRecord = document.getElementById('btn-record');
    const btnStop = document.getElementById('btn-stop');
    const btnPause = document.getElementById('btn-pause');
    const btnResume = document.getElementById('btn-resume');
    const indicator = document.getElementById('recording-indicator');
    
    if (isRecording) {
        btnRecord.classList.add('hidden');
        btnStop.classList.remove('hidden');
        indicator.classList.remove('hidden');
        
        if (isPaused) {
            btnPause.classList.add('hidden');
            btnResume.classList.remove('hidden');
        } else {
            btnPause.classList.remove('hidden');
            btnResume.classList.add('hidden');
        }
    } else {
        btnRecord.classList.remove('hidden');
        btnStop.classList.add('hidden');
        btnPause.classList.add('hidden');
        btnResume.classList.add('hidden');
        indicator.classList.add('hidden');
    }
}

async function startRecording() {
    await fetch('/api/record/start', { method: 'POST' });
    setTimeout(updateStatus, 500);
}

async function stopRecording() {
    await fetch('/api/record/stop', { method: 'POST' });
    setTimeout(updateStatus, 500);
}

async function pauseRecording() {
    await fetch('/api/record/pause', { method: 'POST' });
    setTimeout(updateStatus, 500);
}

async function resumeRecording() {
    await fetch('/api/record/resume', { method: 'POST' });
    setTimeout(updateStatus, 500);
}

async function switchCamera() {
    await fetch('/api/camera/switch', { method: 'POST' });
    // Reload stream after a short delay
    setTimeout(() => {
        document.getElementById('stream').src = '/stream.mjpg?' + Date.now();
        loadSettings();
    }, 1000);
}

async function toggleFlash() {
    await fetch('/api/flash/toggle', { method: 'POST' });
    flashEnabled = !flashEnabled;
    updateFlashButton();
}

function updateFlashButton() {
    document.getElementById('btn-flash').textContent = 
        flashEnabled ? '💡 Flash: ON' : '💡 Flash: OFF';
}

async function updateCamera() {
    const camera = document.getElementById('camera-select').value;
    await fetch('/api/settings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cameraFacing: camera })
    });
    setTimeout(() => {
        document.getElementById('stream').src = '/stream.mjpg?' + Date.now();
    }, 1000);
}

async function updateResolution() {
    const resolution = document.getElementById('resolution-select').value;
    await fetch('/api/settings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resolution: resolution })
    });
}

function takeSnapshot() {
    const link = document.createElement('a');
    link.href = '/snapshot.jpg';
    link.download = 'snapshot_' + Date.now() + '.jpg';
    link.click();
}
""".trimIndent()
}
