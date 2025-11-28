# Hidden Camera App - Development Progress

## Project Overview
App Android quay video ẩn từ camera trước/sau, hỗ trợ chạy nền và khi khóa màn hình.

## Completed Tasks

### 1. Project Setup & Dependencies ✅
- Updated build.gradle.kts with necessary dependencies:
  - CameraX (camera-core, camera2, lifecycle, video) 1.4.0
  - DataStore Preferences 1.1.1
  - Navigation Compose 2.8.4
  - Hilt (Dependency Injection) 2.51.1
  - Lifecycle Service
  - Material Icons Extended
- Updated libs.versions.toml with all version catalogs
- Configured Kotlin 2.0.21 with Java 17
- compileSdk = 36

### 2. Permissions Configuration ✅
- AndroidManifest.xml updated with:
  - CAMERA, RECORD_AUDIO
  - WRITE_EXTERNAL_STORAGE, READ_MEDIA_VIDEO
  - FOREGROUND_SERVICE, FOREGROUND_SERVICE_CAMERA, FOREGROUND_SERVICE_MICROPHONE
  - POST_NOTIFICATIONS
  - WAKE_LOCK (for recording when screen off)
  - RECEIVE_BOOT_COMPLETED
  - **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS** *(NEW - xin quyền tắt battery optimization)*

### 3. Clean Architecture Structure ✅

#### Domain Layer
- **Models:**
  - VideoSettings.kt - Settings model (camera facing, resolution, fps, bitrate, audio source, **orientation, flashEnabled**)
  - RecordingState.kt - Recording state sealed class (Idle, Starting, Recording, Paused, Stopping, Error)
  - **VideoOrientation** enum - PORTRAIT ("Portrait (Dọc)"), LANDSCAPE ("Landscape (Ngang)")

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
  - UpdateSettingsUseCase.kt (added **setOrientation, setFlashEnabled**)

#### Data Layer
- **Local Storage:**
  - SettingsDataStore.kt - DataStore Preferences implementation
    - Added: ORIENTATION, FLASH_ENABLED keys

- **Repositories:**
  - SettingsRepositoryImpl.kt - Settings repository implementation
  - VideoRecordingRepositoryImpl.kt - Recording repository implementation

- **Services:**
  - VideoRecordingService.kt - Foreground service for background recording
    - ✅ **Hỗ trợ Flash/Torch** (back camera only)
    - ✅ **Hỗ trợ Video Orientation** (Portrait/Landscape) via targetRotation
    - ✅ **FallbackStrategy** cho QualitySelector (fix lỗi camera không hỗ trợ quality)
    - ✅ **Improved Notification** với thời gian quay, camera info
    - ✅ **Widget integration** - cập nhật widget khi recording state thay đổi

#### Presentation Layer
- **DI:**
  - RepositoryModule.kt - Hilt module for dependency injection

- **Navigation:**
  - AppNavigation.kt - Navigation component setup

- **Screens:**
  - **Home:**
    - HomeScreen.kt - Main recording screen UI
    - HomeViewModel.kt - Home screen ViewModel
    - **BatteryOptimizationCard** *(NEW)* - Warning card để xin quyền battery optimization
  - **Settings:**
    - SettingsScreen.kt - Settings screen UI
    - SettingsViewModel.kt - Settings screen ViewModel

- **Widget (NEW):**
  - RecordingWidgetReceiver.kt - Widget để quay video nhanh từ home screen
  - widget_recording.xml - Layout cho widget
  - recording_widget_info.xml - Widget metadata
  - widget_background.xml, widget_background_recording.xml - Widget backgrounds
  - ic_videocam.xml, ic_stop_recording.xml - Widget icons

- **Utilities (NEW):**
  - BatteryOptimizationHelper.kt - Helper để quản lý battery optimization

- **Application:**
  - HiddenCamApplication.kt - Application class with Hilt and notification channel
  - MainActivity.kt - Main activity with permission handling

