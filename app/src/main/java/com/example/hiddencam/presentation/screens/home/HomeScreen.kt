package com.example.hiddencam.presentation.screens.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hiddencam.data.service.VideoRecordingService
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.presentation.util.BatteryOptimizationHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    recordingState: RecordingState,
    settings: VideoSettings,
    allPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPreview: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showBatteryOptimizationWarning by remember { 
        mutableStateOf(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hidden Camera") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Preview button
                    IconButton(onClick = onNavigateToPreview) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = "Camera Preview"
                        )
                    }
                    // Settings button
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Battery Optimization Warning
            if (showBatteryOptimizationWarning && allPermissionsGranted) {
                BatteryOptimizationCard(
                    onRequestOptimization = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                        showBatteryOptimizationWarning = false
                    },
                    onDismiss = { showBatteryOptimizationWarning = false }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Status Card
            StatusCard(
                recordingState = recordingState,
                settings = settings
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recording Duration Display
            RecordingDurationDisplay(recordingState = recordingState)
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Control Buttons
            if (!allPermissionsGranted) {
                PermissionButton(onRequestPermissions = onRequestPermissions)
            } else {
                RecordingControls(
                    recordingState = recordingState,
                    onStartRecording = { 
                        startRecordingService(context, settings)
                        viewModel.startRecording()
                    },
                    onStopRecording = { viewModel.stopRecording() },
                    onPauseRecording = { viewModel.pauseRecording() },
                    onResumeRecording = { viewModel.resumeRecording() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Quick Tips
            QuickTipsCard(settings = settings)
        }
    }
}

@Composable
private fun BatteryOptimizationCard(
    onRequestOptimization: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.BatteryAlert,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Battery Optimization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Disable battery optimization to prevent recording from stopping",
                    fontSize = 12.sp,
                    color = Color(0xFF795548)
                )
            }
            TextButton(onClick = onRequestOptimization) {
                Text("Allow", color = Color(0xFFFF5722))
            }
            TextButton(onClick = onDismiss) {
                Text("Later", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun StatusCard(
    recordingState: RecordingState,
    settings: VideoSettings
) {
    val statusColor by animateColorAsState(
        targetValue = when (recordingState) {
            is RecordingState.Recording -> Color(0xFF4CAF50)
            is RecordingState.Paused -> Color(0xFFFFA000)
            is RecordingState.Error -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when (recordingState) {
                        is RecordingState.Idle -> "Ready to Record"
                        is RecordingState.Starting -> "Starting..."
                        is RecordingState.Recording -> "Recording"
                        is RecordingState.Paused -> "Paused"
                        is RecordingState.Stopping -> "Stopping..."
                        is RecordingState.Error -> "Error"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Camera: ${if (settings.cameraFacing == CameraFacing.FRONT) "Front" else "Back"} | ${settings.resolution.displayName}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun RecordingDurationDisplay(recordingState: RecordingState) {
    val durationMs = when (recordingState) {
        is RecordingState.Recording -> recordingState.durationMs
        is RecordingState.Paused -> recordingState.durationMs
        else -> 0L
    }
    
    val isRecording = recordingState is RecordingState.Recording
    
    // Pulse animation for recording indicator
    var pulseScale by remember { mutableStateOf(1f) }
    
    LaunchedEffect(isRecording) {
        while (isRecording) {
            pulseScale = 1.2f
            delay(500)
            pulseScale = 1f
            delay(500)
        }
        pulseScale = 1f
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = pulseScale,
        animationSpec = tween(500),
        label = "pulseScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRecording || recordingState is RecordingState.Paused) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .scale(animatedScale)
                            .background(Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = formatDuration(durationMs),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RecordingControls(
    recordingState: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit
) {
    val isRecording = recordingState is RecordingState.Recording ||
                      recordingState is RecordingState.Paused
    val isPaused = recordingState is RecordingState.Paused
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            // Pause/Resume Button
            FilledIconButton(
                onClick = if (isPaused) onResumeRecording else onPauseRecording,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFFFA000)
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
            
            // Stop Button
            FilledIconButton(
                onClick = onStopRecording,
                modifier = Modifier.size(80.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Recording",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        } else {
            // Start Recording Button
            FilledIconButton(
                onClick = onStartRecording,
                modifier = Modifier.size(96.dp),
                enabled = recordingState is RecordingState.Idle || recordingState is RecordingState.Error,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Start Recording",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = when {
            recordingState is RecordingState.Recording -> "Tap to pause or stop"
            recordingState is RecordingState.Paused -> "Tap to resume or stop"
            else -> "Tap to start recording"
        },
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        fontSize = 14.sp
    )
}

@Composable
private fun PermissionButton(onRequestPermissions: () -> Unit) {
    Button(
        onClick = onRequestPermissions,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Grant Permissions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "Camera, microphone, and storage permissions are required to record videos.",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        fontSize = 12.sp
    )
}

@Composable
private fun QuickTipsCard(settings: VideoSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Tips",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (settings.volumeButtonEnabled) {
                Text(
                    text = "• Long press Volume Down to start recording",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (settings.powerButtonEnabled) {
                Text(
                    text = "• Double tap Power button to stop recording",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "• Recording continues when screen is locked",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

private fun startRecordingService(context: Context, settings: VideoSettings) {
    val intent = VideoRecordingService.getIntent(context, VideoRecordingService.ACTION_START_RECORDING)
    ContextCompat.startForegroundService(context, intent)
}
