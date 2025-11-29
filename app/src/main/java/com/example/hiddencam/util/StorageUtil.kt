package com.example.hiddencam.util

import android.content.ContentResolver
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import java.io.File

/**
 * Utility class for monitoring storage space and managing loop recording.
 */
object StorageUtil {
    
    private const val GB_IN_BYTES = 1024L * 1024L * 1024L
    private const val MB_IN_BYTES = 1024L * 1024L
    
    /**
     * Gets the available storage space in bytes
     */
    fun getAvailableStorageBytes(): Long {
        return try {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Gets the available storage space in GB
     */
    fun getAvailableStorageGB(): Double {
        return getAvailableStorageBytes().toDouble() / GB_IN_BYTES
    }
    
    /**
     * Gets the total storage space in bytes
     */
    @Suppress("DEPRECATION")
    fun getTotalStorageBytes(): Long {
        return try {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            // Use blockCountLong * blockSizeLong for total bytes
            stat.blockCountLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Gets the total storage space in GB
     */
    fun getTotalStorageGB(): Double {
        return getTotalStorageBytes().toDouble() / GB_IN_BYTES
    }
    
    /**
     * Checks if storage is below the minimum threshold
     * @param minFreeGB Minimum free storage in GB
     * @return True if storage is below threshold
     */
    fun isStorageLow(minFreeGB: Int): Boolean {
        return getAvailableStorageGB() < minFreeGB
    }
    
    /**
     * Checks if there's enough storage for recording
     * @param requiredMB Required space in MB (default 100MB)
     * @return True if there's enough space
     */
    fun hasEnoughStorageForRecording(requiredMB: Long = 100): Boolean {
        return getAvailableStorageBytes() >= requiredMB * MB_IN_BYTES
    }
    
    /**
     * Gets the storage usage percentage
     */
    fun getStorageUsagePercent(): Int {
        val total = getTotalStorageBytes()
        if (total == 0L) return 0
        val used = total - getAvailableStorageBytes()
        return ((used.toDouble() / total) * 100).toInt()
    }
    
    /**
     * Formats bytes to a human-readable string
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= GB_IN_BYTES -> String.format("%.2f GB", bytes.toDouble() / GB_IN_BYTES)
            bytes >= MB_IN_BYTES -> String.format("%.2f MB", bytes.toDouble() / MB_IN_BYTES)
            bytes >= 1024 -> String.format("%.2f KB", bytes.toDouble() / 1024)
            else -> "$bytes bytes"
        }
    }
    
    /**
     * Data class representing a video file with its metadata
     */
    data class VideoFileInfo(
        val id: Long,
        val name: String,
        val path: String,
        val size: Long,
        val dateAdded: Long
    )
    
    /**
     * Gets all HiddenCam recordings sorted by date (oldest first)
     */
    fun getHiddenCamRecordings(contentResolver: ContentResolver): List<VideoFileInfo> {
        val videos = mutableListOf<VideoFileInfo>()
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )
        
        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/HiddenCam/%")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} ASC" // Oldest first
        
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            
            while (cursor.moveToNext()) {
                videos.add(
                    VideoFileInfo(
                        id = cursor.getLong(idColumn),
                        name = cursor.getString(nameColumn),
                        path = cursor.getString(pathColumn),
                        size = cursor.getLong(sizeColumn),
                        dateAdded = cursor.getLong(dateColumn)
                    )
                )
            }
        }
        
        return videos
    }
    
    /**
     * Deletes the oldest HiddenCam recording
     * @return The size of the deleted file in bytes, or 0 if no file was deleted
     */
    fun deleteOldestRecording(contentResolver: ContentResolver): Long {
        val recordings = getHiddenCamRecordings(contentResolver)
        if (recordings.isEmpty()) return 0
        
        val oldest = recordings.first()
        
        return try {
            val deletedRows = contentResolver.delete(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                "${MediaStore.Video.Media._ID} = ?",
                arrayOf(oldest.id.toString())
            )
            
            if (deletedRows > 0) oldest.size else 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * Deletes oldest recordings until minimum free storage is achieved
     * @param contentResolver The content resolver
     * @param minFreeGB Minimum free storage in GB
     * @return The total size of deleted files in bytes
     */
    fun freeUpStorage(contentResolver: ContentResolver, minFreeGB: Int): Long {
        var totalFreed = 0L
        var maxIterations = 50 // Prevent infinite loop
        
        while (isStorageLow(minFreeGB) && maxIterations > 0) {
            val freed = deleteOldestRecording(contentResolver)
            if (freed == 0L) break // No more files to delete
            totalFreed += freed
            maxIterations--
        }
        
        return totalFreed
    }
    
    /**
     * Estimates recording time available based on bitrate and available storage
     * @param bitrateKbps Video bitrate in kbps
     * @return Estimated recording time in seconds
     */
    fun estimateRecordingTimeSeconds(bitrateKbps: Int): Long {
        val availableBytes = getAvailableStorageBytes()
        val bytesPerSecond = (bitrateKbps * 1000L) / 8
        return if (bytesPerSecond > 0) availableBytes / bytesPerSecond else 0
    }
    
    /**
     * Formats recording time to a human-readable string
     */
    fun formatRecordingTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
