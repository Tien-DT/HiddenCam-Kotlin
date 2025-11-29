package com.example.hiddencam.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.hiddencam.data.datastore.SecurityDataStore
import com.example.hiddencam.data.service.VideoRecordingService
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.domain.repository.SettingsRepository
import com.example.hiddencam.domain.repository.VideoRecordingRepository
import com.example.hiddencam.presentation.navigation.AppNavigation
import com.example.hiddencam.ui.theme.HiddenCamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var videoRecordingRepository: VideoRecordingRepository
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    @Inject
    lateinit var securityDataStore: SecurityDataStore
    
    private var videoRecordingService: VideoRecordingService? = null
    private var isBound = false
    
    private var volumeKeyPressTime = 0L
    private var powerButtonPressCount = 0
    private var lastPowerButtonPressTime = 0L
    
    private var allPermissionsGranted by mutableStateOf(false)
    private var isAppUnlocked by mutableStateOf(false)
    
    private val requiredPermissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        allPermissionsGranted = permissions.all { it.value }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? VideoRecordingService.LocalBinder
            videoRecordingService = binder?.getService()
            isBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            videoRecordingService = null
            isBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()
        
        setContent {
            HiddenCamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val recordingState by videoRecordingRepository.getRecordingStateFlow()
                        .collectAsState(initial = RecordingState.Idle)
                    val settings by settingsRepository.getSettingsFlow()
                        .collectAsState(initial = VideoSettings())
                    
                    AppNavigation(
                        recordingState = recordingState,
                        settings = settings,
                        allPermissionsGranted = allPermissionsGranted,
                        onRequestPermissions = { checkAndRequestPermissions() },
                        securityDataStore = securityDataStore,
                        isAppUnlocked = isAppUnlocked,
                        onAppUnlocked = { isAppUnlocked = true }
                    )
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, VideoRecordingService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        
        // Check if app should be locked based on timeout
        lifecycleScope.launch {
            if (securityDataStore.shouldLockApp()) {
                isAppUnlocked = false
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        
        // Save the time when app goes to background
        lifecycleScope.launch {
            securityDataStore.setLastBackgroundTime(System.currentTimeMillis())
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isEmpty()) {
            allPermissionsGranted = true
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        lifecycleScope.launch {
            val settings = settingsRepository.getSettingsFlow().first()
            
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (settings.volumeButtonEnabled) {
                        handleVolumeDownPress(settings)
                        return@launch
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun handleVolumeDownPress(settings: VideoSettings) {
        val currentTime = System.currentTimeMillis()
        
        // Double press detection (within 500ms)
        if (currentTime - volumeKeyPressTime < 500) {
            volumeKeyPressTime = 0L // Reset to prevent triple press
            
            // Double press detected - toggle recording with vibration
            lifecycleScope.launch {
                val currentState = videoRecordingRepository.getRecordingStateFlow().first()
                
                when (currentState) {
                    is RecordingState.Idle, is RecordingState.Error -> {
                        // Start recording
                        val intent = VideoRecordingService.getIntent(
                            this@MainActivity, 
                            VideoRecordingService.ACTION_TOGGLE_RECORDING_WITH_VIBRATION
                        )
                        ContextCompat.startForegroundService(this@MainActivity, intent)
                    }
                    is RecordingState.Recording, is RecordingState.Paused -> {
                        // Stop recording
                        val intent = VideoRecordingService.getIntent(
                            this@MainActivity, 
                            VideoRecordingService.ACTION_TOGGLE_RECORDING_WITH_VIBRATION
                        )
                        ContextCompat.startForegroundService(this@MainActivity, intent)
                    }
                    else -> { }
                }
            }
        } else {
            volumeKeyPressTime = currentTime
        }
    }
    
    private fun startRecordingService(settings: VideoSettings) {
        val intent = VideoRecordingService.getIntent(this, VideoRecordingService.ACTION_START_RECORDING)
        ContextCompat.startForegroundService(this, intent)
    }
}
