package com.example.hiddencam.presentation.screens.settings

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Iso
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShutterSpeed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hiddencam.data.datastore.SecurityDataStore
import com.example.hiddencam.domain.model.AppIcon
import com.example.hiddencam.domain.model.AppName
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.FocusMode
import com.example.hiddencam.domain.model.IsoMode
import com.example.hiddencam.domain.model.RecordingMode
import com.example.hiddencam.domain.model.ShutterSpeedMode
import com.example.hiddencam.domain.model.ShutterSpeedValues
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: VideoSettings,
    onNavigateBack: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    securityDataStore: SecurityDataStore
) {
    val currentSettings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Security settings state
    val appLockEnabled by securityDataStore.appLockEnabled.collectAsState(initial = false)
    val biometricEnabled by securityDataStore.biometricEnabled.collectAsState(initial = false)
    val lockTimeout by securityDataStore.lockTimeout.collectAsState(initial = 0)
    val encryptVideo by securityDataStore.encryptVideo.collectAsState(initial = false)
    val bluetoothRemoteEnabled by securityDataStore.bluetoothRemoteEnabled.collectAsState(initial = false)
    val pairedDeviceName by securityDataStore.pairedDeviceName.collectAsState(initial = null)
    
    // Bluetooth pairing dialog state
    var showBluetoothPairingDialog by remember { mutableStateOf(false) }
    
    // Bluetooth permission launcher
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            showBluetoothPairingDialog = true
        } else {
            Toast.makeText(context, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Video Settings Section
            SettingsSectionHeader(title = "Video Settings")
            
            SettingsCard {
                // Camera Selection
                CameraFacingSelector(
                    currentFacing = currentSettings.cameraFacing,
                    onFacingSelected = { viewModel.setCameraFacing(it) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Resolution
                DropdownSettingItem(
                    icon = Icons.Default.Videocam,
                    title = "Resolution",
                    currentValue = currentSettings.resolution.displayName,
                    options = VideoResolution.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        VideoResolution.entries.find { it.displayName == selected }?.let {
                            viewModel.setResolution(it)
                        }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Frame Rate
                DropdownSettingItem(
                    icon = Icons.Default.Speed,
                    title = "Frame Rate (FPS)",
                    currentValue = "${currentSettings.frameRate} fps",
                    options = listOf("24 fps", "30 fps", "60 fps"),
                    onOptionSelected = { selected ->
                        val fps = selected.replace(" fps", "").toIntOrNull() ?: 30
                        viewModel.setFrameRate(fps)
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Bitrate
                DropdownSettingItem(
                    icon = Icons.Default.HighQuality,
                    title = "Video Quality (Bitrate)",
                    currentValue = currentSettings.bitrate.displayName,
                    options = VideoBitrate.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        VideoBitrate.entries.find { it.displayName == selected }?.let {
                            viewModel.setBitrate(it)
                        }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Orientation
                DropdownSettingItem(
                    icon = Icons.Default.ScreenRotation,
                    title = "Video Orientation",
                    currentValue = currentSettings.orientation.displayName,
                    options = VideoOrientation.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        VideoOrientation.entries.find { it.displayName == selected }?.let {
                            viewModel.setOrientation(it)
                        }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Flash
                SwitchSettingItem(
                    icon = Icons.Default.FlashOn,
                    title = "Flash Light",
                    description = "Enable flash/torch while recording (back camera only)",
                    isChecked = currentSettings.flashEnabled,
                    onCheckedChange = { viewModel.setFlashEnabled(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Audio Settings Section
            SettingsSectionHeader(title = "Audio Settings")
            
            SettingsCard {
                DropdownSettingItem(
                    icon = Icons.Default.AudioFile,
                    title = "Audio Source",
                    currentValue = currentSettings.audioSource.displayName,
                    options = AudioSource.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        AudioSource.entries.find { it.displayName == selected }?.let {
                            viewModel.setAudioSource(it)
                        }
                    }
                )
                
                Text(
                    text = "💡 Bluetooth audio requires a paired Bluetooth headset. Mixed mode records from both phone mic and Bluetooth.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recording Mode Section
            SettingsSectionHeader(title = "Recording Mode")
            
            SettingsCard {
                DropdownSettingItem(
                    icon = Icons.Default.Loop,
                    title = "Recording Mode",
                    currentValue = currentSettings.recordingMode.displayName,
                    options = RecordingMode.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        RecordingMode.entries.find { it.displayName == selected }?.let {
                            viewModel.setRecordingMode(it)
                        }
                    }
                )
                
                if (currentSettings.recordingMode == RecordingMode.LOOP) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Loop Recording Minimum Free Storage
                    val minFreeGBOptions = listOf(1, 2, 5, 10)
                    DropdownSettingItem(
                        icon = Icons.Default.Storage,
                        title = "Minimum Free Storage",
                        currentValue = "${currentSettings.loopRecordingMinFreeGB} GB",
                        options = minFreeGBOptions.map { "$it GB" },
                        onOptionSelected = { selected ->
                            val gb = selected.replace(" GB", "").toIntOrNull() ?: 2
                            viewModel.setLoopRecordingMinFreeGB(gb)
                        }
                    )
                    
                    Text(
                        text = "⚠️ Loop recording will automatically delete oldest recordings when storage falls below this threshold.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Text(
                    text = when (currentSettings.recordingMode) {
                        RecordingMode.MANUAL -> "📝 Manual: Recording stops only when you stop it."
                        RecordingMode.UNTIL_FULL -> "💾 Until Full: Recording continues until storage is full, then stops."
                        RecordingMode.LOOP -> "🔄 Loop: Oldest recordings are deleted automatically to free up space."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Controls Section
            SettingsSectionHeader(title = "Quick Controls")
            
            SettingsCard {
                SwitchSettingItem(
                    icon = Icons.AutoMirrored.Filled.VolumeDown,
                    title = "Volume Button Control",
                    description = "Double press volume down to toggle recording",
                    isChecked = currentSettings.volumeButtonEnabled,
                    onCheckedChange = { viewModel.setVolumeButtonEnabled(it) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                SwitchSettingItem(
                    icon = Icons.Default.Power,
                    title = "Power Button Control",
                    description = "Double tap power button to stop recording",
                    isChecked = currentSettings.powerButtonEnabled,
                    onCheckedChange = { viewModel.setPowerButtonEnabled(it) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                SwitchSettingItem(
                    icon = Icons.Default.Vibration,
                    title = "Vibration Feedback",
                    description = "Vibrate when recording starts/stops via quick controls",
                    isChecked = currentSettings.vibrationFeedbackEnabled,
                    onCheckedChange = { viewModel.setVibrationFeedbackEnabled(it) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Screen-off control requires accessibility service
                ScreenOffControlItem(context = context)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Disguise Section
            SettingsSectionHeader(title = "App Disguise")
            
            SettingsCard {
                // App Icon
                DropdownSettingItem(
                    icon = Icons.Default.Apps,
                    title = "App Icon",
                    currentValue = currentSettings.appIcon.displayName,
                    options = AppIcon.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        AppIcon.entries.find { it.displayName == selected }?.let {
                            viewModel.setAppIcon(it)
                        }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // App Name
                DropdownSettingItem(
                    icon = Icons.AutoMirrored.Filled.Label,
                    title = "App Name",
                    currentValue = currentSettings.appName.displayName,
                    options = AppName.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        AppName.entries.find { it.displayName == selected }?.let {
                            viewModel.setAppName(it)
                        }
                    }
                )
                
                Text(
                    text = "⚠️ Note: After changing the icon, the app may take a few seconds to update on your home screen. You may need to restart the launcher.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Advanced Camera Settings Section
            SettingsSectionHeader(title = "Advanced Camera Settings")
            
            SettingsCard {
                // ISO Mode
                DropdownSettingItem(
                    icon = Icons.Default.Iso,
                    title = "ISO",
                    currentValue = currentSettings.isoMode.displayName,
                    options = IsoMode.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        IsoMode.entries.find { it.displayName == selected }?.let {
                            viewModel.setIsoMode(it)
                        }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Exposure Compensation
                ExposureCompensationItem(
                    currentValue = currentSettings.exposureCompensation,
                    onValueChange = { viewModel.setExposureCompensation(it) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Shutter Speed Mode
                DropdownSettingItem(
                    icon = Icons.Default.ShutterSpeed,
                    title = "Shutter Speed",
                    currentValue = currentSettings.shutterSpeedMode.displayName,
                    options = ShutterSpeedMode.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        ShutterSpeedMode.entries.find { it.displayName == selected }?.let {
                            viewModel.setShutterSpeedMode(it)
                        }
                    }
                )
                
                // Custom Shutter Speed (only shown when mode is CUSTOM)
                if (currentSettings.shutterSpeedMode == ShutterSpeedMode.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val availableSpeeds = viewModel.getAvailableShutterSpeeds()
                    val currentSpeedDisplay = ShutterSpeedValues.getDisplayName(currentSettings.customShutterSpeed)
                    
                    DropdownSettingItem(
                        icon = Icons.Default.ShutterSpeed,
                        title = "Custom Speed",
                        currentValue = currentSpeedDisplay,
                        options = availableSpeeds.map { it.second },
                        onOptionSelected = { selected ->
                            availableSpeeds.find { it.second == selected }?.let { (speedNs, _) ->
                                viewModel.setCustomShutterSpeed(speedNs)
                            }
                        }
                    )
                    
                    Text(
                        text = "⚠️ Minimum shutter speed for ${currentSettings.frameRate}fps: 1/${currentSettings.frameRate}s",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp, start = 40.dp)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Focus Mode
                DropdownSettingItem(
                    icon = Icons.Default.CenterFocusStrong,
                    title = "Focus Mode",
                    currentValue = currentSettings.focusMode.displayName,
                    options = FocusMode.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        FocusMode.entries.find { it.displayName == selected }?.let {
                            viewModel.setFocusMode(it)
                        }
                    }
                )
                
                Text(
                    text = "💡 Tip: Use 'Continuous Video' for general recording, 'Face Detection' for vlogs, or 'Infinity' for landscapes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Security Section
            SettingsSectionHeader(title = "Security")
            
            SettingsCard {
                // App Lock Toggle
                SwitchSettingItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    description = if (appLockEnabled) "PIN protection enabled" else "Protect app with PIN",
                    isChecked = appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Navigate to PIN setup
                            onNavigateToPinSetup()
                        } else {
                            // Disable app lock
                            scope.launch {
                                securityDataStore.setAppLockEnabled(false)
                            }
                        }
                    }
                )
                
                if (appLockEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Change PIN
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPinSetup() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Change PIN",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Set a new 4-digit PIN",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Biometric Toggle
                    SwitchSettingItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Unlock",
                        description = "Use fingerprint or face to unlock",
                        isChecked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                securityDataStore.setBiometricEnabled(enabled)
                            }
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Lock Timeout
                    val lockTimeoutOptions = listOf(
                        0 to "Immediately",
                        1 to "After 1 minute",
                        2 to "After 2 minutes",
                        5 to "After 5 minutes",
                        10 to "After 10 minutes"
                    )
                    val currentTimeoutLabel = lockTimeoutOptions.find { it.first == lockTimeout }?.second ?: "Immediately"
                    
                    DropdownSettingItem(
                        icon = Icons.Default.Lock,
                        title = "Lock After",
                        currentValue = currentTimeoutLabel,
                        options = lockTimeoutOptions.map { it.second },
                        onOptionSelected = { selected ->
                            val timeout = lockTimeoutOptions.find { it.second == selected }?.first ?: 0
                            scope.launch {
                                securityDataStore.setLockTimeout(timeout)
                            }
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Video Encryption
                    SwitchSettingItem(
                        icon = Icons.Default.Security,
                        title = "Encrypt Videos",
                        description = "Encrypt recordings with your PIN",
                        isChecked = encryptVideo,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                securityDataStore.setEncryptVideo(enabled)
                            }
                        }
                    )
                    
                    if (encryptVideo) {
                        Text(
                            text = "🔐 Videos will be encrypted using your app lock PIN. You'll need to decrypt them in the Video Gallery to view.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp, start = 40.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bluetooth Remote Section
            SettingsSectionHeader(title = "Bluetooth Remote")
            
            SettingsCard {
                SwitchSettingItem(
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth Remote",
                    description = "Control recording with Bluetooth remote",
                    isChecked = bluetoothRemoteEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            securityDataStore.setBluetoothRemoteEnabled(enabled)
                        }
                    }
                )
                
                if (bluetoothRemoteEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Paired Device Info / Pairing Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Check Bluetooth permissions
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_CONNECT,
                                        Manifest.permission.BLUETOOTH_SCAN
                                    )
                                } else {
                                    arrayOf(Manifest.permission.BLUETOOTH)
                                }
                                
                                val allGranted = permissions.all {
                                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                                }
                                
                                if (allGranted) {
                                    showBluetoothPairingDialog = true
                                } else {
                                    bluetoothPermissionLauncher.launch(permissions)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (pairedDeviceName != null) 
                                Icons.Default.BluetoothConnected 
                            else 
                                Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (pairedDeviceName != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (pairedDeviceName != null) "Paired Device" else "Pair Remote",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = pairedDeviceName ?: "Tap to select a Bluetooth remote",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        if (pairedDeviceName != null) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        securityDataStore.clearPairedDevice()
                                    }
                                }
                            ) {
                                Text("Unpair")
                            }
                        }
                    }
                    
                    Text(
                        text = "⚠️ Note: Bluetooth remote works best when app is in foreground. Some remotes may not work when screen is locked.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Card
            InfoCard()
        }
    }
    
    // Bluetooth Pairing Dialog
    if (showBluetoothPairingDialog) {
        BluetoothPairingDialog(
            context = context,
            onDismiss = { showBluetoothPairingDialog = false },
            onDeviceSelected = { name, address ->
                scope.launch {
                    securityDataStore.setPairedDevice(name, address)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun CameraFacingSelector(
    currentFacing: CameraFacing,
    onFacingSelected: (CameraFacing) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (currentFacing) {
                CameraFacing.FRONT -> Icons.Default.CameraFront
                CameraFacing.BACK -> Icons.Default.CameraRear
                CameraFacing.USB -> Icons.Default.Usb
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Camera",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Row {
            CameraFacing.entries.forEach { facing ->
                val isSelected = facing == currentFacing
                Card(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .clickable { onFacingSelected(facing) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = when (facing) {
                            CameraFacing.FRONT -> "Front"
                            CameraFacing.BACK -> "Back"
                            CameraFacing.USB -> "USB"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingItem(
    icon: ImageVector,
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ExposureCompensationItem(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Exposure,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Exposure Compensation",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = "Adjust brightness: ${if (currentValue >= 0) "+$currentValue" else currentValue} EV",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minus button
                Card(
                    modifier = Modifier.clickable { 
                        if (currentValue > -4) onValueChange(currentValue - 1)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentValue > -4) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "−",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Exposure value indicators
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (-4..4).forEach { value ->
                        val isSelected = value == currentValue
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 1.dp)
                                .clickable { onValueChange(value) },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    value == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Text(
                                text = if (value == 0) "0" else "",
                                modifier = Modifier.padding(vertical = 6.dp),
                                fontSize = 10.sp,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Plus button
                Card(
                    modifier = Modifier.clickable { 
                        if (currentValue < 4) onValueChange(currentValue + 1)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentValue < 4) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "+",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ℹ️ Information",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Videos are saved to Movies/HiddenCam folder\n" +
                       "• Recording continues in background and when screen is locked\n" +
                       "• Higher resolution and bitrate will use more storage\n" +
                       "• Make sure to have enough storage space before recording",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BluetoothPairingDialog(
    context: Context,
    onDismiss: () -> Unit,
    onDeviceSelected: (String, String) -> Unit
) {
    val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    
    val bondedDevices = remember(hasBluetoothPermission) {
        if (!hasBluetoothPermission) return@remember emptyList<BluetoothDevice>()
        
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Bluetooth Remote") },
        text = {
            Column {
                if (bondedDevices.isEmpty()) {
                    Text(
                        text = "No paired Bluetooth devices found.\n\nPlease pair your Bluetooth remote in system settings first.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Select a paired device:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    bondedDevices.forEach { device ->
                        val deviceName = try {
                            device.name ?: "Unknown Device"
                        } catch (e: SecurityException) {
                            "Unknown Device"
                        }
                        
                        val deviceAddress = try {
                            device.address ?: ""
                        } catch (e: SecurityException) {
                            ""
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onDeviceSelected(deviceName, deviceAddress)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = deviceName,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = deviceAddress,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Screen-off control item that links to Accessibility Settings.
 * User needs to enable the accessibility service for volume button control when screen is off.
 */
@Composable
private fun ScreenOffControlItem(context: Context) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Check if accessibility service is enabled
    val isAccessibilityEnabled = remember {
        isAccessibilityServiceEnabled(
            context,
            "com.example.hiddencam/.data.service.VolumeKeyAccessibilityService"
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ScreenLockPortrait,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Screen-Off Control",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isAccessibilityEnabled) {
                    "✓ Enabled - Double press volume down when screen off"
                } else {
                    "Tap to enable in Accessibility Settings"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isAccessibilityEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
        
        Icon(
            imageVector = Icons.Default.AccessibilityNew,
            contentDescription = null,
            tint = if (isAccessibilityEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            }
        )
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enable Screen-Off Control") },
            text = {
                Column {
                    Text(
                        text = "To use volume button control when the screen is off, you need to enable the HiddenCam Volume Control accessibility service.",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "How to enable:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "1. Tap 'Open Settings' below\n" +
                               "2. Find 'HiddenCam Volume Control'\n" +
                               "3. Turn it ON\n" +
                               "4. Confirm when prompted",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "\n⚠️ This service only listens for volume button presses and does not access any other data.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        // Open accessibility settings
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Check if accessibility service is enabled
 */
private fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    
    return enabledServices.contains(serviceName) ||
           enabledServices.contains("VolumeKeyAccessibilityService")
}