### 4. Features Implemented

#### Video Recording
- [x] Record from front or back camera
- [x] Background recording (foreground service)
- [x] Recording continues when screen is locked (WakeLock)
- [x] Pause/Resume recording
- [x] Stop recording
- [x] Save videos to Movies/HiddenCam folder
- [x] **Flash/Torch support** *(NEW - back camera only)*
- [x] **Video orientation - Portrait/Landscape** *(NEW)*
- [x] **FallbackStrategy for unsupported video qualities** *(NEW - fix crash on some devices)*

#### Settings
- [x] Camera selection (Front/Back)
- [x] Resolution (480p, 720p, 1080p, 4K)
- [x] Frame rate (24, 30, 60 fps)
- [x] Bitrate quality (Low, Medium, High, Ultra)
- [x] Audio source (None, Microphone, Camcorder)
- [x] Volume button control toggle
- [x] Power button control toggle
- [x] **Video Orientation (Portrait/Landscape)** *(NEW)*
- [x] **Flash Light toggle** *(NEW)*
- [x] **App Icon Disguise** - 3 icons: X (default), Molecule (calculator), Gear (settings) *(NEW)*
- [x] **App Name Disguise** - 4 names: HiddenCam, Calculator, Notes, Settings *(NEW)*

#### UI Features
- [x] Material 3 design
- [x] Recording status display
- [x] Duration timer with pulse animation
- [x] Quick tips card
- [x] Permission request handling
- [x] **Improved notification với duration, camera info, stop button** *(NEW)*
- [x] **Battery optimization warning card** *(NEW)*

#### Background Optimization (NEW)
- [x] **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission**
- [x] **BatteryOptimizationHelper utility class**
- [x] **UI card để request battery optimization exemption**
- [x] **Link đến Battery settings để người dùng tự cấu hình**

#### Widget (NEW)
- [x] **Recording Widget** - tap để start/stop recording từ home screen
- [x] **Widget cập nhật với recording state và duration**
- [x] **Icon thay đổi khi đang quay (videocam ↔ stop)**
- [x] **Compact 1x1 Widget** - chỉ icon, không text *(UPDATED)*
- [x] **SharedPreferences state** - fix lỗi mất trạng thái khi process bị kill *(FIXED)*
- [x] **Circular widget design** - background oval thay vì rectangle *(UPDATED)*

### 5. In Progress / TODO

#### Hardware Button Controls
- [ ] Long press volume down to start recording (partially implemented)
- [ ] Double tap power button to stop recording (needs AccessibilityService)

#### Improvements Needed
- [ ] Test on physical device
- [ ] Add video gallery screen
- [ ] Add video playback
- [ ] Improve hardware button detection

## File Structure
```
app/src/main/java/com/example/hiddencam/
├── HiddenCamApplication.kt
├── data/
│   ├── local/
│   │   └── SettingsDataStore.kt
│   ├── repository/
│   │   ├── SettingsRepositoryImpl.kt
│   │   └── VideoRecordingRepositoryImpl.kt
│   └── service/
│       └── VideoRecordingService.kt
├── di/
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   ├── RecordingState.kt
│   │   └── VideoSettings.kt (+ VideoOrientation enum)
│   ├── repository/
│   │   ├── SettingsRepository.kt
│   │   └── VideoRecordingRepository.kt
│   └── usecase/
│       ├── GetRecordingStateUseCase.kt
│       ├── GetSettingsUseCase.kt
│       ├── PauseRecordingUseCase.kt
│       ├── ResumeRecordingUseCase.kt
│       ├── StartRecordingUseCase.kt
│       ├── StopRecordingUseCase.kt
│       └── UpdateSettingsUseCase.kt
├── presentation/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt (+ BatteryOptimizationCard)
│   │   │   └── HomeViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt (+ Orientation, Flash UI)
│   │       └── SettingsViewModel.kt
│   ├── util/
│   │   └── BatteryOptimizationHelper.kt (NEW)
│   └── widget/
│       └── RecordingWidgetReceiver.kt (NEW)
└── ui/
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt

app/src/main/res/
├── drawable/
│   ├── ic_videocam.xml (NEW)
│   ├── ic_stop_recording.xml (NEW)
│   ├── widget_background.xml (NEW)
│   └── widget_background_recording.xml (NEW)
├── layout/
│   └── widget_recording.xml (NEW)
├── values/
│   └── strings.xml (UPDATED with widget strings)
└── xml/
    └── recording_widget_info.xml (NEW)
```

