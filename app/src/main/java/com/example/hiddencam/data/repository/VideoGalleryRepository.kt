package com.example.hiddencam.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.hiddencam.domain.model.VideoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoGalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VideoGalleryRepository"
        private const val HIDDEN_CAM_FOLDER = "HiddenCam"
    }
    
    /**
     * Get all videos recorded by HiddenCam
     */
    suspend fun getRecordedVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.RELATIVE_PATH
        )
        
        // Filter videos from HiddenCam folder
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Video.Media.DATA} LIKE ?"
        }
        
        val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf("%$HIDDEN_CAM_FOLDER%")
        } else {
            val moviesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            arrayOf("%${moviesPath.absolutePath}/$HIDDEN_CAM_FOLDER%")
        }
        
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        
        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    // Get thumbnail URI
                    val thumbnailUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentUri
                    } else {
                        contentUri
                    }
                    
                    videos.add(
                        VideoItem(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            duration = duration,
                            size = size,
                            dateAdded = dateAdded,
                            thumbnailUri = thumbnailUri
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying videos", e)
        }
        
        videos
    }
    
    /**
     * Delete a video by its ID
     */
    suspend fun deleteVideo(videoId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoId
            )
            val deleted = context.contentResolver.delete(uri, null, null)
            deleted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video", e)
            false
        }
    }
    
    /**
     * Get video count
     */
    suspend fun getVideoCount(): Int = withContext(Dispatchers.IO) {
        getRecordedVideos().size
    }
}
