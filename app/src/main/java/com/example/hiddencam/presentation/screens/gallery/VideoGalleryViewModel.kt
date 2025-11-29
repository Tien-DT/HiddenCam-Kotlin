package com.example.hiddencam.presentation.screens.gallery

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiddencam.data.repository.VideoGalleryRepository
import com.example.hiddencam.domain.model.VideoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoGalleryViewModel @Inject constructor(
    private val application: Application,
    private val videoGalleryRepository: VideoGalleryRepository
) : AndroidViewModel(application) {

    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _videos.value = videoGalleryRepository.getRecordedVideos()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Failed to load videos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVideo(videoId: Long) {
        viewModelScope.launch {
            try {
                val success = videoGalleryRepository.deleteVideo(videoId)
                if (success) {
                    _videos.value = _videos.value.filter { it.id != videoId }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "Video deleted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(application, "Failed to delete video", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Error deleting video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