## Recent Changes Log

### 2025-11-29 - Session 3: App Disguise & Widget Fix

#### New Features
1. **App Icon Disguise**
   - 3 icon options: X Logo (default), Molecule (calculator style), Gear (settings style)
   - Created vector drawables cho từng icon
   - Dùng `activity-alias` pattern trong AndroidManifest
   - `PackageManager.setComponentEnabledSetting()` để switch icon
   - Icons được tạo dạng adaptive icon (foreground + background)

2. **App Name Disguise**
   - 4 tên: HiddenCam, Calculator, Notes, Settings
   - Mỗi activity-alias có label riêng
   - Người dùng chọn trong Settings > App Disguise section

3. **Widget Fix & Compact Design**
   - **Fix bug**: Widget không record khi tap
   - **Root cause**: Static variable `isRecording` bị reset khi process killed
   - **Solution**: Dùng SharedPreferences ("widget_prefs") để lưu trạng thái
   - **Compact size**: 1x1 cell (56dp), chỉ icon không text
   - **Circular design**: Background oval thay vì rectangle
   - **Better logging**: Thêm Log.d statements để debug

#### Files Created
| File | Purpose |
|------|---------|
| ic_launcher_default.xml | X logo icon (drawable) |
| ic_launcher_molecule.xml | Molecule/calculator icon (drawable) |
| ic_launcher_gear.xml | Gear/settings icon (drawable) |
| mipmap-anydpi/ic_launcher_default.xml | Adaptive icon wrapper |
| mipmap-anydpi/ic_launcher_molecule.xml | Adaptive icon wrapper |
| mipmap-anydpi/ic_launcher_gear.xml | Adaptive icon wrapper |
| widget_icon_bg.xml | Circular background for widget icon |

#### Files Modified
| File | Changes |
|------|---------|
| VideoSettings.kt | + `AppIcon` enum, `AppName` enum, `appIcon`, `appName` fields |
| SettingsDataStore.kt | + APP_ICON, APP_NAME keys |
| SettingsRepository.kt | + `setAppIcon()`, `setAppName()` |
| SettingsRepositoryImpl.kt | + Implementation |
| UpdateSettingsUseCase.kt | + `setAppIcon()`, `setAppName()` |
| SettingsViewModel.kt | + Icon switching with PackageManager |
| SettingsScreen.kt | + "App Disguise" section với dropdowns |
| AndroidManifest.xml | + 3 activity-alias (MainActivityDefault, MainActivityMolecule, MainActivityGear) |
| RecordingWidgetReceiver.kt | **Rewrite**: SharedPreferences instead of static, better logging |
| widget_recording.xml | Simplified to FrameLayout + ImageView only |
| recording_widget_info.xml | 1x1 cell (56dp), resizeMode="none" |
| widget_background.xml | Changed to oval shape |
| widget_background_recording.xml | Changed to oval shape |
| VideoRecordingRepositoryImpl.kt | + SettingsDataStore injection, `getSettings()` |
| VideoRecordingService.kt | + Load settings in onStartCommand for widget starts |
| colors.xml | + launcher_background colors |
| strings.xml | + app_name_calculator, app_name_notes, app_name_settings |

### 2025-11-28 - Session 2: Feature Enhancements

