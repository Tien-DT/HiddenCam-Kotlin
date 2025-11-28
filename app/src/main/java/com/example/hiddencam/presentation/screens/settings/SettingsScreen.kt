package com.example.hiddencam.presentation.screens.settings

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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hiddencam.domain.model.AppIcon
import com.example.hiddencam.domain.model.AppName
import com.example.hiddencam.domain.model.AudioSource
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.domain.model.VideoBitrate
import com.example.hiddencam.domain.model.VideoOrientation
import com.example.hiddencam.domain.model.VideoResolution
import com.example.hiddencam.domain.model.VideoSettings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: VideoSettings,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentSettings by viewModel.settings.collectAsState()
    
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
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quick Controls Section
            SettingsSectionHeader(title = "Quick Controls")
            
            SettingsCard {
                SwitchSettingItem(
                    icon = Icons.AutoMirrored.Filled.VolumeDown,
                    title = "Volume Button Control",
                    description = "Long press volume down to start recording",
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
            
            // Info Card
            InfoCard()
        }
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
            imageVector = if (currentFacing == CameraFacing.FRONT) 
                Icons.Default.CameraFront else Icons.Default.CameraRear,
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
                        .padding(horizontal = 4.dp)
                        .clickable { onFacingSelected(facing) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = facing.name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
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
