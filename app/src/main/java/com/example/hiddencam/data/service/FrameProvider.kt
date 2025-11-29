package com.example.hiddencam.data.service

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Singleton that provides camera frames for MJPEG streaming.
 * This allows WebServerService to get frames from VideoRecordingService when recording,
 * or from its own camera binding when not recording.
 */
object FrameProvider {
    private const val TAG = "FrameProvider"
    
    // Current JPEG frame
    private val currentFrame = AtomicReference<ByteArray?>(null)
    
    // Whether VideoRecordingService is providing frames
    private val isRecordingServiceActive = AtomicBoolean(false)
    
    // Listener for frame updates
    private var frameListener: ((ByteArray) -> Unit)? = null
    
    /**
     * Get the latest frame as JPEG
     */
    fun getLatestFrame(): ByteArray? {
        return currentFrame.get()
    }
    
    /**
     * Check if recording service is actively providing frames
     */
    fun isRecordingActive(): Boolean {
        return isRecordingServiceActive.get()
    }
    
    /**
     * Set frame listener (called by WebServerService)
     */
    fun setFrameListener(listener: ((ByteArray) -> Unit)?) {
        frameListener = listener
    }
    
    /**
     * Update frame from VideoRecordingService
     */
    fun updateFrame(jpeg: ByteArray) {
        currentFrame.set(jpeg)
        frameListener?.invoke(jpeg)
    }
    
    /**
     * Set recording active state
     */
    fun setRecordingActive(active: Boolean) {
        isRecordingServiceActive.set(active)
        if (!active) {
            // Clear frame when recording stops
            currentFrame.set(null)
        }
        Log.d(TAG, "Recording active: $active")
    }
    
    /**
     * Convert ImageProxy to JPEG bytes
     */
    fun imageProxyToJpeg(imageProxy: ImageProxy, quality: Int = 70): ByteArray {
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
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), quality, out)
        return out.toByteArray()
    }
    
    /**
     * Create ImageAnalysis.Analyzer for frame capture
     */
    fun createAnalyzer(): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { imageProxy ->
            try {
                val jpeg = imageProxyToJpeg(imageProxy)
                updateFrame(jpeg)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing frame", e)
            } finally {
                imageProxy.close()
            }
        }
    }
}
