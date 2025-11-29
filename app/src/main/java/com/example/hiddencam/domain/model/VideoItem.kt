package com.example.hiddencam.domain.model

import android.net.Uri

/**
 * Represents a recorded video item
 */
data class VideoItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val duration: Long, // in milliseconds
    val size: Long, // in bytes
    val dateAdded: Long, // timestamp
    val thumbnailUri: Uri? = null
) {
    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    
    val formattedSize: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            
            return when {
                gb >= 1 -> String.format("%.2f GB", gb)
                mb >= 1 -> String.format("%.2f MB", mb)
                else -> String.format("%.2f KB", kb)
            }
        }
    
    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(dateAdded * 1000))
        }
}
