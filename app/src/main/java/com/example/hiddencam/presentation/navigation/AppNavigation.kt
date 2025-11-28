package com.example.hiddencam.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hiddencam.domain.model.RecordingState
import com.example.hiddencam.domain.model.VideoSettings
import com.example.hiddencam.presentation.screens.home.HomeScreen
import com.example.hiddencam.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    recordingState: RecordingState,
    settings: VideoSettings,
    allPermissionsGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                recordingState = recordingState,
                settings = settings,
                allPermissionsGranted = allPermissionsGranted,
                onRequestPermissions = onRequestPermissions,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                settings = settings,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
