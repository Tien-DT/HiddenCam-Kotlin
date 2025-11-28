package com.example.hiddencam.domain.model

/**
 * Represents the current state of video recording
 */
sealed class RecordingState {
    object Idle : RecordingState()
    object Starting : RecordingState()
    data class Recording(val durationMs: Long = 0L) : RecordingState()
    data class Paused(val durationMs: Long = 0L) : RecordingState()
    object Stopping : RecordingState()
    data class Error(val message: String) : RecordingState()
}
