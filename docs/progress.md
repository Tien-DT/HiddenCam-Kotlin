# Hidden Camera App - Development Progress

## Project Overview
App Android quay video ?n t? camera tru?c/sau, h? tr? ch?y n?n và khi khóa màn hình.

## Completed Tasks

### 1.  Project Setup & Dependencies (Completed)
- Updated uild.gradle.kts with necessary dependencies:
  - CameraX (camera-core, camera2, lifecycle, video)
  - DataStore Preferences
  - Navigation Compose  
  - Hilt (Dependency Injection)
  - Lifecycle Service
  - Material Icons Extended
- Updated libs.versions.toml with all version catalogs
- Configured Kotlin 2.0.21 with Java 17

### 2.  Permissions Configuration (Completed)
- AndroidManifest.xml updated with:
  - CAMERA, RECORD_AUDIO
  - WRITE_EXTERNAL_STORAGE, READ_MEDIA_VIDEO
  - FOREGROUND_SERVICE, FOREGROUND_SERVICE_CAMERA, FOREGROUND_SERVICE_MICROPHONE
  - POST_NOTIFICATIONS
  - WAKE_LOCK (for recording when screen off)
  - RECEIVE_BOOT_COMPLETED

### 3.  Clean Architecture Structure (Completed)

#### Domain Layer
- **Models:**
  - VideoSettings.kt - Settings model (camera facing, resolution, fps, bitrate, audio source)
  - RecordingState.kt - Recording state sealed class (Idle, Starting, Recording, Paused, Stopping, Error)

- **Repository Interfaces:**
  - SettingsRepository.kt - Settings CRUD operations
  - VideoRecordingRepository.kt - Recording control operations

- **Use Cases:**
  - StartRecordingUseCase.kt
  - StopRecordingUseCase.kt
  - PauseRecordingUseCase.kt
  - ResumeRecordingUseCase.kt
  - GetRecordingStateUseCase.kt
  - GetSettingsUseCase.kt
  - UpdateSettingsUseCase.kt

#### Data Layer
- **Local Storage:**
  - SettingsDataStore.kt - DataStore Preferences implementation

- **Repositories:**
  - SettingsRepositoryImpl.kt - Settings repository implementation
  - VideoRecordingRepositoryImpl.kt - Recording repository implementation

- **Services:**
  - VideoRecordingService.kt - Foreground service for background recording

#### Presentation Layer
- **DI:**
  - RepositoryModule.kt - Hilt module for dependency injection

- **Navigation:**
  - AppNavigation.kt - Navigation component setup

- **Screens:**
  - **Home:**
    - HomeScreen.kt - Main recording screen UI
    - HomeViewModel.kt - Home screen ViewModel
  - **Settings:**
    - SettingsScreen.kt - Settings screen UI
    - SettingsViewModel.kt - Settings screen ViewModel

- **Application:**
  - HiddenCamApplication.kt - Application class with Hilt and notification channel
  - MainActivity.kt - Main activity with permission handling

### 4.  Features Implemented

#### Video Recording
- [x] Record from front or back camera
- [x] Background recording (foreground service)
- [x] Recording continues when screen is locked (WakeLock)
- [x] Pause/Resume recording
- [x] Stop recording
- [x] Save videos to Movies/HiddenCam folder

#### Settings
- [x] Camera selection (Front/Back)
- [x] Resolution (480p, 720p, 1080p, 4K)
- [x] Frame rate (24, 30, 60 fps)
- [x] Bitrate quality (Low, Medium, High, Ultra)
- [x] Audio source (None, Microphone, Camcorder)
- [x] Volume button control toggle
- [x] Power button control toggle

#### UI Features
- [x] Material 3 design
- [x] Recording status display
- [x] Duration timer with pulse animation
- [x] Quick tips card
- [x] Permission request handling

### 5.  In Progress / TODO

#### Hardware Button Controls
- [ ] Long press volume down to start recording (partially implemented)
- [ ] Double tap power button to stop recording (needs AccessibilityService)

#### Improvements Needed
- [ ] Test on physical device
- [ ] Add video gallery screen
- [ ] Add video playback
- [ ] Improve hardware button detection
- [ ] Add widget for quick recording

## File Structure
\\\
app/src/main/java/com/example/hiddencam/
 HiddenCamApplication.kt
 data/
    local/
       SettingsDataStore.kt
    repository/
       SettingsRepositoryImpl.kt
       VideoRecordingRepositoryImpl.kt
    service/
        VideoRecordingService.kt
 di/
    RepositoryModule.kt
 domain/
    model/
       RecordingState.kt
       VideoSettings.kt
    repository/
       SettingsRepository.kt
       VideoRecordingRepository.kt
    usecase/
        GetRecordingStateUseCase.kt
        GetSettingsUseCase.kt
        PauseRecordingUseCase.kt
        ResumeRecordingUseCase.kt
        StartRecordingUseCase.kt
        StopRecordingUseCase.kt
        UpdateSettingsUseCase.kt
 presentation/
    MainActivity.kt
    navigation/
       AppNavigation.kt
    screens/
        home/
           HomeScreen.kt
           HomeViewModel.kt
        settings/
            SettingsScreen.kt
            SettingsViewModel.kt
 ui/
     theme/
         Color.kt
         Theme.kt
         Type.kt
\\\

## Build & Run
1. Open project in Android Studio
2. Sync Gradle files
3. Build and run on device (physical device recommended for camera testing)
4. Grant all permissions when prompted

## Last Updated
- Date: 2025-11-28
- Status: Core functionality complete, needs testing on physical device
