package com.example.hiddencam.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hiddencam.data.datastore.SecurityDataStore
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.presentation.screens.gallery.VideoGalleryScreen
import com.example.hiddencam.presentation.screens.home.HomeScreen
import com.example.hiddencam.presentation.screens.lock.PinLockScreen
import com.example.hiddencam.presentation.screens.preview.CameraPreviewScreen
import com.example.hiddencam.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object CameraPreview : Screen("camera_preview")
    object VideoGallery : Screen("video_gallery")
    object PinSetup : Screen("pin_setup")
    object PinLock : Screen("pin_lock")
}

@Composable
fun AppNavigation(
    recordingState: RecordingState,
    settings: VideoSettings,
    allPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    securityDataStore: SecurityDataStore,
    isAppUnlocked: Boolean,
    onAppUnlocked: () -> Unit
) {
    val navController = rememberNavController()
    val appLockEnabled by securityDataStore.appLockEnabled.collectAsState(initial = false)
    
    // Determine start destination based on lock status
    val startDestination = if (appLockEnabled && !isAppUnlocked) {
        Screen.PinLock.route
    } else {
        Screen.Home.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.PinLock.route) {
            PinLockScreen(
                securityDataStore = securityDataStore,
                isSetupMode = false,
                onSuccess = {
                    onAppUnlocked()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PinLock.route) { inclusive = true }
                    }
                },
                onNavigateBack = { /* Cannot go back from lock screen */ }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                recordingState = recordingState,
                settings = settings,
                allPermissionsGranted = allPermissionsGranted,
                onRequestPermissions = onRequestPermissions,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPreview = { navController.navigate(Screen.CameraPreview.route) },
                onNavigateToGallery = { navController.navigate(Screen.VideoGallery.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                settings = settings,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPinSetup = { navController.navigate(Screen.PinSetup.route) },
                securityDataStore = securityDataStore
            )
        }
        
        composable(Screen.CameraPreview.route) {
            CameraPreviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.VideoGallery.route) {
            VideoGalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PinSetup.route) {
            PinLockScreen(
                securityDataStore = securityDataStore,
                isSetupMode = true,
                onSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