#### Bug Fixes
1. **Fix Camera QualitySelector Error**
   - Vấn đề: Crash "Unable to find supported quality by QualitySelector" trên một số thiết bị
   - Giải pháp: Thêm `FallbackStrategy.higherQualityOrLowerThan(Quality.SD)` và kiểm tra supportedQualities

2. **Fix Notification Display**
   - Vấn đề: Notification không hiển thị đúng thông tin
   - Giải pháp: Cải thiện notification với duration, camera info, chronometer

#### New Features
1. **Video Orientation Setting**
   - Thêm enum `VideoOrientation` (PORTRAIT, LANDSCAPE)
   - UI dropdown trong Settings
   - Service áp dụng `targetRotation` khi quay

2. **Flash Light Setting**
   - Thêm toggle `flashEnabled` trong Settings
   - Service gọi `camera.cameraControl.enableTorch()` (chỉ back camera)

3. **Battery Optimization Request**
   - Permission: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
   - Helper class: `BatteryOptimizationHelper.kt`
   - UI: `BatteryOptimizationCard` trong HomeScreen

4. **Recording Widget**
   - `RecordingWidgetReceiver.kt` extends `AppWidgetProvider`
   - Layout: `widget_recording.xml`
   - Config: `recording_widget_info.xml`
   - Backgrounds: `widget_background.xml`, `widget_background_recording.xml`
   - Icons: `ic_videocam.xml`, `ic_stop_recording.xml`

5. **Improved Notifications**
   - Hiển thị: "🔴 Recording Video"
   - Duration với chronometer
   - Camera info: "BACK • 720p (HD)"
   - Stop button action

#### Files Modified
| File | Changes |
|------|---------|
| VideoSettings.kt | + `orientation: VideoOrientation`, `flashEnabled: Boolean` |
| SettingsDataStore.kt | + ORIENTATION, FLASH_ENABLED keys |
| SettingsRepository.kt | + `setOrientation()`, `setFlashEnabled()` |
| SettingsRepositoryImpl.kt | + Implementation |
| UpdateSettingsUseCase.kt | + `setOrientation()`, `setFlashEnabled()` |
| SettingsViewModel.kt | + UI binding methods |
| SettingsScreen.kt | + Orientation dropdown, Flash toggle |
| VideoRecordingService.kt | + Flash control, orientation, notification, widget |
| HomeScreen.kt | + BatteryOptimizationCard |
| AndroidManifest.xml | + Battery permission, widget receiver |

#### Files Created
| File | Purpose |
|------|---------|
| BatteryOptimizationHelper.kt | Battery optimization utilities |
| RecordingWidgetReceiver.kt | Widget AppWidgetProvider |
| recording_widget_info.xml | Widget metadata |
| widget_recording.xml | Widget layout |
| widget_background.xml | Widget normal background |
| widget_background_recording.xml | Widget recording background |
| ic_videocam.xml | Widget start icon |
| ic_stop_recording.xml | Widget stop icon |

### 2025-11-28 - Session 1: Initial Development
- Tạo project structure với Clean Architecture
- Implement tất cả layers: Domain, Data, Presentation
- Setup CameraX, Hilt, DataStore, Navigation
- Tạo UI với Material 3

## Build & Run
1. Open project in Android Studio
2. Sync Gradle files
3. Build and run on device (physical device recommended for camera testing)
4. Grant all permissions when prompted
5. **Khuyến nghị: Cho phép battery optimization exemption để chạy nền tốt hơn**

## Known Issues
- Hardware button detection cần AccessibilityService cho power button
- gradle-wrapper.jar có thể bị thiếu, cần chạy `gradle wrapper` để tạo lại

## Last Updated
- **Date:** 2025-11-29
- **Status:** Core functionality complete + App Disguise (icon/name switching) + Widget fix
- **Next:** Test trên physical device, fix remaining issues
