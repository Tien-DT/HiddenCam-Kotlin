package com.example.hiddencam.presentation.screens.preview

import android.Manifest
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hiddencam.domain.model.CameraFacing
import com.example.hiddencam.presentation.screens.settings.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var currentCameraFacing by remember { mutableStateOf(settings.cameraFacing) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Request camera permission if not granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Initialize camera provider
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }
    
    // Update camera facing when settings change
    LaunchedEffect(settings.cameraFacing) {
        currentCameraFacing = settings.cameraFacing
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Preview") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Camera Preview
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    cameraProvider = cameraProvider,
                    lifecycleOwner = lifecycleOwner,
                    cameraFacing = currentCameraFacing,
                    onCameraReady = { cam -> camera = cam }
                )
                
                // Camera Controls Overlay
                CameraControlsOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    currentCameraFacing = currentCameraFacing,
                    isFlashEnabled = isFlashEnabled,
                    onSwitchCamera = {
                        currentCameraFacing = if (currentCameraFacing == CameraFacing.BACK) {
                            CameraFacing.FRONT
                        } else {
                            CameraFacing.BACK
                        }
                        // Disable flash when switching to front camera
                        if (currentCameraFacing == CameraFacing.FRONT) {
                            isFlashEnabled = false
                            camera?.cameraControl?.enableTorch(false)
                        }
                    },
                    onToggleFlash = {
                        if (currentCameraFacing == CameraFacing.BACK) {
                            isFlashEnabled = !isFlashEnabled
                            camera?.cameraControl?.enableTorch(isFlashEnabled)
                        }
                    },
                    settings = settings
                )
            } else {
                // Permission not granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please grant camera permission to use the preview feature",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
            cameraProvider?.unbindAll()
        }
    }
}

@Composable
private fun CameraPreviewView(
    modifier: Modifier = Modifier,
    cameraProvider: ProcessCameraProvider?,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraFacing: CameraFacing,
    onCameraReady: (androidx.camera.core.Camera) -> Unit
) {
    val context = LocalContext.current
    
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    
    LaunchedEffect(cameraProvider, cameraFacing) {
        cameraProvider?.let { provider ->
            provider.unbindAll()
            
            val cameraSelector = when (cameraFacing) {
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            }
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
            
            try {
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
                onCameraReady(camera)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun CameraControlsOverlay(
    modifier: Modifier = Modifier,
    currentCameraFacing: CameraFacing,
    isFlashEnabled: Boolean,
    onSwitchCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    settings: com.example.hiddencam.domain.model.VideoSettings
) {
    Box(modifier = modifier) {
        // Top info card
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preview Mode",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${settings.resolution.displayName} • ${settings.frameRate}fps",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = "ISO: ${settings.isoMode.displayName} • Focus: ${settings.focusMode.displayName.split(" ")[0]}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
        
        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash toggle button
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFlashEnabled) MaterialTheme.colorScheme.primary
                        else Color.Black.copy(alpha = 0.5f)
                    )
            ) {
                Icon(
                    imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = if (currentCameraFacing == CameraFacing.BACK) Color.White else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Switch camera button
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Camera facing indicator
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = if (currentCameraFacing == CameraFacing.BACK) "📷 Back" else "🤳 Front",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
