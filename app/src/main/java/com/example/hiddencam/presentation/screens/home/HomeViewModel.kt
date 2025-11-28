package com.example.hiddencam.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.usecase.GetRecordingStateUseCase
import com.example.hiddencam.domain.usecase.PauseRecordingUseCase
import com.example.hiddencam.domain.usecase.ResumeRecordingUseCase
import com.example.hiddencam.domain.usecase.StartRecordingUseCase
import com.example.hiddencam.domain.usecase.StopRecordingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val pauseRecordingUseCase: PauseRecordingUseCase,
    private val resumeRecordingUseCase: ResumeRecordingUseCase,
    getRecordingStateUseCase: GetRecordingStateUseCase
) : ViewModel() {
    
    val recordingState: StateFlow<RecordingState> = getRecordingStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecordingState.Idle
        )
    
    fun startRecording() {
        viewModelScope.launch {
            startRecordingUseCase()
        }
    }
    
    fun stopRecording() {
        viewModelScope.launch {
            stopRecordingUseCase()
        }
    }
    
    fun pauseRecording() {
        viewModelScope.launch {
            pauseRecordingUseCase()
        }
    }
    
    fun resumeRecording() {
        viewModelScope.launch {
            resumeRecordingUseCase()
        }
    }
    
    fun togglePauseResume() {
        viewModelScope.launch {
            when (recordingState.value) {
                is RecordingState.Recording -> pauseRecordingUseCase()
                is RecordingState.Paused -> resumeRecordingUseCase()
                else -> { }
            }
        }
    }
}
